package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList; // Import ArrayList
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_db";
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "users";
    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_PASSWORD = "password";
    private static final String COL_NAME = "name";
    private static final String COL_AGE_RANGE = "age_range";
    private static final String COL_HEIGHT = "height";
    private static final String COL_WEIGHT = "weight";
    private static final String COL_DIET_PREF = "diet_preference";
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "task_id";
    private static final String COL_TASK_USER_EMAIL = "user_email";
    private static final String COL_TASK_NAME = "task_name";
    private static final String COL_TASK_DATE = "task_date";
    private static final String COL_TASK_START_TIME = "start_time";
    private static final String COL_TASK_END_TIME = "end_time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + "("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_EMAIL + " TEXT UNIQUE, "
                + COL_PASSWORD + " TEXT, "
                + COL_NAME + " TEXT, "
                + COL_AGE_RANGE + " TEXT, "
                + COL_HEIGHT + " TEXT, "
                + COL_WEIGHT + " TEXT, "
                + COL_DIET_PREF + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + "("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_USER_EMAIL + " TEXT, "
                + COL_TASK_NAME + " TEXT, "
                + COL_TASK_DATE + " TEXT, "
                + COL_TASK_START_TIME + " TEXT, "
                + COL_TASK_END_TIME + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }
    public boolean insertTask(String userEmail, String taskName, String date, String startTime, String endTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_USER_EMAIL, userEmail);
        values.put(COL_TASK_NAME, taskName);
        values.put(COL_TASK_DATE, date);
        values.put(COL_TASK_START_TIME, startTime);
        values.put(COL_TASK_END_TIME, endTime);

        long result = db.insert(TABLE_TASKS, null, values);
        return result != -1;
    }
    // --- GANTI FUNGSI INI ---
    public List<Task> getTasksForUser(String userEmail) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE "
                + COL_TASK_USER_EMAIL + "=? ORDER BY " + COL_TASK_DATE + " ASC, "
                + COL_TASK_START_TIME + " ASC", new String[]{userEmail});

        if (cursor.moveToFirst()) {
            do {
                // --- AMBIL ID DARI DB ---
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COL_TASK_ID));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(COL_TASK_NAME));
                @SuppressLint("Range") String date = cursor.getString(cursor.getColumnIndex(COL_TASK_DATE));
                @SuppressLint("Range") String startTime = cursor.getString(cursor.getColumnIndex(COL_TASK_START_TIME));
                @SuppressLint("Range") String endTime = cursor.getString(cursor.getColumnIndex(COL_TASK_END_TIME));
                // --- MASUKKAN ID KE CONSTRUCTOR ---
                taskList.add(new Task(id, taskName, date, startTime, endTime));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    // --- GANTI FUNGSI INI ---
    public List<Task> getTasksForUserByDate(String userEmail, String date) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE "
                + COL_TASK_USER_EMAIL + "=? AND "
                + COL_TASK_DATE + "=? ORDER BY "
                + COL_TASK_START_TIME + " ASC", new String[]{userEmail, date});

        if (cursor.moveToFirst()) {
            do {
                // --- AMBIL ID DARI DB ---
                @SuppressLint("Range") long id = cursor.getLong(cursor.getColumnIndex(COL_TASK_ID));
                @SuppressLint("Range") String taskName = cursor.getString(cursor.getColumnIndex(COL_TASK_NAME));
                @SuppressLint("Range") String taskDate = cursor.getString(cursor.getColumnIndex(COL_TASK_DATE));
                @SuppressLint("Range") String startTime = cursor.getString(cursor.getColumnIndex(COL_TASK_START_TIME));
                @SuppressLint("Range") String endTime = cursor.getString(cursor.getColumnIndex(COL_TASK_END_TIME));
                // --- MASUKKAN ID KE CONSTRUCTOR ---
                taskList.add(new Task(id, taskName, taskDate, startTime, endTime));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public boolean deleteTaskById(long taskId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Hapus berdasarkan ID
        int result = db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});

        return result > 0; // true jika ada baris yang terhapus
    }
    public UserProfile getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=?", new String[]{email});

        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COL_NAME));
            @SuppressLint("Range") String age = cursor.getString(cursor.getColumnIndex(COL_AGE_RANGE));
            @SuppressLint("Range") String height = cursor.getString(cursor.getColumnIndex(COL_HEIGHT));
            @SuppressLint("Range") String weight = cursor.getString(cursor.getColumnIndex(COL_WEIGHT));
            @SuppressLint("Range") String diet = cursor.getString(cursor.getColumnIndex(COL_DIET_PREF));

            cursor.close();
            return new UserProfile(name, age, height, weight, diet);
        }
        cursor.close();
        return null; // Jika user tidak ditemukan
    }
    public boolean insertUser(String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);

        long result = db.insert(TABLE_NAME, null, values);
        return result != -1; // true jika berhasil
    }

    public boolean updateOnboardingData(String email, String name, String ageRange, String height, String weight, String dietPref) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_AGE_RANGE, ageRange);
        values.put(COL_HEIGHT, height);
        values.put(COL_WEIGHT, weight);
        values.put(COL_DIET_PREF, dietPref);

        // Update data berdasarkan email
        int rowsAffected = db.update(TABLE_NAME, values, COL_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0; // true jika berhasil update
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, password});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COL_EMAIL + "=?",
                new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }
}
