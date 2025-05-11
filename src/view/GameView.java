package main.view;

import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import main.controller.GameController;

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
     * Display the main menu and handle user input
     */
    public void showMainMenu() {
        System.out.println("======================================");
        System.out.println("         MOVIE NAME GAME");
        System.out.println("======================================");
        System.out.println("1. Start New Game");
        System.out.println("2. Exit");
        System.out.print("Enter your choice: ");

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
    }

    /**
     * Start a new game by getting player information
     */
    private void startNewGame() {
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
     * @param genres Available genres (not used with new logic)
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
            // Genre-based connection
            gameController.setPlayerConnectionType(player, "genre");
        } else if (typeChoice == 2) {
            // Person-based connection
            gameController.setPlayerConnectionType(player, "person");
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

        // Display player information with connection method
        System.out.println("--- " + player1.getName() + " ---");
        System.out.println("Connection method: " + gameController.getPlayerConnectionType(player1));
        System.out.println("Movies selected: " + player1.getSelectedMovies().size());

        System.out.println("\n--- " + player2.getName() + " ---");
        System.out.println("Connection method: " + gameController.getPlayerConnectionType(player2));
        System.out.println("Movies selected: " + player2.getSelectedMovies().size());

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

        // Ensure timer starts on its own clean line
        System.out.println();

        // Get player input immediately
        handlePlayerTurn();
    }

    /**
     * Update the timer display
     *
     * @param secondsLeft Seconds left in the turn
     */
    public void updateTimer(int secondsLeft) {
        // Clear the line and print timer only
        System.out.print("\r                                        "); // Clear line
        System.out.print("\rTime left: " + secondsLeft + " seconds");
        System.out.flush();
    }

    /**
     * Show game over screen
     *
     * @param winner Winning player
     */
    public void showGameOver(Player winner) {
        System.out.println("\n======================================");
        System.out.println("            GAME OVER");
        System.out.println("======================================");
        System.out.println(winner.getName() + " wins!");

        System.out.println("\nPress Enter to return to main menu...");
        scanner.nextLine();

        showMainMenu();
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
     * Handle player turn input
     */
    public void handlePlayerTurn() {
        // Get the current player and their connection type
        Player currentPlayer = gameController.getCurrentPlayer();
        String connectionType = gameController.getPlayerConnectionType(currentPlayer);

        // Safety check for null connectionType
        if (connectionType == null) {
            showError("Error: Player connection type not set. Please restart the game.");
            return;
        }

        // Pause timer display
        gameController.pauseTimer();

        // Clear any existing timer line by moving to new line
        System.out.println();

        if (connectionType.equals("genre")) {
            // Genre-based connection
            System.out.println("You need to select a movie that shares a genre with the current movie.");

            System.out.println("Enter the movie title:");
            String movieTitle = scanner.nextLine();

            while (movieTitle.trim().isEmpty()) {
                System.out.println("Movie title cannot be empty. Please enter again:");
                movieTitle = scanner.nextLine();
            }

            // Resume timer display on a fresh line
            System.out.println();
            gameController.resumeTimer();

            // Call selectMovie for genre connection
            gameController.selectMovieByGenre(movieTitle);

        } else if (connectionType.equals("person")) {
            // Person-based connection
            System.out.println("You need to select a movie that shares a person with the current movie.");

            System.out.println("Enter the movie title:");
            String movieTitle = scanner.nextLine();

            while (movieTitle.trim().isEmpty()) {
                System.out.println("Movie title cannot be empty. Please enter again:");
                movieTitle = scanner.nextLine();
            }

            System.out.println("Enter connection type (actor/director/writer/composer):");
            String connectionPerson = scanner.nextLine();

            while (!isValidConnection(connectionPerson)) {
                System.out.println("Please enter a valid connection type (actor/director/writer/composer):");
                connectionPerson = scanner.nextLine();
            }

            System.out.println("Enter the name of the " + connectionPerson + " who appears in both movies:");
            String personName = scanner.nextLine();

            while (personName.trim().isEmpty()) {
                System.out.println("Person name cannot be empty. Please enter again:");
                personName = scanner.nextLine();
            }

            // Resume timer display on a fresh line
            System.out.println();
            gameController.resumeTimer();

            // Call selectMovie for person connection
            gameController.selectMovieByPerson(movieTitle, connectionPerson, personName);
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