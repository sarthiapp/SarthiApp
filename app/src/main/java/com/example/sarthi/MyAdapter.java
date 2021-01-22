package com.example.sarthi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<MyHolder> {

    Context c;
    ArrayList<Model> models;

    public MyAdapter(Context c, ArrayList<Model> models) {
        this.c = c;
        this.models = models;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, null);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder myHolder, int i) {

        myHolder.day_time.setText(models.get(i).getDay_time());
        myHolder.car_no.setText(models.get(i).getCarNumber());
        myHolder.cost.setText(models.get(i).getCost());
        myHolder.location.setText(models.get(i).getLocation());

        myHolder.image1.setImageResource(models.get(i).getImg());

    }

    @Override
    public int getItemCount()
    {
        return models.size();
    }
}
