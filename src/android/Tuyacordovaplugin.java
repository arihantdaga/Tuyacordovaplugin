package com.arihant.tuyaplugin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.arihant.tuyaplugin.utils.MessageUtil;
import com.tuya.sdk.core.PluginManager;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.device.bean.UpgradeInfoBean;
import com.tuya.smart.android.network.Business;
import com.tuya.smart.android.network.http.BusinessResponse;
import com.tuya.smart.android.user.api.ILogoutCallback;
import com.tuya.smart.android.user.api.IUidLoginCallback;
import com.tuya.smart.android.user.api.IReNickNameCallback;
import com.tuya.smart.android.user.bean.User;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.home.sdk.builder.ActivatorBuilder;
import com.tuya.smart.interior.api.ITuyaPersonalCenterPlugin;
import com.tuya.smart.ipc.messagecenter.bean.CameraMessageBean;
import com.tuya.smart.sdk.api.IGetOtaInfoCallback;
import com.tuya.smart.sdk.api.IOtaListener;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaOta;
import com.tuya.smart.sdk.api.ITuyaSmartActivatorListener;
import com.tuya.smart.sdk.api.WifiSignalListener;
import com.tuya.smart.home.sdk.bean.HomeBean;
import com.tuya.smart.home.sdk.builder.TuyaCameraActivatorBuilder;
import com.tuya.smart.home.sdk.callback.ITuyaGetHomeListCallback;
import com.tuya.smart.home.sdk.callback.ITuyaHomeResultCallback;
import com.tuya.smart.sdk.api.ITuyaActivatorGetToken;
import com.tuya.smart.sdk.api.ITuyaCameraDevActivator;
import com.tuya.smart.sdk.api.ITuyaSmartCameraActivatorListener;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.sdk.bean.OTAErrorMessageBean;
import com.tuya.smart.sdk.enums.ActivatorModelEnum;

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

