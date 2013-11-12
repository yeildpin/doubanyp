package com.example.doubanyp;

import com.example.doubanyp.util.ConfigData;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.client.douban.DoubanService;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BaseListActivity extends ListActivity {
	protected SharedPreferences sharedata;

	protected void initDouban() {
		String accessToken = sharedata.getString(ConfigData.ACCESSTOKEN, null);
		String tokenSecret = sharedata.getString(ConfigData.TOKENSECRET, null);
		if(NetUtil.doubanService == null)
			NetUtil.doubanService = new DoubanService("DoubanYP",
					NetUtil.apiKey, NetUtil.secret);
		if (accessToken != null && tokenSecret != null) {
			NetUtil.doubanService.setAccessToken(accessToken, tokenSecret);
			NetUtil.isAuthed = true;
		} else {
			NetUtil.isAuthed = false;
		}
	}
	
	protected void doAuth(){
		new AlertDialog.Builder(BaseListActivity.this)
		.setTitle("提示")
		.setMessage("用户未登录或授权已过期，请先登录！")
		.setPositiveButton("登录", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialoginterface, int i) {
				Intent intent = new Intent(BaseListActivity.this, AuthActivity.class);
				startActivityForResult(intent, 0);
				finish();
			}
		}).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (sharedata == null)
			sharedata = getSharedPreferences(ConfigData.TAG, 0);
		initDouban();
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
