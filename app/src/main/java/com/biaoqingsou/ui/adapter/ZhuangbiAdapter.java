package com.biaoqingsou.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.biaoqingsou.R;
import com.biaoqingsou.data.ZhuangbiData;
import com.biaoqingsou.ui.PictureActivity;
import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Mystery406.
 */
public class ZhuangbiAdapter extends RecyclerView.Adapter<ZhuangbiAdapter.ViewHolder> {
    private Context mContext;
    private List<ZhuangbiData> mZhuangbiDataList;


    public ZhuangbiAdapter(Context context, List<ZhuangbiData> zhuangbiDataList) {
        mContext = context;
        mZhuangbiDataList = zhuangbiDataList;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zhuangbi, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final ZhuangbiData zhuangbiData = mZhuangbiDataList.get(position);
        Glide.with(mContext)
                .load(zhuangbiData.image_url)
                .into(holder.iv_image);
        holder.tv_description.setText(zhuangbiData.description);

        holder.iv_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PictureActivity.class);
                intent.putExtra(PictureActivity.EXTRA_IMAGE_URL, zhuangbiData.image_url);
                intent.putExtra(PictureActivity.EXTRA_IMAGE_TITLE, zhuangbiData.description);
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mZhuangbiDataList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.iv_image)
        ImageView iv_image;
        @BindView(R.id.tv_description)
        TextView tv_description;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


    }
}
