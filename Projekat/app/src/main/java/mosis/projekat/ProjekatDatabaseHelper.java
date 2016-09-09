package mosis.projekat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Neca on 7.6.2016..
 */
public class ProjekatDatabaseHelper extends SQLiteOpenHelper {

    // SQL naredba za kreiranje nove tabele
    private static final String DATABASE_CREATE = "create table "
            + ProjekatDBAdapter.DATABASE_TABLE + " ("
            + ProjekatDBAdapter.USER_ID + " integer primary key autoincrement, "
            + ProjekatDBAdapter.USERNAME + " text unique not null, "
            + ProjekatDBAdapter.PASSWORD + " text, "
            + ProjekatDBAdapter.NAME + " text, "
            + ProjekatDBAdapter.LASTNAME + " text, "
            + ProjekatDBAdapter.PHONE_NUMBER + " text, "
            + ProjekatDBAdapter.IMAGE + " text, "
            + ProjekatDBAdapter.TEAM_NAME + " text, "
            + ProjekatDBAdapter.CREATED + " text);";

    private static final String DATABASE_CREATE2 = "create table "
            + ProjekatDBAdapter.DATABASE_TABLE2  + " ("
            + ProjekatDBAdapter.QID + " integer primary key autoincrement, "
            + ProjekatDBAdapter.QUEST_ID + " text, "
            + ProjekatDBAdapter.QUEST_QUESTIONS +" text, "
            + ProjekatDBAdapter.QUEST_CORRECT_ANSWER +" text, "
            + ProjekatDBAdapter.QUEST_WRONG_ANSWER1 +" text, "
            + ProjekatDBAdapter.QUEST_WRONG_ANSWER2 +" text, "
            + ProjekatDBAdapter.QUEST_WRONG_ANSWER3 +" text);"
            ;

    public ProjekatDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE2);
        } catch (SQLiteException ec){
            Log.v("ProjekatDatabaseHelper", ec.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE2);
        onCreate(db);
    }

    public void deleteDatabase(SQLiteDatabase db)
    {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE2);
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE2);
    } catch (SQLiteException ec){
        Log.v("ProjekatDatabaseHelper", ec.getMessage());
    }
    }

    public void deleteUserDatabase(SQLiteDatabase db)
    {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE);
            db.execSQL(DATABASE_CREATE);
        } catch (SQLiteException ec){
            Log.v("ProjekatDatabaseHelper", ec.getMessage());
        }
    }

    public void dropCreateQuestions(SQLiteDatabase db)
    {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + ProjekatDBAdapter.DATABASE_TABLE2);
            db.execSQL(DATABASE_CREATE2);
        } catch (SQLiteException ec){
            Log.v("ProjekatDatabaseHelper", ec.getMessage());
        }
    }

}
