package com.example.whatsappcloning.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.whatsappcloning.ui.MessageActivity;
import com.example.whatsappcloning.R;
import com.example.whatsappcloning.databinding.UserItemBinding;
import com.example.whatsappcloning.model.Users;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersHolder> {

    private Context context;
    private ArrayList<Users> usersList;

    private boolean isChat;

    public UsersAdapter(Context context, ArrayList<Users> usersList, boolean isChat) {
        this.context = context;
        this.usersList = usersList;
        this.isChat = isChat;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new UsersHolder(LayoutInflater.from(context).inflate(R.layout.user_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UsersHolder holder, int position) {

        Users users = usersList.get(position);

        if (isChat) {

            if (users.getStatus().equals("online")) {

                holder.binding.statusON.setVisibility(View.VISIBLE);
                holder.binding.statusOFF.setVisibility(View.GONE);
            }
            else {

                holder.binding.statusON.setVisibility(View.GONE);
                holder.binding.statusOFF.setVisibility(View.VISIBLE);
            }
        }
        else {

            holder.binding.statusON.setVisibility(View.GONE);
            holder.binding.statusOFF.setVisibility(View.GONE);
        }

        if (users.getImageUrl().equals("default")) {

            holder.binding.ivImageUser.setImageResource(R.drawable.user);
        }
        else {

            Glide.with(context).load(users.getImageUrl()).circleCrop()
                    .into(holder.binding.ivImageUser);
        }

        holder.binding.tvUserName.setText(users.getUserName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, MessageActivity.class);
                intent.putExtra("userId", users.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class UsersHolder extends RecyclerView.ViewHolder {

        UserItemBinding binding;

        public UsersHolder(@NonNull View itemView) {
            super(itemView);

            binding = UserItemBinding.bind(itemView);
        }
    }
}
