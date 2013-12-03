package com.example.phonehometest;



import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import co.nebulabs.phonehome.PhoneHomeConfig;
import co.nebulabs.phonehome.PhoneHomeLogger;

public class MainActivity extends Activity {
	private static final PhoneHomeLogger Log = PhoneHomeLogger.forClass(MainActivity.class);

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
		.flushIntervalSeconds(1800)
		// when developing, log all messages to logcat (everything is flushed to our sink)
		.debugLogLevel(android.util.Log.VERBOSE)
		// in production, only log INFO messages and above to logcat (everything is flushed to our sink)
		.productionLogLevel(android.util.Log.INFO)
		// the actual sink used when it's time to flush logs (required if you ever enable log flushing!)
		.logSink(new ExampleSink());

		// queue up a handler to create logs
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
