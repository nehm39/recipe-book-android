using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.Text;
using PrzepisyService.Data;
using PrzepisyService.Db;
using PrzepisyService.Interfaces;

namespace PrzepisyService
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the class name "Api" in code, svc and config file together.
    // NOTE: In order to launch WCF Test Client for testing this service, please select Api.svc or Api.svc.cs at the Solution Explorer and start debugging.
    public class Api : IApi
    {
        private Ado db = new Ado();

        public Api()
        {
            //Console.WriteLine("Api constructor");
        }

        public Recipe GetRecipe(string apiVersion, string recipeId)
        {
            throw new NotImplementedException();
        }


        public List<Recipe> GetAllRecipes(string apiVersion)
        {
            return db.GetRecipes(apiVersion);
        }


        public List<Category> GetAllCategories(string apiVersion)
        {
            return db.GetCategories(apiVersion);
        }
    }
}
