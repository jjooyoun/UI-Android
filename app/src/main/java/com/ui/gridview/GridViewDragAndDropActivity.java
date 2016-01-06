package com.ui.gridview;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;

import com.ui.R;

import java.util.ArrayList;

public class GridViewDragAndDropActivity extends Activity implements OnItemLongClickListener  {

	private Context mContext;

	private CustomGridView mCustomGridView;
	private CustomGridViewDragAndDropAdapter mCustomGridViewDragAndDropAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview_drag_drop);
		mContext = GridViewDragAndDropActivity.this;
		init();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		mCustomGridView.startEditMode(position);
		return true;
	}

	private void init() {
		mCustomGridView = (CustomGridView) findViewById(R.id.custom_gridview);
		mCustomGridViewDragAndDropAdapter = new CustomGridViewDragAndDropAdapter(GridViewDragAndDropActivity.this, R.layout.activity_gridview_layout, makePhotoList());
		mCustomGridView.setAdapter(mCustomGridViewDragAndDropAdapter);
		mCustomGridView.setOnItemLongClickListener(this);

		mCustomGridView.setOnDropListener(new CustomGridView.OnDropListener() {

			@Override
			public void onActionDrop() {
				mCustomGridView.stopEditMode();
			}
		});
		mCustomGridView.setOnDragListener(new CustomGridView.OnDragListener() {

			@Override
			public void onDragStarted(int position) {
			}

			@Override
			public void onDragPositionsChanged(int oldPosition, int newPosition) {
			}
		});
	}

	private ArrayList<PhotoData> makePhotoList() {
		ArrayList<PhotoData> mPhotoList = new ArrayList<PhotoData>();

		Cursor cursor = GridViewDragAndDropActivity.this.getContentResolver().
				query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.MediaColumns._ID}, null, null, null);

		if (cursor == null) {
			return mPhotoList;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return mPhotoList;
		}

		int count = cursor.getCount() > 50 ? 50 : cursor.getCount();

		cursor.moveToNext();
		for (int i = 0; i < count; i++) {
			PhotoData photoData = new PhotoData();
			photoData.setUri(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))));
			mPhotoList.add(photoData);
			cursor.moveToNext();
		}

		if (cursor != null) {
			cursor.close();
		}
		return mPhotoList;
	}
}
