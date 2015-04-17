package com.toraysoft.zitimanager_lib;

import java.io.File;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;
import android.widget.TextView;

import com.xinmei365.fontsdk.FontCenter;
import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.ThumbnailCallBack;

public class FontListView extends ListView implements OnScrollListener{

	public FontListView(Context context) {
		super(context);
		setOnScrollListener(this);
	}
	
	public FontListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setOnScrollListener(this);
	}
	
	public FontListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnScrollListener(this);
	}

	
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE) {
            int first = view.getFirstVisiblePosition();
            int count = view.getChildCount();
            for (int i = 0; i < count; i++) {
                Object obj = view.getAdapter().getItem(i + first);
                if (!(obj instanceof Font)) {
                    continue;
                }
                final Font fd = (Font) obj;

                final View viewholder = view.getChildAt(i);
                final TextView name = (TextView) viewholder.findViewById(R.id.font_name);
                File f = new File(fd.getThumbnailLocalPath());
                if (!f.exists()) {
                    if (fd == null || fd.getThumbnailUrl() == null) {
                        continue;
                    }
                    setTypeface(fd, name);
                } else {
                    try {
                        Typeface face = Typeface.createFromFile(fd
                                .getThumbnailLocalPath());
                        name.setTypeface(face);
                    } catch (Exception e) {
                        e.printStackTrace();
                        name.setTypeface(Typeface.DEFAULT);
                    }
                }
            }
        }
    }

	@Override
	public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
	}

	
	
	
	public static boolean setTypeface(final Font fd, final TextView tvName) {

		File f = new File(fd.getThumbnailLocalPath());
		if (f.exists()) { // 字体已下载，直接读取typeface

			try {
				Typeface face = Typeface.createFromFile(new File(fd.getThumbnailLocalPath()));
				tvName.setTypeface(face);
				tvName.setText(fd.getFontName());
			} catch (Exception e) {
				e.printStackTrace();
				tvName.setTypeface(Typeface.DEFAULT);
			}
			return true;
		} else {
			tvName.setTypeface(Typeface.DEFAULT);
			FontCenter.getInstance().getThumbnail(new ThumbnailCallBack() {


				@Override
				public void onSuccessed(String id ,Typeface typeface) {
					Log.i("AAAA", id);
					setTypeface(fd, tvName);
				}


				@Override
				public void onFailed(String id , String msg) {
				}
			}, fd);
		}
		return false;
	}


	
}
