package main.controller;

import com.opencsv.exceptions.CsvValidationException;
import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import main.model.MovieDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Controller class for the Movie Name Game.
 * Handles high-level game logic and exposes state for the UI layer.
 * Interacts with MovieDatabase and GameState.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class GameController {
    /**
     * Turn time limit in seconds for each player's move.
     */
    public static final int TURN_TIME_SECONDS = 30;

    private final GameState gameState;
    private final MovieDatabase movieDatabase;

    /**
     * Constructs a GameController instance with a given MovieDatabase.
     *
     * @param movieDatabase Movie database to be used in gameplay
     */
    public GameController(MovieDatabase movieDatabase) {
        this.movieDatabase = movieDatabase;
        this.gameState = new GameState();
    }

    /**
     * Loads movies and credits data from TMDB CSV files.
     *
     * @param movieFilePath   File path for the movie metadata
     * @param creditsFilePath File path for the movie credits
     */
    public void initialize(String movieFilePath, String creditsFilePath) {
        try {
            movieDatabase.loadMoviesFromCSV(movieFilePath);
            movieDatabase.loadCreditsFromCSV(creditsFilePath);
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the current game state object.
     *
     * @return GameState instance
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Returns the player whose turn it is.
     *
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    /**
     * Gets the connection type used by a given player.
     *
     * @param player Player to check
     * @return Connection type string ("genre" or "person")
     */
    public String getPlayerConnectionType(Player player) {
        return player.getConnectionType();
    }

    /**
     * Called when the timer expires during a player's turn.
     * Automatically sets the other player as the winner.
     */
    public void handleTimeUp() {
        Player current = gameState.getCurrentPlayer();
        Player other = (current == gameState.getPlayer1()) ?
                gameState.getPlayer2() : gameState.getPlayer1();

        // Give the other player an extra point to ensure a win
        other.incrementProgress();

        if (gameState.getCurrentState() != GameState.State.GAME_OVER) {
            gameState.setState(GameState.State.GAME_OVER);
        }
    }

    /**
     * Allows the current player to select a movie by genre.
     *
     * @param movieTitle Title of the selected movie
     * @return true if the move was valid and accepted
     */
    public boolean selectMovieByGenre(String movieTitle) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);
        Player current = gameState.getCurrentPlayer();

        if (!movies.isEmpty()) {
            boolean success = gameState.selectMovie(movies.get(0), "genre", null);
            if (success) {
                if (current.hasWon()) {
                    gameState.setState(GameState.State.GAME_OVER);
                } else {
                    gameState.switchToNextPlayer();
                }
            }
            return success;
        }
        return false;
    }

    /**
     * Allows the current player to select a movie with a person-based connection.
     * The system attempts to auto-detect the connection.
     *
     * @param movieTitle Title of the selected movie
     * @return true if the move was valid and accepted
     */
    public boolean selectMovieByPersonAutoDetect(String movieTitle) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);
        Player current = gameState.getCurrentPlayer();

        if (!movies.isEmpty()) {
            boolean success = gameState.selectMovie(movies.get(0), "person", null);
            if (success) {
                if (current.hasWon()) {
                    gameState.setState(GameState.State.GAME_OVER);
                } else {
                    gameState.switchToNextPlayer();
                }
            }
            return success;
        }
        return false;
    }

    /**
     * Returns all unique genres available in the movie database.
     *
     * @return Set of genre strings
     */
    public Set<String> getAllGenres() {
        return movieDatabase.getAllGenres();
    }

    /**
     * Selects and returns a random movie.
     *
     * @return A randomly selected movie
     */
    public Movie getRandomMovie() {
        return movieDatabase.getRandomMovie();
    }

    /**
     * Adds a player to the game.
     * Automatically advances to SETTING_WIN_CONDITIONS state after two players join.
     *
     * @param name Name of the new player
     * @return true if the player was added
     */
    public boolean addPlayer(String name) {
        boolean added = gameState.addPlayer(name);
        if (added && gameState.getPlayer2() != null) {
            gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);
        }
        return added;
    }

    /**
     * Retrieves the most recent movie connections played.
     *
     * @param limit Max number of entries to return
     * @return List of recent MovieConnection records
     */
    public List<GameState.MovieConnection> getRecentMovieHistory(int limit) {
        return gameState.getRecentMovieHistory(limit);
    }
}
