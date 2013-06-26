package com.ivy.appshare.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ivy.appshare.R;

/**
 * This is a drop down list menu. You can set a custom adapter to it. U can set
 * a OnPopMenuItemClickListener to get a String return when click the menu item.
 * 
 * @author b456
 * 
 */
public class SimplePopMenu implements OnItemClickListener {
	private Context mContext;
	private PopupWindow mPopupWindow;
	private OnPopMenuItemClickListener mPopMenuItemClickListener;
	private BaseAdapter mAdapter;

	public SimplePopMenu(Context context, String[] menuItem) {
		mContext = context;
		BaseAdapter menuAdapter = new DefaultMenuAdapter(menuItem);
		InitPopMenu(context, menuAdapter, mContext.getResources()
				.getDimensionPixelSize(R.dimen.popmenu_width),
				LayoutParams.WRAP_CONTENT);
	}
	
    public SimplePopMenu(Context context, String menuItem) {
        mContext = context;
        BaseAdapter menuAdapter = new SingleMenuAdapter(new String[]{menuItem});
        InitPopMenu(context, menuAdapter, mContext.getResources().getDimensionPixelSize(R.dimen.broadcast_popmenu_width),
                LayoutParams.WRAP_CONTENT);
    }	

	public SimplePopMenu(Context context, BaseAdapter adapter) {
		mContext = context;
		InitPopMenu(context, adapter, mContext.getResources()
				.getDimensionPixelSize(R.dimen.popmenu_width),
				LayoutParams.WRAP_CONTENT);
	}

	public SimplePopMenu(Context context, BaseAdapter adapter, int width,
			int height) {
		mContext = context;
		InitPopMenu(context, adapter, width, height);
	}

	public void setOnPopMenuItemClickListener(
			OnPopMenuItemClickListener listener) {
		mPopMenuItemClickListener = listener;
	}

	private void InitPopMenu(Context context, BaseAdapter adapter, int width,
			int height) {
		mAdapter = adapter;

		View menuView = LayoutInflater.from(mContext).inflate(
				R.layout.pop_menu_layout, null);

		ListView menuListView = (ListView) menuView
				.findViewById(R.id.menulistView);
		menuListView.setAdapter(adapter);
		menuListView.setOnItemClickListener(this);

		mPopupWindow = new PopupWindow(menuView, width, height);
		mPopupWindow
				.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
	}

	public void menuActive(View parent) {
		if (mPopupWindow.isShowing())
			dismiss();
		else
			show(parent);
	}

	public boolean isShowing() {
		return mPopupWindow.isShowing();
	}

	// show as drop down
	public void showSingle(View parent) {
		mPopupWindow.showAsDropDown(parent, mContext.getResources()
				.getDimensionPixelSize(R.dimen.broadcast_popmenu_xoffset), 0);

		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.update();
	}
	
	   // show as drop down
    public void show(View parent) {
        mPopupWindow.showAsDropDown(parent, mContext.getResources()
                .getDimensionPixelSize(R.dimen.popmenu_xoffset), 0);

        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.update();
    }

	// dismiss
	public void dismiss() {
		mPopupWindow.dismiss();
	}

	/**
	 * This is default menu list adapter, if no custom adapter, will use this by
	 * default
	 * 
	 */
	private class DefaultMenuAdapter extends BaseAdapter {

		private String[] menuItem;

		public DefaultMenuAdapter(String[] menuItem) {
			this.menuItem = menuItem;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (menuItem == null)
				return 0;

			return menuItem.length;
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return menuItem[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub
			ViewHolder holder;
			if (view == null) {
				holder = new ViewHolder();
				view = LayoutInflater.from(mContext).inflate(
						R.layout.pop_menu_item_layout, null);
				holder.menuTextView = (TextView) view
						.findViewById(R.id.menuTextView);
				view.setTag(holder);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			holder.menuTextView.setText(menuItem[position]);

			return view;
		}

		private final class ViewHolder {
			TextView menuTextView;
		}
	}

	   /**
     * This is default menu list adapter, if no custom adapter, will use this by
     * default
     * 
     */
    private class SingleMenuAdapter extends BaseAdapter {

        private String[] menuItem;

        public SingleMenuAdapter(String[] menuItem) {
            this.menuItem = menuItem;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            if (menuItem == null)
                return 0;

            return menuItem.length;
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return menuItem[arg0];
        }

        @Override
        public long getItemId(int arg0) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
//            // TODO Auto-generated method stub
//            ViewHolder holder;
//            if (view == null) {
//                holder = new ViewHolder();
//                view = LayoutInflater.from(mContext).inflate(
//                        R.layout.pop_broadcast_layout, null);
//                holder.menuTextView = (TextView) view
//                        .findViewById(R.id.menuTextView);
//                view.setTag(holder);
//            } else {
//                holder = (ViewHolder) view.getTag();
//            }
//
//            holder.menuTextView.setText(menuItem[position]);
//
            return view;
        }

        private final class ViewHolder {
            TextView menuTextView;
        }
    }

	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		if (mPopMenuItemClickListener != null) {
			mPopMenuItemClickListener.onPopMenuItemClick(arg2,
					String.valueOf(mAdapter.getItem(arg2)));
		}
		dismiss();
	}

	/**
	 * This listener can return a String, when click menu item.
	 * 
	 * @author b456
	 * 
	 */
	public interface OnPopMenuItemClickListener {
		public void onPopMenuItemClick(int index, String menuText);
	}
}
