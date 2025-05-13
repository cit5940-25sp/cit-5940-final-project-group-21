package main.test;

import main.controller.GameController;
import main.model.GameState;
import main.model.Movie;
import main.model.MovieDatabase;
import main.model.Player;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Unit tests for the GameController class.
 * Validates game logic behaviors including player management, movie selection,
 * timer handling, and game state transitions.
 *
 * Uses test CSV files to simulate loading movie and credit data.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class GameControllerTest {

    private MovieDatabase movieDatabase;
    private GameController gameController;
    private Movie testMovie;
    private final String testMoviesCsv = "test_movies_gc.csv";
    private final String testCreditsCsv = "test_credits_gc.csv";

    @Before
    public void setUp() {
        movieDatabase = new MovieDatabase();
        gameController = new GameController(movieDatabase);
        testMovie = new Movie(1, "Test Movie", "2023",
                Arrays.asList("Action", "Comedy"), "Test overview", 7.5);

        // Set up test movie with crew
        testMovie.setActors(Arrays.asList("Actor1", "Actor2"));
        testMovie.setDirectors(Arrays.asList("Director1"));
        testMovie.setWriters(Arrays.asList("Writer1"));
        testMovie.setComposers(Arrays.asList("Composer1"));

        createTestCSVFiles();
    }

    private void createTestCSVFiles() {
        // Create a simple movies CSV file for testing
        try (FileWriter writer = new FileWriter(testMoviesCsv)) {
            // Header
            writer.write("budget,genres,homepage,id,keywords,original_language,original_title," +
                    "overview,popularity,production_companies,production_countries,release_date," +
                    "revenue,runtime,spoken_languages,status,tagline,title,vote_average," +
                    "vote_count\n");
            // Test movie 1
            writer.write("1000000,\"[{\"\"name\"\":\"\"Action\"\"}," +
                    "{\"\"name\"\":\"\"Adventure\"\"}]\"" +
                    ",http://test.com,1,\"\",en,Test Movie 1,\"Test overview 1\",10.0,\"\",\"\"" +
                    ",2023-01-01,2000000,120,\"\",Released,\"Test tagline\"," +
                    "Test Movie 1,7.5,100\n");
            // Test movie 2
            writer.write("2000000,\"[{\"\"name\"\":\"\"Comedy\"\"}," +
                    "{\"\"name\"\":\"\"Drama\"\"}]\",http://test2.com,2,\"\"" +
                    ",en,Test Movie 2,\"Test overview 2\",8.0,\"\",\"\"," +
                    "2023-02-01,3000000,110,\"\",Released,\"Test tagline 2\"" +
                    ",Test Movie 2,8.0,200\n");
        } catch (IOException e) {
            System.err.println("Failed to create test movies CSV: " + e.getMessage());
        }

        // Create a simple credits CSV file for testing
        try (FileWriter writer = new FileWriter(testCreditsCsv)) {
            // Header
            writer.write("movie_id,title,cast,crew\n");
            // Test movie 1 credits
            writer.write("1,Test Movie 1,\"[{\"\"name\"\":\"\"Actor1\"\"}," +
                    "{\"\"name\"\":\"\"Actor2\"\"}]\",\"[{\"\"name\"\":\"\"Director1\"\"" +
                    ",\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer1\"\",\"\"job\"\":" +
                    "\"\"Writer\"\"}]\"\n");
            // Test movie 2 credits
            writer.write("2,Test Movie 2,\"[{\"\"name\"\":\"\"Actor3\"\"}," +
                    "{\"\"name\"\":\"\"Actor4\"\"}]\",\"[{\"\"name\"\":\"\"Director2\"\"," +
                    "\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer2\"\",\"\"job\"\":" +
                    "\"\"Writer\"\"},{\"\"name\"\":\"\"Composer1\"\",\"\"job\"\":\"\"" +
                    "Original Music Composer\"\"}]\"\n");
        } catch (IOException e) {
            System.err.println("Failed to create test credits CSV: " + e.getMessage());
        }
    }

    @Test
    public void testGetGameState() {
        // Act
        GameState gameState = gameController.getGameState();

        // Assert
        assertNotNull(gameState);
        assertEquals(GameState.State.WAITING_FOR_PLAYERS, gameState.getCurrentState());
    }

    @Test
    public void testGetCurrentPlayer() {
        // Arrange - Set up players and game
        gameController.addPlayer("Player 1");
        gameController.addPlayer("Player 2");

        // Set conditions and manually change state to PLAYING
        Player player1 = gameController.getGameState().getPlayer1();
        Player player2 = gameController.getGameState().getPlayer2();
        player1.setConnectionType("genre");
        player1.setTargetCount(3);
        player2.setConnectionType("person");
        player2.setTargetCount(2);

        // Manually set state to PLAYING
        gameController.getGameState().setState(GameState.State.PLAYING);
        gameController.getGameState().setCurrentPlayer(player1);

        gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);
        gameController.getGameState().startGame(testMovie);
        gameController.getGameState().setState(GameState.State.PLAYING);

        // Act
        Player currentPlayer = gameController.getCurrentPlayer();

        // Assert
        assertNotNull(currentPlayer);
        assertEquals(player1, currentPlayer);
    }

    @Test
    public void testHandleTimeUp() {
        // Arrange
        gameController.addPlayer("Player 1");
        gameController.addPlayer("Player 2");

        Player player1 = gameController.getGameState().getPlayer1();
        Player player2 = gameController.getGameState().getPlayer2();

        // Set state to SETTING_WIN_CONDITIONS, then use startGame
        gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);
        gameController.getGameState().startGame(testMovie);

        // Initial state
        assertEquals(player1, gameController.getGameState().getCurrentPlayer());
        assertEquals(0, player1.getWinProgress());
        assertEquals(0, player2.getWinProgress());

        // Act
        gameController.handleTimeUp();

        // Assert
        assertEquals(GameState.State.GAME_OVER, gameController.getGameState().getCurrentState());
        assertEquals(0, player1.getWinProgress());
        assertTrue(player2.getWinProgress() > 0); // Player 2 should have gained a point
    }

    @Test
    public void testAddPlayerSuccess() {
        // Act
        boolean result = gameController.addPlayer("Player 1");

        // Assert
        assertTrue(result);
        assertEquals("Player 1", gameController.getGameState().getPlayer1().getName());
    }

    @Test
    public void testAddPlayerTransitionToSettingWinConditions() {
        // Arrange
        gameController.addPlayer("Player 1");

        // Initial state
        assertEquals(GameState.State.WAITING_FOR_PLAYERS,
                gameController.getGameState().getCurrentState());

        // Act
        boolean result = gameController.addPlayer("Player 2");

        // Assert
        assertTrue(result);
        assertEquals(GameState.State.SETTING_WIN_CONDITIONS,
                gameController.getGameState().getCurrentState());
    }

    @Test
    public void testGetPlayerConnectionType() {
        // Arrange
        gameController.addPlayer("Player 1");
        Player player1 = gameController.getGameState().getPlayer1();
        player1.setConnectionType("genre");

        // Act
        String connectionType = gameController.getPlayerConnectionType(player1);

        // Assert
        assertEquals("genre", connectionType);
    }

    @Test
    public void testGetAllGenres() {
        assertTrue(gameController.getAllGenres().isEmpty());
    }

    @Test
    public void testInitialize() {
        try {
            // Act
            gameController.initialize(testMoviesCsv, testCreditsCsv);

            // Assert
            List<Movie> allMovies = movieDatabase.getAllMovies();
            assertFalse("Movies should be loaded", allMovies.isEmpty());
            assertEquals("Should have loaded 2 test movies", 2, allMovies.size());

            // Check if the first movie has actors loaded
            Movie movie = allMovies.stream()
                    .filter(m -> m.getId() == 1)
                    .findFirst()
                    .orElse(null);

            assertNotNull(movie);
            assertFalse("Movie should have actors", movie.getActors().isEmpty());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRecentMovieHistory() {
        // Arrange
        gameController.addPlayer("Player 1");
        gameController.addPlayer("Player 2");

        // Set state to SETTING_WIN_CONDITIONS, then use startGame
        gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);
        gameController.getGameState().startGame(testMovie);

        // Act
        List<GameState.MovieConnection> history = gameController.getRecentMovieHistory(5);

        // Assert
        assertNotNull(history);
        assertEquals(1, history.size()); // Should contain only the starting movie
        assertEquals(testMovie, history.get(0).getMovie());
    }

    @Test
    public void testGetRandomMovie() {
        try {
            // Arrange
            gameController.initialize(testMoviesCsv, testCreditsCsv);

            // Act
            Movie randomMovie = gameController.getRandomMovie();

            // Assert
            assertNotNull("Should return a random movie", randomMovie);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSelectMovieByGenre() {
        try {
            // Arrange
            gameController.initialize(testMoviesCsv, testCreditsCsv);
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            player1.setConnectionType("genre");
            player1.setTargetCount(3);

            // Set state to SETTING_WIN_CONDITIONS, then use startGame
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // Get movie from database to use as starting movie
            Movie movie = movieDatabase.findMovieById(1);
            assertNotNull("Test movie should exist", movie);

            gameController.getGameState().startGame(movie);

            // Act
            boolean result = gameController.selectMovieByGenre("Test Movie 2");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testSelectMovieByPersonAutoDetect() {
        try {
            // Arrange
            gameController.initialize(testMoviesCsv,testCreditsCsv);
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            player1.setConnectionType("person");
            player1.setTargetCount(3);

            // Set state to SETTING_WIN_CONDITIONS, then use startGame
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // Get movie from database to use as starting movie
            Movie movie = movieDatabase.findMovieById(1); // This has Actor1
            assertNotNull("Test movie should exist", movie);

            gameController.getGameState().startGame(movie);

            // Act - person
            boolean result = gameController.selectMovieByPersonAutoDetect("Test Movie 2");

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}