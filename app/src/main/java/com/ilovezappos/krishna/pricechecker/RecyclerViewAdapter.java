package com.ilovezappos.krishna.pricechecker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ProductViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    ArrayList<Product> products = new ArrayList<>();
    private Context context;


    RecyclerViewAdapter(Context context, ArrayList<Product> products) {
        this.products = products;
        this.context = context;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, null);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ProductViewHolder holder, int i) {
        Log.v(TAG,"Displaying the results on recycler view");
        String dis = products.get(i).getDiscount();
        dis = dis.substring(0, 1);
        int disc = Integer.parseInt(dis);
        if (disc == 0) {
            holder.discount.setVisibility(View.GONE);
            holder.finalPrice.setText(products.get(i).getOrigPrice());
            holder.origPrice.setVisibility(View.GONE);
            holder.discLabel.setVisibility(View.GONE);
        } else {
            dis = products.get(i).getDiscount();
            dis += this.context.getResources().getString(R.string.discount_value_recycler_view_adapter_activity);
            holder.discLabel.setVisibility(View.VISIBLE);
            holder.discount.setVisibility(View.VISIBLE);
            holder.origPrice.setVisibility(View.VISIBLE);
            holder.discount.setText(dis);
            holder.origPrice.setText(products.get(i).getOrigPrice());
            holder.finalPrice.setText(products.get(i).getFinalPrice());
            holder.origPrice.setPaintFlags(holder.origPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        holder.productName.setText(products.get(i).getProductName());
        holder.brandName.setText(products.get(i).getBrandName());
        holder.productImage.setImageBitmap(products.get(i).getPhotoId());
        // needs a final variable for use inside an innerclass
        final int j = i;
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProductPage.class);
                intent.putExtra("product", products.get(j));
                context.startActivity(intent);
            }
        });
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    @Override
    public int getItemCount() {
        return products.size();
    }


    public class ProductViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView productName;
        TextView brandName;
        TextView origPrice;
        TextView finalPrice;
        TextView discount;
        ImageView productImage;
        TextView discLabel;

        ProductViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cardView);
            productImage = (ImageView) itemView.findViewById(R.id.productImage);
            productName = (TextView) itemView.findViewById(R.id.productName);
            brandName = (TextView) itemView.findViewById(R.id.brandName);
            origPrice = (TextView) itemView.findViewById(R.id.origPrice);
            finalPrice = (TextView) itemView.findViewById(R.id.finalPrice);
            discount = (TextView) itemView.findViewById(R.id.discount);
            discLabel = (TextView) itemView.findViewById(R.id.discountLabel);

        }
    }

}
