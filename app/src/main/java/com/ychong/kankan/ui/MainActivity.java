package com.ychong.kankan.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ychong.baselib.base.BaseActivity;
import com.ychong.kankan.utils.ApiService;
import com.ychong.picandvideo.ui.main.common.PreViewActivity;
import com.ychong.kankan.utils.BaseContract;
import com.ychong.kankan.entity.ImageBean;
import com.ychong.kankan.adapter.MainRecyclerAdapter;
import com.ychong.kankan.R;
import com.ychong.baselib.utils.http.RetrofitUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;

/**
 * @author Administrator
 * //图片浏览(主界面)
 */
public class MainActivity extends BaseActivity {
    private ImageView ivPhoto;
    private String createTime;
    private LinearLayout headLl;
    private ImageView addIv;
    private TextView titleTv;
    private boolean isHideBar;
    private RecyclerView recyclerView;
    private MainRecyclerAdapter adapter;
    private List<ImageBean> list = new ArrayList<>();
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        initView();
        intiData();
        initListener();
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_main_pv;
    }

    private void initListener() {
        addIv.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, AddActivity.class)));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(MainActivity.this).resumeRequests();
                } else {
                    Glide.with(MainActivity.this).pauseRequests();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                Log.e("dx/dy", dx + "/" + dy);
//                if (dy>0&&!isHideBar){
//                    isHideBar=true;
//                    //上拉
//                    //隐藏标题栏
//                    hideTitleBar();
//                }else if (dy<0&&isHideBar){
//                    isHideBar = false;
//                    //下拉
//                    //显示标题栏
//                    showTitleBar();
//                }
            }
        });
        adapter.setOnClickListener(path -> {
            if ("mp4".equals(path.substring(path.lastIndexOf(".") + 1))) {
                //播放MP4视频
                displayVideo(path);
            } else if ("jpg".equals(path.substring(path.lastIndexOf(".") + 1))) {
                //查看jpg图片
                displayPic(path);
            }
        });
    }

    private void showTitleBar() {
        Log.e("titleBar", "显示");
        ObjectAnimator animator = ObjectAnimator.ofFloat(headLl, "translationY", height + 100);
        animator.setDuration(1000);
        animator.start();
    }

    private void hideTitleBar() {
        Log.e("titleBar", "隐藏");
        ObjectAnimator animator = ObjectAnimator.ofFloat(headLl, "translationY", -height - 100);
        animator.setDuration(1000);
        animator.start();
    }

    private void intiData() {
        titleTv.setText("首页");
        height = headLl.getHeight();
        adapter = new MainRecyclerAdapter(this, list);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);
    }

    private void initView() {
        addIv = (ImageView) findViewById(R.id.right_iv);
        titleTv = (TextView) findViewById(R.id.title_tv);
        headLl = (LinearLayout) findViewById(R.id.head_ll);
        ivPhoto = (ImageView) findViewById(R.id.iv_photo);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
    }

    private void initLayout() {
        //setContentView(R.layout.activity_main_pv);
    }


    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    @SuppressLint("CheckResult")
    private void refreshData() {
        showProgressDialog(this,"正在刷新",false);
        queryAllImage();
    }

    private void queryAllImage() {
        RetrofitUtils.getInstance(this).getRetrofit().create(ApiService.class)
                .queryAllImages().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ResponseBody>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(ResponseBody responseBody) {
                try {
                    String json = responseBody.string();
                    JSONObject jsonObject = new JSONObject(json);
                    String msg = jsonObject.getString("msg");
                    boolean success = jsonObject.getBoolean("success");
                    JSONArray jsonArray = jsonObject.getJSONArray("data");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        ImageBean bean = new ImageBean();
                        JSONObject jsonObject1 = new JSONObject(jsonArray.getString(i));
                        bean.name = jsonObject1.getString("name");
                        bean.path = jsonObject1.getString("path");
                        bean.uploadTime = jsonObject1.getString("uploadTime");
                        bean.userId = jsonObject1.getInt("userId");
                        bean.title = jsonObject1.getString("title");
                        list.add(bean);
                    }
                    adapter.notifyDataSetChanged();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(Throwable e) {
                hideProgressDialog();
            }

            @Override
            public void onComplete() {
                hideProgressDialog();
            }
        });
    }

    private void displayVideo(String path) {
        Intent intent = new Intent(MainActivity.this, PreViewActivity.class);
        intent.putExtra(BaseContract.PREVIEW_TYPE, BaseContract.VIDEO_TYPE);
        intent.putExtra(BaseContract.PATH, path);
        startActivity(intent);
    }

    private void displayPic(String path) {
        Intent intent = new Intent(MainActivity.this, PreViewActivity.class);
        intent.putExtra(BaseContract.PREVIEW_TYPE, BaseContract.PIC_TYPE);
        intent.putExtra(BaseContract.PATH, path);
        startActivity(intent);
    }
}
