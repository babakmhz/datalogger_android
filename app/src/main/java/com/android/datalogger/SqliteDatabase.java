package com.android.datalogger;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SqliteDatabase extends SQLiteOpenHelper {

    private static final String DB_NAME = "data_logger";
    private static final String MAIN_TABLE = "main_table";

    private static final String MAIN_DATA = "data";
    private static final String DATE = "date";

    private static final int DB_VERSION = 1;


    private static final String CREATE_CATEGORIES_TABLE
            = "CREATE TABLE IF NOT EXISTS " + MAIN_TABLE + " ( " + MAIN_DATA + "" +
            " TEXT , " + DATE + " TEXT );";

    public SqliteDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CATEGORIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void saveData(DataModel model) {
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MAIN_DATA, model.getData());
        contentValues.put(DATE, model.getDate());
        database.insert(MAIN_TABLE, null, contentValues);
        database.close();
    }

    public List<DataModel> getData() {
        List<DataModel> models = new ArrayList<>();
        SQLiteDatabase database = this.getReadableDatabase();
        Cursor cursor = database.rawQuery("select * from " + MAIN_TABLE, null);
        cursor.moveToFirst();
        try {
            do {
                DataModel model = new DataModel();
                model.setData(cursor.getString(0));
                model.setDate(cursor.getString(1));
                models.add(model);
            } while (cursor.moveToNext());
            database.close();
            cursor.close();
            return models;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
