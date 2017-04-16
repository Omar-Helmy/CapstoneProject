package com.omar.capstoneproject.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class DataProvider extends ContentProvider {

    // Creates a UriMatcher object.
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TABLE_MATCH=1, ITEM_NAME_MATCH=2, ITEM_ID_MATCH=3;

    // Get database object
    DatabaseHelper databaseHelper;

    static {
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.DATABASE_TABLE_NAME, TABLE_MATCH);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.DATABASE_TABLE_NAME+"/#", ITEM_ID_MATCH);
        uriMatcher.addURI(DataContract.AUTHORITY, DataContract.DATABASE_TABLE_NAME+"/*", ITEM_NAME_MATCH);
    }

    public DataProvider() {
    }

    @Override
    public boolean onCreate() {
         // Creates a new helper object. This method always returns quickly.
        databaseHelper = new DatabaseHelper(
                getContext(),        // the application context
                DataContract.DATABASE_NAME,              // the name of the database)
                null,                // uses the default SQLite cursor
                DataContract.DATABASE_VERSION                    // the version number
        );

        return true;
    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
        /*switch (uriMatcher.match(uri)) {
            case TABLE_MATCH:
        }*/
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        Uri newUri;
        long id;
        switch (uriMatcher.match(uri)){
            case TABLE_MATCH:
                id = db.insert(DataContract.DATABASE_TABLE_NAME,null,contentValues);
                if(id>0){
                    newUri = DataContract.appendToUri(id);
                    getContext().getContentResolver().notifyChange(newUri, null);
                    break;
                }
            default: throw new UnsupportedOperationException("URI not matched!");
        }
        db.close();
        return newUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor newCursor;
        switch (uriMatcher.match(uri)){
            case TABLE_MATCH:
                newCursor = db.query(DataContract.DATABASE_TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case ITEM_ID_MATCH: /*NOTE: must match numeric # first before * as * will match any number or string!! */
                newCursor = db.query(DataContract.DATABASE_TABLE_NAME,projection,
                        DataContract._ID+" == "+DataContract.getIdFromUri(uri),selectionArgs,null,null,sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;
            case ITEM_NAME_MATCH:
                newCursor = db.query(DataContract.DATABASE_TABLE_NAME,projection,
                        DataContract.COLUMN_NAME+" == "+uri.getLastPathSegment(),selectionArgs,null,null,sortOrder);
                newCursor.setNotificationUri(getContext().getContentResolver(), uri);
                break;

            default: throw new UnsupportedOperationException("URI not matched!");
        }
        //db.close();   // causes crash!!
        return newCursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

/*
    https://guides.codepath.com/android/Creating-Content-Providers
 */
