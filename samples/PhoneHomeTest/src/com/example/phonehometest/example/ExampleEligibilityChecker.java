package com.example.phonehometest.example;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;

public final class ExampleEligibilityChecker {
	// matches with the example backend provided
	private static final String HTTP_ENDPOINT = Utils.HTTP_BASE + "/eligibility";

	private final Context context;
	private final AndroidHttpClient httpClient = AndroidHttpClient.newInstance("PhoneHomeTest");

	public ExampleEligibilityChecker(Context context) {
		this.context = context;
	}

	public interface EligibilityCallback {
		public void handleEligibilty(boolean isEligible);
	}

	public void checkEligibility(final EligibilityCallback eligibilityCallback) {
		new AsyncTask<Void, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(Void... params) {
				HttpGet httpGet = new HttpGet(HTTP_ENDPOINT);
				httpGet.setHeaders(Utils.getAndroidHeaders(context));

				HttpResponse response;
				try {
					response = httpClient.execute(httpGet);
					String json = EntityUtils.toString(response.getEntity(), "UTF-8");
					
					JSONObject obj = new JSONObject(json);
					return obj.getBoolean("is_eligible");
					
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
			
			@Override
			protected void onPostExecute(final Boolean isEligible) {
				eligibilityCallback.handleEligibilty(isEligible);
			}
		}.execute();
	}
}
