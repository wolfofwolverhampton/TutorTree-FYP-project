package com.javainternal.Students.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ChatActivity;
import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.Model.TuitionPackageModel;
import com.javainternal.R;
import com.javainternal.Students.StudentAttendanceActivity;
import com.javainternal.Students.StudentResutlActivity;
import com.javainternal.Utils.KhaltiUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyTeacherAdapter extends RecyclerView.Adapter<MyTeacherAdapter.TeacherViewHolder> {

    private final Context context;
    private final List<SubscriptionModel> subscriptions;

    public MyTeacherAdapter(Context context, List<SubscriptionModel> subscriptions) {
        this.context = context;
        this.subscriptions = subscriptions;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_teacher, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        SubscriptionModel sub = subscriptions.get(position);
        SubscriptionStatus status = sub.getStatusEnum();

        holder.packageTitle.setText(sub.getPackageTitle());
        holder.packagePrice.setText("Rs. " + sub.getPackagePrice());
        holder.teacherName.setText(sub.getTeacherName());

        switch (status) {
            case PENDING:
                holder.subscriptionStatus.setText("Pending - Waiting for teacher's approval");
                holder.payNowBtn.setVisibility(View.GONE);
                holder.cancelBtn.setVisibility(View.VISIBLE);
                holder.cancelBtn.setOnClickListener(v -> {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition == RecyclerView.NO_POSITION) return;

                    FirebaseDatabase.getInstance().getReference("subscriptions")
                            .child(sub.getSubscriptionId())
                            .removeValue()
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(context, "Subscription request cancelled", Toast.LENGTH_SHORT).show();

                                subscriptions.remove(adapterPosition);
                                notifyItemRemoved(adapterPosition);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(context, "Failed to cancel: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                });
                break;

            case ACCEPTED:
                holder.subscriptionStatus.setText("Accepted - You can pay now");
                holder.payNowBtn.setVisibility(View.VISIBLE);
                holder.payNowBtn.setText("Pay Now");
                holder.payNowBtn.setOnClickListener(v -> {
                    TuitionPackageModel packageModel = new TuitionPackageModel(sub.getPackageTitle(), sub.getPackageDuration(), sub.getPackagePrice());
                    KhaltiUtils.payWithKhalti(context, packageModel, sub);
                });
                break;

            case PAID:
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(sub.getSubscribedAt());
                calendar.add(Calendar.MONTH, sub.getPackageDuration());

                Date deadlineDate = calendar.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
                holder.subscriptionStatus.setText("Paid | Valid till: " + sdf.format(deadlineDate));
                holder.payNowBtn.setVisibility(View.GONE);

                holder.ratingBar.setVisibility(View.VISIBLE);
                holder.submitRatingBtn.setVisibility(View.VISIBLE);
                holder.tvRateTeacher.setVisibility(View.VISIBLE);
                holder.chatBtn.setVisibility(View.VISIBLE);
                holder.viewAttendanceBtn.setVisibility(View.VISIBLE);
                holder.viewResultsBtn.setVisibility(View.VISIBLE);

                holder.ratingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));

                String teacherUid = sub.getTeacherUid();
                String studentUid = sub.getStudentUid();

                DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference ratingRef = dbRef.child("teachers")
                        .child(teacherUid)
                        .child("ratings")
                        .child(studentUid);

                ratingRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists() && snapshot.child("stars").getValue() != null) {
                            long stars = snapshot.child("stars").getValue(Long.class);
                            holder.ratingBar.setRating((float) stars);
                            holder.ratingBar.setIsIndicator(false);
                            holder.submitRatingBtn.setEnabled(true);
                        } else {
                            holder.ratingBar.setRating(0f);
                            holder.ratingBar.setIsIndicator(false);
                            holder.submitRatingBtn.setEnabled(true);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(context, "Failed to fetch rating", Toast.LENGTH_SHORT).show();
                    }
                });

                holder.submitRatingBtn.setOnClickListener(v -> {
                    int rating = (int) holder.ratingBar.getRating();
                    if (rating == 0) {
                        Toast.makeText(context, "Please select a rating before submitting", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(context, "You rated " + sub.getTeacherName() + " " + rating + " stars", Toast.LENGTH_SHORT).show();

                    Map<String, Object> ratingData = new HashMap<>();
                    ratingData.put("stars", rating);

                    ratingRef.setValue(ratingData).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Rating submitted!", Toast.LENGTH_SHORT).show();
                            holder.submitRatingBtn.setEnabled(false);
                            holder.ratingBar.setIsIndicator(true); // lock after submit
                        } else {
                            Toast.makeText(context, "Rating failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                });

                holder.chatBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("name", sub.getTeacherName());
                        intent.putExtra("senderUid", sub.getStudentUid());
                        intent.putExtra("receiverUid", sub.getTeacherUid());
                        context.startActivity(intent);
                    }
                });

                holder.viewAttendanceBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, StudentAttendanceActivity.class);
                        intent.putExtra("subscribedAt", sub.getSubscribedAt());
                        intent.putExtra("tuitionDuration", sub.getPackageDuration());
                        context.startActivity(intent);
                    }
                });

                holder.viewResultsBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, StudentResutlActivity.class);
                        intent.putExtra("teacherUid", sub.getTeacherUid());
                        intent.putExtra("durationInMonths", sub.getPackageDuration());
                        intent.putExtra("subscribedAt", sub.getSubscribedAt());
                        context.startActivity(intent);
                    }
                });

                break;

            case CANCELLED:
                holder.subscriptionStatus.setText("Cancelled");
                holder.payNowBtn.setVisibility(View.GONE);
                break;

            case COMPLETED:
                holder.subscriptionStatus.setText("Completed");
                holder.payNowBtn.setVisibility(View.GONE);
                break;

            default:
                holder.subscriptionStatus.setText("Unknown status");
                holder.payNowBtn.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return subscriptions.size();
    }

    static class TeacherViewHolder extends RecyclerView.ViewHolder {
        TextView packageTitle, packagePrice, subscriptionStatus, teacherName, tvRateTeacher;
        Button payNowBtn, cancelBtn, chatBtn, viewAttendanceBtn, viewResultsBtn;
        RatingBar ratingBar;
        Button submitRatingBtn;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            packageTitle = itemView.findViewById(R.id.packageTitle);
            packagePrice = itemView.findViewById(R.id.packagePrice);
            subscriptionStatus = itemView.findViewById(R.id.subscriptionStatus);
            teacherName = itemView.findViewById(R.id.teacherName);
            payNowBtn = itemView.findViewById(R.id.payNowBtn);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            submitRatingBtn = itemView.findViewById(R.id.submitRatingBtn);
            tvRateTeacher = itemView.findViewById(R.id.tvRateTeacher);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
            chatBtn = itemView.findViewById(R.id.chatBtn);
            viewAttendanceBtn = itemView.findViewById(R.id.viewAttendanceBtn);
            viewResultsBtn = itemView.findViewById(R.id.viewResultsBtn);
        }
    }
}
