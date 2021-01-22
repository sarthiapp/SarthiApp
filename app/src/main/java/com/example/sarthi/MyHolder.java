package com.example.sarthi;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyHolder extends RecyclerView.ViewHolder {

    ImageView image1;
    TextView day_time,car_no,cost,location;

    public MyHolder(@NonNull View itemView) {
        super(itemView);

        this.image1 = itemView.findViewById(R.id.image1);
        this.day_time = itemView.findViewById(R.id.day_time);
        this.car_no = itemView.findViewById(R.id.car_no);
        this.cost = itemView.findViewById(R.id.cost);
        this.location = itemView.findViewById(R.id.location);

    }
}
