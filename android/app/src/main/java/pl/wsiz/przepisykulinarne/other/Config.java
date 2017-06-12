package pl.wsiz.przepisykulinarne.other;

import android.content.Context;

import java.io.File;

public class Config {
    public static final String DATABASE_NAME = "przepisy.db";
    public static final int DATABASE_VERSION = 1;

    public static final String URL = "http://przepisy-001-site1.smarterasp.net/";
    public static final String URL_PHOTOS = URL + "photos/%s";
    public static final String API_URL = URL + "Api.svc/json/";
    public static final String API_URL_RECIPES = API_URL + "%s/GetAllRecipes";
    public static final String API_URL_CATEGORIES = API_URL + "%s/GetAllCategories";


    public static File getDatabasePath(Context ctx) {
        return ctx.getDatabasePath(DATABASE_NAME);
    }
}
