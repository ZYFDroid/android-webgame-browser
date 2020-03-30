package com.example.majsoulwindows;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
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

import static android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION;
import static wei.mark.standout.Utils.no;
import static wei.mark.standout.Utils.yes;

public class LauncherActivity extends Activity {

    public static final int OVERLAY_PERMISSION_REQ_CODE = 4331;
    public static final int STORAGE_PERMISSION_REQ_CODE = 4332;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        clickcount=0;

        if(hasFloatWindow()){
            findViewById(R.id.btnFloat).setVisibility(View.GONE);
        }

        if(hasStorage()){
            findViewById(R.id.btnStorage).setVisibility(View.GONE);
        }


    }

    public boolean hasFloatWindow(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    public boolean hasScreenCapture(){
        return getSharedPreferences("0",0).getBoolean("screenshot",false);
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
            Toast.makeText(this, "存储权限没有获取，省流量功能被禁用", Toast.LENGTH_SHORT).show();
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

    public void introduce(View view) {
        String msg="雀魂麻将Majsoul Windows Ver~ 客户端\n" +
                "\n" +
                "-最大化/最小化隐藏/自由调整大小(需要悬浮窗权限)\n" +
                "-自主研发(可能)缓存机制，加载更快，更省流量\n" +
                "-复制牌谱/好友房链接打开客户端会有提示(安卓10.0不支持)\n" +
                "-半透明窗口功能\n" +
                "-恶搞截图功能(点击窗口图标，有个下拉菜单)\n" +
                "\n" +
                "\n" +
                "本软件通过系统浏览器实现，仅呈现 https://www.majsoul.com/1/ 的内容，部分太老的系统和安卓模拟器可能打不开\n" +
                "* “雀魂麻将Majsoul” 是由 上海腾娱网络科技有限公司 开发和提供服务的产品。官网地址\n" +
                "https://www.majsoul.com/";
        AlertDialog ald =  new AlertDialog.Builder(this).setTitle("功能介绍").setMessage(msg).setPositiveButton(android.R.string.ok,null).create();
        ald.show();
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

    public void close(View view) {
        finish();
    }


    int clickcount=0;
    public void hidefunc(View view) {
        clickcount++;
        if(clickcount<10){return;}
        String msg="打开/sdcard/Android/data/<PKGNAME>/files 可以看到三个文件夹。其中：\n" +
                "\n" +
                "webres存放的是魔改参考（本地缓存）\n" +
                "patch存放的是魔改补丁（目录结构要和webres相同）\n" +
                "mods存放的是插件，相当于油猴脚本，编码为UTF-8\n" +
                "剩下的你懂的\n" +
                "\n" +
                "推荐插件：\n" +
                "最近大铳：https://github.com/paulzzh/Majsoul-Chong\n" +
                "\n" +
                "最后提醒：\n" +
                "魔改千万条，安全第一条。\n" +
                "使用不规范，账号两行泪。\n" +
                "\n" +
                "参考：\n" +
                "MajsoulPlus https://github.com/MajsoulPlus/majsoul-plus";
        AlertDialog ald =  new AlertDialog.Builder(this).setTitle("里功能介绍").setMessage(msg.replace("<PKGNAME>",getPackageName())).setPositiveButton(android.R.string.ok,null).create();
        ald.show();
    }

    public void setResolution(View view) {
        new AlertDialog.Builder(this).setTitle("设置分辨率").setItems(resolutions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String str = resolutions[i];
                String resstr = str.split("\\s")[0];
                String[] resp = resstr.split("x");

                getSharedPreferences("0",0).edit().putInt("rw",Integer.parseInt(resp[0])).commit();
                getSharedPreferences("0",0).edit().putInt("rh",Integer.parseInt(resp[1])).commit();

                Toast.makeText(LauncherActivity.this, "已设置"+resstr, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(LauncherActivity.this, "已设置"+servername[i], Toast.LENGTH_SHORT).show();
            }
        }).create().show();

    }
}
