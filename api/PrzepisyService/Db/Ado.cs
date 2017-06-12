using System;
using System.Collections.Generic;
using System.Data.SqlClient;
using System.Linq;
using System.Web;
using PrzepisyService.Data;

namespace PrzepisyService.Db
{
    public class Ado
    {
        public static string GetConnectionString()
        {
            return System.Configuration.ConfigurationManager.ConnectionStrings["connectionString"].ToString();
        }

        public List<Recipe> GetRecipes(string version)
        {
            List<Recipe> recipes = new List<Recipe>();
            using (SqlConnection sqlConnection = new SqlConnection(GetConnectionString()))
            {
                sqlConnection.Open();
                SqlCommand cmd = new SqlCommand { Connection = sqlConnection };
                cmd.CommandText = @"SELECT r.RecipeId, Name, Description, CategoryId, Photo, ModificationDate, Version
                                                    FROM Recipes r
                                                    LEFT JOIN RecipesCategories rc
                                                    ON r.RecipeId = rc.RecipeId
                                                    WHERE Version > @Version";
                //cmd.CommandText = @"SELECT * FROM Recipes WHERE Version > @Version";
                cmd.Parameters.Add(new SqlParameter("@Version", version));

                SqlDataReader dr = cmd.ExecuteReader();
                while (dr.Read())
                {
                    Recipe recipe = new Recipe();
                    recipe.Id = Convert.ToInt32(dr[0]);
                    recipe.Categories = new List<int>();

                    int category = 0;
                    try
                    {
                        category = Convert.ToInt32(dr[3]);
                    }
                    catch (InvalidCastException)
                    {
                    }

                    if (category != 0)
                    {
                        if(recipes.Count > 0)
                        {
                            Recipe prevRecipe = recipes[recipes.Count - 1];
                            if (prevRecipe.Equals(recipe))
                            {
                                prevRecipe.Categories.Add(category);
                                continue;
                            }
                        }

                        recipe.Categories.Add(category);
                    }

                    recipe.Name = Convert.ToString(dr[1]);
                    recipe.Description = Convert.ToString(dr[2]);

                    if (string.IsNullOrEmpty(recipe.Name) && string.IsNullOrEmpty(recipe.Description))
                        recipe.Modifier = 'd';
                    else
                        recipe.Modifier = 'm';

                    recipe.Photo = Convert.ToString(dr[4]);
                    DateTime dt = Convert.ToDateTime(dr[5]);
                    recipe.ModificationDate = dt.ToString("yyyy-MM-dd HH:mm:ss");
                    recipe.Version = Convert.ToInt32(dr[6]);

                    recipes.Add(recipe);
                }
                dr.Close();
            }
            return recipes;
        }

        public List<Category> GetCategories(string version)
        {
            List<Category> categories = new List<Category>();
            using (SqlConnection sqlConnection = new SqlConnection(GetConnectionString()))
            {
                sqlConnection.Open();
                SqlCommand cmd = new SqlCommand { Connection = sqlConnection };
                cmd.CommandText = "SELECT * FROM Categories WHERE Version > @Version";
                cmd.Parameters.Add(new SqlParameter("@Version", version));

                SqlDataReader dr = cmd.ExecuteReader();
                while (dr.Read())
                {
                    Category category = new Category();
                    category.Id = Convert.ToInt32(dr[0]);
                    category.Name = Convert.ToString(dr[1]);
                    category.Modifier = string.IsNullOrEmpty(category.Name) ? 'd' : 'm';
                    category.Version = Convert.ToInt32(dr[2]);

                    categories.Add(category);
                }
                
                dr.Close();
            }
            return categories;
        }

        private List<int> GetCategoryIds(SqlConnection sqlConnection, int recipeId)
        {
            List<int> idList = new List<int>();

            SqlCommand cmd = new SqlCommand { Connection = sqlConnection };
            cmd.CommandText = "SELECT CategoryId FROM RecipesCategories WHERE RecipeId = @recipeId";
            cmd.Parameters.Add(new SqlParameter("@recipeId", recipeId));

            SqlDataReader dr = cmd.ExecuteReader();
            while (dr.Read())
            {
                idList.Add(Convert.ToInt32(dr[0]));
            }
            dr.Close();

            return idList;
        }
    }
}