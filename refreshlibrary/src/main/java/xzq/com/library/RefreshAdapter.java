package xzq.com.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ================================================
 * 作    者：xzqbetter@163.com
 * 日    期：2017/11/17
 * 描    述：
 * ================================================
 */
public abstract class RefreshAdapter extends RecyclerView.Adapter implements FailedView.FailedCallback {

    private Context context;
    private static final int TYPE_HEAD = -1;
    private static final int TYPE_TAIL = -2;
    private static final int TYPE_EMPTY = -3;
    private static final int TYPE_FAILED = -4;
    private static final int TYPE_LOADING = -5;
    private int mStateMore = STATE_MORE_SUCCESS;
    private final static int STATE_MORE_LOADING = 1;
    private final static int STATE_MORE_SUCCESS = 2;
    private final static int STATE_MORE_FAILED = 3;
    private final static int STATE_MORE_EMPTY = 4;
    private int mStateRefresh = STATE_REFRESH_LOADING;
    private final static int STATE_REFRESH_IDLE = 1;
    private final static int STATE_REFRESH_RELEASE = 2;
    private final static int STATE_REFRESH_LOADING = 3;
    private final static int STATE_REFRESH_SUCCESS = 4;
    private final static int STATE_REFRESH_FAILED = 5;
    private Handler mHandler = new Handler();
    private View mEmptyView = null;
    private View mFailedView = null;
    private boolean mIsEnd = false;
    private boolean mIsEmpty = false;
    private boolean mIsFailed = false;
    private boolean mIsLoadingInit = true;
    public int FLAG_REFRESH_HEIGHT = 0;
    private ViewGroup.LayoutParams mHeadParam;
    private LinearLayout mRefreshHead;
    private ImageView mRefreshArrow;
    private ProgressBar mRefreshPb;
    private TextView mRefreshText;
    private RefreshRecyclerView mRecyclerView;
    private int mRealCount;
    private ValueAnimator mOriginAnimator;
    private ValueAnimator mRefreshAnimator;
    private RotateAnimation mArrowUpAnimation;
    private RotateAnimation mArrowDownAnimation;
    private boolean mShowTail = true;
    private String mErrorMessage = "网络异常";

    public RefreshAdapter(Context context) {
        this.context = context;
        FLAG_REFRESH_HEIGHT = CalUtils.dp2px(context, 40);
        initAnimation();
    }

    @Override
    public int getItemCount() {
        checkEmpty();
        if (mIsEmpty || mIsFailed || mIsLoadingInit) {
            return 2;
        } else {
            return getItemCountBody() + 2;
        }
    }

    public abstract int getItemCountBody();

    @Override
    public int getItemViewType(int position) {
        if (mIsLoadingInit) {
            return getItemViewTypeLoading(position);
        } else if (mIsFailed) {
            return getItemViewTypeFailed(position);
        } else if (mIsEmpty) {
            return getItemViewTypeEmpty(position);
        } else {
            return getItemViewTypeCommon(position);
        }
    }

    private int getItemViewTypeLoading(int position) {
        switch (position) {
            case 0:
                return TYPE_HEAD;
            default:
                return TYPE_LOADING;
        }
    }

    private int getItemViewTypeFailed(int position) {
        switch (position) {
            case 0:
                return TYPE_HEAD;
            default:
                return TYPE_FAILED;
        }
    }

    private int getItemViewTypeEmpty(int position) {
        switch (position) {
            case 0:
                return TYPE_HEAD;
            default:
                return TYPE_EMPTY;
        }
    }

    private int getItemViewTypeCommon(int position) {
        if (position == 0) {
            return TYPE_HEAD;
        } else if (position == getItemCount() - 1) {
            return TYPE_TAIL;
        } else {
            return getItemViewTypeBody(position - 1);
        }
    }

    public int getItemViewTypeBody(int position) {
        return 0;
    }

