package liubaoyua.customtext.ui;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
                try {
                    donateViaWeChat();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_alipay:
                try {
                    startActivity(new Intent("android.intent.action.VIEW",
                            Uri.parse("https://ds.alipay.com/?from=mobilecodec&" +
                                    "scheme=alipayqr%3A%2F%2Fplatformapi%2Fstartapp%3FsaId%3D10000007%26clientVersion%3D3.7.0.0718%26qrcode%3D" +
                                    "https%253A%252F%252Fqr.alipay.com%252Fapeiz5mouj1akmf2cc%253F_s%253Dweb-other")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.button_paypal:
                try {
                    startActivity(new Intent("android.intent.action.VIEW",
                            Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_xclick&business=liubaoyua@gmail.com")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }

    }

    private boolean donateViaWeChat() {
        File path = getPngPath();
        if (path == null) {
            return false;
        }
        try {
            InputStream is = getAssets().open("donate_wechat.png");
            byte[] buffer = new byte[0x1000];
            OutputStream os = new FileOutputStream(path);
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            is.close();
        } catch (IOException e) {
            return false;
        }
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(path));
        sendBroadcast(mediaScanIntent);
        Intent intent = new Intent("com.tencent.mm.action.BIZSHORTCUT");
        intent.setPackage("com.tencent.mm");
        intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            startActivity(intent);
            for (int i = 0; i < 0x3; ++i) {
                Toast.makeText(this, R.string.str_wechat_choose_qr_code, Toast.LENGTH_LONG).show();
            }
        } catch (Throwable t) {
        }
        return true;
    }

    private File getPngPath() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (dir == null) {
            return null;
        }

        File screenshots = new File(dir, "Screenshots");
        if (!screenshots.exists()) {
            screenshots.mkdirs();
        }
        return new File(screenshots, "donate_wechat.png");
    }
}


