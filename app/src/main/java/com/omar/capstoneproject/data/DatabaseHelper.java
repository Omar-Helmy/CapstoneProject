package com.omar.capstoneproject.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by OMAR on 06/04/2017.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    // Holds the database object
    private SQLiteDatabase db;

    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+DataContract.DATABASE_TABLE_NAME+
                " ("+DataContract._ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                DataContract.COLUMN_NAME+" TEXT NOT NULL, "+
                DataContract.COLUMN_PRICE+" TEXT NOT NULL, "+
                DataContract.COLUMN_DESCRIPTION+" TEXT NOT NULL, "+
                DataContract.COLUMN_INGREDIENT+" TEXT NOT NULL, "+
                DataContract.COLUMN_ORDER+" TEXT NOT NULL, "+
                DataContract.COLUMN_DETAILS+" TEXT NOT NULL, "+
                DataContract.COLUMN_IMAGE+" TEXT NOT NULL, "+
                DataContract.COLUMN_TYPE+" TEXT NOT NULL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + DataContract.DATABASE_TABLE_NAME);
        onCreate(db);
    }
}
