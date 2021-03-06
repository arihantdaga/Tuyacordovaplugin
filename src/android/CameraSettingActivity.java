package com.arihant.tuyaplugin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSONObject;
import com.arihant.tuyaplugin.utils.DPConfig;
import com.arihant.tuyaplugin.utils.DPConstants;
import com.arihant.tuyaplugin.utils.ToastUtil;
import com.tuya.smart.sdk.api.ITuyaDevice;
import com.tuya.smart.sdk.api.IDevListener;
import com.tuya.smart.home.sdk.TuyaHomeSdk;
import com.tuya.smart.sdk.api.IResultCallback;
import com.alibaba.fastjson.JSON;
import com.tuya.smart.sdk.bean.DeviceBean;

import static com.arihant.tuyaplugin.utils.Constants.INTENT_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DEV_ID;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_DP_CONFIG;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_ITEM_BG_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_PRIMARY_COLOR;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_1;
import static com.arihant.tuyaplugin.utils.Constants.INTENT_TEXT_COLOR_2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSON;

import org.w3c.dom.Text;

public class CameraSettingActivity extends AppCompatActivity {

    String TAG = "Tuyacordovaplugin";
    private String devId;
    ITuyaDevice mDevice = null;
    private List<DPConfig> dpConfig;
    HashMap<String, Integer> dpElements = new HashMap<String, Integer>();
    HashMap<String, Boolean> programmaticElementValueChange = new HashMap<String, Boolean>();
    HashMap<String, String> dpLabels = new HashMap<String, String>();

