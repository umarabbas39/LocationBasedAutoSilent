package com.jisolution.autosilence.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class DBHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "geofenceddbassee";
    private static final String TABLE_Users = "geofencedetails";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_RADIUS = "radius";
    private static final String KEY_TIME = "time";
    private static final String Key_Lat="lat";
    private static final String Key_Lng="lng";
    public static final String  Key_Date="date";

    public DBHelper(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        String CREATE_TABLE = "CREATE TABLE " + TABLE_Users + "("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_NAME + " TEXT,"
                + KEY_RADIUS + " TEXT,"
                + KEY_TIME + " TEXT,"
                + Key_Date + " TEXT,"
                + Key_Lat + " TEXT,"
                + Key_Lng + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_Users);
        // Create tables again
        onCreate(db);
    }
    // **** CRUD (Create, Read, Update, Delete) Operations ***** //

    // Adding new User Details
    public void insertUserDetails(String name, String radius, String time,String date,String lat,String lng){
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_NAME, name);
        cValues.put(KEY_RADIUS, radius);
        cValues.put(KEY_TIME, time);
        cValues.put(Key_Date, date);
        cValues.put(Key_Lat, lat);
        cValues.put(Key_Lng, lng);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_Users,null, cValues);
        db.close();
    }
    // Get User Details
    public ArrayList<HashMap<String, String>> GetUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT * FROM "+ TABLE_Users;
        Cursor cursor = db.rawQuery(query,null);
        while (cursor.moveToNext()){
            HashMap<String,String> user = new HashMap<>();
            user.put("name",cursor.getString(cursor.getColumnIndex(KEY_NAME)));
            user.put("time",cursor.getString(cursor.getColumnIndex(KEY_TIME)));
            user.put("date",cursor.getString(cursor.getColumnIndex(Key_Date)));
            user.put("radius",cursor.getString(cursor.getColumnIndex(KEY_RADIUS)));
            user.put("lat",cursor.getString(cursor.getColumnIndex(Key_Lat)));
            user.put("lng",cursor.getString(cursor.getColumnIndex(Key_Lng)));
            user.put("id",cursor.getString(cursor.getColumnIndex(KEY_ID)));
            userList.add(user);
        }
        db.close();
        return  userList;
    }
    // Get User Details based on userid
    public ArrayList<HashMap<String, String>> GetUserByUserId(String userid){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<HashMap<String, String>> userList = new ArrayList<>();
        String query = "SELECT name, radius, time FROM "+ TABLE_Users;
//        Cursor cursor = db.query(TABLE_Users, new String[]{Key_Lat, Key_Lng}, KEY_NAME+ " = ?", new String[]{userid.trim()},null, null, null, null);
        Cursor cursor = db.rawQuery("SELECT * FROM " +  TABLE_Users + " WHERE name=" + "name", null);
        if (cursor.moveToFirst()){
            do{
                String name = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                Log.d("Dialog", "name = " + name);
                if(userid.equals(name)){
                    HashMap<String,String> user = new HashMap<>();
                    user.put("lat",cursor.getString(cursor.getColumnIndex(Key_Lat)));
                    user.put("lng",cursor.getString(cursor.getColumnIndex(Key_Lng)));
                    userList.add(user);
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        Log.d("Dialog", "userList =  " + userList + "userId = " + userid + " " + cursor.getCount());
        return  userList;
    }
    // Delete User Details
    public void DeleteUser(String userid){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete(TABLE_Users, KEY_NAME + " = ?", new String[]{userid});
        db.close();
    }
    // Update User Details
    public int UpdateUserDetails(String name, String time, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVals = new ContentValues();
        cVals.put(KEY_NAME, name);
        cVals.put(KEY_TIME, time);
        int count = db.update(TABLE_Users, cVals, KEY_NAME+" = ?",new String[]{id});
        db.close();
        return  count;
    }
    public int UpdateDetails(String name, String radius, String id){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cVals = new ContentValues();
        cVals.put(KEY_NAME, name);
        cVals.put(KEY_RADIUS, radius);
        int count = db.update(TABLE_Users, cVals, KEY_NAME+" = ?",new String[]{id});
        db.close();
        return  count;
    }
    public int getUserId(String name){
        SQLiteDatabase db = this.getWritableDatabase();
        String id = "";
        Cursor cursor = db.rawQuery("SELECT * FROM " +  TABLE_Users + " WHERE name=" + "name", null);
        if (cursor.moveToFirst()){
            do{
                String nam = cursor.getString(cursor.getColumnIndex(KEY_NAME));
                Log.d("Dialog", "name = " + name);
                if(name.equals(nam)){
                    id = cursor.getString(cursor.getColumnIndex(KEY_ID));
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return Integer.parseInt(id);
    }

}
