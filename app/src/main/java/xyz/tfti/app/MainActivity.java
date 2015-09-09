package xyz.tfti.app;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookSdk;

public class MainActivity extends AppCompatActivity {
   private static final int TOKEN_UPDATE_TIMEOUT = 3000; // timeout for token update in milliseconds
   private static final String STATE_ACCESS_TOKEN = "accessToken"; // savedInstanceState

   private boolean tokenUpdated = false; // flag indicating user access token is updated

   private AccessTokenTracker accessTokenTracker; // called by facebook sdk when token is updated
   private Handler timeout;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      // initialize facebook sdk
      FacebookSdk.sdkInitialize(getApplicationContext());

      // facebook's token update callback
      accessTokenTracker = new AccessTokenTracker() {
         @Override
         protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken,
                                                    AccessToken newAccessToken) {
            Log.d("main", "token updated, token updated: " + tokenUpdated);
            if (!tokenUpdated) {
               Log.d("main", "onCurrentAccessTokenChanged()");
               stopTimeout();
               // update flag
               tokenUpdated = true;
               updateLoginStatus(newAccessToken);
            }
         }
      };

      Log.d("main", "main activity, token updated: " + tokenUpdated);

      // update login if onCurrentAccessTokenChanged() takes too long
      timeout = new Handler();
      timeout.postDelayed(new Runnable() {
         public void run() {
            Log.d("main", "onCurrentAccessTokenChanged() time out, token updated: " + tokenUpdated);
            if (!tokenUpdated) {
               tokenUpdated = true;
               updateLoginStatus(AccessToken.getCurrentAccessToken());
            }
         }
      }, TOKEN_UPDATE_TIMEOUT);
   }

  /* @Override
   public void onSaveInstanceState(Bundle savedInstanceState) {
      savedInstanceState.putString(STATE_ACCESS_TOKEN, );
   }*/

   @Override
   public void onDestroy() {
      Log.d("main", "onDestroy()");
      super.onDestroy();
      accessTokenTracker.stopTracking();
      stopTimeout();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_home, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_settings) {
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   private void stopTimeout() {
      Log.d("main", "stopTimeout()");
      if (timeout != null) {
         timeout.removeCallbacksAndMessages(null);
      }
   }

   // check to display login or home
   private void updateLoginStatus(AccessToken accessToken) {
      if (accessToken == null) {
         Log.d("main", "not logged in");
         Intent intent = new Intent(this, LoginActivity.class);
         startActivity(intent);
         finish();
      } else {
         Log.d("main", "logged in");
         Intent intent = new Intent(this, HomeActivity.class);
         startActivity(intent);
         finish();
      }
   }
}
