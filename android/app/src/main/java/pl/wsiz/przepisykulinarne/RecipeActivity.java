package pl.wsiz.przepisykulinarne;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.other.Config;
import pl.wsiz.przepisykulinarne.other.Recipe;


public class RecipeActivity extends ActionBarActivity {

    private Recipe rec = null;
    private boolean random = false;
    private Menu menu;
    private boolean isMenuReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        int id = getIntent().getExtras().getInt("RecipeId");
        random = getIntent().getExtras().getBoolean("Random");
        loadRecipe(id);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        getMenuInflater().inflate(R.menu.recipe_menu, menu);
        this.menu = menu;
        if (dbHelper.isFavorite(rec.RecipeId))
        {
            MenuItem item = menu.findItem(R.id.menu_item_favorite);
            item.setIcon(R.drawable.ic_star_white_full);
        }
        MenuItem randomItem = menu.findItem(R.id.menu_item_random);
        if(random) randomItem.setVisible(true);
        isMenuReady = true;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_favorite)
        {
            //add
            DbHelper db = new DbHelper(getApplicationContext());
            boolean isFavorite = db.isFavorite(rec.RecipeId);
            if(!isFavorite)
            {
                db.addToFavorites(rec.RecipeId);
                item.setIcon(R.drawable.ic_star_white_full);
                Toast.makeText(getApplicationContext(), getString(R.string.favoritesAdded), Toast.LENGTH_SHORT).show();
            }
            else
            {
                db.removeFromFavorites(rec.RecipeId);
                item.setIcon(R.drawable.ic_star_white_empty);
                Toast.makeText(getApplicationContext(), getString(R.string.favoritesRemoved), Toast.LENGTH_SHORT).show();
            }
        }

        if (id == R.id.menu_item_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, rec.Name);
            String shareContent = rec.Name + "\n\n" + rec.Description + "\n\n" + getString(R.string.shareFooter);
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareContent);
            startActivity(Intent.createChooser(sharingIntent, getString(R.string.shareName)));
            return true;
        }

        if (id == R.id.menu_item_random)
        {
            DbHelper dbHelper = new DbHelper(getApplicationContext());
            int randomId = dbHelper.getRandomRecipeId();
            loadRecipe(randomId);
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadRecipe(int id)
    {
        DbHelper dbHelper = new DbHelper(getApplicationContext());
        ImageView img = (ImageView) findViewById(R.id.image);
        rec = dbHelper.getRecipe(id);
        TextView tv = (TextView) findViewById(R.id.text);
        TextView tvRecipeTitle = (TextView) findViewById(R.id.tvRecipeTitle);
        if(rec != null) {
            tvRecipeTitle.setText(rec.Name);
            tv.setText(rec.Description);
            setTitle("");

            ImageLoader.getInstance().displayImage(String.format(Config.URL_PHOTOS, rec.Photo), img, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

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
        }
        if (isMenuReady) {
            MenuItem item = menu.findItem(R.id.menu_item_favorite);
            if (dbHelper.isFavorite(rec.RecipeId)) {
                item.setIcon(R.drawable.ic_star_white_full);
            } else item.setIcon(R.drawable.ic_star_white_empty);
        }
    }
}
