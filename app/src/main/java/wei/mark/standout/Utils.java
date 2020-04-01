package wei.mark.standout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class Utils {
	public static boolean isSet(int flags, int flag) {
		return (flags & flag) == flag;
	}

	public static final String yes="是的";
	public static final String no ="不是";
	public static final String uhh="好";

	public static void Confirm(Context ctx, String msg, final Runnable action){
		AlertDialog ald =  new AlertDialog.Builder(ctx).setMessage(msg).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				action.run();
			}
		}).setNegativeButton(no,null).create();
		if (Build.VERSION.SDK_INT >= 26) {
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
		}
		else{
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		ald.show();
	}

	public static void showDialog(Context ctx, String msg){
		AlertDialog ald =  new AlertDialog.Builder(ctx).setMessage(msg).setPositiveButton(uhh,null).create();

		if (Build.VERSION.SDK_INT >= 26) {
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
		}
		else{
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		ald.show();
	}

	public static void Prompt(Context ctx, String msg, final OnPromptResult action){
		AlertDialog ald =  new AlertDialog.Builder(ctx).setMessage(msg).setPositiveButton(yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialogInterface, int i) {
				action.onResult(true);
			}
		}).setNegativeButton(no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				action.onResult(false);
			}
		}).setCancelable(false).create();
		if (Build.VERSION.SDK_INT >= 26) {
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
		}
		else{
			ald.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		}
		ald.show();
	}

	public static interface OnPromptResult{
		public void onResult(boolean isYesPressed);
	}


	public static abstract class EditDialog
	{
		String title;
		Context ctx;
		String def;
		public EditDialog(Context mctx,String mtitle,String defaultValue){
			title=mtitle;
			ctx=mctx;
			def=defaultValue;
		}

		public void show(){
			onCreate();

			if (Build.VERSION.SDK_INT >= 26) {
				adbd.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
			}
			else{
				adbd.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			}
			adbd.show();
		}

		EditText edt;
		AlertDialog adbd;
		protected void onCreate()
		{
			AlertDialog.Builder adb = new AlertDialog.Builder(ctx);
			adb.setTitle(title);
			LinearLayout base=new LinearLayout(ctx);
			base.setOrientation(base.VERTICAL);
			base.setGravity(Gravity.CENTER);
			edt=new EditText(ctx);
			base.addView(edt);
			LinearLayout.LayoutParams lyp=(LinearLayout.LayoutParams)edt.getLayoutParams();
			lyp.width=lyp.MATCH_PARENT;
			edt.setLayoutParams(lyp);
			edt.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			edt.setText(def);
			LinearLayout btnbase=new LinearLayout(ctx);
			btnbase.setOrientation(btnbase.HORIZONTAL);
			btnbase.setGravity(Gravity.CENTER);
			base.addView(btnbase);
			lyp=(LinearLayout.LayoutParams)btnbase.getLayoutParams();
			lyp.width=lyp.MATCH_PARENT;
			btnbase.setLayoutParams(lyp);
			Button btnCancel=new Button(ctx);
			btnCancel.setText("取消");
			btnCancel.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					adbd.dismiss();
				}
			});
			Button btnOk=new Button(ctx);
			btnOk.setText("确定");
			btnOk.setOnClickListener(new Button.OnClickListener(){
				@Override
				public void onClick(View p1)
				{

					onConfirmText(edt.getText().toString());
					adbd.dismiss();
				}
			});
			btnbase.addView(btnCancel);
			btnbase.addView(btnOk);
			adb.setView(base);
			adbd = adb.create();
		}


		public abstract void onConfirmText(String text);
	}

}


