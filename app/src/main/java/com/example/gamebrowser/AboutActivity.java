package com.example.gamebrowser;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import wei.mark.standout.Utils;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }


    public void introduce(View view) {
        String msg="电竞浏览器\n" +
                "\n（窗口模式下，选中文本需要点左上角图标复制）\n" +
                "-最大化/最小化隐藏/自由调整大小(需要悬浮窗权限)\n" +
                "-随手研发(可能)加速引擎，游戏载入更快，更省流量\n" +
                "-半透明窗口功能\n" +
                "-全屏启动，沉浸模式，退出确认，客户端级体验（全屏模式下支持分屏）\n" +
                "\n" +
                "\n" +
                "本软件通过系统浏览器实现，部分太老的系统和安卓模拟器可能打不开\n" +
                "若您正在玩的游戏有Android版客户端，推荐使用官方客户端而不是浏览器\n" +
                "如果你喜欢这个项目，请在github为我点个star（如果你不知道什么是github就算了）";
        AlertDialog ald =  new AlertDialog.Builder(this).setTitle("功能介绍").setMessage(msg).setPositiveButton(android.R.string.ok,null).create();
        ald.show();
        Utils.setDialogVersion(this,"firstrun",2);
    }

    public void github(View view) {
        openUrl("https://github.com/ZYFDroid/android-webgame-browser");
    }

    public void update(View view) {
        openUrl("https://github.com/ZYFDroid/android-webgame-browser/releases");
    }
    
    void openUrl(String url){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse(url);
            intent.setData(uri);
            startActivity(intent);
        }catch (Exception ex){
            Toast.makeText(this, "建议先安装一个浏览器", Toast.LENGTH_SHORT).show();
        }
    }
}
