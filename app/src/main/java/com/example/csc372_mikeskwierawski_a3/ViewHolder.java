package com.example.csc372_mikeskwierawski_a3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
    Context context;
    TextView smymbAbbreviation;
    TextView fullSymbolName;
    TextView stockPrice;
    TextView arrow;
    TextView stockChange;
    private static final String MARKET_WATCH_LINK = "https://www.marketwatch.com/investing/stock/";


    ViewHolder(View view) {
        super(view);
        context = view.getContext();
        smymbAbbreviation = view.findViewById(R.id.symbAbbreviation);
        fullSymbolName = view.findViewById(R.id.fullSymbolName);
        stockPrice = view.findViewById(R.id.stockPrice);
        arrow = view.findViewById(R.id.stockArrow);
        stockChange = view.findViewById(R.id.stockChange);

        view.setOnClickListener(this);
        view.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Toast.makeText(view.getContext(), "position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(MARKET_WATCH_LINK).withAppendedPath(MARKET_WATCH_LINK,;

    }

    @Override
    public boolean onLongClick(View view) {
        final View aView = view;
        final Context th = aView.getContext();
        Toast.makeText(view.getContext(), "long click. position = " + getLayoutPosition(), Toast.LENGTH_SHORT).show();
        final int deletePosition = getLayoutPosition();
        boolean delete = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        builder.setTitle("Delete Stock");
        builder.setMessage("Do you want to delete this stock?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO context.delete(deletePosition)
                // But I cant get the context of mainactivity from inside of this anonymous function
//                deleteFromList(deletePosition);
            }
        }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        return false;


    }

}