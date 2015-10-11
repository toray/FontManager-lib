package com.toraysoft.zitimanager_lib.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.toraysoft.utils.download.DLListener;
import com.toraysoft.utils.download.DLManager;
import com.toraysoft.zitimanager_lib.FontListView;
import com.toraysoft.zitimanager_lib.FontManagerListActivity;
import com.toraysoft.zitimanager_lib.R;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.FontDownloadCallBack;
import com.xinmei365.fontsdk.callback.OnResult;

public class FontManagerAdapter extends BaseAdapter {

	static String TAG = "FontManager";

	FontManagerListActivity mContext;
	List<Font> data;

	List<Font> downloadingFont = new ArrayList<Font>();

	public FontManagerAdapter(FontManagerListActivity context, List<Font> items) {
		this.mContext = context;
		this.data = items;
	}

	@Override
	public int getCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh = null;
		if (convertView == null || convertView.getTag() == null) {
			vh = new ViewHolder();
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		vh.init(position);
		vh.view_parent.setTag(vh);
		return vh.view_parent;
	}

	class ViewHolder implements OnClickListener {

		boolean isInit;
		View view_parent;
		TextView tv_name, tv_desc;
		Button btn_use;

		void init(int position) {
			if (!isInit) {
				isInit = true;
				view_parent = LayoutInflater.from(mContext).inflate(
						R.layout.item_font_list, null);
				tv_name = (TextView) view_parent.findViewById(R.id.font_name);
				tv_desc = (TextView) view_parent.findViewById(R.id.font_desc);
				btn_use = (Button) view_parent
						.findViewById(R.id.font_manager_btn_use);

				btn_use.setOnClickListener(this);
			}
			final Font item = data.get(position);
			if (isDownloading(item)) {
				btn_use.setText(R.string.font_manager_apk_download_start);
			} else if (item.isDownloaded()) {
				btn_use.setText(R.string.font_manager_apk_use);
			} else {
				btn_use.setText(R.string.font_manager_btn_use);
			}
			btn_use.setTag(item);
			view_parent.setTag(item);
			tv_name.setText(item.getFontName());
			tv_desc.setText(parseFileSize(item.getFontSize()));
			view_parent.setOnClickListener(this);
			FontListView.setTypeface(item, tv_name);
		}

