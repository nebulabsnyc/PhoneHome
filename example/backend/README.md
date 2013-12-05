Example backend
==============

This is an example backend for determining log flushing eligibility and storing the logs.

Flask server
------------

To begin, install PostgreSQL.  The example was written on 9.1, but it should be compatible with future versions, too.

Set up your database:

``$ sudo -u postgres psql -f db.psql``

This inserts a sample, hardcoded user ('testuser') and an initial set of criteria (versionCode == 1, which matches the PhoneHomeTest app in the example directory).

Install the dependencies.  This may involve installing libpq-dev for interacting with PostgreSQL.

``$ pip install arrow flask psycopg2``

Run your flask server and note the IP address.  You may have to change it to run on host=0.0.0.0 to make it externally accessible.  It depends on where you're running your mobile app.

``$ python flaskapp.py``

Using PhoneHomeTest
-------------------

If you'd like to use the PhoneHomeTest to show off the backend you set up, change `com.example.phonehometest.example.Utils.HTTP_BASE` to point at your Flask server.  If you're running in an emulator, use `http://10.0.2.2:5000`.  Otherwise, run your Flask app on host=0.0.0.0 (see above) and use `http://<YOUR_IP>:5000`.

Check eligibility by clicking the button.  The app generates logs and flushes them in batches of 100 or every 30 minutes.

Configuration
-------------

You can add new criteria or users that you'd like to send logs to your backend by modifying the logcat\_criteria table.  View the logs themselves in logcat\_events.  They're best ordered by `(client_ts, insert_order)`.
