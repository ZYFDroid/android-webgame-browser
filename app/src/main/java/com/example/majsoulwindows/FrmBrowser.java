package com.example.majsoulwindows;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.SafeBrowsingResponse;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;
import java.lang.reflect.ParameterizedType;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

/**
 * Created by WorldSkills2020 on 10/23/2019.
 */

public class FrmBrowser extends StandOutWindow {

    public static boolean isRunning = false;


    public HashMap<String,String> clipboardFinder = new HashMap<>();

    public static String baseUrl = "https://www.majsoul.com/1/";
    WebView mWebView;

    @Override
    public String getAppName() {
        return getSharedPreferences("0",0).getString("wndtext","雀魂麻将majsoul - Windows Ver~");
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic;
    }



    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        baseUrl = getSharedPreferences("0",0).getString("url","https://www.majsoul.com/1/");

        isRunning = true;

        this.mWebView = new WebView(this);
        frame.addView(mWebView);
        renderW = getSharedPreferences("0",0).getInt("rw",854);
        renderH = getSharedPreferences("0",0).getInt("rh",480);
        //this.mWebView.setWebViewClient(new baseUrl(this));

        WebGameBoostEngine.boost(this,mWebView,baseUrl);

        if (Build.VERSION.SDK_INT <= 28) {
            loadClipboardMap();
            String clipboardData = paste(this);
            for (String key : clipboardFinder.keySet()) {
                if (clipboardData.contains(key)) {
                    final String targetUrl = findFirstUrl(clipboardData);
                    Utils.Prompt(this, "您复制了一个 " + clipboardFinder.get(key) + "，打开它吗？", new Utils.OnPromptResult() {
                        @Override
                        public void onResult(boolean isYesPressed) {
                            if (isYesPressed) {
                                mWebView.loadUrl(targetUrl);
                            } else {
                                mWebView.loadUrl(baseUrl);

                            }

                            Utils.Prompt(FrmBrowser.this, "是否删除剪切板中的链接？", new Utils.OnPromptResult() {
                                @Override
                                public void onResult(boolean isYesPressed) {
                                    if (isYesPressed) {
                                        clearClipboard();
                                    }
                                }
                            });

                        }
                    });
                    return;
                }
            }
        }
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
        frame.setClickable(true);
        lp.width=renderW;
        lp.height = renderH;
        mWebView.setLayoutParams(lp);
        lp.gravity = Gravity.CENTER;
        rootView = frame;
        scaleView();
        mWebView.loadUrl(baseUrl);
    }

    int renderW=854,renderH=480;

    public void loadClipboardMap(){
        clipboardFinder.put(baseUrl+"?room=","好友房链接");
        clipboardFinder.put(baseUrl+"?paipu=","牌谱链接");
    }



    public void clearClipboard(){
        ClipboardManager manager = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if (manager != null) {
            try {
                manager.setPrimaryClip(manager.getPrimaryClip());
                manager.setPrimaryClip(ClipData.newPlainText(null, ""));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    FrameLayout rootView;
    void scaleView(){
        float pw = rootView.getWidth();
        float ph = rootView.getHeight();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mWebView.getLayoutParams();
        if(pw==0 || ph==0){
            Log.e("SCALE","View not initialized");
            return;
        }
        if(pw>=ph){
            lp.width=renderW;
            lp.height = renderH;
            if(pw / renderW * renderH < ph){
                //屏幕更高的场合
                mWebView.setScaleX(pw/renderW);
                mWebView.setScaleY(pw/renderW);
            }
            else{
                //屏幕更窄的场合
                mWebView.setScaleX(ph/renderH);
                mWebView.setScaleY(ph/renderH);
            }

        }
        else{
            lp.width=renderH;
            lp.height = renderW;


            if(ph / renderW * renderH < pw){
                //屏幕更高的场合
                mWebView.setScaleX(ph/renderW);
                mWebView.setScaleY(ph/renderW);
            }
            else{
                //屏幕更窄的场合
                mWebView.setScaleX(pw/renderH);
                mWebView.setScaleY(pw/renderH);
            }
        }


        mWebView.setLayoutParams(lp);
    }

    @Override
    public void onResize(int id, Window window, View view, MotionEvent event) {
        super.onResize(id, window, view, event);
        if(null!=rootView){
            scaleView();
        }
    }



    public static String paste(Context context) {
        try {
            ClipboardManager cmb = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if(cmb.getPrimaryClip().getItemCount() > 0)
            return cmb.getPrimaryClip().getItemAt(0).getText().toString().trim();
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    Handler hWnd = new Handler();

    @Override
    public Animation getCloseAnimation(int id) {
        AnimationSet anims = new AnimationSet(false);
        StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
        ScaleAnimation scale = new ScaleAnimation(1,0.75f,1f,0.75f,orignal.width/2,orignal.height/2);
        scale.setDuration(400);
        scale.setFillAfter(true);
        scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
        anims.addAnimation(scale);
        AlphaAnimation alpha = new AlphaAnimation(1,0);
        alpha.setDuration(300);
        alpha.setFillAfter(true);
        anims.addAnimation(alpha);
        isAnimShow = false;
        return anims;
    }




    @Override
    public Animation getShowAnimation(int id) {
        if(isAnimShow) {
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            int sourceX = orignal.x + orignal.width /2;
            int sourceY = orignal.y + orignal.height /2;
            int destX = getSharedPreferences("0", 0).getInt("bx", 0) + dip2px(this,18);
            int destY = getSharedPreferences("0", 0).getInt("by", 0) + dip2px(this,18);

            float xscale = (float)dip2px(this,36) / (float)orignal.width;
            float yscale = (float)dip2px(this,36) / (float)orignal.height;

            AnimationSet anims = new AnimationSet(false);

            TranslateAnimation translate = new TranslateAnimation(destX-sourceX,0,destY-sourceY,0);
            translate.setDuration(400);
            translate.setFillAfter(true);
            translate.setInterpolator(this,android.R.anim.accelerate_interpolator);
            ScaleAnimation scale = new ScaleAnimation(xscale,1f,yscale,1f,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            anims.addAnimation(translate);
            AlphaAnimation alpha = new AlphaAnimation(0,1);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            alpha.setStartOffset(100);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }
        else{

            AnimationSet anims = new AnimationSet(false);
            ScaleAnimation scale = new ScaleAnimation(0.75f,1f,0.75f,1f,ScaleAnimation.RELATIVE_TO_SELF,0.5f,ScaleAnimation.RELATIVE_TO_SELF,0.5f);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.decelerate_interpolator);
            anims.addAnimation(scale);
            AlphaAnimation alpha = new AlphaAnimation(0,1);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            alpha.setStartOffset(100);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }

    }

    @Override
    public Animation getHideAnimation(int id) {
        if(isAnimHide) {
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            int sourceX = orignal.x + orignal.width /2;
            int sourceY = orignal.y + orignal.height /2;
            int destX = getSharedPreferences("0", 0).getInt("bx", 0) + dip2px(this,18);
            int destY = getSharedPreferences("0", 0).getInt("by", 0) + dip2px(this,18);

            float xscale = (float)dip2px(this,36) / (float)orignal.width;
            float yscale = (float)dip2px(this,36) / (float)orignal.height;

            AnimationSet anims = new AnimationSet(false);

            TranslateAnimation translate = new TranslateAnimation(0,destX-sourceX,0,destY-sourceY);
            translate.setDuration(400);
            translate.setFillAfter(true);
            translate.setInterpolator(this,android.R.anim.accelerate_interpolator);
            ScaleAnimation scale = new ScaleAnimation(1f,xscale,1f,yscale,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            anims.addAnimation(translate);
            AlphaAnimation alpha = new AlphaAnimation(1,0);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            anims.addAnimation(alpha);
            isAnimHide = false;
            return anims;
        }
        else{
            AnimationSet anims = new AnimationSet(false);
            StandOutLayoutParams orignal = getWindow(id).getLayoutParams();
            ScaleAnimation scale = new ScaleAnimation(1,0.75f,1f,0.75f,orignal.width/2,orignal.height/2);
            scale.setDuration(400);
            scale.setFillAfter(true);
            scale.setInterpolator(this,android.R.anim.accelerate_interpolator);
            anims.addAnimation(scale);
            AlphaAnimation alpha = new AlphaAnimation(1,0);
            alpha.setDuration(300);
            alpha.setFillAfter(true);
            anims.addAnimation(alpha);
            isAnimShow = false;
            return anims;
        }
    }





    public static boolean isAnimShow = false;
    @Override
    public boolean onShow(int id, Window window) {

        StandOutLayoutParams lp = window.getLayoutParams();

        lp.x = getSharedPreferences("0",0).getInt("wx",100);
        lp.y = getSharedPreferences("0",0).getInt("wy",100);
        lp.width = getSharedPreferences("0",0).getInt("ww",dip2px(this,240));
        lp.height = getSharedPreferences("0",0).getInt("wh",dip2px(this,200));


        window.setLayoutParams(lp);

        hWnd.postDelayed(resizer,300);

        return super.onShow(id, window);

    }

    public static boolean isAnimHide = false;
    @Override
    public boolean onHide(int id, Window window) {
        startService(new Intent(getApplicationContext(),WindowService.class));
        getSharedPreferences("0",0).edit().putInt("wx",window.getLayoutParams().x).commit();
        getSharedPreferences("0",0).edit().putInt("wy",window.getLayoutParams().y).commit();
        getSharedPreferences("0",0).edit().putInt("ww",window.getLayoutParams().width).commit();
        getSharedPreferences("0",0).edit().putInt("wh",window.getLayoutParams().height).commit();
        isAnimHide = true;
        return super.onHide(id, window);
    }

    @Override
    public boolean onClose(int id, Window window) {
        getSharedPreferences("0",0).edit().putInt("wx",window.getLayoutParams().x).commit();
        getSharedPreferences("0",0).edit().putInt("wy",window.getLayoutParams().y).commit();
        getSharedPreferences("0",0).edit().putInt("ww",window.getLayoutParams().width).commit();
        getSharedPreferences("0",0).edit().putInt("wh",window.getLayoutParams().height).commit();
        mWebView.destroy();
        isRunning = false;
        return super.onClose(id, window);
    }

    @Override
    public List<DropDownListItem> getDropDownItems(int id) {
        List<DropDownListItem> list = new ArrayList<>();
        list.add(new DropDownListItem(android.R.drawable.ic_menu_rotate,"重新加载",new Runnable(){
            @Override
            public void run() {
                Utils.Confirm(getApplicationContext(), "是否重新载入？", new Runnable() {
                    @Override
                    public void run() {
                        mWebView.reload();
                    }
                });
            }
        }));
        list.add(new DropDownListItem(android.R.drawable.ic_menu_view, "隐藏", new Runnable() {
            @Override
            public void run() {
                hide(StandOutWindow.DEFAULT_ID);
            }
        }));


        list.add(new DropDownListItem(android.R.drawable.ic_menu_close_clear_cancel, "退出", new Runnable() {
            @Override
            public void run() {
                Utils.Confirm(getApplicationContext(), "是否退出？", new Runnable() {
                    @Override
                    public void run() {
                        close(StandOutWindow.DEFAULT_ID);
                    }
                });
            }
        }));


        list.add(new DropDownListItem(android.R.drawable.ic_menu_crop, "翻转画面", new Runnable() {
            @Override
            public void run() {
                mWebView.setRotation(mWebView.getRotation() < 1 ? 180f : 0f);
            }
        }));

        return list;
    }



    @SuppressWarnings("WrongConstant")
    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        StandOutLayoutParams slp = new StandOutLayoutParams(id, dip2px(this,240), dip2px(this,200),
                StandOutLayoutParams.CENTER, StandOutLayoutParams.CENTER,dip2px(this,200), dip2px(this,150));
        if(Build.VERSION.SDK_INT >= 26){
            slp.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else{
            slp.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }

        return slp;
    }

    public int getFlags(int id) {
        return super.getFlags(id)
                |StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
                |StandOutFlags.FLAG_DECORATION_SYSTEM;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return getResources().getString(R.string.app_name)+" 正在运行";
    }

    Runnable resizer = new Runnable() {
        @Override
        public void run() {
            if(null!=rootView){
                if(rootView.getWidth()>0) {
                    scaleView();
                    return;
                }
            }
            hWnd.postDelayed(this,300);
        }
    };

    @Override
    public void onWindowStateChanged(int id, Window window, View view) {
        super.onWindowStateChanged(id, window, view);
        hWnd.postDelayed(resizer,300);
    }

    @Override
    public Notification getPersistentNotification(int id) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("Windows","前台服务驻留通知", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
        int icon = getAppIcon();
        long when = System.currentTimeMillis();
        Context c = getApplicationContext();
        String contentTitle = getPersistentNotificationTitle(id);
        String contentText = getPersistentNotificationMessage(id);
        String tickerText = String.format("%s: %s", contentTitle, contentText);

        // getPersistentNotification() is called for every new window
        // so we replace the old notification with a new one that has
        // a bigger id
        Intent notificationIntent = getPersistentNotificationIntent(id);

        PendingIntent contentIntent = null;

        if (notificationIntent != null) {
            contentIntent = PendingIntent.getService(this, 0,
                    notificationIntent,
                    // flag updates existing persistent notification
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        Notification.Builder notification =null;

        if (Build.VERSION.SDK_INT >= 26) {
            notification = new Notification.Builder(c,"Windows");
        }
        else{
            notification= new Notification.Builder(c);
        }
        notification.setSmallIcon(icon);
        notification.setTicker(tickerText);
        notification.setWhen(when);

        notification.setContentText(contentTitle);
        notification.setContentText(contentText);
        notification.setContentIntent(contentIntent);

        //notification.setLatestEventInfo(c, contentTitle, contentText,contentIntent);

        Notification noti = notification.build();

        return noti;
    }

    @Override
    public boolean onFocusChange(int id, Window window, boolean focus) {
        if(focus){
            window.setAlpha(1.0f);
        }
        else{
            window.setAlpha(0.7f);
        }

        return super.onFocusChange(id, window, focus);
    }

    @Override
    public void onCustomButton1Click(final int id) {
        super.onCustomButton1Click(id);
    }

    @Override
    public Notification getHiddenNotification(int id) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        rootView.removeAllViews();
        try{
            mWebView.destroy();
        }catch (Exception ex){}
    }

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    static String regex_url="(https?|ftp|file)://[-A-Za-z0-9+&@#/%?=~_|!:,.;]+[-A-Za-z0-9+&@#/%=~_|]";

    private static String findFirstUrl(String data) {
        Pattern p = Pattern.compile(regex_url);
        Matcher matcher = p.matcher(data);
        while (matcher.find()) {
            String findUrl = matcher.group();
            if(findUrl.startsWith(baseUrl)){
                return  findUrl;
            }
        }
        return baseUrl;
    }
}
