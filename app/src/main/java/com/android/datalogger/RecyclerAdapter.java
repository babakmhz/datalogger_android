package com.android.datalogger;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {


    private Context context;
    private List<DataModel> modelList;

    public RecyclerAdapter(Context context, List<DataModel> modelList) {

        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.items_template, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        DataModel model = modelList.get(i);
        viewHolder.textData.setText(model.getData());
        viewHolder.textDate.setText(model.getDate());
    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public void addItems(List<DataModel> models) {
        this.modelList = models;
        notifyItemInserted(models.size() - 1);
        notifyDataSetChanged();
    }


    public void addItem(DataModel model) {
        this.modelList.add(model);
        notifyItemInserted(modelList.size() - 1);
        notifyDataSetChanged();
    }

    public void reverseItemOrder(){
        Collections.reverse(this.modelList);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textData;
        TextView textDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textData = itemView.findViewById(R.id.text_title);
            textDate = itemView.findViewById(R.id.text_date);
        }
    }
}
