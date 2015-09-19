package xyz.tfti.app;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ViewSpotActivity extends AppCompatActivity
   implements LoaderManager.LoaderCallbacks<Cursor> {

   private SimpleCursorAdapter dataAdapter;

   private String rowId; // row ID of spot

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_view_spot);

      // get row ID of spot
      if (this.getIntent().getExtras() != null) {
         Bundle bundle = this.getIntent().getExtras();
         rowId = bundle.getString("rowId").trim();
         setTitle(bundle.getString("spotName").trim());
      }

      displayListView();

      // set toolbar as action bar
      Toolbar toolbar = (Toolbar)findViewById(R.id.view_spot_toolbar);
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
      getMenuInflater().inflate(R.menu.menu_view_spot, menu);
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

   private void displayListView() {
      String[] columns = new String[] {
         Contract.Users.COLUMN_NAME_NAME
      };
      int[] to = new int[] {
         R.id.userEntryName
      };

      dataAdapter = new SimpleCursorAdapter(this, R.layout.user_entry, null, columns, to, 0);

      ListView listView = (ListView)findViewById(R.id.userList);
      listView.setAdapter(dataAdapter);
      // ensure loader is initialized
      getLoaderManager().initLoader(0, null, this);
   }

   @Override
   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      String[] projection = {
         Contract.Users.COLUMN_NAME_NAME
      };
      CursorLoader cursorLoader = new CursorLoader(this, DatabaseContentProvider.USERS_AT_SPOT_URI,
         projection, rowId, null, null);
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
}
