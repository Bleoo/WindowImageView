package io.github.bleoo.windowimageview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bleoo on 2017/11/1.
 */

public class MyAdaper extends RecyclerView.Adapter<MyAdaper.ViewHolder> {

    private Context context;
    private List<String> data;
    private RecyclerView recyclerView;

    public MyAdaper(Context context, RecyclerView recyclerView) {
        this.context = context;
        this.recyclerView = recyclerView;
        data = new ArrayList<>();
        data.add("1");
        data.add("2");
        data.add("3");
        data.add("4");
        data.add("5");
        data.add("6");
        data.add("7");
        data.add("8");
        data.add("9");
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 4) {
            return 1;
        }
        return 0;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            ImageView imageView = new ImageView(context);
            imageView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 600));
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(R.mipmap.ic_launcher_round);
            return new ViewHolder(imageView);
        } else if (viewType == 1) {
//            WindowImageView windowImageView = new WindowImageView(context);
//            windowImageView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, 600));
//            windowImageView.setImageBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.timg));
//            windowImageView.bindRecyclerView(recyclerView);
//            return new ViewHolder(windowImageView);
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_wiv, null));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position == 4) {
            holder.window_image_view.bindRecyclerView(recyclerView);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        WindowImageView window_image_view;

        public ViewHolder(View itemView) {
            super(itemView);
            window_image_view = itemView.findViewById(R.id.window_image_view);
        }
    }

}
