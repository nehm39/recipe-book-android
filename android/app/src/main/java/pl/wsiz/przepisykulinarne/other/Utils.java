package pl.wsiz.przepisykulinarne.other;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Base64;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    public static Bitmap getImageFromDb(Context ctx, String photo) {
        byte[] imageAsBytes = Base64.decode(photo.getBytes(), Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
    }

    public static Date parseDate(String date) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.parse(date);
    }

    public static String dateToString(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    //region Shared Preferences
    public static boolean getSharedPreferencesBool(Context ctx, String key, boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getBoolean(key, defaultValue);
    }

    public static void setSharedPreferencesBool(Context ctx, String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static long getSharedPreferencesLong(Context ctx, String key, long defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        return sharedPref.getLong(key, defaultValue);
    }

    public static void setSharedPreferencesLong(Context ctx, String key, long value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
        editor.putLong(key, value);
        editor.apply();
    }

    public static Date getSharedPreferencesDate(Context ctx, String key) {
        return new Date(getSharedPreferencesLong(ctx, key, 0));
    }

    public static void setSharedPreferencesDate(Context ctx, String key, Date value) {
        setSharedPreferencesLong(ctx, key, value.getTime());
    }
    //endregion

    public static boolean isConnected(Context ctx) {
        ConnectivityManager cm =
                (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }
}