		@Override
		public void onClick(View v) {
			if (v.getTag() != null) {
				if (v.getId() == R.id.font_manager_btn_use) {
					final TextView tv = (TextView) v;
					final Font font = (Font) v.getTag();
					if (font.isDownloaded()) {
						invokeAPK(font.getFontLocalPath());
					} else {
						if (font.isCanDownload()) {
							Log.i(TAG, "Font " + font.getFontName()
									+ " download start");
							tv.setText(mContext
									.getString(R.string.font_manager_apk_download_start));

							downloadingFont.add(font);
							FontCenter.getInstance().downloadFont(
									new FontDownloadCallBack() {

										@Override
										public void waited(String arg0) {
										}

										@Override
										public void paused(String arg0) {
										}

										@Override
										public void onUpgrade(String arg0,
												long arg1, long arg2) {
										}

										@Override
										public void onSuccessed(String arg0,
												String arg1) {
											FontCenter.unzip(font);
											downloadingFont.remove(font);
											Toast.makeText(
													mContext,
													mContext.getString(
															R.string.font_manager_download_success,
															font.getFontName()),
													Toast.LENGTH_SHORT).show();
											Log.i(TAG,
													"Font "
															+ font.getFontName()
															+ " download OK "
															+ font.getFontLocalPath());
											invokeAPK(font.getFontLocalPath());
										}

										@Override
										public void onStart(String arg0) {
										}

										@Override
										public void onFailed(String arg0,
												int arg1, String arg2) {
											Log.i(TAG,
													"Font "
															+ font.getFontName()
															+ " download failed !!!");
											downloadingFont.remove(font);
											Toast.makeText(
													mContext,
													mContext.getString(
															R.string.font_manager_download_fail,
															font.getFontName()),
													Toast.LENGTH_LONG).show();
										}

										@Override
										public void canceled(String arg0) {
										}
									}, font, mContext);

						} else {
							int r = FontCenter.getInstance().checkFontManager();
							if (r == 0) {
								OnResult onResult = new OnResult() {

									@Override
									public void onFailure(String arg0) {
										downloadingFont.remove(font);
									}

									@Override
									public void onSuccess(Font arg0) {
										downloadingFont.remove(font);
									}

								};
								downloadingFont.add(font);
								FontCenter.getInstance()
										.getFontFromFontManager(
												font.getFontKey(), mContext,
												onResult);
							} else if (r == 1) {
								Toast.makeText(mContext,
										R.string.font_manager_too_old,
										Toast.LENGTH_LONG).show();
							} else if (r == 2) {
								if (mContext.downloadUrl != null) {
									DialogInterface.OnClickListener okDownloadListener = new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											final String tmp = Environment
													.getExternalStorageDirectory()
													+ "/temp/";
											final String fileName = "fontManager.apk";
											File f = new File(tmp);
											if (!f.exists())
												f.mkdirs();

											DLListener downloadListener = new DLListener() {

												@Override
												public void onDownloadPercent(
														String _id,
														String percent,
														long completedTot) {
												}

												@Override
												public void onDownloadFinish(
														String _id) {
													showTip(R.string.font_manager_apk_download_success);
													invokeAPK(tmp + fileName);
												}

												@Override
												public void onDownloadCancel(
														String _id) {
													showTip(R.string.font_manager_apk_download_failed);
												}

												@Override
												public void onDownloadError(
														String _id, int state) {
													showTip(R.string.font_manager_apk_download_failed);
												}

											};
											DLManager.get().download(mContext,
													"FontManager",
													mContext.downloadUrl, f,
													fileName, downloadListener);
										}
									};
									showDialog(
											mContext,
											mContext.getString(R.string.font_manager_dialog_title),
											mContext.getString(R.string.font_manager_dialog_message),
											mContext.getString(R.string.font_manager_dialog_cancel),
											null,
											mContext.getString(R.string.font_manager_dialog_ok),
											okDownloadListener, true);
								} else {
									Toast.makeText(mContext,
											R.string.font_manager_not_install,
											Toast.LENGTH_LONG).show();
								}
							}
						}
					}
				}
			}
		}

		void showDialog(Activity context, String title, String text,
				String ntbtnText, DialogInterface.OnClickListener ntLintener,
				String ptbtnText, DialogInterface.OnClickListener ptLintener,
				boolean isCanceledOnTouchOutside) {
			if (context.isFinishing())
				return;
			AlertDialog dialog = new AlertDialog.Builder(context)
					.setTitle(title).setMessage(text)
					.setNegativeButton(ntbtnText, ntLintener)
					.setPositiveButton(ptbtnText, ptLintener).create();
			dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutside);
			dialog.show();
		}

	}

	public List<Font> getData() {
		return data;
	}

	public void setData(List<Font> data) {
		this.data = data;
	}

	String parseFileSize(long size) {
		String ret = null;
		if (size <= 999) {
			ret = size + "B";
		} else if (size <= 999999) {
			long no = size / 100;
			String nos = Long.toString(no);
			String last_no = nos.substring(nos.length() - 1, nos.length());
			String head_no = nos.substring(0, nos.length() - 1);
			ret = head_no + "." + last_no + "KB";
		} else {
			long no = size / 100000;
			String nos = Long.toString(no);
			String last_no = nos.substring(nos.length() - 1, nos.length());
			String head_no = nos.substring(0, nos.length() - 1);
			ret = head_no + "." + last_no + "MB";
		}
		return ret;
	}

	void invokeAPK(String path) {
		if (path != null) {
			File f = new File(path);
			if (f.exists()) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(f),
						"application/vnd.android.package-archive");
				mContext.startActivity(intent);
			}
		}
	}

	@SuppressLint("NewApi")
	void showTip(int msg) {
		if (mContext != null && mContext.isDestroyed())
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
	}

	@SuppressLint("NewApi")
	void showTip(String msg) {
		if (mContext != null && mContext.isDestroyed())
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
	}

	boolean isDownloading(Font font) {
		return downloadingFont.contains(font);
	}
}
