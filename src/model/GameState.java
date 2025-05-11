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
    private Map<String, Integer> personConnectionCounts; // Tracks specific person connections (e.g., "actor:Tom Hanks")
    private boolean DEBUG_MODE = false; // 设置为false以关闭调试信息

    /**
     * Constructor
     */
    public GameState() {
        currentState = State.WAITING_FOR_PLAYERS;
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

        // Reset game data but keep connection types and target counts
        player1.reset();
        player2.reset();
        personConnectionCounts.clear();
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
        if (DEBUG_MODE) {
            System.out.println("\n--- GameState.selectMovie DEBUG START ---");
            System.out.println("Current state: " + currentState);
            System.out.println("Current player: " + currentPlayer.getName());
            System.out.println("Connection type: " + connectionType);
            System.out.println("Connection value: " + connectionValue);
        }

        if (currentState != State.PLAYING) {
            if (DEBUG_MODE) {
                System.out.println("ERROR: Game not in PLAYING state");
                System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            }
            return false;
        }

        // Check if movie has already been used
        if (player1.getSelectedMovies().contains(movie) ||
                player2.getSelectedMovies().contains(movie)) {
            if (DEBUG_MODE) {
                System.out.println("ERROR: Movie already used");
                System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            }
            return false;
        }

        boolean validConnection = false;
        boolean isPlayerConnectionMatch = false;

        // Check connection based on type
        if (connectionType.equals("genre")) {
            // Genre connection
            validConnection = currentMovie.getGenres().contains(connectionValue) &&
                    movie.getGenres().contains(connectionValue);
            // For genre connections, always increment progress if connection is valid
            isPlayerConnectionMatch = validConnection;
        } else {
            // Person connection (actor, director, writer, composer)
            validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);

            if (validConnection) {
                // Check if person connection has been used too many times
                String personKey = connectionType + ":" + connectionValue;
                int personCount = personConnectionCounts.getOrDefault(personKey, 0);
                if (personCount >= 3) {
                    if (DEBUG_MODE) {
                        System.out.println("ERROR: Person connection used too many times");
                    }
                    validConnection = false;
                } else {
                    // Update person connection count
                    personConnectionCounts.put(personKey, personCount + 1);
                    // For person connections, always increment progress if connection is valid
                    isPlayerConnectionMatch = validConnection;
                }
            }
        }

        if (!validConnection) {
            if (DEBUG_MODE) {
                System.out.println("ERROR: Invalid connection");
                System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            }
            return false;
        }

        // Record selection
        currentPlayer.addSelectedMovie(movie);
        if (isPlayerConnectionMatch) {
            currentPlayer.incrementProgress();
        }
        currentMovie = movie;

        // Check win condition
        if (currentPlayer.hasWon()) {
            if (DEBUG_MODE) {
                System.out.println("Player " + currentPlayer.getName() + " has won!");
            }
            currentState = State.GAME_OVER;
            if (DEBUG_MODE) {
                System.out.println("--- GameState.selectMovie DEBUG END ---\n");
            }
            return true;
        }

        roundCount++;

        if (DEBUG_MODE) {
            System.out.println("--- GameState.selectMovie DEBUG END ---\n");
        }
        return true;
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
     * Get the count for a specific person connection
     *
     * @param connectionKey The connection key (e.g., "actor:Tom Hanks")
     * @return The current count for this connection
     */
    public int getPersonConnectionCount(String connectionKey) {
        return personConnectionCounts.getOrDefault(connectionKey, 0);
    }

    /**
     * Switch to the next player (public method for controller access)
     */
    public void switchToNextPlayer() {
        switchPlayer();
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
     * Set the current game state
     *
     * @param state New state
     */
    public void setState(State state) {
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