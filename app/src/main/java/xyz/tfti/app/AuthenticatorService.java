package xyz.tfti.app;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by jimmy on 9/11/2015.
 */
public class AuthenticatorService extends Service {
   private StubAuthenticator authenticator;

   @Override
   public void onCreate() {
      authenticator = new StubAuthenticator(this);
   }

   @Override
   public IBinder onBind(Intent intent) {
      return authenticator.getIBinder();
   }
}
