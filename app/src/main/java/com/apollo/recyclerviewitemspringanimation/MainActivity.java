package com.apollo.recyclerviewitemspringanimation;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView=findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        MyAdapter accountAdapter=new MyAdapter();
        View view = LayoutInflater.from(this).inflate(R.layout.cardview,null);
        recyclerView.setAdapter( new RecyclerViewSpringAdapter(recyclerView,accountAdapter,view));
    }

    class MyAdapter extends RecyclerView.Adapter<ItemViewHolder>{
        String data[]={"小明","小红","小白","小黑","小狗","小猫","小猪","小明","小红","小白","小黑","小狗","小猫","小猪","小明","小红","小白","小黑","小狗","小猫","小猪","小明","小红","小白","小黑","小狗","小猫","小猪"};

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new ItemViewHolder(LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1,viewGroup,false));
        }

        @Override
        public void onBindViewHolder(@NonNull ItemViewHolder itemViewHolder, int i) {
            itemViewHolder.textView.setText(data[i]);
        }

        @Override
        public int getItemCount() {
            return data.length;
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            textView=itemView.findViewById(android.R.id.text1);
        }
    }

}
