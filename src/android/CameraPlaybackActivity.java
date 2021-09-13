package com.arihant.tuyaplugin;

import android.app.DatePickerDialog;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.arihant.tuyaplugin.bean.RecordInfoBean;
import com.arihant.tuyaplugin.bean.TimePieceBean;
import com.arihant.tuyaplugin.utils.MessageUtil;
import com.arihant.tuyaplugin.utils.ToastUtil;
import com.tuya.smart.android.camera.timeline.OnBarMoveListener;
import com.tuya.smart.android.camera.timeline.OnSelectedTimeListener;
import com.tuya.smart.android.camera.timeline.TimeBean;
import com.tuya.smart.android.camera.timeline.TuyaTimelineView;
import com.tuya.smart.android.common.utils.L;
import com.tuya.smart.camera.camerasdk.typlayer.callback.AbsP2pCameraListener;
import com.tuya.smart.camera.camerasdk.typlayer.callback.OperationDelegateCallBack;
import com.tuya.smart.camera.ipccamerasdk.bean.MonthDays;
import com.tuya.smart.camera.ipccamerasdk.p2p.ICameraP2P;
import com.tuya.smart.camera.middleware.p2p.ITuyaSmartCameraP2P;
import com.tuya.smart.camera.middleware.p2p.TuyaSmartCameraP2PFactory;
import com.tuya.smart.camera.middleware.widget.AbsVideoViewCallback;
import com.tuya.smart.camera.middleware.widget.TuyaCameraView;
import com.tuya.smart.camera.utils.AudioUtils;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_FAIL;
import static com.arihant.tuyaplugin.utils.Constants.ARG1_OPERATE_SUCCESS;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_ITEM_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_P2P_TYPE;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_PRIMARY_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_1;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_2;
import static com.arihant.tuyaplugin.utils.Constants.MSG_DATA_DATE;
import static com.arihant.tuyaplugin.utils.Constants.MSG_DATA_DATE_BY_DAY_FAIL;
import static com.arihant.tuyaplugin.utils.Constants.MSG_DATA_DATE_BY_DAY_SUCC;
import static com.arihant.tuyaplugin.utils.Constants.MSG_MUTE;


/**
 * @author chenbj
 */
