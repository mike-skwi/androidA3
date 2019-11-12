package com.example.csc372_mikeskwierawski_a3;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {

    TextView smymbAbbreviation;
    TextView fullSymbolName;
    TextView stockPrice;
    TextView arrow;
    TextView stockChange;

    ViewHolder(View view) {
        super(view);
        smymbAbbreviation = view.findViewById(R.id.symbAbbreviation);
        fullSymbolName = view.findViewById(R.id.fullSymbolName);
        stockPrice = view.findViewById(R.id.stockPrice);
        arrow = view.findViewById(R.id.stockArrow);
        stockChange = view.findViewById(R.id.stockChange);

    }
}