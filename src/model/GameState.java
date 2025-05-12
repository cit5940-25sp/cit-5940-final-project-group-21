package main.model;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents the current state of the Movie Name Game and manages all gameplay rules and flow.
 * Tracks players, current movie, round count, and verifies connections between movie selections.
 * Also maintains a history of movie plays and win conditions.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class GameState {

    /**
     * Defines the different phases the game can be in.
     */
    public enum State {
        WAITING_FOR_PLAYERS,
        SETTING_WIN_CONDITIONS,
        PLAYING,
        GAME_OVER
    }

    private State currentState;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Movie currentMovie;
    private int roundCount;
    private Map<String, Integer> personConnectionCounts;
    private boolean DEBUG_MODE = false;

    // Tracks history of all played movies and their connection details
    private List<MovieConnection> movieHistory;

    /**
     * Represents a record of a movie played and the reason it was valid (genre, actor, etc.).
     */
    public static class MovieConnection {
        private Movie movie;
        private String connectionType;
        private String connectionValue;

        /**
         * Constructs a MovieConnection record.
         *
         * @param movie           The movie that was selected.
         * @param connectionType  The type of connection used (e.g., "genre", "actor").
         * @param connectionValue The actual value of the connection (e.g., "Comedy", "Tom Hanks").
         */
        public MovieConnection(Movie movie, String connectionType, String connectionValue) {
            this.movie = movie;
            this.connectionType = connectionType;
            this.connectionValue = connectionValue;
        }

        public Movie getMovie() {
            return movie;
        }

        public String getConnectionType() {
            return connectionType;
        }

        public String getConnectionValue() {
            return connectionValue;
        }
    }

    /**
     * Constructs a new GameState in the initial WAITING_FOR_PLAYERS phase.
     */
    public GameState() {
        currentState = State.WAITING_FOR_PLAYERS;
        roundCount = 0;
        personConnectionCounts = new HashMap<>();
        movieHistory = new ArrayList<>();
    }

    /**
     * Adds a new player to the game. Only two players are allowed.
     *
     * @param name The name of the player to add.
     * @return true if the player was added successfully; false if both slots are filled.
     */
    public boolean addPlayer(String name) {
        if (player1 == null) {
            player1 = new Player(name);
            return true;
        } else {
            if (player2 == null) {
                player2 = new Player(name);
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the game with the given movie, resets progress, and enters PLAYING state.
     *
     * @param startingMovie The movie to begin the game with.
     */
    public void startGame(Movie startingMovie) {
        if (currentState != State.SETTING_WIN_CONDITIONS) {
            throw new IllegalStateException("Cannot start game, current state: " + currentState);
        }
        this.currentMovie = startingMovie;
        this.roundCount = 1;
        this.currentPlayer = player1;
        player1.reset();
        player2.reset();
        personConnectionCounts.clear();
        movieHistory.clear();
        movieHistory.add(new MovieConnection(startingMovie, null, null));
        this.currentState = State.PLAYING;
    }

    /**
     * Applies a movie selection by the current player and checks for a valid connection.
     *
     * @param movie           The movie selected.
     * @param connectionType  Type of connection ("genre" or "person").
     * @param connectionValue Specific value of the connection (genre or name).
     * @return true if the move was valid and applied, false otherwise.
     */
    public boolean selectMovie(Movie movie, String connectionType, String connectionValue) {
        if (currentState != State.PLAYING) {
            return false;
        }

        if ((player1.getSelectedMovies().contains(movie)) || (player2.getSelectedMovies().contains(movie))) {
            return false;
        }

        boolean validConnection = false;
        boolean shouldIncrement = false;
        String actualConnectionValue = connectionValue;

        // Handle genre-based connection
        if ("genre".equals(connectionType)) {
            List<String> commonGenres = new ArrayList<>(currentMovie.getGenres());
            commonGenres.retainAll(movie.getGenres());

            if (!commonGenres.isEmpty()) {
                validConnection = true;
                shouldIncrement = true;
                actualConnectionValue = commonGenres.get(0);
            }

        } else if ("person".equals(connectionType)) {
            if (connectionValue == null) {
                // Try to infer person connection if value not given
                List<String> actorsA = currentMovie.getActors();
                List<String> actorsB = movie.getActors();
                for (String actor : actorsA) {
                    if (actorsB.contains(actor)) {
                        actualConnectionValue = actor;
                        connectionType = "actor";
                        validConnection = true;
                        break;
                    }
                }
                if (!validConnection) {
                    List<String> directorsA = currentMovie.getDirectors();
                    List<String> directorsB = movie.getDirectors();
                    for (String director : directorsA) {
                        if (directorsB.contains(director)) {
                            actualConnectionValue = director;
                            connectionType = "director";
                            validConnection = true;
                            break;
                        }
                    }
                }
                if (!validConnection) {
                    List<String> writersA = currentMovie.getWriters();
                    List<String> writersB = movie.getWriters();
                    for (String writer : writersA) {
                        if (writersB.contains(writer)) {
                            actualConnectionValue = writer;
                            connectionType = "writer";
                            validConnection = true;
                            break;
                        }
                    }
                }
                if (!validConnection) {
                    List<String> composersA = currentMovie.getComposers();
                    List<String> composersB = movie.getComposers();
                    for (String composer : composersA) {
                        if (composersB.contains(composer)) {
                            actualConnectionValue = composer;
                            connectionType = "composer";
                            validConnection = true;
                            break;
                        }
                    }
                }
            } else {
                validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);
            }

            if (validConnection) {
                String key = connectionType + ":" + actualConnectionValue;
                int used = personConnectionCounts.getOrDefault(key, 0);
                if (used < 3) {
                    personConnectionCounts.put(key, used + 1);
                    shouldIncrement = true;
                } else {
                    validConnection = false;
                }
            }

        } else {
            validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);
            if (validConnection) {
                String key = connectionType + ":" + connectionValue;
                int used = personConnectionCounts.getOrDefault(key, 0);
                if (used < 3) {
                    personConnectionCounts.put(key, used + 1);
                    shouldIncrement = true;
                } else {
                    validConnection = false;
                }
            }
        }

        if (!validConnection) {
            return false;
        }

        // Apply the move
        currentPlayer.addSelectedMovie(movie);
        if (shouldIncrement) {
            currentPlayer.incrementProgress();
        }
        movieHistory.add(new MovieConnection(movie, connectionType, actualConnectionValue));
        currentMovie = movie;

        if (currentPlayer.hasWon()) {
            currentState = State.GAME_OVER;
        } else {
            roundCount++;
        }

        return true;
    }

    /**
     * Verifies a connection between two movies for a specific person role.
     *
     * @param from           Starting movie.
     * @param to             Target movie.
     * @param connectionType One of "actor", "director", "writer", "composer".
     * @param personName     The name of the person to check.
     * @return true if the connection is valid; false otherwise.
     */
    public boolean verifyConnection(Movie from, Movie to, String connectionType, String personName) {
        List<String> fromList;
        List<String> toList;

        switch (connectionType) {
            case "actor": {
                fromList = from.getActors();
                toList = to.getActors();
                break;
            }
            case "director": {
                fromList = from.getDirectors();
                toList = to.getDirectors();
                break;
            }
            case "writer": {
                fromList = from.getWriters();
                toList = to.getWriters();
                break;
            }
            case "composer": {
                fromList = from.getComposers();
                toList = to.getComposers();
                break;
            }
            default: {
                return false;
            }
        }
        return fromList.contains(personName) && toList.contains(personName);
    }

    /**
     * Returns a sublist of the most recent movies played in the game.
     *
     * @param limit Maximum number of recent movies to return.
     * @return A list of MovieConnection objects representing recent history.
     */
    public List<MovieConnection> getRecentMovieHistory(int limit) {
        int size = movieHistory.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(movieHistory.subList(start, size));
    }

    // ------------------------- Accessors and Utility -------------------------

    public State getCurrentState() { return currentState; }

    public Player getPlayer1() { return player1; }

    public Player getPlayer2() { return player2; }

    public Player getCurrentPlayer() { return currentPlayer; }

    public Movie getCurrentMovie() { return currentMovie; }

    public int getRoundCount() { return roundCount; }

    public void setState(State state) { this.currentState = state; }

    public void setCurrentPlayer(Player p) { this.currentPlayer = p; }

    /**
     * Switches the active player to the other one.
     */
    public void switchToNextPlayer() {
        if (currentPlayer == player1) {
            currentPlayer = player2;
        } else {
            currentPlayer = player1;
        }
    }
}