    /*------------------------- onCreateViewHolder ------------------------*/

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mRecyclerView == null) {
            mRecyclerView = (RefreshRecyclerView) parent;
        }
        switch (viewType) {
            case TYPE_LOADING:
                return onCreateViewHolderLoading(parent, viewType);
            case TYPE_HEAD:
                return onCreateViewHolderHead(parent, viewType);
            case TYPE_TAIL:
                return onCreateViewHolderTail(parent, viewType);
            case TYPE_EMPTY:
                return onCreateViewHolderEmpty(parent, viewType);
            case TYPE_FAILED:
                return onCreateViewHolderFailed(parent, viewType);
            default:
                return onCreateViewHolderBody(parent, viewType);
        }
    }

    protected RecyclerView.ViewHolder onCreateViewHolderLoading(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_refresh_loading, parent, false);
        return new ViewHolderLoading(view);
    }

    private RecyclerView.ViewHolder onCreateViewHolderHead(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_refresh_head, parent, false);
        return new ViewHolderHead(view);
    }

    private RecyclerView.ViewHolder onCreateViewHolderTail(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_refresh_tail, parent, false);
        return new ViewHolderTail(view);
    }

    private RecyclerView.ViewHolder onCreateViewHolderEmpty(ViewGroup parent, int viewType) {
        View view;
        if (mEmptyView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_refresh_empty, parent, false);
        } else {
            view = mEmptyView;
        }
        return new ViewHolderEmpty(view);
    }

    private RecyclerView.ViewHolder onCreateViewHolderFailed(ViewGroup parent, int viewType) {
        if (mFailedView == null) {
            FailedView failedView = new FailedView(context);
            failedView.setCallback(this);
            mFailedView = failedView;
        }
        return new ViewHolderFailed(mFailedView);
    }

    public abstract RecyclerView.ViewHolder onCreateViewHolderBody(ViewGroup parent, int viewType);


    /*------------------------- onBindViewHolder ------------------------*/

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == TYPE_LOADING) {
            onBindViewHolderLoading(holder, position);
        } else if (viewType == TYPE_HEAD) {
            onBindViewHolderHead(holder, position);
        } else if (viewType == TYPE_TAIL) {
            onBindViewHolderTail(holder, position);
        } else if (viewType == TYPE_EMPTY) {
            onBindViewHolderEmpty(holder, position);
        } else if (viewType == TYPE_FAILED) {
            onBindViewHolderFailed(holder, position);
        } else {
            onBindViewHolderBody(holder, position - 1);
            judgeLoadMore(position);
        }
    }

    private void onBindViewHolderLoading(RecyclerView.ViewHolder holder, int position) {
        /*ViewHolderLoading viewHolder = (ViewHolderLoading) holder;
        AnimationDrawable animationDrawable = (AnimationDrawable) viewHolder.ivLoading.getBackground();
        animationDrawable.start();*/
    }

    private void onBindViewHolderFailed(RecyclerView.ViewHolder holder, int position) {
        if (mFailedView instanceof FailedView) {
            ((FailedView) mFailedView).setErrorMessage(mErrorMessage);
        }
    }

    private void onBindViewHolderEmpty(RecyclerView.ViewHolder holder, int position) {

    }

    private void onBindViewHolderHead(RecyclerView.ViewHolder holder, int position) {
        ViewHolderHead viewHolderHead = (ViewHolderHead) holder;
        mRefreshHead = viewHolderHead.llRefreshHead;
        mRefreshArrow = viewHolderHead.ivRefreshArrow;
        mRefreshPb = viewHolderHead.pbRefresh;
        mRefreshText = viewHolderHead.tvRefreshText;
        mHeadParam = mRefreshHead.getLayoutParams();
    }

    private void onBindViewHolderTail(RecyclerView.ViewHolder holder, int position) {
        mIsEnd = true;
        ViewHolderTail viewHolderTail = (ViewHolderTail) holder;
        if (mStateMore == STATE_MORE_LOADING) {
            changeMoreLoading(viewHolderTail);
        } else if (mStateMore == STATE_MORE_FAILED) {
            changeMoreFailed(viewHolderTail);
        } else if (mStateMore == STATE_MORE_SUCCESS) {
            changeMoreSuccess(viewHolderTail);
        } else if (mStateMore == STATE_MORE_EMPTY) {
            changeMoreEmpty(viewHolderTail);
        }
    }

    protected abstract void onBindViewHolderBody(RecyclerView.ViewHolder holder, int i);

    /*------------------------- LoadMore ------------------------*/

    private void changeMoreEmpty(ViewHolderTail viewHolderTail) {
        viewHolderTail.llTail.setOnClickListener(null);
        viewHolderTail.ivTail.clearAnimation();
        viewHolderTail.llTail.setVisibility(View.VISIBLE);
        viewHolderTail.ivTail.setVisibility(View.GONE);
        viewHolderTail.tvTail.setText("没有更多了 =_=\"");
        if (!mShowTail)
            viewHolderTail.llTail.setVisibility(View.GONE);
    }

    private void changeMoreSuccess(ViewHolderTail viewHolderTail) {
        viewHolderTail.llTail.setVisibility(View.GONE);
        viewHolderTail.llTail.setOnClickListener(null);
        viewHolderTail.ivTail.clearAnimation();
    }

    private void changeMoreFailed(final ViewHolderTail viewHolderTail) {
        viewHolderTail.ivTail.clearAnimation();
        viewHolderTail.llTail.setVisibility(View.VISIBLE);
        viewHolderTail.ivTail.setVisibility(View.GONE);
        viewHolderTail.llTail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStateMore = STATE_MORE_LOADING;
                mRecyclerView.loadMore();
                changeMoreLoading(viewHolderTail);
            }
        });
        viewHolderTail.tvTail.setText("加载失败，点击重试 =_=\"");
    }

    private void changeMoreLoading(ViewHolderTail viewHolderTail) {
        viewHolderTail.llTail.setVisibility(View.VISIBLE);
        viewHolderTail.llTail.setOnClickListener(null);
        viewHolderTail.ivTail.setVisibility(View.VISIBLE);
        AnimationDrawable loadAnim = (AnimationDrawable) viewHolderTail.ivTail.getBackground();
        loadAnim.start();
        viewHolderTail.tvTail.setText("加载中...");
    }

    /**
     * 判断是否要加载更多
     *
     * @param position
     */
    private void judgeLoadMore(int position) {
        if (position >= (getItemCount() - 5) && mStateRefresh != STATE_REFRESH_LOADING && mStateMore != STATE_MORE_LOADING) {
            if (mRealCount != getItemCountBody()) {
                mStateMore = STATE_MORE_LOADING;
                mRealCount = getItemCountBody();
                mRecyclerView.loadMore();
            } else if (mStateMore == STATE_MORE_SUCCESS) {
                mStateMore = STATE_MORE_EMPTY;
            }
        }
    }

    public void setMoreSuccess() {
        mStateMore = STATE_MORE_SUCCESS;
        if (mRealCount == getItemCountBody()) {
            mStateMore = STATE_MORE_EMPTY;
        }
        if (mIsEnd) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void setMoreFailed() {
        mStateMore = STATE_MORE_FAILED;
        if (mIsEnd) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void hideTail() {
        mShowTail = false;
    }

    /*------------------------- 下拉刷新 ------------------------*/

    public void changeRefreshIdle() {
        if (mStateRefresh == STATE_REFRESH_RELEASE) {
            mStateRefresh = STATE_REFRESH_IDLE;
            mRefreshPb.setVisibility(View.GONE);
            mRefreshArrow.setVisibility(View.VISIBLE);
            mRefreshArrow.startAnimation(mArrowDownAnimation);
            mRefreshText.setText("下拉刷新");
        } else if (mStateRefresh == STATE_REFRESH_SUCCESS || mStateRefresh == STATE_REFRESH_FAILED) {
            mStateRefresh = STATE_REFRESH_IDLE;
            mRefreshPb.setVisibility(View.GONE);
            mRefreshArrow.setVisibility(View.VISIBLE);
            mRefreshArrow.setBackgroundResource(R.drawable.refresh_arrow);
            mRefreshText.setText("下拉刷新");
        }
    }

    public void changeRefreshRelease() {
        if (mStateRefresh == STATE_REFRESH_IDLE) {
            mStateRefresh = STATE_REFRESH_RELEASE;
            mRefreshPb.setVisibility(View.GONE);
            mRefreshArrow.setVisibility(View.VISIBLE);
            mRefreshArrow.startAnimation(mArrowUpAnimation);
            mRefreshText.setText("释放更新");
        }
    }

    public void changeRefreshLoading() {
        if (mStateRefresh == STATE_REFRESH_RELEASE) {
            mRealCount = 0;
            mStateRefresh = STATE_REFRESH_LOADING;
            mRefreshArrow.clearAnimation();
            mRefreshPb.setVisibility(View.VISIBLE);
            mRefreshArrow.setVisibility(View.GONE);
            mRefreshText.setText("加载中...");
        }
    }

    public void changeRefreshSuccess() {
        mStateRefresh = STATE_REFRESH_SUCCESS;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mRefreshArrow != null) {
                        (CalUtils.getActivity(mRefreshArrow.getContext())).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRefreshPb.setVisibility(View.GONE);
                                mRefreshArrow.setVisibility(View.VISIBLE);
                                mRefreshArrow.clearAnimation();
                                mRefreshArrow.setBackgroundResource(R.drawable.refresh_success);
                                mRefreshText.setText("刷新成功");
                                checkLoadFinishState();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        releaseToOrigin();
                                    }
                                }, 500);
                            }
                        });
                        break;
                    }
                }
            }
        }).start();
    }

    public void changeRefreshFailed() {
        try {
            mStateRefresh = STATE_REFRESH_FAILED;
            mRefreshPb.setVisibility(View.GONE);
            mRefreshArrow.setVisibility(View.VISIBLE);
            mRefreshArrow.clearAnimation();
            mRefreshArrow.setBackgroundResource(R.drawable.refresh_failed);
            mRefreshText.setText("网络异常");
            checkLoadFinishState();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    releaseToOrigin();
                }
            }, 500);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public void changeRefreshFailed(String message) {
        if (TextUtils.isEmpty(message))
            mErrorMessage = "网络异常";
        else
            mErrorMessage = message;
        try {
            mStateRefresh = STATE_REFRESH_FAILED;
            mRefreshPb.setVisibility(View.GONE);
            mRefreshArrow.setVisibility(View.VISIBLE);
            mRefreshArrow.clearAnimation();
            mRefreshArrow.setBackgroundResource(R.drawable.refresh_failed);
            mRefreshText.setText(message);
            checkLoadFinishState();
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    releaseToOrigin();
                }
            }, 500);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取头部高度
     *
     * @return
     */
    public int getHeadHeight() {
        if (mRefreshHead == null)
            return 0;
        return mRefreshHead.getHeight();
    }

    /**
     * 设置头部高度
     *
     * @param height
     */
    public void setHeadHeight(float height) {
        if (mHeadParam == null)
            return;
        height = height<0 ? 0 : height;
        mHeadParam.height = (int) height;
        mRefreshHead.setLayoutParams(mHeadParam);
        if (height >= FLAG_REFRESH_HEIGHT) {
            changeRefreshRelease();
        } else if (height < FLAG_REFRESH_HEIGHT) {
            changeRefreshIdle();
        }
    }

    /**
     * 释放触摸事件
     */
    public void releaseTouchEvent() {
        if (mStateRefresh == STATE_REFRESH_IDLE) {
            releaseToOrigin();
        } else if (mStateRefresh == STATE_REFRESH_RELEASE) {
            releaseToRefresh();
        }
    }

    /**
     * 滑动到初始状态
     */
    private void releaseToOrigin() {
        changeRefreshIdle();
        mOriginAnimator = ValueAnimator.ofInt(getHeadHeight(), 0);
        mOriginAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHeadParam.height = (int) animation.getAnimatedValue();
                mRefreshHead.setLayoutParams(mHeadParam);
            }
        });
        mOriginAnimator.start();
    }

    /**
     * 滑动到刷新状态
     */
    private void releaseToRefresh() {
        mRefreshAnimator = ValueAnimator.ofInt(getHeadHeight(), FLAG_REFRESH_HEIGHT);
        mRefreshAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mHeadParam.height = (int) animation.getAnimatedValue();
                mRefreshHead.setLayoutParams(mHeadParam);
            }
        });
        mRefreshAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                changeRefreshLoading();
                mRecyclerView.refresh();
            }
        });
        mRefreshAnimator.setDuration(300);
        mRefreshAnimator.start();
    }

    /*------------------------- EmptyView ------------------------*/

    public void setEmptyView(View view) {
        mEmptyView = view;
    }

    public void setFailedView(View view) {
        mFailedView = view;
    }

    /**
     * 是否显示空视图
     */
    private void checkEmpty() {
        mIsEmpty = getItemCountBody() == 0;
    }

    /**
     * 是否显示失败视图
     */
    private void checkFailed() {
        if (mStateRefresh == STATE_REFRESH_SUCCESS) {
            mIsFailed = false;
        }
        if (mStateRefresh == STATE_REFRESH_FAILED) {
            mIsFailed = mIsEmpty;
        }
    }

    /**
     * 重新加载
     */
    private void checkRetry() {
        if (mFailedView != null && mFailedView instanceof FailedView) {
            ((FailedView) mFailedView).setRetryFinished();
        }
    }

    /**
     * 检查空视图和失败视图
     */
    private void checkLoadFinishState() {
        mIsLoadingInit = false;
        checkEmpty();
        checkFailed();
        checkRetry();
        notifyDataSetChanged();
    }

    /**
     * 是否处于刷新状态
     *
     * @return
     */
    public boolean checkRefresh() {
        return mStateRefresh == STATE_REFRESH_LOADING || mStateRefresh == STATE_REFRESH_SUCCESS || mStateRefresh == STATE_REFRESH_FAILED;
    }

    @Override
    public void onRetryClick() {
        mRecyclerView.retry();
    }

    @Override
    public void onLoginClick() {

    }

    /*------------------------- 动画 ------------------------*/

    private void initAnimation() {
        // 箭头向上动画
        mArrowUpAnimation = new RotateAnimation(
                0, -180f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mArrowUpAnimation.setDuration(150);
        mArrowUpAnimation.setFillAfter(true);
        // 箭头向下动画
        mArrowDownAnimation = new RotateAnimation(
                -180, 0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mArrowDownAnimation.setDuration(150);
        mArrowDownAnimation.setFillAfter(true);
    }

    public void setRetryListener(FailedView.FailedCallback callback) {
        if (mFailedView instanceof FailedView) {
            ((FailedView) mFailedView).setCallback(callback);
        }
    }

    /*------------------------- ViewHolder ------------------------*/

    static class ViewHolderHead extends RecyclerView.ViewHolder {
        @BindView(R2.id.iv_refresh_arrow)
        ImageView ivRefreshArrow;
        @BindView(R2.id.tv_refresh_text)
        TextView tvRefreshText;
        @BindView(R2.id.ll_refresh_head)
        LinearLayout llRefreshHead;
        @BindView(R2.id.pb_refresh)
        ProgressBar pbRefresh;

        ViewHolderHead(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderTail extends RecyclerView.ViewHolder {
        @BindView(R2.id.tv_tail)
        TextView tvTail;
        @BindView(R2.id.iv_tail)
        ImageView ivTail;
        @BindView(R2.id.ll_tail)
        LinearLayout llTail;

        ViewHolderTail(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class ViewHolderEmpty extends RecyclerView.ViewHolder {
        ViewHolderEmpty(View view) {
            super(view);
        }
    }

    static class ViewHolderFailed extends RecyclerView.ViewHolder {
        ViewHolderFailed(View view) {
            super(view);
        }
    }

    static class ViewHolderLoading extends RecyclerView.ViewHolder {
        ViewHolderLoading(View view) {
            super(view);
        }
    }
}
