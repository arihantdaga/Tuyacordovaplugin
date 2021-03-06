package com.arihant.tuyaplugin.utils;

import android.app.Application;
import android.widget.Toast;

import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCDoorBellManager;
import com.tuya.smart.android.camera.sdk.bean.TYDoorBellCallModel;
import com.tuya.smart.android.camera.sdk.callback.TuyaSmartDoorBellObserver;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.sdk.bean.DeviceBean;

//import com.tuya.smart.android.demo.camera.CameraDoorBellActivity;

/**
 * Created by HuangXin on 2/20/21.
 */
public final class CameraDoorbellManager {
    private static final String TAG = "CameraDoorbellManager";
    public static final String EXTRA_AC_DOORBELL = "ac_doorbell";

    ITuyaIPCDoorBellManager doorBellInstance = TuyaIPCSdk.getDoorbell().getIPCDoorBellManagerInstance();

    private CameraDoorbellManager() {
    }

    public static CameraDoorbellManager getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        static final CameraDoorbellManager INSTANCE = new CameraDoorbellManager();
    }

    public void init(Application application) {
        if (doorBellInstance != null) {
            doorBellInstance.addObserver(new TuyaSmartDoorBellObserver() {
                @Override
                public void doorBellCallDidReceivedFromDevice(TYDoorBellCallModel callModel, DeviceBean deviceBean) {
                    L.d(TAG, "Receiving a doorbell call");
                    if (null == callModel) {
                        return;
                    }
                    String type = callModel.getType();
                    String messageId = callModel.getMessageId();
                    Toast.makeText(application.getApplicationContext(), "Doorbellactivity yet to be implemented", Toast.LENGTH_SHORT);
                    /*if (EXTRA_AC_DOORBELL.equals(type)) {
                        Intent intent = new Intent(application.getApplicationContext(), CameraDoorBellActivity.class);
                        intent.putExtra(INTENT_MSGID, messageId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        application.getApplicationContext().startActivity(intent);
                    }*/
                }
            });
        }
    }

    public void deInit() {
        if (doorBellInstance != null) {
            doorBellInstance.removeAllObservers();
        }
    }
}
