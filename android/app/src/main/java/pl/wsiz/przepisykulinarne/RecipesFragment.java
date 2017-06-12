package pl.wsiz.przepisykulinarne;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.other.DownloadManager;
import pl.wsiz.przepisykulinarne.other.RecipesListAdapter;

public class RecipesFragment extends Fragment {

    public RecipesFragment() {
    }

    private EditText mSearchEt;
    private boolean mSearchOpened;
    private String searchText;
    private ListView lvRecipes;
    private RecipesListAdapter recipesListAdapter;
    private boolean activeSearch = false;
    private String activeSearchQuery;
    private View fragmentView = null;
    private String CategoryId = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.fragment_recipes, container, false);

        getActivity().registerReceiver(recipesReceiver, new IntentFilter(DownloadManager.RECIPES));

        Bundle bundle = getArguments();

        CategoryId = bundle != null ? bundle.getString("CategoryId") : null;

        lvRecipes = (ListView) rootView.findViewById(R.id.lvRecipes);
        recipesListAdapter = new RecipesListAdapter(getActivity(), false, null, CategoryId);
        lvRecipes.setAdapter(recipesListAdapter);

        lvRecipes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), RecipeActivity.class);
                i.putExtra("RecipeId", (int) id);
                startActivity(i);
            }
        });

        fragmentView = rootView;
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().unregisterReceiver(recipesReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recipes, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search) {
            if (mSearchOpened) {
                closeSearchBar();
            } else {
                openSearchBar(searchText);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSearchBar(String queryText) {
        android.support.v7.app.ActionBar actionBar = ((ActionBarActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search);
        mSearchEt = (EditText) actionBar.getCustomView().findViewById(R.id.etSearch);
        mSearchEt.setText(queryText);
        mSearchEt.requestFocus();
        mSearchEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchRecipes(mSearchEt.getText().toString());
                    activeSearch = true;
                    activeSearchQuery = mSearchEt.getText().toString();
                    closeSearchBar();
                    handled = true;
                }
                return handled;
            }
        });
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
        mSearchOpened = true;

    }

    private void closeSearchBar() {
        searchText = mSearchEt.getText().toString();
        MainActivity act = (MainActivity) getActivity();
        act.getSupportActionBar().setDisplayShowCustomEnabled(false);
        if (activeSearch)
        {
            if (!activeSearchQuery.isEmpty())
            {
                if (CategoryId != null)
                {
                    DbHelper dbHelper = new DbHelper(getActivity());
                    String catName = dbHelper.getCategoryName(Integer.parseInt(CategoryId));
                    act.getSupportActionBar().setSubtitle(catName + ": \"" + activeSearchQuery + "\"");
                }
                else act.getSupportActionBar().setSubtitle("\"" + activeSearchQuery + "\"");
            }
            else act.getSupportActionBar().setSubtitle(null);
        }
        InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
        mSearchOpened = false;
    }

    private void searchRecipes(String text)
    {
        if(lvRecipes != null) {
            recipesListAdapter = new RecipesListAdapter(getActivity(), false, text, CategoryId);
            lvRecipes.setAdapter(recipesListAdapter);
            recipesListAdapter.notifyDataSetChanged();
        }
        RelativeLayout lay = (RelativeLayout) fragmentView.findViewById(R.id.noResultsLayout);
        if (recipesListAdapter.isEmpty()) lay.setVisibility(View.VISIBLE);
        else lay.setVisibility(View.GONE);
    }

    private BroadcastReceiver recipesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newRecipesCount = intent.getIntExtra(DownloadManager.RECIPES, 0);
            if(recipesListAdapter != null && newRecipesCount > 0) {
                recipesListAdapter.update(false, activeSearchQuery, CategoryId);
                recipesListAdapter.notifyDataSetChanged();
            }
        }
    };
}
