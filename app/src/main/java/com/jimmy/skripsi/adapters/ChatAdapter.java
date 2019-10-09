package com.jimmy.skripsi.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.models.ChatModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ItemHolder> {

    private Context context;
    private List<ChatModel> items = new ArrayList<>();

    public ChatAdapter(Context context){
        this.context = context;
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        ItemHolder item = new ItemHolder(view);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder holder, int position) {
        ChatModel message = items.get(position);
        if(PrefManager.isAdmin()){
            if (message.getName().equals("admin")) {
                holder.tvTimeSend.setText(message.getTime());
                holder.tvMessageContentSend.setText(message.getMessage());
                holder.setIsSender(true);
            } else {
                holder.tvUsername.setText(message.getName());
                holder.tvTimeReceive.setText(message.getTime());
                holder.tvMessageContentReceive.setText(message.getMessage());
                holder.setIsSender(false);
            }
        }else {
            if (!message.getName().equals("admin")) {
                holder.tvTimeSend.setText(message.getTime());
                holder.tvMessageContentSend.setText(message.getMessage());
                holder.setIsSender(true);
            } else {
                holder.tvUsername.setText(message.getName());
                holder.tvTimeReceive.setText(message.getTime());
                holder.tvMessageContentReceive.setText(message.getMessage());
                holder.setIsSender(false);
            }
        }

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_time_send) TextView tvTimeSend;
        @BindView(R.id.tv_message_content_send) TextView tvMessageContentSend;
        @BindView(R.id.tv_username) TextView tvUsername;
        @BindView(R.id.tv_time_receive) TextView tvTimeReceive;
        @BindView(R.id.tv_message_content_receive) TextView tvMessageContentReceive;
        @BindView(R.id.lyt_sender) View lyt_sender;
        @BindView(R.id.lyt_receiver) View lyt_receiver;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void setIsSender(boolean isSender){
            lyt_sender.setVisibility(isSender ? View.VISIBLE:View.GONE);
            lyt_receiver.setVisibility(isSender ? View.GONE:View.VISIBLE);
        }
    }

    public void addItem(List<ChatModel> data){
        if(items!=null && items.size() > 0)items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    public void add(ChatModel item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null item to the Recycler adapter");
        }
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }


}
