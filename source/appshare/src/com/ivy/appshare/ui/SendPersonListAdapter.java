package com.ivy.appshare.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.appshare.R;
import com.ivy.appshare.engin.control.LocalSetting;
import com.ivy.appshare.engin.control.LocalSetting.UserIconEnvironment;
import com.ivy.appshare.engin.im.Person;
import com.ivy.appshare.utils.CommonUtils;

public class SendPersonListAdapter extends BaseAdapter implements OnClickListener {
    private Context mContext;
    private List<Person> mListPersons;
    private List<Person> mListSelectPersons = null;

    public SendPersonListAdapter(Context context, List<Person> listPersons) {
        mContext = context;
        mListPersons = listPersons;
        mListSelectPersons = new ArrayList<Person>();
    }

    public void changeList(List<Person> listPersons) {
        mListPersons = listPersons;
    }

    @Override
    public int getCount() {
        if (mListPersons == null) {
            return 0;
        }
        return mListPersons.size();
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
            view = factory.inflate(R.layout.listitem_send_personlist , null);

            myClass = new ViewClass();
            myClass.image = (ImageView)view.findViewById(R.id.photo);
            myClass.name = (TextView)view.findViewById(R.id.share_persons_name);
            myClass.ip = (TextView)view.findViewById(R.id.share_persons_ip);
            myClass.checkbox = (CheckBox)view.findViewById(R.id.checkbox_person);
            myClass.layout = (LinearLayout)view.findViewById(R.id.listitem);
            view.setTag(myClass);
        } else {
            myClass = (ViewClass)view.getTag();
        }
        
        myClass.image.setOnClickListener(this);
        myClass.layout.setOnClickListener(this);

        Person person = null;

        int pos = position;
        if (pos >=0 && pos < mListPersons.size()) {
            person = mListPersons.get(pos);
        }
        
        if(checkSelectPerson(person)){
            myClass.checkbox.setChecked(true);
        }else{
            myClass.checkbox.setChecked(false);
        }
        
        myClass.checkbox.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                /*boolean checked = ((CheckBox)v).isChecked();
                if(checked){
                    addSelectPerson(mListPersons.get(position));
                }else{
                    subbSelectPerson(mListPersons.get(position));
                }*/
            }
        });
        
        showPerson(myClass, person);

        return view;
    }

    private void showPerson(ViewClass myClass, Person person) {
        if (person == null) {
            return;
        }

        UserIconEnvironment userIconEnvironment = LocalSetting.getInstance().getUserIconEnvironment();
        if (person.mImage != null && userIconEnvironment.isExistHead(person.mImage, -1)) {
            String headimagepath = userIconEnvironment.getFriendHeadFullPath(person.mImage);
            // Log.d(TAG, "image name = " + person.mImage + ", nickname = " + person.mNickName + ", headimagepath = " + headimagepath);
            Bitmap bitmap = CommonUtils.DecodeBitmap(headimagepath, 256*256);
            if (bitmap != null) {
                myClass.image.setImageBitmap(bitmap);
            }
        } else {
            myClass.image.setImageResource(R.drawable.ic_contact_picture_holo_light);
        }

        myClass.name.setText(person.mNickName);
        if (person.mIP != null) {
            myClass.ip.setText(person.mIP.getHostAddress());
        }
        
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        
    }

    private static class ViewClass {       
        ImageView image;
        TextView name;
        TextView ip;
        CheckBox checkbox;
        LinearLayout layout;
    }


    private void addSelectPerson(Person person) {
        if( mListSelectPersons.contains(person) != true ){
            mListSelectPersons.add(person);
        }
    }

    private void subbSelectPerson(Person person) {
        if( mListSelectPersons.contains(person) == true ){
            mListSelectPersons.remove(person);
        }
    }
    
    private boolean checkSelectPerson(Person person) {
        if( mListSelectPersons.contains(person) == true ){
            return true;
        }else{
            return false;
        }
    }
    
}
