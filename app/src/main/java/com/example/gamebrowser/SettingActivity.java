package com.example.gamebrowser;

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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wei.mark.standout.StandOutWindow;
import wei.mark.standout.Utils;

import static wei.mark.standout.Utils.no;
import static wei.mark.standout.Utils.yes;

public class SettingActivity extends Activity {
    public static final String defaultPage = "file:////android_asset/help.html";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
    }

//    public boolean hasStorage(){
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
//        }
//        return true;
//    }

//    public void debug(View view) {
//        if(!hasStorage()){
//            Toast.makeText(this, "存储权限没有获取", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        try {
//            Process ps = Runtime.getRuntime().exec("sh");
//            PrintStream p = new PrintStream(ps.getOutputStream());
//            p.println("logcat -v time > /sdcard/不科学的Logcat.txt");
//            p.println();
//            p.close();
//            view.setEnabled(false);
//        } catch (IOException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "无法获取Logcat，"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//        }
//
//    }

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

        Utils.getSP(SettingActivity.this).edit().putBoolean("clear",true).commit();

        try{

            StandOutWindow.close(this,FrmBrowser.class,StandOutWindow.DEFAULT_ID);

        }catch (Exception ex){}
        try {
            stopService(new Intent(getApplicationContext(), FrmBrowser.class));
        }catch (Exception ex){}
        try {
            stopService(new Intent(getApplicationContext(), WindowService.class));
        }catch (Exception ex){}


        Toast.makeText(this, "重置完成，重启生效", Toast.LENGTH_SHORT).show();
    }



    public String[] resolutions={
            "640x360 (最低能玩分辨率)",
            "854x480 (480P,省电首选)",
            "960x540 (IPhone4)",
            "1024x600 (华强北平板电脑)",
            "1280x720 (720P)",
            "1366x768 (一般笔记本电脑)",
            "1600x900 (...)",
            "1920x1080 (1080P)",
            "2560x1440 (2K)",
            "4096x2160 (4K)"
    };

    public void setResolution(View view) {
        new AlertDialog.Builder(this).setTitle("设置分辨率").setItems(resolutions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String str = resolutions[i];
                String resstr = str.split("\\s")[0];
                String[] resp = resstr.split("x");

                Utils.getSP(SettingActivity.this).edit().putInt("rw",Integer.parseInt(resp[0])).putInt("rh",Integer.parseInt(resp[1])).commit();

                Toast.makeText(SettingActivity.this, "已设置"+resstr, Toast.LENGTH_SHORT).show();
            }
        }).create().show();
    }

    public void setServer(View view) {
        final List<GameEntry> gameList = getGameList(this);


        new AlertDialog.Builder(this).setTitle("可用游戏列表").setAdapter(new GameAdapter(gameList), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setGame(gameList.get(which));
            }
        }).create().show();

    }

     class GameAdapter extends ArrayAdapter<GameEntry>{
        public GameAdapter(List<GameEntry> objects) {
            super(SettingActivity.this,android.R.layout.simple_list_item_1,android.R.id.text1,objects);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            ((TextView)v.findViewById(android.R.id.text1)).setText(getItem(position).name);
            return v;
        }
    }

    public void setGame(GameEntry entry){
        Utils.getSP(SettingActivity.this).edit().putString("url",entry.url).putString("tmp",entry.uuid).putString("windowtext",entry.name).commit();
    }

    public static List<GameEntry> getGameList(Context ctx){
        ArrayList<GameEntry> gameList = new ArrayList<>();

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(ctx.getResources().getAssets().open("gamelist.json")));
            String line = "";
            StringBuilder out = new StringBuilder();
            while ((line=reader.readLine())!=null){
                out.append(line).append("\n");
            }
            reader.close();
            JSONArray jarr = new JSONArray(out.toString());
            for (int i = 0; i < jarr.length(); i++) {
                JSONObject jobj = jarr.getJSONObject(i);
                gameList.add(new GameEntry(jobj.getString("guid"),jobj.getString("name"),jobj.getString("url")));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(ctx, "读取游戏列表失败", Toast.LENGTH_SHORT).show();
        } catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(ctx, "读取游戏列表失败", Toast.LENGTH_SHORT).show();
        }
        return gameList;
    }

    public static class GameEntry{
        public String uuid,name,url;
        public GameEntry(String uuid, String name, String url) {
            this.uuid = uuid;
            this.name = name;
            this.url = url;
        }
    }

    public static String[] windowStyle={"默认","Win7","WindowsXP"};

    public void setWindowStyle(View view) {
        new AlertDialog.Builder(this).setTitle("选择窗口边框样式").setItems(windowStyle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Utils.getSP(SettingActivity.this).edit().putString("borderstyle",windowStyle[i]).commit();
                Toast.makeText(SettingActivity.this, "已设置边框样式："+windowStyle[i], Toast.LENGTH_SHORT).show();
            }
        }).create().show();
    }

    public void loadPlugin(View view) {
//        if(!hasStorage()){
//            Toast.makeText(this, "存储权限没有获取", Toast.LENGTH_SHORT).show();
//            return;
//        }
        startActivity(new Intent(this,PluginActivity.class));
        finish();
    }

}
