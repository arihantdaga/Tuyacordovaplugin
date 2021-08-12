package com.arihant.tuyaplugin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.arihant.tuyaplugin.utils.Constants;
import com.arihant.tuyaplugin.utils.DPConstants;
import com.arihant.tuyaplugin.utils.MessageUtil;
import com.arihant.tuyaplugin.utils.ToastUtil;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCDoorbell;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OnRenderDirectionCallback;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;
import com.tuya.smart.camera.utils.permission.PermissionChecker;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.bean.DeviceBean;
import com.tuya.smart.camera.utils.IPCCameraUtils;


import org.apache.cordova.LOG;
import org.json.JSONObject;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Map;


import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_FAIL;
import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DP_CONFIG;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_ITEM_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_PRIMARY_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_1;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_2;
import static com.arihant.tuyaplugin.utils.Constants.MSG_CONNECT;
import static com.arihant.tuyaplugin.utils.Constants.MSG_GET_VIDEO_CLARITY;
import static com.arihant.tuyaplugin.utils.Constants.MSG_MUTE;
import static com.arihant.tuyaplugin.utils.Constants.MSG_SCREENSHOT;
import static com.arihant.tuyaplugin.utils.Constants.MSG_SET_CLARITY;
import static com.arihant.tuyaplugin.utils.Constants.MSG_TALK_BACK_BEGIN;
import static com.arihant.tuyaplugin.utils.Constants.MSG_TALK_BACK_OVER;
import static com.arihant.tuyaplugin.utils.Constants.MSG_VIDEO_RECORD_BEGIN;
import static com.arihant.tuyaplugin.utils.Constants.MSG_VIDEO_RECORD_FAIL;
import static com.arihant.tuyaplugin.utils.Constants.MSG_VIDEO_RECORD_OVER;
import static com.arihant.tuyaplugin.utils.Constants.SETTING_ACTIVITY_REQ_CODE;

