package main.model;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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
    private Map<String, Integer> personConnectionCounts; // Tracks specific person connections (e.g., "actor:Tom Hanks")

    /**
     * Constructor
     */
    public GameState() {
        currentState = State.WAITING_FOR_PLAYERS;
        connectionCounts = new HashMap<>();
        bannedConnections = new HashMap<>();
        roundCount = 0;
        personConnectionCounts = new HashMap<>();
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
        currentPlayer = player1; // Player 1 goes first

        // Clear previous game data
        player1.reset();
        player2.reset();
        connectionCounts.clear();
        bannedConnections.clear();
        personConnectionCounts.clear();
    }

    /**
     * Player selects the next movie
     *
     * @param movie Selected movie
     * @param connection Connection type (actor, director, etc.)
     * @return true if selection is valid, false otherwise
     */
    public boolean selectMovie(Movie movie, String connection) {
        // This is the old method for backward compatibility
        // For the new logic, we use selectMovie with different parameters
        return selectMovie(movie, connection, null);
    }

    /**
     * Player selects the next movie with specific connection
     *
     * @param movie Selected movie
     * @param connectionType Connection type ("genre", "actor", "director", etc.)
     * @param connectionValue For person: person name; for genre: genre name
     * @return true if selection is valid, false otherwise
     */
    public boolean selectMovie(Movie movie, String connectionType, String connectionValue) {
        System.out.println("\n--- GameState.selectMovie DEBUG START ---");
        System.out.println("Current state: " + currentState);
        System.out.println("Current player: " + currentPlayer.getName());
        System.out.println("Connection type: " + connectionType);
        System.out.println("Connection value: " + connectionValue);

        if (currentState != State.PLAYING) {
            System.out.println("ERROR: Game not in PLAYING state");
            System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            return false;
        }

        // Check if movie has already been used
        if (player1.getSelectedMovies().contains(movie) ||
                player2.getSelectedMovies().contains(movie)) {
            System.out.println("ERROR: Movie already used");
            System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            return false;
        }

        boolean validConnection = false;

        // Check connection based on type
        if (connectionType.equals("genre")) {
            // Genre connection
            validConnection = currentMovie.getGenres().contains(connectionValue) &&
                    movie.getGenres().contains(connectionValue);
        } else {
            // Person connection (actor, director, writer, composer)
            validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);

            if (validConnection) {
                // Check if person connection has been used too many times
                String personKey = connectionType + ":" + connectionValue;
                int personCount = personConnectionCounts.getOrDefault(personKey, 0);
                if (personCount >= 3) {
                    System.out.println("ERROR: Person connection used too many times");
                    validConnection = false;
                } else {
                    // Update person connection count
                    personConnectionCounts.put(personKey, personCount + 1);
                }
            }
        }

        if (!validConnection) {
            System.out.println("ERROR: Invalid connection");
            System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            return false;
        }

        // Record selection
        currentPlayer.addSelectedMovie(movie);
        currentMovie = movie;

        // Check win condition
        if (currentPlayer.hasWon()) {
            System.out.println("Player " + currentPlayer.getName() + " has won!");
            currentState = State.GAME_OVER;
            System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            return true;
        }

        // Switch players
        System.out.println("Before switchPlayer: " + currentPlayer.getName());
        switchPlayer();
        System.out.println("After switchPlayer: " + currentPlayer.getName());
        roundCount++;

        System.out.println("--- GameState.selectMovie DEBUG END ---\n");
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
     * Verify if a specific person connection exists between two movies
     *
     * @param from Source movie
     * @param to Target movie
     * @param connectionType Connection type (actor, director, etc.)
     * @param personName Name of the person
     * @return true if valid connection exists
     */
    public boolean verifyConnection(Movie from, Movie to, String connectionType, String personName) {
        List<String> toPeople = new ArrayList<>();
        List<String> fromPeople = new ArrayList<>();

        switch (connectionType) {
            case "actor":
                toPeople = to.getActors();
                fromPeople = from.getActors();
                break;
            case "director":
                toPeople = to.getDirectors();
                fromPeople = from.getDirectors();
                break;
            case "writer":
                toPeople = to.getWriters();
                fromPeople = from.getWriters();
                break;
            case "composer":
                toPeople = to.getComposers();
                fromPeople = from.getComposers();
                break;
            default:
                return false;
        }

        // Check if the person exists in both movies
        return toPeople.contains(personName) && fromPeople.contains(personName);
    }

    /**
     * Get available connections between current movie and target movie
     *
     * @param targetMovie Target movie to connect to
     * @return List of available connections in format "connectionType:personName"
     */
    public List<String> getAvailableConnections(Movie targetMovie) {
        List<String> availableConnections = new ArrayList<>();
        Movie from = currentMovie;

        // Check shared actors
        for (String actor : targetMovie.getActors()) {
            if (from.getActors().contains(actor)) {
                String personKey = "actor:" + actor;
                int count = personConnectionCounts.getOrDefault(personKey, 0);
                if (count < 3) {
                    availableConnections.add(personKey);
                }
            }
        }

        // Check shared directors
        for (String director : targetMovie.getDirectors()) {
            if (from.getDirectors().contains(director)) {
                String personKey = "director:" + director;
                int count = personConnectionCounts.getOrDefault(personKey, 0);
                if (count < 3) {
                    availableConnections.add(personKey);
                }
            }
        }

        // Check shared writers
        for (String writer : targetMovie.getWriters()) {
            if (from.getWriters().contains(writer)) {
                String personKey = "writer:" + writer;
                int count = personConnectionCounts.getOrDefault(personKey, 0);
                if (count < 3) {
                    availableConnections.add(personKey);
                }
            }
        }

        // Check shared composers
        for (String composer : targetMovie.getComposers()) {
            if (from.getComposers().contains(composer)) {
                String personKey = "composer:" + composer;
                int count = personConnectionCounts.getOrDefault(personKey, 0);
                if (count < 3) {
                    availableConnections.add(personKey);
                }
            }
        }

        return availableConnections;
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

    /**
     * Set the current game state
     *
     * @param state New state
     */
    public void setState(GameState.State state) {
        this.currentState = state;
    }

    /**
     * Set the current player
     *
     * @param player Player to set as current
     */
    public void setCurrentPlayer(main.model.Player player) {
        this.currentPlayer = player;
    }
}