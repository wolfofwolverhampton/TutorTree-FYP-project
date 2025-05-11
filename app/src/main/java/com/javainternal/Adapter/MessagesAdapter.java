package com.javainternal.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.javainternal.Model.Message;
import com.javainternal.R;
import com.javainternal.databinding.ItemReceiveBinding;
import com.javainternal.databinding.ItemSentBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<Message> messages;
    private String senderUid; // Add this field to store the sender UID
    private final int ITEM_SENT = 1;
    private final int ITEM_RECEIVE = 2;

    public MessagesAdapter(Context context, ArrayList<Message> messages, String senderUid) {
        this.context = context;
        this.messages = messages;
        this.senderUid = senderUid; // Initialize the sender UID
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SENT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_sent, parent, false);
            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_receive, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String senderId = message.getSenderId();
        // Use the senderUid passed to the adapter instead of FirebaseAuth.getInstance().getUid()
        if (senderUid != null && senderId != null && senderUid.equals(senderId)) {
            return ITEM_SENT; // Sent by the current user
        } else {
            return ITEM_RECEIVE; // Received from another user
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        // Format the timestamp
        String formattedTimestamp = formatTimestamp(message.getTimestamp());

        if (holder instanceof SentViewHolder) {
            ((SentViewHolder) holder).binding.message.setText(message.getMessage());
            ((SentViewHolder) holder).binding.timestamp.setText(formattedTimestamp);
        } else if (holder instanceof ReceiverViewHolder) {
            ((ReceiverViewHolder) holder).binding.message.setText(message.getMessage());
            ((ReceiverViewHolder) holder).binding.timestamp.setText(formattedTimestamp);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class SentViewHolder extends RecyclerView.ViewHolder {
        ItemSentBinding binding;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemSentBinding.bind(itemView);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        ItemReceiveBinding binding;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = ItemReceiveBinding.bind(itemView);
        }
    }

    // Utility method to format the timestamp
    private String formatTimestamp(long timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            return sdf.format(new Date(timestamp));
        } catch (Exception e) {
            e.printStackTrace();
            return "Unknown";
        }
    }
}