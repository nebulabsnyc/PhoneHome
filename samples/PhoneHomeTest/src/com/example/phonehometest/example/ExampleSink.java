package com.example.phonehometest.example;

import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import co.nebulabs.phonehome.PhoneHomeLogEvent;
import co.nebulabs.phonehome.PhoneHomeSink;

public class ExampleSink implements PhoneHomeSink {
	// matches with the example backend provided
	private static final String HTTP_ENDPOINT = Utils.HTTP_BASE + "/logevents";

	private final Context context;
	private final AndroidHttpClient httpClient = AndroidHttpClient.newInstance("PhoneHomeTest");

	public ExampleSink(final Context context) {
		this.context = context;
	}

	private String encodeLogEvents(final List<PhoneHomeLogEvent> logEvents) throws JSONException {
		// JSON-encoded the hard way; you're better off using something nice like Jackson or Gson
		JSONArray array = new JSONArray();
		for (PhoneHomeLogEvent event : logEvents) {
			JSONObject eventObj = new JSONObject();
			eventObj.put("ts", event.getTimestamp());
			eventObj.put("lvl", event.getLevel());
			eventObj.put("tag", event.getTag());
			eventObj.put("msg", event.getMessage());
			array.put(eventObj);
		}

		JSONObject obj = new JSONObject();
		obj.put("events", array);
		return obj.toString();
	}

	@Override
	public void flushLogs(final List<PhoneHomeLogEvent> logEvents) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(final Void... params) {
				try {
					String postBody = encodeLogEvents(logEvents);

					StringEntity postEntity = new StringEntity(postBody, "UTF-8");
					postEntity.setContentType("application/json");

					HttpPost httpPost = new HttpPost(HTTP_ENDPOINT);
					httpPost.setEntity(postEntity);
					httpPost.setHeaders(Utils.getAndroidHeaders(context));

					httpClient.execute(httpPost);

				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
				return null;
			}
		}.execute();
	}
}