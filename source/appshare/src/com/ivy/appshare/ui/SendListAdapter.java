package com.ivy.appshare.ui;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ivy.appshare.R;

public class SendListAdapter extends BaseAdapter implements OnClickListener {
    private Context mContext;
    List<String> mListSendItems;


    public SendListAdapter(Context context, List<String> sendApps) {
        mContext = context;
        mListSendItems = sendApps;
    }

    @Override
    public int getCount() {
        return mListSendItems.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup arg2) {
        if (position < 0 || position >= getCount()) {
            return null;
        }
        
        ViewClass myClass = null;
        if(view == null) {
            LayoutInflater factory = LayoutInflater.from(mContext);
            view = factory.inflate(R.layout.listitem_send , null);
            myClass = new ViewClass();

            myClass.mAppName = (TextView)view.findViewById(R.id.name);

            view.setTag(myClass);
        } else {
            myClass = (ViewClass)view.getTag();
        }

        myClass.mAppName.setText(mListSendItems.get(position));

        return view;
    }


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

    private static class ViewClass {
        TextView mAppName;
    }
}
