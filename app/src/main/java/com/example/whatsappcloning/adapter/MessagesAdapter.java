package com.example.whatsappcloning.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.whatsappcloning.R;
import com.example.whatsappcloning.model.Chats;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesHolder> {

    private Context context;
    private ArrayList<Chats> chatsList;
    private String imageUrl;

    private FirebaseUser fUser;

    public static final int CHAT_SENDER_USER_0 = 0;
    public static final int CHAT_RECEIVER_USER_1 = 1;

    public MessagesAdapter(Context context, ArrayList<Chats> chatsList, String imageUrl) {
        this.context = context;
        this.chatsList = chatsList;
        this.imageUrl = imageUrl;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessagesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        //this viewType Belong to Method getItemViewType(); which return numbers.
        if (viewType == CHAT_SENDER_USER_0) {

            view = LayoutInflater.from(context).inflate(R.layout.chat_right_item, parent, false);
            return new MessagesHolder(view);
        }
        else {

            view = LayoutInflater.from(context).inflate(R.layout.chat_left_item, parent, false);
            return new MessagesHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessagesHolder holder, int position) {

        Chats chats = chatsList.get(position);

        holder.tvMessage.setText(chats.getMessage());

        if (imageUrl.equals("default")) {

            holder.ivMessage.setImageResource(R.drawable.user);
        }
        else {

            Glide.with(context).load(imageUrl).circleCrop().into(holder.ivMessage);
        }

        if (position == chatsList.size() -1) {

            if (chats.isSeen()) {

                holder.tvSeen.setText("Seen");
            }
            else {

                holder.tvSeen.setText("Delivered");
            }
        }
        else {

            holder.tvSeen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemViewType(int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        Chats chats = chatsList.get(position);

        if (chats.getSender().equals(fUser.getUid())) {

            return CHAT_SENDER_USER_0;
        }
        else {

            return CHAT_RECEIVER_USER_1;
        }
    }

    @Override
    public int getItemCount() {
        return chatsList.size();
    }

    class MessagesHolder extends RecyclerView.ViewHolder {

        ImageView ivMessage;
        TextView tvMessage, tvSeen;

        public MessagesHolder(@NonNull View itemView) {
            super(itemView);

            ivMessage = itemView.findViewById(R.id.iv_chat);
            tvMessage = itemView.findViewById(R.id.tv_chat);
            tvSeen = itemView.findViewById(R.id.tv_status_chat);
        }
    }
}
