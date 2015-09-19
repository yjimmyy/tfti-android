package xyz.tfti.app;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by jimmy on 9/11/2015.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
   public final static String KEY_REFRESH = "refresh";
   public final static String KEY_SPOTS_UPDATED = "spotsUpdated";

   ContentResolver contentResolver;
   private Context context;

   public SyncAdapter(Context context, boolean autoInitialize) {
      super(context, autoInitialize);
      this.context = context;
      contentResolver = context.getContentResolver();
   }

   public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
      super(context, autoInitialize, allowParallelSyncs);
      this.context = context;
      contentResolver = context.getContentResolver();
   }

   public void onPerformSync(Account account, Bundle extras, String authority,
                             ContentProviderClient provider, SyncResult syncResult) {
      Log.d("main", "SyncAdapter sync");
      new SyncHelper(context).startSync(extras);
   }
}