import static com.arihant.tuyaplugin.utils.Constants.INTENT_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DP_CONFIG;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_ITEM_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_PRIMARY_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_1;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_2;

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
        try {
            TuyaHomeSdk.onDestroy();
        } catch (Exception e) {
            //ignoring this too
        }
        this.sdkInitialized = false;
        try {
            activity.unregisterReceiver(br);
        } catch (IllegalArgumentException e) {
            //We can ignore this exception
        }
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

    public void home_initNotifications(CordovaArgs args, CallbackContext callbackContext) throws  JSONException{
        String pushToken = args.getString(0);
        LOG.d(TAG, "registerforNotifications: registerforNotifications is called");
        String pushProvider = "fcm";
        ITuyaPersonalCenterPlugin iTuyaPersonalCenterPlugin = (ITuyaPersonalCenterPlugin) PluginManager.service(ITuyaPersonalCenterPlugin.class);
        iTuyaPersonalCenterPlugin.getPushInstance().registerDevice(pushToken, pushProvider, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                callbackContext.error(makeError(code, error));
            }
            @Override
            public void onSuccess() {
                LOG.d(TAG, "registerforNotifications success: ");
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "OK");
                callbackContext.sendPluginResult(pluginResult);
            }
        });
    }

    public void user_logout(CordovaArgs args, CallbackContext callbackContext) {
        TuyaHomeSdk.getUserInstance().logout(new ILogoutCallback() {
            @Override
            public void onSuccess() {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "OK");
                callbackContext.sendPluginResult(pluginResult);
            }

            @Override
            public void onError(String code, String error) {
                callbackContext.error(makeError(code, error));
            }
        });
    }

    public void user_loginOrRegitserWithUID(CordovaArgs args, CallbackContext callbackContext){
        try{
            String countryCode = args.getString(0);
            String uid = args.getString(1);
            String pass = args.getString(2);
            TuyaHomeSdk.getUserInstance().loginOrRegisterWithUid (countryCode, uid, pass, true, new IUidLoginCallback(){
                @Override
                public void onSuccess(User user, long homeId) {
                    JSONObject successObj = new JSONObject();
                    if(TuyaHomeSdk.getUserInstance().getUser().getNickName() == null){
                            TuyaHomeSdk.getUserInstance().updateNickName(uid, new IReNickNameCallback() {
                                @Override
                                public void onSuccess() {
                                    LOG.d(TAG, "user_loginsetNickname = %s", TuyaHomeSdk.getUserInstance().getUser().getNickName());
                                }

                                @Override
                                public void onError(String code, String error) {
                                    LOG.d(TAG, "user_loginsetNickname error = %s", error);
                                }
                            });
                    }
                    try{
                        successObj.put("homeId", homeId);
                        TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                            @Override
                            public void onSuccess(HomeBean bean) {
                                sendPluginResult(callbackContext, successObj);
                            }

                            @Override
                            public void onError(String errorCode, String errorMsg) {
                                callbackContext.error(makeError(errorCode,errorMsg));
                            }
                        });
                    }catch (Exception e){
                        LOG.d(TAG, "user_loginOrRegitserWithUID error = %s", e.toString());
                    }
                }
                @Override
                public void onError(String code, String error) {
                    callbackContext.error(makeError(code,error));
                }
            });
        }catch (JSONException e){
            callbackContext.error(makeError(e));
        }
    }

    public void user_isLoggedIn(CordovaArgs args, CallbackContext callbackContext) {
        try {
            boolean isLoggedIn = TuyaHomeSdk.getUserInstance().isLogin();
            JSONObject jsonObj = new JSONObject();
            jsonObj.put("status", isLoggedIn);
            sendPluginResult(callbackContext, jsonObj);
        } catch (JSONException e){
            callbackContext.error(makeError(e));
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
                // Log.d(TAG,  JSON.toJSONString(deviceList.get(0)));
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

    public void home_initHome(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        long homeId = Long.parseLong(args.getString(0));
        try {
            TuyaHomeSdk.newHomeInstance(homeId).getHomeDetail(new ITuyaHomeResultCallback() {
                @Override
                public void onSuccess(HomeBean bean) {
                    PluginResult successResult = new PluginResult(PluginResult.Status.OK, "success");
                    callbackContext.sendPluginResult(successResult);
                }

                @Override
                public void onError(String errorCode, String errorMsg) {
                    callbackContext.error(makeError(errorCode,errorMsg));
                }
            });
        } catch (Exception e){
            LOG.d(TAG, "home_initHome error = %s", e.toString());
        }
    }

    public void device_data(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        long homeId = Long.parseLong(args.getString(1));
        DeviceBean mDevBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        String devicedata = JSON.toJSONString(mDevBean);
        PluginResult deviceresult = new PluginResult(PluginResult.Status.OK, devicedata);
        callbackContext.sendPluginResult(deviceresult);
    }

    public void firmwareUpdate(CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        String devId = args.getString(0);
        ITuyaOta iTuyaOta = TuyaHomeSdk.newOTAInstance(devId);

        iTuyaOta.setOtaListener(new IOtaListener() {
            @Override
            public void onSuccess(int otaType) {
                iTuyaOta.onDestroy();
                Log.i(TAG, "upgrade success");
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("progress", 100);
                    resultObj.put("status", 3);
                } catch (Exception e) {
                    LOG.d(TAG, "error = %s", e.toString());
                }
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
                pluginResult.setKeepCallback(false);
                callbackContext.sendPluginResult(pluginResult);
            }

            @Override
            public void onFailure(int otaType, String code, String error) {
                iTuyaOta.onDestroy();
                callbackContext.error(makeError(code, "Upgrade failed, try again"));
            }

            @Override
            public void onFailureWithText(int otaType, String code, OTAErrorMessageBean messageBean) {
                iTuyaOta.onDestroy();
                callbackContext.error(makeError(code, "Upgrade failed - "+messageBean.text));
            }

            @Override
            public void onProgress(int otaType, int progress) {
                Log.i(TAG, "upgrade progress = " + progress);
                JSONObject resultObj = new JSONObject();
                try {
                    resultObj.put("progress", progress);
                    resultObj.put("status", 2);
                } catch (Exception e) {
                    LOG.d(TAG, "error = %s", e.toString());
                }
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
                pluginResult.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult);
            }

            @Override
            public void onTimeout(int otaType) {
                iTuyaOta.onDestroy();
                callbackContext.error(makeError("TO1", "Upgrade timed out, please check internet connection and try again"));
            }

            @Override
            public void onStatusChanged(int otaStatus, int otaType) {

            }
        });

        iTuyaOta.getOtaInfo(new IGetOtaInfoCallback() {
            @Override
            public void onSuccess(List<UpgradeInfoBean> upgradeInfoBeans) {
                if (hasHardwareUpdate(upgradeInfoBeans)) {
                    //Starts an OTA update.
                    iTuyaOta.startOta();
                    JSONObject resultObj = new JSONObject();
                    try {
                        resultObj.put("status", 1);
                    } catch (Exception e) {
                        LOG.d(TAG, "error = %s", e.toString());
                    }
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, resultObj);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                }
            }

            @Override
            public void onFailure(String code, String error) {
                callbackContext.error(makeError(code, "Unable to get OTA Update info, please try again"));
            }
        });

    }

    private boolean hasHardwareUpdate(List<UpgradeInfoBean> list) {
        if (null == list || list.size() == 0) {
            return false;
        }
        for (UpgradeInfoBean upgradeInfoBean : list) {
            if (upgradeInfoBean != null && upgradeInfoBean.getUpgradeStatus() == 1) {
                return true;
            }
        }
        return false;
    }

    public void renameDevice(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        String deviceName = args.getString(1);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);
        mDevice.renameDevice(deviceName, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                // Failed to rename the device.
                callbackContext.error(makeError(code, error));
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
        });
    }


    public void setDPs(CordovaArgs args, CallbackContext callbackContext) throws  JSONException{
        String devId = args.getString(0);
        String dps = args.getString(1);
        ITuyaDevice mDevice = TuyaHomeSdk.newDeviceInstance(devId);

        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                PluginResult dpUpdateResult = new PluginResult(PluginResult.Status.OK, dpStr);
                callbackContext.sendPluginResult(dpUpdateResult);
                mDevice.onDestroy();
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
                mDevice.onDestroy();
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSucasdasd12323" );
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
                                        JSONObject resultObj = new JSONObject();
                                        try {
                                            resultObj.put("status", "qr");
                                            resultObj.put("qrCode", qrcodeUrl);
                                        } catch (Exception e) {
                                            LOG.d(TAG, "error = %s", e.toString());
                                        }
                                        PluginResult qrCodeResult = new PluginResult(PluginResult.Status.OK, resultObj);
                                        qrCodeResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(qrCodeResult);

                                    }

                                    @Override
                                    public void onError(String errorCode, String errorMsg) {
                                        JSONObject resultObj = new JSONObject();
                                        try {
                                            resultObj.put("code", errorCode);
                                            resultObj.put("message", errorMsg);
                                        } catch (Exception e) {
                                            LOG.d(TAG, "error = %s", e.toString());
                                        }
                                        PluginResult configResult = new PluginResult(PluginResult.Status.ERROR, resultObj);
                                        callbackContext.sendPluginResult(configResult);
                                        mTuyaActivator.stop();
                                    }

                                    @Override
                                    public void onActiveSuccess(DeviceBean devResp) {
                                        JSONObject resultObj = new JSONObject();
                                        try {
                                            resultObj.put("status", "success");
                                            resultObj.put("deviceId", devResp.devId);
                                            resultObj.put("mac", devResp.uuid);
                                            resultObj.put("deviceName", devResp.getName());
                                        } catch (Exception e) {
                                            LOG.d(TAG, "error = %s", e.toString());
                                        }
                                        PluginResult configResult = new PluginResult(PluginResult.Status.OK, resultObj);
                                        configResult.setKeepCallback(true);
                                        callbackContext.sendPluginResult(configResult);
                                        mTuyaActivator.stop();
                                    }
                                });

                        mTuyaActivator = TuyaHomeSdk.getActivatorInstance().newCameraDevActivator(builder);
                        mTuyaActivator.createQRCode();
                        mTuyaActivator.start();
                    }


                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        callbackContext.error(makeError("0", "Unknown error"));
                    }
                });

    }
    public void network_startEasyConfiguration(CordovaArgs args, CallbackContext callbackContext) throws  JSONException {
        String ssid = args.getString(0);
        String pass = args.getString(1);
        long homeId = Long.parseLong(args.getString(2));
        TuyaHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                new ITuyaActivatorGetToken() {
                    @Override
                    public void onSuccess(String token) {
                         ActivatorBuilder builder = new ActivatorBuilder()
                        .setSsid(ssid)
                        .setContext(activity.getApplicationContext())
                        .setPassword(pass)
                        .setActivatorModel(ActivatorModelEnum.TY_EZ)
                        .setTimeOut(120)
                        .setToken(token)
                        .setListener(new ITuyaSmartActivatorListener() {

                            @Override
                            public void onError(String errorCode, String errorMsg) {
                                JSONObject resultObj = new JSONObject();
                                try {
                                    resultObj.put("code", errorCode);
                                    resultObj.put("message", errorMsg);
                                } catch (Exception e) {
                                    LOG.d(TAG, "error = %s", e.toString());
                                }
                                PluginResult configResult = new PluginResult(PluginResult.Status.ERROR, resultObj);
                                callbackContext.sendPluginResult(configResult);
                                mTuyaActivator.stop();
                            }

                            @Override
                            public void onActiveSuccess(DeviceBean devResp) {
                               JSONObject resultObj = new JSONObject();
                                try {
                                    resultObj.put("status", "success");
                                    resultObj.put("deviceId", devResp.devId);
                                    resultObj.put("mac", devResp.uuid);
                                    resultObj.put("deviceName", devResp.getName());
                                } catch (Exception e) {
                                    LOG.d(TAG, "error = %s", e.toString());
                                }
                                PluginResult configResult = new PluginResult(PluginResult.Status.OK, resultObj);
                                configResult.setKeepCallback(true);
                                callbackContext.sendPluginResult(configResult);
                                mTuyaActivator.stop();
                            }

                            @Override
                            public void onStep(String step, Object data) {
                                // Log.d(TAG, "Step: " + step + " ");
                            }
                        });

                        mTuyaActivator = (ITuyaCameraDevActivator) TuyaHomeSdk.getActivatorInstance().newMultiActivator(builder);
                        mTuyaActivator.start();
                    }


                    @Override
                    public void onFailure(String errorCode, String errorMsg) {
                        callbackContext.error(makeError("0", "Unknown error"));
                    }
                });

    }


    public void ipc_startCameraLivePlay(CordovaArgs args, CallbackContext callbackContext) throws JSONException{
        String devId = args.getString(0);
        String bgColor = args.getString(1);
        String primaryColor = args.getString(2);
        String itemBgColor = args.getString(3);
        String textColor1 = args.getString(4);
        String textColor2 = args.getString(5);
        String dpConfig = args.getString(6);
        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            if (cameraInstance.isIPCDevice(devId)) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK);
                callbackContext.sendPluginResult(pluginResult);

                Log.d(TAG, "startCameraLivePlay: " + devId);
                Intent intent = new Intent(activity, CameraPanelActivity.class);
                intent.putExtra(INTENT_DEV_ID, devId);
                intent.putExtra(INTENT_BG_COLOR, bgColor);
                intent.putExtra(INTENT_PRIMARY_COLOR, primaryColor);
                intent.putExtra(INTENT_DP_CONFIG, dpConfig);
                intent.putExtra(INTENT_ITEM_BG_COLOR, itemBgColor);
                intent.putExtra(INTENT_TEXT_COLOR_1, textColor1);
                intent.putExtra(INTENT_TEXT_COLOR_2, textColor2);
                cordova.setActivityResultCallback (this);
                cordova.startActivityForResult(this,intent, 1000);
            }
        }else{
            callbackContext.error(makeError("0", "Unknown error"));
        }
    }

    public void ipc_getImagesOnMotionDetection(CordovaArgs args, CallbackContext callbackContext) {
        try {
            String devId = args.getString(0);
            long startTime = Long.parseLong(args.getString(1));
            long endTime = Long.parseLong(args.getString(2));
            int limit = Integer.parseInt(args.getString(3));
            int offset = Integer.parseInt(args.getString(4));
            JSONObject object = new JSONObject();
            object.put("msgSrcId", devId);
            object.put("startTime", startTime);
            object.put("endTime", endTime);
            object.put("msgType", 4);
            object.put("limit", limit);
            object.put("keepOrig", true);
            object.put("offset", offset);
//        if (null != selectClassify) {
//            object.put("msgCodes", selectClassify.getMsgCode());
//        }
            TuyaIPCSdk.getMessage().createMsgManager().getAlarmDetectionMessageList(JSON.toJSONString(object), new Business.ResultListener() {
                @Override
                public void onFailure(BusinessResponse businessResponse, Object o, String s) {
                    callbackContext.error(makeError("101", "Unknown error"));
                }

                @Override
                public void onSuccess(BusinessResponse businessResponse, Object o, String s) {
                    List<CameraMessageBean> msgList;
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, o.toString());
                    callbackContext.sendPluginResult(pluginResult);
                }

            });
        } catch (Exception e) {
            callbackContext.error(makeError("103", "Unknown error"));
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
