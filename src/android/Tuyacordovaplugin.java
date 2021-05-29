package com.arihant.tuyaplugin;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class echoes a string called from JavaScript.
 */
public class Tuyacordovaplugin extends CordovaPlugin {
    private CallbackContext callbackContext;
    private Activity activity;
    String TAG = "Tuyacordovaplugin";
    private boolean sdkInitialized = false;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Log.d(TAG, "initialize: ");
        if(!sdkInitialized){
            sdkInitialized = true;
            TuyaHomeSdk.init(cordova.getActivity().getApplication());
        }
    }
    @Override
    public void onResume(boolean p) {
        super.onPause(p);
    }

    @Override
    public void onPause(boolean p) {
        super.onPause(p);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TuyaHomeSdk.onDestroy();
        this.sdkInitialized = false;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        activity = cordova.getActivity();
        if (this.callbackContext == null) {
            this.callbackContext = callbackContext;
        }

        LOG.d(TAG, "action = %s", action);

        boolean validAction = true;
        java.lang.reflect.Method method;

        try {
            // method = this.getClass().getMethod(action);
            method = Tuyacordovaplugin.class.getMethod(action, CordovaArgs.class, CallbackContext.class);
        } catch (java.lang.SecurityException e) {
            LOG.d(TAG, "getMethod SecurityException = %s", e.toString());
            return false;

        } catch (java.lang.NoSuchMethodException e) {
            LOG.d(TAG, "getMethod NoSuchMethodException = %s", e.toString());
            return false;
        }

        try {
            method.invoke(this, args, callbackContext);
        } catch (java.lang.IllegalArgumentException e) {
            callbackContext.error(e.toString());
        } catch (java.lang.IllegalAccessException e) {
            callbackContext.error(e.toString());
        } catch (java.lang.reflect.InvocationTargetException e) {
            callbackContext.error(e.toString());
        }
        return true;
    }

    public void login(CordovaArgs args, CallbackContext callbackContext){
        
    }

    private void coolMethod(String message) {
        if (message != null && message.length() > 0) {
            TuyaHomeSdk.getUserInstance().loginWithEmail("91", "arihantdaga2010@gmail.com", "sdad7323", new ILoginCallback(){
                @Override
                public void onSuccess(User user) {
                    Toast.makeText(activity.getApplicationContext(),
                            "Login success",
                            Toast.LENGTH_SHORT).show();
                    callbackContext.success("Hello brother"+ message);
//                    fetchUserHomeList();
                }
                @Override
                public void onError(String code, String error) {
                    Toast.makeText(activity.getApplicationContext(),
                            "code: " + code + "error:" + error,
                            Toast.LENGTH_SHORT).show();
                    callbackContext.success("Hello brother"+ message);
                }
            });


//            this.callbackContext.success("Hello brother"+ message);
        } else {
            this.callbackContext.error("Expected one non-empty string argument.");
        }
    }
}
