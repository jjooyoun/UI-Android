package com.ui.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.net.Uri;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.ui.R;

public class ImageLoader {
	private static final int THUMBNAIL_SIZE = 300;

	private MemoryCache mMemoryCache = new MemoryCache();
	private Map<ImageView, String> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());
	private ExecutorService mExecutorService;
	private Context mContext;
	private Handler mHandler = new Handler();

	public ImageLoader(Context context) {
		mContext = context;
		mExecutorService = Executors.newFixedThreadPool(5);
	}

	public void displayImage(String url, ImageView imageView) {
		mImageViews.put(imageView, url);
		Bitmap bitmap = mMemoryCache.get(url);
		if (bitmap != null) {
			imageView.setImageBitmap(bitmap);
		} else {
			queuePhoto(url, imageView);
			imageView.setImageResource(R.mipmap.bg);
			imageView.setScaleType(ImageView.ScaleType.FIT_XY);
		}
	}

	public void clear() {
		mImageViews.clear();
		clearCache();
	}

	public void clearCache() {
		mMemoryCache.clear();
	}

	private void queuePhoto(String url, ImageView imageView) {
		PhotoToLoad p = new PhotoToLoad(url, imageView);
		mExecutorService.submit(new PhotosLoader(p));
	}

	private Bitmap getBitmap(String url) {
		try {
			Bitmap bitmap = null;
			bitmap = decodeFromUri(Uri.parse(url));
			return bitmap;
		} catch (Throwable ex) {
			ex.printStackTrace();
			if (ex instanceof OutOfMemoryError) {
				mMemoryCache.clear();
			}
			return null;
		}
	}

	private Bitmap decodeFromUri(Uri uri) {
		InputStream input;
		Bitmap bitmap = null;
		try {
			input = mContext.getContentResolver().openInputStream(uri);

			BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
			onlyBoundsOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
			input.close();
			if ((onlyBoundsOptions.outWidth == -1)
					|| (onlyBoundsOptions.outHeight == -1)) {
				return null;
			}

			int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight
					: onlyBoundsOptions.outWidth;

			double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE)
					: 1.0;

			BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
			bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
			input = mContext.getContentResolver().openInputStream(uri);
			bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
			input.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private int getPowerOfTwoForSampleRatio(double ratio) {
		int k = Integer.highestOneBit((int) Math.floor(ratio));
		if (k == 0) {
			return 1;
		} else {
			return k;
		}
	}

	private boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = mImageViews.get(photoToLoad.mImageView);
		if (tag == null || !tag.equals(photoToLoad.mUrl)) {
			return true;
		}
		return false;
	}

	private class PhotoToLoad {
		public String mUrl;
		public ImageView mImageView;

		public PhotoToLoad(String u, ImageView i) {
			mUrl = u;
			mImageView = i;
		}
	}

	private class PhotosLoader implements Runnable {
		PhotoToLoad mPhotoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.mPhotoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {
				if (imageViewReused(mPhotoToLoad)) {
					return;
				}

				Bitmap bmp = getBitmap(mPhotoToLoad.mUrl);
				mMemoryCache.put(mPhotoToLoad.mUrl, bmp);

				if (imageViewReused(mPhotoToLoad)) {
					return;
				}
				BitmapDisplayer bd = new BitmapDisplayer(bmp, mPhotoToLoad);
				mHandler.post(bd);
			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	private class BitmapDisplayer implements Runnable {
		Bitmap mBitmap;
		PhotoToLoad mPhotoToLoad;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
			mBitmap = b;
			mPhotoToLoad = p;
		}

		@Override
		public void run() {
			if (imageViewReused(mPhotoToLoad)) {
				return;
			}
			if (mBitmap != null) {
				mPhotoToLoad.mImageView.setImageBitmap(mBitmap);
			} else {
				mPhotoToLoad.mImageView.setImageResource(R.mipmap.bg);
				mPhotoToLoad.mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
			}

		}
	}
}
