package com.toraysoft.zitimanager_lib;

import com.xinmei365.fontsdk.bean.Font;
import com.xinmei365.fontsdk.callback.IChangeFont;

public class ChangeFont implements IChangeFont{

	@Override
	public void ChangeFont(Font font) {
		System.out.println("====Got font "+font.getFontName()+"====");
	}

}
