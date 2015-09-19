package xyz.tfti.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.util.Log;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Helper method for working with sync framework
 */
public class SyncHelper implements WebService.Callback<JSONArray> {
   private static final String ACCOUNT_TYPE = "tfti.xyz";
   private static final String ACCOUNT_NAME = "dummyaccount";

   public static Account account;
   private Context context;

   public SyncHelper(Context context) {
      this.context = context;
   }

   public static void createSyncAccount(Context context) {
      account = new Account(ACCOUNT_NAME, ACCOUNT_TYPE);
      AccountManager accountManager =
         (AccountManager)context.getSystemService(Context.ACCOUNT_SERVICE);

      if (accountManager.addAccountExplicitly(account, null, null)) {
         ContentResolver.setIsSyncable(account, DatabaseContentProvider.AUTHORITY, 1);
      } else {
         Log.d("main", "account error");
      }
   }

   // Requests a sync
   public static void refresh() {
      Log.d("main", "refresh()");
      Bundle bundle = new Bundle();

      // Run sync on demand
      bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
      //bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
      // Refresh option
      //bundle.putBoolean(SyncAdapter.KEY_REFRESH, true);

      ContentResolver.requestSync(account, DatabaseContentProvider.AUTHORITY, bundle);
   }

   // Called from Sync Adapter to perform sync
   public void startSync(Bundle extras) {
      WebService.getInstance(context).getSpots(this);
   }

   // Callback from WebService requests
   // TODO: possibly make anonymous if multiple types of responses are needed
   @Override
   public void onResponse(JSONArray response, VolleyError error) {
      if (error != null) {
         Log.d("main", "error with web service");
         return;
      }

      ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

      // Clear Spots TODO: consider using dirty flags to reduce data usage
      batch.add(ContentProviderOperation.newDelete(DatabaseContentProvider.SPOTS_URI)
         .withSelection(null, null).build());
      // Clear Users
      batch.add(ContentProviderOperation.newDelete(DatabaseContentProvider.USERS_URI)
         .withSelection(null, null).build());
      // Clear SpotsUsers
      batch.add(ContentProviderOperation.newDelete(DatabaseContentProvider.SPOTS_USERS_URI)
         .withSelection(null, null).build());

      for (int i = 0; i < response.length(); i++) {
         ContentValues spotValues = new ContentValues();
         try {
            // Get Spot fields
            JSONObject spotObject = response.getJSONObject(i);
            spotValues.put(
               Contract.Spots.COLUMN_NAME_OBJECT_ID, spotObject.getString(WebService.KEY_SPOT_OBJECT_ID));
            spotValues.put(
               Contract.Spots.COLUMN_NAME_NAME, spotObject.getString(WebService.KEY_SPOT_NAME));
            spotValues.put(
               Contract.Spots.COLUMN_NAME_LAT, spotObject.getDouble(WebService.KEY_SPOT_LAT));
            spotValues.put(
               Contract.Spots.COLUMN_NAME_LNG, spotObject.getDouble(WebService.KEY_SPOT_LNG));
            spotValues.put(
               Contract.Spots.COLUMN_NAME_RADIUS, spotObject.getInt(WebService.KEY_SPOT_RADIUS));

            JSONArray membersArray = spotObject.getJSONArray(WebService.KEY_SPOT_MEMBERS);
            // Get members
            for (int j = 0; j < membersArray.length(); j++) {
               JSONObject memberObject = membersArray.getJSONObject(j);
               // TODO: handle duplicate users
               // Add Users
               JSONObject userObject = memberObject.getJSONObject(WebService.KEY_SPOT_MEMBER_USER_ID);
               ContentValues userValues = new ContentValues();
               userValues.put(Contract.Users.COLUMN_NAME_OBJECT_ID,
                  userObject.getString(WebService.KEY_USER_OBJECT_ID));
               userValues.put(Contract.Users.COLUMN_NAME_NAME,
                  userObject.getString(WebService.KEY_USER_NAME));
               batch.add(ContentProviderOperation.newInsert(DatabaseContentProvider.USERS_URI)
                  .withValues(userValues).build());

               // Add SpotsUsers
               ContentValues spotUserValues = new ContentValues();
               spotUserValues.put(Contract.SpotsUsers.COLUMN_NAME_SPOT_ID,
                  spotObject.getString(WebService.KEY_SPOT_OBJECT_ID));
               spotUserValues.put(Contract.SpotsUsers.COLUMN_NAME_USER_ID,
                  userObject.getString(WebService.KEY_USER_OBJECT_ID));
               batch.add(ContentProviderOperation.newInsert(DatabaseContentProvider.SPOTS_USERS_URI)
                  .withValues(spotUserValues).build());
            }
         } catch (JSONException e) {
            Log.d("main", "ERROR: " + i + " " + e);
         }
         batch.add(ContentProviderOperation.newInsert(DatabaseContentProvider.SPOTS_URI)
            .withValues(spotValues).build());
      } // end loop

      try {
         context.getContentResolver().applyBatch(DatabaseContentProvider.AUTHORITY, batch);
      } catch (RemoteException e) {
         Log.d("main", "ERROR " + e);
      } catch (OperationApplicationException e) {
         Log.d("main", "ERROR " + e);
      }
   }

   private void getSpotsToUpdate() {
      String[] columns = {
         Contract.Spots.COLUMN_NAME_NAME,
         Contract.Spots.COLUMN_NAME_LAT,
         Contract.Spots.COLUMN_NAME_LNG,
         Contract.Spots.COLUMN_NAME_RADIUS
      };
      String selection = Contract.Spots.COLUMN_NAME_FLAG + " == 1";
      Cursor cursor = context.getContentResolver().query(
         DatabaseContentProvider.SPOTS_TO_UPDATE_URI, columns, selection, null, null);
      Log.d("main", "getSpotsToUpdate: " + cursor.getCount());
   }
}
