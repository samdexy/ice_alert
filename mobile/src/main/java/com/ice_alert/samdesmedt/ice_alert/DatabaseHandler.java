package com.ice_alert.samdesmedt.ice_alert;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "contactManager",

    TABLE_CONTACTS = "contacts",
    KEY_ID = "id",
    KEY_NAME = "name",
    KEY_PHONE = "phone",
    KEY_RANKING = "ranking",

    TABLE_SETTINGS = "settings",
    KEY_ID_SETTINGS = "id",
    KEY_AVAILABLE = "available",
    KEY_UNAVAILABLE = "unavailable",
    KEY_MESSAGE = "message";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT," + KEY_PHONE + " TEXT," + KEY_RANKING + " INT)");
        db.execSQL("CREATE TABLE " + TABLE_SETTINGS + "(" + KEY_ID_SETTINGS + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_AVAILABLE + " TEXT," + KEY_UNAVAILABLE + " TEXT," + KEY_MESSAGE + " TEXT)");
        db.execSQL("INSERT INTO " + TABLE_SETTINGS + " ("
                + KEY_ID_SETTINGS + ", " + KEY_AVAILABLE + ", "
                + KEY_UNAVAILABLE + ", " + KEY_MESSAGE + ") VALUES ('1', 'yes', 'no', 'I need help. Please answer \"yes\" if you are available, or \"no\" if you are unavailable')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);

        onCreate(db);
    }

    public void createContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_RANKING, contact.getRanking());


        db.insert(TABLE_CONTACTS, null, values);
        db.close();
    }

    public Contact getContact(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME, KEY_PHONE, KEY_RANKING }, KEY_ID + "=?", new String[] { String.valueOf(id) }, null, null, null, null );

        if (cursor != null)
            cursor.moveToFirst();

        Contact contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
        db.close();
        cursor.close();
        return contact;
    }

    public Settings getSettings(int id) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(TABLE_SETTINGS, new String[] { KEY_ID_SETTINGS, KEY_AVAILABLE, KEY_UNAVAILABLE, KEY_MESSAGE }, KEY_ID_SETTINGS + "=?", new String[] { String.valueOf(id) }, null, null, null, null );

        if (cursor != null)
            cursor.moveToFirst();

        Settings setting = new Settings(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        db.close();
        cursor.close();
        return setting;
    }

    public void deleteContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_CONTACTS, KEY_ID + "=?", new String[] { String.valueOf(contact.getId()) });
        db.close();
    }

    public int getContactsCount() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);
        int count = cursor.getCount();
        db.close();
        cursor.close();

        return count;
    }

    public Contact getContactRank(int rank) {
        SQLiteDatabase db = getReadableDatabase();

        Contact contact = null;
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME, KEY_PHONE, KEY_RANKING }, KEY_RANKING + "=?", new String[] { String.valueOf(rank) }, null, null, null, null );

        if (cursor.moveToFirst()){
            contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
            db.close();
            cursor.close();

        }

        return contact;
    }

    public Contact getContactPhone(String phone) {
        SQLiteDatabase db = getReadableDatabase();

        Contact contact = null;
        Cursor cursor = db.query(TABLE_CONTACTS, new String[] { KEY_ID, KEY_NAME, KEY_PHONE, KEY_RANKING }, KEY_PHONE + "=?", new String[] { String.valueOf(phone) }, null, null, null, null );

        if (cursor.moveToFirst()){
            contact = new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3));
            db.close();
            cursor.close();

        }

        return contact;
    }


    public int updateContact(Contact contact) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_NAME, contact.getName());
        values.put(KEY_PHONE, contact.getPhone());
        values.put(KEY_RANKING, contact.getRanking());


        int rowsAffected = db.update(TABLE_CONTACTS, values, KEY_ID + "=?", new String[] { String.valueOf(contact.getId()) });
        db.close();

        return rowsAffected;
    }

    public int updateSetting(Settings settings) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(KEY_AVAILABLE, settings.getAnswerAvailable());
        values.put(KEY_UNAVAILABLE, settings.getAnswerUnavailable());
        values.put(KEY_MESSAGE, settings.getMessage());


        int rowsAffected = db.update(TABLE_SETTINGS, values, KEY_ID_SETTINGS + "=?", new String[] { String.valueOf(settings.getId()) });
        db.close();

        return rowsAffected;
    }

    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<Contact>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS, null);

        if (cursor.moveToFirst()) {
            do {
                contacts.add(new Contact(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getInt(3)));
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contacts;
    }
}
