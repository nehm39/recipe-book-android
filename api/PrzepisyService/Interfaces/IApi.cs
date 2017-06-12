using System.Collections.Generic;
using System.Runtime.Serialization;
using System.ServiceModel;
using System.ServiceModel.Web;
using PrzepisyService.Data;

namespace PrzepisyService.Interfaces
{
    // NOTE: You can use the "Rename" command on the "Refactor" menu to change the interface name "IApi" in both code and config file together.
    [ServiceContract]
    public interface IApi
    {
        [OperationContract]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Wrapped,
            UriTemplate = "json/{apiVersion}/GetRecipe/{recipeId}")]
        [return: MessageParameter(Name = "Recipe")]
        Recipe GetRecipe(string apiVersion, string recipeId);

        [OperationContract]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Wrapped,
            UriTemplate = "json/{apiVersion}/GetAllRecipes")]
        List<Recipe> GetAllRecipes(string apiVersion);

        [OperationContract]
        [WebInvoke(Method = "GET",
            ResponseFormat = WebMessageFormat.Json,
            BodyStyle = WebMessageBodyStyle.Wrapped,
            UriTemplate = "json/{apiVersion}/GetAllCategories")]
        List<Category> GetAllCategories(string apiVersion);
    }
}
