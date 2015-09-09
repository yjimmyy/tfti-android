package xyz.tfti.app;

import android.provider.BaseColumns;

/**
 * Created by jimmy on 8/11/2015.
 */
public class Contract {
   public static final int DATABASE_VERSION = 8;
   public static final String DATABASE_NAME = "db";
   private static final String TEXT_TYPE = " TEXT";
   private static final String INT_TYPE = " INTEGER";
   private static final String REAL_TYPE = " REAL";
   private static final String COMMA_SEP = ", ";

   // to prevent accidental instantiation of contract class
   protected Contract() {}

   public static abstract class Spots implements BaseColumns {
      public static final String TABLE_NAME = "spots";
      public static final String COLUMN_NAME_NAME = "name";
      public static final String COLUMN_NAME_NUM_USERS = "numUsers";
      public static final String COLUMN_NAME_LAT = "lat";
      public static final String COLUMN_NAME_LNG = "lng";
      public static final String COLUMN_NAME_RADIUS = "radius";

      public static final String CREATE_TABLE = "CREATE TABLE if not exists " + TABLE_NAME + " (" +
         _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
         COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
         COLUMN_NAME_NUM_USERS + INT_TYPE + COMMA_SEP +
         COLUMN_NAME_LAT + REAL_TYPE + COMMA_SEP +
         COLUMN_NAME_LNG + REAL_TYPE + COMMA_SEP +
         COLUMN_NAME_RADIUS + INT_TYPE + ");";

      public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   }

   public static abstract class Users implements BaseColumns {
      public static final String TABLE_NAME = "users";
      public static final String COLUMN_NAME_NAME = "name";
      public static final String COLUMN_NAME_FB_ID = "fbId";

      public static final String CREATE_TABLE = "CREATE TABLE if not exists " + TABLE_NAME + " (" +
         _ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
         COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
         COLUMN_NAME_FB_ID + INT_TYPE + ");";

      public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   }

   public static abstract class SpotsUsers implements BaseColumns {
      public static final String TABLE_NAME = "spots_users";
      public static final String COLUMN_NAME_SPOT_ID = "spotId";
      public static final String COLUMN_NAME_USER_ID = "userId";

      public static final String CREATE_TABLE = "CREATE TABLE if not exists " + TABLE_NAME + " (" +
         _ID + " INTEGER PRIMARY KEY," +
         COLUMN_NAME_SPOT_ID + INT_TYPE + COMMA_SEP +
         COLUMN_NAME_USER_ID + INT_TYPE + ");";

      public static final String DELETE_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;
   }
}
