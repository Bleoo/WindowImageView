package io.github.bleoo.windowImageView;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import io.github.bleoo.simple.R;

public class MainActivity extends AppCompatActivity {

    RecyclerView rv_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rv_content = findViewById(R.id.rv_content);
        rv_content.setLayoutManager(new LinearLayoutManager(this));
        rv_content.setAdapter(new MyAdapter());
    }

    class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyHolder> {

        @Override
        public int getItemViewType(int position) {
            if (position == 9) {
                return 1;
            }
            if (position == 19) {
                return 1;
            }
            return 0;
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;
            if (viewType == 1) {
                view = View.inflate(MainActivity.this, R.layout.item1, null);
            } else {
                view = View.inflate(MainActivity.this, R.layout.item0, null);
                RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(lp);
            }
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(MyHolder holder, int position) {
            if (position == 9) {
                holder.windowImageView.bindRecyclerView(rv_content);
                holder.windowImageView.setFrescoEnable(false);
                holder.windowImageView.setImageResource(R.drawable.timg2);
            } else if (position == 19) {
                holder.windowImageView.bindRecyclerView(rv_content);
//                holder.windowImageView.setFrescoEnable(false);
//                holder.windowImageView.setImageResource(R.drawable.timg);
                holder.windowImageView.setFrescoEnable(true);
//                holder.windowImageView.setImageURI(Uri.parse("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1510654468262&di=c878e5c02043f8dc7720abaab760549e&imgtype=0&src=http%3A%2F%2Fimg.bbs.cnhubei.com%2Fforum%2Fdvbbs%2F2004-4%2F200441915031894.jpg"));
                holder.windowImageView.setImageURI(Uri.parse("http://www.fzlu.net/uploads/allimg/150918/3-15091Q00R2a2.jpg"));
//                holder.windowImageView.setImageURI(Uri.parse("http://pic92.nipic.com/file/20160316/20647925_150655660000_2.jpg"));
            } else {
                holder.itemView.setBackgroundColor(Color.rgb((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            }
        }

        @Override
        public int getItemCount() {
            return 30;
        }

        class MyHolder extends RecyclerView.ViewHolder {

            WindowImageView windowImageView;

            public MyHolder(View itemView) {
                super(itemView);
                windowImageView = itemView.findViewById(R.id.wiv);
            }
        }
    }
}
