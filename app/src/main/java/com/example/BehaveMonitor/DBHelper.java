package com.example.BehaveMonitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper sInstance = null;

    private static final String DATABASE_NAME = "chickens.db";
    private static int DATABASE_VERSION = 1;

    private static final String PREFERENCES_CREATE =
            "CREATE Table Preferences (" +
                    "_id integer primary key," +
                    "LastFolder text not null," +
                    "LastTemplate text null);";

    public static DBHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DBHelper(context);
        }

        return sInstance;
    }

    private DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PREFERENCES_CREATE);

        insertInitialPreferences(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (DATABASE_VERSION != newVersion) {
            db.execSQL("DROP TABLE IF EXISTS Preferences");

            DATABASE_VERSION = newVersion;
            onCreate(db);
        }
    }

    private void insertInitialPreferences(SQLiteDatabase db) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("_id", 1);
        contentValues.put("LastFolder", "Default");
        long result = db.insert("Preferences", null, contentValues);
        result = result;
    }

    public String getFolder() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Preferences", new String[] {"LastFolder"}, null, null, null, null, null);
        String folder = "Default";
        if (cursor.moveToFirst()) {
            folder = cursor.getString(0);
        }

        cursor.close();
        return folder;
    }

    public String getTemplate() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("Preferences", new String[] {"LastTemplate"}, null, null, null, null, null);
        String template = null;
        if (cursor.moveToFirst()) {
            template = cursor.getString(0);
        }

        cursor.close();
        return template;
    }

    public void setFolderTemplate(String folder, String template) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("_id", 1);
        contentValues.put("LastFolder", folder);
        contentValues.put("LastTemplate", template);

        db.update("Preferences", contentValues, null, null);
    }

    public void setFolder(String folder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("_id", 1);
        contentValues.put("LastFolder", folder);

        db.update("Preferences", contentValues, null, null);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("DROP TABLE IF EXISTS Preferences");

        onCreate(db);
    }
}