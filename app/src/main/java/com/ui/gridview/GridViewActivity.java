package com.ui.gridview;

import java.util.ArrayList;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ui.R;

public class GridViewActivity extends Activity {

	private static final int DATA_QUERY_TOKEN = 41;

	private Context mContext;

	private GridView mGridView;
	private CustomGridViewAdapter mCustomGridViewAdapter;

	private DataQueryHandler mDataQueryHandler;

	private class DataQueryHandler extends AsyncQueryHandler {

		public DataQueryHandler(Context context) {
			super(context.getContentResolver());
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			mCustomGridViewAdapter.changeCursor(cursor);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);
		mContext = GridViewActivity.this;
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mDataQueryHandler == null) {
			mDataQueryHandler = new DataQueryHandler(GridViewActivity.this);
		}
		startDataQuery();
	}

	private void init() {
		mGridView = (GridView) findViewById(R.id.custom_gridview);
		mCustomGridViewAdapter = new CustomGridViewAdapter(GridViewActivity.this, null, true);
		mGridView.setAdapter(mCustomGridViewAdapter);
	}

	private void startDataQuery() {
		mDataQueryHandler.cancelOperation(DATA_QUERY_TOKEN);
		mDataQueryHandler.startQuery(DATA_QUERY_TOKEN, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
	}
}
