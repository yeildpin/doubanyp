package com.example.doubanyp;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

import com.example.doubanyp.entity.Book;
import com.example.doubanyp.entity.BookListAdapter;
import com.example.doubanyp.util.ConvertUtil;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.douban.SubjectFeed;

public class DoubanActivity extends BaseActivity {
	private List<Book> books = new ArrayList<Book>();
	private EditText searchText;
	private int bookIndex = 1;
	private int count = 6; // ÿ�λ�ȡ��Ŀ
	private boolean isFilling = false; // �ж��Ƿ����ڻ�ȡ����
	protected BookListAdapter bookListAdapter;

	private int bookTotal; // �����Ŀ��

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_douban);

		initView();

		initDouban();
	}

	private void initView() {
		searchText = (EditText) this.findViewById(R.id.search_text);
		searchText.setHint(R.string.book_search_hint);
		ImageButton searchButton = (ImageButton) this
				.findViewById(R.id.search_button);
		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				if (checkNetWorkStatus())
					doSearch();
				else {
					Toast toast = Toast.makeText(getApplicationContext(), "����δ������",
							Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
			}
		});

		ListView listView = (ListView) this.findViewById(R.id.list);
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(DoubanActivity.this,
						BookViewActivity.class);
				Book book = books.get(position);
				i.putExtra("book", book);
				startActivity(i);
			}
		});

		listView.setOnScrollListener(new OnScrollListener() {

			public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {

			}

			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
					// �жϹ������ײ�
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						loadRemnantListItem();
					}
				}
			}
		});

	}

	// ��ȡ������Ŀ
	private void loadRemnantListItem() {
		if (isFilling) {
			return;
		}
		bookIndex = bookIndex + count;
		if (bookIndex > bookTotal) {
			return;
		}

		RelativeLayout loading = (RelativeLayout) this
				.findViewById(R.id.loading);
		LayoutParams lp = (LayoutParams) loading.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		loading.setLayoutParams(lp);

		fillData();
	}

	private boolean checkNetWorkStatus() {
		boolean netSataus = false;

		ConnectivityManager cwjManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cwjManager.getActiveNetworkInfo();
		if (info != null) {
			netSataus = info.isAvailable();
		}
		return netSataus;
	}

	private void doSearch() {
		String searchTitle = searchText.getText().toString();
		if ("".equals(searchTitle.trim())) {
			return;
		}
		bookIndex = 1;
		bookListAdapter = null;
		books.clear();
		fillData();
	}

	private AsyncTask<String, Void, SubjectFeed> task;

	private void fillData() {
		if (task != null)
			task.cancel(true);
		task = new AsyncTask<String, Void, SubjectFeed>() {

			@Override
			protected SubjectFeed doInBackground(String... args) {

				String title = searchText.getText().toString();
				SubjectFeed feed = null;
				try {
					feed = NetUtil.doubanService.findBook(title, "", bookIndex,
							count);
					bookTotal = feed.getTotalResults();
				} catch (Exception e) {
					e.printStackTrace();
				}
				return feed;
			}

			@Override
			protected void onPostExecute(SubjectFeed result) {
				super.onPostExecute(result);
				closeProgressBar();
				if (result != null) {
					ListView listView = (ListView) DoubanActivity.this
							.findViewById(R.id.list);
					books.addAll(ConvertUtil.ConvertSubjects(result));
					if (bookListAdapter == null) {
						bookListAdapter = new BookListAdapter(
								DoubanActivity.this, listView, books);
						listView.setAdapter(bookListAdapter);
					} else {
						bookListAdapter.notifyDataSetChanged();
					}

				}
				isFilling = false;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				isFilling = true;
				showProgressBar();
			}

		}.execute("");
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doExit();
			return true;
		}
		return true;
	}

}