    String bgColor;
    String primaryColor;
    String itemBgColor;
    String textColor1;
    String textColor2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initView();
        initDps();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDevice != null) {
            mDevice.onDestroy();
        }
    }

    private void initData() {
        devId = getIntent().getStringExtra(INTENT_DEV_ID);
        bgColor = getIntent().getStringExtra(INTENT_BG_COLOR);
        primaryColor = getIntent().getStringExtra(INTENT_PRIMARY_COLOR);
        itemBgColor = getIntent().getStringExtra(INTENT_ITEM_BG_COLOR);
        textColor1 = getIntent().getStringExtra(INTENT_TEXT_COLOR_1);
        textColor2 = getIntent().getStringExtra(INTENT_TEXT_COLOR_2);
        String dpConfigString = getIntent().getStringExtra(INTENT_DP_CONFIG);
        try {
            dpConfig = JSON.parseArray(dpConfigString, DPConfig.class);
        } catch (Exception e) {
            Log.e(TAG, e + "Unable to parse config");
        }
    }


    private void initView() {
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(getApplicationContext().LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(_getResource("activity_camera_setting", "layout"), null);
        if (bgColor != null) {
            v.setBackgroundColor(Color.parseColor(bgColor));
        }

        // Find the ScrollView
        ScrollView sv = v.findViewById(_getResource("dpsContainer", "id"));

        // Create a LinearLayout element
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));


        if (dpConfig.isEmpty()) {
            TextView tv = new TextView(this);
            tv.setText("No settings available");
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            tv.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
            ll.addView(tv);
        } else {
            for(int i=0; i < dpConfig.size(); i++) {
                DPConfig d = dpConfig.get(i);
                if (d.dpId != null && d.displayName != null) {
                    int newId = ((i+1) * 200) + Integer.parseInt(d.dpId);
                    if (d.inputType.equals(DPConfig.TYPE_BOOLEAN)) {
                        Switch toggleButton = new Switch(this);
                        toggleButton.setText(d.displayName);
                        toggleButton.setPadding(15, 30, 15, 30);
                        toggleButton.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                        toggleButton.setHeight(180);
                        if (itemBgColor != null && textColor1 != null) {
                            toggleButton.setBackgroundColor(Color.parseColor(itemBgColor));
                            toggleButton.setTextColor(Color.parseColor(textColor1));
                        }
                        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                try {
                                    if (programmaticElementValueChange == null || (programmaticElementValueChange != null && !programmaticElementValueChange.get(d.dpId))) {
                                        HashMap<String, Boolean> newDps = new HashMap<String, Boolean>();
                                        newDps.put(d.dpId, isChecked);
                                        publishDps(JSON.toJSONString(newDps));
                                    } else {
                                        programmaticElementValueChange.put(d.dpId, false);
                                    }
                                } catch (NullPointerException e) {
                                    HashMap<String, Boolean> newDps = new HashMap<String, Boolean>();
                                    newDps.put(d.dpId, isChecked);
                                    publishDps(JSON.toJSONString(newDps));
                                }
                            }
                        });
                        toggleButton.setId(newId);
                        dpElements.put(d.dpId, newId);
                        ll.addView(toggleButton);
                    }
                    else if (d.inputType.equals(DPConfig.TYPE_VALUE)) {
                        dpLabels.put(d.dpId, "");
                        LinearLayout tl = new LinearLayout(this);
                        TextView label = new TextView(tl.getContext());
                        TextView name = new TextView(tl.getContext());
//                        TableRow tr = new TableRow(this);
                        label.setText(d.displayName);
                        label.setPadding(15, 30, 15, 30);
                        tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                        if (itemBgColor != null && textColor1 != null) {
                            tl.setBackgroundColor(Color.parseColor(itemBgColor));
                            label.setTextColor(Color.parseColor(textColor1));
                        }
                        tl.addView(label);
                        name.setText(dpLabels.get(d.dpId));
                        name.setPadding(250, 30, 15, 30);
                        name.setTextColor(Color.parseColor(primaryColor));
                        name.setTextAlignment(tl.TEXT_ALIGNMENT_VIEW_END);
                        tl.addView(name);
                        name.setId(newId);
                        dpElements.put(d.dpId, newId);
                        ll.addView(tl);
                    }
                    else if (d.inputType.equals(DPConfig.TYPE_SELECT)) {
                        if (d.supportedValueLables != null && d.supportedValues != null) {
                            TableLayout tl = new TableLayout(this);
                            TableRow tr = new TableRow(this);
                            tr.setPadding(15, 35, 15, 10);
                            tr.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                            Spinner sp = new Spinner(this);
                            _getResource("spinner_item_layout", "layout");
                            ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item , d.supportedValueLables){
                                public View getView(int position, View convertView,ViewGroup parent) {
                                    View v = super.getView(position, convertView, parent);
                                    ((TextView) v).setGravity(Gravity.RIGHT);
                                    ((TextView) v).setPadding(0, 10, 0, 30);
                                    if (primaryColor != null) {
                                        ((TextView) v).setTextColor(Color.parseColor(primaryColor));
                                    }
                                    return v;

                                }

                                public View getDropDownView(int position, View convertView,ViewGroup parent) {

                                    View v = super.getDropDownView(position, convertView,parent);
                                    if (itemBgColor != null) {
                                        ((TextView) v).setBackgroundColor(Color.parseColor(itemBgColor));
                                    }
                                    if (textColor1 != null) {
                                        ((TextView) v).setTextColor(Color.parseColor(textColor1));
                                    }
                                    return v;

                                }
                            };
                            aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            sp.setAdapter(aa);
                            sp.setId(newId);
                            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                    HashMap<String, String> newDps = new HashMap<String, String>();
                                    newDps.put(d.dpId, d.supportedValues[position]);
                                    publishDps(JSON.toJSONString(newDps));
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parentView) {
                                    // your code here
                                }

                            });
                            TextView tv = new TextView(this);
                            tv.setText(d.displayName);
                            if (itemBgColor != null && textColor1 != null) {
                                tr.setBackgroundColor(Color.parseColor(itemBgColor));
                                tv.setTextColor(Color.parseColor(textColor1));
                            }
                            tv.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 5));
                            sp.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT, 5));

                            dpElements.put(d.dpId, newId);
                            tr.addView(tv);
                            tr.addView(sp);
                            tl.addView(tr);
                            ll.addView(tl);
                        }
                    }

                    if (d.description != null) {
                        TextView descTv = new TextView(this);
                        descTv.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
                        descTv.setPadding(15, 2, 15, 20);
                        if (textColor2 != null) {
                            descTv.setTextColor(Color.parseColor(textColor2));
                        }
                        descTv.setText(d.description);
                        ll.addView(descTv);
                    }
                }

            }
        }
            LinearLayout tl = new LinearLayout(this);
            Button btn = new Button(tl.getContext());
            btn.setText("Format SD Card");
            btn.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            btn.setTextColor(Color.parseColor(primaryColor));
            Object formatValue = queryValueByDPID("111");
            if (formatValue != null) {
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        btn.setEnabled(false);
                        publishDp(DPConstants.SD_FORMAT, true);
                        listenDPUpdate(DPConstants.SD_FORMAT_STATUS, new DPCallback() {
                            @Override
                            public void callback(Object obj) {
                                if(!btn.getText().equals("Formatting..." +"100" + '%')) {
                                    btn.setText("Formatting..." + String.valueOf(obj) + '%');
                                    if ("100".equals(String.valueOf(obj))) {
                                        ToastUtil.shortToast(CameraSettingActivity.this, "SD card Format Success!");
                                    }
                                }
                            }
                        });
                    }
                });
            }
            tl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            if (itemBgColor != null && textColor1 != null) {
                tl.setBackgroundColor(Color.parseColor(itemBgColor));
            }
            tl.addView(btn);
            ll.addView(tl);
        TextView descTv = new TextView(this);
        descTv.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        descTv.setPadding(15, 2, 15, 20);
        if (textColor2 != null) {
            descTv.setTextColor(Color.parseColor(textColor2));
        }
        descTv.setText("Click on the above button to erase and format SD Card data.");
        ll.addView(descTv);
        sv.addView(ll);
        setContentView(v);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);
        CameraSettingActivity.this.setTitle("Settings");
    }
    private Object queryValueByDPID(String dpId) {
        DeviceBean deviceBean = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        if (deviceBean != null) {
            Map<String, Object> dps = deviceBean.getDps();
            if (dps != null) {
                return dps.get(dpId);
            }
        }
        return null;
    }

    private int _getResource(String name, String type) {
        String package_name = getApplication().getPackageName();
        Resources resources = getApplication().getResources();
        return resources.getIdentifier(name, type, package_name);
    }

    private void initDps(){
        DeviceBean mDev = TuyaHomeSdk.getDataInstance().getDeviceBean(devId);
        mDevice = TuyaHomeSdk.newDeviceInstance(devId);
        mDevice.registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
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
        HashMap<String, Object> dps = (HashMap<String, Object>) mDev.getDps();
        setDps(dps);
    }

    private void publishDp (String dpId, Object value) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(dpId, value);
        String dps = jsonObject.toString();
        mDevice.publishDps(dps, new IResultCallback() {
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

    private void publishDps(String dps) {
        mDevice.publishDps(dps, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Log.e(TAG, "publishDps err " + code);
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSucasdasd12323" );
                Log.d("dps",dps);
            }
        });
    }
    private void listenDPUpdate(String dpId, DPCallback callback) {
        TuyaHomeSdk.newDeviceInstance(devId).registerDevListener(new IDevListener() {
            @Override
            public void onDpUpdate(String devId, String dpStr) {
                if (callback != null) {
                    Map<String, Object> dps = (Map) JSONObject.parseObject(dpStr, Map.class);
                    if (dps.containsKey(dpId)) {
                        callback.callback(dps.get(dpId));
                    }
                }
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        else
        {
            return super.onOptionsItemSelected(item);
        }
    }
    private interface DPCallback {
        void callback(Object obj);
    }

    public void setDps(HashMap<String, Object> dps) {
        if (dpConfig.isEmpty()) return;
        for (Map.Entry<String,Object> entry : dps.entrySet()){
            try {
                int elementId = dpElements.get(entry.getKey());
                assert elementId != 0;
                String tag1 = entry.getValue().getClass().getName();
                Log.d(TAG,tag1);
                    if (entry.getValue().getClass().getName().equals(Boolean.class.getName())) {
                        Switch sw = findViewById(elementId);
                        if (sw != null) {
                            if (sw.isChecked() != (boolean) entry.getValue()) {
                                programmaticElementValueChange.put(entry.getKey(), true);
                                sw.setChecked((boolean) entry.getValue());
                            }
                        }
                    } else if (entry.getValue().getClass().getName().equals(Integer.class.getName())) {
                        TextView tl = findViewById(elementId);
                        if (tl != null) {
                            for (int i = 0; i < dpConfig.size(); i++) {
                                DPConfig d = dpConfig.get(i);
                                if (d.dpId.equals(entry.getKey())) {
                                    if (d.supportedValues.length != 0) {
                                        for (int j = 0; j < d.supportedValues.length; j++) {
                                            if (JSON.toJSONString(entry.getValue()).equals(d.supportedValues[j])) {
                                                String n = d.supportedValueLables[j];
                                                dpLabels.put(d.dpId, n);
                                                tl.setText(n);
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    } else if (entry.getValue().getClass().getName().equals(String.class.getName())) {
                        for (int i = 0; i < dpConfig.size(); i++) {
                            DPConfig d = dpConfig.get(i);
                            if (d.dpId.equals(entry.getKey())) {
                                if (d.inputType.equals(DPConfig.TYPE_VALUE)) {
                                    TextView tl = findViewById(elementId);
                                    String res = (String) entry.getValue();
                                    List<String> capacityList = Arrays.asList(res.split("\\|"));
                                    String finalres = String.format("%.2f",(Double.valueOf((double) (Integer.valueOf(capacityList.get(1)) /1024000f)))) +"|"+String.format("%.2f", (Double.valueOf((double) (Integer.valueOf(capacityList.get(0)) /1024000f))));
                                    dpLabels.put(d.dpId, finalres);
                                    tl.setText(finalres);
                                } else {
                                    Spinner sp = findViewById(elementId);
                                    if (sp != null) {
                                        for (int j = 0; j < d.supportedValues.length; j++) {
                                            if (entry.getValue().equals(d.supportedValues[j])) {
                                                programmaticElementValueChange.put(entry.getKey(), true);
                                                sp.setSelection(j);
                                                break;
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
            } catch (Exception ignored) {
            }

        }
    }
}