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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.GridView;

import com.ui.R;

public class CustomGridView extends GridView {

	private static final int COLUMN_NUM = 3;

	private static final int INVALID_ID = -1;
	private static final int CELL_AVAILABLE_ID = 0;

	private static final int MOVE_DURATION = 300;
	private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 8;

	private long mMoveItemId = INVALID_ID;
	private int mActivePointerId = INVALID_ID;

	private BitmapDrawable mMoveCell;
	private Rect mMoveCellCurrentBounds;
	private Rect mMoveCellOriginalBounds;

	private int mTotalOffsetY = 0;
	private int mTotalOffsetX = 0;

	private int mDownX = -1;
	private int mDownY = -1;
	private int mLastEventY = -1;
	private int mLastEventX = -1;

	private boolean mCellIsMove = false;
	private boolean mIsMoveViewScrolling;

	private int mSmoothScrollAmountAtEdge = 0;

	private boolean mIsWaitingForScrollFinish = false;
	private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	private boolean mIsEditMode = false;
	private boolean mReorderAnimation;

	private List<Long> mNeighborIdList = new ArrayList<Long>();

	public interface OnDropListener {
		void onActionDrop();
	}
	private OnDropListener mDropListener;

	private OnScrollListener mScrollListener = new OnScrollListener() {
		private int mPreviousFirstVisibleItem = -1;
		private int mPreviousVisibleItemCount = -1;
		private int mCurrentFirstVisibleItem;
		private int mCurrentVisibleItemCount;
		private int mCurrentScrollState;

		@Override
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

		private void checkAndHandleFirstVisibleCellChange() {
			if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
				if (mCellIsMove && mMoveItemId != INVALID_ID) {
					updateNeighborViewsForId(mMoveItemId);
					handleMoveCellSwitch();
				}
			}
		}

		private void checkAndHandleLastVisibleCellChange() {
			int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
			int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
			if (currentLastVisibleItem != previousLastVisibleItem) {
				if (mCellIsMove && mMoveItemId != INVALID_ID) {
					updateNeighborViewsForId(mMoveItemId);
					handleMoveCellSwitch();
				}
			}
		}

