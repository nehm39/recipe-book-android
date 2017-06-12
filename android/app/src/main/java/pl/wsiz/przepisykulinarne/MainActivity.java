package pl.wsiz.przepisykulinarne;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.other.DownloadManager;
import pl.wsiz.przepisykulinarne.other.DrawerAdapter;
import pl.wsiz.przepisykulinarne.other.DrawerItems;
import pl.wsiz.przepisykulinarne.other.Utils;


public class MainActivity extends ActionBarActivity {
    private static final String KEY_FIRST_DOWNLOAD = "firstDownload";
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private boolean backPressed;
    static final String BUNDLE_FRAGMENTID = "";
    private int lastFragmentPosition = -1;
    private int fragmentPosition = -1;
    private ImageView drawerIcon = null;
    private boolean makeSelection = false;
    private int[] drawerIcons = {R.drawable.kategorie, R.drawable.przepisy, R.drawable.ulubione, R.drawable.losuj, R.drawable.ustawienia};
    private int[] drawerIconsSelected = {R.drawable.kategorie_selected, R.drawable.przepisy_selected, R.drawable.ulubione_selected, R.drawable.losuj_selected, R.drawable.ustawienia_selected};
    private String[] fragmentsNames = {"Kategorie", "Przepisy", "Ulubione", "Losuj", "Ustawienia"};
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final boolean automaticUpdate = PreferencesFragment.getBoolSetting(getApplicationContext(), PreferencesFragment.AUTOMATIC_UPDATE, true);
        if(Utils.getSharedPreferencesBool(getApplication(), KEY_FIRST_DOWNLOAD, true)) {
            registerReceiver(categoriesReceiver, new IntentFilter(DownloadManager.CATEGORIES));

            if(!Utils.isConnected(getApplication())) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle(getString(R.string.no_connection));
                alertDialogBuilder
                        .setMessage("Połącz się z internetem i naciśnij \"OK\".")
                        .setCancelable(false)
                        .setPositiveButton("OK", null)
                        .setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(final DialogInterface dialog) {

                        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        b.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                if(!Utils.isConnected(getApplication())) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                                } else {
                                    showProgressDialogAndDownload(automaticUpdate);
                                    dialog.cancel();
                                }
                            }
                        });
                    }
                });
                alertDialog.show();
            } else {
                showProgressDialogAndDownload(automaticUpdate);
            }
        } else {
            if(automaticUpdate)
                startDownloadThread(getApplication());
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new RecipesFragment(), "recipes").commit();
            fragmentPosition = 1;
            makeSelection = true;
        }
        else
        {
            fragmentPosition = savedInstanceState.getInt(BUNDLE_FRAGMENTID);
            setTitle(fragmentsNames[fragmentPosition]);
            makeSelection = true;
        }

        DrawerItems[] drawerItem = new DrawerItems[5];
        for(int x = 0; x < 5; x++) {
            drawerItem[x] = new DrawerItems(drawerIcons[x], fragmentsNames[x]);
        }
        DrawerAdapter adapter = new DrawerAdapter(this, R.layout.drawer_items, drawerItem);
        ListView drawerListView = (ListView) findViewById(R.id.lvDrawer);
        drawerListView.setAdapter(adapter);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            @Override
            public void onDrawerClosed(View view) {
                setTitle(fragmentsNames[fragmentPosition]);
            }

            /** W przypadku gdy drawer zostal otwarty, gdy klawiatura byla widoczna na ekranie - zsuwamy klawiature.
             * @see android.support.v4.app.ActionBarDrawerToggle#onDrawerOpened(View)
             */
            @Override
            public void onDrawerOpened(View drawerView) {
                setTitle("Przepisy kulinarne");
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                drawerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        setSelections(fragmentPosition);
                    }});

            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        drawerListView.setOnItemClickListener(new DrawerItemClickListener());
    }

    @Override
    public void onResume(){
        super.onResume();
        if (fragmentPosition != -1) setTitle(fragmentsNames[fragmentPosition]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(categoriesReceiver);
        } catch (Exception e) {

        }
    }

    public static void startDownloadThread(final Context ctx) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {

                    DbHelper dbHelper = new DbHelper(ctx);
                    DownloadManager downloadManager = new DownloadManager(ctx);
                    int categoriesMaxVersion = dbHelper.getLastVersion(DbHelper.CATEGORIES_TABLE_NAME);
                    int recipesMaxVersion = dbHelper.getLastVersion(DbHelper.RECIPES_TABLE_NAME);

                    downloadManager.getAllCategories(categoriesMaxVersion);
                    downloadManager.getAllRecipes(recipesMaxVersion);

                    Utils.setSharedPreferencesBool(ctx, KEY_FIRST_DOWNLOAD, false);

                    return null;
                }
            }.execute();
    }

    private void showProgressDialogAndDownload(boolean automaticUpdate) {
        if(automaticUpdate) {
            progressDialog = ProgressDialog.show(MainActivity.this, "", "Pierwsze uruchomienie. Pobieranie danych.", true);
            progressDialog.setTitle(null);
            progressDialog.setCancelable(false);
            startDownloadThread(getApplication());
            progressDialog.show();
        }
    }

    private BroadcastReceiver categoriesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(progressDialog != null)
                progressDialog.cancel();

            unregisterReceiver(categoriesReceiver);
        }
    };

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(BUNDLE_FRAGMENTID, fragmentPosition);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
        drawerLayout.closeDrawers();
    }

    private class DrawerItemClickListener implements
            ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView parent, View view, int position,
                                long id) {
            if (position != 3) {
                setSelections(position);
                setTitle(fragmentsNames[position]);
            }
            setFragment(position, null);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {

            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                setTitle(fragmentsNames[fragmentPosition]);
            } else setTitle("Przepisy kulinarne");

            if (makeSelection)
            {
                setSelections(fragmentPosition);
                makeSelection = false;
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setFragment(int position, Fragment frg) {
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        if (frg == null) getSupportActionBar().setSubtitle(null);
        backPressed = false;
        Fragment fragment;
        lastFragmentPosition = fragmentPosition;
        if (position != 3) fragmentPosition = position;
        switch (position) {
            case 0:
                fragment = new CategoriesFragment();
                drawerLayout.closeDrawers();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, "category").commit();
                break;
            case 1:
                if (frg == null) {
                    fragment = new RecipesFragment();
                    drawerLayout.closeDrawers();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, fragment, "recipes").commit();
                }
                else
                {
                    drawerLayout.closeDrawers();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, frg, "recipes").commit();
                }
                break;
            case 2:
                fragment = new FavoritesFragment();
                drawerLayout.closeDrawers();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, "favorites").commit();
                break;
            case 3:
                drawerLayout.closeDrawers();
                DbHelper dbHelper = new DbHelper(getApplicationContext());
                int id = dbHelper.getRandomRecipeId();
                if(id < 1) {
                    Toast.makeText(getApplication(), getString(R.string.no_recipes), Toast.LENGTH_LONG).show();
                    break;
                }
                Intent i = new Intent(MainActivity.this, RecipeActivity.class);
                i.putExtra("RecipeId", id);
                i.putExtra("Random", true);
                startActivity(i);
                break;
		case 4:
            fragment = new PreferencesFragment();
            drawerLayout.closeDrawers();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, fragment, "preferences").commit();
            break;
        }
    }

    public void setSelections(int pos)
    {
        try
        {
            ListView drawerListView = (ListView) findViewById(R.id.lvDrawer);
            View itemView;

            if(lastFragmentPosition != -1)
            {
                itemView = drawerListView.getChildAt(lastFragmentPosition);
                itemView.setBackgroundColor(Color.WHITE);
                TextView txtTitle = (TextView) itemView.findViewById(R.id.itemTitle);
                txtTitle.setTextColor(getResources().getColor(R.color.black));
                drawerIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
                if (drawerIcon != null) {
                    drawerIcon.setImageBitmap(null);
                    drawerIcon.destroyDrawingCache();
                }
                drawerIcon.setImageResource(drawerIcons[lastFragmentPosition]);

            }

            itemView = drawerListView.getChildAt(pos);
            itemView.setBackgroundColor(getResources().getColor(R.color.drawerSelection));
            TextView txtTitle = (TextView) itemView.findViewById(R.id.itemTitle);
            txtTitle.setTextColor(getResources().getColor(R.color.primary));

            drawerIcon = (ImageView) itemView.findViewById(R.id.itemIcon);
            if (drawerIcon != null) {
                drawerIcon.setImageBitmap(null);
                drawerIcon.destroyDrawingCache();
            }
            drawerIcon.setImageResource(drawerIconsSelected[pos]);

        }
        catch (NullPointerException ex) {}

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {

            if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                drawerLayout.closeDrawers();
            } else
                drawerLayout.openDrawer(Gravity.LEFT);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (backPressed) {
            super.onBackPressed();
        } else {
            Toast.makeText(getApplicationContext(), "Naciśnij jeszcze raz aby wyjść", Toast.LENGTH_SHORT).show();
            backPressed = true;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
