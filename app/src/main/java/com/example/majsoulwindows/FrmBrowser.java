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
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.JsResult;
import android.webkit.MimeTypeMap;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    //public static boolean isRunning = false;


    public HashMap<String,String> clipboardFinder = new HashMap<>();

    private static String baseUrl = "https://www.majsoul.com/1/";
    WebView mWebView;

    @Override
    public String getAppName() {
        return "雀魂麻将majsoul - Windows Ver~";
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        this.mWebView = new WebView(this);
        frame.addView(mWebView);
        //this.mWebView.setWebViewClient(new baseUrl(this));
        this.mWebView.setKeepScreenOn(true);
        this.mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return !url.startsWith(baseUrl);
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return !request.getUrl().toString().startsWith(baseUrl);
            }

            MimeTypeMap mimt = MimeTypeMap.getSingleton();

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String resUrl = request.getUrl().toString();
                if (resUrl.equals(baseUrl)) {
                    resUrl += "<INDEX>";
                }
                try {

                    if (request.getMethod().equals("GET")) {
                        if (shouldCache(resUrl)) {

                            File cache = new File(urlToLocalPath(resUrl, getBaseDir()));
                            String type = "*.*";
                            if (mimt.hasExtension(MimeTypeMap.getFileExtensionFromUrl(resUrl))) {
                                type = mimt.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resUrl));
                            }
                            if (resUrl.endsWith("/")) {
                                type = "text/html";
                            }
                            if (cache.exists()) {

                                try {
                                    Log.e("USES_CACHE", resUrl + " -> " + urlToLocalPath(resUrl, getBaseDir()));
                                    return new WebResourceResponse(type, null, new FileInputStream(cache));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    cache.getParentFile().mkdirs();
                                    cache.createNewFile();
                                    HttpURLConnection conn = (HttpURLConnection) new URL(resUrl.replace("<INDEX>", "")).openConnection();
                                    conn.setRequestMethod("GET");
                                    for (Map.Entry<String, String> header :
                                            request.getRequestHeaders().entrySet()) {
                                        conn.setRequestProperty(header.getKey(), header.getValue());
                                    }
                                    conn.connect();
                                    byte[] buffer = new byte[4096];
                                    InputStream is = conn.getInputStream();
                                    OutputStream os = new FileOutputStream(cache);
                                    int len = 0;
                                    while ((len = is.read(buffer)) != -1) {
                                        os.write(buffer, 0, len);
                                        os.flush();
                                    }
                                    is.close();
                                    os.close();
                                    conn.disconnect();
                                    Log.e("MAKE_CACHE", resUrl + " -> " + urlToLocalPath(resUrl, getBaseDir()));
                                    return new WebResourceResponse(type, null, new FileInputStream(cache));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    try {
                                        cache.delete();
                                    } catch (Exception exxx) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Log.e("DIRECTLOAD_NOCACHE", resUrl);
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public void onReceivedSslError(WebView view,
                                           SslErrorHandler handler, SslError error) {
                // handler.cancel();// Android默认的处理方式
                handler.proceed();// 接受所有网站的证书
                // handleMessage(Message msg);// 进行其他处理
            }


            boolean shouldCache(String url) {
                if (!url.startsWith(baseUrl)) {
                    return false;
                }
                if (url.contains("?")) {
                    return false;
                }

                if(url.endsWith("<INDEX>")){return false;}

                String path = url.replace(baseUrl, "");

                //if(path.endsWith("json") && !path.contains("/")){
                //    return false;
                //}

                return true;
            }

            String urlToLocalPath(String url, String baseDir) {
                return url.replace(baseUrl, baseDir);
            }

            String getBaseDir() {
                String path = getExternalFilesDir("").getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                return path + "hardcache/1/";
            }
        });
        this.mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
                hWnd.post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showDialog(FrmBrowser.this, message);
                    }
                });
                return super.onJsAlert(view, url, message, result);
            }
        });
        WebSettings settings = this.mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCachePath(getCacheDir().getAbsolutePath());
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);

        //mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //this.mWebView.setWebContentsDebuggingEnabled(true);

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
        mWebView.loadUrl(baseUrl);
    }

    public void loadClipboardMap(){
        clipboardFinder.put("https://www.majsoul.com/1/?room=","好友房链接");
        clipboardFinder.put("https://www.majsoul.com/1/?paipu=","牌谱链接");
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

        list.add(new DropDownListItem(android.R.drawable.ic_menu_compass, "恶搞截图", new Runnable() {
            @Override
            public void run() {

                new Utils.EditDialog(FrmBrowser.this,"输入对话框内容","您吃了把四，要卸载游戏吗？"){
                    @Override
                    public void onConfirmText(final String text) {
                        Utils.Prompt(FrmBrowser.this, text, new Utils.OnPromptResult() {
                            @Override
                            public void onResult(boolean isYesPressed) {

                            }
                        });
                    }
                }.show();
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
//                | StandOutFlags.FLAG_DECORATION_CLOSE_DISABLE
                |StandOutFlags.FLAG_WINDOW_BRING_TO_FRONT_ON_TAP
                |StandOutFlags.FLAG_WINDOW_HIDE_ENABLE
//                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
//                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
//                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
//                | StandOutFlags.FLAG_WINDOW_EDGE_LIMITS_ENABLE

                |StandOutFlags.FLAG_BODY_MOVE_ENABLE
                |StandOutFlags.FLAG_DECORATION_SYSTEM;
    }

    @Override
    public String getPersistentNotificationMessage(int id) {
        return getResources().getString(R.string.app_name)+" 正在运行";
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
