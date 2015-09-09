package xyz.tfti.app;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class FindLocationActivity extends AppCompatActivity
   implements OnMapReadyCallback, GoogleMap.OnMapClickListener, SeekBar.OnSeekBarChangeListener {

   private static final int MIN_RADIUS = 20;
   private static final int MAX_RADIUS = 200;

   private Toolbar toolbar;
   private SeekBar radiusSeekBar;
   private GoogleMap map;
   private Marker marker;
   private Circle radiusCircle;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_find_location);

      // setup toolbar
      toolbar = (Toolbar)findViewById(R.id.find_location_toolbar);
      setSupportActionBar(toolbar);

      // setup map
      SupportMapFragment mapFragment = (SupportMapFragment)getSupportFragmentManager()
         .findFragmentById(R.id.map);
      mapFragment.getMapAsync(this);

      // setup radius selection
      radiusSeekBar = (SeekBar)findViewById(R.id.radius_seekbar);
      radiusSeekBar.setMax(MAX_RADIUS - MIN_RADIUS);
      radiusSeekBar.setOnSeekBarChangeListener(this);

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_find_location, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_finish) {
         Intent resultIntent = new Intent();
         Bundle resultBundle = new Bundle();
         resultBundle.putInt("radius", (int)radiusCircle.getRadius());
         resultBundle.putDouble("lat", marker.getPosition().latitude);
         resultBundle.putDouble("lng", marker.getPosition().longitude);
         resultIntent.putExtras(resultBundle);
         setResult(Activity.RESULT_OK, resultIntent);
         finish();
         return true;
      }

      return super.onOptionsItemSelected(item);
   }

   // TODO save location when screen rotated
   @Override
   public void onMapReady(GoogleMap map) {
      this.map = map;
      if (marker == null) {
         LatLng point = new LatLng(37.720773, -122.255543);
         marker = map.addMarker(new MarkerOptions().position(point));
         map.moveCamera(CameraUpdateFactory.newLatLng(point));
         map.moveCamera(CameraUpdateFactory.zoomTo(9));
         CircleOptions circleOptions = new CircleOptions().center(point).radius(MIN_RADIUS);
         radiusCircle = map.addCircle(circleOptions);
      }
      map.setOnMapClickListener(this);
   }

   // when map is clicked
   @Override
   public void onMapClick(LatLng point) {
      // move marker and camera to clicked position
      marker.setPosition(point);
      radiusCircle.setCenter(point);
      map.moveCamera(CameraUpdateFactory.newLatLng(point));
   }

   // when radius slider is changed
   @Override
   public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      // update radius circle
      int radius = progress + MIN_RADIUS;
      radiusCircle.setRadius(radius);
   }

   @Override
   public void onStartTrackingTouch(SeekBar seekBar) {}

   @Override
   public void onStopTrackingTouch (SeekBar seekBar) {}
}