		private void isScrollCompleted() {
			if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
				if (mCellIsMove && mIsMoveViewScrolling) {
					handleMoveCellScroll();
				} else if (mIsWaitingForScrollFinish) {
					touchEventsEnded();
				}
			}
		}
	};

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

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		if (mMoveCell != null) {
			mMoveCell.draw(canvas);
		}
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

				if (mCellIsMove) {
					mMoveCellCurrentBounds.offsetTo(mMoveCellOriginalBounds.left + deltaX + mTotalOffsetX, mMoveCellOriginalBounds.top + deltaY
							+ mTotalOffsetY);
					mMoveCell.setBounds(mMoveCellCurrentBounds);
					invalidate();
					if (moveId >= CELL_AVAILABLE_ID) {
						handleMoveCellSwitch();
					}
					mIsMoveViewScrolling = false;
					handleMoveCellScroll();
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

			default:
				break;
		}
		return super.onTouchEvent(event);
	}

	public void init(Context context) {
		setOnScrollListener(mScrollListener);
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE * metrics.density + 0.5f);
	}

	public void startEditMode(int position) {
		requestDisallowInterceptTouchEvent(true);
		if (position != -1) {
			startDragAtPosition(position);
		}
		mIsEditMode = true;
	}

	public void stopEditMode() {
		mIsEditMode = false;
		requestDisallowInterceptTouchEvent(false);
	}

	public void setOnDropListener(OnDropListener dropListener) {
		this.mDropListener = dropListener;
	}

	private void startDragAtPosition(int position) {
		mTotalOffsetY = 0;
		mTotalOffsetX = 0;
		int itemNum = position - getFirstVisiblePosition();
		View selectedView = getChildAt(itemNum);
		if (selectedView != null) {
			mMoveItemId = getAdapter().getItemId(position);
			mMoveCell = getMoveView(selectedView);
			selectedView.setVisibility(View.INVISIBLE);
			mCellIsMove = true;
			updateNeighborViewsForId(mMoveItemId);
		}
	}

	private BitmapDrawable getMoveView(View v) {
		int w = v.getWidth();
		int h = v.getHeight();
		int top = v.getTop();
		int left = v.getLeft();

		Bitmap b = getBitmapFromView(v);
		BitmapDrawable drawable = new BitmapDrawable(getResources(), b);
		mMoveCellOriginalBounds = new Rect(left, top, left + w, top + h);
		mMoveCellCurrentBounds = new Rect(mMoveCellOriginalBounds);
		drawable.setBounds(mMoveCellCurrentBounds);
		return drawable;
	}

	private Bitmap getBitmapFromView(View v) {
		View view = v.findViewById(R.id.thumbnail_view);
		view.clearFocus();
		view.setWillNotCacheDrawing(false);
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		Bitmap scaleBitmap = null;
		if(bitmap != null){
			scaleBitmap = Bitmap.createScaledBitmap(bitmap, v.getWidth(), v.getHeight(), false);
			view.destroyDrawingCache();
			view.setWillNotCacheDrawing(view.willNotCacheDrawing());
		}
		return scaleBitmap;
	}

	private void updateNeighborViewsForId(long itemId) {
		mNeighborIdList.clear();
		int draggedPos = getPositionForId(itemId);
		for (int pos = getFirstVisiblePosition(); pos <= getLastVisiblePosition(); pos++) {
			if (draggedPos != pos) {
				mNeighborIdList.add(getId(pos));
			}
		}
	}

	private int getPositionForId(long itemId) {
		View v = getViewForId(itemId);
		if (v == null) {
			return -1;
		} else {
			return getPositionForView(v);
		}
	}

	private View getViewForId(long itemId) {
		int firstVisiblePosition = getFirstVisiblePosition();
		for (int i = 0; i < getChildCount(); i++) {
			View v = getChildAt(i);
			int position = firstVisiblePosition + i;
			long id = getAdapter().getItemId(position);
			if (id == itemId) {
				return v;
			}
		}
		return null;
	}

	private long getId(int position) {
		return getAdapter().getItemId(position);
	}

	private void touchEventsEnded() {
		final View moveView = getViewForId(mMoveItemId);
		if (moveView != null && (mCellIsMove || mIsWaitingForScrollFinish)) {
			mCellIsMove = false;
			mIsWaitingForScrollFinish = false;
			mIsMoveViewScrolling = false;
			mActivePointerId = INVALID_ID;

			if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				mIsWaitingForScrollFinish = true;
				return;
			}

			mMoveCellCurrentBounds.offsetTo(moveView.getLeft(), moveView.getTop());
			mMoveCell.setBounds(mMoveCellCurrentBounds);
			invalidate();
			reset(moveView);
		} else {
			touchEventsCancelled();
		}
	}

	private void touchEventsCancelled() {
		View moveView = getViewForId(mMoveItemId);
		if (mCellIsMove) {
			reset(moveView);
		}
		mIsEditMode = true;
		mCellIsMove = false;
		mIsMoveViewScrolling = false;
		mActivePointerId = INVALID_ID;
	}

	private void reset(View mobileView) {
		mNeighborIdList.clear();
		mMoveItemId = INVALID_ID;
		mobileView.setVisibility(View.VISIBLE);
		mMoveCell = null;
		invalidate();
	}

	private void handleMoveCellScroll() {
		mIsMoveViewScrolling = handleMoveCellScroll(mMoveCellCurrentBounds);
	}

	private boolean handleMoveCellScroll(Rect r) {
		int offset = computeVerticalScrollOffset();
		int height = getHeight();
		int extent = computeVerticalScrollExtent();
		int range = computeVerticalScrollRange();
		int moveViewTop = r.top;
		int moveViewHeight = r.height();

		if (moveViewTop <= 0 && offset > 0) {
			smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
			return true;
		}

		if (moveViewTop + moveViewHeight >= height && (offset + extent) < range) {
			smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
			return true;
		}
		return false;
	}

	private void handleMoveCellSwitch() {
		final int deltaY = mLastEventY - mDownY;
		final int deltaX = mLastEventX - mDownX;
		int deltaYTotal = mMoveCellOriginalBounds.centerY() + mTotalOffsetY + deltaY;
		int deltaXTotal = mMoveCellOriginalBounds.centerX() + mTotalOffsetX + deltaX;

		View moveView = getViewForId(mMoveItemId);
		if (moveView == null) {
			return;
		}

		View targetView = null;
		float vX = 0;
		float vY = 0;
		Point mobileColumnRowPair = getColumnAndRowForView(moveView);

		for (Long id : mNeighborIdList) {
			View view = getViewForId(id);
			if (view != null) {
				Point targetColumnRowPair = getColumnAndRowForView(view);
				if ((aboveRight(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom() && deltaXTotal > view.getLeft()
						|| aboveLeft(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom() && deltaXTotal < view.getRight()
						|| belowRight(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop() && deltaXTotal > view.getLeft()
						|| belowLeft(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop() && deltaXTotal < view.getRight()
						|| above(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal < view.getBottom()
						|| below(targetColumnRowPair, mobileColumnRowPair) && deltaYTotal > view.getTop()
						|| right(targetColumnRowPair, mobileColumnRowPair) && deltaXTotal > view.getLeft()
						|| left(targetColumnRowPair, mobileColumnRowPair) && deltaXTotal < view.getRight())) {

					float xDiff = Math.abs(getViewX(view) - getViewX(moveView));
					float yDiff = Math.abs(getViewY(view) - getViewY(moveView));
					if (xDiff >= vX && yDiff >= vY) {
						vX = xDiff;
						vY = yDiff;
						targetView = view;
					}
				}
			}
		}
		if (targetView != null) {
			final int originalPosition = getPositionForView(moveView);
			int targetPosition = getPositionForView(targetView);

			if (targetPosition == INVALID_POSITION) {
				updateNeighborViewsForId(mMoveItemId);
				return;
			}
			reorderView(originalPosition, targetPosition);

			mDownY = mLastEventY;
			mDownX = mLastEventX;
			moveView.setVisibility(View.VISIBLE);
			targetView.setVisibility(View.INVISIBLE);

			updateNeighborViewsForId(mMoveItemId);

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

	private Point getColumnAndRowForView(View view) {
		int pos = getPositionForView(view);
		int columns = COLUMN_NUM;
		int column = pos % columns;
		int row = pos / columns;
		return new Point(column, row);
	}

	private boolean aboveRight(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y < mobileColumnRowPair.y && targetColumnRowPair.x > mobileColumnRowPair.x;
	}

	private boolean aboveLeft(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y < mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
	}

	private boolean belowRight(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y > mobileColumnRowPair.y && targetColumnRowPair.x > mobileColumnRowPair.x;
	}

	private boolean belowLeft(Point targetColumnRowPair, Point mobileColumnRowPair) {
		return targetColumnRowPair.y > mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
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
		return targetColumnRowPair.y == mobileColumnRowPair.y && targetColumnRowPair.x < mobileColumnRowPair.x;
	}

	private float getViewX(View view) {
		return Math.abs((view.getRight() - view.getLeft()) / 2);
	}

	private float getViewY(View view) {
		return Math.abs((view.getBottom() - view.getTop()) / 2);
	}

	private void reorderView(int originalPosition, int targetPosition) {
		((CustomGridViewDragAndDropAdapter) getAdapter()).reorderItems(originalPosition, targetPosition);
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
		setEnabled(!mReorderAnimation);
	}
}
