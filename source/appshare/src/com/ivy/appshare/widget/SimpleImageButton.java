package com.ivy.appshare.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.ivy.appshare.R;

public class SimpleImageButton extends ImageButton {

	public SimpleImageButton(Context context) {
		this(context, null);
	}

	public SimpleImageButton(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SimpleImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOnTouch(context);
		setFocusable(true);
		setScaleType(ImageView.ScaleType.FIT_CENTER);
		setBackgroundColor(Color.TRANSPARENT);
	}

	private void setOnTouch(final Context context) {
		this.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					v.setBackgroundColor(context.getResources().getColor(
							R.color.image_button_on_focus));
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_CANCEL:
					v.setBackgroundColor(Color.TRANSPARENT);
					break;
				default:
					break;
				}
				return false;
			}
		});
	}

}
