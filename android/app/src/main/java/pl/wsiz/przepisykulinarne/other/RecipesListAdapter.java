package pl.wsiz.przepisykulinarne.other;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.List;

import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.R;

public class RecipesListAdapter extends BaseAdapter {

    private Context ctx;
    private List<Recipe> recipeList;
    private LayoutInflater inflater;

    public RecipesListAdapter(Context ctx, boolean favorites, String searchQuery, String CategoryId) {
        this.ctx = ctx;
        recipeList = new DbHelper(ctx).getFilteredRecipes(favorites, searchQuery, CategoryId);
    }

    public void update(boolean favorites, String searchQuery, String CategoryId) {
        recipeList = new DbHelper(ctx).getFilteredRecipes(favorites, searchQuery, CategoryId);
    }

    @Override
    public int getCount() {
        return recipeList.size();
    }

    @Override
    public Recipe getItem(int position) {
        return recipeList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).RecipeId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.lv_item_recipe, null);

        final ImageView imgRecipe = (ImageView) convertView.findViewById(R.id.imgRecipe);
        TextView tvRecipe = (TextView) convertView.findViewById(R.id.tvRecipeTitle);

        Recipe recipe = getItem(position);

        ImageLoader.getInstance().displayImage(String.format(Config.URL_PHOTOS, recipe.Photo), imgRecipe, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imgRecipe.setImageResource(R.drawable.no_image);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });

        tvRecipe.setText(recipe.Name);

        return convertView;
    }
}
