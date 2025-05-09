
package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a movie in the game.
 * Contains basic movie information such as ID, title, release date, and genres.
 */
public class Movie {
    private int id;
    private String title;
    private String releaseDate;
    private List<String> genres;
    private String overview;
    private double voteAverage;

    /**
     * Constructor for the Movie class
     *
     * @param id Movie ID
     * @param title Movie title
     * @param releaseDate Release date
     * @param genres List of genres
     * @param overview Movie overview
     * @param voteAverage Movie rating
     */
    public Movie(int id, String title, String releaseDate, List<String> genres,
                 String overview, double voteAverage) {
        this.id = id;
        this.title = title;
        this.releaseDate = releaseDate;
        this.genres = new ArrayList<>(genres);
        this.overview = overview;
        this.voteAverage = voteAverage;
    }

    /**
     * Get the movie ID
     *
     * @return Movie ID
     */
    public int getId() {
        return id;
    }

    /**
     * Get the movie title
     *
     * @return Movie title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the movie release date
     *
     * @return Release date
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * Get the movie release year
     *
     * @return Release year, or "Unknown" if date format is invalid
     */
    public String getReleaseYear() {
        if (releaseDate != null && releaseDate.length() >= 4) {
            return releaseDate.substring(0, 4);
        }
        return "Unknown";
    }

    /**
     * Get the list of movie genres
     *
     * @return List of genres
     */
    public List<String> getGenres() {
        return new ArrayList<>(genres);
    }

    /**
     * Check if the movie belongs to a specific genre
     *
     * @param genre Genre to check
     * @return true if the movie has this genre, false otherwise
     */
    public boolean hasGenre(String genre) {
        return genres.contains(genre);
    }

    /**
     * Get the movie overview
     *
     * @return Movie overview
     */
    public String getOverview() {
        return overview;
    }

    /**
     * Get the movie rating
     *
     * @return Movie rating
     */
    public double getVoteAverage() {
        return voteAverage;
    }

    @Override
    public String toString() {
        return title + " (" + getReleaseYear() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Movie other = (Movie) obj;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}