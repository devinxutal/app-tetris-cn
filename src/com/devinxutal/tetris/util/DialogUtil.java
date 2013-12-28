package com.devinxutal.tetris.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.devinxutal.tetris.cfg.Constants;
import com.devinxutal.tetris.record.TwentySecondsRecord;
import com.devinxutal.tetriscn.R;

public class DialogUtil {
	public static void showDialog(Context context, int title, int content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(content).setCancelable(false)
				.setPositiveButton(R.string.common_ok, null);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public static void showDialog(Context context, String title, String content) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setMessage(content).setCancelable(false)
				.setPositiveButton(R.string.common_ok, null);
		AlertDialog alert = builder.create();
		alert.show();

	}

	public static void showRankDialog(final Activity context) {
		AlertDialog.Builder alert = new AlertDialog.Builder(context);
		final ProgressDialog dialog = ProgressDialog.show(context, "", context
				.getResources().getString(
						R.string.success_screen_retrieving_data), true);
		Thread thread = new Thread() {

			public void run() {
				String url = Constants.URL_QUERY_RECORD;
				Map<String, String> data = new HashMap<String, String>();
				data.put("from", 1 + "");
				data.put("count", 10 + "");
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url);
				ArrayList<BasicNameValuePair> postData = new ArrayList<BasicNameValuePair>();
				for (Map.Entry<String, String> m : data.entrySet()) {
					postData.add(new BasicNameValuePair(m.getKey(), m
							.getValue()));
				}
				int statusCode = HttpStatus.SC_ACCEPTED;
				final List<TwentySecondsRecord> records = new LinkedList<TwentySecondsRecord>();
				try {
					UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
							postData, HTTP.UTF_8);

					httpPost.setEntity(entity);

					HttpResponse response = httpClient.execute(httpPost);
					statusCode = response.getStatusLine().getStatusCode();
					if (statusCode == HttpStatus.SC_OK) {
						BufferedReader reader = new BufferedReader(
								new InputStreamReader(response.getEntity()
										.getContent()));
						String line = null;
						while ((line = reader.readLine()) != null) {
							if (line.trim().length() > 0) {
								Log.v("DialogUtil", line);
								TwentySecondsRecord record = TwentySecondsRecord
										.parse(line);
								if (record != null) {
									records.add(record);
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				final int sc = statusCode;
				context.runOnUiThread(new Runnable() {

					public void run() {
						if (dialog != null) {
							dialog.cancel();
						}
						if (sc == HttpStatus.SC_OK) {
							AlertDialog.Builder alert = new AlertDialog.Builder(
									context);
							alert.setView(createRankView(context, records));

							alert.setTitle(context.getResources().getString(
									R.string.success_screen_world_rank));

							alert.setPositiveButton(R.string.common_ok,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {

										}
									});
							alert.setNegativeButton(R.string.common_more,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int whichButton) {
											Intent goToMarket = null;
											goToMarket = new Intent(
													Intent.ACTION_VIEW,
													Uri
															.parse(Constants.URL_WORLD_RANK));
											((Activity) context)
													.startActivity(goToMarket);
										}
									});
							alert.show();
						} else {
							Toast.makeText(context, "Network connection error",
									1000).show();
						}
					}

				});
			}
		};
		thread.start();
	}

	private static View createRankView(Activity context,
			List<TwentySecondsRecord> records) {
		ScrollView ll = new ScrollView(context);
		ll.setScrollContainer(true);
		TableLayout layout = new TableLayout(context);
		layout.setStretchAllColumns(true);
		layout.addView(createRankHeader(context), new TableLayout.LayoutParams(
				TableLayout.LayoutParams.FILL_PARENT,
				TableLayout.LayoutParams.WRAP_CONTENT));
		for (TwentySecondsRecord record : records) {
			layout.addView(createRankEntry(context, record),
					new TableLayout.LayoutParams(
							TableLayout.LayoutParams.FILL_PARENT,
							TableLayout.LayoutParams.WRAP_CONTENT));

		}
		ll.addView(layout);
		return ll;
	}

	public static TableRow createRankEntry(Context context,
			TwentySecondsRecord record) {
		TableRow row = new TableRow(context);

		row.addView(createTableCell(context, record.getRank() + ""),
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.addView(createTableCell(context, record.getPlayer() + ""),
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		row.addView(createTableCell(context, record.getTimeString() + ""),
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		String mr = "General";
		row.addView(createTableCell(context, mr), LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		return row;
	}

	public static TableRow createRankHeader(Activity context) {
		TableRow row = new TableRow(context);
		//
		// row.addView(createTableCell(context,
		// context.getResources().getString(
		// R.string.record_rank)), LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT);
		// row.addView(createTableCell(context,
		// context.getResources().getString(
		// R.string.record_player)), LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT);
		// row.addView(createTableCell(context,
		// context.getResources().getString(
		// R.string.record_time)), LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT);
		// row.addView(createTableCell(context,
		// context.getResources().getString(
		// R.string.record_military_rank)), LayoutParams.FILL_PARENT,
		// LayoutParams.WRAP_CONTENT);
		return row;
	}

	public static TextView createTableCell(Context context, String content) {
		TextView view = new TextView(context);
		view.setText(content);
		view.setPadding(5, 2, 5, 2);
		return view;
	}

	public static void showDialogWithView(Context context, String title,
			int view_id) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout l = new LinearLayout(context);
		inflater.inflate(R.layout.help, l);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(title).setCancelable(false).setPositiveButton(
				R.string.common_ok, null).setView(l);
		AlertDialog alert = builder.create();

		// alert.setContentView(view_id);
		alert.show();

	}

}
