package com.example.majsoulwindows;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.SslError;
import android.os.Build;
import android.util.Log;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Vector;
import android.os.Handler;

import wei.mark.standout.Utils;

/**
 * Created by ZYFDroid on 2020-03-31.
 */

public class WebGameBoostEngine {
    public static void boost(final Context ctx,final WebView mWebView, final String baseUrl){
        final String cachepref  = Utils.getSP(ctx).getString("tmp","zh");
        if(null==hWnd){hWnd=new Handler();}
        mWebView.setKeepScreenOn(true);
        mWebView.setWebViewClient(new WebViewClient() {
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
                            File patch = new File(urlToLocalPath(resUrl,getPatchDir()));



                            String type = "*.*";
                            if (mimt.hasExtension(MimeTypeMap.getFileExtensionFromUrl(resUrl))) {
                                type = mimt.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(resUrl));
                            }
                            if (resUrl.endsWith("/")) {
                                type = "text/html";
                            }
                            if (patch.exists() && !resUrl.endsWith("/code.js")) {

                                try {
                                    if(BuildConfig.DEBUG)
                                        Log.v("USES_PATCH", resUrl + " -> " + urlToLocalPath(resUrl, getPatchDir()));


                                    return new WebResourceResponse(type, null, new FileInputStream(patch));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            else if(cache.exists()){
                                try {
                                    if(BuildConfig.DEBUG)
                                        Log.v("USES_CACHE", resUrl + " -> " + urlToLocalPath(resUrl, getBaseDir()));

                                    if(resUrl.endsWith("/code.js")){
                                        return new WebResourceResponse(type, null, getModedStream(new FileInputStream(cache)));
                                    }

                                    return new WebResourceResponse(type, null, new FileInputStream(cache));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try {

                                    String source = resUrl.replace("<INDEX>", "");
                                    String dest = cache.getAbsolutePath();

                                    AsyncWebDownloader downer = new AsyncWebDownloader(request,source,dest);
                                    downer.start();
                                    InputStream cacheIs = downer.getParallelInputStream();


                                    if(BuildConfig.DEBUG)
                                        Log.v("MAKE_CACHE", source + " -> " + dest);

                                    if(resUrl.endsWith("/code.js")){
                                        return new WebResourceResponse(type, null, getModedStream(cacheIs));
                                    }
                                    return new WebResourceResponse(type, null, cacheIs);
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
                if(BuildConfig.DEBUG)
                    Log.v("DIRECTLOAD_NOCACHE", resUrl);
                return super.shouldInterceptRequest(view, request);
            }

            public InputStream getModedStream(InputStream orignal){
                Vector<InputStream> patches = new Vector<InputStream>();
                patches.add(orignal);


                File patchList = new File(getModDir());
                if(!patchList.exists()){
                    patchList.mkdirs();
                }
                for (File p :
                        patchList.listFiles()) {
                    if(p.getName().endsWith(".js")){
                        try {
                            patches.add(crlfStream());
                            patches.add(new FileInputStream(p));
                            if(BuildConfig.DEBUG) {
                                Log.v("LOAD_MOD", p.getAbsolutePath());
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                return new SequenceInputStream(patches.elements());
            }

            InputStream crlfStream(){
                return new ByteArrayInputStream("\r\n\r\n\r\n".getBytes());
            }


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }

            @TargetApi(27)
            @Override
            public void onSafeBrowsingHit(WebView view, WebResourceRequest request, int threatType, SafeBrowsingResponse callback) {
                callback.proceed(false);
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
                File baseFile = new File(baseDir);
                if(!baseFile.exists()){
                    try{
                        baseFile.mkdirs();
                    }catch (Exception ex){}
                }

                return url.replace(baseUrl, baseDir);
            }

            String getBaseDir() {
                String path = ctx.getExternalFilesDir(null).getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                return path +cachepref+ "/webres/";
            }

            String getPatchDir() {
                String path = ctx.getExternalFilesDir(null).getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                return path +cachepref+ "/patch/";
            }
            String getModDir() {
                String path = ctx.getExternalFilesDir(null).getAbsolutePath();
                if (!path.endsWith("/")) {
                    path += "/";
                }
                return path +cachepref+ "/mods/";
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, final String message, JsResult result) {
                hWnd.post(new Runnable() {
                    @Override
                    public void run() {
                        Utils.showDialog(ctx, message);
                    }
                });
                return super.onJsAlert(view, url, message, result);
            }
        });
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAppCachePath(ctx.getCacheDir().getAbsolutePath());
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setAppCacheEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        //mWebView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        //this.mWebView.setWebContentsDebuggingEnabled(true);
    }
    private static android.os.Handler hWnd;
}


class AsyncWebDownloader extends Thread{
    private java.io.PipedInputStream pin;
    private java.io.PipedOutputStream pout;
    private WebResourceRequest req;
    private String source;
    private String dest;

    private int available = -2;

    public AsyncWebDownloader(WebResourceRequest req, String source, String dest) throws IOException {
        this.req = req;
        this.source = source;
        this.dest = dest;
        pin = new PipedInputStream();
        pout = new PipedOutputStream();
        pin.connect(pout);
    }

    @Override
    public void run(){
        try {
            HttpURLConnection conn = null;
            InputStream is = null;
            byte[] buffer = new byte[4096];
            synchronized (this) {
                conn = (HttpURLConnection) new URL(source).openConnection();
                conn.setRequestMethod("GET");

                boolean downloadFirst = false;

                for (Map.Entry<String, String> header :
                        req.getRequestHeaders().entrySet()) {
                    if (!header.getKey().equals("Range")) {
                        conn.setRequestProperty(header.getKey(), header.getValue());
                    } else {
                        downloadFirst = true;
                    }
                }

                if(!downloadFirst){available=-3;}
                conn.connect();
                is = conn.getInputStream();
                if(downloadFirst)
                try {
                    available = conn.getContentLength();
                } catch (Exception ex) {
                    available = -1;
                }
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            boolean canWrite = true;
            int len = 0;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
                os.flush();
                try{
                    if(canWrite){
                        pout.write(buffer,0,len);
                        pout.flush();
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                    canWrite = false;
                }
            }
            is.close();
            try{
                pout.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
            File cache = new File(dest);
            cache.getParentFile().mkdirs();
            cache.createNewFile();
            OutputStream fos = new FileOutputStream(cache);
            os.writeTo(fos);
            os.close();
            fos.close();
            conn.disconnect();

        }catch (IOException ex){
            ex.printStackTrace();
            try {
                pout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public InputStream getParallelInputStream(){
        return new WrappedInputStream(pin);
    }


    class WrappedInputStream extends InputStream{

        public InputStream baseStream;

        public WrappedInputStream(InputStream baseStream) {
            this.baseStream = baseStream;
        }

        @Override
        public int read() throws IOException {
            return baseStream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            return baseStream.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            return baseStream.read(b, off, len);
        }

        @Override
        public int available() throws IOException {
            while (available<-1 && available!=-3) {
                try{
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    throw new IOException();
                }
            }
            if(available>=0) {
                return available;
            }
            return baseStream.available();
        }

        @Override
        public boolean markSupported() {
            return baseStream.markSupported();
        }

        @Override
        public long skip(long n) throws IOException {
            return baseStream.skip(n);
        }

        @Override
        public void close() throws IOException {
            baseStream.close();
        }

        @Override
        public synchronized void mark(int readlimit) {
            baseStream.mark(readlimit);
        }

        @Override
        public synchronized void reset() throws IOException {
            baseStream.reset();
        }
    }



}

