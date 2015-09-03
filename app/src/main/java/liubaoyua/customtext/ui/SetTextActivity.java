package liubaoyua.customtext.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import liubaoyua.customtext.R;
import liubaoyua.customtext.adapters.TextRecyclerAdapter;
import liubaoyua.customtext.app.AppHelper;
import liubaoyua.customtext.entity.AppInfo;
import liubaoyua.customtext.entity.CustomText;
import liubaoyua.customtext.entity.DataLoadedEvent;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;


public class SetTextActivity extends AppCompatActivity {

    private static ArrayList<CustomText> clipboard = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private TextRecyclerAdapter textRecyclerAdapter;
    private SwitchCompat switchCompat;

    private String packageName = "";
    private String appName = "";
    private int maxPage = 0;
    private int position = -1;

    private ArrayList<CustomText> data = new ArrayList<>();

    // mPrefs 用于当前 packageName 所在包的数据读写， prefs 是全局信息
    private SharedPreferences mPrefs, prefs;

    private boolean isInActionMode;
    private ActionMode actionMode;
    private ActionMode.Callback mCallback = new ActionMode.Callback() {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.menu_copy:{
                    clipboard = textRecyclerAdapter.getSelectedItem();
                    break;
                }
                case R.id.menu_cut:{
                    clipboard = textRecyclerAdapter.cutSelectedItem();
                    break;
                }
                case R.id.menu_paste:{
                    textRecyclerAdapter.pasteClipBoard(clipboard);
                    break;
                }
                case R.id.menu_select_all:{
                    textRecyclerAdapter.selectAll();
                }
            }
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            getMenuInflater().inflate(R.menu.menu_action_mode, menu);
            isInActionMode = true;
            textRecyclerAdapter.multiSelectMode=true;
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            textRecyclerAdapter.deselectAllItem();
            isInActionMode = false;
            textRecyclerAdapter.multiSelectMode=false;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                getWindow().setStatusBarColor(getColor(R.color.primary_dark));
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
              getWindow().setStatusBarColor(Color.rgb(0,0,0));
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_text);
        Intent intent = getIntent();
        packageName = intent.getStringExtra(Common.PACKAGE_NAME_ARG);
        position = intent.getIntExtra(Common.POSITION_ARG,-1);

        if(Common.FAST_DEBUG){
            packageName = Common.SYSTEM_UI_PACKAGE_NAME;
        }

        if(packageName.equals(Common.GLOBAL_SETTING_PACKAGE_NAME)){
            appName = getString(R.string.global_replacement);
        }else if(packageName.equals(Common.SHARING_SETTING_PACKAGE_NAME)) {
            appName = getString(R.string.enabled_replacement);
        }else{
            PackageInfo packageInfo = Utils.getPackageInfoByPackageName(this,packageName);
            if(packageInfo == null){
                Toast.makeText(this.getApplicationContext(), getString(R.string.error_found)
                        + packageName ,Toast.LENGTH_SHORT).show();
                this.finish();
            }else {
                appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            }
        }

        prefs = getSharedPreferences(Common.PREFS, MODE_WORLD_READABLE);
        mPrefs = getSharedPreferences(packageName, MODE_WORLD_READABLE);


        maxPage = mPrefs.getInt(Common.MAX_PAGE_OLD, 0);

        int count = (maxPage + 1) * Common.DEFAULT_NUM ;

        for(int i = 0; i < count ; i++){
            String oriText = mPrefs.getString(Common.ORI_TEXT_PREFIX + i,"");
            String newText = mPrefs.getString(Common.NEW_TEXT_PREFIX + i,"");
            CustomText customText = new CustomText(oriText,newText);
            data.add(customText);
        }


        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(appName);
        }


        final View fab = (findViewById(R.id.action_button_save));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
