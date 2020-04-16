package com.example.gamebrowser;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.graphics.*;
import android.util.*;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;

public class WindowService extends Service
{
    private static final String TAG = "MainService";

    //要引用的布局文件.
    LinearLayout toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;
    static WindowService mInstance;
    ImageButton imageButton1;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;
    @Override
    public IBinder onBind(Intent p1)
    {
        // TODO: Implement this method
        return null;
    }

    @Override
    public void onCreate()
    {
        // TODO: Implement this method
        super.onCreate();
        mInstance=this;
        createToucher();
    }

    @SuppressWarnings("WrongConstant")
    private void createToucher()
    {
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        params.type = Utils.getFlagCompat();
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = Utils.getSP(this).getInt("bx",0);
        params.y = Utils.getSP(this).getInt("by",0);
        blx = params.x;
        bly = params.y;
        params.width = dip2px(this,36);
        params.height = dip2px(this,36);

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        toucherLayout = (LinearLayout) inflater.inflate(R.layout.fwd,null);
        windowManager.addView(toucherLayout,params);
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);
        int resourceId = getResources().getIdentifier("status_bar_height","dimen","android");
        if (resourceId > 0)
        {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG,"状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);
        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            boolean isDragged = false;
            float dx =0,dy=0;
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    isDragged = false;
                    dx = event.getRawX();
                    dy = event.getRawY();
                }

                if(event.getAction()==MotionEvent.ACTION_MOVE) {

                    if(!isDragged){
                        if(Math.abs(dx-event.getRawX()) + Math.abs(dy-event.getRawY()) > px2dip(getApplicationContext(),75)){
                            isDragged = true;
                        };
                    }
                    else {
                        params.x = (int) event.getRawX() - dip2px(18);
                        params.y = (int) event.getRawY() - dip2px(18) - statusBarHeight;
                        windowManager.updateViewLayout(toucherLayout, params);
                        blx = params.x;
                        bly = params.y;
                    }
                }

                if(event.getAction() == MotionEvent.ACTION_UP){
                    if(!isDragged){
                        FrmBrowser.isAnimShow = true;

                        StandOutWindow.show(getApplicationContext(), FrmBrowser.class, StandOutWindow.DEFAULT_ID);

                        Utils.getSP(WindowService.this).edit().putInt("bx",blx).putInt("by",bly).commit();

                        stopSelf();
                    }
                }

                return true;
            }
        });



        //其他代码...
    }

    private int blx=0,bly=0;

    @Override
    public void onDestroy()
    {
        if (imageButton1 != null)
        {
            windowManager.removeView(toucherLayout);
        }
        mInstance=null;
        super.onDestroy();
    }


    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    int dip2px(float dp){return dip2px(this,dp);}
}
