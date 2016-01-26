package com.ui.component;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

public class RoundedDrawable extends Drawable {
	private final Paint mPaint;
	private final RectF mRectF;
	private final int mBitmapWidth;
	private final int mBitmapHeight;

	public RoundedDrawable(Bitmap bitmap, float size) {
		mRectF = new RectF();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);

		BitmapShader shader = new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
		mPaint.setShader(shader);

		mBitmapWidth = (int) size;
		mBitmapHeight = (int) size;
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawOval(mRectF, mPaint);
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);
		mRectF.set(bounds);
	}

	@Override
	public void setAlpha(int alpha) {
		if (mPaint.getAlpha() != alpha) {
			mPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public int getIntrinsicWidth() {
		return mBitmapWidth;
	}

	@Override
	public int getIntrinsicHeight() {
		return mBitmapHeight;
	}

	@Override
	public void setFilterBitmap(boolean filter) {
		mPaint.setFilterBitmap(filter);
		invalidateSelf();
	}

	@Override
	public void setDither(boolean dither) {
		mPaint.setDither(dither);
		invalidateSelf();
	}
}
