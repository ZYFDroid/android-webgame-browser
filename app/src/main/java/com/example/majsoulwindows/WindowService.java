package com.example.majsoulwindows;
import android.app.*;
import android.os.*;
import android.content.*;
import android.widget.*;
import android.view.*;
import android.graphics.*;
import android.util.*;

import wei.mark.standout.StandOutWindow;

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
    private void createToucher()
    {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        if (Build.VERSION.SDK_INT >= 26) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = getSharedPreferences("0",0).getInt("bx",0);
        params.y = getSharedPreferences("0",0).getInt("by",0);
        blx = params.x;
        bly = params.y;
        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = dip2px(this,36);
        params.height = dip2px(this,36);

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = (LinearLayout) inflater.inflate(R.layout.fwd,null);
        //添加toucherlayout
        windowManager.addView(toucherLayout,params);

        Log.i(TAG,"toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG,"toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG,"toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG,"toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED,View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
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
                    if(isDragged){
                        Log.i(TAG,"拖动了");
                    }
                    else{
                        Log.i(TAG,"点击了");
                        FrmBrowser.isAnimShow = true;

                        StandOutWindow.show(getApplicationContext(), FrmBrowser.class, StandOutWindow.DEFAULT_ID);

                        getSharedPreferences("0",0).edit().putInt("bx",blx).commit();
                        getSharedPreferences("0",0).edit().putInt("by",bly).commit();

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
        //用imageButton检查悬浮窗还在不在，这里可以不要。优化悬浮窗时要用到。
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
