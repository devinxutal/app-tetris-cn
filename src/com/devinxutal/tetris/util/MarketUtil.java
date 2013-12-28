package com.devinxutal.tetris.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class MarketUtil {
	public static final int MARKET_ANDROID = 0;
	public static final int MARKET_AMAZON = 1;
	public static final int MARKET_SLIDEME = 2;
	public static final int MARKET_MOTO = 3;
	public static final int MARKET_OPHONE = 4;

	public static int TARGET_MARKET = MARKET_ANDROID;

	public static void openMarketForApp(Activity activity, String pkg) {
		Intent i = null;
		String url = null;
		switch (TARGET_MARKET) {
		case MARKET_ANDROID:

			url = "market://details?id=" + pkg;
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			break;
		case MARKET_AMAZON:
			url = "http://www.amazon.com/gp/mas/dl/android?p=" + pkg;
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			break;
		}
		activity.startActivity(i);
	}

	public static void openMarketForAuthor(Activity activity, String author) {
		Intent i = null;
		switch (TARGET_MARKET) {
		case MARKET_ANDROID:
			i = new Intent(Intent.ACTION_SEARCH);
			i.setPackage("com.android.vending");
			i.putExtra("query", "pub:\"" + author + "\"");
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
		case MARKET_AMAZON:
			String url = "http://www.amazon.com/gp/mas/dl/android?s=" + author;
			i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			break;
		}
		activity.startActivity(i);
	}
}
