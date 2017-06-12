package pl.wsiz.przepisykulinarne.other;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;

import pl.wsiz.przepisykulinarne.Db.DbHelper;

public class DownloadManager {

    private static RequestQueue requestQueue;

    public static final String RECIPES = "recipes_receiver";
    public static final String CATEGORIES = "categories_receiver";

    private int queueItems = 0;
    private int newRecipes = 0;
    private int newCategories = 0;

    private Context ctx;
    private DbHelper dbHelper;

    public DownloadManager(Context ctx) {
        this.ctx = ctx;
        dbHelper = new DbHelper(ctx);
    }

    private RequestQueue getRequestQueue() {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(ctx);
        return requestQueue;
    }

    public void getAllRecipes(final int version) {

        final JsonObjectRequest recipeRequest = new JsonObjectRequest
                (Request.Method.GET, String.format(Config.API_URL_RECIPES, version), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray jRecipes = response.optJSONArray("GetAllRecipesResult");

                        if (jRecipes != null) {
                            for (int recipeIndex = 0; recipeIndex < jRecipes.length(); recipeIndex++) {
                                JSONObject jRecipe;

                                try {
                                    jRecipe = (JSONObject) jRecipes.get(recipeIndex);
                                } catch (JSONException ex) {
                                    continue;
                                }

                                Recipe recipe = new Recipe();
                                recipe.RecipeId = jRecipe.optInt("Id", -1);
                                recipe.Name = jRecipe.optString("Name");
                                recipe.Description = jRecipe.optString("Description");
                                recipe.Photo = jRecipe.optString("Photo");

                                JSONArray jRecipeCategories = jRecipe.optJSONArray("Categories");

                                if (jRecipeCategories != null) {
                                    for (int catIndex = 0; catIndex < jRecipes.length(); catIndex++) {
                                        int categoryId;

                                        try {
                                            categoryId = jRecipeCategories.getInt(catIndex);
                                        } catch (JSONException ex) {
                                            continue;
                                        }

//                                        recipe.Categories.add(categoryId);

                                        //TODO budowanie inserta do bazy

                                        dbHelper.addRecipeCategory(recipe.RecipeId, categoryId);
                                    }
                                }

                                try {
                                    recipe.ModificationDate = Utils.parseDate(jRecipe.optString("ModificationDate"));
                                } catch (ParseException ex) {

                                }

                                recipe.Version = jRecipe.optInt("Version", -1);

                                String Modifier = jRecipe.optString("Modifier");
                                switch (Modifier) {
                                    case "m":
                                        dbHelper.addRecipe(recipe);
                                        break;
                                    case "d":
                                        dbHelper.removeRecipe(recipe.RecipeId);
                                        dbHelper.removeRecipeCategory(recipe.RecipeId);
                                        break;
                                }
                            }
                        }

                        newRecipes = jRecipes != null ? jRecipes.length() : 0;

                        queueDecrement();

                        Intent recipeIntent = new Intent();
                        recipeIntent.setAction(RECIPES);
                        recipeIntent.putExtra(RECIPES, newRecipes);
                        ctx.sendBroadcast(recipeIntent);

                        Log.e("Pobieranie", "Pobrano " + newRecipes + " przepisow (v " + version + ")");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        queueDecrement();

                        newRecipes = 0;
                    }
                });

        recipeRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 4, 1.0f));
        requestQueueAdd(recipeRequest);
    }

    public void getAllCategories(final int version) {

        JsonObjectRequest categoryRequest = new JsonObjectRequest
                (Request.Method.GET, String.format(Config.API_URL_CATEGORIES, version), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        JSONArray jCategories = response.optJSONArray("GetAllCategoriesResult");

                        if (jCategories != null) {
                            for (int catIndex = 0; catIndex < jCategories.length(); catIndex++) {
                                JSONObject jCategory;

                                try {
                                    jCategory = (JSONObject) jCategories.get(catIndex);
                                } catch (JSONException ex) {
                                    continue;
                                }

                                Category category = new Category();
                                category.CategoryId = jCategory.optInt("Id", -1);
                                category.Name = jCategory.optString("Name");
                                category.Version = jCategory.optInt("Version", -1);

                                String Modifier = jCategory.optString("Modifier");
                                switch (Modifier) {
                                    case "m":
                                        dbHelper.addCategory(category);
                                        break;
                                    case "d":
                                        dbHelper.removeCategory(category.CategoryId);
                                        break;
                                }
                            }
                        }

                        newCategories = jCategories != null ? jCategories.length() : 0;

                        queueDecrement();

                        Log.e("Pobieranie", "Pobrano " + newCategories + " kategorii (v " + version + ")");
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        queueDecrement();

                        newCategories = 0;
                    }
                });

        categoryRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 4, 1.0f));
        requestQueueAdd(categoryRequest);
    }

    private void requestQueueAdd(JsonObjectRequest jsonObjectRequest) {
        queueItems++;
        getRequestQueue().add(jsonObjectRequest);
    }

    private void queueDecrement() {
        queueItems--;

        if(queueItems < 1) {
            // wyslanie broadcasta dla kategorii dopiero jak zostana pobrane przepisy
            // musi tak byc po kategorie potrzebuja wypelnionej tabeli RecipesCategories
            Intent categoryIntent = new Intent();
            categoryIntent.setAction(CATEGORIES);
            categoryIntent.putExtra(CATEGORIES, newCategories);
            categoryIntent.putExtra(RECIPES, newRecipes);
            ctx.sendBroadcast(categoryIntent);
        }
    }
}