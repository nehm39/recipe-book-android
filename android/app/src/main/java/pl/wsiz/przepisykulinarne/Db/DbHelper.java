package pl.wsiz.przepisykulinarne.Db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pl.wsiz.przepisykulinarne.other.Category;
import pl.wsiz.przepisykulinarne.other.Config;
import pl.wsiz.przepisykulinarne.other.Recipe;
import pl.wsiz.przepisykulinarne.other.Utils;

public class DbHelper extends SQLiteOpenHelper {

    public final static String CATEGORIES_TABLE_NAME = "Categories";
    private final static String CATEGORIES_SQL_CREATE = "CREATE TABLE \"" + CATEGORIES_TABLE_NAME + "\" (\"CategoryId\" INTEGER PRIMARY KEY  NOT NULL , \"Name\" TEXT NOT NULL , \"Version\" INTEGER NOT NULL )";

    public final static String RECIPES_TABLE_NAME = "Recipes";
    private final static String RECIPES_SQL_CREATE = "CREATE TABLE \"" + RECIPES_TABLE_NAME + "\" (\"RecipeId\" INTEGER PRIMARY KEY  NOT NULL , \"Name\" TEXT NOT NULL , \"Description\" TEXT, \"Photo\" TEXT, \"ModificationDate\" DATETIME, \"Version\" INTEGER)";

    public final static String FAVORITES_TABLE_NAME = "Favorites";
    private final static String FAVORITES_SQL_CREATE = "CREATE TABLE \"" + FAVORITES_TABLE_NAME + "\" (\"FavoriteId\" INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL , \"RecipeId\" INTEGER NOT NULL )";

    public final static String RECIPES_CATEGORIES_TABLE_NAME = "RecipesCategories";
    private final static String RECIPES_CATEGORIES_SQL_CREATE = "CREATE TABLE \"" + RECIPES_CATEGORIES_TABLE_NAME + "\" (\"RecipeId\" INTEGER, \"CategoryId\" INTEGER)";

    Random rand;

