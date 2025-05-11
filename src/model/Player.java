package main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the game.
 * Contains player's name, selected movies, and win conditions.
 */
public class Player {
    private String name;
    private List<Movie> selectedMovies;
    private int winProgress;
    private String connectionType; // "genre" or "person"
    private int targetCount; // Number of movies needed to win

    /**
     * Constructor for the Player class
     *
     * @param name Player's name
     */
    public Player(String name) {
        this.name = name;
        this.selectedMovies = new ArrayList<>();
        this.winProgress = 0;
        this.connectionType = null;
        this.targetCount = 0;
    }

    /**
     * Get the player's name
     *
     * @return Player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set player's connection type
     *
     * @param connectionType Connection type ("genre" or "person")
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Get player's connection type
     *
     * @return Connection type
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Set the target count for winning
     *
     * @param count Number of movies needed to win
     */
    public void setTargetCount(int count) {
        this.targetCount = count;
    }

    /**
     * Get the target count for winning
     *
     * @return Target count
     */
    public int getTargetCount() {
        return targetCount;
    }

    /**
     * Add a selected movie
     *
     * @param movie Selected movie
     * @return true if added successfully, false if the movie was already selected
     */
    public boolean addSelectedMovie(Movie movie) {
        if (selectedMovies.contains(movie)) {
            return false;
        }

        selectedMovies.add(movie);
        return true;
    }

    /**
     * Increment win progress
     */
    public void incrementProgress() {
        winProgress++;
    }

    /**
     * Get all movies selected by the player
     *
     * @return List of selected movies
     */
    public List<Movie> getSelectedMovies() {
        return new ArrayList<>(selectedMovies);
    }

    /**
     * Get the N most recently selected movies
     *
     * @param count Number of movies to retrieve
     * @return List of recent movies
     */
    public List<Movie> getRecentMovies(int count) {
        int size = selectedMovies.size();
        int startIndex = Math.max(0, size - count);
        return new ArrayList<>(selectedMovies.subList(startIndex, size));
    }

    /**
     * Check if player has met win condition
     *
     * @return true if player has won, false otherwise
     */
    public boolean hasWon() {
        return winProgress >= targetCount;
    }

    /**
     * Get current win progress
     *
     * @return Current progress
     */
    public int getWinProgress() {
        return winProgress;
    }

    /**
     * Reset player state
     */
    public void reset() {
        selectedMovies.clear();
        winProgress = 0;
        // Don't reset connectionType and targetCount as they're set at game start
    }
}