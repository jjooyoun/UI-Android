package com.ui.gridview;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListAdapter;

public class CustomGridView extends GridView {

	private static final int INVALID_ID = -1;
	private static final int ITEM_PLUS_ID = 0;
	private static final int COLUMN_NUM = 3;

	private static final int MOVE_DURATION = 300;
	private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 8;

	private BitmapDrawable mHoverCell;
	private Rect mHoverCellCurrentBounds;
	private Rect mHoverCellOriginalBounds;

	private int mTotalOffsetY = 0;
	private int mTotalOffsetX = 0;

	private int mDownX = -1;
	private int mDownY = -1;
	private int mLastEventY = -1;
	private int mLastEventX = -1;

	// used to distinguish straight line and diagonal switching
	private int mOverlapIfSwitchStraightLine;

	private long mMobileItemId = INVALID_ID;

	private boolean mCellIsMobile = false;
	private int mActivePointerId = INVALID_ID;

	private boolean mIsMobileScrolling;
	private int mSmoothScrollAmountAtEdge = 0;
	private boolean mIsWaitingForScrollFinish = false;
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	private boolean mIsEditMode = false;
	private boolean mHoverAnimation;
	private boolean mReorderAnimation;
	private boolean mIsEditModeEnabled = true;

	private OnDropListener mDropListener;
	private OnDragListener mDragListener;
	private OnEditModeChangeListener mEditModeChangeListener;

	private List<Long> idList = new ArrayList<Long>();

	public CustomGridView(Context context) {
		super(context);
		init(context);
	}

	public CustomGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public void init(Context context) {
		setOnScrollListener(mScrollListener);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE * metrics.density + 0.5f);
		// TODO
//		mOverlapIfSwitchStraightLine = getResources().getDimensionPixelSize(R.dimen.kiwiple_story_scenario_overlap_if_switch_straight_line);
	}

	/**
	 * Start edit mode with position.
	 */
	public void startEditMode(int position) {
		if (!mIsEditModeEnabled) {
			return;
		}
		requestDisallowInterceptTouchEvent(true);

		if (position != -1 && mDragListener != null) {
			startDragAtPosition(position);
		}
		mIsEditMode = true;
	}

	public void stopEditMode() {
		mIsEditMode = false;
		requestDisallowInterceptTouchEvent(false);

		if (mEditModeChangeListener != null)
			mEditModeChangeListener.onEditModeChanged(false);
	}

	/**
	 * Creates the hover cell with the appropriate bitmap and of appropriate
	 * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
	 * single time an invalidate call is made.
	 */
	private BitmapDrawable getAndAddHoverView(View v) {

		int w = v.getWidth();
		int h = v.getHeight();
		int top = v.getTop();
		int left = v.getLeft();

		// TODO
//		Bitmap b = getBitmapFromView(v);
		Bitmap b = null;

		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

		mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
		mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

		drawable.setBounds(mHoverCellCurrentBounds);

		return drawable;
	}

	/**
	 * Returns a bitmap showing a screenshot of the view passed in.
	 */
	// TODO
