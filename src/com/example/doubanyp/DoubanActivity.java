package com.example.doubanyp;

import java.util.ArrayList;
import java.util.List;

import com.example.doubanyp.entity.Book;
import com.example.doubanyp.entity.BookListAdapter;
import com.example.doubanyp.util.ConvertUtil;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.douban.SubjectFeed;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

public class DoubanActivity extends BaseActivity {
	private List<Book> books = new ArrayList<Book>();

	private int bookIndex = 1;
	private int count = 10; // 每次获取数目
	private boolean isFilling = false; // 判断是否正在获取数据
	protected BookListAdapter bookListAdapter;

	private int bookTotal; // 最大条目数

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_douban);

		initView();
	}

	private void initView() {
		EditText searchText = (EditText) this.findViewById(R.id.search_text);
		searchText.setHint(R.string.book_search_hint);
		ImageButton searchButton = (ImageButton) this
				.findViewById(R.id.search_button);

		searchButton.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				doSearch();
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
					// 判断滚动到底部
					if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
						loadRemnantListItem();
					}
				}
			}
		});

	}

	// 获取更多条目
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

	private void doSearch() {
		EditText searchText = (EditText) this.findViewById(R.id.search_text);

		String searchTitle = searchText.getText().toString();
		if ("".equals(searchTitle.trim())) {
			return;
		}
		bookIndex = 1;
		bookListAdapter = null;
		books.clear();
		fillData();
	}

	private void fillData() {
		new AsyncTask<String, Void, SubjectFeed>() {

			@Override
			protected SubjectFeed doInBackground(String... args) {
				EditText searchText = (EditText) DoubanActivity.this
						.findViewById(R.id.search_text);

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
				initDouban();
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
