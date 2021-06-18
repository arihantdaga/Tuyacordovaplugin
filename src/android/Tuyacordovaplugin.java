package com.arihant.tuyaplugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.google.zxing.WriterException;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.user.api.ILoginCallback;
import com.tuya.smart.android.user.api.IUidLoginCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.WifiSignalListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaCameraDevActivator;
import com.tuya.smart.sdk.api.ITuyaSmartCameraActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;

/**
 * This class echoes a string called from JavaScript.
 */
public class Tuyacordovaplugin extends CordovaPlugin {
    private CallbackContext cbCtx;
    private Activity activity;
    String TAG = "Tuyacordovaplugin";
    private boolean sdkInitialized = false;

    private ITuyaCameraDevActivator mTuyaActivator;
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
        activity.unregisterReceiver(br);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        activity = cordova.getActivity();
        if (this.cbCtx == null) {
            this.cbCtx = callbackContext;
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

    BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                String method = intent.getStringExtra("method");
                String data = intent.getStringExtra("data");
                if(method != null){

                }
            }
        }
    };

    public void login(CordovaArgs args, CallbackContext callbackContext){
        
    }

    public void user_loginOrRegitserWithUID(CordovaArgs args, CallbackContext callbackContext){
        try{
            String countryCode = args.getString(0);
            String uid = args.getString(1);
            String pass = args.getString(2);
            TuyaHomeSdk.getUserInstance().loginOrRegisterWithUid (countryCode, uid, pass, true, new IUidLoginCallback(){
                @Override
                public void onSuccess(User user, long homeId) {
                    Toast.makeText(activity.getApplicationContext(),
                            "Login success",
                            Toast.LENGTH_SHORT).show();
                    JSONObject successObj = new JSONObject();
                    try{
                        successObj.put("homeId", homeId);
                    }catch (Exception e){
                        LOG.d(TAG, "user_loginOrRegitserWithUID error = %s", e.toString());
                    }
                    sendPluginResult(callbackContext, successObj);
                }
                @Override
                public void onError(String code, String error) {
                    Toast.makeText(activity.getApplicationContext(),
                            "code: " + code + "error:" + error,
                            Toast.LENGTH_SHORT).show();
                    callbackContext.error(makeError(code,error));
                }
            });
        }catch (JSONException e){
            callbackContext.error(makeError((e)));
        }
    }


    public void home_listHomes(CordovaArgs args, CallbackContext callbackContext) throws  JSONException{
        try{
            TuyaHomeSdk.getHomeManagerInstance().queryHomeList(new ITuyaGetHomeListCallback() {
                @Override
                public void onSuccess(List<HomeBean> homeBeans) {
                    JSONArray homes = new JSONArray();
                    for(int i = 0 ; i < homeBeans.size(); i++){
                        HomeBean hb = homeBeans.get(i);
                        homes.put(hb.getHomeId());
                    }
                    sendPluginResult(callbackContext, homes);
                }

                @Override
                public void onError(String code, String error) {
                    callbackContext.error(makeError(code,error));
                }
            });
        }catch (Exception e){
            callbackContext.error(makeError(e));
        }
    }
    public void home_listDevices(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        long homeId = Long.parseLong(args.getString(0));
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                List<DeviceBean> deviceBeans = homeBean != null ? homeBean.getDeviceList() : null;
                ArrayList deviceList = (ArrayList) deviceBeans;
                Log.d(TAG, "onSuccess: List Home Devices : device length : " + deviceList.size());
                String deviceListResponse = JSON.toJSONString(deviceList);
                try{
                    JSONArray deviceListRespArray = new JSONArray(deviceListResponse);
                    sendPluginResult(callbackContext, deviceListRespArray);
                }catch(Exception e){
                    callbackContext.error(makeError(e));
                    return;
                }


            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                callbackContext.error(makeError(errorCode,errorMsg));
            }
        });
    }

    public void device_data(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        long homeId = Long.parseLong(args.getString(1));
        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
            @Override
            public void onSuccess(HomeBean homeBean) {
                List<DeviceBean> deviceBeans = homeBean != null ? homeBean.getDeviceList() : null;
                ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
                //ArrayList deviceList = (ArrayList) deviceBeans;
                for(DeviceBean device: deviceBeans){
                    if(device.getDevId().equalsIgnoreCase(devId)){
                        String devicedata = JSON.toJSONString(device);
                        PluginResult deviceresult = new PluginResult(PluginResult.Status.OK, devicedata);
                        deviceresult.setKeepCallback(true);
                        callbackContext.sendPluginResult(deviceresult);
                    }
                }

            }

            @Override
            public void onError(String errorCode, String errorMsg) {
                callbackContext.error(makeError(errorCode,errorMsg));
            }
        });
    }

    public void renameDevice(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        String deviceName = args.getString(1);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
        mDevice.renameDevice(deviceName, new IResultCallback() {
	@Override
	public void onError(String code, String error) {
		// Failed to rename the device.
        callbackContext.error(makeError(errorCode,errorMsg));
	}
	@Override
	public void onSuccess() {
		// The device is renamed successfully.
        PluginResult renameResult = new PluginResult(PluginResult.Status.OK, deviceName);
                renameResult.setKeepCallback(true);
                callbackContext.sendPluginResult(renameResult);
	}
});
    }

    public void removeDevice(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
        mDevice.removeDevice(new IResultCallback() {
	@Override
	public void onError(String errorCode, String errorMsg) {
         callbackContext.error(makeError(errorCode,errorMsg));
	}

	@Override
	public void onSuccess() {
          PluginResult removeResult = new PluginResult(PluginResult.Status.OK,devId );
                removeResult.setKeepCallback(true);
                callbackContext.sendPluginResult(removeResult);
	}
});
    }

      public void signalStrength(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
      mDevice.requestWifiSignal(new WifiSignalListener() {

	@Override
	public void onSignalValueFind(String signal) {
          PluginResult signalResult = new PluginResult(PluginResult.Status.OK,signal);
                signalResult.setKeepCallback(true);
                callbackContext.sendPluginResult(signalResult);
	}

	@Override
	public void onError(String errorCode, String errorMsg) {
         callbackContext.error(makeError(errorCode,errorMsg));
	}
});;
    }

    public void setDPs(CordovaArgs args, CallbackContext callbackContext) throws  JSONException{
        String devId = args.getString(0);
        String dps = args.getString(1);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                PluginResult dpUpdateResult = new PluginResult(PluginResult.Status.OK, dpStr);
                dpUpdateResult.setKeepCallback(true);
                callbackContext.sendPluginResult(dpUpdateResult);
            }
            @Override
            public void onRemoved(String devId) {

            }
            @Override
            public void onStatusChanged(String devId, boolean online) {

            }
            @Override
            public void onNetworkStatusChanged(String devId, boolean status) {

            }
            @Override
            public void onDevInfoUpdate(String devId) {

            }
        });
        mDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                //Log.d(TAG, "onSuccess12312",code);
                Log.e(TAG, "publishDps err " + code);
                callbackContext.error(makeError(code,error));
               //Toast.makeText(mContext, "Failed to switch on the light.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSucasdasd12323" );
                PluginResult dpUpdateResult = new PluginResult(PluginResult.Status.OK, dps);
                dpUpdateResult.setKeepCallback(true);
                callbackContext.sendPluginResult(dpUpdateResult);

                //Toast.makeText(mContext, "The light is switched on successfully.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void network_smartCameraConfiguration(CordovaArgs args, CallbackContext callbackContext) throws  JSONException {
        String ssid = args.getString(0);
        String pass = args.getString(1);
        long homeId = Long.parseLong(args.getString(2));
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token) {
                        //Create and show qrCode
                        TuyaCameraActivatorBuilder builder = new TuyaCameraActivatorBuilder()
                                .setToken(token)
                                .setPassword(pass)
                                .setTimeOut(100)
                                .setContext(activity)
                                .setSsid(ssid)
                                .setListener(new ITuyaSmartCameraActivatorListener() {
                                    @Override
                                    public void onQRCodeSuccess(String qrcodeUrl) {
                                        Toast.makeText(activity,qrcodeUrl,Toast.LENGTH_LONG).show();
                                        PluginResult qrCodeResult = new PluginResult(PluginResult.Status.OK, qrcodeUrl);
                                        qrCodeResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(qrCodeResult);
                                       // eventSendPluginResult( callbackContext, "Event 1", "Message 1");

                                        // Send qrCOdeUrl to cordovacallback.

                                    }

                                    @Override
                                    public void onError(String errorCode, String errorMsg) {
                                        PluginResult configResult = new PluginResult(PluginResult.Status.OK,errorMsg);
                                        configResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(configResult);
                                        eventSendPluginResult( callbackContext, "Event 1", "Message 1");
                                        Toast.makeText(activity,"config error!",Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void onActiveSuccess(DeviceBean devResp) {
                                        PluginResult configResult = new PluginResult(PluginResult.Status.OK, devResp.devId);
                                        configResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(configResult);
                                        eventSendPluginResult( callbackContext, "Event 1", "Message 1");
                                        Toast.makeText(activity,"config success!",Toast.LENGTH_LONG).show();
                                       // devResp.
                                    }
                                });

                        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
                        mTuyaActivator.createQRCode();
                        mTuyaActivator.start();
                        mTuyaActivator.stop();
                    }


                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        callbackContext.error(makeError("0", "Unknown error"));
                    }
                });

    }

    public void ipc_startCameraLivePlay(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            if (cameraInstance.isIPCDevice(devId)) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);

                Log.d(TAG, "startCameraLivePlay: " + devId);
                Intent intent = new Intent(activity, CameraPanelActivity.class);
                intent.putExtra(INTENT_DEV_ID, devId);
                Log.d(TAG, "startCameraLivePlay:  point 1");
                cordova.startActivityForResult(this,intent, 1000);
                Log.d(TAG, "startCameraLivePlay: Point 2");
                eventSendPluginResult( callbackContext, "Event 1", "Message 1");
                eventSendPluginResult( callbackContext, "Event 2", "Message 2");

            }
        }else{
            callbackContext.error(makeError("0", "Unknown error"));
        }
    }


    /**
     * Home Model Cache
     *
     * @author chuanfeng <a href="mailto:developer@tuya.com"/>
     * @since 2021/2/18 9:31 AM
     */
    public enum HomeModel {
        INSTANCE;

        public static final String CURRENT_HOME_ID = "currentHomeId";

        /**
         * Set current home's homeId
         */
        public final void setCurrentHome(Context context, long homeId) {
            SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();

            editor.putLong(CURRENT_HOME_ID, homeId);
            editor.apply();
        }

        /**
         * Get current home's homeId
         */
        public static final long getCurrentHome(Context context) {
            SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
            return sp.getLong(CURRENT_HOME_ID, 0);
        }

        /**
         * check if current home set
         */
        public final boolean checkHomeId(Context context) {
            return getCurrentHome(context) != 0L;
        }

        public final void clear(Context context) {
            SharedPreferences sp = context.getSharedPreferences("HomeModel", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.remove(CURRENT_HOME_ID);
            editor.apply();
        }
    }

    // ============================ Broadcast Receiver =====================//
    private void _broadcastRCV() {
        IntentFilter filter = new IntentFilter(CameraPanelActivity.BROADCAST_LISTENER);
        activity.registerReceiver(br, filter);
    }

    //============================= Helpers ===============================//

    private JSONObject makeError(Exception error){
        JSONObject resultObj = new JSONObject();
        try {
            resultObj.put("error", "JsonException");
            resultObj.put("message", error.getMessage());
        } catch (Exception e) {}

        return resultObj;
    }
    private JSONObject makeError(String code, String message){
        JSONObject resultObj = new JSONObject();
        try {
            resultObj.put("error", code);
            resultObj.put("message", message);
        } catch (Exception e) {}

        return resultObj;
    }

    private void sendPluginResult(CallbackContext callbackContext, boolean success){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, success);
        callbackContext.sendPluginResult(pluginResult);
    }
    private void sendPluginResult(CallbackContext callbackContext, String message){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, message);
        callbackContext.sendPluginResult(pluginResult);
    }
    private void sendPluginResult(CallbackContext callbackContext, JSONObject obj){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, obj);
        callbackContext.sendPluginResult(pluginResult);
    }
    private void sendPluginResult(CallbackContext callbackContext, JSONArray arr){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, arr);
        callbackContext.sendPluginResult(pluginResult);
    }

    private void eventSendPluginResult(CallbackContext callbackContext,String event, String data){
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, event);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }
}
