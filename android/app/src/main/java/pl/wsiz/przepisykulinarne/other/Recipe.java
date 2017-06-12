package pl.wsiz.przepisykulinarne.other;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Recipe {
    public int RecipeId;
    public String Name;
    public String Description;
    public List<Category> Categories = new ArrayList<>();
    public String Photo;
    public Date ModificationDate;
    public int Version;

    public int ocena;
}
