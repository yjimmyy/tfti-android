package xyz.tfti.app;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by jimmy on 9/14/2015.
 */
public class TftiJsonArrayRequest extends JsonArrayRequest {
   private String token;
   private String userId;

   public TftiJsonArrayRequest(int method, String url, JSONArray object, Response.Listener listener,
                                Response.ErrorListener errorListener, String token, String userFbId) {
      super(method, url, object, listener, errorListener);
      this.token = token;
      this.userId = userFbId;
   }

   // Attaches headers to request
   @Override
   public Map getHeaders() throws AuthFailureError {
      Map headers = new HashMap();
      // Web service requires header to be in format of:
      // Authorization: accesstoken:userId
      headers.put("Authorization", token + ":" + userId);
      return headers;
   }
}
