package com.example.majsoulwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }


    public void introduce(View view) {
        String msg="雀魂麻将Majsoul Windows Ver~ 客户端\n" +
                "\n（第一次启动会比较慢）" +
                "-最大化/最小化隐藏/自由调整大小(需要悬浮窗权限)\n" +
                "-随手研发(可能)加速引擎，加载更快，更省流量\n" +
                "-复制牌谱/好友房链接打开客户端会有提示(安卓10.0不支持)\n" +
                "-半透明窗口功能\n" +
                "-切换服务器\n" +
                "-恶搞截图功能(点击窗口图标，有个下拉菜单)\n" +
                "-全屏启动，真正的客户端级别体验（全屏模式下支持分屏）\n" +
                "\n" +
                "\n" +
                "本软件通过系统浏览器实现，部分太老的系统和安卓模拟器可能打不开\n" +
                "如果你喜欢这个项目，请在github为我点个star（如果你不知道什么是github就算了）";
        AlertDialog ald =  new AlertDialog.Builder(this).setTitle("功能介绍").setMessage(msg).setPositiveButton(android.R.string.ok,null).create();
        ald.show();
    }

    public void github(View view) {
        openUrl("https://github.com/ZYFDroid/android-majsoul-windows");
    }

    public void update(View view) {
        openUrl("https://github.com/ZYFDroid/android-majsoul-windows/releases");
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