//	private Bitmap getBitmapFromView(View v) {
//
//		View view = v.findViewById(R.id.kiwiple_story_scenario_thumb);
//		view.clearFocus();
//		boolean flag = view.willNotCacheDrawing();
//		view.setWillNotCacheDrawing(false);
//		view.buildDrawingCache();
//		Bitmap bitmap = view.getDrawingCache();
//		Bitmap scaleBitmap = null;
//		if(bitmap != null){
//			scaleBitmap = Bitmap.createScaledBitmap(bitmap, v.getWidth(), v.getHeight(), false);
//			view.destroyDrawingCache();
//			view.setWillNotCacheDrawing(flag);
//		}
//		return scaleBitmap;
//	}

	private void updateNeighborViewsForId(long itemId) {
		idList.clear();
		int draggedPos = getPositionForID(itemId);
//		Log.d("itemId " + itemId + ", draggedPos : " + draggedPos + ", first?VisiblePosition : " + getFirstVisiblePosition()
//				+ ", lastVisiblePosition : " + getLastVisiblePosition());
//		Log.d("All items count : " + getScenarioEditAdapter().getCount() + ", childCount : " + getChildCount() + ", draggedPos :" + draggedPos);
		for (int pos = getFirstVisiblePosition(); pos <= getLastVisiblePosition(); pos++) {
			if (draggedPos != pos) {
				idList.add(getId(pos));
			}
		}

	}

	/**
	 * Retrieves the position in the grid corresponding to <code>itemId</code>
	 */
	public int getPositionForID(long itemId) {
		View v = getViewForId(itemId);
		if (v == null) {
			return -1;
		} else {
			return getPositionForView(v);
		}
	}

	public View getViewForId(long itemId) {
		int firstVisiblePosition = getFirstVisiblePosition();
		ListAdapter adapter = getAdapter();
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int position = firstVisiblePosition + i;
			long id = adapter.getItemId(position);
			if (id == itemId) {
				return v;
			}
		}
		return null;
	}

	private long getId(int position) {
		return getAdapter().getItemId(position);
	}

	public void setOnDropListener(OnDropListener dropListener) {
		this.mDropListener = dropListener;
	}

	public void setOnDragListener(OnDragListener dragListener) {
		this.mDragListener = dragListener;
	}

	public interface OnDropListener {
		void onActionDrop();
	}

	public interface OnDragListener {

		public void onDragStarted(int position);

		public void onDragPositionsChanged(int oldPosition, int newPosition);
	}

	public interface OnEditModeChangeListener {
		public void onEditModeChanged(boolean inEditMode);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int moveX, moveY;
		int moveId = INVALID_ID;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			mDownX = (int) event.getX();
			mDownY = (int) event.getY();
			mActivePointerId = event.getPointerId(0);

			if (mIsEditMode && isEnabled()) {
				layoutChildren();
				int position = pointToPosition(mDownX, mDownY);
				startDragAtPosition(position);
			} else if (!isEnabled()) {
				return false;
			}

			break;
		case MotionEvent.ACTION_MOVE:

			moveX = (int) event.getX();
			moveY = (int) event.getY();
			moveId = pointToPosition(moveX, moveY) - getFirstVisiblePosition();

			if (mActivePointerId == INVALID_ID) {
				break;
			}

			int pointerIndex = event.findPointerIndex(mActivePointerId);

			mLastEventY = (int) event.getY(pointerIndex);
			mLastEventX = (int) event.getX(pointerIndex);
			int deltaY = mLastEventY - mDownY;
			int deltaX = mLastEventX - mDownX;

			if (mCellIsMobile) {
				mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left + deltaX + mTotalOffsetX, mHoverCellOriginalBounds.top + deltaY
						+ mTotalOffsetY);
				mHoverCell.setBounds(mHoverCellCurrentBounds);
				invalidate();
				if (moveId >= ITEM_PLUS_ID) {
					handleCellSwitch();
				}
				mIsMobileScrolling = false;
				handleMobileCellScroll();
				return false;
			}

			break;
		case MotionEvent.ACTION_UP:
			touchEventsEnded();
			if (mDropListener != null) {
				mDropListener.onActionDrop();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			touchEventsCancelled();
			if (mDropListener != null) {
				mDropListener.onActionDrop();
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);
			if (pointerId == mActivePointerId) {
				touchEventsEnded();
			}
			break;
		default:
			break;
		}
		return super.onTouchEvent(event);
	}

	private void touchEventsEnded() {
		final View mobileView = getViewForId(mMobileItemId);
		if (mobileView != null && (mCellIsMobile || mIsWaitingForScrollFinish)) {
			mCellIsMobile = false;
			mIsWaitingForScrollFinish = false;
			mIsMobileScrolling = false;
			mActivePointerId = INVALID_ID;

			// If the autoscroller has not completed scrolling, we need to wait
			// for it to
			// finish in order to determine the final location of where the
			// hover cell
			// should be animated to.
			if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				mIsWaitingForScrollFinish = true;
				return;
			}

			mHoverCellCurrentBounds.offsetTo(mobileView.getLeft(), mobileView.getTop());

			mHoverCell.setBounds(mHoverCellCurrentBounds);
			invalidate();
			reset(mobileView);
		} else {
			touchEventsCancelled();
		}
	}

	private void touchEventsCancelled() {
		View mobileView = getViewForId(mMobileItemId);
		if (mCellIsMobile) {
			reset(mobileView);
		}
		mIsEditMode = true;
		mCellIsMobile = false;
		mIsMobileScrolling = false;
		mActivePointerId = INVALID_ID;

	}

	private void reset(View mobileView) {
		idList.clear();
		mMobileItemId = INVALID_ID;
		mobileView.setVisibility(View.VISIBLE);
		mHoverCell = null;

		invalidate();
	}

	private void handleMobileCellScroll() {
		mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
	}

	public boolean handleMobileCellScroll(Rect r) {
		int offset = computeVerticalScrollOffset();
		int height = getHeight();
		int extent = computeVerticalScrollExtent();
		int range = computeVerticalScrollRange();
		int hoverViewTop = r.top;
		int hoverHeight = r.height();

		if (hoverViewTop <= 0 && offset > 0) {
			smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
			smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		return false;
	}

	private boolean belowLeft(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y > mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
	}

	private boolean belowRight(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y > mobileColumnRowPair.y && targetColumnRowPair.x > mobileColumnRowPair.x;
	}

	private boolean aboveLeft(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y < mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
	}

	private boolean aboveRight(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y < mobileColumnRowPair.y && targetColumnRowPair.x > mobileColumnRowPair.x;
	}

	private boolean above(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y < mobileColumnRowPair.y && targetColumnRowPair.x == mobileColumnRowPair.x;
	}

	private boolean below(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y > mobileColumnRowPair.y && targetColumnRowPair.x == mobileColumnRowPair.x;
	}

	private boolean right(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y == mobileColumnRowPair.y && targetColumnRowPair.x > mobileColumnRowPair.x;
	}

	private boolean left(Point targetColumnRowPair, Point mobileColumnRowPair) {
		boolean val = targetColumnRowPair.y == mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
		return val;
	}

	private Point getColumnAndRowForView(View view) {
		int pos = getPositionForView(view);
		int columns = COLUMN_NUM;
		int column = pos % columns;
		int row = pos / columns;
		return new Point(column, row);
	}

	private void reorderElements(int originalPosition, int targetPosition) {
		if (mDragListener != null) {
			mDragListener.onDragPositionsChanged(originalPosition, targetPosition);
		}

//		getScenarioEditAdapter().reorderItems(originalPosition, targetPosition);
	}

	public CustomGridViewAdapter getScenarioEditAdapter() {
		CustomGridViewAdapter adapter = (CustomGridViewAdapter) getAdapter();
		if (adapter != null) {
			return (CustomGridViewAdapter) adapter;
		}
		return null;
	}

	private void handleCellSwitch() {
		final int deltaY = mLastEventY - mDownY;
		final int deltaX = mLastEventX - mDownX;
		final int deltaYTotal = mHoverCellOriginalBounds.centerY() + mTotalOffsetY + deltaY;
		final int deltaXTotal = mHoverCellOriginalBounds.centerX() + mTotalOffsetX + deltaX;
		View mobileView = getViewForId(mMobileItemId);
		if (mobileView == null)
			return;
		View targetView = null;
		float vX = 0;
		float vY = 0;
		Point mobileColumnRowPair = getColumnAndRowForView(mobileView);

//		L.d("mobile id : " + mMobileItemId);
//		L.d("mobile column : " + mobileColumnRowPair.x + ", row : " + mobileColumnRowPair.y);
//		L.d("mobile view (" + mobileView.getLeft() + ", " + mobileView.getTop() + ", " + mobileView.getRight() + ", " + mobileView.getBottom() + ")");

		for (Long id : idList) {
			View view = getViewForId(id);
			if (view != null) {
				Point targetColumnRowPair = getColumnAndRowForView(view);
				if ((aboveRight(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom() && deltaXTotal > view.getLeft()
						|| aboveLeft(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom() && deltaXTotal < view.getRight()
						|| belowRight(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop() && deltaXTotal > view.getLeft()
						|| belowLeft(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop() && deltaXTotal < view.getRight()
						|| above(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom() - mOverlapIfSwitchStraightLine
						|| below(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop() + mOverlapIfSwitchStraightLine
						|| right(targetColumnRowPair, mobileColumnRowPair) && deltaXTotal > view.getLeft() + mOverlapIfSwitchStraightLine || left(
						targetColumnRowPair, mobileColumnRowPair) && deltaXTotal < view.getRight() - mOverlapIfSwitchStraightLine)) {

					// TODO
//					float xDiff = Math.abs(ScenarioEditGridViewUtil.getViewX(view) - ScenarioEditGridViewUtil.getViewX(mobileView));
//					float yDiff = Math.abs(ScenarioEditGridViewUtil.getViewY(view) - ScenarioEditGridViewUtil.getViewY(mobileView));
//					if (xDiff >= vX && yDiff >= vY) {
//						vX = xDiff;
//						vY = yDiff;
//						targetView = view;
//					}
				}
			}
		}
		if (targetView != null) {
			final int originalPosition = getPositionForView(mobileView);
			int targetPosition = getPositionForView(targetView);

			if (targetPosition == INVALID_POSITION) {
				updateNeighborViewsForId(mMobileItemId);
				return;
			}
			reorderElements(originalPosition, targetPosition);

			mDownY = mLastEventY;
			mDownX = mLastEventX;
			mobileView.setVisibility(View.VISIBLE);
			targetView.setVisibility(View.INVISIBLE);

			updateNeighborViewsForId(mMobileItemId);

			final ViewTreeObserver observer = getViewTreeObserver();
			final int finalTargetPosition = targetPosition;
			if (observer != null) {
				observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						observer.removeOnPreDrawListener(this);
						mTotalOffsetY += deltaY;
						mTotalOffsetX += deltaX;
						animateReorder(originalPosition, finalTargetPosition);
						return true;
					}
				});
			} else {
				mTotalOffsetY += deltaY;
				mTotalOffsetX += deltaX;
			}
		}
	}

	private void animateReorder(final int oldPosition, final int newPosition) {
		boolean isForward = newPosition > oldPosition;
		List<Animator> resultList = new LinkedList<Animator>();
		if (isForward) {
			for (int pos = Math.min(oldPosition, newPosition); pos < Math.max(oldPosition, newPosition); pos++) {
				View view = getViewForId(getId(pos));
				if ((pos + 1) % COLUMN_NUM == 0) {
					resultList.add(createTranslationAnimations(view, -view.getWidth() * (COLUMN_NUM - 1), 0, view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view, view.getWidth(), 0, 0, 0));
				}
			}
		} else {
			for (int pos = Math.max(oldPosition, newPosition); pos > Math.min(oldPosition, newPosition); pos--) {
				View view = getViewForId(getId(pos));
				if ((pos + COLUMN_NUM) % COLUMN_NUM == 0) {
					resultList.add(createTranslationAnimations(view, view.getWidth() * (COLUMN_NUM - 1), 0, -view.getHeight(), 0));
				} else {
					resultList.add(createTranslationAnimations(view, -view.getWidth(), 0, 0, 0));
				}
			}
		}

		AnimatorSet resultSet = new AnimatorSet();
		resultSet.playTogether(resultList);
		resultSet.setDuration(MOVE_DURATION);
		resultSet.setInterpolator(new AccelerateDecelerateInterpolator());
		resultSet.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationStart(Animator animation) {
				mReorderAnimation = true;
				updateEnableState();
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				mReorderAnimation = false;
				updateEnableState();
			}
		});
		resultSet.start();
	}

	private AnimatorSet createTranslationAnimations(View view, float startX, float endX, float startY, float endY) {
		ObjectAnimator animX = ObjectAnimator.ofFloat(view, "translationX", startX, endX);
		ObjectAnimator animY = ObjectAnimator.ofFloat(view, "translationY", startY, endY);
		AnimatorSet animSetXY = new AnimatorSet();
		animSetXY.playTogether(animX, animY);
		return animSetXY;
	}

	private void updateEnableState() {
		setEnabled(!mHoverAnimation && !mReorderAnimation);
	}

	private void startDragAtPosition(int position) {
		mTotalOffsetY = 0;
		mTotalOffsetX = 0;
		int itemNum = position - getFirstVisiblePosition();
		View selectedView = getChildAt(itemNum);
		if (selectedView != null) {
			mMobileItemId = getAdapter().getItemId(position);
			mHoverCell = getAndAddHoverView(selectedView);
			selectedView.setVisibility(View.INVISIBLE);
			mCellIsMobile = true;
			updateNeighborViewsForId(mMobileItemId);
			if (mDragListener != null) {
				mDragListener.onDragStarted(position);
			}
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mHoverCell != null) {
			mHoverCell.draw(canvas);
		}
	}

	/**
	 * This scroll listener is added to the gridview in order to handle cell
	 * swapping when the cell is either at the top or bottom edge of the
	 * gridview. If the hover cell is at either edge of the gridview, the
	 * gridview will begin scrolling. As scrolling takes place, the gridview
	 * continuously checks if new cells became visible and determines whether
	 * they are potential candidates for a cell swap.
	 */
	private OnScrollListener mScrollListener = new OnScrollListener() {

		private int mPreviousFirstVisibleItem = -1;
		private int mPreviousVisibleItemCount = -1;
		private int mCurrentFirstVisibleItem;
		private int mCurrentVisibleItemCount;
		private int mCurrentScrollState;

		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			mCurrentFirstVisibleItem = firstVisibleItem;
			mCurrentVisibleItemCount = visibleItemCount;

			mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
			mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount : mPreviousVisibleItemCount;

			checkAndHandleFirstVisibleCellChange();
			checkAndHandleLastVisibleCellChange();

			mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
			mPreviousVisibleItemCount = mCurrentVisibleItemCount;

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			mCurrentScrollState = scrollState;
			mScrollState = scrollState;
			isScrollCompleted();
		}

		/**
		 * This method is in charge of invoking 1 of 2 actions. Firstly, if the
		 * gridview is in a state of scrolling invoked by the hover cell being
		 * outside the bounds of the gridview, then this scrolling event is
		 * continued. Secondly, if the hover cell has already been released,
		 * this invokes the animation for the hover cell to return to its
		 * correct position after the gridview has entered an idle scroll state.
		 */
		private void isScrollCompleted() {
			if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
				if (mCellIsMobile && mIsMobileScrolling) {
					handleMobileCellScroll();
				} else if (mIsWaitingForScrollFinish) {
					touchEventsEnded();
				}
			}
		}

		/**
		 * Determines if the gridview scrolled up enough to reveal a new cell at
		 * the top of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleFirstVisibleCellChange() {
			if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForId(mMobileItemId);
					handleCellSwitch();
				}
			}
		}

		/**
		 * Determines if the gridview scrolled down enough to reveal a new cell
		 * at the bottom of the list. If so, then the appropriate parameters are
		 * updated.
		 */
		public void checkAndHandleLastVisibleCellChange() {
			int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
			int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
			if (currentLastVisibleItem != previousLastVisibleItem) {
				if (mCellIsMobile && mMobileItemId != INVALID_ID) {
					updateNeighborViewsForId(mMobileItemId);
					handleCellSwitch();
				}
			}
		}
	};

}
