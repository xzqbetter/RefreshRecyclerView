package xzq.com.refreshrecyclerview;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import xzq.com.library.RefreshAdapter;
import xzq.com.library.RefreshRecyclerView;

public class MainActivity extends AppCompatActivity implements RefreshRecyclerView.OnRefreshListener {

    private RefreshRecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rv = findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this, OrientationHelper.VERTICAL, false));
        rv.setRefreshAdapter(new MyAdapter(this));
        rv.setRefreshListener(this);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rv.setRefreshSuccess();
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        rv.setMoreSuccess();
    }

    public class MyAdapter extends RefreshAdapter {

        private Context mContext;

        public MyAdapter(Context context) {
            super(context);
            mContext = context;
        }

        @Override
        public int getItemCountBody() {
            return 10;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolderBody(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_test, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        protected void onBindViewHolderBody(RecyclerView.ViewHolder holder, int i) {

        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
