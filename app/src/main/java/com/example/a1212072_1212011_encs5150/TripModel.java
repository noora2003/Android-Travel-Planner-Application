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

    public TripModel(int id, String destination, String country,
                     int durationDays, double price, double rating,
                     String description, String image) {
        this.id           = id;
        this.destination  = destination;
        this.country      = country;
        this.durationDays = durationDays;
        this.price        = price;
        this.rating       = rating;
        this.description  = description;
        this.image        = image;
        this.isFavorite   = false;
    }

    public int getId()             { return id; }
    public String getDestination() { return destination; }
    public String getCountry()     { return country; }
    public int getDurationDays()   { return durationDays; }
    public double getPrice()       { return price; }
    public double getRating()      { return rating; }
    public String getDescription() { return description; }
    public String getImage()       { return image; }
    public boolean isFavorite()    { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}
