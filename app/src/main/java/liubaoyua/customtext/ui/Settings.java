package liubaoyua.customtext.ui;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.os.Bundle;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import liubaoyua.customtext.R;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;

public class Settings extends AppCompatActivity {
    private SettingsFragment mSettingsFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.settings_container, mSettingsFragment).commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            if(preference==findPreference(Common.CHECK_UPDATE)) {
                try {
                    PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo("com.coolapk.market", 0);
                    if (packageInfo != null) {
                        Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage("com.coolapk.market");
                        if (intent == null) {
                            Intent urlIntent = new Intent();
                            urlIntent.setData(Uri.parse("http://www.coolapk.com/apk/" + Common.PACKAGE_NAME));
                            urlIntent.setAction(Intent.ACTION_VIEW);
                            startActivity(urlIntent);
                        } else {
                            intent.setComponent(new ComponentName("com.coolapk.market", "com.coolapk.market" + ".AppViewActivity"));
                            intent.setData(Uri.parse("market://details?id="+Common.PACKAGE_NAME));
                            startActivity(intent);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(preference == findPreference(Common.SETTING_ATTENTION)){
                PackageInfo packageInfo = null;
                try{
                    packageInfo = getActivity().getPackageManager().getPackageInfo(Common.PACKAGE_NAME, PackageManager.GET_UNINSTALLED_PACKAGES);
                }catch (PackageManager.NameNotFoundException e){
                    e.printStackTrace();
                }
                if(packageInfo != null){
                    Utils.showMessage(getActivity(), packageInfo.versionName);
                }
            }
            return false;
        }
    }
}