public class CameraPanelActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "Tuyacordovaplugin";
    public static final String BROADCAST_LISTENER = "com.arihant.tuyaplugin.Listener";

    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private TextView qualityTv;
    private RelativeLayout mMainLayout;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt, settingTxt, cloudStorageTxt, messageCenterTxt, deviceInfoTxt,batteryTxt;

    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 10;
    private boolean isSpeaking = false;
    private boolean isRecording = false;
    private boolean isPlay = false;
    private int previewMute = ICameraP2P.MUTE;
    private int videoClarity = ICameraP2P.HD;
    private String currVideoClarity;
    private String batteryLevel;

    private String picPath, videoPath;

    private int p2pType;

    private boolean dontPause = false;

    private String devId;
    private String bgColor;
    private String primaryColor;
    private String itemBgColor;
    private String textColor1;
    private String textColor2;
    private ITuyaSmartCameraP2P mCameraP2P;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(_getResource("activity_camera_panel", "layout"));
        devId = getIntent().getStringExtra(INTENT_DEV_ID);
        initView();
        initData();
        initListener();
        getBatteryLevel();
        if (querySupportByDPID(DPConstants.PTZ_CONTROL)) {
            mVideoView.setOnRenderDirectionCallback(new OnRenderDirectionCallback() {

                @Override
                public void onLeft() {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_LEFT);
                }

                @Override
                public void onRight() {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_RIGHT);
                }

                @Override
                public void onUp() {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_UP);
                }

                @Override
                public void onDown() {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_DOWN);
                }

                @Override
                public void onCancel() {
                    publishDps(DPConstants.PTZ_STOP, true);
                }
            });
        }


    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (reqCode == SETTING_ACTIVITY_REQ_CODE) {
            dontPause = false;
        }
    }

    private Handler mHandler = new Handler() {
        @SuppressLint("HandlerLeak")
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_CONNECT:
                    handleConnect(msg);
                    break;
                case MSG_SET_CLARITY:
                    handleClarity(msg);
                    break;
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_SCREENSHOT:
                    handlesnapshot(msg);
                    break;
                case MSG_VIDEO_RECORD_BEGIN:
                    ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
                    break;
                case MSG_VIDEO_RECORD_FAIL:
                    ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
                    break;
                case MSG_VIDEO_RECORD_OVER:
                    handleVideoRecordOver(msg);
                    break;
                case MSG_TALK_BACK_BEGIN:
                    handleStartTalk(msg);
                    break;
                case MSG_TALK_BACK_OVER:
                    handleStopTalk(msg);
                    break;
                case MSG_GET_VIDEO_CLARITY:
                    handleGetVideoClarity(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void initView() {

        String battery_val;
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        Map<String, Object> dps = deviceBean.getDps();
        Object battery_value = dps.get(DPConstants.BATTERY);
        battery_val = JSON.toJSONString(battery_value);
//        toolbar = findViewById(_getResource("toolbar_view", "id"));
////        setSupportActionBar(toolbar);
//        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onBackPressed();
//            }
//        });
        bgColor = getIntent().getStringExtra(INTENT_BG_COLOR);
        primaryColor = getIntent().getStringExtra(INTENT_PRIMARY_COLOR);
        itemBgColor = getIntent().getStringExtra(INTENT_ITEM_BG_COLOR);
        textColor1 = getIntent().getStringExtra(INTENT_TEXT_COLOR_1);
        textColor2 = getIntent().getStringExtra(INTENT_TEXT_COLOR_2);
        mVideoView = findViewById(_getResource("camera_video_view", "id"));
        muteImg = findViewById(_getResource("camera_mute", "id"));
        qualityTv = findViewById(_getResource("camera_quality", "id"));
        batteryTxt = findViewById(_getResource("battery","id"));
        batteryTxt.setVisibility(View.INVISIBLE);
        speakTxt = findViewById(_getResource("speak_Txt", "id"));
        recordTxt = findViewById(_getResource("record_Txt", "id"));
        photoTxt = findViewById(_getResource("photo_Txt", "id"));
        replayTxt = findViewById(_getResource("replay_Txt", "id"));
        ((ViewGroup) replayTxt.getParent()).removeView(replayTxt);
        settingTxt = findViewById(_getResource("setting_Txt", "id"));
        settingTxt.setOnClickListener(this);
//        ((ViewGroup) settingTxt.getParent()).removeView(settingTxt);
        deviceInfoTxt = findViewById(_getResource("info_Txt", "id"));
        deviceInfoTxt.setOnClickListener(this);
        ((ViewGroup) deviceInfoTxt.getParent()).removeView(deviceInfoTxt);
//        findViewById(_getResource("get_clarity_Txt", "id")).setOnClickListener(this);
        cloudStorageTxt = findViewById(_getResource("cloud_Txt", "id"));
        ((ViewGroup) cloudStorageTxt.getParent()).removeView(cloudStorageTxt);
        messageCenterTxt = findViewById(_getResource("message_center_Txt", "id"));
        ((ViewGroup) messageCenterTxt.getParent()).removeView(messageCenterTxt);
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
//        layoutParams.addRule(RelativeLayout.BELOW, _getResource("toolbar_view", "id"));
        findViewById(_getResource("camera_video_view_Rl", "id")).setLayoutParams(layoutParams);
        batteryTxt.setText(battery_val);
        batteryTxt.setTextColor(Color.parseColor(primaryColor));
        mMainLayout = findViewById(_getResource("main_layout", "id"));
        mMainLayout.setBackgroundColor(Color.parseColor(bgColor));
        muteImg.setSelected(true);
    }

    private void initData() {
        ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
        if (cameraInstance != null) {
            mCameraP2P = cameraInstance.createCameraP2P(devId);
            p2pType = cameraInstance.getP2PType(devId);
        }
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (null != mCameraP2P) {
                    mCameraP2P.generateCameraView(o);
                }
            }
        });
        mVideoView.createVideoView(p2pType);
        if (null == mCameraP2P) {
            showNotSupportToast();
        } else {

        }
    }

    private void showNotSupportToast() {
        ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("not_support_device", "string")));
    }

    private void preview() {
        mCameraP2P.startPreview(videoClarity, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                Log.d(TAG, "start preview onSuccess");
                isPlay = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                isPlay = false;
            }
        });
    }

    private void initListener() {
        if (mCameraP2P == null) {
            return;
        }

        muteImg.setOnClickListener(this);
        qualityTv.setOnClickListener(this);
        speakTxt.setOnClickListener(this);
        recordTxt.setOnClickListener(this);
        photoTxt.setOnClickListener(this);
        replayTxt.setOnClickListener(this);
        settingTxt.setOnClickListener(this);
        cloudStorageTxt.setOnClickListener(this);
        messageCenterTxt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == _getResource("camera_mute", "id")) {
            muteClick();
        } else if (id == _getResource("camera_quality", "id")) {
            setVideoClarity();
        } else if (id == _getResource("speak_Txt", "id")) {
            speakClick();
        } else if (id == _getResource("record_Txt", "id")) {
            recordClick();
        } else if (id == _getResource("photo_Txt", "id")) {
            snapShotClick();
        } else if (id == _getResource("replay_Txt", "id")) {
            Toast.makeText(this, "Yet to be Implemented", Toast.LENGTH_SHORT).show();
            /*

            Intent intent = new Intent(CameraPanelActivity.this, CameraPlaybackActivity.class);
            intent.putExtra(INTENT_P2P_TYPE, p2pType);
            intent.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent);*/
        } else if (id == _getResource("setting_Txt", "id")) {
            Intent intent1 = new Intent(CameraPanelActivity.this, CameraSettingActivity.class);
            intent1.putExtra(INTENT_DEV_ID, devId);
            intent1.putExtra(INTENT_DP_CONFIG, getIntent().getStringExtra(INTENT_DP_CONFIG));
            intent1.putExtra(INTENT_BG_COLOR, bgColor);
            intent1.putExtra(INTENT_PRIMARY_COLOR, primaryColor);
            intent1.putExtra(INTENT_ITEM_BG_COLOR, itemBgColor);
            intent1.putExtra(INTENT_TEXT_COLOR_1, textColor1);
            intent1.putExtra(INTENT_TEXT_COLOR_2, textColor2);
            dontPause = true;
            startActivityForResult(intent1, SETTING_ACTIVITY_REQ_CODE);
        } else if (id == _getResource("cloud_Txt", "id")) {
            Toast.makeText(this, "Yet to be Implemented", Toast.LENGTH_SHORT).show();

            /*if (p2pType == SDK_PROVIDER_V1) {
                showNotSupportToast();
                return;
            }
            Intent intent2 = new Intent(CameraPanelActivity.this, CameraCloudStorageActivity.class);
            intent2.putExtra(INTENT_DEV_ID, devId);
            intent2.putExtra(INTENT_P2P_TYPE, p2pType);
            startActivity(intent2);*/
        } else if (id == _getResource("message_center_Txt", "id")) {
            Toast.makeText(this, "Yet to be Implemented", Toast.LENGTH_SHORT).show();

            /*Intent intent3 = new Intent(CameraPanelActivity.this, AlarmDetectionActivity.class);
            intent3.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent3);*/
        } else if (id == _getResource("info_Txt", "id")) {
            Toast.makeText(this, "Yet to be Implemented", Toast.LENGTH_SHORT).show();

            /*
            Intent intent4 = new Intent(CameraPanelActivity.this, CameraInfoActivity.class);
            intent4.putExtra(INTENT_DEV_ID, devId);
            startActivity(intent4);*/
        } else if (id == _getResource("get_clarity_Txt", "id")) {
            if (mCameraP2P != null) {
                mCameraP2P.getVideoClarity(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        currVideoClarity = s;
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_VIDEO_CLARITY, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_GET_VIDEO_CLARITY, ARG1_OPERATE_FAIL));
                    }
                });
            }
        }
    }
    private void recordClick() {
        if (!isRecording) {
            ToastUtil.shortToast(CameraPanelActivity.this, "Enter Recording");
            if (PermissionChecker.hasStoragePermission()) {
                String picPath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    picPath = IPCCameraUtils.recordPathSupportQ(devId);
                } else {
                    picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Camera/";
                    File file = new File(picPath);
                    if (!file.exists()) {
                        file.mkdirs();
                    }
                }

                String fileName = System.currentTimeMillis() + ".mp4";
                videoPath = picPath + fileName;
                mCameraP2P.startRecordLocalMp4(picPath, CameraPanelActivity.this, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isRecording = true;
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_BEGIN);

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        mHandler.sendEmptyMessage(MSG_VIDEO_RECORD_FAIL);
                    }
                });
                recordStatue(true);
            } else {
                PermissionChecker.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 3782, _getResource("pps_external_storage", "string"));
            }
        } else {
            mCameraP2P.stopRecordLocalMp4(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isRecording = false;
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_FAIL));
                }
            });
            recordStatue(false);
        }
    }


    private void snapShotClick() {
        if (PermissionChecker.hasStoragePermission()) {
            ToastUtil.shortToast(CameraPanelActivity.this, "Enter Snapshot");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String path = IPCCameraUtils.recordPathSupportQ(devId);
                picPath = path;
            } else {
                String path = IPCCameraUtils.recordSnapshotPath(devId);
                File file = new File(path);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }

            mCameraP2P.snapshot(picPath, CameraPanelActivity.this, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_FAIL));
                }
            });
        } else {
            PermissionChecker.requestPermission(CameraPanelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 8132, _getResource("pps_external_storage", "string"));
        }
    }

    private void muteClick() {
        int mute;
        mute = previewMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                previewMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void setSpeakIconColor() {
        TextView tv = (TextView) findViewById(_getResource("speak_Txt", "id"));
        if (isSpeaking) {
            tv.setTextColor(Color.parseColor(primaryColor));
        } else {
            tv.setTextColor(ContextCompat.getColor(tv.getContext(),  _getResource("camera_panel_control_color", "color")));
        }
    }

    private void speakClick() {
        if (isSpeaking) {
            mCameraP2P.stopAudioTalk(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    isSpeaking = false;
                    setSpeakIconColor();
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_SUCCESS));
                }

                @Override
                public void onFailure(int sessionId, int requestId, int errCode) {
                    isSpeaking = false;
                    setSpeakIconColor();
                    mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_OVER, ARG1_OPERATE_FAIL));

                }
            });
        } else {
            if (Constants.hasRecordPermission()) {
                mCameraP2P.startAudioTalk(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isSpeaking = true;
                        setSpeakIconColor();
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        isSpeaking = false;
                        setSpeakIconColor();
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_TALK_BACK_BEGIN, ARG1_OPERATE_FAIL));
                    }
                });
            } else {
                Constants.requestPermission(CameraPanelActivity.this, Manifest.permission.RECORD_AUDIO, Constants.EXTERNAL_AUDIO_REQ_CODE, "open_recording");
            }
        }
    }

    private void setVideoClarity() {
        mCameraP2P.setVideoClarity(videoClarity == ICameraP2P.HD ? ICameraP2P.STANDEND : ICameraP2P.HD, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                videoClarity = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SET_CLARITY, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_SET_CLARITY, ARG1_OPERATE_FAIL));
            }
        });
    }

    private void recordStatue(boolean isRecording) {
        speakTxt.setEnabled(!isRecording);
        photoTxt.setEnabled(!isRecording);
        replayTxt.setEnabled(!isRecording);
        recordTxt.setEnabled(true);
        recordTxt.setSelected(isRecording);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        //must register again,or can't callback
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            mCameraP2P.generateCameraView(mVideoView.createdView());
            if (mCameraP2P.isConnecting()) {
                mCameraP2P.startPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlay = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        Log.d(TAG, "start preview onFailure, errCode: " + errCode);
                    }
                });
            }
            if (!mCameraP2P.isConnecting()) {
                ITuyaIPCCore cameraInstance = TuyaIPCSdk.getCameraInstance();
                if (cameraInstance != null && cameraInstance.isLowPowerDevice(devId)) {
                    ITuyaIPCDoorbell doorbell = TuyaIPCSdk.getDoorbell();
                    if (doorbell != null) {
                        doorbell.wirelessWake(devId);
                    }
                }
                mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_SUCCESS));
                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {
                        mHandler.sendMessage(MessageUtil.getMessage(MSG_CONNECT, ARG1_OPERATE_FAIL));
                    }
                });
            }
        }
    }

    private AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveSpeakerEchoData(ByteBuffer pcm, int sampleRate) {
            if (null != mCameraP2P) {
                int length = pcm.capacity();
                L.d(TAG, "receiveSpeakerEchoData pcmlength " + length + " sampleRate " + sampleRate);
                byte[] pcmData = new byte[length];
                pcm.get(pcmData, 0, length);
                mCameraP2P.sendAudioTalkData(pcmData, length);
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (null != mCameraP2P && !dontPause) {
            if (isSpeaking) {
                mCameraP2P.stopAudioTalk(null);
            }
            if (isPlay) {
                mCameraP2P.stopPreview(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {

                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
                isPlay = false;
            }
            mCameraP2P.removeOnP2PCameraListener();
            mCameraP2P.disconnect(new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }
        AudioUtils.changeToNomal(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mCameraP2P) {
            mCameraP2P.destroyP2P();
        }
    }


    private boolean querySupportByDPID(String dpId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            return dps != null && dps.get(dpId) != null;
        }
        return false;
    }
    private String getBatteryLevel() {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            if (dps.containsKey(DPConstants.POWER_SOURCE)) {
                String bStatus = (String) dps.get(DPConstants.POWER_SOURCE);
                if (bStatus.equals(DPConstants.POWER_SOURCE_BATTERY)) {
                    batteryTxt.setVisibility(View.VISIBLE);
                    Object res = dps.get(DPConstants.BATTERY);
                    batteryLevel = JSON.toJSONString(res);
                    batteryTxt.setText("Battery : "+batteryLevel+" %");
                } else if (bStatus.equals(DPConstants.POWER_SOURCE_PLUG_SUPPLY)){
                    batteryTxt.setVisibility(View.VISIBLE);
                    batteryTxt.setText("Plugged in Power");
                } else {
                    batteryTxt.setVisibility(View.INVISIBLE);
                }
            }
        }
        return null;
    }

    private ITuyaDevice iTuyaDevice;

    private void publishDps(String dpId, Object value) {
        if (iTuyaDevice == null) {
            iTuyaDevice = TuyaHomeSdk.newDeviceInstance(devId);
        }
        com.alibaba.fastjson.JSONObject jsonObject = new com.alibaba.fastjson.JSONObject();
        jsonObject.put(dpId, value);
        String dps = jsonObject.toString();
        iTuyaDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "publishDps err " + dps);
            }

            @Override
            public void onSuccess() {
                Log.i(TAG, "publishDps suc " + dps);
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_camera_panel, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == _getResource("menu_remove_device", "id")) {
//            AlertDialog dialog = new AlertDialog.Builder(this)
//                    .setCancelable(true)
//                    .setTitle(getString(_getResource("remove_device_dialog", "string")))
//                    .setPositiveButton(getString(_getResource("confirm", "string")), (dialog1, which) ->
//                            unBindDevice())
//                    .create();
//            dialog.show();
//        }
//        return super.onOptionsItemSelected(item);

        return false;
    }

    private void unBindDevice() {
        TuyaHomeSdk.newDeviceInstance(devId).removeDevice(new IResultCallback() {
            @Override
            public void onError(String s, String s1) {
                ToastUtil.shortToast(CameraPanelActivity.this, s1);
            }

            @Override
            public void onSuccess() {
                mHandler.removeCallbacksAndMessages(null);
                CameraPanelActivity.this.finish();
            }
        });
    }



    private void handleStopTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
//            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }

    private void handleStartTalk(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
//            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }

    private void handleVideoRecordOver(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
//            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }

    private void handlesnapshot(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(previewMute == ICameraP2P.MUTE);
//            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_suc", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }


    private void handleClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            qualityTv.setText(videoClarity == ICameraP2P.HD ? getString(_getResource("hd", "string")) : getString(_getResource("sd", "string")));
            //ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }

    private void handleConnect(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            preview();
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("connect_failed", "string")));
        }
    }

    private void handleGetVideoClarity(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS && !TextUtils.isEmpty(currVideoClarity)) {
            String info = getString(_getResource("other", "string"));
            if (currVideoClarity.equals(String.valueOf(ICameraP2P.HD))) {
                info = getString(_getResource("hd", "string"));
            } else if (currVideoClarity.equals(String.valueOf(ICameraP2P.STANDEND))) {
                info = getString(_getResource("sd", "string"));
            }
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("get_current_clarity", "string")) + info);
        } else {
            ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("operation_failed", "string")));
        }
    }


    /**
     * Resource ID
     *
     * @param name
     * @param type layout, drawable, id
     * @return
     */
    private int _getResource(String name, String type) {
        String package_name = getApplication().getPackageName();
        Resources resources = getApplication().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

    private void _sendBroadCast(String methodName) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        activity.sendBroadcast(intent);
    }

    private void _sendBroadCast(String methodName, JSONObject object) {
        Intent intent = new Intent();
        intent.setAction(BROADCAST_LISTENER);
        intent.putExtra("method", methodName);
        intent.putExtra("data", object.toString());
        activity.sendBroadcast(intent);
    }

}