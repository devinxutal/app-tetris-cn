package com.devinxutal.tetris.util;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;

import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetriscn.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;

public class AdUtil {
	// public static com.admob.android.ads.AdView createAdViewOldSDK(
	// Activity activity) {
	// com.admob.android.ads.AdView adView = new com.admob.android.ads.AdView(
	// activity);
	// adView.setBackgroundColor(Color.rgb(0, 0, 0));
	// // adView.setRequestInterval(12);
	// // for test
	// // AdRequest request = new AdRequest();
	// // AdManager manager = new AdManager();
	//
	// // end for test
	// Log.v("AdUtil", "Request Interval :" + adView.getRequestInterval());
	// adView.setPrimaryTextColor(Color.rgb(255, 255, 255));
	// adView.setSecondaryTextColor(Color.rgb(180, 180, 180));
	// adView.setId(Constants.ADVIEW_ID);
	//
	// return adView;
	//
	// }

	public static com.google.ads.AdView createAdmobAdView(
			final Activity activity) {
		final com.google.ads.AdView adView = new com.google.ads.AdView(
				activity, AdSize.BANNER, Constants.ADMOB_PUBLISHER_ID);

		adView.setId(Constants.ADVIEW_ID);
		return adView;

	}

	public static net.youmi.android.banner.AdView createYoumiAdView(
			final Activity activity) {
		final net.youmi.android.banner.AdView adView = new net.youmi.android.banner.AdView(
				activity, net.youmi.android.banner.AdSize.SIZE_320x50);

		adView.setId(Constants.ADVIEW_ID);
		return adView;
	}

	public static AdRequest getAdRequest() {
		AdRequest req = new AdRequest();
		req.setTesting(true);
		return req;
	}

	private static View createAdView(Activity activity) {
		View adview = null;
		boolean useAdmob = false;
		if (useAdmob) {
			adview = createAdmobAdView(activity);

		} else {
			adview = createYoumiAdView(activity);
		}
		// else {
		// adview = createAdViewOldSDK(activity);
		// }
		return adview;

	}

	public static void determineAd(Activity activity) {
		if (Constants.VERSION == Constants.VERSION_LITE) {
			((LinearLayout) activity.findViewById(R.id.ad_area))
					.addView(createAdView(activity));
		}
	}

	public static void determineAd(Activity activity, int adAreaID) {
		if (Constants.VERSION == Constants.VERSION_LITE) {
			((LinearLayout) activity.findViewById(adAreaID))
					.addView(createAdView(activity));
		}
	}
}
