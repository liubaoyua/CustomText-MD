package liubaoyua.customtext.ui;

import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import liubaoyua.customtext.R;
import liubaoyua.customtext.databinding.ActivitySettingsBinding;
import liubaoyua.customtext.utils.Common;
import liubaoyua.customtext.utils.Utils;

public class SettingActivity extends AppCompatActivity {
    private SettingsFragment mSettingsFragment;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingsBinding b = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        toolbar = b.toolbar;
        setupToolbar();
        Utils.configStatusBarColor(this);
        if (savedInstanceState == null) {
            mSettingsFragment = new SettingsFragment();
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.setting_content, mSettingsFragment).commit();
        }

    }

    private void setupToolbar() {
        toolbar.setTitle(R.string.title_activity_settings);
        toolbar.setNavigationIcon(R.mipmap.ic_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {

        Preference version;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            version = findPreference(Common.SETTING_VERSION_INFO);
            String versionName = Utils.getPackageInfoByPackageName(getActivity(), getActivity().getPackageName()).versionName;
            version.setTitle(version.getTitle() + " " + versionName);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            super.onPreferenceTreeClick(preferenceScreen, preference);
            if (preference == findPreference(Common.CHECK_UPDATE)) {
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
                            intent.setData(Uri.parse("market://details?id=" + Common.PACKAGE_NAME));
                            startActivity(intent);
                        }
                    }
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (preference == findPreference(Common.SETTING_ATTENTION)) {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = getActivity().getPackageManager().getPackageInfo(Common.PACKAGE_NAME, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (packageInfo != null) {
                    Utils.showMessage(getActivity(), packageInfo.versionName);
                }
            }
            return false;
        }
    }
}
