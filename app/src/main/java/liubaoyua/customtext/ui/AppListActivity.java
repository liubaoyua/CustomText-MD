package liubaoyua.customtext.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;
import liubaoyua.customtext.R;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.NewListEvent;
import liubaoyua.customtext.fragments.AppListFragment;
import liubaoyua.customtext.fragments.FragmentAdapter;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.DBManager;
import liubaoyua.customtext.utils.PicassoTools;
import liubaoyua.customtext.utils.Utils;

public class AppListActivity extends AppCompatActivity {

    private static File prefsDir = new File(Environment.getDataDirectory() + "/data/" + Common.PACKAGE_NAME + "/shared_prefs");
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private AppListFragment appListFragment;
    private AppListFragment recentListFragment;
    private SearchView mSearchView;
    private FragmentAdapter fragmentAdapter;
    private Context context;
    private List<AppListFragment> fragmentList = new ArrayList<>();
    private List<String> titles =null;
    private String nameFilter;
    private SharedPreferences prefs;
    private boolean hasDataBase = false;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.activity_app_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        titles = new ArrayList<>();
        titles.add(getResources().getString(R.string.all_apps));
        titles.add(getResources().getString(R.string.recent_apps));

        //三道杠
        ActionBar mActionBar = getSupportActionBar();
        if (mActionBar != null)
            mActionBar.setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.dl_main_drawer);
        setupDrawerLayout();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nv_main_navigation);
        setupDrawerContent(navigationView);

        context = getApplicationContext();
        PicassoTools.init(context);

        prefs = getSharedPreferences(Common.PREFS, MODE_WORLD_READABLE);
        hasDataBase = prefs.getBoolean(Common.PREFS_HAS_DATABASE,false);

        prefs.edit().putString(Common.PACKAGE_NAME_ARG, "^" + getString(R.string.app_name) + "$");
        prefs.edit().putString(Common.MESSAGE, getString(R.string.setting_default_message));

        int version = prefs.getInt(Common.PACKAGE_VERSION_CODE,0);

        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(Common.PACKAGE_NAME, 0);
            if(packageInfo.versionCode > version){
                Utils.showMessage(this,packageInfo.versionName);
            }
            prefs.edit().putInt(Common.PACKAGE_VERSION_CODE, packageInfo.versionCode);
        }catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        prefs.edit().apply();
        setupViewPager();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_app_list, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.action_search).setVisible(true);
        mSearchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                nameFilter = query;
                fragmentList.get(0).filter(nameFilter);
                fragmentList.get(1).filter(nameFilter);
                mSearchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                nameFilter = newText;
                fragmentList.get(0).filter(nameFilter);
                fragmentList.get(1).filter(nameFilter);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onKeyUp(int keyCode,@NonNull KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH && (event.getFlags() & KeyEvent.FLAG_CANCELED) == 0) {
            if (mSearchView.isShown()) {
                mSearchView.setIconified(false);
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupViewPager() {
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));

        appListFragment = new AppListFragment();

        recentListFragment = new AppListFragment();
        fragmentList.add(appListFragment);
        fragmentList.add(recentListFragment);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);

        fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), fragmentList, titles);
        mViewPager.setAdapter(fragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(fragmentAdapter);

        fragmentList.get(mViewPager.getCurrentItem()).stopScrolling();

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                fragmentList.get(tab.getPosition()).stopScrolling();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                fragmentList.get(tab.getPosition()).scrollToTopOrBottom();
            }
        });

}

    @TargetApi(21)
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();
                        switch (id){
                            case R.id.nav_refresh:
                                refreshList(); break;
                            case R.id.nav_settings:
                                startActivity(new Intent(context, SettingActivity.class));
                                break;
                            case R.id.nav_backup:
                                doExport();
                                break;
                            case R.id.nav_exit:
                                if(Build.VERSION.SDK_INT >= 21){
                                    finishAndRemoveTask();
                                }else {
                                    System.exit(0);
                                }
                                break;
                            case R.id.nav_restore:
                                doImport();
                        }
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

    private void setupDrawerLayout() {

//        动画，并设置监听器
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.open,
                R.string.close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void doExport() {
        final String[] name =new String[1];
        final EditText editText =new EditText(this);
        editText.setHint(R.string.imp_exp_backup_hint);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64,64,64,64);
        layout.addView(editText);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_input_backup_name).setView(layout);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name[0] = editText.getText().toString();
                if (name[0].matches("^\\s*$") || name[0].length() == 0) {
                    System.out.println(true);
                    Date d = new Date();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss",Locale.getDefault());
                    name[0] = sdf.format(d);
                }

                new ExportTask().execute(new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + Common.BACKUP_DIR, name[0]));
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void doImport() {
        final List<File> backupList = new ArrayList<>();
        File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + Common.BACKUP_DIR);
        if (root.exists())
        {
            File[] temp  = root.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory();
                }
            });
            Collections.addAll(backupList,temp);
            Collections.sort(backupList,new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().toLowerCase().compareTo(file2.getName().toLowerCase());
                }
            });
        }
        if(backupList.size() == 0){
            Toast.makeText(context,
                    getString(R.string.imp_exp_no_backup_found),Toast.LENGTH_SHORT).show();
            return;
        }
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(64, 64, 64, 64);
        ListView listView = new ListView(this);
        listView.setDivider(null);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final File tmp = backupList.get(position);
                if (!tmp.exists()) {
                    Toast.makeText(context,
                            getString(R.string.imp_exp_file_does_not_exist, tmp.getAbsolutePath()),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(AppListActivity.this);
                builder.setTitle(R.string.menu_restore);
                builder.setMessage(R.string.imp_exp_confirm);
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        new ImportTask().execute(tmp);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
        layout.addView(listView);
        ArrayAdapter<File> adapter = new ArrayAdapter<File>(this,R.layout.list_item_restore,backupList){
            @Override
            public View getView(final int position, View convertView, ViewGroup parent) {
                if(convertView==null){
                    convertView=View.inflate(context, R.layout.list_item_restore, null);
                }
                TextView textView = (TextView)convertView.findViewById(R.id.text_file);
                textView.setText(backupList.get(position).getName());
                final Button button=(Button)convertView.findViewById(R.id.button_delete);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        Utils.emptyDirectory(backupList.get(position));
                        backupList.get(position).delete();
                        backupList.remove(position);
                        notifyDataSetChanged();
                    }
                });
                return convertView;
            }
        };
        listView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_select_restore_item).setView(layout);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    public void refreshList() {
         new LoadAppsTask(true).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (Common.DEBUG) {
            Log.d(Common.TAG, "onActivityResult");
        }
        if (requestCode == Common.APP_REQUEST_CODE) {
            if (resultCode == Common.APP_RESULT_CODE) {
                boolean isEnabled = data.getBooleanExtra(Common.IS_ENABLED_ARG, false);
                int position = data.getIntExtra(Common.POSITION_ARG, -1);
                if (Common.DEBUG) {
                    Log.d(Common.TAG, "onActivityResult  " + isEnabled + "  position   " + position);
                }
                if (position != -1) {
                    List<AppInfo> list = recentListFragment.getAppList();
                    List<AppInfo> showingList = fragmentList.get(mViewPager.getCurrentItem()).getShowingAppList();
                    if (showingList == null)
                        return;
                    AppInfo temp = showingList.get(position);
                    if (isEnabled) {
                        temp.state = AppInfo.ENABLED;
                        if (!list.contains(temp)) {
                            list.add(0, temp);
                        }
                    } else {
                        temp.state = AppInfo.DISABLED;
                    }
                    appListFragment.notifyDataSetChanged();
                    recentListFragment.notifyDataSetChanged();
                }
            }
        }
    }

    //EventBus event
    public void onEvent(NewListEvent event) {
        if (hasDataBase) {
            List<AppInfo> appList = DBManager.getInstance(context).query();
            appListFragment.setAppList(appList);
            recentListFragment.setAppList(Utils.getRecentList(appList));
            recentListFragment.filter(nameFilter);
            appListFragment.filter(nameFilter);
            new LoadAppsTask(false).execute();
        } else {
            new LoadAppsTask(true).execute();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PicassoTools.destroy();
        EventBus.getDefault().unregister(this);
    }

    private class LoadAppsTask extends AsyncTask<Void,String,Void> {
        private ProgressDialog dialog = null;
        private Boolean showDialog = false;
        private List<AppInfo> appList;

        public LoadAppsTask(Boolean showDialog){
            this.showDialog = showDialog;
        }
        @Override
        protected void onPreExecute() {
            if (showDialog) {
                dialog = new ProgressDialog(AppListActivity.this);
                dialog.setMessage(getString(R.string.dialog_loading));
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setCancelable(false);
                dialog.show();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            if (dialog != null)
                dialog.setMessage(values[0]);
        }


        @Override
        protected Void doInBackground(Void... params) {
            appList = new ArrayList<>();
            PackageManager pm = getPackageManager();
            List<PackageInfo> packages = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            if (dialog != null)
                dialog.setMax(packages.size());
            int i = 1;
            for (PackageInfo pkgInfo : packages) {
                if (dialog != null)
                    dialog.setProgress(i++);
                ApplicationInfo applicationInfo = pkgInfo.applicationInfo;
                if (applicationInfo == null)
                    continue;
                if (applicationInfo.packageName.equals(Common.PACKAGE_NAME)) {
                    continue;
                }
                if (dialog != null)
                    publishProgress(getString(R.string.dialog_loading) + "\n" + applicationInfo.loadLabel(pm).toString());
                AppInfo appInfo = new AppInfo(pkgInfo, applicationInfo.loadLabel(pm).toString());
                if (prefs.contains(appInfo.packageName)) {
                    if (prefs.getBoolean(appInfo.packageName, false)) {
                        appInfo.state = AppInfo.ENABLED;
                    } else {
                        appInfo.state = AppInfo.DISABLED;
                    }
                }
                appList.add(appInfo);
                if (Common.FAST_DEBUG) {
                    Log.d(Common.TAG, appInfo.toString());
                    if (appList.size() > 6)
                        break;
                }
            }

            PackageInfo globalPackageInfo = Utils.getPackageInfoByPackageName(context, Common.PACKAGE_NAME);
            PackageInfo sharedPackageInfo = Utils.getPackageInfoByPackageName(context, Common.PACKAGE_NAME);
            if (globalPackageInfo != null) {
                AppInfo globalAppInfo = new AppInfo(globalPackageInfo, getString(R.string.global_replacement), Common.GLOBAL_SETTING_PACKAGE_NAME);
                if (prefs.getBoolean(globalAppInfo.packageName, false)) {
                    globalAppInfo.state = AppInfo.ENABLED;
                } else {
                    globalAppInfo.state = AppInfo.DISABLED;
                }
                appList.add(globalAppInfo);
            }

            if (sharedPackageInfo != null) {
                AppInfo sharedAppInfo = new AppInfo(sharedPackageInfo, getString(R.string.enabled_replacement), Common.SHARING_SETTING_PACKAGE_NAME);
                if (prefs.getBoolean(sharedAppInfo.packageName, false)) {
                    sharedAppInfo.state = AppInfo.ENABLED;
                } else {
                    sharedAppInfo.state = AppInfo.DISABLED;
                }
                appList.add(sharedAppInfo);
            }

            Collections.sort(appList, new Comparator<AppInfo>() {
                @Override
                public int compare(AppInfo lhs, AppInfo rhs) {
                    return Collator.getInstance(Locale.getDefault()).compare(lhs.appName, rhs.appName);
                }
            });

            try {
                DBManager.getInstance(context).delete();
                DBManager.getInstance(context).insert(appList);
                prefs.edit().putBoolean(Common.PREFS_HAS_DATABASE, true).apply();
                hasDataBase = true;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aa) {
            if (recentListFragment != null) {
                recentListFragment.setAppList(Utils.getRecentList(appList));
                appListFragment.setAppList(appList);
                appListFragment.filter(nameFilter);
                recentListFragment.filter(nameFilter);
            }
            if (dialog != null)
                dialog.dismiss();
        }
    }

    class ExportTask extends AsyncTask<File, String, String> {
        @Override
        protected String doInBackground(File... params) {
            File backupDir = params[0];
            if (!backupDir.exists())
                backupDir.mkdirs();
            if (prefsDir.exists()) {
                String files[] = prefsDir.list();
                if (files.length != 0) {
                    for (String file : files) {
                        File srcFile = new File(prefsDir, file);
                        File destFile = new File(backupDir, file);
                        try {
                            Utils.CopyFile(srcFile, destFile);
                        } catch (IOException ex) {
                            return getString(R.string.imp_exp_export_error, ex.getMessage());
                        } catch (Exception ex) {
                            return ex.getMessage();
                        }
                    }
                }
            }
            return getString(R.string.imp_exp_exported, backupDir.toString());
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }

    class ImportTask extends AsyncTask<File, String, String> {

        @Override
        protected String doInBackground(File... params) {
            File inFile = params[0];
            Utils.emptyDirectory(prefsDir);
            String files[] = inFile.list();
            for (String file : files) {
                File srcFile = new File(inFile, file);
                File destFile = new File(prefsDir, file);
                try {
                    Utils.CopyFile(srcFile, destFile);
                    destFile.setReadable(true, false);
                    destFile.setWritable(true, true);
                } catch (IOException ex) {
                    return getString(R.string.imp_exp_import_error, ex.getMessage());
                } catch (Exception ex) {
                    return ex.getMessage();
                }
            }

            return getString(R.string.imp_exp_imported);
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}
