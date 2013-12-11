package com.example.doubanyp;

import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.doubanyp.entity.Book;
import com.example.doubanyp.entity.Review;
import com.example.doubanyp.util.NetUtil;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.douban.ReviewEntry;
import com.google.gdata.data.douban.SubjectEntry;
import com.google.gdata.data.extensions.Rating;
import com.google.gdata.util.ServiceException;

public class ReviewEditActivity extends BaseActivity {
	private Review review;
	private EditText reviewContent;
	private RatingBar reviewRatingbar;
	private EditText reviewTitle;
	private Book book;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.review_edit);

		this.reviewTitle = (EditText) findViewById(R.id.EdtReviewTitle);
		this.reviewContent = (EditText) findViewById(R.id.EdtReviewContent);
		this.reviewRatingbar = (RatingBar) findViewById(R.id.ratingbar);
		Bundle extras = getIntent().getExtras();
		book = extras != null ? (Book) extras.getSerializable("book") : null;
		review = extras != null ? (Review) extras.getSerializable("review")
				: null;
		String title = "���ۡ�" + this.book.getTitle() + "��";
		((TextView) findViewById(R.id.myTitle)).setText(title);
		initView();
		setData();
		
		DOAUTH = 0;
	}

	private void initView() {
		Button btnSave = (Button) findViewById(R.id.btnSave);
		// ����
		btnSave.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				doPost();
			}

		});
		// ȡ��
		Button btnCancel = (Button) findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				finish();
			}

		});
		// ����
		ImageButton backButton = (ImageButton) findViewById(R.id.back_button);
		backButton.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				goBack();
			}

		});

	}

	private void setData() {
		if (this.review != null) {
			reviewTitle.setText(review.getTitle());
			reviewContent.setText(review.getContent());
			reviewRatingbar.setRating(review.getRating());
		}
	}

	private void doPost() {
		String title = this.reviewTitle.getText().toString().trim();
		String content = this.reviewContent.getText().toString().trim();
		int rating = (int) this.reviewRatingbar.getRating();
		if ("".equals(title)) {
			Toast.makeText(this, "���۱��ⲻ��Ϊ�գ�", Toast.LENGTH_SHORT).show();
			return;
		}
		if (rating == 0) {
			Toast.makeText(this, "���ֲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
			return;
		}
		if ("".equals(content)) {
			Toast.makeText(this, "�������ݲ���Ϊ�գ�", Toast.LENGTH_SHORT).show();
			return;
		}
		if (content.length() < 50) {
			Toast.makeText(this, "�������ݲ���С��50���ַ���", Toast.LENGTH_SHORT).show();
			return;
		}

		new AsyncTask<Void, Void, Boolean>() {

			private boolean goAuth;
			
			@Override
			protected void onPostExecute(Boolean result) {
				if(goAuth){
					closeDialog();
					doAuth();
					return;
				}
				if (result) {
					closeDialog();
					String message = "";
					if (review == null) {
						message = "���������ɹ���";
					} else {
						message = "�����޸ĳɹ���";
					}
					Toast.makeText(ReviewEditActivity.this, message,
							Toast.LENGTH_SHORT).show();
					finish();
				} else {
					String message = "";
					if (review == null) {
						message = "��������ʧ�ܣ�";
					} else {
						message = "�����޸�ʧ�ܣ�";
					}
					Toast.makeText(ReviewEditActivity.this, message,
							Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			protected void onPreExecute() {
				goAuth = false;
				showDialog();
			}

			@Override
			protected Boolean doInBackground(Void... arg0) {
				String title = reviewTitle.getText().toString().trim();
				String content = reviewContent.getText().toString().trim();
				int ratingValue = (int) reviewRatingbar.getRating();
				Rating rating = new Rating();
				rating.setValue(ratingValue);
				try {
					// ����
					if (review != null) {
						ReviewEntry reviewEntry = new ReviewEntry();
						reviewEntry.setId(review.getId());
						NetUtil.doubanService.updateReview(reviewEntry,
								new PlainTextConstruct(title),
								new PlainTextConstruct(content), rating);
						return true;
					}
					// ����
					else {
						SubjectEntry subjectEntry = new SubjectEntry();
						subjectEntry.setId(book.getId());
						NetUtil.doubanService.createReview(subjectEntry,
								new PlainTextConstruct(title),
								new PlainTextConstruct(content), rating);
						return true;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					String cause = e.getMessage();
					if(cause.equals("Unauthorized"))
						goAuth = true;
					return false;
				}
			}

		}.execute();
	}

	protected void goBack() {
		String title = this.reviewTitle.getText().toString().trim();
		String content = this.reviewContent.getText().toString().trim();
		if (("".equals(title)) && ("".equals(content))) {
			finish();
			return;
		}

		if (this.review != null) {
			String reviewTitle = this.review.getTitle();
			String reviewContent = this.review.getContent();
			if (title.equals(reviewTitle) && content.equals(reviewContent)) {
				finish();
				return;
			}
		}
		new AlertDialog.Builder(ReviewEditActivity.this)
				.setTitle("��ʾ")
				.setMessage("����δ���棬ȷ��Ҫ�˳���")
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialoginterface, int i) {
						finish();
					}
				})
				.setNeutralButton("ȡ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
					}

				}).show();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		goBack();
	}
	
	

}
