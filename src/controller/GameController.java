package controller;

import model.GameState;
import model.Movie;
import model.MovieDatabase;
import model.Player;
import view.GameView;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Controller class for the Movie Name Game.
 * Handles game logic and user interactions.
 */
public class GameController {
    private GameState gameState;
    private MovieDatabase movieDatabase;
    private GameView gameView;
    private Timer turnTimer;
    private static final int TURN_TIME_SECONDS = 30;

    /**
     * Constructor for the GameController
     *
     * @param movieDatabase Movie database
     */
    public GameController(MovieDatabase movieDatabase) {
        this.movieDatabase = movieDatabase;
        this.gameState = new GameState();
    }

    /**
     * Set the game view
     *
     * @param gameView Game view
     */
    public void setGameView(GameView gameView) {
        this.gameView = gameView;
    }

    /**
     * Initialize the game
     *
     * @param dataFilePath Path to movie data file
     */
    public void initialize(String dataFilePath) {
        try {
            movieDatabase.loadMoviesFromCSV(dataFilePath);
            gameView.showMessage("Loaded " + movieDatabase.getAllMovies().size() + " movies");
        } catch (IOException e) {
            gameView.showError("Error loading movie data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Add a player to the game
     *
     * @param name Player name
     * @return true if player added successfully, false otherwise
     */
    public boolean addPlayer(String name) {
        boolean added = gameState.addPlayer(name);

        if (added) {
            gameView.updatePlayers(gameState.getPlayer1(), gameState.getPlayer2());

            if (gameState.getCurrentState() == GameState.State.SETTING_WIN_CONDITIONS) {
                gameView.showWinConditionSelection(gameState.getPlayer1(), getAllGenres());
                gameView.showWinConditionSelection(gameState.getPlayer2(), getAllGenres());
            }
        }

        return added;
    }

    /**
     * Set a player's win condition
     *
     * @param player Player
     * @param genre Genre for win condition
     * @param count Number of movies required
     */
    public void setPlayerWinCondition(Player player, String genre, int count) {
        Player.WinCondition winCondition = new Player.GenreWinCondition(genre, count);
        gameState.setPlayerWinCondition(player, winCondition);

        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            startGame();
        }
    }

    /**
     * Start the game
     */
    private void startGame() {
        Movie startingMovie = movieDatabase.getRandomMovie();
        gameState.startGame(startingMovie);

        gameView.updateGameState(gameState);
        gameView.showMessage("Game started with movie: " + startingMovie.getTitle());

        startTurnTimer();
    }

    /**
     * Handle movie selection
     *
     * @param movieTitle Selected movie title
     * @param connection Connection type (actor, director, etc.)
     */
    public void selectMovie(String movieTitle, String connection) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);

        if (movies.isEmpty()) {
            gameView.showError("Movie not found: " + movieTitle);
            return;
        }

        Movie selectedMovie = movies.get(0);
        boolean success = gameState.selectMovie(selectedMovie, connection);

        if (success) {
            gameView.updateGameState(gameState);

            if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
                endGame();
            } else {
                resetTurnTimer();
            }
        } else {
            gameView.showError("Invalid selection. Please try again.");
        }
    }

    /**
     * Start the turn timer
     */
    private void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }

        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            private int secondsLeft = TURN_TIME_SECONDS;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    gameView.updateTimer(secondsLeft);
                    secondsLeft--;
                } else {
                    handleTimeUp();
                    cancel();
                }
            }
        }, 0, 1000); // Update every second
    }

    /**
     * Reset the turn timer
     */
    private void resetTurnTimer() {
        startTurnTimer();
    }

    /**
     * Handle time up event
     */
    private void handleTimeUp() {
        if (gameState.getCurrentState() == GameState.State.PLAYING) {
            Player winner = (gameState.getCurrentPlayer() == gameState.getPlayer1())
                    ? gameState.getPlayer2() : gameState.getPlayer1();

            gameView.showMessage("Time's up! " + winner.getName() + " wins!");
            endGame();
        }
    }

    /**
     * End the game
     */
    private void endGame() {
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        Player winner = null;
        if (gameState.getPlayer1().hasWon()) {
            winner = gameState.getPlayer1();
        } else if (gameState.getPlayer2().hasWon()) {
            winner = gameState.getPlayer2();
        } else {
            winner = (gameState.getCurrentPlayer() == gameState.getPlayer1())
                    ? gameState.getPlayer2() : gameState.getPlayer1();
        }

        gameView.showGameOver(winner);
    }

    /**
     * Get all genres from the movie database
     *
     * @return Set of all genres
     */
    public Set<String> getAllGenres() {
        return movieDatabase.getAllGenres();
    }

    /**
     * Get movie suggestions based on title prefix
     *
     * @param prefix Title prefix
     * @param maxResults Maximum number of results
     * @return List of matching movies
     */
    public List<Movie> getMovieSuggestions(String prefix, int maxResults) {
        return movieDatabase.findMoviesByTitlePrefix(prefix, maxResults);
    }
}
