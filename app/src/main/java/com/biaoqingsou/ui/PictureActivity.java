package com.biaoqingsou.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.biaoqingsou.R;
import com.biaoqingsou.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by Mystery406.
 */
public class PictureActivity extends BaseActivity {
    public static final String EXTRA_IMAGE_URL = "image_url";
    public static final String EXTRA_IMAGE_TITLE = "image_title";
    private String image_url;
    private String image_title;
    private boolean isGif = false;
    private PhotoViewAttacher attacher;

    @BindView(R.id.iv_image)
    ImageView iv_image;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        ButterKnife.bind(this);

        parseIntentData();
        Glide.with(this).load(image_url).into(iv_image);
        setTitle(image_title);
        setupPhotoAttach();
    }


    private void setupPhotoAttach() {
        // TODO: 2016/9/23 PhotoViewAttacher
        attacher = new PhotoViewAttacher(iv_image);
        attacher.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                new AlertDialog.Builder(PictureActivity.this)
                        .setMessage(getString(R.string.ask_saving_picture))
                        .setNegativeButton("不要", null)
                        .setPositiveButton("好的", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                saveImageToGallery();
                            }
                        })
                        .show();
                return true;
            }

        });
    }


    private void parseIntentData() {
        image_url = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        image_title = getIntent().getStringExtra(EXTRA_IMAGE_TITLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_picture, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_share:
                // TODO: 2016/9/23 分享图片
                break;
            case R.id.action_save:
                saveImageToGallery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void saveImageToGallery() {
        //判断是否是GIF图片
        isGif = image_url.endsWith(".gif?") || image_url.endsWith(".?");

        Subscription s = Observable.create(new Observable.OnSubscribe<File>() {
            @Override
            public void call(Subscriber<? super File> subscriber) {
                File file = null;
                try {
                    file = Glide.with(getApplicationContext())
                            .load(image_url)
                            .downloadOnly(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();

                } catch (Exception e) {
                    subscriber.onError(e);
                }
                if (file == null) {
                    subscriber.onError(new Exception("下载出错~~"));
                }

                subscriber.onNext(file);
                subscriber.onCompleted();
            }
        })
                .flatMap(new Func1<File, Observable<?>>() {
                    @Override
                    public Observable<?> call(File file) {
                        //保存文件到BiaoqingSou目录
                        File biaoqingSouDir = new File(Environment.getExternalStorageDirectory(), "BiaoqingSou");
                        if (!biaoqingSouDir.exists()) {
                            biaoqingSouDir.mkdir();
                        }
                        String fileName;
                        if (isGif) {
                            fileName = image_title + ".gif";
                        } else {
                            fileName = image_title + ".jpg";
                        }
                        File desFile = new File(biaoqingSouDir, fileName);
                        FileInputStream fis = null;
                        FileOutputStream fos = null;
                        try {
                            fis = new FileInputStream(file);
                            fos = new FileOutputStream(desFile);
                            byte[] buffer = new byte[2048];
                            int length = 0;
                            while ((length = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, length);
                                fos.flush();
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                fis.close();
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

                        //通知图库更新
                        Uri uri = Uri.fromFile(desFile);
                        Intent scannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
                        getApplicationContext().sendBroadcast(scannerIntent);
                        return Observable.just(uri);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        File biaoqingSouDir = new File(Environment.getExternalStorageDirectory(), "BiaoqingSou");
                        String msg = String.format(getString(R.string.picture_has_save_to),
                                biaoqingSouDir.getAbsolutePath());
                        ToastUtil.showShort(msg);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        ToastUtil.showShort(throwable.getMessage() + "\n出了点问题哦");
                    }
                });
        addSubscription(s);
    }


    @Override
    protected boolean canBack() {
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        attacher.cleanup();
    }
}
