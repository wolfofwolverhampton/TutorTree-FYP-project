package com.javainternal.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.Model.TuitionPackageModel;
import com.javainternal.R;
import com.javainternal.Utils.KhaltiUtils;

import java.util.List;

public class TuitionPackageAdapter extends RecyclerView.Adapter<TuitionPackageAdapter.PackageViewHolder> {
    private final List<TuitionPackageModel> packageList;
    private final Context context;
    private final String teacherUid;
    private final String studentUid;

    public TuitionPackageAdapter(Context context, List<TuitionPackageModel> packageList, String teacherUid, String studentUid) {
        this.context = context;
        this.packageList = packageList;
        this.teacherUid = teacherUid;
        this.studentUid = studentUid;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        TuitionPackageModel pkg = packageList.get(position);
        holder.title.setText(pkg.getTitle());
        holder.price.setText("Rs. " + pkg.getPrice());

        holder.subscribeButton.setOnClickListener(v -> {
            FirebaseDatabase.getInstance()
                    .getReference("subscriptions")
                    .orderByChild("studentUid")
                    .equalTo(studentUid)
                    .get()
                    .addOnSuccessListener(dataSnapshot -> {
                        boolean hasActiveOrPending = false;

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            SubscriptionModel existingSubscription = snapshot.getValue(SubscriptionModel.class);

                            if (existingSubscription != null &&
                                    existingSubscription.getTeacherUid().equals(teacherUid)) {

                                SubscriptionStatus status = existingSubscription.getStatusEnum();
                                if (SubscriptionStatus.PENDING == status || SubscriptionStatus.PAID == status) {
                                    hasActiveOrPending = true;
                                    break;
                                }
                            }
                        }

                        if (hasActiveOrPending) {
                            Toast.makeText(context, "You already have a pending or paid subscription.", Toast.LENGTH_SHORT).show();
                        } else {
                            String subscriptionId = FirebaseDatabase.getInstance()
                                    .getReference("subscriptions")
                                    .push()
                                    .getKey();

                            if (subscriptionId == null) {
                                Toast.makeText(context, "Failed to create subscription ID", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            SubscriptionModel subscription = new SubscriptionModel(
                                    subscriptionId,
                                    pkg.getTitle(),
                                    pkg.getPrice(),
                                    pkg.getDurationInMonths(),
                                    teacherUid,
                                    studentUid,
                                    System.currentTimeMillis(),
                                    SubscriptionStatus.PENDING
                            );

                            FirebaseDatabase.getInstance()
                                    .getReference("subscriptions")
                                    .child(subscriptionId)
                                    .setValue(subscription)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(context, "Subscription saved. Proceed to pay.", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(context, "Failed to save subscription", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(context, "Failed to check existing subscriptions.", Toast.LENGTH_SHORT).show()
                    );
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView title, price;
        Button subscribeButton;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.packageTitle);
            price = itemView.findViewById(R.id.packagePrice);
            subscribeButton = itemView.findViewById(R.id.subscribeBtn);
        }
    }
}
