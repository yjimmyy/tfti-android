package xyz.tfti.app;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by jimmy on 8/11/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper{
   public DatabaseHelper(Context context) {
      super(context, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
   }

   @Override
   public void onCreate(SQLiteDatabase db) {
      db.execSQL(Contract.Spots.CREATE_TABLE);
      db.execSQL(Contract.Users.CREATE_TABLE);
      db.execSQL(Contract.SpotsUsers.CREATE_TABLE);
   }

   @Override
   public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.d("db", "old version: " + oldVersion + ", new version: " + newVersion);
      db.execSQL(Contract.Spots.DELETE_TABLE);
      db.execSQL(Contract.Users.DELETE_TABLE);
      db.execSQL(Contract.SpotsUsers.DELETE_TABLE);
      onCreate(db);
   }

   // TODO implement rest of database methods
}
