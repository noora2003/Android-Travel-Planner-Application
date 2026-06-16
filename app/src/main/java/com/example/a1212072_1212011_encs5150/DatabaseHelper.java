package com.example.a1212072_1212011_encs5150;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME    = "TravelPlanner.db";
    private static final int    DATABASE_VERSION = 2;

    // ── Users table ──────────────────────────────────────────────────────────
    public static final String TABLE_USERS       = "users";
    public static final String COL_USER_ID       = "id";          // PK for users
    public static final String COL_EMAIL         = "email";
    public static final String COL_FIRST_NAME    = "first_name";
    public static final String COL_LAST_NAME     = "last_name";
    public static final String COL_PASSWORD      = "password";    // stored as SHA-256 hash
    public static final String COL_GENDER        = "gender";
    public static final String COL_CATEGORY      = "category";
    public static final String COL_PHONE         = "phone";
    public static final String COL_PROFILE_PIC   = "profile_pic";

    // ── Trips table ──────────────────────────────────────────────────────────
    public static final String TABLE_TRIPS       = "trips";
    public static final String COL_TRIP_ID       = "trip_id";     // PK for trips
    public static final String COL_DESTINATION   = "destination";
    public static final String COL_COUNTRY       = "country";
    public static final String COL_DURATION      = "duration_days";
    public static final String COL_PRICE         = "price";
    public static final String COL_RATING        = "rating";
    public static final String COL_DESCRIPTION   = "description";
    public static final String COL_IMAGE         = "image";

    // ── Favorites table ───────────────────────────────────────────────────────
    public static final String TABLE_FAVORITES        = "favorites";

    // ── Reservations table ────────────────────────────────────────────────────
    public static final String TABLE_RESERVATIONS     = "reservations";
    public static final String COL_FAV_USER_ID        = "user_id";
    public static final String COL_FAV_TRIP_ID        = "trip_id";
    public static final String COL_RESERVATION_DATE   = "reservation_date";
    public static final String COL_QUANTITY           = "quantity";
    public static final String COL_RESERVATION_TYPE   = "reservation_type";
    public static final String COL_STATUS             = "status"; // "active","completed","cancelled"

    // ── CREATE statements ─────────────────────────────────────────────────────
    private static final String CREATE_USERS_TABLE =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_EMAIL       + " TEXT UNIQUE NOT NULL, " +
                    COL_FIRST_NAME  + " TEXT NOT NULL, " +
                    COL_LAST_NAME   + " TEXT NOT NULL, " +
                    COL_PASSWORD    + " TEXT NOT NULL, " +
                    COL_GENDER      + " TEXT, " +
                    COL_CATEGORY    + " TEXT, " +
                    COL_PHONE       + " TEXT, " +
                    COL_PROFILE_PIC + " TEXT" +
                    ");";

    private static final String CREATE_TRIPS_TABLE =
            "CREATE TABLE " + TABLE_TRIPS + " (" +
                    COL_TRIP_ID     + " INTEGER PRIMARY KEY, " +
                    COL_DESTINATION + " TEXT, " +
                    COL_COUNTRY     + " TEXT, " +
                    COL_DURATION    + " INTEGER, " +
                    COL_PRICE       + " REAL, " +
                    COL_RATING      + " REAL, " +
                    COL_DESCRIPTION + " TEXT, " +
                    COL_IMAGE       + " TEXT" +
                    ");";

    private static final String CREATE_FAVORITES_TABLE =
            "CREATE TABLE " + TABLE_FAVORITES + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_FAV_USER_ID + " INTEGER, " +
                    COL_FAV_TRIP_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COL_FAV_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                    "FOREIGN KEY(" + COL_FAV_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COL_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_RESERVATIONS_TABLE =
            "CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_FAV_USER_ID        + " INTEGER, " +
                    COL_FAV_TRIP_ID        + " INTEGER, " +
                    COL_RESERVATION_DATE   + " TEXT, " +
                    COL_QUANTITY           + " INTEGER, " +
                    COL_RESERVATION_TYPE   + " TEXT, " +
                    COL_STATUS             + " TEXT, " +
                    "FOREIGN KEY(" + COL_FAV_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + "), " +
                    "FOREIGN KEY(" + COL_FAV_TRIP_ID + ") REFERENCES " + TABLE_TRIPS + "(" + COL_TRIP_ID + ")" +
                    ");";

    // ── Constructor ───────────────────────────────────────────────────────────
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TRIPS_TABLE);
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_RESERVATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ── User methods ──────────────────────────────────────────────────────────

    /** Returns true if email + hashedPassword match a row in the DB */
    public boolean checkUser(String email, String hashedPassword) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + "=? AND " + COL_PASSWORD + "=?",
                new String[]{email, hashedPassword},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /** Returns true if an account with this email already exists */
    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_USER_ID},
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /** Insert a new user — password must already be hashed before calling this */
    public boolean insertUser(String email, String firstName, String lastName,
                              String hashedPassword, String gender,
                              String category, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_EMAIL,      email);
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME,  lastName);
        values.put(COL_PASSWORD,   hashedPassword);
        values.put(COL_GENDER,     gender);
        values.put(COL_CATEGORY,   category);
        values.put(COL_PHONE,      phone);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    /** Get a user's full info by email (for profile screen) */
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);
    }

    /** Update user profile fields */
    public boolean updateUser(String email, String firstName, String lastName, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME,  lastName);
        values.put(COL_PHONE,      phone);
        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    /** Update password only */
    public boolean updatePassword(String email, String newHashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newHashedPassword);
        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    // ── Trip methods ──────────────────────────────────────────────────────────

    public void insertTrip(int id, String destination, String country,
                           int duration, double price, double rating,
                           String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRIP_ID,     id);
        values.put(COL_DESTINATION, destination);
        values.put(COL_COUNTRY,     country);
        values.put(COL_DURATION,    duration);
        values.put(COL_PRICE,       price);
        values.put(COL_RATING,      rating);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE,       image);
        // CONFLICT_IGNORE: skip insert if trip already exists (re-pressing Connect)
        db.insertWithOnConflict(TABLE_TRIPS, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    /** Return all trips as a Cursor */
    public Cursor getAllTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRIPS, null, null, null, null, null, null);
    }

    // ── Favorites methods ─────────────────────────────────────────────────────

    /** Add a trip to a user's favorites. Silently ignores duplicates. */
    public boolean addFavorite(int userId, int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_USER_ID, userId);
        values.put(COL_FAV_TRIP_ID, tripId);
        long result = db.insertWithOnConflict(TABLE_FAVORITES, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    /** Remove a trip from a user's favorites */
    public boolean removeFavorite(int userId, int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_FAVORITES,
                COL_FAV_USER_ID + "=? AND " + COL_FAV_TRIP_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(tripId)});
        return rows > 0;
    }

    /** Check whether a trip is already in a user's favorites */
    public boolean isFavorite(int userId, int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, new String[]{"id"},
                COL_FAV_USER_ID + "=? AND " + COL_FAV_TRIP_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(tripId)},
                null, null, null);
        boolean fav = cursor.getCount() > 0;
        cursor.close();
        return fav;
    }

    /** Get all favorite trips for a user (joined with trips table) */
    public Cursor getFavoriteTrips(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.* FROM " + TABLE_TRIPS + " t " +
                "INNER JOIN " + TABLE_FAVORITES + " f ON t." + COL_TRIP_ID + " = f." + COL_FAV_TRIP_ID +
                " WHERE f." + COL_FAV_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    // ── Reservation methods ───────────────────────────────────────────────────

    /** Insert a new reservation */
    public boolean insertReservation(int userId, int tripId, String date,
                                     int quantity, String type, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_USER_ID,      userId);
        values.put(COL_FAV_TRIP_ID,      tripId);
        values.put(COL_RESERVATION_DATE, date);
        values.put(COL_QUANTITY,         quantity);
        values.put(COL_RESERVATION_TYPE, type);
        values.put(COL_STATUS,           status);
        long result = db.insert(TABLE_RESERVATIONS, null, values);
        return result != -1;
    }

    /** Get all reservations for a user (joined with trips for trip name) */
    public Cursor getUserReservations(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT r.*, t." + COL_DESTINATION + " FROM " + TABLE_RESERVATIONS + " r " +
                "INNER JOIN " + TABLE_TRIPS + " t ON r." + COL_FAV_TRIP_ID + " = t." + COL_TRIP_ID +
                " WHERE r." + COL_FAV_USER_ID + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(userId)});
    }

    /** Get the integer user ID for a given email */
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_EMAIL + "=?", new String[]{email},
                null, null, null);
        int id = -1;
        if (cursor != null && cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
            cursor.close();
        }
        return id;
    }
}