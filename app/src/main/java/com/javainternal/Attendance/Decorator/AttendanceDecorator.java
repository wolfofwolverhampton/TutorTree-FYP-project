package com.javainternal.Attendance.Decorator;

import android.graphics.drawable.ColorDrawable;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;

import java.util.HashSet;
import java.util.List;

public class AttendanceDecorator implements DayViewDecorator {

    private final HashSet<CalendarDay> dates;
    private final int color;

    public AttendanceDecorator(List<CalendarDay> dates, int color) {
        this.dates = new HashSet<>(dates);
        this.color = color;
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setBackgroundDrawable(new ColorDrawable(color));
    }
}
