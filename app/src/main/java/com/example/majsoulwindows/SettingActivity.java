package com.example.majsoulwindows;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.io.PrintStream;

import wei.mark.standout.StandOutWindow;

import static wei.mark.standout.Utils.no;
import static wei.mark.standout.Utils.yes;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

    public boolean hasStorage(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    public void debug(View view) {
        if(!hasStorage()){
            Toast.makeText(this, "存储权限没有获取", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Process ps = Runtime.getRuntime().exec("sh");
            PrintStream p = new PrintStream(ps.getOutputStream());
            p.println("logcat -v time > /sdcard/不科学的Logcat.txt");
            p.println();
            p.close();
            view.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "无法获取Logcat，"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    public void resetposition(View view) {

        Confirm(this, "是否重置位置？这将会停止当前的游戏。", new Runnable() {
            @Override
            public void run() {
                clearandreset();
            }
        });
    }

    public void appinfo(View view) {
        Intent mIntent = new Intent();
        mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        mIntent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(mIntent);
    }

    void Confirm(Context ctx, String msg, final Runnable action){
        AlertDialog ald =  new AlertDialog.Builder(ctx).setMessage(msg).setPositiveButton(yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                action.run();
            }
        }).setNegativeButton(no,null).create();
        ald.show();
    }

    void clearandreset(){

        getSharedPreferences("0",0).edit().putBoolean("clear",true).commit();

        try{

            StandOutWindow.close(this,FrmBrowser.class,StandOutWindow.DEFAULT_ID);

        }catch (Exception ex){}
        try {
            stopService(new Intent(getApplicationContext(), FrmBrowser.class));
        }catch (Exception ex){}
        try {
            stopService(new Intent(getApplicationContext(), WindowService.class));
        }catch (Exception ex){}


        Toast.makeText(this, "重置完成，再次打开游戏生效", Toast.LENGTH_SHORT).show();
    }



    public String[] resolutions={
            "16x9 (Minecraft分辨率)",
            "80x45 (索爱某翻盖手机)",
            "240x135 (GBA)",
            "256x144 (任天堂红白机)",
            "320x180 (世嘉MD)",
            "400x240 (3DS)",
            "480x270 (PSP)",
            "640x360 (最低能玩分辨率)",
            "854x480 (480P,省电首选)",
            "960x540 (IPhone4)",
            "1024x600 (华强北平板电脑)",
            "1136x640 (Iphone5)",
            "1280x720 (720P)",
            "1334x750 (IPhone6/7)",
            "1366x768 (一般笔记本电脑)",
            "1440x810 (...)",
            "1600x900 (...)",
            "1920x1080 (1080P)",
            "2000x1125 (IPhone X)",
            "2560x1440 (2K)",
            "4096x2160 (4K)",
            "8192x4320 (8K)",
            "16384x8640 (试试就逝世)",
    };

    public void setResolution(View view) {
        new AlertDialog.Builder(this).setTitle("设置分辨率").setItems(resolutions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String str = resolutions[i];
                String resstr = str.split("\\s")[0];
                String[] resp = resstr.split("x");

                getSharedPreferences("0",0).edit().putInt("rw",Integer.parseInt(resp[0])).commit();
                getSharedPreferences("0",0).edit().putInt("rh",Integer.parseInt(resp[1])).commit();

                Toast.makeText(SettingActivity.this, "已设置"+resstr, Toast.LENGTH_SHORT).show();
            }
        }).create().show();
    }

    private String[] servername ={"国服（中文）","日服（日本语）","国际服（English）"};
    private String[] serverUrl={
            "https://www.majsoul.com/1/",
            "https://game.mahjongsoul.com/",
            "https://mahjongsoul.game.yo-star.com/"

    };
    private String[] cachepath={
            "zh",
            "jp",
            "en"

    };
    public void setServer(View view) {
        new AlertDialog.Builder(this).setTitle("选择服务器").setItems(servername, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                getSharedPreferences("0",0).edit().putString("url",serverUrl[i]).commit();
                getSharedPreferences("0",0).edit().putString("tmp",cachepath[i]).commit();
                Toast.makeText(SettingActivity.this, "已设置"+servername[i], Toast.LENGTH_SHORT).show();
            }
        }).create().show();

    }

}
