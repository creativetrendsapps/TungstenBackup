package com.creativetrends.tungsten.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import com.creativetrends.tungsten.activities.AppInfo;
import com.creativetrends.tungsten.R;

public class BatchAdapter extends AppInfoAdapter
{
    Context context;
    ArrayList<AppInfo> items;
    int layout;

    public BatchAdapter(Context context, int layout, ArrayList<AppInfo> items)
    {
        super(context, layout, items);
        this.context = context;
        this.items = new ArrayList<AppInfo>(items);
        this.layout = layout;
    }
    @Override
    public View getView(int pos, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layout, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.cb = (CheckBox) convertView.findViewById(R.id.cb);
            viewHolder.tv = (TextView) convertView.findViewById(R.id.tv);
            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = getItem(pos);
        if(appInfo != null)
        {
            viewHolder.cb.setText(appInfo.getLabel());
            viewHolder.tv.setText(appInfo.getPackageName());
            viewHolder.cb.setChecked(appInfo.isChecked());
            if(appInfo.isInstalled())
            {
                int color = appInfo.isSystem() ? Color.RED : Color.GREEN;
                viewHolder.cb.setTextColor(Color.WHITE);
                viewHolder.tv.setTextColor(color);
            }
            else
            {
                viewHolder.cb.setTextColor(Color.GRAY);
                viewHolder.tv.setTextColor(Color.GRAY);
            }
        }
        return convertView;
    }
    static class ViewHolder
    {
        CheckBox cb;
        TextView tv;
    }
}