public class CameraPlaybackActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "CameraPlaybackActivity";
    private Toolbar toolbar;
    private TuyaCameraView mVideoView;
    private ImageView muteImg;
    private EditText dateInputEdt;
    private TextView dateInputTitle;
    private TextView mTvStartTime;
    private TextView mTvDuration;
    private RecyclerView queryRv;
    private ProgressBar pgsBar;
    private ProgressBar progressbarOverlay;
    private View progressView;
    private FrameLayout progressOverlay;
    private TuyaTimelineView timelineView;
    private Button queryBtn;
    private TextView startBtn, pauseBtn, resumeBtn, stopBtn;

    private ITuyaSmartCameraP2P mCameraP2P;
    private static final int ASPECT_RATIO_WIDTH = 9;
    private static final int ASPECT_RATIO_HEIGHT = 16;
    private String devId;
    private RelativeLayout mMainLayout;
    private LinearLayout queryListBox;
    private CameraPlaybackTimeAdapter adapter;
    private List<TimePieceBean> queryDateList;
    String bgColor;
    String primaryColor;
    String itemBgColor;
    String textColor1;
    String textColor2;

    private boolean isPlayback = false;

    protected Map<String, List<String>> mBackDataMonthCache;
    protected Map<String, List<TimePieceBean>> mBackDataDayCache;
    private int mPlaybackMute = ICameraP2P.MUTE;
    private int p2pType;

    final Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            // TODO Auto-generated method stub
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }

    };

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MUTE:
                    handleMute(msg);
                    break;
                case MSG_DATA_DATE:
                    handleDataDate(msg);
                    break;
                case MSG_DATA_DATE_BY_DAY_SUCC:
                case MSG_DATA_DATE_BY_DAY_FAIL:
                    handleDataDay(msg);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void handleDataDay(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            queryDateList.clear();
            //Timepieces with data for the query day
            List<TimePieceBean> timePieceBeans = mBackDataDayCache.get(mCameraP2P.getDayKey());
            if (timePieceBeans != null) {
                pgsBar.setVisibility(progressView.GONE);
                queryDateList.addAll(timePieceBeans);
                List<TimeBean> timelineData = new ArrayList<>();
                for(TimePieceBean bean: timePieceBeans) {
                    TimeBean b = new TimeBean();
                    b.setStartTime(bean.getStartTime());
                    b.setEndTime(bean.getEndTime());
                    timelineData.add(b);
                }
                timelineView.setCurrentTimeConfig(timePieceBeans.get(0).getEndTime()*1000L);
                timelineView.setRecordDataExistTimeClipsList(timelineData);
            } else {
                pgsBar.setVisibility(progressView.GONE);
                showErrorToast();
            }
            adapter.notifyDataSetChanged();
        } else {
            pgsBar.setVisibility(progressView.GONE);
            showErrorToast();
        }
    }

    private void handleDataDate(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
//            pgsBar.setVisibility(progressView.VISIBLE);
            List<String> days = mBackDataMonthCache.get(mCameraP2P.getMonthKey());

            try {
                if (days.size() == 0) {
                    pgsBar.setVisibility(progressView.GONE);
                    showErrorToast();
                    return;
                }
                final String inputStr = dateInputEdt.getText().toString();
                if (!TextUtils.isEmpty(inputStr) && inputStr.contains("/")) {
                    String[] substring = inputStr.split("/");
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);
                    int day = Integer.parseInt(substring[2]);
                    mCameraP2P.queryRecordTimeSliceByDay(year, mouth, day, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            L.e(TAG, inputStr + " --- " + data);
                            parsePlaybackData(data);
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            pgsBar.setVisibility(progressView.GONE);
                            mHandler.sendEmptyMessage(MSG_DATA_DATE_BY_DAY_FAIL);
                        }
                    });
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            pgsBar.setVisibility(progressView.GONE);
        }
    }

    private void parsePlaybackData(Object obj) {
        RecordInfoBean recordInfoBean = JSONObject.parseObject(obj.toString(), RecordInfoBean.class);
        if (recordInfoBean.getCount() != 0) {
            List<TimePieceBean> timePieceBeanList = recordInfoBean.getItems();
            if (timePieceBeanList != null && timePieceBeanList.size() != 0) {
                mBackDataDayCache.put(mCameraP2P.getDayKey(), timePieceBeanList);
            }
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_SUCC, ARG1_OPERATE_SUCCESS));
        } else {
            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE_BY_DAY_FAIL, ARG1_OPERATE_FAIL));
        }
    }

    private void handleMute(Message msg) {
        if (msg.arg1 == ARG1_OPERATE_SUCCESS) {
            muteImg.setSelected(mPlaybackMute == ICameraP2P.MUTE);
        } else {
            ToastUtil.shortToast(CameraPlaybackActivity.this, getString(_getResource("operation_failed","string")));
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(_getResource("activity_camera_playback","layout"));
        progressOverlay = findViewById(_getResource("progress_overlay","id"));
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mMainLayout = findViewById(_getResource("main_layout", "id"));
        bgColor = getIntent().getStringExtra(INTENT_BG_COLOR);
        primaryColor = getIntent().getStringExtra(INTENT_PRIMARY_COLOR);
        itemBgColor = getIntent().getStringExtra(INTENT_ITEM_BG_COLOR);
        textColor1 = getIntent().getStringExtra(INTENT_TEXT_COLOR_1);
        textColor2 = getIntent().getStringExtra(INTENT_TEXT_COLOR_2);
        toolbar = findViewById(_getResource("toolbar_view","id"));
        if(bgColor!=null && textColor1!=null){
            mMainLayout.setBackgroundColor(Color.parseColor(bgColor));
            toolbar.setBackgroundColor(Color.parseColor(bgColor));
            toolbar.setTitleTextColor(Color.parseColor(textColor1));
        }
        timelineView = findViewById(_getResource("timeline","id"));

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mVideoView = findViewById(_getResource("camera_video_view","id"));
        muteImg = findViewById(_getResource("camera_mute","id"));
        dateInputEdt = findViewById(_getResource("date_input_edt","id"));
        dateInputTitle = findViewById(_getResource("date_input_title","id"));
        queryBtn = findViewById(_getResource("query_btn","id"));
        startBtn = findViewById(_getResource("start_btn","id"));
        pauseBtn = findViewById(_getResource("pause_btn","id"));
        resumeBtn = findViewById(_getResource("resume_btn","id"));
        stopBtn = findViewById(_getResource("stop_btn","id"));
        queryRv = findViewById(_getResource("query_list","id"));
        pgsBar = findViewById(_getResource("pBar","id"));
        progressbarOverlay = findViewById(_getResource("progress_bar_overlay","id"));
        // progressOverlay = findViewById(_getResource("progress_overlay","id"));
        if(bgColor!=null && textColor1!=null){
            queryBtn.setBackgroundColor(Color.parseColor(itemBgColor));
            // queryListBox.setBackgroundColor(Color.parseColor(itemBgColor));
            queryBtn.setTextColor(Color.parseColor(primaryColor));
        }
        if(itemBgColor!=null){
            startBtn.setBackgroundColor(Color.parseColor(itemBgColor));
            pauseBtn.setBackgroundColor(Color.parseColor(itemBgColor));
            resumeBtn.setBackgroundColor(Color.parseColor(itemBgColor));
            stopBtn.setBackgroundColor(Color.parseColor(itemBgColor));
//            startBtn.setTextColor(Color.parseColor(primaryColor));
//            pauseBtn.setTextColor(Color.parseColor(primaryColor));
//            resumeBtn.setTextColor(Color.parseColor(primaryColor));
//            stopBtn.setTextColor(Color.parseColor(primaryColor));
            dateInputEdt.setTextColor(Color.parseColor(textColor1));
//            mTvStartTime.setTextColor(Color.parseColor(textColor1));
//            mTvDuration.setTextColor(Color.parseColor(textColor1));
            dateInputTitle.setTextColor(Color.parseColor(primaryColor));
            timelineView.setBackgroundColor(Color.parseColor(itemBgColor));
            timelineView.setLinesColor(Color.parseColor(primaryColor));
            pgsBar.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(primaryColor)));
            progressbarOverlay.setIndeterminateTintList(ColorStateList.valueOf(Color.parseColor(primaryColor)));
            //timelineView.setSelectionBoxColor(Color.parseColor(primaryColor));
        }

        //It is best to set the aspect ratio to 16:9
        WindowManager windowManager = (WindowManager) this.getSystemService(WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        int height = width * ASPECT_RATIO_WIDTH / ASPECT_RATIO_HEIGHT;
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.addRule(RelativeLayout.BELOW, _getResource("toolbar_view","id"));
        findViewById(_getResource("camera_video_view_Rl","id")).setLayoutParams(layoutParams);

        timelineView.setOnBarMoveListener(new OnBarMoveListener() {
            @Override
            public void onBarMove(long l, long l1, long l2) {

            }

            @Override
            public void onBarMoveFinish(long startTime, long endTime, long currentTime) {
                timelineView.setCanQueryData();
                timelineView.setQueryNewVideoData(false);
                if (startTime != -1 && endTime != -1) {
                    playback((int)startTime, (int)endTime, (int)currentTime);
                }
            }

            @Override
            public void onBarActionDown() {

            }
        });
        timelineView.setOnSelectedTimeListener(new OnSelectedTimeListener() {
            @Override
            public void onDragging(long selectStartTime, long selectEndTime) {

            }
        });
    }

    private void initData() {
        mBackDataMonthCache = new HashMap<>();
        mBackDataDayCache = new HashMap<>();
        p2pType = getIntent().getIntExtra(INTENT_P2P_TYPE, 1);
        devId = getIntent().getStringExtra(INTENT_DEV_ID);

        dateInputEdt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(CameraPlaybackActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        queryRv.setLayoutManager(mLayoutManager);
        queryRv.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        queryDateList = new ArrayList<>();
        adapter = new CameraPlaybackTimeAdapter(this, queryDateList,itemBgColor,textColor1);
        queryRv.setAdapter(adapter);
        //queryRv.findContainingViewHolder()
//        mTvStartTime = queryRv.findViewById(_getResource("time_start","id"));
//        mTvDuration = queryRv.findViewById(_getResource("time_duration","id"));
//        queryListBox = queryRv.findViewById(_getResource("query_list_box","id"));
//
//        mTvStartTime.setTextColor(Color.parseColor(textColor1));
//        mTvDuration.setTextColor(Color.parseColor(textColor1));
//        queryListBox.setBackgroundColor(Color.parseColor(itemBgColor));

        mCameraP2P = TuyaSmartCameraP2PFactory.createCameraP2P(p2pType, devId);
        mVideoView.setViewCallback(new AbsVideoViewCallback() {
            @Override
            public void onCreated(Object o) {
                super.onCreated(o);
                if (mCameraP2P != null) {
                    mCameraP2P.generateCameraView(mVideoView.createdView());
                }
            }
        });
        mVideoView.createVideoView(p2pType);
        if (!mCameraP2P.isConnecting()) {
            mCameraP2P.connect(devId, new OperationDelegateCallBack() {
                @Override
                public void onSuccess(int i, int i1, String s) {

                }

                @Override
                public void onFailure(int i, int i1, int i2) {

                }
            });
        }

        muteImg.setSelected(true);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date(System.currentTimeMillis());
        dateInputEdt.setText(simpleDateFormat.format(date));
    }
    private void updateLabel() {
        //String myFormat = "MM/dd/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        dateInputEdt.setText(sdf.format(myCalendar.getTime()));
    }

    private void initListener() {
        muteImg.setOnClickListener(this);
        queryBtn.setOnClickListener(this);
        startBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        resumeBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        adapter.setListener(new CameraPlaybackTimeAdapter.OnTimeItemListener() {
            @Override
            public void onClick(TimePieceBean timePieceBean) {
                playback(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime());
            }
        });
    }

    private void playbackHide() {
//       progressOverlay.setVisibility(View.GONE);
    }

    private void playback(int startTime, int endTime, int playTime) {
        int playbackViewVisible = View.VISIBLE;
//        int playbackViewGone = View.GONE;
        //int playback = View.generateViewId();
        progressOverlay.setVisibility(playbackViewVisible);
        mCameraP2P.startPlayBack(startTime,
                endTime,
                playTime, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        //progressOverlay.setVisibility(playbackViewGone);
                        isPlayback = true;
                        playbackHide();
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        // progressOverlay.setVisibility(playbackViewGone);
                        isPlayback = false;
                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        // progressOverlay.setVisibility(playbackViewGone);
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {
                        // progressOverlay.setVisibility(playbackViewGone);
                        isPlayback = false;
                    }
                });
        progressOverlay.setVisibility(View.GONE);
        //playbackHide();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        progressView = v;
        if (id == _getResource("camera_mute","id")) {
            muteClick();
        } else if (id == _getResource("query_btn","id")) {
            queryDayByMonthClick();
        } else if (id == _getResource("start_btn","id")) {
            startPlayback();
        } else if (id == _getResource("pause_btn","id")) {
            pauseClick();
        } else if (id == _getResource("resume_btn","id")) {
            resumeClick();
        } else if (id == _getResource("stop_btn","id")) {
            stopClick();
        }
    }

    private void startPlayback() {
        if (null != queryDateList && queryDateList.size() > 0) {
            TimePieceBean timePieceBean = queryDateList.get(0);
            if (null != timePieceBean) {
                mCameraP2P.startPlayBack(timePieceBean.getStartTime(), timePieceBean.getEndTime(), timePieceBean.getStartTime(), new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = true;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                }, new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int sessionId, int requestId, String data) {
                        isPlayback = false;
                    }

                    @Override
                    public void onFailure(int sessionId, int requestId, int errCode) {

                    }
                });
            }
        } else {
            ToastUtil.shortToast(this, getString(_getResource("no_data","string")));
        }
    }

    private void stopClick() {
        mCameraP2P.stopPlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {

            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
        isPlayback = false;
    }

    private void resumeClick() {
        mCameraP2P.resumePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = true;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void pauseClick() {
        mCameraP2P.pausePlayBack(new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                isPlayback = false;
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {

            }
        });
    }

    private void queryDayByMonthClick() {
        pgsBar.setVisibility(progressView.VISIBLE);
        queryDateList.clear();
        if (!mCameraP2P.isConnecting()) {
            pgsBar.setVisibility(progressView.GONE);
            ToastUtil.shortToast(CameraPlaybackActivity.this, getString(_getResource("connect_first","string")));
            return;
        }
        String inputStr = dateInputEdt.getText().toString();
        if (TextUtils.isEmpty(inputStr)) {
            pgsBar.setVisibility(progressView.GONE);
            return;
        }
        if (inputStr.contains("/")) {
            String[] substring = inputStr.split("/");
            if (substring.length > 2) {
                try {
                    int year = Integer.parseInt(substring[0]);
                    int mouth = Integer.parseInt(substring[1]);

                    mCameraP2P.queryRecordDaysByMonth(year, mouth, new OperationDelegateCallBack() {
                        @Override
                        public void onSuccess(int sessionId, int requestId, String data) {
                            MonthDays monthDays = JSONObject.parseObject(data, MonthDays.class);
                            mBackDataMonthCache.put(mCameraP2P.getMonthKey(), monthDays.getDataDays());
                            L.e(TAG,   "MonthDays --- " + data);

                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_SUCCESS));
                        }

                        @Override
                        public void onFailure(int sessionId, int requestId, int errCode) {
                            mHandler.sendMessage(MessageUtil.getMessage(MSG_DATA_DATE, ARG1_OPERATE_FAIL));
                        }
                    });
                } catch (Exception e) {
                    pgsBar.setVisibility(progressView.GONE);
                    ToastUtil.shortToast(CameraPlaybackActivity.this, "Date Input Error!");
                }
            }
        }
    }

    private void muteClick() {
        int mute;
        mute = mPlaybackMute == ICameraP2P.MUTE ? ICameraP2P.UNMUTE : ICameraP2P.MUTE;
        mCameraP2P.setMute(mute, new OperationDelegateCallBack() {
            @Override
            public void onSuccess(int sessionId, int requestId, String data) {
                mPlaybackMute = Integer.valueOf(data);
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_SUCCESS));
            }

            @Override
            public void onFailure(int sessionId, int requestId, int errCode) {
                mHandler.sendMessage(MessageUtil.getMessage(MSG_MUTE, ARG1_OPERATE_FAIL));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
        if (null != mCameraP2P) {
            AudioUtils.getModel(this);
            mCameraP2P.registerP2PCameraListener(p2pCameraListener);
            mCameraP2P.generateCameraView(mVideoView.createdView());
        }
    }

    private AbsP2pCameraListener p2pCameraListener = new AbsP2pCameraListener() {
        @Override
        public void onReceiveFrameYUVData(int i, ByteBuffer byteBuffer, ByteBuffer byteBuffer1, ByteBuffer byteBuffer2, int i1, int i2, int i3, int i4, long l, long l1, long l2, Object o) {
            super.onReceiveFrameYUVData(i, byteBuffer, byteBuffer1, byteBuffer2, i1, i2, i3, i4, l, l1, l2, o);
            timelineView.setCurrentTimeInMillisecond(l*1000L);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.onPause();
        if (isPlayback) {
            mCameraP2P.stopPlayBack(null);
        }
        if (null != mCameraP2P) {
            mCameraP2P.removeOnP2PCameraListener();
            if (isFinishing()) {
                mCameraP2P.disconnect(new OperationDelegateCallBack() {
                    @Override
                    public void onSuccess(int i, int i1, String s) {

                    }

                    @Override
                    public void onFailure(int i, int i1, int i2) {

                    }
                });
            }
        }
        AudioUtils.changeToNomal(this);
    }


    private void showErrorToast() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtil.shortToast(CameraPlaybackActivity.this, "No Data found!");
            }
        });
    }
    private int _getResource(String name, String type) {
        String package_name = CameraPanelActivity.getContext().getPackageName();
        Resources resources = CameraPanelActivity.getContext().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

}