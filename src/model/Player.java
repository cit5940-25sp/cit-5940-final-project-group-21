package main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Movie Name Game.
 * This class tracks a player's progress toward their win condition,
 * maintains their movie selection history, and defines their preferred
 * connection strategy (genre or person-based).
 *
 * Players function as both game state components and strategy holders,
 * allowing for different play styles between opponents.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class Player {
    private String name;
    private List<Movie> selectedMovies;
    private String connectionType; // "genre" or "person"
    private int targetCount;
    private int winProgress;

    /**
     * Constructs a new player with the given name.
     * Initializes an empty movie selection list and zero progress.
     * Connection type and target count must be set separately before gameplay.
     *
     * @param name The player's display name
     */
    public Player(String name) {
        this.name = name;
        this.selectedMovies = new ArrayList<>();
        this.connectionType = null;
        this.targetCount = 0;
        this.winProgress = 0;
    }

    /**
     * Gets the player's name.
     *
     * @return The player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the player's current progress toward their win condition.
     * This is mainly used during game initialization or for testing.
     *
     * @param progress The new progress value
     */
    public void setWinProgress(int progress) {
        this.winProgress = progress;
    }

    /**
     * Sets the player's preferred connection type strategy.
     * This determines how they'll score points (matching by genres or people).
     *
     * @param connectionType The connection type ("genre" or "person")
     */
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    /**
     * Gets the player's current connection type strategy.
     *
     * @return The connection type ("genre" or "person")
     */
    public String getConnectionType() {
        return connectionType;
    }

    /**
     * Sets the target number of connections needed to win.
     * This creates variable difficulty levels based on player preference.
     *
     * @param count The number of successful connections needed to win
     */
    public void setTargetCount(int count) {
        this.targetCount = count;
    }

    /**
     * Gets the number of connections needed for this player to win.
     *
     * @return The target count for winning
     */
    public int getTargetCount() {
        return targetCount;
    }

    /**
     * Adds a selected movie to this player's history list.
     * Prevents duplicates to enforce the game rule that each movie can only be used once.
     *
     * @param movie The movie to add to this player's selection history
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
     * Returns a defensive copy of this player's selected movies.
     * Returns a new list to prevent external code from modifying the player's history.
     *
     * @return A new list containing all movies selected by this player
     */
    public List<Movie> getSelectedMovies() {
        return new ArrayList<>(selectedMovies);
    }

    /**
     * Checks if the player has reached their target progress and won the game.
     * This is used to determine when the game should end.
     *
     * @return true if the player has won, false otherwise
     */
    public boolean hasWon() {
        return winProgress >= targetCount;
    }

    /**
     * Increments the player's win progress by one.
     * Called when a player makes a valid movie connection.
     */
    public void incrementProgress() {
        winProgress++;
    }

    /**
     * Resets the player's selected movies and progress.
     * Used when starting a new game or round.
     */
    public void reset() {
        selectedMovies.clear();
        winProgress = 0;
    }

    /**
     * Gets the player's current progress toward their win condition.
     *
     * @return The current number of successful connections made
     */
    public int getWinProgress() {
        return winProgress;
    }
}