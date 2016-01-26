package com.ui.listview;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.GridView;
import android.widget.ListView;

import com.ui.R;
import com.ui.gridview.CustomGridViewAdapter;

public class ListViewActivity extends Activity {

	private static final int DATA_QUERY_TOKEN = 41;

	private Context mContext;

	private ListView mListView;
	private CustomListViewAdapter mCustomListViewAdapter;

	private DataQueryHandler mDataQueryHandler;

	private class DataQueryHandler extends AsyncQueryHandler {

		public DataQueryHandler(Context context) {
			super(context.getContentResolver());
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			super.onQueryComplete(token, cookie, cursor);
			mCustomListViewAdapter.changeCursor(cursor);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_listview);
		mContext = ListViewActivity.this;
		init();
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mDataQueryHandler == null) {
			mDataQueryHandler = new DataQueryHandler(ListViewActivity.this);
		}
		startDataQuery();
	}

	private void init() {
		mListView = (ListView) findViewById(R.id.custom_listview);
		mCustomListViewAdapter = new CustomListViewAdapter(ListViewActivity.this, null, true);
		mListView.setAdapter(mCustomListViewAdapter);
	}

	private void startDataQuery() {
		mDataQueryHandler.cancelOperation(DATA_QUERY_TOKEN);
		mDataQueryHandler.startQuery(DATA_QUERY_TOKEN, null, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
	}
}
