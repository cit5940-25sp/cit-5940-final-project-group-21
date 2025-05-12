package main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Movie Name Game.
 */
public class Player {
    private String name;
    private List<Movie> selectedMovies;
    private String connectionType; // "genre" or "person"
    private int targetCount;
    private int winProgress;

    public Player(String name) {
        this.name = name;
        this.selectedMovies = new ArrayList<>();
        this.connectionType = null;
        this.targetCount = 0;
        this.winProgress = 0;
    }

    public String getName() {
        return name;
    }

    public void setWinProgress(int progress) {
        this.winProgress = progress;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public void setTargetCount(int count) {
        this.targetCount = count;
    }

    public int getTargetCount() {
        return targetCount;
    }

    /**
     * Add a selected movie to this player's list.
     * @param movie the movie to add
     * @return true if the movie was added (not already present), false otherwise
     */
    public boolean addSelectedMovie(Movie movie) {
        if (selectedMovies.contains(movie)) {
            return false;
        }
        selectedMovies.add(movie);
        return true;
    }

    /**
     * Return a copy of this player's selected movies.
     */
    public List<Movie> getSelectedMovies() {
        return new ArrayList<>(selectedMovies);
    }

    /**
     * Check if the player has reached their target.
     */
    public boolean hasWon() {
        return winProgress >= targetCount;
    }

    /**
     * Increment the player's win progress by one.
     */
    public void incrementProgress() {
        winProgress++;
    }

    /**
     * Reset the player's selected movies and progress.
     */
    public void reset() {
        selectedMovies.clear();
        winProgress = 0;
    }

    /**
     * Get the player's current win progress.
     */
    public int getWinProgress() {
        return winProgress;
    }
}
