package com.example.doubanyp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doubanyp.entity.Book;
import com.example.doubanyp.entity.Review;
import com.example.doubanyp.util.ConvertUtil;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.douban.ReviewFeed;
import com.google.gdata.data.douban.UserEntry;
import com.google.gdata.util.ServiceException;

public class ReviewActivity extends BaseListActivity {
	private static final String ORDERBY = "score";// 按评分排序
	private List<Review> reviews = new ArrayList<Review>();
	private ReviewListAdapter listAdapter;
	private int index = 1;
	private int count = 6; 		// 每次获取数目
	private int total;		 // 最大条目数
	private boolean isFilling = false; // 判断是否正在获取数据
	private boolean myReview;
	private boolean bestReview;

//	private static final int DELETE_ID = 0x000002;
//	private static final int EDIT_ID = 0x000003;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review);

		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});

		Bundle extras = getIntent().getExtras();
		myReview = extras != null ? extras.getBoolean("my_review") : false;
		bestReview = extras != null ? extras.getBoolean("best_review") : false;
		if (myReview) {
			registerForContextMenu(getListView());
		}
		TextView titleView = (TextView) findViewById(R.id.myTitle);
		if (myReview) {
			String title = "我的评论";
			titleView.setText(title);
			fillMyReview();
		} else if (bestReview) {
			String title = "豆瓣最受欢迎的书评";
			titleView.setText(title);
			fillBestReview();

			getListView().setOnScrollListener(new OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {

				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						// 判断滚动到底部
						if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
							loadMoreNewBestReview();
						}
					}
				}

			});

		} else {
			final Book book = extras != null ? (Book) extras
					.getSerializable("book") : null;
			String title = "《" + book.getTitle() + "》的评论";
			titleView.setText(title);
			fillDataBySubject(book);
			getListView().setOnScrollListener(new OnScrollListener() {
				public void onScroll(AbsListView view, int firstVisibleItem,
						int visibleItemCount, int totalItemCount) {

				}

				public void onScrollStateChanged(AbsListView view,
						int scrollState) {
					if (scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
						// 判断滚动到底部
						if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
							loadRemnantListItem(book);
						}
					}
				}

			});
		}
	}

	// 加载更多评论
	private void loadRemnantListItem(Book book) {
		if (isFilling) {
			return;
		}
		index = index + count;
		if (index > total) {
			return;
		}
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		LayoutParams lp = (LayoutParams) loading.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		loading.setLayoutParams(lp);

		fillDataBySubject(book);
	}

	// 获取我的评论
	private void fillMyReview() {
		new AsyncTask<Void, Void, ReviewFeed>() {

			private UserEntry ue;

			@Override
			protected ReviewFeed doInBackground(Void... args) {
				ReviewFeed feed = null;
				try {
					ue = NetUtil.doubanService.getAuthorizedUser();
					feed = NetUtil.doubanService.getUserReviews(ue.getUid());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					doAuth();
				}
				return feed;
			}

			@Override
			protected void onPostExecute(ReviewFeed feed) {
				super.onPostExecute(feed);
				closeProgressBar();
				if (feed != null) {
					reviews = ConvertUtil.ConvertReviews(feed, null, ue);
					setListAdapter(new ReviewListAdapter(ReviewActivity.this,
							getListView(), reviews));
					if (reviews.size() == 0) {
						Toast.makeText(ReviewActivity.this, "没有找到相关评论！",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(ReviewActivity.this, "数据加载失败！",
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar("正在获取数据...");
			}
		}.execute();
	}

	// 加载更多最受欢迎书评
	protected void loadMoreNewBestReview() {
		total = 50;
		if (isFilling) {
			return;
		}
		index = index + count;
		if (index > total) {
			return;
		}
		RelativeLayout loading = (RelativeLayout) findViewById(R.id.loading);
		LayoutParams lp = (LayoutParams) loading.getLayoutParams();
		lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		loading.setLayoutParams(lp);
		fillBestReview();
	}

	// 获取最受欢迎的书评
	private void fillBestReview() {
		new AsyncTask<Void, Void, List<Review>>() {

			@Override
			protected List<Review> doInBackground(Void... arg0) {
				return NetUtil.getBestReviews(index);
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				isFilling = true;
			}

			@Override
			protected void onPostExecute(List<Review> newReviews) {
				super.onPostExecute(newReviews);
				closeProgressBar();
				if (newReviews != null) {
					reviews.addAll(newReviews);
					if (listAdapter == null) {
						listAdapter = new ReviewListAdapter(
								ReviewActivity.this, getListView(), reviews);
						setListAdapter(listAdapter);
					} else {
						listAdapter.notifyDataSetChanged();
					}

					if (reviews.size() == 0) {
						Toast.makeText(ReviewActivity.this, "没有找到相关评论！",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(ReviewActivity.this, "数据加载失败！",
							Toast.LENGTH_SHORT).show();
				}
				isFilling = false;
			}
		}.execute();
	}

	// 获取评论数据
	private void fillDataBySubject(final Book book) {
		new AsyncTask<Book, Void, ReviewFeed>() {

			@Override
			protected ReviewFeed doInBackground(Book... args) {
				ReviewFeed feed = null;
				Book book1 = args[0];
				try {
					feed = NetUtil.doubanService.getBookReviews(book1.getId(),
							index, count, ORDERBY);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					doAuth();
				}
				return feed;
			}

			@Override
			protected void onPostExecute(ReviewFeed feed) {
				super.onPostExecute(feed);
				closeProgressBar();
				if (feed != null) {
					total = feed.getTotalResults();
					reviews.addAll(ConvertUtil.ConvertReviews(feed, book));
					if (listAdapter == null) {
						listAdapter = new ReviewListAdapter(
								ReviewActivity.this, getListView(), reviews);
						setListAdapter(listAdapter);
					} else {
						listAdapter.notifyDataSetChanged();
					}

					if (reviews.size() == 0) {
						Toast.makeText(ReviewActivity.this, "没有找到相关评论！",
								Toast.LENGTH_SHORT).show();
					}

				} else {
					Toast.makeText(ReviewActivity.this, "数据加载失败！",
							Toast.LENGTH_SHORT).show();
				}
				isFilling = false;
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showProgressBar();
				isFilling = true;
			}

		}.execute(book);

	}

	// 选中事件
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, ReviewViewActivity.class);
		Review review = reviews.get(position);
		i.putExtra("review", review);
		startActivity(i);
	}

//	// 上下文菜单
//	@Override
//	public void onCreateContextMenu(ContextMenu menu, View v,
//			ContextMenuInfo menuInfo) {
//		super.onCreateContextMenu(menu, v, menuInfo);
//		menu.add(0, EDIT_ID, 0, R.string.menu_edit);
//		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
//	}
//
//	public boolean onContextItemSelected(MenuItem item) {
//		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
//				.getMenuInfo();
//		int id = (int) info.id;
//		Review review = reviews.get(id);
//		switch (item.getItemId()) {
//		case DELETE_ID:
//			deleteReview(review);
//			break;
//		case EDIT_ID:
//			editReview(review);
//			break;
//
//		}
//		return super.onContextItemSelected(item);
//	}

//	// 编辑评论
//	private void editReview(Review review) {
//		// TODO Auto-generated method stub
//
//	}
//
//	// 删除评论
//	private void deleteReview(Review paramReview) {
//		new AsyncTask<Review, Void, Boolean>() {
//
//			@Override
//			protected void onPreExecute() {
//				super.onPreExecute();
//				showProgressBar("正在删除数据...");
//			}
//
//			@Override
//			protected Boolean doInBackground(Review... args) {
//				Review review = args[0];
//				try {
//					ReviewEntry reviewEntry = new ReviewEntry();
//					reviewEntry.setId(review.getUrl());
//					NetUtil.doubanService.deleteReview(reviewEntry);
//				} catch (Exception e) {
//					e.printStackTrace();
//					return false;
//				}
//				return true;
//			}
//
//			@Override
//			protected void onPostExecute(Boolean result) {
//				super.onPostExecute(result);
//				if (result) {
//					closeProgressBar();
//					Toast.makeText(ReviewActivity.this, "评论删除成功！",
//							Toast.LENGTH_SHORT).show();
//					fillMyReview();
//				} else {
//					closeProgressBar();
//					Toast.makeText(ReviewActivity.this, "评论删除失败！",
//							Toast.LENGTH_SHORT).show();
//				}
//			}
//
//		}.execute(paramReview);
//	}

	public class ReviewListAdapter extends BaseAdapter {

		private List<Review> reviews;
		private LayoutInflater mInflater;

		public ReviewListAdapter(Context context, ListView listView,
				List<Review> reviews) {
			this.reviews = reviews;
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return reviews.size();
		}

		public Object getItem(int i) {
			return reviews.get(i);
		}

		public long getItemId(int i) {
			return i;
		}

		public View getView(int i, View view, ViewGroup vg) {
			if (view == null) {
				view = mInflater.inflate(R.layout.review_item, null);
			}

			Review review = reviews.get(i);

			TextView txtTitle = (TextView) view.findViewById(R.id.review_title);
			TextView txtSummary = (TextView) view
					.findViewById(R.id.review_summary);
			RatingBar ratingBar = (RatingBar) view.findViewById(R.id.ratingbar);
			TextView txtAuthorName = (TextView) view
					.findViewById(R.id.author_name);

			txtTitle.setText(review.getTitle());
			String summary = review.getSummary();
			summary = summary.replaceAll("\\\n", "");
			summary = summary.replaceAll("\\\t", "");
			summary = summary.replaceAll(" ", "");
			txtSummary.setText(summary);
			ratingBar.setRating(review.getRating());
			txtAuthorName.setText("评论人:" + review.getAuthorName());
			return view;
		}
	}

}
