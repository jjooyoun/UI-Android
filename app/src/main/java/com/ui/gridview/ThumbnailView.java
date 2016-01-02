package com.ui.gridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ThumbnailView extends ImageView {

	public static enum LoadState {
		DONE, QUEUE, LOAD
	};

	private LoadState mLoadState = LoadState.DONE;

	private Matrix mMatrix = new Matrix();
	private float[] mMatrixValue = new float[9];

	public ThumbnailView(Context context) {
		super(context);
		init(context);
	}

	public ThumbnailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		setScaleType(ScaleType.MATRIX);
		mMatrix.getValues(mMatrixValue);
	}

	public void setLoadState(LoadState state) {
		mLoadState = state;
	}

	public LoadState getLoadState() {
		return mLoadState;
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);

		if (bm != null) {
			setImageFitOnView(getWidth(), getHeight());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setImageFitOnView(w, h);
	}

	private void setImageFitOnView(int widthOfView, int heightOfView) {
		Drawable drawable = getDrawable();
		if (drawable == null) {
			return;
		}

		int imageW = drawable.getIntrinsicWidth();
		int imageH = drawable.getIntrinsicHeight();
		if (imageW < imageH) {
			mMatrixValue[0] = mMatrixValue[4] = widthOfView / (float) imageW;
		} else {
			mMatrixValue[0] = mMatrixValue[4] = heightOfView / (float) imageH;
		}

		int scaleW = Math.round(imageW * mMatrixValue[0]);
		int scaleH = Math.round(imageH * mMatrixValue[4]);
		mMatrixValue[2] = (widthOfView / 2.f) - (scaleW / 2.f);
		mMatrixValue[5] = (heightOfView / 2.f) - (scaleH / 2.f);

		mMatrix.setValues(mMatrixValue);
		setImageMatrix(mMatrix);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
	}
}