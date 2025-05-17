package com.javainternal.Model;

public class TuitionPackageModel {
    private final String title;
    private final int durationInMonths;
    private final double price;

    public TuitionPackageModel(String title, int durationInMonths, double price) {
        this.title = title;
        this.durationInMonths = durationInMonths;
        this.price = price;
    }

    // Getters
    public String getTitle() { return title; }
    public int getDurationInMonths() { return durationInMonths; }
    public double getPrice() { return price; }

}