    public DbHelper(Context context) {
        super(context, Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        rand = new Random();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CATEGORIES_SQL_CREATE);
        db.execSQL(RECIPES_SQL_CREATE);
        db.execSQL(FAVORITES_SQL_CREATE);
        db.execSQL(RECIPES_CATEGORIES_SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public int getLastVersion(String tableName) {
        if(!TextUtils.isEmpty(tableName)) {
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT version FROM " + tableName + " ORDER BY Version DESC LIMIT 1;", null);
            cursor.moveToFirst();
            int version = 0;
            while (!cursor.isAfterLast()) {
                version = cursor.getInt(0);
                cursor.moveToNext();
            }
            cursor.close();
            db.close();

            return version;
        }

        return 0;
    }

    //region Favorites
    public boolean isFavoriteExisting() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + FAVORITES_TABLE_NAME + "'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                db.close();
                return true;
            }
            cursor.close();
            db.close();
        }
        return false;
    }

    public void addToFavorites(int id) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("RecipeId", id);

        db.replace(FAVORITES_TABLE_NAME, null, values);

        db.close();
    }

    public void removeFromFavorites(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(FAVORITES_TABLE_NAME, "RecipeId = " + id, null);

        db.close();
    }

    public boolean isFavorite(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM '" + FAVORITES_TABLE_NAME + "' WHERE RecipeId = " + id;
        Cursor cursor = db.rawQuery(query, null);
        boolean returnValue = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return returnValue;
    }

    public boolean areThereAnyFavorites() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM '" + FAVORITES_TABLE_NAME + "'";
        Cursor cursor = db.rawQuery(query, null);
        boolean returnValue = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return returnValue;
    }
    //endregion

    //region Recipes
    public void addRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("RecipeId", recipe.RecipeId);
        values.put("Name", recipe.Name);
        values.put("Description", recipe.Description);
        values.put("Photo", recipe.Photo);
        if(recipe.ModificationDate != null)
            values.put("ModificationDate", Utils.dateToString(recipe.ModificationDate));
        values.put("Version", recipe.Version);

        db.replace(RECIPES_TABLE_NAME, null, values);

        db.close();
    }

    public void removeRecipe(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(RECIPES_TABLE_NAME, "RecipeId = " + id, null);

        db.close();
    }

    public int getMaxRecipeId() {
        SQLiteDatabase db = getReadableDatabase();
        final SQLiteStatement stmt = db.compileStatement("SELECT MAX(RecipeId) FROM " + RECIPES_TABLE_NAME);
        int id = (int) stmt.simpleQueryForLong();
        stmt.close();
        db.close();
        return id;
    }

    public boolean doRecipeExist(int id) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + RECIPES_TABLE_NAME + " WHERE RecipeId = " + id;
        Cursor cursor = db.rawQuery(query, null);
        boolean returnValue = cursor.getCount() > 0;

        cursor.close();
        db.close();

        return returnValue;
    }

    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(RECIPES_TABLE_NAME, new String[]{"RecipeId", "Name", "Description", "Photo", "ModificationDate", "Version"},
                "RecipeId = ?", new String[]{String.valueOf(id)}, null, null, null);
        cursor.moveToFirst();
        Recipe recipe = null;
        while (!cursor.isAfterLast()) {
            recipe = getRecipe(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipe;
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(RECIPES_TABLE_NAME, new String[]{"RecipeId", "Name", "Description", "Photo", "ModificationDate", "Version"},
                null, null, null, null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            recipeList.add(getRecipe(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipeList;
    }

    public List<Recipe> getAllFavorites() {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT RecipeId, Name, Description, Photo, ModificationDate, Version FROM " + RECIPES_TABLE_NAME + " NATURAL JOIN " + FAVORITES_TABLE_NAME + " ORDER BY Name", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            recipeList.add(getRecipe(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipeList;
    }

    public List<Integer> getFavoritesIds()
    {
        List<Integer> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT RecipeId FROM " + FAVORITES_TABLE_NAME + " ORDER BY RecipeId", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            list.add(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return list;
    }

    public void importFavorites(List<Integer> list)
    {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM "+ FAVORITES_TABLE_NAME);
        db.close();
        for(Integer x : list)
        {
            addToFavorites(x);
        }
    }

    public List<Recipe> getFilteredRecipes(boolean favorites, String searchQuery, String CategoryId) {

        if(!favorites && TextUtils.isEmpty(searchQuery) && TextUtils.isEmpty(CategoryId))
            return getAllRecipes();

        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT r.RecipeId, r.Name, r.Description, r.Photo, r.ModificationDate, r.Version FROM Recipes AS r %s%sORDER BY r.Name";
        String join = "";
        String where = "";

        if(favorites) {
            join += "INNER JOIN Favorites AS f ON f.RecipeId = r.RecipeId ";
        }
        if(!TextUtils.isEmpty(CategoryId) && !CategoryId.equals("0")) {
            if(join.isEmpty())
                join += " ";
            join += "INNER JOIN RecipesCategories AS rc ON rc.RecipeId = r.RecipeId ";

            where += "WHERE rc.CategoryId = " + CategoryId + " ";
        }
        if(!TextUtils.isEmpty(searchQuery)) {
            if(where.isEmpty())
                where += "WHERE ";
            else
                where += "AND ";
            where += "(r.Name LIKE '%" + searchQuery + "%' OR r.Description LIKE '%" + searchQuery + "%') ";
        }

        Cursor cursor = db.rawQuery(String.format(query, join, where), null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            recipeList.add(getRecipe(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipeList;
    }

    public List<Recipe> getSearchedRecipes(String name) {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT RecipeId, Name, Description, Photo, ModificationDate, Version FROM " + RECIPES_TABLE_NAME + " WHERE Name LIKE '%" + name + "%' OR Description LIKE '%" + name + "%' ORDER BY Name", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            recipeList.add(getRecipe(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipeList;
    }

    public List<Recipe> getSearchedFavorites(String name) {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT RecipeId, Name, Description, Photo, ModificationDate, Version FROM " + RECIPES_TABLE_NAME + " NATURAL JOIN " + FAVORITES_TABLE_NAME + " WHERE Name LIKE '%" + name + "%' OR Description LIKE '%" + name + "%' ORDER BY Name", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            recipeList.add(getRecipe(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return recipeList;
    }

    public int getRandomRecipeId()
    {
        int maxId = getMaxRecipeId();
        int returnId = 0;
        if(maxId < 1) {
            return -1;
        }
        do {
            returnId = rand.nextInt(maxId) + 1;
        }
        while (!doRecipeExist(returnId));
        return returnId;
    }

    private Recipe getRecipe(Cursor cursor) {
        Recipe recipe = new Recipe();
        recipe.RecipeId = cursor.getInt(0);
        recipe.Name = cursor.getString(1);
        recipe.Description = cursor.getString(2);
        recipe.Photo = cursor.getString(3);

        try {
            recipe.ModificationDate = Utils.parseDate(cursor.getString(4));
        } catch(Exception ex) {

        }

        recipe.Version = cursor.getInt(5);

        return recipe;
    }
    //endregion

    //region Categories

    public String getCategoryName(int id)
    {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT Name FROM " + CATEGORIES_TABLE_NAME + " WHERE CategoryId = " + id, null);
        cursor.moveToFirst();
        return cursor.getString(0);
    }

    public void addCategory(Category category) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("CategoryId", category.CategoryId);
        values.put("Name", category.Name);
        values.put("Version", category.Version);

        db.replace(CATEGORIES_TABLE_NAME, null, values);

        db.close();
    }

    public void removeCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(CATEGORIES_TABLE_NAME, "CategoryId = " + id, null);

        db.close();
    }

    public List<Category> getAllCategories() {
        List<Category> categoryList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT c.CategoryId, c.Name, c.Version, count(rc.RecipeId) FROM " + CATEGORIES_TABLE_NAME + " AS c LEFT JOIN " + RECIPES_CATEGORIES_TABLE_NAME + " AS rc ON rc.CategoryId = c.CategoryId GROUP BY c.CategoryId, c.Name ORDER BY c.Name", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            categoryList.add(getCategory(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return categoryList;
    }

    private Category getCategory(Cursor cursor) {
        Category category = new Category();
        category.CategoryId = cursor.getInt(0);
        category.Name = cursor.getString(1);
        category.Version = cursor.getInt(2);
        category.itemsCount = cursor.getInt(3);

        return category;
    }
    //endregion

    //region RecipesCategories
    public void addRecipeCategory(int RecipeId, int CategoryId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("RecipeId", RecipeId);
        values.put("CategoryId", CategoryId);

        db.replace(RECIPES_CATEGORIES_TABLE_NAME, null, values);

        db.close();
    }

    public void removeRecipeCategory(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(RECIPES_CATEGORIES_TABLE_NAME, "RecipeId = " + id, null);

        db.close();
    }
    //endregion

}