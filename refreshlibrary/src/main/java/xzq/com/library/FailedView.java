package xzq.com.library;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * ================================================
 * 作    者：xzqbetter@163.com
 * 日    期：2017/12/1
 * 描    述：加载失败视图
 * ================================================
 */
public class FailedView extends LinearLayout {
    @BindView(R2.id.btn_retry)
    Button btnRetry;
    @BindView(R2.id.tv_refresh_text)
    TextView tvRefreshText;
    @BindView(R2.id.ll_refresh)
    LinearLayout llRefresh;
    @BindView(R2.id.tv_error_message)
    TextView tvErrorMessage;
    @BindView(R2.id.ll_retry)
    LinearLayout llRetry;
    @BindView(R2.id.btn_login)
    Button btnLogin;
    @BindView(R2.id.ll_login)
    LinearLayout llLogin;
    @BindView(R2.id.iv_error)
    ImageView ivError;

    private FailedCallback mCallback;

    public FailedView(Context context) {
        this(context, null);
    }

    public FailedView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public FailedView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(final Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_failed, this, false);
        addView(view);
        ButterKnife.bind(this, view);
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup.LayoutParams layoutParams = getLayoutParams();
                if (layoutParams == null) {
                    layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                setLayoutParams(layoutParams);
            }
        });
    }

    /**
     * 点击事件 - 登录
     */
    @OnClick(R2.id.btn_login)
    public void onLoginClicked() {
        if (mCallback != null) {
            mCallback.onLoginClick();
        }
    }

    /**
     * 点击事件 - 重试
     */
    @OnClick(R2.id.btn_retry)
    public void onViewClicked() {
        btnRetry.setVisibility(View.GONE);
        llRefresh.setVisibility(View.VISIBLE);
        if (mCallback != null) {
            mCallback.onRetryClick();
        }
    }

    /*------------------------- 供外界调用的方法 ------------------------*/

    /**
     * 设置视图类型
     */
    public void setViewType(boolean isLogin) {
        if (isLogin) {
            llLogin.setVisibility(View.VISIBLE);
            llRetry.setVisibility(View.GONE);
        } else {
            llLogin.setVisibility(View.GONE);
            llRetry.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 设置错误提示信息
     */
    public void setErrorMessage(String message) {
        if (TextUtils.isEmpty(message))
            message = "系统开了个小差";
        tvErrorMessage.setText(message);
    }

    public void setRetryFinished() {
        llRefresh.setVisibility(View.GONE);
        btnRetry.setVisibility(View.VISIBLE);
    }

    public void setCallback(FailedCallback callback) {
        mCallback = callback;
    }

    public interface FailedCallback {
        void onRetryClick();
        void onLoginClick();
    }

}
