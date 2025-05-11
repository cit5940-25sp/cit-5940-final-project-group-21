package main.controller;

import com.opencsv.exceptions.CsvValidationException;
import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import main.model.MovieDatabase;

import main.view.GameView;

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
    private boolean timerPaused = false;
    private boolean DEBUG_MODE = false; // 设置为false以关闭调试信息
    private boolean gameEnding = false; // 防止重复调用 endGame

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
     * @param movieFilePath,creditsFilePath Path to movie data file
     */
    public void initialize(String movieFilePath, String creditsFilePath) {
        try {
            movieDatabase.loadMoviesFromCSV(movieFilePath);
            movieDatabase.loadCreditsFromCSV(creditsFilePath);
            // 只有在非静默模式下才显示加载信息
            if (DEBUG_MODE) {
                gameView.showMessage("Loaded " + movieDatabase.getAllMovies().size() + " movies with credits");
            }

        } catch (IOException | CsvValidationException e) {
            gameView.showError("Error loading movie data. Continuing with available data...");
            if (DEBUG_MODE) {
                System.out.println("ERROR: Exception details:");
                e.printStackTrace();
            }
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

            // Only show connection selection for the FIRST player after second player joins
            if (gameState.getCurrentState() == GameState.State.SETTING_WIN_CONDITIONS &&
                    gameState.getPlayer2() != null &&
                    gameState.getPlayer1().getConnectionType() == null) {
                // Start with player 1
                gameView.showWinConditionSelection(gameState.getPlayer1(), getAllGenres());
            }
        }

        return added;
    }

    /**
     * Set a player's connection type
     *
     * @param player Player
     * @param connectionType Connection type ("genre" or "person")
     * @param targetCount Number of movies needed to win
     */
    public void setPlayerConnectionType(Player player, String connectionType, int targetCount) {
        player.setConnectionType(connectionType);
        player.setTargetCount(targetCount);

        // If player 1 just selected, show selection for player 2
        if (player == gameState.getPlayer1() && gameState.getPlayer2().getConnectionType() == null) {
            gameView.showWinConditionSelection(gameState.getPlayer2(), getAllGenres());
        }

        // Check if both players have set their connection types
        else if (gameState.getPlayer1().getConnectionType() != null &&
                gameState.getPlayer2().getConnectionType() != null) {

            if (DEBUG_MODE) {
                System.out.println("DEBUG: Both players have selected. Starting game...");
            }

            // Set the state to PLAYING before calling startGame()
            gameState.setState(GameState.State.PLAYING);
            gameState.setCurrentPlayer(gameState.getPlayer1()); // Make sure player 1 starts

            startGame();
        }
    }

    /**
     * Get a player's connection type
     *
     * @param player Player
     * @return Connection type ("genre" or "person")
     */
    public String getPlayerConnectionType(Player player) {
        return player.getConnectionType();
    }

    /**
     * Get the current player
     *
     * @return Current player
     */
    public Player getCurrentPlayer() {
        return gameState.getCurrentPlayer();
    }

    /**
     * Check if timer is paused
     *
     * @return true if timer is paused
     */
    public boolean isTimerPaused() {
        return timerPaused;
    }

    /**
     * Start the game
     */
    private void startGame() {
        if (DEBUG_MODE) {
            System.out.println("DEBUG: startGame() method called");
        }

        Movie startingMovie = movieDatabase.getRandomMovie();
        if (DEBUG_MODE && startingMovie != null) {
            System.out.println("DEBUG: Random movie selected: " + startingMovie.getTitle());
        }

        try {
            gameState.startGame(startingMovie);
            if (DEBUG_MODE) {
                System.out.println("DEBUG: gameState.startGame() completed");
            }
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.out.println("DEBUG: Error in gameState.startGame()");
                e.printStackTrace();
            }
            return;
        }

        try {
            gameView.updateGameState(gameState);
            if (DEBUG_MODE) {
                System.out.println("DEBUG: gameView.updateGameState() completed");
            }
        } catch (Exception e) {
            if (DEBUG_MODE) {
                System.out.println("DEBUG: Error in gameView.updateGameState()");
                e.printStackTrace();
            }
            return;
        }

        gameView.showMessage("Game started with movie: " + startingMovie.getTitle());

        if (DEBUG_MODE) {
            System.out.println("DEBUG: Game started successfully");
        }
    }

    /**
     * Handle movie selection by genre
     *
     * @param movieTitle Selected movie title
     */
    public void selectMovieByGenre(String movieTitle) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);

        // Cancel current timer
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        boolean success = false;
        String resultMessage = "";
        Player currentPlayer = gameState.getCurrentPlayer();

        if (movies.isEmpty()) {
            resultMessage = "Movie not found: " + movieTitle;
        } else {
            Movie selectedMovie = movies.get(0);
            Movie currentMovie = gameState.getCurrentMovie();

            // Check if the selected movie shares any genre with the current movie
            boolean hasSharedGenre = false;
            String sharedGenre = null;
            for (String genre : selectedMovie.getGenres()) {
                if (currentMovie.getGenres().contains(genre)) {
                    hasSharedGenre = true;
                    sharedGenre = genre;
                    break;
                }
            }

            if (!hasSharedGenre) {
                resultMessage = "Selected movie does not share any genre with the current movie.";
            } else {
                // Get progress before update
                int progressBefore = currentPlayer.getWinProgress();

                // Process valid move
                success = gameState.selectMovie(selectedMovie, "genre", sharedGenre);
                if (success) {
                    resultMessage = "Good! You found a " + sharedGenre + " movie.";

                    // Check if progress actually increased
                    int progressAfter = currentPlayer.getWinProgress();
                    if (progressAfter > progressBefore) {
                        resultMessage += " Your progress: (" + progressAfter + "/" + currentPlayer.getTargetCount() + ")";
                    }
                } else {
                    resultMessage = "Error processing the movie selection.";
                }
            }
        }

        // Display the result
        if (success) {
            gameView.showMessage(resultMessage);
        } else {
            gameView.showError(resultMessage);
        }

        // Always switch to next player regardless of success/failure
        gameState.switchToNextPlayer();

        // Check for win condition
        if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
            endGame();
        } else {
            // Update game state to show next player's turn
            gameView.updateGameState(gameState);
        }
    }

    /**
     * Handle movie selection with auto-detected person connection
     *
     * @param movieTitle Selected movie title
     */
    public void selectMovieByPersonAutoDetect(String movieTitle) {
        List<Movie> movies = movieDatabase.findMoviesByTitle(movieTitle);

        // Cancel current timer
        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        boolean success = false;
        String resultMessage = "";
        String connectionType = null;
        String personName = null;

        if (movies.isEmpty()) {
            resultMessage = "Movie not found: " + movieTitle;
        } else {
            Movie selectedMovie = movies.get(0);
            Movie currentMovie = gameState.getCurrentMovie();

            // Try to find a valid person connection
            List<String> availableConnections = gameState.getAvailableConnections(selectedMovie);
            if (!availableConnections.isEmpty()) {
                // Use the first available connection
                String connectionInfo = availableConnections.get(0);
                String[] parts = connectionInfo.split(":", 2);
                if (parts.length == 2) {
                    connectionType = parts[0];
                    personName = parts[1];

                    // Get current count for this connection
                    String connectionKey = connectionType + ":" + personName;
                    int currentCount = gameState.getPersonConnectionCount(connectionKey);

                    // Process the move
                    success = gameState.selectMovie(selectedMovie, connectionType, personName);
                    if (success) {
                        resultMessage = "Good! Connected via " + connectionType + ": " + personName +
                                " (" + (currentCount + 1) + "/3)";
                    } else {
                        resultMessage = "Error processing the movie selection.";
                    }
                }
            } else {
                resultMessage = "No valid person connection found between these movies.";
            }
        }

        // Display the result
        if (success) {
            gameView.showMessage(resultMessage);
        } else {
            gameView.showError(resultMessage);
        }

        // Always switch to next player regardless of success/failure
        gameState.switchToNextPlayer();

        // Check for win condition
        if (gameState.getCurrentState() == GameState.State.GAME_OVER) {
            endGame();
        } else {
            // Update game state to show next player's turn
            gameView.updateGameState(gameState);
        }
    }

    /**
     * Start the turn timer
     */
    public void startTurnTimer() {
        if (turnTimer != null) {
            turnTimer.cancel();
        }

        timerPaused = false; // Reset pause flag
        turnTimer = new Timer();
        turnTimer.schedule(new TimerTask() {
            private int secondsLeft = TURN_TIME_SECONDS;

            @Override
            public void run() {
                if (secondsLeft > 0) {
                    if (!timerPaused) { // Only update display when not paused
                        gameView.updateTimer(secondsLeft);
                    }
                    secondsLeft--;
                } else {
                    handleTimeUp();
                    cancel();
                }
            }
        }, 0, 1000);
    }

    /**
     * Handle time up event
     */
    private void handleTimeUp() {
        if (gameState.getCurrentState() == GameState.State.PLAYING && !gameEnding) {
            // Stop the timer first
            if (turnTimer != null) {
                turnTimer.cancel();
                turnTimer = null;
            }

            // Clear any remaining timer display
            System.out.println("\n"); // Move to a new line

            // Current player loses - switch to next player
            gameState.switchToNextPlayer();

            // Check if we have a winner (the other player)
            Player currentPlayer = gameState.getCurrentPlayer();
            Player winner = currentPlayer;

            gameView.showMessage("Time's up! " + winner.getName() + " wins!");

            // 设置游戏状态为结束
            gameState.setState(GameState.State.GAME_OVER);

            // 调用 endGame
            endGame();
        }
    }

    /**
     * End the game
     */
    private void endGame() {
        // 防止重复调用
        if (gameEnding) {
            return;
        }
        gameEnding = true;

        if (turnTimer != null) {
            turnTimer.cancel();
            turnTimer = null;
        }

        Player winner = null;
        Player currentPlayer = gameState.getCurrentPlayer();

        // Check who actually won
        if (gameState.getPlayer1().hasWon()) {
            winner = gameState.getPlayer1();
        } else if (gameState.getPlayer2().hasWon()) {
            winner = gameState.getPlayer2();
        } else {
            // If no one won by connection count, it's a timeout win
            winner = currentPlayer;
        }

        if (winner != null) {
            // 显示游戏结束画面
            gameView.showGameOver(winner);
        }

        // 重置标志
        gameEnding = false;
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

    /**
     * Pause timer
     */
    public void pauseTimer() {
        timerPaused = true;
    }

    /**
     * Resume timer
     */
    public void resumeTimer() {
        timerPaused = false;
    }
}