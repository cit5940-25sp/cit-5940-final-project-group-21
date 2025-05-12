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
 * Handles game logic and exposes state for the Terminal-based UI.
 */
public class GameController {
    /**
     * Turn time (seconds) for each player; used by TerminalGameUI
     */
    public static final int TURN_TIME_SECONDS = 30;

    private final GameState gameState;
    private final MovieDatabase movieDatabase;

    /**
     * Constructor for the GameController.
     *
     * @param movieDatabase Movie database
     */
    public GameController(MovieDatabase movieDatabase) {
        this.movieDatabase = movieDatabase;
        this.gameState = new GameState();
    }

    /**
     * Load movies and credits data.
     *
     * @param movieFilePath   Path to movie CSV file
     * @param creditsFilePath Path to credits CSV file
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
     * Get the current game state.
     *
     * @return GameState instance
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Get the current player whose turn it is.
     *
     * @return Current Player
     */
    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    /**
     * Get a player's connection type: "genre" or "person".
     *
     * @param player Player instance
     * @return Connection type string
     */
    public String getPlayerConnectionType(Player player) {
        return player.getConnectionType();
    }

    /**
     * Handle the event where time has run out for the current turn.
     * Sets the other player as the winner and ends the game.
     */
    /**
     * Handle the event where time has run out for the current turn.
     * Sets the other player as the winner and ends the game.
     */
    public void handleTimeUp() {
        // 将当前玩家设为输家，另一个玩家设为赢家
        Player current = gameState.getCurrentPlayer();
        Player other = (current == gameState.getPlayer1()) ?
                gameState.getPlayer2() : gameState.getPlayer1();

        // 强制设置另一个玩家为胜利者
        other.incrementProgress(); // 给另一个玩家增加一点分数以确保赢

        // If game isn't over already, set it to game over state
        if (gameState.getCurrentState() != GameState.State.GAME_OVER) {
            gameState.setState(GameState.State.GAME_OVER);
        }
    }

    /**
     * Process movie selection by genre.
     *
     * @param movieTitle Selected movie title
     * @return true if movie was successfully selected, false otherwise
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
     * Process movie selection by person connection (auto-detected).
     *
     * @param movieTitle Selected movie title
     * @return true if movie was successfully selected, false otherwise
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
     * Get all available genres.
     *
     * @return Set of genre strings
     */
    public Set<String> getAllGenres() {
        return movieDatabase.getAllGenres();
    }

    /**
     * Get a random movie from the database.
     *
     * @return Random movie instance
     */
    public Movie getRandomMovie() {
        return movieDatabase.getRandomMovie();
    }

    public boolean addPlayer(String name) {
        boolean added = gameState.addPlayer(name);
        // 如果刚加入了第二个玩家，就切换到“设置胜利条件”阶段
        if (added && gameState.getPlayer2() != null) {
            gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);
        }
        return added;

    }

    /**
     * Get recent movie history.
     *
     * @param limit Maximum number of movie connections to return
     * @return List of movie connections
     */
    public List<GameState.MovieConnection> getRecentMovieHistory(int limit) {
        return gameState.getRecentMovieHistory(limit);
    }

}