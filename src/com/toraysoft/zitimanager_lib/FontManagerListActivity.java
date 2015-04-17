package com.toraysoft.zitimanager_lib;

import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ListView;

import com.toraysoft.zitimanager_lib.adapter.FontManagerAdapter;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Category;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.IHttpCallBack;
import com.xinmei365.fontsdk.callback.OnResult;

public class FontManagerListActivity extends ActionBarActivity {

	ActionBar actionBar;

	FontManagerAdapter adapter;
	ListView mListView;
	List<Font> data;

	public String downloadUrl;

	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_font_manager_list);
		actionBar = getSupportActionBar();
		actionBar.setTitle(R.string.font_manager_title);
		// actionBar.setDisplayHomeAsUpEnabled(true);

		FontCenter.getInstance().init();

		mListView = (ListView) findViewById(R.id.listview_font_manager);
		adapter = new FontManagerAdapter(this, data);
		mListView.setAdapter(adapter);

		if (getIntent().hasExtra("url")) {
			downloadUrl = getIntent().getStringExtra("url");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getData();
	}

	public void getData() {
		progressDialog = ProgressDialog.show(this, "",
				getString(R.string.font_manager_loading), false, false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

		FontCenter.getInstance().getCateListFromServer(new IHttpCallBack() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object obj) {

				List<Category> cat = (List<Category>) obj;

				if (cat.size() > 0) {
					FontCenter.getInstance().getCateFontListFromServer(
							new IHttpCallBack() {

								@Override
								public void onErr(String err) {
									if (progressDialog != null) {
										progressDialog.hide();
									}
									Log.i("FontManager",
											"getCateFontListFromServer onErr");
								}

								@Override
								public void onSuccess(Object obj) {
									data = (List<Font>) obj;
									adapter.setData((List<Font>) obj);
									adapter.notifyDataSetChanged();

									if (progressDialog != null) {
										progressDialog.hide();
									}
								}
							}, cat.get(0).getCategoryId());
				}
			}

			@Override
			public void onErr(String err) {
				if (progressDialog != null) {
					progressDialog.hide();
				}
				Log.i("FontManager", "getCateListFromServer onErr");
			}
		});
	}

	@Override
	protected void onDestroy() {
		FontCenter.getInstance().recovery();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FontCenter.getInstance().onActivityResult(requestCode, resultCode,
				data, new OnResult() {

					@Override
					public void onSuccess(Font arg0) {
						System.out.println("-------OK------");
					}

					@Override
					public void onFailure(String arg0) {
						System.out.println("-------Nooooo------");
					}
				});
	}
}
