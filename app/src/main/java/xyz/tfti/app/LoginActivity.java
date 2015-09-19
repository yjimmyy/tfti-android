package xyz.tfti.app;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
   implements FacebookCallback<LoginResult>, WebService.Callback<JSONObject> {
   private TextView info;
   private LoginButton loginButton;
   private CallbackManager callbackManager; // fb callback manager

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      Log.d("login", "login activity");
      super.onCreate(savedInstanceState);

      // initialize fb sdk
      callbackManager = CallbackManager.Factory.create();

      // set up layout and get view elements
      setContentView(R.layout.activity_login);
      info = (TextView)findViewById(R.id.info);
      loginButton = (LoginButton)findViewById(R.id.fb_login_button);

      // callback for fb login button
      loginButton.registerCallback(callbackManager, this);
   }

   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      callbackManager.onActivityResult(requestCode, resultCode, data);
   }

   private void finishLogin() {
      // go to HomeActivity
      Intent intent = new Intent(this, HomeActivity.class);
      Bundle bundle = new Bundle();
      bundle.putBoolean("initialLogin", true);
      intent.putExtras(bundle);
      startActivity(intent);

      // prevent user from coming back to this activity
      finish();
   }

   @Override
   public void onSuccess(LoginResult loginResult) {
      info.setText("User ID: " + loginResult.getAccessToken().getUserId() + "\n" +
         "Auth Token: " + loginResult.getAccessToken().getToken());

      Log.d("login", "login successful");

      WebService.getInstance(this).loadUser(loginResult.getAccessToken().getToken(),
         loginResult.getAccessToken().getUserId(), this);
   }

   @Override
   public void onCancel() {
      info.setText("Login attempt cancelled");
   }

   @Override
   public void onError(FacebookException e) {
      info.setText("Login attempt failed");
   }

   @Override
   public void onResponse(JSONObject response, VolleyError error) {
      if (error != null) {
         Log.d("main", "Login Volley Error: " + error);
      } else {
         Log.d("main", "Login Volley Success: " + response);
         finishLogin();
      }
   }
}
