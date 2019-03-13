package xzq.com.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ================================================
 * 作    者：xzqbetter@163.com
 * 日    期：2017/11/17
 * 描    述：自定义控件 - 下拉刷新
 * ================================================
 */
public class RefreshRecyclerView extends RecyclerView {

    private RefreshAdapter mAdapter;
    private LayoutManager mLayoutManager;
    private View mRefreshView;
    private Float mStartY = null;
    private Float mEndY = null;
    private Float mDifY = null;
    private Float mFactor = 2f;
    private int mHeadHeight = 0;

    public RefreshRecyclerView(Context context) {
        this(context, null);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public RefreshRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setLayoutManager(final LayoutManager layout) {
        super.setLayoutManager(layout);
        mLayoutManager = layout;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean superResult = super.onTouchEvent(e);
        if (mAdapter.checkRefresh())
            return true;
        boolean isChanging = changePaddingTop(e);

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                releaseTouchEvent();
                break;
        }
        return isChanging || superResult;
    }

    private boolean changePaddingTop(MotionEvent e) {
        if (mStartY == null) {
            mStartY = e.getY();
        }
        mEndY = e.getY();
        mDifY = (mEndY - mStartY) / mFactor;
        mStartY = mEndY;
        mRefreshView = mLayoutManager.findViewByPosition(0);
        mHeadHeight = mAdapter.getHeadHeight();
        if ( (mRefreshView!=null&&mDifY>0) || (mHeadHeight>0) ) {
            mAdapter.setHeadHeight((int) (mHeadHeight+mDifY));
            return true;
        }
        return false;
    }

    private void releaseTouchEvent() {
        mStartY = null;
        mAdapter.releaseTouchEvent();
    }

    public void setRefreshAdapter(RefreshAdapter adapter) {
        mAdapter = adapter;
        if (mRetryCallback != null) {
            mAdapter.setRetryListener(mRetryCallback);
        }
        setAdapter(adapter);
    }

    /*------------------------- 刷新 ------------------------*/

    private OnRefreshListener mRefreshListener;
    private FailedView.FailedCallback mRetryCallback;

    public interface OnRefreshListener{
        void onRefresh();
        void onLoadMore();
    }

    public void setRefreshListener(OnRefreshListener onRefreshListener) {
        mRefreshListener = onRefreshListener;
    }

    public void setRetryListener(FailedView.FailedCallback onRetryListener) {
        mRetryCallback = onRetryListener;
        if (mAdapter != null) {
            mAdapter.setRetryListener(mRetryCallback);
        }
    }

    public void refresh() {
        if (mRefreshListener != null) {
            mRefreshListener.onRefresh();
        } else {
            setRefreshSuccess();
        }
    }

    public void loadMore() {
        if (mRefreshListener != null) {
            mRefreshListener.onLoadMore();
        } else {
            setMoreSuccess();
        }
    }

    public void retry() {
        refresh();
    }

    /*------------------------- 供外界调用的方法 ------------------------*/

    public void setMoreSuccess() {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setMoreSuccess();
            }
        });
    }

    public void setMoreFailed() {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.setMoreFailed();
            }
        });
    }

    public void setRefreshSuccess() {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.changeRefreshSuccess();
            }
        });
    }

    public void setRefreshFailed() {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.changeRefreshFailed();
            }
        });
    }

    public void setRefreshFailed(final String message) {
        post(new Runnable() {
            @Override
            public void run() {
                mAdapter.changeRefreshFailed(message);
            }
        });
    }

    public void setEmptyView(View emptyView) {
        mAdapter.setEmptyView(emptyView);
    }

    public void setFailedView(View failedView) {
        mAdapter.setFailedView(failedView);
    }

    public void hideTail() {
        mAdapter.hideTail();
    }

}
