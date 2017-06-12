package pl.wsiz.przepisykulinarne;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ar.com.daidalos.afiledialog.FileChooserDialog;
import pl.wsiz.przepisykulinarne.Db.DbHelper;
import pl.wsiz.przepisykulinarne.other.DownloadManager;
import pl.wsiz.przepisykulinarne.other.PreferenceFragment;
import pl.wsiz.przepisykulinarne.other.Utils;

public class PreferencesFragment extends PreferenceFragment {

    public static final String AUTOMATIC_UPDATE = "settingsAutomaticUpdate";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        getActivity().registerReceiver(categoriesReceiver, new IntentFilter(DownloadManager.CATEGORIES));

        final LayoutInflater li = LayoutInflater.from(getActivity().getApplicationContext());

        Preference settingsCheckUpdate = (Preference) findPreference("settingsCheckUpdate");
        settingsCheckUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(Utils.isConnected(getActivity()))
                    MainActivity.startDownloadThread(getActivity());
                else
                    Toast.makeText(getActivity(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return true;
            }
        });


        // Informacje o aplikacji:
        Preference about = (Preference) findPreference("settingsAbout");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            // Listener kliknięcia opcji:
            public boolean onPreferenceClick(Preference preference) {
                View aboutView = li.inflate(R.layout.dialog_about, null);
                // Otworzenie okienka z dialogiem:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());
                alertDialogBuilder.setView(aboutView);
                alertDialogBuilder.setInverseBackgroundForced(true);
                alertDialogBuilder.setCancelable(true).setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;

            }
        });


        Preference licence = (Preference) findPreference("settingsLicense");
        licence.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            // Listener kliknięcia opcji:
            public boolean onPreferenceClick(Preference preference) {
                View dialogView = li.inflate(R.layout.dialog_license, null);
                // Otworzenie okienka z dialogiem:
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        getActivity());
                alertDialogBuilder.setView(dialogView);
                alertDialogBuilder.setInverseBackgroundForced(true);
                alertDialogBuilder.setCancelable(true).setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;

            }
        });

        //Zaciemnienie wersji aplikacji:
        Preference appVersion = (Preference) findPreference("settingsVersion");
        appVersion.setEnabled(false);

        Preference dbVersion = (Preference) findPreference("settingsDatabaseVersion");
        dbVersion.setEnabled(false);

        Preference exportSD = (Preference) findPreference("settingsExportToSD");
        exportSD.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            //Listener kliknięcia opcji:
            public boolean onPreferenceClick(Preference preference) {
                exportFavorites();
                return true;

            }
        });

        Preference importSD = (Preference) findPreference("settingsImportFromSD");
        importSD.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            //Listener kliknięcia opcji:
            public boolean onPreferenceClick(Preference preference) {
                importFavorites();
                return true;

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        getActivity().unregisterReceiver(categoriesReceiver);
    }

    private void exportFavorites()
    {
        FileChooserDialog dialog = new FileChooserDialog(getActivity());
        dialog.setFolderMode(true);
        dialog.setShowConfirmation(true, false);
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(Dialog source, File file) {
                source.hide();
                File exportFile = new File(file, "ulubionePrzepisy.rbe");
                try {
                    exportFile.createNewFile();
                    CSVWriter csvWrite = new CSVWriter(new FileWriter(exportFile));
                    DbHelper dbHelper = new DbHelper(getActivity());
                    List<Integer> favoritesList = dbHelper.getFavoritesIds();
                    String columnNames[] = {"RecipeId"};
                    csvWrite.writeNext(columnNames);
                    for(Integer x : favoritesList)
                    {
                        String column[] ={x.toString()};
                        csvWrite.writeNext(column);
                    }

                    csvWrite.close();
                    Toast.makeText(getActivity().getApplicationContext(), R.string.exportSuccessful, Toast.LENGTH_SHORT).show();
                }
                catch (IOException ex)
                {
                    Log.e("Export", ex.getMessage(), ex);
                    Toast.makeText(getActivity().getApplicationContext(), R.string.exportFailed, Toast.LENGTH_SHORT).show();
                }
            }
            public void onFileSelected(Dialog source, File folder, String name) { }
        });
        dialog.show();
    }

    private void importFavorites()
    {
        FileChooserDialog dialog = new FileChooserDialog(getActivity());
        dialog.setFolderMode(false);
        dialog.setFilter(".*rbe");
        dialog.addListener(new FileChooserDialog.OnFileSelectedListener() {
            public void onFileSelected(final Dialog source, final File file) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.settingsImportConfirmation)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                source.hide();
                                try {
                                    CSVReader reader = new CSVReader(new FileReader(file));
                                    String[] nextLine;
                                    List<Integer> favoritesList = new ArrayList<>();
                                    while ((nextLine = reader.readNext()) != null) {
                                        String id = nextLine[0];
                                        if (!id.equalsIgnoreCase("RecipeId")) {
                                            favoritesList.add(Integer.parseInt(id));
                                        }
                                    }
                                    DbHelper dbHelper = new DbHelper(getActivity());
                                    dbHelper.importFavorites(favoritesList);
                                    reader.close();
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.importSuccessful, Toast.LENGTH_SHORT).show();
                                } catch (Exception ex) {
                                    Log.e("Import", ex.getMessage(), ex);
                                    Toast.makeText(getActivity().getApplicationContext(), R.string.importFailed, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
            }

            public void onFileSelected(final Dialog source, final File folder, final String name) { }
        });
        dialog.show();
    }

    private BroadcastReceiver categoriesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int newRecipesCount = intent.getIntExtra(DownloadManager.RECIPES, 0);
            if(newRecipesCount > 0)
                Toast.makeText(getActivity(), String.format(context.getString(R.string.new_recipes), newRecipesCount), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getActivity(), context.getString(R.string.no_new_recipes), Toast.LENGTH_SHORT).show();
        }
    };

    public static boolean getBoolSetting(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPref.getBoolean(key, defaultValue);
    }
}