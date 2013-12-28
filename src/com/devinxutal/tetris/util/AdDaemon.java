package com.devinxutal.tetris.util;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;

public class AdDaemon {
	public static final String TAG = "AdDaemon";
	public static final int ANIMATION_INTERVAL = 15000;
	private Activity activity;
	private View adView;
	private Handler handler;
	private String name;

	private boolean run = false;

	private long animationSerial = 0;
	private long requestSerial = 0;
	private boolean requestSucceed = false;
	private boolean nofill = false;
	private boolean adClicked = false;

	public void run() {
		if (!run) {
			run = true;
			this.postAnimation();
			this.postRequest(100);
		}
	}

	public void stop() {
		this.run = false;
	}

	public AdDaemon(String name, Activity activity, View adView, Handler handler) {
		super();
		this.activity = activity;
		this.adView = adView;
		this.handler = handler;
		this.name = name;
		if (this.adView == null) {
			return;
		}
		this.adView.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				Log.v(TAG, "Ad Clicked");
				adClicked = true;
			}
		});
		if (this.adView instanceof com.google.ads.AdView) {
			final com.google.ads.AdView v = (com.google.ads.AdView) adView;
			v.setAdListener(new com.google.ads.AdListener() {

				public void onDismissScreen(com.google.ads.Ad ad) {
					Log.v(TAG, "onDismissScreen");
				}

				public void onFailedToReceiveAd(com.google.ads.Ad ad,
						com.google.ads.AdRequest.ErrorCode code) {
					if (code == com.google.ads.AdRequest.ErrorCode.NO_FILL) {
						requestSucceed = false;
						nofill = true;
					} else {
						nofill = false;
					}
					Log.v(TAG, "onFailedToReceiveAd: " + code);

				}

				public void onLeaveApplication(com.google.ads.Ad ad) {
					Log.v(TAG, "onLeaveApplication");
				}

				public void onPresentScreen(com.google.ads.Ad ad) {
					Log.v(TAG, "onPresentScreen");
				}

				public void onReceiveAd(com.google.ads.Ad ad) {
					requestSucceed = true;
					Log.v(TAG, "onReceiveAd");
				}
			});
		}else if (this.adView instanceof net.youmi.android.banner.AdView) {
			final net.youmi.android.banner.AdView v = (net.youmi.android.banner.AdView) adView;
			
		}
	}

	private void postAnimation() {
		animationSerial++;
		this.handler.postDelayed(new AnimationRunnable(animationSerial),
				ANIMATION_INTERVAL);

	}

	private long requestInterval = 10000;
	private long requestCount = 0;

	private void postRequest() {
		requestCount++;
		if (requestCount > 3) {
			requestCount = 0;
			requestInterval = requestInterval * 3 / 2;
		}
		postRequest(requestInterval);

	}

	private void postRequest(long interval) {
		requestSerial++;
		this.handler.postDelayed(new RequestRunnable(requestSerial), interval);

	}

	private String getName() {
		return "[ AdDaemon " + name + " ]";
	}

	class RequestRunnable implements Runnable {
		private long serialID = 0;

		public RequestRunnable(long serialID) {
			this.serialID = serialID;
		}

		public void run() {
			Log.v(TAG, getName() + "requestRunnable running");
			if (activity.isFinishing() || !run) {
				return;
			}
			if (this.serialID != AdDaemon.this.requestSerial) {
				return;
			}
			if (!requestSucceed) {

				Log
						.v(TAG, getName()
								+ "requestRunnable: no ad, request for ad");
				if (nofill) {
					nofill = false;
					postRequest(120000);
				} else {
					if (adView != null) {
						if (adView instanceof com.google.ads.AdView) {
							((com.google.ads.AdView) adView).loadAd(AdUtil
									.getAdRequest());
						}if (adView instanceof net.youmi.android.banner.AdView) {
							final net.youmi.android.banner.AdView v = 
								(net.youmi.android.banner.AdView) adView;
						}

					}
					postRequest();
				}

			} else if (requestSucceed && adClicked) {
				Log.v(TAG, getName()
						+ "requestRunnable: click detected, change ad");
				requestSucceed = false;
				nofill = false;
				postRequest(1000);
			} else {
				Log.v(TAG, getName()
						+ "requestRunnable: ad showed but no click, wait");
				postRequest(1000 * 60);
			}
		}
	}

	class AnimationRunnable implements Runnable {
		private long serialID = 0;

		public AnimationRunnable(long serialID) {
			this.serialID = serialID;
		}

		public void run() {
			if (activity.isFinishing()) {
				return;
			}
			if (this.serialID != AdDaemon.this.animationSerial) {
				return;
			}
			partialAnimationRunnable.run();
			postAnimation();
		}
	}

	private Runnable partialAnimationRunnable = new Runnable() {
		public void run() {
			if (adView != null) {
				Animation ani = new ScaleAnimation(1f, 1f, 1f, 0f,
						Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				ani.setDuration(600);
				ani.setAnimationListener(new AnimationListener() {

					public void onAnimationStart(Animation ad) {
					}

					public void onAnimationRepeat(Animation ad) {
					}

					public void onAnimationEnd(Animation ad) {
						Animation ani = new ScaleAnimation(1f, 1f, 0f, 1f,
								Animation.RELATIVE_TO_SELF, 0.5f,
								Animation.RELATIVE_TO_SELF, 0.5f);
						ani.setDuration(600);
						adView.startAnimation(ani);
					}
				});
				adView.startAnimation(ani);
			}
		}
	};

}