import itertools
import json

import arrow
from flask import Flask, request
import psycopg2

app = Flask(__name__)
app.debug = True

@app.route("/eligibility", methods=["GET"])
def eligibility():
    """ Checks eligibility for this particular user. """
    user_id = get_user_id()
    model, sdk_version, app_version = get_android_info()

    return json.dumps({
        "is_eligible": is_eligible_for_logcat(user_id, model, sdk_version, app_version)
    })

@app.route("/logevents", methods=["POST"])
def logevents():
    """ Actually flush the log events to our database. """
    post_data = json.loads(request.data)

    user_id = get_user_id()
    model, sdk_version, app_version = get_android_info()

    # confirm that this user should flush data
    if is_eligible_for_logcat(user_id, model, sdk_version, app_version):
        add_log_events_for_user(user_id, model, sdk_version, app_version, post_data["events"])

    return ""

def get_user_id():
    """ Normally, you'd fetch this from the request context (or request.session)
    This is just an arbitrary UUID. """

    return "9f13322ea7c04539bef01f488c70e431"

def get_android_info():
    """ Send these along as headers in your API request. """

    # android.os.Build.MODEL
    model = request.headers.get("X-Android-Model")

    # android.os.Build.VERSION.SDK_INT
    sdk_version = request.headers.get("X-Android-Sdk")

    # PackageManager manager = applicationContext.getPackageManager();
    # PackageInfo info = manager.getPackageInfo("com.your.package.name", 0);
    # int thisIsTheVersionCode = info.versionCode;
    app_version = request.headers.get("X-Android-AppVersion")

    return model, sdk_version, app_version

# database functions
def get_connection():
    conn = psycopg2.connect("dbname=phonehome user=postgres")
    # saves the manual connection.commit() for writes
    conn.autocommit = True
    return conn

def is_eligible_for_logcat(user_id, android_model, android_sdk_version, android_app_version):
    # find whether this user is eligible for at least one set of criteria; add all the enabled eligible ones
    with get_connection().cursor() as cursor:
        cursor.execute("""
            WITH search_criteria (user_id, android_model, android_sdk_version, android_app_version) AS (
                VALUES(%s::uuid, %s, %s::int, %s::int)
            ), eligible_criteria AS (
                SELECT lc.id AS logcat_criteria_id
                FROM logcat_criteria AS lc, search_criteria AS sc
                WHERE enabled = true
                AND (lc.user_id IS NULL
                    OR sc.user_id IS NOT DISTINCT FROM lc.user_id)
                AND (lc.android_model IS NULL
                    OR LOWER(sc.android_model) IS NOT DISTINCT FROM LOWER(lc.android_model))
                AND (lc.android_sdk_version IS NULL
                    OR sc.android_sdk_version IS NOT DISTINCT FROM lc.android_sdk_version)
                AND (lc.android_app_version IS NULL
                    OR sc.android_app_version IS NOT DISTINCT FROM lc.android_app_version)
            ), the_insert AS (
                INSERT INTO logcat_users (user_id, logcat_criteria_id)
                SELECT search_criteria.user_id, eligible_criteria.logcat_criteria_id
                FROM   eligible_criteria, search_criteria
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM logcat_users
                    WHERE user_id = search_criteria.user_id
                    AND   logcat_criteria_id = eligible_criteria.logcat_criteria_id
                )
            )
            SELECT logcat_criteria_id
            FROM eligible_criteria
            """, (
                user_id,
                android_model,
                android_sdk_version,
                android_app_version))
    return cursor.rowcount > 0

def add_log_events_for_user(user_id, android_model, android_sdk_version, android_app_version, log_events):
    """ Adds the specified lines for the user if they don't already exist. """
    unique_event_items = set( frozenset(event.items()) for event in log_events )
    unique_events = [ dict(event_items) for event_items in unique_event_items ]

    with get_connection().cursor() as cursor:
        cursor.execute("""
            WITH new_values (model, sdk_version, app_version) AS (
                VALUES(%s, %s::int, %s::int)
            ), existing_values AS (
                SELECT id
                FROM   android_info AS ai, new_values AS nv
                WHERE  LOWER(ai.model) = LOWER(nv.model)
                AND    ai.sdk_version = nv.sdk_version
                AND    ai.app_version = nv.app_version
            ), the_insert AS (
                INSERT INTO android_info (model, sdk_version, app_version)
                SELECT model, sdk_version, app_version
                FROM   new_values
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM existing_values
                    )
                RETURNING id
            )
            SELECT id
            FROM   the_insert
            UNION
            SELECT id
            FROM   existing_values
            """, (
                android_model,
                android_sdk_version,
                android_app_version
                ))
        assert cursor.rowcount == 1, "got {:d} rows, expected 1!".format(cursor.rowcount)
        android_info_id, = cursor.fetchone()

        BATCH_SIZE = 500
        for log_event_batch in iterbatch(unique_events, BATCH_SIZE):

            # ts is sent in milliseconds!
            vals = list(itertools.chain(*(
                (user_id, android_info_id, datetime_from_timestamp(d["ts"] / 1000.0), d["lvl"], d["tag"], d["msg"])
                for d in log_event_batch)))

            cursor.execute("""
                INSERT INTO logcat_events (user_id, android_info_id, client_ts, level, tag, message)
                VALUES {}""".format(
                    ", ".join(["(%s::uuid, %s::uuid, %s, %s::int, %s, %s)"]*len(log_event_batch))),
                vals)

def iterbatch(iterable, batch_size):
    assert batch_size > 0

    batch = []
    for idx, val in enumerate(iterable):
        if (batch and idx % batch_size == 0):
            yield batch
            batch = []
        batch.append(val)

    if batch:
        yield batch

def datetime_from_timestamp(ts):
    return arrow.get(ts).naive

if __name__ == "__main__":
    app.run()
