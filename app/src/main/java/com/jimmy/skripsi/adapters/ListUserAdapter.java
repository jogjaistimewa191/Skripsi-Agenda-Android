package com.jimmy.skripsi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.UserModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListUserAdapter extends RecyclerView.Adapter<ListUserAdapter.ItemHolder> {

    private Context context;
    private List<String> items = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    List<String> name = new ArrayList<>();
    public ListUserAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        ItemHolder item = new ItemHolder(view);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        final String roomId = items.get(position);
        String userKey = roomId.substring(roomId.lastIndexOf("-")+1);
        String userName = roomId.substring(0, roomId.indexOf("-"));
        String inisialName = roomId.substring(0, 1);
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference("users");
        mRef.child(userKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //holder.tvInitial.setText(data.getNama());
                UserModel mData = dataSnapshot.getValue(UserModel.class);
                if(mData!=null) {
                    holder.tvName.setText(mData.getNama());
                    name.add(mData.getNama());
                }else {
                    name.add("user");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        holder.tvInitial.setText(inisialName);
        holder.ic_circle.setCardBackgroundColor(Util.getRandColor());
        holder.root.setOnClickListener(v -> {
            if(onItemClickListener!=null) onItemClickListener.onClick(userName, roomId, holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.root) View root;
        @BindView(R.id.ic_circle) CardView ic_circle;
        @BindView(R.id.tvInitial) TextView tvInitial;
        @BindView(R.id.tvName) TextView tvName;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public void removeItem(int position){
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
        notifyDataSetChanged();
    }

    public interface OnItemClickListener {
        void onClick(String user, String room, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.onItemClickListener = mItemClickListener;
    }
    public void add(String item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null item to the Recycler adapter");
        }
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }
}
