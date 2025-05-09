package view;

import model.GameState;
import model.Movie;
import model.Player;
import controller.GameController;

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
     * @param genres Available genres
     */
    public void showWinConditionSelection(Player player, Set<String> genres) {
        System.out.println("\n--- Win Condition for " + player.getName() + " ---");

        // Display available genres
        System.out.println("Available genres:");
        int i = 1;
        String[] genreArray = genres.toArray(new String[0]);
        for (String genre : genreArray) {
            System.out.println(i + ". " + genre);
            i++;
        }

        System.out.print("Select genre (1-" + genres.size() + "): ");
        int genreChoice = getIntInput();
        if (genreChoice < 1 || genreChoice > genres.size()) {
            System.out.println("Invalid choice. Please try again.");
            showWinConditionSelection(player, genres);
            return;
        }

        String selectedGenre = genreArray[genreChoice - 1];

        System.out.print("How many " + selectedGenre + " movies to win (1-5): ");
        int count = getIntInput();
        if (count < 1 || count > 5) {
            System.out.println("Invalid count. Please choose between 1 and 5.");
            showWinConditionSelection(player, genres);
            return;
        }

        gameController.setPlayerWinCondition(player, selectedGenre, count);
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

        // Display player information
        System.out.println("--- " + player1.getName() + " ---");
        System.out.println("Win condition: " + player1.getWinCondition().getDescription());
        System.out.println("Progress: " + player1.getWinProgress() + "/" +
                player1.getWinCondition().getRequiredCount());

        System.out.println("\n--- " + player2.getName() + " ---");
        System.out.println("Win condition: " + player2.getWinCondition().getDescription());
        System.out.println("Progress: " + player2.getWinProgress() + "/" +
                player2.getWinCondition().getRequiredCount());

        // Display current movie
        Movie currentMovie = gameState.getCurrentMovie();
        System.out.println("\nCurrent movie: " + currentMovie.getTitle() +
                " (" + currentMovie.getReleaseYear() + ")");
        System.out.println("Genres: " + String.join(", ", currentMovie.getGenres()));

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
    }

    /**
     * Update the timer display
     *
     * @param secondsLeft Seconds left in the turn
     */
    public void updateTimer(int secondsLeft) {
        System.out.print("\rTime left: " + secondsLeft + " seconds    ");
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
}