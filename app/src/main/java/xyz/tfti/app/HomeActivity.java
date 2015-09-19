package xyz.tfti.app;

import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.facebook.login.LoginManager;

public class HomeActivity extends AppCompatActivity
   implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

   private AlarmManager alarmManager;
   private PendingIntent alarmIntent;

   private SimpleCursorAdapter dataAdapter;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      Log.d("home", "home activity");

      if (this.getIntent().getExtras() != null) {
         if (this.getIntent().getExtras().getBoolean("initialLogin")) {
            // Start location service
            //Intent intent = new Intent(this, LocationService.class);
            //startService(intent);
            Log.d("main", "initialize alarm manager");
            // Alarm manager for location service
            alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent(this, LocationService.class);
            Bundle bundle = new Bundle();
            bundle.putString("token", WebService.getToken());
            bundle.putString("userFbId", WebService.getUserFbId());
            bundle.putString("userObjectId", WebService.getUserObjectId());
            intent.putExtras(bundle);
            alarmIntent = PendingIntent.getService(this.getApplicationContext(), 0, intent,
               PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.cancel(alarmIntent); // prevent duplicates
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 0,
               LocationService.PING_INTERVAL,alarmIntent);
         }
      }

      setContentView(R.layout.activity_home);

      displayListView(); // TODO move down

      // set toolbar as action bar
      Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
   }

   @Override
   protected void onResume() {
      super.onResume();
      getLoaderManager().restartLoader(0, null, this);
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
      if (id == R.id.action_add_spot) {
         addSpot();
         return true;
      } else if (id == R.id.action_join_spot) {
         joinSpot();
         return true;
      } else if (id == R.id.action_settings) {
         return true;
      } else if (id == R.id.action_logout) {
         logout();
         return true;
      } else if (id == R.id.action_refresh) {
         SyncHelper.refresh();
         return true;
      } else if (id == R.id.action_stop_service) {
         stopAlarms();
      }

      return super.onOptionsItemSelected(item);
   }

   // create a new spot
   private void addSpot() {
      // open EditSpotActivity activity in add mode
      Intent intent = new Intent(this, EditSpotActivity.class);
      Bundle bundle = new Bundle();
      bundle.putString("mode", "add");
      intent.putExtras(bundle);
      startActivity(intent);
   }

   private void joinSpot() {
      Intent intent = new Intent(this, JoinSpotActivity.class);
      startActivity(intent);
   }

   // log user out
   private void logout() {
      // log out of facebook session
      LoginManager.getInstance().logOut();

      // go to MainActivity to restart login process
      Intent intent = new Intent(this, MainActivity.class);
      startActivity(intent);

      // prevent backing to this activity
      finish();
   }

   private void stopAlarms() {
      alarmManager.cancel(alarmIntent);
   }

   private void displayListView() {
      String[] columns = new String[] {
         Contract.Spots.COLUMN_NAME_NAME,
         Contract.Spots.COLUMN_NAME_NUM_USERS,
         Contract.Spots.COLUMN_NAME_LAT,
         Contract.Spots.COLUMN_NAME_LNG,
         Contract.Spots.COLUMN_NAME_RADIUS
      };
      int[] to = new int[] {
         R.id.spotEntryName,
         R.id.spotEntryNumUsers,
         R.id.spotEntryLat,
         R.id.spotEntryLng,
         R.id.spotEntryRadius
      };

      dataAdapter = new SimpleCursorAdapter(this, R.layout.spot_entry, null, columns, to, 0);

      // TODO: fix naming inconsistencies
      ListView listView = (ListView)findViewById(R.id.spotList);
      listView.setAdapter(dataAdapter);
      // ensure loader is initialized
      getLoaderManager().initLoader(0, null, this);

      listView.setOnItemClickListener(this);
   }

   // TODO use ViewBinder to show proper precision for doubles
   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      String[] projection = {
         Contract.Spots._ID,
         Contract.Spots.COLUMN_NAME_OBJECT_ID,
         Contract.Spots.COLUMN_NAME_NAME,
         Contract.Spots.COLUMN_NAME_NUM_USERS,
         Contract.Spots.COLUMN_NAME_LAT,
         Contract.Spots.COLUMN_NAME_LNG,
         Contract.Spots.COLUMN_NAME_RADIUS
      };
      CursorLoader cursorLoader = new CursorLoader(this, DatabaseContentProvider.SPOTS_URI,
         projection, null, null, null);
      return cursorLoader;
   }

   @Override
   public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
      // framework takes care of closing old adapter after return
      dataAdapter.swapCursor(data);
   }

   @Override
   public void onLoaderReset(Loader<Cursor> loader) {
      dataAdapter.swapCursor(null);
   }

   @Override
   public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      // get cursor from row
      Cursor cursor = (Cursor)parent.getItemAtPosition(position);

      //String rowId = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Users._ID));
      String rowId = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Spots.COLUMN_NAME_OBJECT_ID));
      String spotName = cursor.getString(cursor.getColumnIndexOrThrow(Contract.Spots.COLUMN_NAME_NAME));

      Intent intent = new Intent(this, ViewSpotActivity.class);
      Bundle bundle = new Bundle();
      bundle.putString("rowId", rowId); // TODO make keys (rowId) a constant somewhere
      bundle.putString("spotName", spotName);
      intent.putExtras(bundle);
      startActivity(intent);
   }
}
