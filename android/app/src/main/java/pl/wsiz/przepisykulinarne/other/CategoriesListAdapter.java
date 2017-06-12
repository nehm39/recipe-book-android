package pl.wsiz.przepisykulinarne.other;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.R;

public class CategoriesListAdapter extends BaseAdapter {

    private Context ctx;
    private List<Category> categoryList;
    private LayoutInflater inflater;

    public CategoriesListAdapter(Context ctx) {
        this.ctx = ctx;
        categoryList = new DbHelper(ctx).getAllCategories();
    }

    public void update() {
        categoryList = new DbHelper(ctx).getAllCategories();
    }

    @Override
    public int getCount() {
        return categoryList.size();
    }

    @Override
    public Category getItem(int position) {
        return categoryList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).CategoryId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (inflater == null)
            inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.lv_item_category, null);

        TextView tvCategoryName = (TextView) convertView.findViewById(R.id.tvCategoryName);
        TextView tvCategoryItemsNumber = (TextView) convertView.findViewById(R.id.tvCategoryItemsNumber);

        Category category = getItem(position);

        tvCategoryName.setText(category.Name);
        tvCategoryItemsNumber.setText("" + category.itemsCount + " " + recipeStringCorrectForm(category.itemsCount));

        return convertView;
    }

    private String recipeStringCorrectForm(int number)
    {
        String stringNumber = Integer.toString(number);
        int lastChar = Integer.parseInt(Character.toString(stringNumber.charAt(stringNumber.length() - 1)));
        if (number == 1) return "przepis";
        else if (number >= 5 && number <= 21) return "przepisów";
        else if (lastChar <= 1 || lastChar >= 5) return "przepisów";
        else if (lastChar >= 2 && lastChar <= 4) return "przepisy";
        else return "przepisów";
    }
}
