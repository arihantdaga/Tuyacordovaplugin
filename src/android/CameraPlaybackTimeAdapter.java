package com.arihant.tuyaplugin;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.arihant.tuyaplugin.bean.TimePieceBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

//import com.tuya.smart.android.demo.R;

/**
 * Created by huangdaju on 2018/3/5.
 */

public class CameraPlaybackTimeAdapter extends RecyclerView.Adapter<CameraPlaybackTimeAdapter.MyViewHolder> {

    private LayoutInflater mInflater;
    private List<TimePieceBean> timePieceBeans;
    private OnTimeItemListener listener;
    private String itemBgColor1;
    private String textColor11;


    public CameraPlaybackTimeAdapter(Context context, List<TimePieceBean> timePieceBeans, String itemBgColor, String textColor1) {
        mInflater = LayoutInflater.from(context);
        this.timePieceBeans = timePieceBeans;
        itemBgColor1 = itemBgColor;
        textColor11 = textColor1;

    }

    public void setListener(OnTimeItemListener listener) {
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(mInflater.inflate(_getResource("activity_camera_playback_time_tem","layout"), parent, false));
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final TimePieceBean ipcVideoBean = timePieceBeans.get(position);
        holder.mTvStartTime.setText(timeFormat(ipcVideoBean.getStartTime() * 1000L));
        int lastTime = ipcVideoBean.getEndTime() - ipcVideoBean.getStartTime();
        holder.mTvDuration.setText(holder.mTvDuration.getContext().getString(_getResource("duration","string")) + changeSecond(lastTime));
        holder.mTvStartTime.setTextColor(Color.parseColor(textColor11));
        holder.mTvDuration.setTextColor(Color.parseColor(textColor11));
        holder.queryListBox.setBackgroundColor(Color.parseColor(itemBgColor1));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != listener) {
                    listener.onClick(ipcVideoBean);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return timePieceBeans.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTvStartTime;
        TextView mTvDuration;
        LinearLayout queryListBox;


        public MyViewHolder(final View view) {
            super(view);
            queryListBox = view.findViewById(_getResource("query_list_box","id"));
            mTvStartTime = view.findViewById(_getResource("time_start","id"));
            mTvDuration = view.findViewById(_getResource("time_duration","id"));
        }
    }

    public static String timeFormat(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(time);
        return sdf.format(date);
    }

    public static String changeSecond(int seconds) {
        int temp;
        StringBuilder timer = new StringBuilder();
        temp = seconds / 3600;
        timer.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 / 60;
        timer.append((temp < 10) ? "0" + temp + ":" : "" + temp + ":");

        temp = seconds % 3600 % 60;
        timer.append((temp < 10) ? "0" + temp : "" + temp);
        return timer.toString();
    }

    public interface OnTimeItemListener {
        void onClick(TimePieceBean o);
    }
    /**
     * Resource ID
     *
     * @param name
     * @param type layout, drawable, id
     * @return
     */
    private int _getResource(String name, String type) {
        String package_name = CameraPanelActivity.getContext().getPackageName();
        Resources resources = CameraPanelActivity.getContext().getResources();
        return resources.getIdentifier(name, type, package_name);
    }
}

