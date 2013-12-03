package com.example.phonehometest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import co.nebulabs.phonehome.PhoneHomeLogEvent;
import co.nebulabs.phonehome.PhoneHomeSink;

class ExampleSink implements PhoneHomeSink {
	AndroidHttpClient httpClient = AndroidHttpClient.newInstance("PhoneHomeTest"); 

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
			protected Void doInBackground(Void... params) {
				String postBody;
				try {
					postBody = encodeLogEvents(logEvents);
				} catch (JSONException ex) {
					throw new RuntimeException(ex);
				}

				StringEntity postEntity;
				try {
					postEntity = new StringEntity(postBody, "UTF-8");
				} catch (UnsupportedEncodingException ex) {
					throw new RuntimeException(ex);
				}
				postEntity.setContentType("application/json");

				HttpPost httpPost = new HttpPost("http://example.com/api/logevents");
				httpPost.setEntity(postEntity);

				try {
					httpClient.execute(httpPost);
				} catch (IOException ex) {
					throw new RuntimeException(ex);
				}
				return null;
			}
		}.execute();
	}
}