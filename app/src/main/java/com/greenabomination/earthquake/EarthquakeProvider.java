package com.greenabomination.earthquake;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import java.sql.SQLException;
import java.util.HashMap;

/**
 * Created by green on 06.04.15.
 */
public class EarthquakeProvider extends ContentProvider {

    public static final Uri CONTENT_URI = Uri.parse("content://com.greenabomination.earthquake/earthquakes");

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_DETAILS = "details";
    public static final String KEY_SUMMARY = "summary";
    public static final String KEY_LOCATION_LAT = "latitude";
    public static final String KEY_LOCATION_LNG = "longitude";
    public static final String KEY_MAGNITUDE = "magnitude";
    public static final String KEY_LINK = "link";

    private static final int QUAKES = 1;
    private static final int QUAKE_ID = 2;
    private static final int SEARCH = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("com.greenabomination.earthquake", "earthquakes", QUAKES);
        uriMatcher.addURI("com.greenabomination.earthquake", "earthquakes/#", QUAKE_ID);
        uriMatcher.addURI("com.greenabomination.earthquake", SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH);
        uriMatcher.addURI("com.greenabomination.earthquake", SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH);
        uriMatcher.addURI("com.greenabomination.earthquake", SearchManager.SUGGEST_URI_PATH_SHORTCUT, SEARCH);
        uriMatcher.addURI("com.greenabomination.earthquake", SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", SEARCH);
    }

    private static final HashMap<String, String> SEARCH_PROJECTION_MAP;

    static {
        SEARCH_PROJECTION_MAP = new HashMap<String, String>();
        SEARCH_PROJECTION_MAP.put(SearchManager.SUGGEST_COLUMN_TEXT_1,
                KEY_SUMMARY + " AS " + SearchManager.SUGGEST_COLUMN_TEXT_1);
        SEARCH_PROJECTION_MAP.put("_id",
                KEY_ID + " AS _id");
    }


    EarthquakeDatabaseHelper dbHelper;


    @Override
    public boolean onCreate() {
        Context context = getContext();

        dbHelper = new EarthquakeDatabaseHelper(context,
                EarthquakeDatabaseHelper.DATABASE_NAME,
                null,
                EarthquakeDatabaseHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase database = dbHelper.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(EarthquakeDatabaseHelper.DATABASE_TABLE);

        switch (uriMatcher.match(uri)) {
            case QUAKE_ID:
                qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
                break;
            case SEARCH:
                qb.appendWhere(KEY_SUMMARY + " LIKE \"%" + uri.getPathSegments().get(1) + "%\"");
                qb.setProjectionMap(SEARCH_PROJECTION_MAP);
                break;
            default:
                break;
        }

        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = KEY_DATE;
        } else {
            orderBy = sortOrder;
        }

        Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                return "vnd.android.cursor.dir/vnd.greenabomination.earthquake";
            case QUAKE_ID:
                return "vnd.android.cursor.item/vnd.greenabomination.earthquake";
            case SEARCH:
                return SearchManager.SUGGEST_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long rowID = database.insert(EarthquakeDatabaseHelper.DATABASE_TABLE, "quake", values);

        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }
        try {
            throw new SQLException("Failed to insert row into " + uri);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        int cnt;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                cnt = database.delete(EarthquakeDatabaseHelper.DATABASE_TABLE, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String segment = uri.getPathSegments().get(1);
                cnt = database.delete(EarthquakeDatabaseHelper.DATABASE_TABLE,
                        KEY_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);

        }

        getContext().getContentResolver().notifyChange(uri, null);

        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        int cnt;
        switch (uriMatcher.match(uri)) {
            case QUAKES:
                cnt = database.update(EarthquakeDatabaseHelper.DATABASE_TABLE,
                        values, selection, selectionArgs);
                break;
            case QUAKE_ID:
                String segment = uri.getPathSegments().get(1);
                cnt = database.update(EarthquakeDatabaseHelper.DATABASE_TABLE, values,
                        KEY_ID + "=" + segment + (!TextUtils.isEmpty(selection) ? " AND ("
                                + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unsupported uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return cnt;
    }

    private static class EarthquakeDatabaseHelper extends SQLiteOpenHelper {
        public static final String TAG = "EarthquakeProvider";

        private static final String DATABASE_NAME = "earthquakes.db";
        private static final int DATABASE_VERSION = 1;
        private static final String DATABASE_TABLE = "earthquakes";
        private static final String DATABASE_CREATE =
                "create table " + DATABASE_TABLE + " ( "
                        + KEY_ID + " integer primary key autoincrement, "
                        + KEY_DATE + " integer, "
                        + KEY_DETAILS + " TEXT, "
                        + KEY_SUMMARY + " TEXT, "
                        + KEY_LOCATION_LAT + " FLOAT, "
                        + KEY_LOCATION_LNG + " FLOAT, "
                        + KEY_MAGNITUDE + " FLOAT, "
                        + KEY_LINK + " TEXT);";

        private SQLiteDatabase earthquakeDB;

        public EarthquakeDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(db);
        }
    }
}
