package com.example.doubanyp;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.doubanyp.entity.Book;
import com.example.doubanyp.util.AsyncImageLoader.ImageCallback;
import com.example.doubanyp.util.ConvertUtil;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.douban.SubjectEntry;

public class BookViewActivity extends BaseActivity {
	private TextView txtTitle;
	private TextView txtDescription;
	private TextView txtSummary;
	private ImageView bookImage;
	private RatingBar ratingBar;
	private Button showReview1;
	private Button showReview2;
	private Book book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_view);

		Bundle extras = getIntent().getExtras();
		book = extras != null ? (Book) extras.getSerializable("book") : null;

		txtTitle = (TextView) findViewById(R.id.book_title);
		txtDescription = (TextView) findViewById(R.id.book_description);
		txtSummary = (TextView) findViewById(R.id.book_summary);
		bookImage = (ImageView) findViewById(R.id.book_img);
		ratingBar = (RatingBar) findViewById(R.id.ratingbar);

		LinearLayout bookToolbar = (LinearLayout) findViewById(R.id.book_toolbar);

		bookToolbar.setVisibility(View.VISIBLE);

		TextView txtInfo = (TextView) findViewById(R.id.txtInfo);

		TextView titleText = (TextView) findViewById(R.id.myTitle);
		titleText.setText("《" + book.getTitle() + "》");

		if (book != null) {
			fillData(book.getUrl());
			titleText.setText("《" + book.getTitle() + "》");
			txtInfo.setText(R.string.bookInfo);
		}

		findViewById(R.id.btn_book_new_review).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						if (NetUtil.isAuthed) {
							Intent intent = null;
							intent = new Intent(BookViewActivity.this,
									ReviewEditActivity.class);
							intent.putExtra("book", book);
//							intent.putExtra("id", v.getId());
							startActivity(intent);
						} else {
							doAuth();
						}
					}
				});

		// 回退按钮
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		
		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}

		});

		// 查看评论按钮
		showReview1 = (Button) findViewById(R.id.btnShowComment1);
		showReview2 = (Button) findViewById(R.id.btnShowComment2);
		OnClickListener showReviewClicklistener = new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(BookViewActivity.this,
						ReviewActivity.class);
				i.putExtra("book", book);
				startActivity(i);
			}

		};
		showReview1.setOnClickListener(showReviewClicklistener);
		showReview2.setOnClickListener(showReviewClicklistener);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if (requestCode == DOAUTH && resultCode == AuthActivity.SUCCESS) {
			Intent intent = new Intent(BookViewActivity.this,
					ReviewEditActivity.class);
			intent.putExtra("book", book);
			startActivity(intent);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void fillData(String bookId) {

		new AsyncTask<String, Void, SubjectEntry>() {

			@Override
			protected SubjectEntry doInBackground(String... args) {
				String bookId = args[0];
				SubjectEntry entry = null;

				try {
					entry = NetUtil.doubanService.getBook(bookId);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return entry;
			}

			@Override
			protected void onPostExecute(SubjectEntry result) {
				super.onPostExecute(result);
				if (result != null) {
					closeDialog();
					Book book = ConvertUtil.convertOneSubject(result);
					txtTitle.setText(book.getTitle());
					txtDescription.setText(book.getDescription());
					String summary = "\t\t" + book.getSummary();

					if (!"".equals(book.getAuthorIntro())) {
						summary = summary + "\n\n作者简介:" + book.getAuthorIntro();
					}

					if (!"".equals(book.getTagsToString())) {
						summary = summary + "\n\n标签:" + book.getTagsToString();
					}

					txtSummary.setText(summary);

					ratingBar.setRating(book.getRating());
					ratingBar.setVisibility(View.VISIBLE);
					if (summary.length() > 200) {
						showReview2.setVisibility(View.VISIBLE);
					}
					showReview1.setVisibility(View.VISIBLE);
					String imageUrl = book.getImgUrl();
					Drawable drawable = NetUtil.asyncImageLoader.loadDrawable(
							imageUrl, new ImageCallback() {
								public void imageLoaded(Drawable imageDrawable,
										String imageUrl) {
									bookImage.setImageDrawable(imageDrawable);
								}
							});
					if (drawable != null) {
						bookImage.setImageDrawable(drawable);
					} else {
						bookImage.setImageResource(R.drawable.book);
					}
				}
			}

			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showDialog();
			}

		}.execute(bookId);

	}

}
