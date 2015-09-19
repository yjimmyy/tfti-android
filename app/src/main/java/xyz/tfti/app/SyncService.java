package xyz.tfti.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jimmy on 9/12/2015.
 */
public class SyncService extends Service {
   private static SyncAdapter syncAdapter = null;

   // Object to use as a thread-safe lock
   private static final Object syncAdapterLock = new Object();

   @Override
   public void onCreate() {
      synchronized (syncAdapterLock) {
         /* Create sync adapter as singleton
          * Set sync adapter as syncable
          * Disallow parallel syncs
          */
         if (syncAdapter == null) {
            syncAdapter = new SyncAdapter(getApplicationContext(), true);
         }
      }
   }

   @Override
   public IBinder onBind(Intent intent) {
      return syncAdapter.getSyncAdapterBinder();
   }


}
