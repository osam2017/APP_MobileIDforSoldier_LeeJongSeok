package com.example.administrator.mobileidforsoldier;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 출입증 데이터를 저장하고 관리하기 위한 헬퍼 클래스
 */

public class Dao extends SQLiteOpenHelper{
    private static final String dbName = "OSAM";
    private static final String tableName = "LeeJongSeok_idcard";

    // Error Log 출력에 사용할 Tag 선언
    private static final String ERROR_TAG = "DB Error";

    public Dao(Context context) {
        super(context, dbName, null, 1);
    }

    public static String getTableName() {   // tableName 변수의 getter
        return tableName;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + tableName + "("
                + "_id integer PRIMARY KEY autoincrement, "
                + " servicenumber text UNIQUE not null, "
                + " rank text not null, "
                + " name text not null, "
                + " date text not null);";

        try {   // 데이터베이스 생성
            db.execSQL(CREATE_SQL);
            Log.i("DB Created", "Database created.");
        } catch(Exception e) {
            Log.e(ERROR_TAG, "Exception in CREATE_SQL", e);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        Log.i("DB Open", "Database opened...");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w("DB Upgrade", "Upgrading database...");
    }

    public void insertRecord(SQLiteDatabase db, String number, String rank, String name, String date) {        // 데이터 추가를 위한 메소드
        try {        // 기존의 출입증 정보가 있으면 제거
            String DROP_OLD_SQL = "DELETE FROM " + tableName + " WHERE servicenumber IN (SELECT servicenumber FROM " + tableName + " ORDER BY servicenumber ASC LIMIT 1);";
            db.execSQL(DROP_OLD_SQL);
        } catch(Exception e) {
            Log.e(ERROR_TAG, "Exception in DROP_OLD", e);
        }

        try {       // 출입증 데이터 추가
            String INSERT_SQL = "INSERT INTO " + tableName + " (servicenumber, rank, name, date) VALUES (" + "'" + number +  "', '" + rank + "', '" + name + "', '" + date + "');";
            db.execSQL(INSERT_SQL);
            Log.i("DB Insert", "Data Inserted.");
        } catch(Exception e) {
            Log.e(ERROR_TAG, "Exception in DB_INSERT", e);
        }
    }
}