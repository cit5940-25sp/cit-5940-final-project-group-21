package main.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the game state and manages game logic and rules.
 */
public class GameState {
    // Game states
    public enum State {
        WAITING_FOR_PLAYERS, // Waiting for players to join
        SETTING_WIN_CONDITIONS, // Setting win conditions
        PLAYING, // Game in progress
        GAME_OVER // Game ended
    }

    private State currentState;
    private main.model.Player player1;
    private main.model.Player player2;
    private main.model.Player currentPlayer;
    private Movie currentMovie;
    private int roundCount;
    private Map<String, Integer> connectionCounts; // Tracks number of times a connection is used
    private Map<String, Boolean> bannedConnections; // Tracks banned connections

    /**
     * Constructor
     */
    public GameState() {
        currentState = State.WAITING_FOR_PLAYERS;
        connectionCounts = new HashMap<>();
        bannedConnections = new HashMap<>();
        roundCount = 0;
    }

    /**
     * Add a player to the game
     *
     * @param name Player name
     * @return true if player added successfully, false if game is full
     */
    public boolean addPlayer(String name) {
        if (player1 == null) {
            player1 = new main.model.Player(name);
            return true;
        } else if (player2 == null) {
            player2 = new main.model.Player(name);
            currentState = State.SETTING_WIN_CONDITIONS;
            return true;
        }
        return false;
    }

    /**
     * Set a player's win condition
     *
     * @param player Player
     * @param winCondition Win condition
     */
    public void setPlayerWinCondition(main.model.Player player, main.model.Player.WinCondition winCondition) {
        player.setWinCondition(winCondition);

        // If both players have set win conditions, start the game
        if (player1.getWinCondition() != null && player2.getWinCondition() != null) {
            currentState = State.PLAYING;
            currentPlayer = player1; // Player 1 goes first
        }
    }

    /**
     * Start a new game
     *
     * @param startingMovie Starting movie
     */
    public void startGame(Movie startingMovie) {
        if (currentState != State.PLAYING) {
            throw new IllegalStateException("Cannot start game, current state: " + currentState);
        }

        currentMovie = startingMovie;
        roundCount = 1;

        // Clear previous game data
        player1.reset();
        player2.reset();
        connectionCounts.clear();
        bannedConnections.clear();
    }

    /**
     * Player selects the next movie
     *
     * @param movie Selected movie
     * @param connection Connection type (actor, director, etc.)
     * @return true if selection is valid, false otherwise
     */
    public boolean selectMovie(Movie movie, String connection) {
        if (currentState != State.PLAYING) {
            return false;
        }

        // Check if movie has already been used
        if (player1.getSelectedMovies().contains(movie) ||
                player2.getSelectedMovies().contains(movie)) {
            return false;
        }

        // Check if connection is valid
        if (!isConnectionValid(connection)) {
            return false;
        }

        // Record selection
        currentPlayer.addSelectedMovie(movie);
        updateConnectionCount(connection);
        currentMovie = movie;

        // Check win condition
        if (currentPlayer.hasWon()) {
            currentState = State.GAME_OVER;
            return true;
        }

        // Switch players
        switchPlayer();
        roundCount++;

        return true;
    }

    /**
     * Check if connection is valid
     *
     * @param connection Connection to check
     * @return true if valid, false otherwise
     */
    private boolean isConnectionValid(String connection) {
        // Check if connection is banned
        if (bannedConnections.getOrDefault(connection, false)) {
            return false;
        }

        // Check if connection has been used too many times
        int count = connectionCounts.getOrDefault(connection, 0);
        return count < 3; // Maximum 3 uses per connection
    }

    /**
     * Update connection usage count
     *
     * @param connection Connection type
     */
    private void updateConnectionCount(String connection) {
        int count = connectionCounts.getOrDefault(connection, 0);
        connectionCounts.put(connection, count + 1);

        // Ban connection if it has been used 3 times
        if (count + 1 >= 3) {
            bannedConnections.put(connection, true);
        }
    }

    /**
     * Switch to the other player
     */
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }

    /**
     * Get the current game state
     *
     * @return Current state
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Get player 1
     *
     * @return Player 1
     */
    public main.model.Player getPlayer1() {
        return player1;
    }

    /**
     * Get player 2
     *
     * @return Player 2
     */
    public main.model.Player getPlayer2() {
        return player2;
    }

    /**
     * Get current player
     *
     * @return Current player
     */
    public main.model.Player getCurrentPlayer() {
        return currentPlayer;
    }

    /**
     * Get current movie
     *
     * @return Current movie
     */
    public Movie getCurrentMovie() {
        return currentMovie;
    }

    /**
     * Get round count
     *
     * @return Round count
     */
    public int getRoundCount() {
        return roundCount;
    }

    /**
     * Ban a connection
     *
     * @param connection Connection to ban
     */
    public void banConnection(String connection) {
        bannedConnections.put(connection, true);
    }

    /**
     * Check if a connection is banned
     *
     * @param connection Connection to check
     * @return true if banned, false otherwise
     */
    public boolean isConnectionBanned(String connection) {
        return bannedConnections.getOrDefault(connection, false);
    }

    /**
     * Get remaining uses for a connection
     *
     * @param connection Connection type
     * @return Remaining uses (0-3)
     */
    public int getRemainingConnectionUses(String connection) {
        int count = connectionCounts.getOrDefault(connection, 0);
        return Math.max(0, 3 - count);
    }
}