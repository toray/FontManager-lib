package com.toraysoft.zitimanager_lib.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
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

import com.changefontmanager.sdk.ChangeFontManager;
import com.changefontmanager.sdk.IChangeFont;
import com.toraysoft.utils.download.DLListener;
import com.toraysoft.utils.download.DLManager;
import com.toraysoft.zitimanager_lib.FontListView;
import com.toraysoft.zitimanager_lib.FontManagerListActivity;
import com.toraysoft.zitimanager_lib.R;
import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.FileDownloadCallBack;
import com.xinmei365.fontsdk.callback.FontDownloadCallBack;
import com.xinmei365.fontsdk.callback.OnResult;

public class FontManagerAdapter extends BaseAdapter {

	static String TAG = "FontManager";

	FontManagerListActivity mContext;
	List<Font> data;
	IChangeFont chan;
	List<Font> downloadingFont = new ArrayList<Font>();

	public FontManagerAdapter(FontManagerListActivity context, List<Font> items) {
		this.mContext = context;
		this.data = items;
		chan = ChangeFontManager.getInstance().getChangefont(context);
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
				view_parent = LayoutInflater.from(mContext).inflate(R.layout.item_font_list, null);
				tv_name = (TextView) view_parent.findViewById(R.id.font_name);
				tv_desc = (TextView) view_parent.findViewById(R.id.font_desc);
				btn_use = (Button) view_parent.findViewById(R.id.font_manager_btn_use);

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

					if (!font.isDownloaded()) {
						int r = FontCenter.getInstance().checkFontManager();
						if (r == 0) {
							if (font.isCanDownload()) {
								Log.i(TAG, "Font " + font.getFontName() + " download start");
								tv.setText(mContext.getString(R.string.font_manager_apk_download_start));

								downloadingFont.add(font);
								FontCenter.getInstance().downloadFont(new FontDownloadCallBack() {

									@Override
									public void waited(String arg0) {
									}

									@Override
									public void paused(String arg0) {
									}

									@Override
									public void onUpgrade(String arg0, long arg1, long arg2) {
									}

									@Override
									public void onSuccessed(String arg0, String arg1) {
										FontCenter.unzip(font);
										Toast.makeText(mContext, mContext
												.getString(R.string.font_manager_download_success, font.getFontName()),
												Toast.LENGTH_SHORT).show();
										Log.i(TAG, "Font " + font.getFontName() + " download OK "
												+ font.getFontLocalPath());
										invokeType(font);
										downloadingFont.remove(font);
									}

									@Override
									public void onStart(String arg0) {
									}

									@Override
									public void onFailed(String arg0, int arg1, String arg2) {
										Log.i(TAG, "Font " + font.getFontName() + " download failed !!!");
										downloadingFont.remove(font);
										Toast.makeText(mContext, mContext.getString(R.string.font_manager_download_fail,
												font.getFontName()), Toast.LENGTH_LONG).show();
									}

									@Override
									public void canceled(String arg0) {
									}
								}, font, mContext);
							}
						} else {
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
								FontCenter.getInstance().getFontFromFontManager(font.getFontKey(), mContext, onResult);
							} else if (r == 1) {
								Toast.makeText(mContext, R.string.font_manager_too_old, Toast.LENGTH_LONG).show();
							} else if (r == 2) {
								if (mContext.downloadUrl != null) {

									DialogInterface.OnClickListener okDownloadListener = new DialogInterface.OnClickListener() {

										@Override
										public void onClick(DialogInterface arg0, int arg1) {
											FontCenter.getInstance().downloadFontmanager(new FileDownloadCallBack() {

												@Override
												public void onSuccess(String arg0) {
													invokeAPK(arg0);
													// Toast.makeText(mContext,
													// arg0,
													// Toast.LENGTH_LONG).show();
												}

												@Override
												public void onStart() {

												}

												@Override
												public void onLoading(long arg0, long arg1) {

												}

												@Override
												public void onFailure(Throwable arg0) {

												}

												@Override
												public void handleFile(String arg0) {

												}
											});
											// final String tmp =
											// Environment.getExternalStorageDirectory()
											// + "/temp/";
											// final String fileName =
											// "fontManager.apk";
											// File f = new File(tmp);
											// if (!f.exists())
											// f.mkdirs();
											//
											// DLListener downloadListener = new
											// DLListener() {
											//
											// @Override
											// public void
											// onDownloadPercent(String _id,
											// String percent,
											// long completedTot) {
											// }
											//
											// @Override
											// public void
											// onDownloadFinish(String _id) {
											// showTip(R.string.font_manager_apk_download_success);
											// invokeAPK(tmp + fileName);
											// }
											//
											// @Override
											// public void
											// onDownloadCancel(String _id) {
											// showTip(R.string.font_manager_apk_download_failed);
											// }
											//
											// @Override
											// public void
											// onDownloadError(String _id, int
											// state) {
											// showTip(R.string.font_manager_apk_download_failed);
											// }
											//
											// };
											// DLManager.get().download(mContext,
											// "FontManager",
											// mContext.downloadUrl, f,
											// fileName, downloadListener);
										}
									};
									showDialog(mContext, mContext.getString(R.string.font_manager_dialog_title),
											mContext.getString(R.string.font_manager_dialog_message),
											mContext.getString(R.string.font_manager_dialog_cancel), null,
											mContext.getString(R.string.font_manager_dialog_ok), okDownloadListener,
											true);

								} else {
									Toast.makeText(mContext, R.string.font_manager_not_install, Toast.LENGTH_LONG)
											.show();
								}
							}
						}

					} else {
						invokeType(font);
					}
				}
			}
		}

		void showDialog(Activity context, String title, String text, String ntbtnText,
				DialogInterface.OnClickListener ntLintener, String ptbtnText,
				DialogInterface.OnClickListener ptLintener, boolean isCanceledOnTouchOutside) {
			if (context.isFinishing())
				return;
			AlertDialog dialog = new AlertDialog.Builder(context).setTitle(title).setMessage(text)
					.setNegativeButton(ntbtnText, ntLintener).setPositiveButton(ptbtnText, ptLintener).create();
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
				intent.setDataAndType(Uri.fromFile(f), "application/vnd.android.package-archive");
				mContext.startActivity(intent);
				// Typeface type = Typeface.createFromFile(path);
				// TextView tv = new TextView(mContext);
				// tv.setTypeface(type);
				// Paint paint = new Paint();
				// paint.setTypeface(type);
			}
		}
	}

	private void invokeType(final Font font) {
		// if (path != null) {
		// File f = new File(path);
		// Log.e("tag", "path:" + path);
		// if (f.exists()) {
		// Typeface type = Typeface.createFromFile(path);
		// TextView tv = new TextView(mContext);
		// tv.setTypeface(type);
		// Paint paint = new Paint();
		// paint.setTypeface(type);
		// Toast.makeText(mContext, "字体下载成功，即将跳转到字体管家替换字体…",
		// Toast.LENGTH_LONG).show();
		// doStartApplicationWithPackageName("com.xinmei365.font");
		// }
		AlertDialog.Builder builder = new Builder(mContext);
		builder.setTitle("温馨提示");
		builder.setMessage("是否替换字体，替换后某部分机型会重启手机~");
		builder.setPositiveButton("确定", new Dialog.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				final ProgressDialog proDialog = new ProgressDialog(mContext);
				proDialog.setTitle("正在更换字体。。。稍后可能自动重启手机");
				proDialog.show();
				new AsyncTask<Void, Void, Integer>() {

					@Override
					protected void onPostExecute(Integer result) {
						proDialog.dismiss();
						String message = "";
						Log.e("tag", "msg = " + result);
						switch (result) {
						case 1:
							message = "中英文全部替换成功";
							break;
						case 2:
							message = "没有权限";
							break;
						case 3:
							message = "中文替换成功";
							break;
						case 4:
							message = "英文替换成功";
							break;
						case -1:
							message = "替换失败";
							break;
						case -2:
							message = "内存不足";
							break;
						}
						if (result == 1 || result == 3 || result == 4) {
							Toast.makeText(mContext, "换字体状态: " + message, Toast.LENGTH_SHORT).show();
							chan.changeSuccessed(mContext);
							
//							AlertDialog.Builder builder = new Builder(mContext);
//							builder.setTitle("温馨提示");
//							builder.setMessage("字体替换成功，有部分手机或许需要手动重启手机才能看到效果哦~~");
//							builder.setPositiveButton("确定", null);
							
							
							// AlertDialog.Builder builder = new
							// Builder(context);
							// builder.setMessage("换字体状态: " + message);
							// builder.setTitle("是否重启手机？");
							// builder.setPositiveButton("立即重启", new
							// OnClickListener() {
							//
							// @Override
							// public void onClick(DialogInterface dialog, int
							// which) {
							// dialog.dismiss();
							// chan.changeSuccessed(context);
							// }
							// });
							// builder.setNegativeButton("稍后重启", new
							// OnClickListener() {
							//
							// @Override
							// public void onClick(DialogInterface dialog, int
							// which) {
							// dialog.dismiss();
							// }
							// });
							// builder.create().show();
						} else {
							if (result == 2){
								Toast.makeText(mContext, "需要root才能更换字体" + message, Toast.LENGTH_SHORT).show();
							}else {
								Toast.makeText(mContext, "换字体状态: " + message, Toast.LENGTH_SHORT).show();
							}
						}
					}

					@Override
					protected Integer doInBackground(Void... params) {
						int a = chan.changeFont(mContext, font);
						return a;
					}
				}.execute();
			}
		});
		builder.setCancelable(true);
		builder.show();

	
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

	private void doStartApplicationWithPackageName(String packagename) {

		// 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
		PackageInfo packageinfo = null;
		try {
			packageinfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageinfo == null) {
			return;
		}

		// 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
		Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
		resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		resolveIntent.setPackage(packageinfo.packageName);

		// 通过getPackageManager()的queryIntentActivities方法遍历
		List<ResolveInfo> resolveinfoList = mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);

		ResolveInfo resolveinfo = resolveinfoList.iterator().next();
		if (resolveinfo != null) {
			// packagename = 参数packname
			String packageName = resolveinfo.activityInfo.packageName;
			// 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packagename.mainActivityname]
			String className = resolveinfo.activityInfo.name;
			// LAUNCHER Intent
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);

			// 设置ComponentName参数1:packagename参数2:MainActivity路径
			ComponentName cn = new ComponentName(packageName, className);

			intent.setComponent(cn);
			mContext.startActivity(intent);
		}
	}
}
