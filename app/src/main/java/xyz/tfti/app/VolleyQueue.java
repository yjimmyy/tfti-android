package xyz.tfti.app;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by jimmy on 9/11/2015.
 */
public class VolleyQueue {
   private static VolleyQueue instance;
   private RequestQueue requestQueue;
   private static Context context;

   private VolleyQueue(Context context) {
      this.context = context;
      requestQueue = getRequestQueue();
   }

   public static synchronized VolleyQueue getInstance(Context context) {
      if (instance == null) {
         Log.d("main", "New VolleyQueue instance");
         instance = new VolleyQueue(context);
      }
      return instance;
   }

   public RequestQueue getRequestQueue() {
      if (requestQueue == null) {
         requestQueue = Volley.newRequestQueue(context.getApplicationContext());
      }
      return requestQueue;
   }

   public <T> void addToRequestQueue(Request<T> req) {
      getRequestQueue().add(req);
   }
}
