package com.emagroup.example;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Administrator on 2017/4/18.
 */

public class MsgAdapter extends RecyclerView.Adapter {

    public List<String> datas = null;

    public MsgAdapter(List<String> datas) {
        this.datas = datas;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.iteam_msg, parent, false);

        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ((MyHolder)holder).mTvItem.setText(datas.get(position));
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    public class MyHolder extends ViewHolder {

        public final TextView mTvItem;

        public MyHolder(View itemView) {
            super(itemView);
            mTvItem = (TextView) itemView.findViewById(R.id.tv_msg);
        }
    }
}
