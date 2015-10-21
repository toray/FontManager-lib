package com.toraysoft.zitimanager_lib;

import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.toraysoft.zitimanager_lib.adapter.FontManagerAdapter;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Category;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.IHttpCallBack;
import com.xinmei365.fontsdk.callback.OnResult;

public class FontManagerListActivity extends FragmentActivity {

	// ActionBar actionBar;

	private FontManagerAdapter adapter;
	private ListView mListView;
	private List<Font> data;

	public String downloadUrl;
	ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_font_manager_list);
		//// actionBar = getSupportActionBar();
		//// actionBar.setTitle(R.string.font_manager_title);
		// // actionBar.setDisplayHomeAsUpEnabled(true);
		//
		FontCenter.getInstance().init();
		data = new ArrayList<Font>();
		mListView = (ListView) findViewById(R.id.listview_font_manager);
		adapter = new FontManagerAdapter(this, data);
		mListView.setAdapter(adapter);
		FontCenter.getInstance().setFolder_font(Environment.getExternalStorageDirectory()+"/zitiguanjia/");
		if (getIntent().hasExtra("url")) {
			downloadUrl = getIntent().getStringExtra("url");
		}
		getData();
	}

	public void onBack(View v) {
		finish();
		// FontCenter.getInstance().get
	}

	@Override
	protected void onResume() {
		super.onResume();

	}

	public void getData() {
		progressDialog = ProgressDialog.show(this, "", getString(R.string.font_manager_loading), false, false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// getCateListFromServer
		FontCenter.getInstance().getCateListFromServer(new IHttpCallBack() {

			@SuppressWarnings("unchecked")
			@Override
			public void onSuccess(Object obj) {
				List<Category> cat = (List<Category>) obj;
				if (cat == null) {
					Log.e("err", "null");
					if (progressDialog != null) {
						Toast.makeText(FontManagerListActivity.this, "字体数据为空", Toast.LENGTH_LONG).show();
						progressDialog.hide();
					}

					return;
				}
				if (cat.size() > 0) {
					FontCenter.getInstance().getCateFontListFromServer(new IHttpCallBack() {

						@Override
						public void onErr(String err) {
							if (progressDialog != null) {
								progressDialog.hide();
							}
							Log.i("FontManager", "getCateFontListFromServer onErr");
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
		}, "cn");
	}

	@Override
	protected void onDestroy() {
		// FontCenter.getInstance().recovery();//recovery
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FontCenter.getInstance().onActivityResult(requestCode, resultCode, data, new OnResult() {

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
