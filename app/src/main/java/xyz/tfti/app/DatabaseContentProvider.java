package xyz.tfti.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by jimmy on 8/11/2015.
 */
public class DatabaseContentProvider extends ContentProvider {
   private static final int SPOTS = 10;
   private static final int SPOTS_ID = 11;
   private static final int USERS = 20;
   private static final int USERS_ID = 21;
   private static final int SPOTS_USERS = 30;
   private static final int SPOTS_USERS_ID = 31;
   private static final int USERS_AT_SPOT = 40;

   private static final String AUTHORITY = "xyz.tfti.app.provider";
   private static final String URI_DIR = "vnd.android.cursor.dir/vnd." + AUTHORITY + ".";
   private static final String URI_ITEM = "vnd.android.cursor.item/vnd." + AUTHORITY + ".";

   private static final String PATH_USERS_AT_SPOT = "usersAtSpot";

   public static final Uri SPOTS_URI = Uri.parse("content://" + AUTHORITY + "/" + Contract.Spots.TABLE_NAME);
   public static final Uri USERS_URI = Uri.parse("content://" + AUTHORITY + "/" + Contract.Users.TABLE_NAME);
   public static final Uri SPOTS_USERS_URI = Uri.parse("content://" + AUTHORITY + "/" + Contract.SpotsUsers.TABLE_NAME);
   public static final Uri USERS_AT_SPOT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH_USERS_AT_SPOT);

   private DatabaseHelper dbHelper;

   // TODO add rest of database
   private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
   static {
      uriMatcher.addURI(AUTHORITY, Contract.Spots.TABLE_NAME, SPOTS);
      uriMatcher.addURI(AUTHORITY, Contract.Spots.TABLE_NAME + "/#", SPOTS_ID);
      uriMatcher.addURI(AUTHORITY, Contract.Users.TABLE_NAME, USERS);
      uriMatcher.addURI(AUTHORITY, Contract.Users.TABLE_NAME + "/#", USERS_ID);
      uriMatcher.addURI(AUTHORITY, Contract.SpotsUsers.TABLE_NAME, SPOTS_USERS);
      uriMatcher.addURI(AUTHORITY, Contract.SpotsUsers.TABLE_NAME, SPOTS_USERS_ID);
      uriMatcher.addURI(AUTHORITY, PATH_USERS_AT_SPOT, USERS_AT_SPOT);
   }

   @Override
   public boolean onCreate() {
      dbHelper = new DatabaseHelper(getContext());

      // return value may not actually be used by android
      return false;
   }

   // return MIME type
   @Override
   public String getType(Uri uri) {
      switch (uriMatcher.match(uri)) {
         case SPOTS:
            return URI_DIR + Contract.Spots.TABLE_NAME;
         case SPOTS_ID:
            return URI_ITEM + Contract.Spots.TABLE_NAME;
         case USERS:
            return URI_DIR + Contract.Users.TABLE_NAME;
         case USERS_ID:
            return URI_ITEM + Contract.Users.TABLE_NAME;
         case SPOTS_USERS:
            return URI_DIR + Contract.SpotsUsers.TABLE_NAME;
         case SPOTS_USERS_ID:
            return URI_ITEM + Contract.SpotsUsers.TABLE_NAME;
         case USERS_AT_SPOT:
            return URI_DIR + PATH_USERS_AT_SPOT; // TODO not sure if this is right
         default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
      }
   }

   // Create
   @Override
   public Uri insert(Uri uri, ContentValues values) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      String table = getTable(uri); // name of table to insert into
      Uri retUri;

      // match uri to table
      switch (uriMatcher.match(uri)) {
         case SPOTS:
            retUri = SPOTS_URI;
            break;
         case USERS:
            retUri = USERS_URI;
            break;
         case SPOTS_USERS:
            retUri = SPOTS_USERS_URI;
            break;
         default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
      }

      long id = db.insert(table, null, values);
      // check if error occurred
      if (id == -1) {
         throw new SQLException("Failed to insert row into " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      Log.d("db", "insert");
      return Uri.parse(retUri + table + "/" + id);
   }

   // TODO remove unused switch cases
   // Read
   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                       String sortOrder) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
      Cursor cursor = null;

      switch (uriMatcher.match(uri)) {
         case SPOTS:
            queryBuilder.setTables(Contract.Spots.TABLE_NAME);
            break;
         case SPOTS_ID:
            queryBuilder.setTables(Contract.Spots.TABLE_NAME);
            selection = selection + "_id = " + uri.getLastPathSegment();
            break;
         case USERS:
            queryBuilder.setTables(Contract.Users.TABLE_NAME);
            break;
         case USERS_ID:
            queryBuilder.setTables(Contract.Users.TABLE_NAME);
            selection = selection + "_id = " + uri.getLastPathSegment();
            break;
         case SPOTS_USERS:
            queryBuilder.setTables(Contract.SpotsUsers.TABLE_NAME);
            break;
         case SPOTS_USERS_ID:
            queryBuilder.setTables(Contract.SpotsUsers.TABLE_NAME);
            selection = selection + "_id = " + uri.getLastPathSegment();
            break;
         case USERS_AT_SPOT:
            cursor = db.rawQuery( // TODO possibly make more readable
               "SELECT " + Contract.Users.TABLE_NAME + ".*" + //Contract.Users.COLUMN_NAME_NAME +
                  " FROM " + Contract.Users.TABLE_NAME + " AS users INNER JOIN " +
                  Contract.SpotsUsers.TABLE_NAME + " AS spotsUsers ON users." +
                  Contract.Spots._ID + " = spotsUsers." + Contract.SpotsUsers.COLUMN_NAME_USER_ID +
                  " WHERE spotsUsers." + Contract.SpotsUsers.COLUMN_NAME_SPOT_ID + " = " +
                  selection,
               null
            );
            break;
         default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
      }

      // TODO make less spaghetti
      if (cursor == null) {
         cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null,
            sortOrder);
      }

      cursor.setNotificationUri(getContext().getContentResolver(), uri);

      return cursor;
   }

   // Update
   @Override
   public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      String table = getTable(uri);

      switch (uriMatcher.match(uri)) {
         case SPOTS:
            break;
         case SPOTS_ID:
            selection = Contract.Spots._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         case USERS:
            break;
         case USERS_ID:
            selection = Contract.Users._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         case SPOTS_USERS:
            break;
         case SPOTS_USERS_ID:
            selection = Contract.SpotsUsers._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         default:
            throw new IllegalArgumentException("Unsupported URI: " + uri);
      }

      int count = db.update(table, values, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      SQLiteDatabase db = dbHelper.getWritableDatabase();
      String table = getTable(uri);

      switch (uriMatcher.match(uri)) {
         case SPOTS:
            break;
         case SPOTS_ID:
            selection = Contract.Spots._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         case USERS:
            break;
         case USERS_ID:
            selection = Contract.Users._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         case SPOTS_USERS:
            break;
         case SPOTS_USERS_ID:
            selection = Contract.SpotsUsers._ID + " = " + uri.getLastPathSegment() +
               (TextUtils.isEmpty(selection) ? "" : " AND (" + selection + ")");
            break;
         default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
      }

      int count = db.delete(table, selection, selectionArgs);
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   // get table name corresponding to uri
   private String getTable(Uri uri) {
      switch (uriMatcher.match(uri)) {
         case SPOTS:
         case SPOTS_ID:
            return Contract.Spots.TABLE_NAME;
         case USERS:
         case USERS_ID:
            return Contract.Users.TABLE_NAME;
         case SPOTS_USERS:
         case SPOTS_USERS_ID:
            return Contract.SpotsUsers.TABLE_NAME;
         default:
            throw new IllegalArgumentException("Unsupported URI " + uri);
      }
   }
}
