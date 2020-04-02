package com.example.majsoulwindows;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.lang.Process;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;

import static android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
import static wei.mark.standout.Utils.no;
import static wei.mark.standout.Utils.yes;

public class LauncherActivity extends Activity {

    public static final int OVERLAY_PERMISSION_REQ_CODE = 4331;
    public static final int STORAGE_PERMISSION_REQ_CODE = 4332;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);


        Utils.showDialogVersion(this,"firstrun", 1,"第一次使用，请先阅读 关于 里的功能介绍。阅读一次之后该提示将不再显示");

    }

    public boolean hasFloatWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    public boolean hasStorage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void startService(){
        if(getSharedPreferences("0",0).getBoolean("clear",false)) {
            getSharedPreferences("0", 0).edit()
                    .remove("wx")
                    .remove("wy")
                    .remove("ww")
                    .remove("wh")
                    .remove("bx")
                    .remove("by")
                    .remove("clear")
                    .commit();
        }
        StandOutWindow.show(this, FrmBrowser.class, StandOutWindow.DEFAULT_ID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask();
        }
        else{
            finish();
        }
    }

    public void start(View view) {

        if(!hasFloatWindow()){
            Toast.makeText(this, "悬浮窗权限没有获取", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!hasStorage()) {
            Toast.makeText(this, "存储权限没有获取，无法使用加速功能", Toast.LENGTH_SHORT).show();
            return;
        }

        if(FullScreenActivity.isRunning){
            Toast.makeText(this, "禁止多开。如果你没有多开，请强行停止本应用以示清白", Toast.LENGTH_SHORT).show();
            return;
        }
        startAfterPermission();
    }

    void startAfterPermission(){
        startService();
        try {
            stopService(new Intent(getApplicationContext(), WindowService.class));
        }catch (Exception ex){}
    }

    public void floatwindow(View view) {
        if(Build.VERSION.SDK_INT>=23)
        {
            if(hasFloatWindow())
            {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            }else{
                try{
                    Intent intent=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(this, "手机不兼容，建议换一部", Toast.LENGTH_SHORT).show();
                }
            }
        } else{
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        }
    }

    public void storage(View view) {
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if (!hasStorage()){
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_REQ_CODE);
            }else {
                Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
        }
    }

    public void close(View view) {
        finish();
    }

    public void startfull(View view) {
        if(!hasStorage()) {
            Toast.makeText(this, "存储权限没有获取，无法使用加速功能", Toast.LENGTH_SHORT).show();
            return;
        }

        if(FrmBrowser.isRunning){
            Toast.makeText(this, "禁止多开。如果你没有多开，请强行停止本应用以示清白", Toast.LENGTH_SHORT).show();
            return;
        }

        startActivity(new Intent(this,FullScreenActivity.class));
        finish();
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            if(hasFloatWindow()){
                findViewById(R.id.btnFloat).setVisibility(View.GONE);
            }

            if(hasStorage()){
                findViewById(R.id.btnStorage).setVisibility(View.GONE);
            }
        }
    }

    public void setting(View view) {
        startActivity(new Intent(this,SettingActivity.class));
    }

    public void help(View view) {
        startActivity(new Intent(this,AboutActivity.class));
    }
}
