package mosis.projekat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Neca on 7.6.2016..
 */
public class ProjekatDBAdapter {
    public static final String DATABASE_NAME = "FriendsDb";
    public static final String DATABASE_TABLE = "Friends";
    public static final int DATABASE_VERSION = 1;

    public static final String USER_ID = "ID";
    public static final String USERNAME = "Username";
    public static final String PASSWORD = "Password";
    public static final String NAME = "Name";
    public static final String LASTNAME = "Lastname";
    public static final String PHONE_NUMBER = "PhoneNumber";
    public static final String IMAGE = "Image";
    public static final String CREATED = "Created";

    private SQLiteDatabase db;

    private final Context context;
    private ProjekatDatabaseHelper dbHelper;

    public ProjekatDBAdapter(Context cont){
        context = cont;
        dbHelper = new ProjekatDatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Otvaranje konekcije
    public ProjekatDBAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
        return this;
    }

    // Zatvaranje konekcije
    public void close() {
        db.close();
    }

    // Dodavanje podatka u bazu
    public long insertEntry(User user) {
        // dbHelper.onCreate(db);
        ContentValues contentValues = new ContentValues();

        contentValues.put(USERNAME, user.getUsername());
        contentValues.put(PASSWORD, user.getPassword());
        contentValues.put(NAME, user.getName());
        contentValues.put(LASTNAME, user.getLastname());
        contentValues.put(PHONE_NUMBER, user.getPhoneNumber());
        //contentValues.put(IMAGE, getStringImage(user.getImage())); // TREBA ISPRAVITI OVO
        contentValues.put(IMAGE, user.getImage());
        contentValues.put(CREATED, user.getCreated());
        long id = -1;
        db.beginTransaction();
        try {
            id = db.insert(DATABASE_TABLE, null, contentValues);
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.w("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }
        return id;
    }

    // Brisanje podatka iz baze
    public boolean removeEntry(String username){
        boolean success = false;
        db.beginTransaction();
        try {
            success = db.delete(DATABASE_TABLE, USERNAME + "='" + username +"'", null) > 0;
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.w("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    public boolean removeEntries(String usernames){
        boolean success = false;
        String[] username = usernames.split(",");
        String query = "";
        for (int i=0; i<username.length; i++)
        {
            if (i < username.length - 1)
                query += USERNAME + "='" + username[i] + "'" + " OR ";
            else
                query += USERNAME + "='" + username[i] + "'";
        }
        db.beginTransaction();
        try {
            success = db.delete(DATABASE_TABLE,query, null) > 0;
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.w("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }
        return success;
    }

    // Vracanje svih podataka iz baze
    public ArrayList<User> getAllEntries() {
        ArrayList<User> users = null;
        Cursor cursor = null;
        db.beginTransaction();
        try {
            cursor = db.query(DATABASE_TABLE, null, null, null, null, null, null);
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.v("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }

        if (cursor != null)
        {
            users = new ArrayList<User>();
            User user = null;
            while (cursor.moveToNext()){
                user = new User(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.USERNAME)));
                user.setID(cursor.getLong(cursor.getColumnIndex(ProjekatDBAdapter.USER_ID)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.PASSWORD)));
                user.setName(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.NAME)));
                user.setLastName(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.LASTNAME)));
                user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.PHONE_NUMBER)));
                //user.setImage(StringToBitMap(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.IMAGE))));
                user.setImage(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.IMAGE)));
                user.setCreated(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.CREATED)));
                users.add(user);
            }
        }
        return users;
    }

    // Vracanje svih Username-a i kada su kreirani iz baze
    public ArrayList<User> getAllEntriesUsernameCreated() {
        ArrayList<User> users = null;
        Cursor cursor = null;
        db.beginTransaction();
        try {
            String[] columnsToReturn ={USERNAME, /*USER_ID,*/ CREATED};
            cursor = db.query(DATABASE_TABLE, columnsToReturn, null, null, null, null, null);
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.v("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }

        if (cursor != null)
        {
            users = new ArrayList<User>();
            User user = null;
            while (cursor.moveToNext()){
                user = new User(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.USERNAME)));
                //user.setID(cursor.getLong(cursor.getColumnIndex(ProjekatDBAdapter.USER_ID)));
                user.setCreated(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.CREATED)));
                users.add(user);
            }
        }
        return users;
    }

    // Vracanje 1 podatka iz baze
    public User getEntry(String username) {
        User user = null;
        Cursor cursor = null;
        db.beginTransaction();
        try {
            cursor = db.query(DATABASE_TABLE, null, USERNAME + "='" + username + "'", null, null, null, null);
            db.setTransactionSuccessful();
        } catch (SQLiteException ec) {
            Log.v("ProjekatDBAdapter", ec.getMessage());
        } finally {
            db.endTransaction();
        }

        if (cursor != null)
        {
            if (cursor.moveToFirst()) {
                user = new User(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.USERNAME)));
                user.setID(cursor.getLong(cursor.getColumnIndex(ProjekatDBAdapter.USER_ID)));
                user.setPassword(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.PASSWORD)));
                user.setName(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.NAME)));
                user.setLastName(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.LASTNAME)));
                user.setPhoneNumber(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.PHONE_NUMBER)));
                //user.setImage(StringToBitMap(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.IMAGE))));
                user.setImage(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.IMAGE)));
                user.setCreated(cursor.getString(cursor.getColumnIndex(ProjekatDBAdapter.CREATED)));
            }
        }
        return user;
    }

    // Izmena 1 podatka u bazi
    public int updateEntry(User user){
        String where = USERNAME + "='" + user.getUsername() + "'";

        ContentValues contentValues = new ContentValues();

        contentValues.put(USERNAME, user.getUsername());
        contentValues.put(PASSWORD, user.getPassword());
        contentValues.put(NAME, user.getName());
        contentValues.put(LASTNAME, user.getLastname());
        contentValues.put(PHONE_NUMBER, user.getPhoneNumber());
        //contentValues.put(IMAGE, getStringImage(user.getImage())); // TREBA ISPRAVITI OVO
        contentValues.put(IMAGE, user.getImage());
        contentValues.put(CREATED, user.getCreated());

        return db.update(DATABASE_TABLE, contentValues, where, null);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0,
                    encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

}
