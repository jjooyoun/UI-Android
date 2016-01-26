package com.ui.component;

import com.ui.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RoundedImageView extends ImageView {
	private Context mContext;

	public RoundedImageView(Context context) {
		super(context);
		mContext = context;
	}

	public RoundedImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public RoundedImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		RoundedDrawable roundedDrawable = new RoundedDrawable(bitmap, mContext.getResources().getDimension(R.dimen.test_round_image_size));
		setImageDrawable(roundedDrawable);
	}
}
