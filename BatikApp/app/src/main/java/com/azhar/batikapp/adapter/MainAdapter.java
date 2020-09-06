package com.azhar.batikapp.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.azhar.batikapp.R;
import com.azhar.batikapp.model.ModelMain;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by Azhar Rivaldi on 22-12-2019.
 */

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<ModelMain> items;
    private MainAdapter.onSelectData onSelectData;
    private Context mContext;

    public interface onSelectData {
        void onSelected(ModelMain modelMain);
    }

    public MainAdapter(Context context, List<ModelMain> items, MainAdapter.onSelectData xSelectData) {
        this.mContext = context;
        this.items = items;
        this.onSelectData = xSelectData;
    }

    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_batik, parent, false);
        return new MainAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MainAdapter.ViewHolder holder, int position) {
        final ModelMain data = items.get(position);

        holder.tvTitle.setText(data.getNamaBatik());
        holder.tvDilihat.setText("Dilihat " +data.getHitungView() + " orang");
        holder.tvDesc.setText(data.getMaknaBatik());

        Glide.with(mContext)
                .load(data.getLinkBatik())
                .apply(new RequestOptions())
                .into(holder.imgBatik);

        holder.cvBatik.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSelectData.onSelected(data);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //Class Holder
    class ViewHolder extends RecyclerView.ViewHolder {

        public CardView cvBatik;
        public ImageView imgBatik;
        public TextView tvTitle;
        public TextView tvDilihat;
        public TextView tvDesc;

        public ViewHolder(View itemView) {
            super(itemView);
            cvBatik = itemView.findViewById(R.id.cvBatik);
            imgBatik = itemView.findViewById(R.id.imgBatik);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDilihat = itemView.findViewById(R.id.tvDilihat);
            tvDesc = itemView.findViewById(R.id.tvDesc);
        }
    }
}