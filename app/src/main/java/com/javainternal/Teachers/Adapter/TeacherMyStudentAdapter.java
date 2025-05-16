package com.javainternal.Teachers.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.R;

import java.util.List;

public class TeacherMyStudentAdapter extends RecyclerView.Adapter<TeacherMyStudentAdapter.StudentViewHolder> {

    public interface OnRequestActionListener {
        void onAccept(SubscriptionModel subscription);
        void onReject(SubscriptionModel subscription);
        void onAssign(String studentUid);
    }

    private final Context context;
    private final List<SubscriptionModel> studentRequests;
    private final OnRequestActionListener listener;

    public TeacherMyStudentAdapter(Context context, List<SubscriptionModel> studentRequests, OnRequestActionListener listener) {
        this.context = context;
        this.studentRequests = studentRequests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_student, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        SubscriptionModel sub = studentRequests.get(position);

        holder.studentName.setText(sub.getStudentName());
        holder.packageTitle.setText(sub.getPackageTitle());

        SubscriptionStatus status = sub.getStatusEnum();

        if (status == SubscriptionStatus.PENDING) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);

            holder.acceptButton.setOnClickListener(v -> {
                if (listener != null) listener.onAccept(sub);
            });

            holder.rejectButton.setOnClickListener(v -> {
                if (listener != null) listener.onReject(sub);
            });
        } else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
        }

        if (status == SubscriptionStatus.PAID) {
            holder.assignButton.setVisibility(View.VISIBLE);
            holder.assignButton.setOnClickListener(v -> {
                if (listener != null) listener.onAssign(sub.getStudentUid());
            });
        } else {
            holder.assignButton.setVisibility(View.GONE);
        }

        switch (status) {
            case ACCEPTED:
                holder.subscriptionStatus.setText("Accepted - You can pay now");
                break;
            case CANCELLED:
                holder.subscriptionStatus.setText("Cancelled");
                break;
            case PAID:
                holder.subscriptionStatus.setText("Paid");
                break;
            case COMPLETED:
                holder.subscriptionStatus.setText("Completed");
                break;
            default:
                holder.subscriptionStatus.setText("Pending");
        }

    }

    @Override
    public int getItemCount() {
        return studentRequests.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView studentName, packageTitle, subscriptionStatus;
        Button acceptButton, rejectButton, assignButton;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            studentName = itemView.findViewById(R.id.studentName);
            packageTitle = itemView.findViewById(R.id.packageTitle);
            subscriptionStatus = itemView.findViewById(R.id.subscriptionStatus);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            assignButton = itemView.findViewById(R.id.assignButton);
        }
    }
}

