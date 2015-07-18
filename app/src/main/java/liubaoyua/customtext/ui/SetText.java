package liubaoyua.customtext.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;


import java.io.DataOutputStream;
import java.util.ArrayList;
import liubaoyua.customtext.utils.CustomText;
import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.TextRecyclerAdapter;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;


public class SetText extends AppCompatActivity {

//    private FloatingActionsMenu fam;
    private RecyclerView mRecyclerView;
    private TextRecyclerAdapter textRecyclerAdapter;
    private SwitchCompat switchCompat;

    private String packageName = "";
    private CharSequence appName = "";
    private int maxPage = 0;
    private int position = -1;

    private ArrayList<CustomText> data = new ArrayList<>();

    // mPrefs 用于当前 packageName 所在包的数据读写， prefs 是全局信息
    private SharedPreferences mPrefs, prefs;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_text);
        Intent intent = getIntent();
        packageName = intent.getStringExtra(Common.PACKAGE_NAME_ARG);
        position = intent.getIntExtra(Common.POSITION_ARG,-1);
        PackageManager pm = getPackageManager();
        PackageInfo packageInfo = null;

        if(Common.FAST_DEBUG){
            packageName = Common.SYSTEM_UI;
        }
        try{
            packageInfo = pm.
                    getPackageInfo(packageName,PackageManager.GET_UNINSTALLED_PACKAGES);
        }catch (PackageManager.NameNotFoundException e){
            Toast.makeText(this.getApplicationContext(), getString(R.string.error_found)
                    + e.getMessage(),Toast.LENGTH_LONG).show();
            this.finish();
        }
        if (packageInfo != null){
            appName = packageInfo.applicationInfo.loadLabel(pm);
        }
        if (Common.DEBUG){
            Log.d(Common.TAG, Common.PACKAGE_NAME_ARG + packageName);
        }


        prefs = getSharedPreferences(Common.PREFS, MODE_WORLD_READABLE);
        mPrefs = getSharedPreferences(packageName, MODE_WORLD_READABLE);


        maxPage = mPrefs.getInt(Common.MAX_PAGE_OLD, 0);

        int count = (maxPage + 1) * Common.DEFAULT_NUM ;

        for(int i = 0; i < count ; i++){
            String oriText = mPrefs.getString(Common.ORI_TEXT_PREFIX + i,"");
            String newText = mPrefs.getString(Common.NEW_TEXT_PREFIX + i,"");
            CustomText customText = new CustomText(i,oriText,newText);
            data.add(customText);
        }


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(appName);
        }


        (findViewById(R.id.action_button_save))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        saveData();
                    }
                });


        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        textRecyclerAdapter = new TextRecyclerAdapter(SetText.this,data,mRecyclerView);
        mRecyclerView.setAdapter(textRecyclerAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_text, menu);
        // systemui 要特殊处理
        if(!packageName.equals(Common.SYSTEM_UI) ){
            if(getPackageManager().getLaunchIntentForPackage(packageName) == null){
                MenuItem relaunchMenuItem = menu.findItem(R.id.action_relaunch_app);
                relaunchMenuItem.setEnabled(false);
            }
        }

        MenuItem switchMenuItem = menu.findItem(R.id.action_switch).setVisible(true);
        switchCompat = (SwitchCompat) MenuItemCompat.getActionView(switchMenuItem);
        if(prefs.getBoolean(packageName,false)){
            switchCompat.setChecked(true);
        }else{
            switchCompat.setChecked(false);
        }
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean(packageName, b).commit();
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        <string name="menu_add_item">增加楼层</string>
//        <string name="menu_clear_empty">清除空行</string>
//        <string name="menu_relaunch_app">重开应用</string>
//        <string name="menu_app_info">应用信息</string>
//        <string name="menu_market_link">市场链接</string>
//        <string name="menu_clear_all">清除所有</string>

        if (id == android.R.id.home){
            onBackPressed();

        }else if(id == R.id.action_add_item){
            int size = textRecyclerAdapter.getData().size();
                for (int i = 0; i < 5; i++) {
                    textRecyclerAdapter.getData().add(new CustomText((size + i), "", ""));
                    textRecyclerAdapter.notifyItemRangeInserted(size, size + 4);
                    Snackbar.make(mRecyclerView
                            , getString(R.string.menu_add_item) + " " + getString(R.string.succeed)
                            , Snackbar.LENGTH_LONG).show();
                }

        }else if(id == R.id.action_clear_empty){
            ArrayList<CustomText> list = textRecyclerAdapter.getData();
            for (int i = 0; i < list.size() ; i++) {
                CustomText customText = list.get(i);
                if(customText.oriText.equals("")&&customText.newText.equals("")){
                    list.remove(i);
                    i--;
                }
            }
            textRecyclerAdapter.notifyDataSetChanged();
            Snackbar.make(mRecyclerView
                    , getString(R.string.menu_clear_all)+" "+getString(R.string.succeed)
                    ,Snackbar.LENGTH_LONG )
                    .setAction(getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            textRecyclerAdapter.setData(data);
                            textRecyclerAdapter.notifyDataSetChanged();
                        }
                    }).show();

        }else if(id == R.id.action_relaunch_app){
            killPackage(packageName);
            if(!packageName.equals(Common.SYSTEM_UI)){
                Intent LaunchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                startActivity(LaunchIntent);
            }
        }else if(id == R.id.action_app_info){
            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + packageName)));

        }else if(id == R.id.action_market_link){
            Intent intent = new Intent("android.intent.action.VIEW",
                    Uri.parse("market://details?id="+packageName));
            startActivity(intent);

        }else if(id == R.id.action_clear_all){
            textRecyclerAdapter.setData(new ArrayList<CustomText>());
            for (int i = 0; i < Common.DEFAULT_NUM ; i++) {
                textRecyclerAdapter.getData().add(new CustomText(i, "", ""));
            }
            textRecyclerAdapter.notifyDataSetChanged();
            switchCompat.setChecked(false);
            prefs.edit().remove(packageName).commit();
            Snackbar.make(mRecyclerView
                    , getString(R.string.menu_clear_all)+" "+getString(R.string.succeed)
                    ,Snackbar.LENGTH_LONG )
                    .setAction(getString(R.string.undo), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            prefs.edit().putBoolean(packageName, switchCompat.isChecked()).commit();
                            textRecyclerAdapter.setData(data);
                            textRecyclerAdapter.notifyDataSetChanged();
                        }
                    }).show();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(Common.DEBUG){
            Log.d(Common.TAG,"onBackPressed different texts" +Utils.isIdenticalTextList(data, textRecyclerAdapter.getData()));
        }
        if(!Utils.isIdenticalTextList(data, textRecyclerAdapter.getData())){

            AlertDialog.Builder builder = new AlertDialog.Builder(SetText.this);
            builder.setIcon(R.mipmap.ic_launcher);
            builder.setTitle(getString(R.string.dialog_data_not_saved));
            builder.setMessage(getString(R.string.dialog_save_now));
            builder.setPositiveButton(getString(R.string.dialog_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    saveData();
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNeutralButton(getString(R.string.dialog_not_to_save), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }else{
            Intent intent = new Intent();
            intent.putExtra(Common.IS_ENABLED_ARG,switchCompat.isChecked());
            intent.putExtra(Common.POSITION_ARG, position);
            setResult(Common.APP_RESULT_CODE, intent);
            super.onBackPressed();
        }

    }

    private void killPackage(String packageToKill) {
        // code modified from :
        // http://forum.xda-developers.com/showthread.php?t=2235956&page=6
        try { // get superuser
            Process su = Runtime.getRuntime().exec("su");
            if (su == null)
                return;
            DataOutputStream os = new DataOutputStream(su.getOutputStream());
            os.writeBytes("pkill " + packageToKill + "\n");
            os.writeBytes("exit\n");
            su.waitFor();
            os.close();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void saveData(){
        ArrayList<CustomText> newData = textRecyclerAdapter.getData();
        data.clear();
        data.addAll(newData);
        SharedPreferences.Editor mEditor = mPrefs.edit();
        for (int i = 0; i < newData.size(); i++) {
            CustomText temp = newData.get(i);
            mEditor.putString(Common.ORI_TEXT_PREFIX+i,temp.oriText);
            mEditor.putString(Common.NEW_TEXT_PREFIX + i, temp.newText);
        }
        int pageNum = newData.size()/Common.DEFAULT_NUM;
        if(pageNum < maxPage){
            int rest = (maxPage - pageNum) *Common.DEFAULT_NUM;
            for (int i = 0; i < rest; i++) {
                mEditor.remove(Common.ORI_TEXT_PREFIX + i);
                mEditor.remove(Common.NEW_TEXT_PREFIX + i);
            }
        }
        mEditor.putInt(Common.MAX_PAGE_OLD, pageNum);
        mEditor.commit();
        if(switchCompat.isChecked()){
            Snackbar.make(mRecyclerView
                    , getString(R.string.dialog_save)+" "+getString(R.string.succeed)
                    ,Snackbar.LENGTH_LONG ).show();
        }else{
            Snackbar.make(mRecyclerView
                    , getString(R.string.dialog_save)+" "+getString(R.string.succeed)
                    + "," + getString(R.string.switch_is_not_activated)
                    ,Snackbar.LENGTH_LONG )
                    .setAction(getString(R.string.open), new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            switchCompat.setChecked(true);
                        }
                    }).show();
        }
    }
}
