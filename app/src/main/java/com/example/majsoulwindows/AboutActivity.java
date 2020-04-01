package com.example.majsoulwindows;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about);
    }


    public void introduce(View view) {
        String msg="雀魂麻将Majsoul Windows Ver~ 客户端\n" +
                "\n" +
                "-最大化/最小化隐藏/自由调整大小(需要悬浮窗权限)\n" +
                "-自主研发(可能)缓存机制，加载更快，更省流量\n" +
                "-复制牌谱/好友房链接打开客户端会有提示(安卓10.0不支持)\n" +
                "-半透明窗口功能\n" +
                "-切换服务器\n" +
                "-恶搞截图功能(点击窗口图标，有个下拉菜单)\n" +
                "\n" +
                "\n" +
                "本软件通过系统浏览器实现，仅呈现 https://www.majsoul.com/1/ 的内容，部分太老的系统和安卓模拟器可能打不开\n";
        AlertDialog ald =  new AlertDialog.Builder(this).setTitle("功能介绍").setMessage(msg).setPositiveButton(android.R.string.ok,null).create();
        ald.show();
    }
}
