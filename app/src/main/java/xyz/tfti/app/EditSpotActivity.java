package xyz.tfti.app;

import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

public class EditSpotActivity extends AppCompatActivity
   implements View.OnClickListener {

   private static final String MODE_ADD = "add"; // used to determine editing mode
   private static final String MODE_EDIT = "edit";
   private static final int FIND_LOCATION_REQUEST = 1; // request code for FindLocationActivity

   private Toolbar toolbar;
   private Button buttonSave;
   private Button buttonFindLocation;
   private EditText fieldName;
   private String mode = MODE_ADD; // add mode by default
   private LocationResult locationResult;

   // TODO possibly not use LocationResult
   private class LocationResult {
      private boolean set = false;
      private int radius;
      private double lat;
      private double lng;
      public LocationResult(int radius, double lat, double lng) {
         this.radius = radius;
         this.lat = lat;
         this.lng = lng;
         set = true;
      }
      public boolean isSet() {
         return set;
      }
      public int getRadius() {
         return radius;
      }
      public double getLat() {
         return lat;
      }
      public double getLng() {
         return lng;
      }
   }

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit_spot);

      // get editing mode
      if (this.getIntent().getExtras() != null) {
         Bundle bundle = this.getIntent().getExtras();
         mode = bundle.getString("mode").trim().toLowerCase();
      }

      // setup toolbar
      toolbar = (Toolbar)findViewById(R.id.edit_spot_toolbar);
      setSupportActionBar(toolbar);
      if (mode.equals(MODE_ADD)) {
         setTitle("Add Spot");
      } else if (mode.equals(MODE_EDIT)) {
         setTitle("Edit Spot");
      }

      // get references to ui elements
      buttonFindLocation = (Button)findViewById(R.id.find_location);
      buttonFindLocation.setOnClickListener(this);
      buttonSave = (Button)findViewById(R.id.save);
      buttonSave.setOnClickListener(this);
      fieldName = (EditText)findViewById(R.id.name);
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_edit_spot, menu);
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

   public void onClick(View v) {
      switch(v.getId()) {
         case R.id.find_location:
            onClickButtonFindLocation();
            break;
         case R.id.save:
            onClickButtonSave();
            break;
      }
   }

   private void onClickButtonFindLocation() {
      Intent intent = new Intent(this, FindLocationActivity.class);
      startActivityForResult(intent, FIND_LOCATION_REQUEST);
   }

   private void onClickButtonSave() {
      String name = fieldName.getText().toString();

      // check for valid location
      if (locationResult == null || !locationResult.isSet()) {
         Toast.makeText(this, "Location required", Toast.LENGTH_SHORT).show();
         return;
      }
      // check for valid name
      // TODO: add floating text for error instead of toast
      if (name.trim().toLowerCase().equals("")) {
         Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
         return;
      }

      JSONObject spot = new JSONObject();
      try {
         spot.put(WebService.KEY_SPOT_NAME, name);
         spot.put(WebService.KEY_SPOT_LAT, locationResult.getLat());
         spot.put(WebService.KEY_SPOT_LNG, locationResult.getLng());
         spot.put(WebService.KEY_SPOT_RADIUS, locationResult.getRadius());
      } catch (JSONException e) {
         Log.d("main", "Error: " + e);
         saveError();
      }

      WebService.getInstance(this).createSpot(spot, new WebService.Callback<JSONObject>() {
         @Override
         public void onResponse(JSONObject response, VolleyError error) {
            if (error != null || response == null) {
               saveError();
            } else {
               SyncHelper.refresh();
               finish();
            }
         }
      });
   }

   private void saveError() {
      Toast.makeText(this, "Unable to create spot", Toast.LENGTH_SHORT).show();
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode == FIND_LOCATION_REQUEST) {
         if (resultCode == RESULT_OK) {
            locationResult = new LocationResult(
               data.getIntExtra("radius", -1),
               data.getDoubleExtra("lat", -1),
               data.getDoubleExtra("lng", -1)
            );
         }
      }
   }
}
