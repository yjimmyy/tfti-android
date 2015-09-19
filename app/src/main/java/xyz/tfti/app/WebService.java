package xyz.tfti.app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Handles http requests to web service
public class WebService {
   public static final int OBJECTID_LENGTH = 24;

   public static final String KEY_SPOT_OBJECT_ID = "_id";
   public static final String KEY_SPOT_NAME = "name";
   public static final String KEY_SPOT_LAT = "lat";
   public static final String KEY_SPOT_LNG = "lng";
   public static final String KEY_SPOT_RADIUS = "radius";
   public static final String KEY_SPOT_MEMBERS = "members";
   public static final String KEY_SPOT_MEMBER_USER_ID = "user_id";

   public static final String KEY_USER_OBJECT_ID = "_id";
   public static final String KEY_USER_NAME = "name";

   private static final String ROOT_URL = "http://192.168.1.106:3000/api/";//"http://10.0.2.2:3000/api"; // server url
   private static final String USER_URL = ROOT_URL + "user/"; // user endpoint
   private static final String SPOT_URL = ROOT_URL + "spot/"; // spot endpoint
   private static final String SPOT_MEMBER_URL = "/member/";

   private static final int REQUEST_TIMEOUT = 2;

   private static WebService instance;
   private Context context;
   private static String token; // User's access token
   private static String userFbId; // User's Faceboook id
   private static String userObjectId; // User's _id in MongoDB
   // TODO: use accessor and mutator methods

   // Callback interface
   public interface Callback<ResponseType> {
      public void onResponse(ResponseType response, VolleyError error);
   }

   // TODO: maybe this shouldn't be a singleton
   private WebService(Context context) {
      this.context = context;
   }

   private WebService(Context context, String token, String userId, String userObjectId) {
      this.context = context;
      this.token = token;
      this.userFbId = userId;
      this.userObjectId = userObjectId;
   }

   public static synchronized WebService getInstance(Context context) {
      if (instance == null) {
         Log.d("main", "new WebService instance");
         instance = new WebService(context);
      }
      return instance;
   }

   // Allows token and userId to be manually set if called from another context
   public static synchronized  WebService getInstance(Context context, String token, String userId,
                                                      String userObjectId) {
      if (instance == null) {
         Log.d("main", "new WebService instance with manually set token and userId \n token: " +
            token + " userId: " + userId);
         instance = new WebService(context, token, userId, userObjectId);
      }
      return instance;
   }

   public static String getToken() {
      return token;
   }

   public static String getUserFbId() {
      return userFbId;
   }

   public static String getUserObjectId() {
      return userObjectId;
   }

   // TODO: make asynchronous
   // Registers user and results in object id
   public void loadUser(String token, String userFbId, Callback<JSONObject> cb) {
      this.token = token;
      this.userFbId = userFbId;
      final Callback<JSONObject> listener = cb;

      Log.d("main", "loadUser(), token: " + token + " userFbId: " + userFbId);
      RequestQueue queue = VolleyQueue.getInstance(context).getRequestQueue();

      // Construct request body
      /*JSONObject obj = new JSONObject();
      try {
         //JSONObject obj = new JSONObject();
         obj.put("name", "Test User 3");
         obj.put("fbUID", userFbId);
      } catch (JSONException e) {
         e.printStackTrace();
      }*/
      // Construct request
      TftiJsonObjectRequest request = new TftiJsonObjectRequest(Request.Method.POST, USER_URL, null,
         new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               try {
                  userObjectId = response.getString("_id");
               } catch (JSONException e) {
                  e.printStackTrace();
               }
               Log.d("main", "object id: " + userObjectId);
               listener.onResponse(response, null);
            }
         },
         new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Log.d("main", "Volley Login Error response: " + error);
               listener.onResponse(null, error);
            }
         }, token, userFbId);

      queue.add(request);
   }

   public void getSpots(Callback<JSONArray> cb) {
      if (cb == null) {
         Log.d("main", "ERROR no callback"); // TODO: proper error checking and handling
         return;
      }
      final Callback<JSONArray> listener = cb;
      RequestQueue queue = VolleyQueue.getInstance(context).getRequestQueue();
      TftiJsonArrayRequest request = new TftiJsonArrayRequest(Request.Method.GET, SPOT_URL, null,
         new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
               listener.onResponse(response, null);
            }
         },
         new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Log.d("main", "Volley get spot error: " + error);// + error.networkResponse.statusCode);
               listener.onResponse(null, error);
            }
         }, token, userFbId);
      queue.add(request);
   }

   // Synchronous request to add Spot
   public void createSpot(JSONObject spot, final Callback<JSONObject> listener) {
      RequestQueue queue = VolleyQueue.getInstance(context).getRequestQueue();
      TftiJsonObjectRequest request = new TftiJsonObjectRequest(Request.Method.POST, SPOT_URL, spot,
         new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               listener.onResponse(response, null);
            }
         }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               listener.onResponse(null, error);
            }
         }, token, userFbId);
      queue.add(request);
   }

   public void joinSpot(String spotObjectId, final Callback<JSONObject> listener) {
      if (listener == null) {
         Log.d("main", "ERROR no callback");
      }
      RequestQueue queue = VolleyQueue.getInstance(context).getRequestQueue();
      TftiJsonObjectRequest request = new TftiJsonObjectRequest(Request.Method.POST,
         SPOT_URL + spotObjectId + SPOT_MEMBER_URL, null, new Response.Listener<JSONObject>() {
         @Override
            public void onResponse(JSONObject response) {
               listener.onResponse(response, null);
            }
         }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               listener.onResponse(null, error);
            }
         }, token, userFbId);
      queue.add(request);
   }

   public void pingSpot(String spotObjectId/*, final Callback<JSONObject> listener*/) {
      /*if (listener == null) {
         Log.d("main", "ERROR no callback");
      }*/
      String url = SPOT_URL + spotObjectId + SPOT_MEMBER_URL + userObjectId;
      RequestQueue queue = VolleyQueue.getInstance(context).getRequestQueue();
      TftiJsonObjectRequest request = new TftiJsonObjectRequest(Request.Method.GET, url, null,
         new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
               Log.d("main", "ping success");
            }
         }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
               Log.d("main", "ping error");
            }
         }, token, userFbId);
      queue.add(request);
   }
}
