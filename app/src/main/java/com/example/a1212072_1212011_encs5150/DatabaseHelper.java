package com.example.a1212072_1212011_encs5150;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME    = "TravelPlanner.db";
    private static final int    DATABASE_VERSION = 3;

    // Users table
    public static final String TABLE_USERS       = "users";
    public static final String COL_USER_ID       = "id";
    public static final String COL_EMAIL         = "email";
    public static final String COL_FIRST_NAME    = "first_name";
    public static final String COL_LAST_NAME     = "last_name";
    public static final String COL_PASSWORD      = "password";   // stored as SHA-256 hash
    public static final String COL_GENDER        = "gender";
    public static final String COL_CATEGORY      = "category";
    public static final String COL_PHONE         = "phone";
    public static final String COL_PROFILE_PIC   = "profile_pic";

    // Trips table
    public static final String TABLE_TRIPS      = "trips";
    public static final String COL_TRIP_ID      = "id";
    public static final String COL_DESTINATION  = "destination";
    public static final String COL_COUNTRY      = "country";
    public static final String COL_DURATION     = "duration_days";
    public static final String COL_PRICE        = "price";
    public static final String COL_RATING       = "rating";
    public static final String COL_DESCRIPTION  = "description";
    public static final String COL_IMAGE        = "image";

    // Favorites table
    public static final String TABLE_FAVORITES  = "favorites";
    public static final String COL_FAV_ID       = "id";
    public static final String COL_FAV_EMAIL    = "user_email";
    public static final String COL_FAV_TRIP_ID  = "trip_id";
    //  Reservations table
    public static final String TABLE_RESERVATIONS    = "reservations";
    public static final String COL_RES_ID            = "id";
    public static final String COL_RES_EMAIL         = "user_email";
    public static final String COL_RES_TRIP_ID       = "trip_id";
    public static final String COL_RES_TRIP_NAME     = "trip_name";
    public static final String COL_RES_QUANTITY      = "quantity";
    public static final String COL_RES_TYPE          = "reservation_type";
    public static final String COL_RES_DATE          = "reservation_date";
    public static final String COL_RES_TOTAL_PRICE   = "total_price";
    public static final String COL_RES_STATUS        = "status";


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
                    COL_FAV_ID     + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_FAV_EMAIL  + " TEXT, " +
                    COL_FAV_TRIP_ID+ " INTEGER, " +
                    "UNIQUE(" + COL_FAV_EMAIL + ", " + COL_FAV_TRIP_ID + ")" +
                    ");";

    private static final String CREATE_RESERVATIONS_TABLE =
            "CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                    COL_RES_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_RES_EMAIL       + " TEXT, " +
                    COL_RES_TRIP_ID     + " INTEGER, " +
                    COL_RES_TRIP_NAME   + " TEXT, " +
                    COL_RES_QUANTITY    + " INTEGER, " +
                    COL_RES_TYPE        + " TEXT, " +
                    COL_RES_DATE        + " TEXT, " +
                    COL_RES_TOTAL_PRICE + " REAL, " +
                    COL_RES_STATUS      + " TEXT" +
                    ");";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_TRIPS_TABLE); // add this line
        db.execSQL(CREATE_FAVORITES_TABLE);
        db.execSQL(CREATE_RESERVATIONS_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);

    }

    //  User methods
    // Returns true if email+hashedPassword match a row in the DB
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

    // Returns true if an account with this email already exists
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

    // Insert a new user — password must already be hashed before calling this
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

    // Get a user's full info by email (for profile screen)
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_USERS,
                null,
                COL_EMAIL + "=?",
                new String[]{email},
                null, null, null);
    }

    // Update user profile fields
    public boolean updateUser(String email, String firstName, String lastName, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRST_NAME, firstName);
        values.put(COL_LAST_NAME,  lastName);
        values.put(COL_PHONE,      phone);

        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    // Update password only
    public boolean updatePassword(String email, String newHashedPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newHashedPassword);

        int rows = db.update(TABLE_USERS, values, COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public void insertTrip(int id, String destination, String country,
                           int duration, double price, double rating,
                           String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRIP_ID,    id);
        values.put(COL_DESTINATION,destination);
        values.put(COL_COUNTRY,    country);
        values.put(COL_DURATION,   duration);
        values.put(COL_PRICE,      price);
        values.put(COL_RATING,     rating);
        values.put(COL_DESCRIPTION,description);
        values.put(COL_IMAGE,      image);
        db.insertWithOnConflict(TABLE_TRIPS, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getAllTrips() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_TRIPS, null, null,
                null, null, null, null);
    }

    //  FAVORITES METHODS

    public void addFavorite(String email, int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAV_EMAIL,   email);
        values.put(COL_FAV_TRIP_ID, tripId);
        db.insertWithOnConflict(TABLE_FAVORITES, null, values,
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void removeFavorite(String email, int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FAVORITES,
                COL_FAV_EMAIL + "=? AND " + COL_FAV_TRIP_ID + "=?",
                new String[]{email, String.valueOf(tripId)});
    }

    public boolean isFavorite(String email, int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_FAVORITES, null,
                COL_FAV_EMAIL + "=? AND " + COL_FAV_TRIP_ID + "=?",
                new String[]{email, String.valueOf(tripId)},
                null, null, null);
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public Cursor getFavoriteTrips(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT t.* FROM " + TABLE_TRIPS + " t " +
                "INNER JOIN " + TABLE_FAVORITES + " f " +
                "ON t." + COL_TRIP_ID + " = f." + COL_FAV_TRIP_ID +
                " WHERE f." + COL_FAV_EMAIL + " = ?";
        return db.rawQuery(query, new String[]{email});
    }

    //  RESERVATIONS METHODS

    public boolean insertReservation(String email, int tripId, String tripName,
                                     int quantity, String type, String date,
                                     double totalPrice, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_RES_EMAIL,      email);
        values.put(COL_RES_TRIP_ID,    tripId);
        values.put(COL_RES_TRIP_NAME,  tripName);
        values.put(COL_RES_QUANTITY,   quantity);
        values.put(COL_RES_TYPE,       type);
        values.put(COL_RES_DATE,       date);
        values.put(COL_RES_TOTAL_PRICE,totalPrice);
        values.put(COL_RES_STATUS,     status);
        long result = db.insert(TABLE_RESERVATIONS, null, values);
        return result != -1;
    }

    public Cursor getUserReservations(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RESERVATIONS, null,
                COL_RES_EMAIL + "=?",
                new String[]{email},
                null, null, COL_RES_DATE + " DESC");
    }

    public Cursor getAllReservations() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_RESERVATIONS, null,
                null, null, null, null,
                COL_RES_DATE + " DESC");
    }


    //  ADMIN METHODS

    public Cursor getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_USERS, null, null,
                null, null, null, null);
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_USERS,
                COL_EMAIL + "=?", new String[]{email});
        return rows > 0;
    }

    public boolean deleteTrip(int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_TRIPS,
                COL_TRIP_ID + "=?", new String[]{String.valueOf(tripId)});
        return rows > 0;
    }

    public boolean updateTrip(int id, String destination, String country,
                              int duration, double price, double rating,
                              String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_DESTINATION, destination);
        values.put(COL_COUNTRY,     country);
        values.put(COL_DURATION,    duration);
        values.put(COL_PRICE,       price);
        values.put(COL_RATING,      rating);
        values.put(COL_DESCRIPTION, description);
        values.put(COL_IMAGE,       image);
        int rows = db.update(TABLE_TRIPS, values,
                COL_TRIP_ID + "=?", new String[]{String.valueOf(id)});
        return rows > 0;
    }


}
