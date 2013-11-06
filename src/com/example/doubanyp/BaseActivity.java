package com.example.doubanyp;

import com.example.doubanyp.util.ConfigData;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.client.douban.DoubanService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseActivity extends Activity {

	protected ProgressDialog pd;
	protected SharedPreferences sharedata;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		sharedata = getSharedPreferences(ConfigData.TAG, 0);
	}

	protected void initDouban(){
		String accessToken = sharedata.getString(ConfigData.ACCESSTOKEN, null);
		String tokenSecret = sharedata.getString(ConfigData.TOKENSECRET, null);
		if(accessToken != null && tokenSecret != null){
			NetUtil.doubanService.setAccessToken(accessToken, tokenSecret);
		}
		else{
			NetUtil.doubanService = new DoubanService("DoubanYP", NetUtil.apiKey, NetUtil.secret);
		}
	}
	
	protected void doAuth(){
		new AlertDialog.Builder(BaseActivity.this)
		.setTitle("提示")
		.setMessage("用户未登录或授权已过期，请先登录！")
		.setPositiveButton("登录", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				Intent intent = new Intent(getApplicationContext(), AuthActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		}).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// 退出
	protected void doExit() {
		new AlertDialog.Builder(BaseActivity.this)
				.setTitle("提示")
				.setMessage("确定要退出我的豆瓣吗？")
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						finish();
					}
				})
				.setNeutralButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}

				}).show();

	}

	// 加载对话框
	public void showDialog() {
		pd = ProgressDialog.show(BaseActivity.this, "信息", "加载数据中...");
	}
	
	public void closeDialog(){
		pd.dismiss();
	}

	public void showProgressBar() {
		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		loading.setVisibility(View.VISIBLE);
		loading.setLayoutAnimation(controller);
	}

	public void closeProgressBar() {

		AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
		animation.setDuration(500);
		set.addAnimation(animation);

		LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);

		loading.setLayoutAnimation(controller);

		loading.setVisibility(View.INVISIBLE);
	}

	public void showProgressBar(String title) {
		TextView loading = (TextView) findViewById(R.id.txt_loading);
		loading.setText(title);
		showProgressBar();
	}
}
