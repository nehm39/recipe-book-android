package pl.wsiz.przepisykulinarne;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import pl.wsiz.przepisykulinarne.other.CategoriesListAdapter;
import pl.wsiz.przepisykulinarne.other.Category;
import pl.wsiz.przepisykulinarne.other.DownloadManager;

public class CategoriesFragment extends Fragment{

    private CategoriesListAdapter categoriesListAdapter;

    public CategoriesFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_categories, container, false);

        getActivity().registerReceiver(categoriesReceiver, new IntentFilter(DownloadManager.CATEGORIES));

        final ListView lvCategories = (ListView) rootView.findViewById(R.id.lvCategories);
        categoriesListAdapter = new CategoriesListAdapter(getActivity());
        lvCategories.setAdapter(categoriesListAdapter);

        lvCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                RecipesFragment recipesFragment = new RecipesFragment();
                Bundle bundle = new Bundle();
                bundle.putString("CategoryId", "" + id);
                recipesFragment.setArguments(bundle);
                MainActivity act = (MainActivity) getActivity();
                act.setSelections(1);
                act.setTitle("Przepisy");
                act.getSupportActionBar().setSubtitle(((Category) lvCategories.getItemAtPosition(position)).Name.toString());
                act.setFragment(1, recipesFragment);

            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getActivity().unregisterReceiver(categoriesReceiver);
    }

    private BroadcastReceiver categoriesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newCategoriesCount = intent.getIntExtra(DownloadManager.CATEGORIES, 0);
            if(categoriesListAdapter != null && newCategoriesCount > 0) {
                categoriesListAdapter.update();
                categoriesListAdapter.notifyDataSetChanged();
            }
        }
    };
}
