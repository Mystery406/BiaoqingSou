package com.biaoqingsou;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.biaoqingsou.data.ZhuangbiData;
import com.biaoqingsou.http.HttpManager;
import com.biaoqingsou.ui.BaseActivity;
import com.biaoqingsou.ui.adapter.ZhuangbiAdapter;
import com.biaoqingsou.util.Once;
import com.biaoqingsou.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.rv_zhuangbi)
    RecyclerView rv_zhuangbi;
    @BindView(R.id.srl_refresh)
    SwipeRefreshLayout srl_refresh;

    private ArrayList<ZhuangbiData> mZhuangbiList;
    private ZhuangbiAdapter mZhuangbiAdapter;
    private Subscription s;
    private long exitTime = 0;

    Observer<List<ZhuangbiData>> observer = new Observer<List<ZhuangbiData>>() {
        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            srl_refresh.setRefreshing(false);
            ToastUtil.showShort("加载出错");
            Log.e("tag", e.getMessage());
        }

        @Override
        public void onNext(List<ZhuangbiData> zhuangbiDataList) {
            srl_refresh.setRefreshing(false);
            if (zhuangbiDataList.size() > 0) {
                mZhuangbiList.clear();
                mZhuangbiList.addAll(zhuangbiDataList);
                mZhuangbiAdapter.notifyDataSetChanged();
            } else {
                ToastUtil.showShort("未找到关键字对应表情");
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        srl_refresh.setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW);
        srl_refresh.setEnabled(false);

        initData();
        initRecyclerView();
    }

    @OnClick(R.id.fab)
    public void onFabClick(View view) {
//        loadData("厉害");
        showSearch();
    }

    private void showSearch() {
        final EditText editText = new EditText(this);
        editText.setHint("输入搜索关键词");

        new AlertDialog.Builder(this)
                .setMessage("提示")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String key = editText.getText().toString();
                        if (!TextUtils.isEmpty(key)) {
                            loadData(key);
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }


    private void initData() {
        mZhuangbiList = new ArrayList<>();
    }


    private void initRecyclerView() {
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2,
                StaggeredGridLayoutManager.VERTICAL);
        rv_zhuangbi.setLayoutManager(staggeredGridLayoutManager);
        mZhuangbiAdapter = new ZhuangbiAdapter(this, mZhuangbiList);
        rv_zhuangbi.setAdapter(mZhuangbiAdapter);

        new Once(this).show("guide", new Once.OnceCallback() {
            @Override
            public void onOnce() {
                Snackbar.make(rv_zhuangbi, R.string.tip_guide, Snackbar.LENGTH_INDEFINITE)
                        .setAction(R.string.i_know, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        })
                        .show();
            }
        });

        loadData("装逼");
    }


    private void loadData(String key) {
        if (!srl_refresh.isRefreshing()) {
            srl_refresh.setRefreshing(true);
        }
        s = HttpManager.getZhuangbiApi()
                .search(key)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);

        addSubscription(s);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            new AlertDialog.Builder(this)
                    .setTitle("关于")
                    .setView(R.layout.dialog_about)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - exitTime > 2000) {
            ToastUtil.showShort("再按一次退出");
            exitTime = System.currentTimeMillis();
        } else {
            super.onBackPressed();
        }
    }
}
