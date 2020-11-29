package com.example.rpibell;

import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.util.Collections;
import java.util.List;

public class RecyclerView_Adapter extends RecyclerView.Adapter<RecyclerView_Adapter.ViewHolder> {

    List<Guest> list = Collections.emptyList();
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView guestName;
        TextView emailView;
        //ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            guestName = (TextView) itemView.findViewById(R.id.guestName);
            emailView = (TextView) itemView.findViewById(R.id.emailView);
        }

        /**public TextView getGuestName() {
         return guestName;
         }
         public ImageView getImageView() {
         return imageView;
         }**/

    }

    public RecyclerView_Adapter(List<Guest> guest, Application application) {
        this.list = guest;
        this.context = application;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout, initialize the ViewHolder
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_layout, parent, false);

        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Use the provided View Holder on the onCreateViewHolder method to
        // populate the current row on the RecyclerView
        holder.guestName.setText(list.get(position).name);
        holder.emailView.setText(list.get(position).email);
        //holder.imageView.setImageResource(list.get(position).imageID);

    }

    /**
     * Size of dataset as invoked by the layout manager.
     * @return Size of list
     */
    @Override
    public int getItemCount() {
        return list.size();
    }
}