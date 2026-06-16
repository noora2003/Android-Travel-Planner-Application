package com.example.a1212072_1212011_encs5150;

public class Trip {
    private int id;
    private String destination;
    private String country;
    private int durationDays;
    private double price;
    private double rating;
    private String description;
    private String imageUrl;
    private boolean isFavorite; // محلي، ليس في DB

    public Trip(int id, String destination, String country, int durationDays,
                double price, double rating, String description, String imageUrl) {
        this.id = id;
        this.destination = destination;
        this.country = country;
        this.durationDays = durationDays;
        this.price = price;
        this.rating = rating;
        this.description = description;
        this.imageUrl = imageUrl;
        this.isFavorite = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public String getDestination() { return destination; }
    public String getCountry() { return country; }
    public int getDurationDays() { return durationDays; }
    public double getPrice() { return price; }
    public double getRating() { return rating; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}