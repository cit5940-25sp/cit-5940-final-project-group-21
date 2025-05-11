package main.view;

import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import main.controller.GameController;
import main.controller.AutocompleteController;

import java.util.List;
import java.util.Set;
import java.util.Scanner;

/**
 * Text-based user interface for the Movie Name Game.
 * Displays game state and processes user input.
 */
public class GameView {
    private GameController gameController;
    private Scanner scanner;
    private AutocompleteController autocompleteController;
    private AutocompleteView autocompleteView;
    private volatile boolean gameInProgress = false;

    /**
     * Constructor for the GameView
     */
    public GameView() {
        scanner = new Scanner(System.in);
    }

    /**
     * Set the game controller
     *
     * @param gameController Game controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Set autocomplete components
     *
     * @param controller Autocomplete controller
     * @param view Autocomplete view
     */
    public void setAutocompleteComponents(AutocompleteController controller, AutocompleteView view) {
        this.autocompleteController = controller;
        this.autocompleteView = view;
    }

    /**
     * Display the main menu and handle user input
     */
    public void showMainMenu() {
        System.out.println("======================================");
        System.out.println("         MOVIE NAME GAME");
        System.out.println("======================================");
        System.out.println("1. Start New Game");
        System.out.println("2. Exit");
        System.out.print("Enter your choice: ");

        try {
            int choice = getIntInput();
            switch (choice) {
                case 1:
                    startNewGame();
                    break;
                case 2:
                    System.out.println("Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
                    showMainMenu();
                    break;
            }
        } catch (Exception e) {
            System.out.println("Error reading input. Please try again.");
            showMainMenu();
        }
    }

    /**
     * Start a new game by getting player information
     */
    private void startNewGame() {
        gameInProgress = true;
        System.out.println("\n--- Player 1 ---");
        System.out.print("Enter name: ");
        String player1Name = scanner.nextLine();

        gameController.addPlayer(player1Name);

        System.out.println("\n--- Player 2 ---");
        System.out.print("Enter name: ");
        String player2Name = scanner.nextLine();

        gameController.addPlayer(player2Name);
    }

    /**
     * Show win condition selection for a player
     *
     * @param player Player
     * @param genres Available genres
     */
    public void showWinConditionSelection(Player player, Set<String> genres) {
        System.out.println("\n--- Connection Method for " + player.getName() + " ---");
        System.out.println("How do you want to connect movies?");

        System.out.println("Select connection method:");
        System.out.println("1. Genre (connect by same genre)");
        System.out.println("2. Person (connect by shared actor/director/writer/composer)");
        System.out.print("Enter your choice (1 or 2): ");
        int typeChoice = getIntInput();

        if (typeChoice == 1) {
            // Ask for target count
            System.out.print("How many movies to win (1-5): ");
            int count = getIntInput();
            if (count < 1 || count > 5) {
                System.out.println("Invalid count. Please choose between 1 and 5.");
                showWinConditionSelection(player, genres);
                return;
            }
            gameController.setPlayerConnectionType(player, "genre", count);
        } else if (typeChoice == 2) {
            // Ask for target count
            System.out.print("How many movies to win (1-5): ");
            int count = getIntInput();
            if (count < 1 || count > 5) {
                System.out.println("Invalid count. Please choose between 1 and 5.");
                showWinConditionSelection(player, genres);
                return;
            }
            gameController.setPlayerConnectionType(player, "person", count);
        } else {
            System.out.println("Invalid input. Please try again.");
            showWinConditionSelection(player, genres);
        }
    }

    /**
     * Update the displayed game state
     *
     * @param gameState Current game state
     */
    public void updateGameState(GameState gameState) {
        System.out.println("\n======================================");
        System.out.println("            ROUND " + gameState.getRoundCount());
        System.out.println("======================================");

        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        // Display player information with progress
        System.out.println("--- " + player1.getName() + " ---");
        System.out.println("Connection method: " + gameController.getPlayerConnectionType(player1));
        System.out.println("Progress: (" + player1.getWinProgress() + "/" + player1.getTargetCount() + ")");

        System.out.println("\n--- " + player2.getName() + " ---");
        System.out.println("Connection method: " + gameController.getPlayerConnectionType(player2));
        System.out.println("Progress: (" + player2.getWinProgress() + "/" + player2.getTargetCount() + ")");

        // Display current movie
        Movie currentMovie = gameState.getCurrentMovie();
        System.out.println("\nCurrent movie: " + currentMovie.getTitle() +
                " (" + currentMovie.getReleaseYear() + ")");
        System.out.println("Genres: " + String.join(", ", currentMovie.getGenres()));

        // Show available connections
        System.out.println("\nAvailable connections:");
        System.out.println("Actors: " + (currentMovie.getActors().isEmpty() ? "None" : String.join(", ", currentMovie.getActors())));
        System.out.println("Directors: " + (currentMovie.getDirectors().isEmpty() ? "None" : String.join(", ", currentMovie.getDirectors())));
        System.out.println("Writers: " + (currentMovie.getWriters().isEmpty() ? "None" : String.join(", ", currentMovie.getWriters())));
        System.out.println("Composers: " + (currentMovie.getComposers().isEmpty() ? "None" : String.join(", ", currentMovie.getComposers())));

        // Display recent movies
        System.out.println("\nRecent movies:");
        List<Movie> player1Recent = player1.getRecentMovies(3);
        List<Movie> player2Recent = player2.getRecentMovies(3);

        for (Movie movie : player1Recent) {
            System.out.println("- " + player1.getName() + ": " + movie.getTitle() +
                    " (" + movie.getReleaseYear() + ")");
        }

        for (Movie movie : player2Recent) {
            System.out.println("- " + player2.getName() + ": " + movie.getTitle() +
                    " (" + movie.getReleaseYear() + ")");
        }

        // Display current player's turn
        Player currentPlayer = gameState.getCurrentPlayer();
        System.out.println("\n--- " + currentPlayer.getName() + "'s turn ---");
        System.out.println("Your connection method: " + gameController.getPlayerConnectionType(currentPlayer));
        System.out.println("Your progress: (" + currentPlayer.getWinProgress() + "/" + currentPlayer.getTargetCount() + ")");

        // Start the turn immediately
        handlePlayerTurn();
    }

    /**
     * Update the timer display
     *
     * @param secondsLeft Seconds left in the turn
     */
    public void updateTimer(int secondsLeft) {
        // Only update if timer is not paused
        if (!gameController.isTimerPaused()) {
            // Clear the line and print timer
            System.out.print("\r                                        "); // Clear line
            System.out.print("\rTime left: " + secondsLeft + " seconds");
            System.out.flush();
        }
    }

    /**
     * Show game over screen
     *
     * @param winner Winning player
     */
    public void showGameOver(Player winner) {
        gameInProgress = false;

        System.out.println("\n======================================");
        System.out.println("            GAME OVER");
        System.out.println("======================================");
        System.out.println(winner.getName() + " wins!");
        System.out.println("======================================");

        // 创建一个新的线程来等待任何键并返回主菜单
        new Thread(() -> {
            try {
                System.out.println("Press Enter to return to main menu...");
                System.in.read(); // 简单地等待任何输入
                System.out.println("\nReturning to main menu...");

                // 使用新的 Scanner 实例来避免线程问题
                scanner = new Scanner(System.in);
                showMainMenu();
            } catch (Exception e) {
                e.printStackTrace();
                // 如果出错，直接返回主菜单
                showMainMenu();
            }
        }).start();
    }

    /**
     * Update player information display
     *
     * @param player1 Player 1
     * @param player2 Player 2
     */
    public void updatePlayers(Player player1, Player player2) {
        System.out.println("\nPlayers:");
        if (player1 != null) {
            System.out.println("- Player 1: " + player1.getName());
        }
        if (player2 != null) {
            System.out.println("- Player 2: " + player2.getName());
        }
    }

    /**
     * Show an information message
     *
     * @param message Message to display
     */
    public void showMessage(String message) {
        System.out.println("\n" + message);
    }

    /**
     * Show an error message
     *
     * @param message Error message
     */
    public void showError(String message) {
        System.out.println("\nERROR: " + message);
    }

    /**
     * Get integer input from user
     *
     * @return User input as integer
     */
    private int getIntInput() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Get movie title with autocomplete
     *
     * @return Movie title entered by user
     */
    private String getMovieTitleWithAutocomplete() {
        System.out.println("\nEnter movie title (type and press Enter):");

        while (gameInProgress) {
            String input = scanner.nextLine().trim();

            if (!input.isEmpty()) {
                // Show suggestions based on input
                List<Movie> suggestions = autocompleteController.getSuggestions(input);

                if (!suggestions.isEmpty()) {
                    System.out.println("\nSuggestions:");
                    for (int i = 0; i < suggestions.size(); i++) {
                        Movie movie = suggestions.get(i);
                        System.out.println((i + 1) + ". " + movie.getTitle() + " (" + movie.getReleaseYear() + ")");
                    }

                    System.out.println("\nEnter selection number or type new search:");
                    String response = scanner.nextLine().trim();

                    try {
                        int selection = Integer.parseInt(response);
                        if (selection > 0 && selection <= suggestions.size()) {
                            return suggestions.get(selection - 1).getTitle();
                        }
                    } catch (NumberFormatException e) {
                        // If not a number, treat as new search term
                        if (!response.isEmpty()) {
                            input = response;
                            continue;
                        }
                    }
                }

                // If no suggestions or user provided exact title
                return input;
            }

            System.out.println("Please enter a movie title:");
        }

        return ""; // 游戏已结束
    }

    /**
     * Handle player turn input
     */
    public void handlePlayerTurn() {
        // Get the current player and their connection type
        Player currentPlayer = gameController.getCurrentPlayer();
        String connectionType = gameController.getPlayerConnectionType(currentPlayer);

        // Make sure timer starts for this turn
        gameController.startTurnTimer();

        // Pause timer immediately before getting input
        gameController.pauseTimer();

        // Clear line for clean input
        System.out.println();

        if (connectionType.equals("genre")) {
            // Genre-based connection
            System.out.println("You need to select a movie that shares a genre with the current movie.");

            String movieTitle = getMovieTitleWithAutocomplete();

            // Resume timer after getting input
            gameController.resumeTimer();

            // Call selectMovie for genre connection
            if (!movieTitle.isEmpty()) {
                gameController.selectMovieByGenre(movieTitle);
            }

        } else if (connectionType.equals("person")) {
            // Person-based connection - system auto-detects connection
            System.out.println("You need to select a movie that shares a person with the current movie.");
            System.out.println("(The system will auto-detect and display the connection type)");

            String movieTitle = getMovieTitleWithAutocomplete();

            // Resume timer after getting input
            gameController.resumeTimer();

            // Call selectMovie for auto-detected person connection
            if (!movieTitle.isEmpty()) {
                gameController.selectMovieByPersonAutoDetect(movieTitle);
            }
        }
    }

    /**
     * Validate if connection type is valid
     *
     * @param connection Connection type to validate
     * @return true if valid, false otherwise
     */
    private boolean isValidConnection(String connection) {
        return connection.equals("actor") || connection.equals("director") ||
                connection.equals("writer") || connection.equals("composer");
    }
}