package com.example.phonehometest;



import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import co.nebulabs.phonehome.PhoneHomeConfig;
import co.nebulabs.phonehome.PhoneHomeLogger;

import com.example.phonehometest.example.ExampleEligibilityChecker;
import com.example.phonehometest.example.ExampleEligibilityChecker.EligibilityCallback;
import com.example.phonehometest.example.ExampleSink;
import com.example.phonehometest.example.Utils;
import com.example.phonehometest.example.Utils.AndroidInfo;

public class MainActivity extends Activity {
	private static final PhoneHomeLogger Log = PhoneHomeLogger.forClass(MainActivity.class);
	private ExampleEligibilityChecker eligibilityChecker;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		PhoneHomeConfig.getInstance()
		// disable sending log flushing for now (eligibility checked by button below)
		.enabled(false)
		// wait until we have this many events to flush a batch of logs...
		.batchSize(100)
		// ... or until this many seconds have passed since our last flush
		.flushIntervalSeconds(1800)
		// when developing, log all messages to logcat (everything is flushed to our sink)
		.debugLogLevel(android.util.Log.VERBOSE)
		// in production, only log INFO messages and above to logcat (everything is flushed to our sink)
		.productionLogLevel(android.util.Log.INFO)
		// the actual sink used when it's time to flush logs (required if you ever enable log flushing!)
		.logSink(new ExampleSink(this));


		eligibilityChecker = new ExampleEligibilityChecker(this);


		AndroidInfo androidInfo = Utils.getAndroidInfo(this);
		((TextView) findViewById(R.id.model)).setText("Model: " + androidInfo.model);
		((TextView) findViewById(R.id.sdkVersion)).setText("SDK version: " + Integer.toString(androidInfo.sdkVersion));
		((TextView) findViewById(R.id.appVersion)).setText("App version: " + Integer.toString(androidInfo.appVersion));

		// queue up a handler to create logs
		final Handler handler = new Handler();
		handler.post(new Runnable() {
			int debugCounter = 0;
			int infoCounter = 0;

			@Override
			public void run() {
				debugCounter++;
				Log.d("Debug event #" + debugCounter);

				if (debugCounter % 2 == 0) {
					infoCounter++;
					Log.d("Info event #" + infoCounter);
				}

				((TextView) findViewById(R.id.counterText)).setText("Events: " + debugCounter + " debug, " + infoCounter + " info");

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

	public void checkEligibility(View view) {
		eligibilityChecker.checkEligibility(
				new EligibilityCallback() {
					@Override
					public void handleEligibilty(boolean isEligible) {
						PhoneHomeConfig.getInstance()
						.enabled(isEligible);

						String description;
						if (isEligible) {
							description = "Eligible to send logs! Make sure you're receiving them on the backend!";
						} else {
							description = "NOT eligible to receive logs. Are you sure that the android info above matches one of your logcat criteria?";
						}

						((TextView) findViewById(R.id.description)).setText(description);
					}
				});
	}
}
