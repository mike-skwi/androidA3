package com.example.csc372_mikeskwierawski_a3;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class StockListAdapter extends RecyclerView.Adapter<ViewHolder>{

    private ArrayList<Stock> stockList;
    private MainActivity mainActivity;


    StockListAdapter(ArrayList<Stock> stockList, MainActivity mainActivity){
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // item view is one particular instance of this
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_item,parent,false);

        //        itemView.setOnClickListener(mainActivity);
//        itemView.setOnLongClickListener((View.OnLongClickListener) mainActivity);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Stock selectedNote = stockList.get(position);

        if (selectedNote.getChangePercentage()>0){
            // Greentext
            holder.fullSymbolName.setText(selectedNote.getCompanyName());
            holder.smymbAbbreviation.setText(selectedNote.getStockSymbol());
            //TODO make a conditional for the arrow
            holder.arrow.setText("A");
            holder.stockPrice.setText(selectedNote.getPriceString());
            holder.stockChange.setText(selectedNote.getChangeString());

            holder.fullSymbolName.setTextColor(Color.parseColor("#23C72D"));
            holder.smymbAbbreviation.setTextColor(Color.parseColor("#23C72D"));
            holder.arrow.setTextColor(Color.parseColor("#23C72D"));
            holder.stockPrice.setTextColor(Color.parseColor("#23C72D"));
            holder.stockChange.setTextColor(Color.parseColor("#23C72D"));

        }
        else{
            //red text
            holder.fullSymbolName.setText(selectedNote.getCompanyName());
            holder.smymbAbbreviation.setText(selectedNote.getStockSymbol());
            //TODO make a conditional for the arrow
            holder.arrow.setText("V");
            holder.stockPrice.setText(selectedNote.getPriceString());
            holder.stockChange.setText(selectedNote.getChangeString());

            holder.fullSymbolName.setTextColor(Color.parseColor("#FF0000"));
            holder.smymbAbbreviation.setTextColor(Color.parseColor("#FF0000"));
            holder.arrow.setTextColor(Color.parseColor("#FF0000"));
            holder.stockPrice.setTextColor(Color.parseColor("#FF0000"));
            holder.stockChange.setTextColor(Color.parseColor("#FF0000"));

        }

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

//    @Override
//    public long getItemId(int position) {
//        return position;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return position;
//    }


}
