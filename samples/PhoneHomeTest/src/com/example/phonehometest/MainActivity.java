package com.example.phonehometest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import co.nebulabs.phonehome.PhoneHomeConfig;
import co.nebulabs.phonehome.PhoneHomeLogEvent;
import co.nebulabs.phonehome.PhoneHomeLogger;
import co.nebulabs.phonehome.PhoneHomeSink;

public class MainActivity extends Activity {
	private static final PhoneHomeLogger Log = PhoneHomeLogger.forClass(MainActivity.class);

	private static class ExampleSink implements PhoneHomeSink {
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

					HttpPost httpPost = new HttpPost("http://10.0.2.2:8080/api/logevents");
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		PhoneHomeConfig.getInstance()
		// disable sending log flushing for now (toggled by the button in this activity)
		.enabled(false)
		// wait until we have this many events to flush a batch of logs...
		.batchSize(100)
		// ... or until this many seconds have passed since our last flush
		.flushIntervalSeconds(5)
		// when developing, log all messages to logcat
		.debugLogLevel(android.util.Log.VERBOSE)
		// in production, only log INFO messages and above
		.productionLogLevel(android.util.Log.INFO)
		// the actual sink used when it's time to flush logs (required if you ever enable log flushing!)
		.logSink(new ExampleSink());

		
		// queue up a handler to log some stuff
		final Handler handler = new Handler();
		handler.post(new Runnable() {
			int counter = 0;

			@Override
			public void run() {
				String text = "I've run " + ++counter + " times!"; 
				((TextView) findViewById(R.id.counterText)).setText(text);

				if (counter % 2 == 0)
					Log.i(text);
				Log.d(text);

				handler.postDelayed(this, 1000);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private boolean phoneHomeEnabled = false;
	public void toggleFlushing(View view) {
		phoneHomeEnabled = !phoneHomeEnabled;

		PhoneHomeConfig.getInstance()
		.enabled(phoneHomeEnabled);

		((Button) view).setText(phoneHomeEnabled ? "Disable" : "Enable");
	}
}
