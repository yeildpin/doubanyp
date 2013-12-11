package com.example.doubanyp;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doubanyp.util.ConfigData;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

public class AuthActivity extends BaseActivity {
	public static final int SUCCESS = 1;

	RelativeLayout loading;
	WebView browser;

	private String requestTokenSecret;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_auth);

		TextView myTitle = (TextView) this.findViewById(R.id.myTitle);
		myTitle.setText("µÇÂ¼ÊÚÈ¨");
		((ImageButton) this.findViewById(R.id.back_button))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AuthActivity.this.finish();
					}
				});

		loading = (RelativeLayout) this.findViewById(R.id.loading);
		initWeb();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		loadUrl();
		super.onResume();
	}

	private String getAuthUrl() {
		String url = NetUtil.doubanService
				.getAuthorizationUrl(NetUtil.callback);
		requestTokenSecret = NetUtil.doubanService.getRequestTokenSecret();
		return url;
	}

	private boolean isFirstLoad = true;

	@SuppressLint("SetJavaScriptEnabled")
	void initWeb() {
		browser = (WebView) AuthActivity.this.findViewById(R.id.browser);
		browser.requestFocus();
		WebSettings settings = browser.getSettings();
		settings.setSupportZoom(true);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptEnabled(true);
		browser.setWebViewClient(new WebViewClient() {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				System.out.println(url);
				if (url.matches(NetUtil.callback + "?.*")) {
					view.loadUrl("about:blank");
					doAuth(Uri.parse(url));
					return true;
				}
				return super.shouldOverrideUrlLoading(view, url);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				if (isFirstLoad) {
					closeDialog();
					isFirstLoad = false;
				}
			}

		});
	}

	void loadUrl() {
		new AsyncTask<Void, Void, String>() {

			@Override
			protected String doInBackground(Void... params) {
				// TODO Auto-generated method stub
				return getAuthUrl();
			}

			@Override
			protected void onPostExecute(String result) {
				super.onPostExecute(result);
				isFirstLoad = true;
				browser.loadUrl(result);
				browser.setVisibility(View.VISIBLE);
			}

			@Override
			protected void onPreExecute() {
				// TODO Auto-generated method stub
				super.onPreExecute();
				showDialog();
			}

		}.execute();
	}

	void doAuth(final Uri uri) {
		new AsyncTask<String, Void, String[]>() {

			@Override
			protected String[] doInBackground(String... args) {
				String request_token = uri.getQueryParameter("oauth_token");
				return saveAccessToken(request_token, requestTokenSecret);
			}

			@Override
			protected void onPostExecute(String[] result) {
				super.onPostExecute(result);
				loading.setVisibility(View.GONE);
				if (result != null) {
					Toast toast = null;
					toast = Toast.makeText(AuthActivity.this, "µÇÂ¼³É¹¦",
							Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					NetUtil.isAuthed = true;

					setResult(SUCCESS);
					AuthActivity.this.finish();
				} else {
					doAgain();
					NetUtil.isAuthed = false;
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				loading.setVisibility(View.VISIBLE);
				browser.setVisibility(View.GONE);
			}

		}.execute("");
	}

	private void doAgain() {
		LayoutParams params = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		Button btn = new Button(getApplicationContext());
		btn.setText("µÇÂ¼Ê§°Ü,ÇëÖØÊÔ");
		btn.setLayoutParams(params);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.setVisibility(View.GONE);
				isFirstLoad = true;
				loadUrl();
			}
		});
		RelativeLayout layout = (RelativeLayout) AuthActivity.this
				.findViewById(R.id.content);
		layout.addView(btn);
	}

	String[] saveAccessToken(String requestToken, String requestTokenSecret) {
		String[] result = null;

		NetUtil.doubanService.setRequestToken(requestToken);
		NetUtil.doubanService.setRequestTokenSecret(requestTokenSecret);

		try {
			ArrayList<String> list = NetUtil.doubanService.getAccessToken();
			if (list == null) {
				return null;
			}
			result = new String[3];
			result[0] = list.get(0);
			result[1] = list.get(1);

			NetUtil.doubanService.setAccessToken(result[0], result[1]);
			UserEntry user = NetUtil.doubanService.getAuthorizedUser();
			result[2] = user.getUid();
			sharedata.edit().putString(ConfigData.ACCESSTOKEN, result[0])
					.putString(ConfigData.TOKENSECRET, result[1])
					.putString(ConfigData.UID, result[2]).commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return result;
	}

}
