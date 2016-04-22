package liubaoyua.customtext.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import liubaoyua.customtext.R;
import liubaoyua.customtext.databinding.ActivityDonateBinding;

/**
 * Created by liubaoyua on 2016/4/22.
 */
public class DonateActivity extends AppCompatActivity implements View.OnClickListener {


    ActivityDonateBinding binding;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_donate);
        int color = getResources().getColor(R.color.primary);

        binding.toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        binding.toolbar.setTitle(R.string.menu_donate);

        binding.buttonAlipay.setOnClickListener(this);
        tintBackground(binding.buttonAlipay, color);

        binding.buttonPaypal.setOnClickListener(this);
        tintBackground(binding.buttonPaypal, color);

        binding.buttonWechat.setOnClickListener(this);
        tintBackground(binding.buttonWechat, color);
    }

    public void tintBackground(View view, int color) {
        if (view == null) {
            return;
        }
        Drawable drawable = view.getBackground();
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN)
            view.setBackground(drawable);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        switch (v.getId()) {
            case R.id.button_wechat:
                intent.setClassName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
                try {
                    intent.putExtra("biokevi", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startActivity(intent);
                break;
            case R.id.button_alipay:
                ((ClipboardManager) getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("text", "#\u5431\u53e3\u4ee4#NvaMMJ95Pt"));
                intent.setClassName("com.eg.android.AlipayGphone", "com.alipay.mobile.quinox.LauncherActivity");
                try {
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_paypal:
                startActivity(new Intent("android.intent.action.VIEW",
                        Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=liubaoyua@gmail.com")));
                break;
        }

    }
}


