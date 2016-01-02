package com.ui.gridview;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ui.R;
import com.ui.util.ImageLoader;

public class CustomGridViewAdapter extends CursorAdapter {

	private LayoutInflater mLayoutInflater;
	private ImageLoader mImageLoader;

	public CustomGridViewAdapter(Context context, Cursor c, boolean autoRequery) {
		super(context, c, autoRequery);
		mLayoutInflater = LayoutInflater.from(context);
		mImageLoader = new ImageLoader(context.getApplicationContext());
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mLayoutInflater.inflate(R.layout.activity_gridview_layout, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.mThumbnailView = (ThumbnailView) view.findViewById(R.id.thumbnail_view);
		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ViewHolder holder = (ViewHolder) view.getTag();
		Uri dataUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				 cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)));
		 mImageLoader.displayImage(dataUri.toString(), holder.mThumbnailView);
	}

	private class ViewHolder {
		ThumbnailView mThumbnailView;
	}
}