//                String s = null;System.out.println( s.toCharArray());
            }
        });


        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        textRecyclerAdapter = new TextRecyclerAdapter(SetTextActivity.this,data,mRecyclerView);
        mRecyclerView.setAdapter(textRecyclerAdapter);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
            float firstRawY, oldRawY, newRawY;
            boolean flag = false;
            float dy;

            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            int fabBottomMargin = lp.bottomMargin;

            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction) {
                    case MotionEvent.ACTION_DOWN: // 按下事件，记录按下时手指在悬浮窗的XY坐标值
                        firstRawY = oldRawY = event.getRawY();
                        flag = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        newRawY = event.getRawY();
                        dy = newRawY - oldRawY;
                        if (oldRawY == 0) {
                            firstRawY = oldRawY = newRawY;
                            flag = false;
                            break;
                        }
                        if (dy < -50) {
                            if (fab.getVisibility() == View.VISIBLE) {
                                ObjectAnimator animator = ObjectAnimator.ofFloat(fab,
                                        "translationY", 0, fab.getHeight() + fabBottomMargin);
                                animator.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        fab.setVisibility(View.INVISIBLE);
                                    }
                                });
                                animator.start();
                            }
                        } else if (dy > 50) {           // 下滑
                            if (fab.getVisibility() == View.INVISIBLE) {
                                fab.setVisibility(View.VISIBLE);
                                ObjectAnimator animator = ObjectAnimator.ofFloat(fab,
                                        "translationY", fab.getHeight() + fabBottomMargin, 0);
                                animator.start();
                            }
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        flag = false;
                        oldRawY = 0;
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_text, menu);
        // systemui 要特殊处理
        if(!packageName.equals(Common.SYSTEM_UI_PACKAGE_NAME) ){
            if(getPackageManager().getLaunchIntentForPackage(packageName) == null){
                MenuItem relaunchMenuItem = menu.findItem(R.id.action_relaunch_app);
                relaunchMenuItem.setEnabled(false);
            }
        }

        // 全局替换 和 共享替换有特殊处理
        if(packageName.equals(Common.GLOBAL_SETTING_PACKAGE_NAME)
                || packageName.equals(Common.SHARING_SETTING_PACKAGE_NAME)){
            menu.findItem(R.id.action_market_link).setEnabled(false);
            menu.findItem(R.id.action_app_info).setEnabled(false);
            menu.findItem(R.id.action_relaunch_app).setEnabled(false);
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
                for (int i = 0; i < 10; i++) {
                    textRecyclerAdapter.getData().add(new CustomText());
                    textRecyclerAdapter.notifyItemRangeInserted(size, size + 9);
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
            Utils.killPackage(packageName);
            if(!packageName.equals(Common.SYSTEM_UI_PACKAGE_NAME)){
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
            textRecyclerAdapter.notifyItemRangeRemoved(0, textRecyclerAdapter.getData().size());
            textRecyclerAdapter.setData(new ArrayList<CustomText>());
            for (int i = 0; i < Common.DEFAULT_NUM ; i++) {
                textRecyclerAdapter.getData().add(new CustomText());
            }
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
        }else if(id == R.id.action_select_mode){
            if(actionMode != null) {
                return false;
            }
            if(!isInActionMode){
                actionMode = startSupportActionMode(mCallback);
                textRecyclerAdapter.notifyDataSetChanged();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        if(!Utils.isIdenticalTextList(data, textRecyclerAdapter.getData())){
            AlertDialog.Builder builder = new AlertDialog.Builder(SetTextActivity.this);
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
            AppInfo info = Utils.getAppInfoByPackageName(packageName);
            if(info != null){
                if(prefs.contains(packageName)){
                    if(prefs.getBoolean(packageName,false)){
                        info.state = AppInfo.ENABLED;
                    }else {
                        info.state = AppInfo.DISABLED;
                    }
                }else {
                    info.state = AppInfo.UNKNOWN;
                }
                EventBus.getDefault().post(new DataLoadedEvent());
            }
            super.onBackPressed();
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
