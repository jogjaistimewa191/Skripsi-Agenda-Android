package com.jimmy.skripsi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimmy.skripsi.models.AgendaModel;
import com.jimmy.skripsi.R;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AgendaAdapter extends RecyclerView.Adapter<AgendaAdapter.ItemHolder> implements Filterable {

    private Context context;
    private boolean isAdmin;
    private List<AgendaModel> items = new ArrayList<>();
    private List<AgendaModel> itemFiltered = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public AgendaAdapter(Context context, boolean isAdmin){
        this.context = context;
        this.isAdmin = isAdmin;

    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_agenda, parent, false);
        ItemHolder item = new ItemHolder(view);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        final AgendaModel data = itemFiltered.get(position);
        holder.tvAgenda.setText(data.getNama());
        holder.tvDeskripsi.setText(data.getDeskripsi());
        holder.btnLihat.setOnClickListener(v -> {
            if(onItemClickListener!=null) onItemClickListener.onLihat(v, data, holder.getAdapterPosition());
        });
        holder.actLihat.setOnClickListener(v -> {
            if(onItemClickListener!=null) onItemClickListener.onLihat(v, data, holder.getAdapterPosition());
        });
        holder.btnEdit.setOnClickListener(v -> {
            if(onItemClickListener!=null) onItemClickListener.onEdit(v, data, holder.getAdapterPosition());
        });
        holder.btnHapus.setOnClickListener(v -> {
            if(onItemClickListener!=null) onItemClickListener.onHapus(v, data, holder.getAdapterPosition());
        });

        holder.lytAdmin.setVisibility(isAdmin ? View.VISIBLE:View.GONE);
        holder.lytUser.setVisibility(isAdmin ? View.GONE:View.VISIBLE);

    }

    @Override
    public int getItemCount() {
        return itemFiltered.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvAgenda) TextView tvAgenda;
        @BindView(R.id.tvDeskripsi) TextView tvDeskripsi;
        @BindView(R.id.btnLihat) Button btnLihat;
        @BindView(R.id.actLihat) Button actLihat;
        @BindView(R.id.actAdmin) View lytAdmin;
        @BindView(R.id.actUser) View lytUser;
        @BindView(R.id.btnEdit) Button btnEdit;
        @BindView(R.id.btnHapus) Button btnHapus;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void addItem(List<AgendaModel> data){
        if(items!=null && items.size() > 0)items.clear();
        items.addAll(data);
        itemFiltered = items;
        notifyDataSetChanged();
    }

    public void removeItem(int position){
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onLihat(View view, AgendaModel obj, int position);
        void onHapus(View view, AgendaModel obj, int position);
        void onEdit(View view, AgendaModel obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    itemFiltered = items;
                } else {
                    List<AgendaModel> filteredList = new ArrayList<>();
                    for (AgendaModel row : items) {

                        // name match condition. this might differ depending on your requirement
                        // here we are looking for name or phone number match
                        if (row.getNama().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    itemFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                itemFiltered = (ArrayList<AgendaModel>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }
}
