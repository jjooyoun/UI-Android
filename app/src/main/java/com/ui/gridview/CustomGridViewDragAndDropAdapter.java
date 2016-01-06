package com.ui.gridview;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;

import com.ui.R;
import com.ui.util.ImageLoader;

import java.util.ArrayList;

public class CustomGridViewDragAndDropAdapter extends ArrayAdapter<PhotoData> {

	private LayoutInflater mLayoutInflater;
	private ImageLoader mImageLoader;
	private ArrayList<PhotoData> mItemList;

	public CustomGridViewDragAndDropAdapter(Context context, int textViewResourceId, ArrayList<PhotoData> itemlist) {
		super(context, textViewResourceId, itemlist);
		mLayoutInflater = LayoutInflater.from(context);
		mImageLoader = new ImageLoader(context.getApplicationContext());
		mItemList = itemlist;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mLayoutInflater.inflate(R.layout.activity_gridview_layout, parent, false);
			holder = new ViewHolder();
			holder.mThumbnailView = (ThumbnailView) convertView.findViewById(R.id.thumbnail_view);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		PhotoData photoData = mItemList.get(position);
		mImageLoader.displayImage(photoData.getUri().toString(), holder.mThumbnailView);
		return convertView;
	}

	private class ViewHolder {
		ThumbnailView mThumbnailView;
	}
}
