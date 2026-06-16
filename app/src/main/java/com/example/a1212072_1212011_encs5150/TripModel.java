package com.example.a1212072_1212011_encs5150;

public class TripModel {
    private int id;
    private String destination;
    private String country;
    private int durationDays;
    private double price;
    private double rating;
    private String description;
    private String image;
    private boolean isFavorite;

    public TripModel(int id, String destination, String country, int durationDays,
                     double price, double rating, String description, String image) {
        this.id = id;
        this.destination = destination;
        this.country = country;
        this.durationDays = durationDays;
        this.price = price;
        this.rating = rating;
        this.description = description;
        this.image = image;
        this.isFavorite = false;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getDurationDays() { return durationDays; }
    public void setDurationDays(int durationDays) { this.durationDays = durationDays; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}