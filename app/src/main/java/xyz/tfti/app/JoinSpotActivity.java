package xyz.tfti.app;

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

import org.json.JSONObject;

public class JoinSpotActivity extends AppCompatActivity {
   private Toolbar toolbar;
   private EditText fieldSpotId;
   private Button buttonJoin;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_join_spot);

      toolbar = (Toolbar)findViewById(R.id.join_spot_toolbar);
      setSupportActionBar(toolbar);

      fieldSpotId = (EditText)findViewById(R.id.spotId);
      buttonJoin = (Button)findViewById(R.id.join);
      buttonJoin.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            joinSpot();
         }
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_join_spot, menu);
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

   private void joinSpot() {
      String spotObjectId = fieldSpotId.getText().toString().trim();
      Log.d("main", spotObjectId);

      if (spotObjectId.length() != WebService.OBJECTID_LENGTH) {
         Toast.makeText(this, "ID needs to be 24 characters long", Toast.LENGTH_SHORT).show();
         return;
      }

      WebService.getInstance(this).joinSpot(spotObjectId, new WebService.Callback<JSONObject>() {
         @Override
         public void onResponse(JSONObject response, VolleyError error) {
            if (error != null) {
               showToastMessage("Unable to join spot");
            } else {
               SyncHelper.refresh();
               finish();
            }
         }
      });
   }

   private void showToastMessage(String message) {
      Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
   }
}
