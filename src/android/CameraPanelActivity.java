package com.arihant.tuyaplugin;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSON;
import com.arihant.tuyaplugin.utils.Constants;
import com.arihant.tuyaplugin.utils.DPConstants;
import com.arihant.tuyaplugin.utils.MessageUtil;
import com.arihant.tuyaplugin.utils.ToastUtil;
import com.tuya.smart.android.camera.sdk.TuyaIPCSdk;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCCore;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCDoorbell;
import com.tuya.smart.android.camera.sdk.api.ITuyaIPCPTZ;
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


import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map;


import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_FAIL;
import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_P2P_TYPE;
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
    private static Context sContext;

    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private TextView qualityTv;
    private RelativeLayout mMainLayout;
    private FrameLayout  progressOverlay;
    private TextView speakTxt, recordTxt, photoTxt, replayTxt,zoomInTxt,zoomOutTxt,moveCameraTxt, backToCameraTxt, motionRightCameraTxt,motionLeftCameraTxt,motionUpCameraTxt, motionDownCameraTxt,settingTxt, cloudStorageTxt, messageCenterTxt, deviceInfoTxt,batteryTxt;

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
        progressOverlay = findViewById(_getResource("progress_overlay","id"));
        devId = getIntent().getStringExtra(INTENT_DEV_ID);
        sContext= getApplication();
        initView();
        initData();
        initListener();
        getBatteryLevel();
        if (querySupportByDPID(DPConstants.PTZ_CONTROL)) {
            moveCameraTxt.setVisibility(View.VISIBLE);
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
        replayTxt.setOnClickListener(this);
        zoomInTxt = findViewById(_getResource("zoom_in_Txt", "id"));
        zoomInTxt.setOnClickListener(this);
        zoomOutTxt = findViewById(_getResource("zoom_out_Txt", "id"));
        zoomOutTxt.setOnClickListener(this);
        moveCameraTxt = findViewById(_getResource("move_camera_Txt", "id"));
        moveCameraTxt.setOnClickListener(this);
        backToCameraTxt = findViewById(_getResource("back_to_camera_Txt", "id"));
        backToCameraTxt.setOnClickListener(this);
        motionUpCameraTxt = findViewById(_getResource("motion_Up_Txt", "id"));
        motionUpCameraTxt.setOnClickListener(this);
        motionDownCameraTxt = findViewById(_getResource("motion_Down_Txt", "id"));
        motionDownCameraTxt.setOnClickListener(this);
        motionLeftCameraTxt =  findViewById(_getResource("motion_Left_Txt", "id"));
        motionLeftCameraTxt.setOnClickListener(this);
        motionRightCameraTxt =  findViewById(_getResource("motion_Right_Txt", "id"));
        motionRightCameraTxt.setOnClickListener(this);
        // ((ViewGroup) replayTxt.getParent()).removeView(replayTxt);
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
        moveCameraTxt.setOnClickListener(this);
        backToCameraTxt.setOnClickListener(this);
        motionUpCameraTxt.setOnClickListener(this);
        motionDownCameraTxt.setOnClickListener(this);
        motionLeftCameraTxt.setOnClickListener(this);
        motionRightCameraTxt.setOnClickListener(this);
        muteImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        motionUpCameraTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_UP);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.PTZ_STOP, true);
                }
                return true;
            }
        });

        motionDownCameraTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_DOWN);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.PTZ_STOP, true);
                }
                return true;
            }
        });

        motionLeftCameraTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_RIGHT);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.PTZ_STOP, true);
                }
                return true;
            }
        });

        motionRightCameraTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.PTZ_CONTROL, DPConstants.PTZ_LEFT);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.PTZ_STOP, true);
                }
                return true;
            }
        });

        zoomInTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.ZOOM_CONTROL, DPConstants.ZOOM_IN);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.ZOOM_STOP, true);
                }
                return true;
            }
        });

        zoomOutTxt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    publishDps(DPConstants.ZOOM_CONTROL, DPConstants.ZOOM_OUT);
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    publishDps(DPConstants.ZOOM_STOP, true);
                }
                return true;
            }
        });
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
        }  else if(id == _getResource("move_camera_Txt", "id")){
            displayCameraMove();
        } else if(id == _getResource("back_to_camera_Txt", "id")){
            displayCameraOptions();
        } else if(id == _getResource("motion_Up_Txt", "id")){

        } else if(id == _getResource("motion_Down_Txt", "id")){

        } else if (id == _getResource("replay_Txt", "id")) {
            //Toast.makeText(this, "Yet to be Implemented", Toast.LENGTH_SHORT).show();


            Intent intent = new Intent(CameraPanelActivity.this, CameraPlaybackActivity.class);
            intent.putExtra(INTENT_P2P_TYPE, p2pType);
            intent.putExtra(INTENT_DEV_ID, devId);
            dontPause = true;
            intent.putExtra(INTENT_DP_CONFIG, getIntent().getStringExtra(INTENT_DP_CONFIG));
            intent.putExtra(INTENT_BG_COLOR, bgColor);
            intent.putExtra(INTENT_PRIMARY_COLOR, primaryColor);
            intent.putExtra(INTENT_ITEM_BG_COLOR, itemBgColor);
            intent.putExtra(INTENT_TEXT_COLOR_1, textColor1);
            intent.putExtra(INTENT_TEXT_COLOR_2, textColor2);
            //startActivity(intent);
            startActivityForResult(intent,SETTING_ACTIVITY_REQ_CODE);
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
            if (!mCameraP2P.isConnecting()) {
                //pgsBar.setVisibility(progressView.GONE);
                ToastUtil.shortToast(CameraPanelActivity.this, getString(_getResource("connect_first","string")));
                return;
            } else {
                startActivityForResult(intent1, SETTING_ACTIVITY_REQ_CODE);
            }
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

    private void displayCameraMove(){
        LinearLayout camera_options = (LinearLayout )findViewById(_getResource("camera_options_layout", "id"));
        camera_options.setVisibility(View.GONE);
        TableLayout camera_move_options = (TableLayout ) findViewById(_getResource("camera_move_options_layout", "id"));
        camera_move_options.setVisibility(View.VISIBLE);

    }

    private void displayCameraOptions(){
        TableLayout camera_move_options = (TableLayout) findViewById(_getResource("camera_move_options_layout", "id"));
        camera_move_options.setVisibility(View.GONE);
        LinearLayout camera_options = (LinearLayout )findViewById(_getResource("camera_options_layout", "id"));
        camera_options.setVisibility(View.VISIBLE);
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        copyFiles(data, true);
                    }
                    //mHandler.sendMessage(MessageUtil.getMessage(MSG_VIDEO_RECORD_OVER, ARG1_OPERATE_SUCCESS));
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void copyFiles(String sourcePath) {
        copyFiles(sourcePath, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private void copyFiles(String sourcePath, boolean video) {

        FileChannel source;
        File sourceFile = new File(sourcePath);
        String mimeType = "image/jpeg";
        if (video) {
            mimeType = "video/mp4";
        }

        ContentResolver resolver = CameraPanelActivity.this.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, ""+(System.currentTimeMillis()));
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH, (video ? Environment.DIRECTORY_DCIM : Environment.DIRECTORY_PICTURES) + File.separator + devId);
        Uri temp = resolver.insert(video ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        try {
            source = new FileInputStream(sourceFile).getChannel();
            OutputStream fos = resolver.openOutputStream(temp);
            byte[] bytes = new byte[1024 * 100]; //100KB
            while (source.read(ByteBuffer.wrap(bytes)) != -1) {
                fos.write(bytes);
            }
            fos.flush();
            source.close();
            sourceFile.delete();
        } catch (Exception e) {
            mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_FAIL));
            return;
        }
        mHandler.sendMessage(MessageUtil.getMessage(MSG_SCREENSHOT, ARG1_OPERATE_SUCCESS));
    }


    private void snapShotClick() {
        if (PermissionChecker.hasStoragePermission()) {
            ToastUtil.shortToast(CameraPanelActivity.this, "Taking a Snapshot ...");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String path = IPCCameraUtils.recordPathSupportQ(devId);
                picPath = path;
            } else {
                String picPath = IPCCameraUtils.recordSnapshotPath(devId);
                File file = new File(picPath);
                if (!file.exists()) {
                    file.mkdirs();
                }
            }

            mCameraP2P.snapshot(picPath, CameraPanelActivity.this, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int sessionId, int requestId, String data) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        copyFiles(data);
                    }
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
               // ITuyaIPCPTZ tuyaIPCPTZ = TuyaIPCSdk.getPTZInstance(devId);

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
                String bStatus_Of_PowerSource = (String) dps.get(DPConstants.POWER_SOURCE);
                Integer bStatus_Of_Battery = (Integer) dps.get(DPConstants.BATTERY);
                //If both battery and power Supply are plugged in
                if (bStatus_Of_PowerSource.equals(DPConstants.POWER_SOURCE_BATTERY) && bStatus_Of_Battery > 0) {
                    batteryTxt.setVisibility(View.VISIBLE);
                    Object res = dps.get(DPConstants.BATTERY);
                    batteryLevel = JSON.toJSONString(res);
                    batteryTxt.setText("Battery : "+batteryLevel+" %");
                }
                //If only battery is plugged in
                else if(bStatus_Of_Battery > 0) {
                    batteryTxt.setVisibility(View.VISIBLE);
                    Object res = dps.get(DPConstants.BATTERY);
                    batteryLevel = JSON.toJSONString(res);
                    batteryTxt.setText("Battery : "+batteryLevel+" %");
                }
                //If only power supply is plugged in
                else if (bStatus_Of_PowerSource.equals(DPConstants.POWER_SOURCE_BATTERY) && bStatus_Of_Battery ==0){
                    batteryTxt.setVisibility(View.VISIBLE);
                    batteryTxt.setText("Plugged in Power");
                } else {
                    batteryTxt.setVisibility(View.INVISIBLE);
                }
            }
         if (dps.containsKey(DPConstants.SD_STATUS)) {
             String rStatus = dps.get(DPConstants.SD_STATUS).toString();
             if(rStatus.equals(DPConstants.NO_SD_CARD)){
                 replayTxt.setVisibility(View.INVISIBLE);
             } else {
                 replayTxt.setVisibility(View.VISIBLE);
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

    public static Context getContext() {
        return sContext;
    }

}