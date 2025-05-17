package com.javainternal.Attendance.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.Attendance.Model.AttendanceModel;
import com.javainternal.R;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DayViewHolder> {

    private final List<AttendanceModel> attendanceDays;

    public CalendarAdapter(List<AttendanceModel> attendanceDays) {
        this.attendanceDays = attendanceDays;
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.day_item, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        AttendanceModel day = attendanceDays.get(position);
//        holder.dayText.setText(String.valueOf(day.getDate().getDayOfMonth()));

        if (day.isPresent()) {
            holder.dayText.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.dayText.setBackgroundColor(Color.parseColor("#F44336")); // Red
        }
    }

    @Override
    public int getItemCount() {
        return attendanceDays.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayText;
        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayText = itemView.findViewById(R.id.dayText);
        }
    }
}
