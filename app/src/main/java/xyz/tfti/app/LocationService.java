package xyz.tfti.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by jimmy on 9/17/2015.
 */
public class LocationService extends Service
   implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
   public static final int PING_INTERVAL = 1000 * 10;

   // Duration to allow location listener to update location
   private static final int LOCATION_UPDATE_DURATION = 1000 * 20;

   private GoogleApiClient googleApiClient;
   private Handler timeout;

   private boolean timedOut = false;

   public LocationService() {
   }

   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
      Log.d("main", "Service intent received MKII");

      Bundle bundle = intent.getExtras();
      if (bundle == null) {
         return START_NOT_STICKY;
      }

      String token = bundle.getString("token");
      String userFbId = bundle.getString("userFbId");
      String userObjectId = bundle.getString("userObjectId");
      Log.d("main", "Service token: " + token + " userId: " + userFbId + " objectId: " + userObjectId);
      if (token == null || userFbId == null || userObjectId == null) {
         Log.d("main", "Service error: no token, userId, or objectId");
         return START_NOT_STICKY;
      }

      buildGoogleApiClient();
      googleApiClient.connect();

      timeout = new Handler();
      timeout.postDelayed(new Runnable() {
         public void run() {
            Log.d("main", "Service timed out");
            finish();
         }
      }, LOCATION_UPDATE_DURATION);

      //WebService.getInstance(this, token, userFbId, userObjectId).pingSpot("55f76bf610bdb3c41aa53784");

      //stopSelf();

      return START_NOT_STICKY;
   }

   @Override
   public IBinder onBind(Intent intent) {
      Log.d("main", "Service onBind()");
      return null;
   }

   @Override
   public void onCreate() {
      Log.d("main", "Service onCreate()");
   }

   @Override
   public void onDestroy() {
      Log.d("main", "Service onDestroy()");
   }

   // Build google api client
   protected synchronized void buildGoogleApiClient() {
      Log.d("main", "buildGoogleApiClient");
      googleApiClient = new GoogleApiClient.Builder(this)
         .addConnectionCallbacks(this)
         .addOnConnectionFailedListener(this)
         .addApi(LocationServices.API)
         .build();
   }

   @Override
   public void onConnected(Bundle connectionHint) {
      Log.d("main", "Connection to google services success");
      if (timedOut) {
         return;
      } else {
         stopTimeout();
      }

      Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
      if (lastLocation != null) {
         Log.d("main", "Lat: " + lastLocation.getLatitude() + ", Lng: " + lastLocation.getLongitude());
      }

      finish();
   }

   public void onConnectionSuspended(int cause) {
      Log.d("main", "Connection to google services suspended");
   }

   public void onConnectionFailed(ConnectionResult result) {
      Log.d("main", "Connection to google services failed");
      finish();
   }

   private void stopTimeout() {
      if (timeout != null) {
         timeout.removeCallbacksAndMessages(null);
      }
   }

   private void finish() {
      stopTimeout();
      timedOut = true;
      if (googleApiClient != null) {
         googleApiClient.disconnect();
      }
      stopSelf();
   }
}
