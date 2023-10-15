package edu.uci.ics.fabflixmobile.data.model;

/**
 * Movie class that captures movie information for movies retrieved from MovieListActivity
 */
public class Movie {
    private final String name;

    private final String id;
    private final short year;

    private final String director;

    private final String genre;

    private final String stars;


    public Movie(String id, String name, short year, String director, String genre, String stars) {
        this.id = id;
        this.name = name;
        this.year = year;
        this.director = director;
        this.genre = genre;
        this.stars = stars;
    }
    public String getName() {
        return name;
    }
    public String getId() {
        return id;
    }

    public short getYear() {
        return year;
    }

    public String getDirector() {
        return director;
    }
    public String getGenre() {
        return genre;
    }
    public String getStars() {
        return stars;
    }
}