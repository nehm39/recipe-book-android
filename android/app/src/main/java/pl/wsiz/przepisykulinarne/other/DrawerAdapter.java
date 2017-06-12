package pl.wsiz.przepisykulinarne.other;

import pl.wsiz.przepisykulinarne.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DrawerAdapter extends ArrayAdapter<DrawerItems> {
	 
    private Context context;
    private int layoutID;
    private DrawerItems items[] = null;
 
    public DrawerAdapter(Context context, int layoutID, DrawerItems[] items) {
 
        super(context, layoutID, items);
        this.layoutID = layoutID;
        this.context = context;
        this.items = items;
    }
 
    @Override
    public View getView(int position, View view, ViewGroup parent) {

        View drawerView = view;
    	LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        drawerView = inflater.inflate(layoutID, parent, false);
        ImageView ivIcon = (ImageView) drawerView.findViewById(R.id.itemIcon);
        TextView txtTitle = (TextView) drawerView.findViewById(R.id.itemTitle);
        DrawerItems drawerItems = items[position];
        ivIcon.setImageResource(drawerItems.icon);
        txtTitle.setText(drawerItems.title);   
        return drawerView;
    }
 
}