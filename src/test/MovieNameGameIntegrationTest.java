package main.test;

import main.controller.AutocompleteController;
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
 * Integration tests for the full Movie Name Game flow.
 * Validates the interaction between GameController, MovieDatabase,
 * and AutocompleteController using simulated movie and credit datasets.
 *
 * Covers game state transitions, win conditions, invalid moves,
 * autocomplete functionality, and timer expiration logic.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class MovieNameGameIntegrationTest {

    private MovieDatabase movieDatabase;
    private GameController gameController;
    private AutocompleteController autocompleteController;
    private final String testMoviesIntCsv = "test_movies_int.csv";
    private final String testCreditsIntCsv = "test_credits_int.csv";

    @Before
    public void setUp() {
        movieDatabase = new MovieDatabase();
        gameController = new GameController(movieDatabase);
        autocompleteController = new AutocompleteController(movieDatabase);
        createTestCSVFiles();
    }

    private void createTestCSVFiles() {
        // Create a simple movies CSV file for testing
        try (FileWriter writer = new FileWriter(testMoviesIntCsv)) {
            // Header
            writer.write("budget,genres,homepage,id,keywords,original_language," +
                    "original_title,overview,popularity,production_companies" +
                    ",production_countries,release_date,revenue,runtime,spoken_" +
                    "languages,status,tagline,title,vote_average,vote_count\n");
            // Movie 1
            writer.write("1000000,\"[{\"\"name\"\":\"\"Action\"\"}," +
                    "{\"\"name\"\":\"\"Adventure\"\"}]\",http://test.com,1,\"" +
                    "\",en,Test Movie 1,\"Test overview 1\",10.0,\"\",\"\"," +
                    "2023-01-01,2000000,120,\"\",Released,\"Test tagline\"," +
                    "Test Movie 1,7.5,100\n");
            // Movie 2
            writer.write("2000000,\"[{\"\"name\"\":\"\"Action\"\"}," +
                    "{\"\"name\"\":\"\"Drama\"\"}]\",http://test2.com,2,\"\",en," +
                    "Test Movie 2,\"Test overview 2\",8.0,\"\",\"\",2023-02-01,3000000,110," +
                    "\"\",Released,\"Test tagline 2\",Test Movie 2,8.0,200\n");
            // Movie 3
            writer.write("3000000,\"[{\"\"name\"\":\"\"Comedy\"\"}," +
                    "{\"\"name\"\":\"\"Drama\"\"}]\",http://test3.com,3,\"\",en," +
                    "Test Movie 3,\"Test overview 3\",9.0,\"\",\"\",2023-03-01,4000000,130," +
                    "\"\",Released,\"Test tagline 3\",Test Movie 3,8.5,300\n");
        } catch (IOException e) {
            System.err.println("Failed to create test movies CSV: " + e.getMessage());
        }

        // Create a simple credits CSV file for testing
        try (FileWriter writer = new FileWriter(testCreditsIntCsv)) {
            // Header
            writer.write("movie_id,title,cast,crew\n");
            // Movie 1 credits
            writer.write("1,Test Movie 1,\"[{\"\"name\"\":\"\"Actor1\"\"}," +
                    "{\"\"name\"\":\"\"Actor2\"\"}]\",\"[{\"\"name\"\":\"\"Director1\"\"" +
                    ",\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer1\"\"," +
                    "\"\"job\"\":\"\"Writer\"\"}]\"\n");
            // Movie 2 credits
            writer.write("2,Test Movie 2,\"[{\"\"name\"\":\"\"Actor1\"\"}," +
                    "{\"\"name\"\":\"\"Actor3\"\"}]\",\"[{\"\"name\"\":\"\"Director2\"\"," +
                    "\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer2\"\"," +
                    "\"\"job\"\":\"\"Writer\"\"}]\"\n");
            // Movie 3 credits
            writer.write("3,Test Movie 3,\"[{\"\"name\"\":\"\"Actor4\"\"}," +
                    "{\"\"name\"\":\"\"Actor5\"\"}]\",\"[{\"\"name\"\":\"\"Director3\"\"" +
                    ",\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer3\"\"," +
                    "\"\"job\"\":\"\"Writer\"\"}]\"\n");
        } catch (IOException e) {
            System.err.println("Failed to create test credits CSV: " + e.getMessage());
        }
    }

    @Test
    public void testBasicGameFlow() {
        // 1. Add players
        gameController.addPlayer("Player 1");
        gameController.addPlayer("Player 2");

        // 2. Get the players and set their properties
        Player player1 = gameController.getGameState().getPlayer1();
        Player player2 = gameController.getGameState().getPlayer2();

        player1.setConnectionType("genre");
        player1.setTargetCount(1); // Easy to test win condition

        player2.setConnectionType("person");
        player2.setTargetCount(2);

        // Manually set state to SETTING_WIN_CONDITIONS
        gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

        // 3. Start game with a specific movie
        Movie startMovie = new Movie(1, "Start Movie", "2023",
                Arrays.asList("Action", "Adventure"), "A start movie", 8.0);

        startMovie.setActors(Arrays.asList("Actor1", "Actor2"));
        startMovie.setDirectors(Arrays.asList("Director1"));

        gameController.getGameState().startGame(startMovie);

        // 4. Verify game state
        assertEquals(GameState.State.PLAYING, gameController.getGameState().getCurrentState());
        assertEquals(player1, gameController.getCurrentPlayer());
    }

    @Test
    public void testFullGamePlayWithGenreConnection() {
        try {
            // Load test data
            gameController.initialize(testMoviesIntCsv, testCreditsIntCsv);

            // 1. Add players
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // 2. Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            Player player2 = gameController.getGameState().getPlayer2();

            player1.setConnectionType("genre");
            player1.setTargetCount(1); // Just need 1 match to win

            player2.setConnectionType("genre");
            player2.setTargetCount(2);

            // Manually set state to SETTING_WIN_CONDITIONS
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // 3. Get the first movie from database
            Movie movie1 = movieDatabase.findMovieById(1);
            assertNotNull("Movie 1 should exist", movie1);

            // 4. Start game
            gameController.getGameState().startGame(movie1);

            // 5. Verify game state
            assertEquals(GameState.State.PLAYING, gameController.getGameState().getCurrentState());
            assertEquals(player1.getName(), gameController.getCurrentPlayer().getName());

            // 6. Make a move with genre connection (both movies have Action genre)
            Movie movie2 = movieDatabase.findMovieById(2);
            assertNotNull("Movie 2 should exist", movie2);

            boolean result = gameController.getGameState().selectMovie(movie2, "genre", "Action");
            assertTrue("Move should be valid", result);

            // 7. Test that the game is over - player1 only needed 1 point to win
            assertEquals(GameState.State.GAME_OVER, gameController.getGameState()
                    .getCurrentState());
            assertEquals(1, player1.getWinProgress());
            assertTrue(player1.hasWon());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGameWithPersonConnection() {
        try {
            // Load test data
            gameController.initialize(testMoviesIntCsv, testCreditsIntCsv);

            // 1. Add players
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // 2. Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            Player player2 = gameController.getGameState().getPlayer2();

            player1.setConnectionType("person");
            player1.setTargetCount(1); // Just need 1 match to win

            player2.setConnectionType("genre");
            player2.setTargetCount(2);

            // Manually set state to SETTING_WIN_CONDITIONS
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // 3. Get the first movie from database
            Movie movie1 = movieDatabase.findMovieById(1);
            assertNotNull("Movie 1 should exist", movie1);

            // Verify it has Actor1
            assertTrue("Movie 1 should have Actor1", movie1.getActors().contains("Actor1"));

            // 4. Start game
            gameController.getGameState().startGame(movie1);

            // 5. Verify game state
            assertEquals(GameState.State.PLAYING, gameController.getGameState().getCurrentState());
            assertEquals(player1.getName(), gameController.getCurrentPlayer().getName());

            // 6. Make a move with actor connection (Actor1 is in both movies)
            Movie movie2 = movieDatabase.findMovieById(2);
            assertNotNull("Movie 2 should exist", movie2);
            assertTrue("Movie 2 should have Actor1", movie2.getActors().contains("Actor1"));

            boolean result = gameController.getGameState().selectMovie(movie2, "actor", "Actor1");
            assertTrue("Move should be valid", result);

            // 7. Test that the game is over - player1 only needed 1 point to win
            assertEquals(GameState.State.GAME_OVER, gameController.
                    getGameState().getCurrentState());
            assertEquals(1, player1.getWinProgress());
            assertTrue(player1.hasWon());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testAutocompleteIntegration() {
        try {
            // Load test data
            gameController.initialize(testMoviesIntCsv, testCreditsIntCsv);

            // Test autocomplete functionality
            List<Movie> suggestions = autocompleteController.getSuggestions("Test");

            // Verify suggestions
            assertFalse("Should return suggestions", suggestions.isEmpty());
            assertEquals("Should return 3 movies", 3, suggestions.size());

            // Check if the suggestions are ordered properly
            for (Movie movie : suggestions) {
                assertTrue("Should return movie titles starting with 'Test'",
                        movie.getTitle().startsWith("Test"));
            }

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testInvalidMoveWithNoConnection() {
        try {
            // Load test data
            gameController.initialize(testMoviesIntCsv, testCreditsIntCsv);

            // 1. Add players
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // 2. Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            Player player2 = gameController.getGameState().getPlayer2();

            player1.setConnectionType("person");
            player1.setTargetCount(2);

            // Manually set state to SETTING_WIN_CONDITIONS
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // 3. Start with movie1 (has Actor1, Actor2)
            Movie movie1 = movieDatabase.findMovieById(1);
            gameController.getGameState().startGame(movie1);

            // 4. Try to select movie3 which has no common actors with movie1
            Movie movie3 = movieDatabase.findMovieById(3);

            boolean result = gameController.getGameState().selectMovie(movie3, "actor", "Actor4");

            // 5. Verify move was rejected
            assertFalse("Move should be invalid - no connection", result);
            assertEquals("Game state should not change", GameState.State.PLAYING,
                    gameController.getGameState().getCurrentState());
            assertEquals("Current movie should not change", movie1,
                    gameController.getGameState().getCurrentMovie());
            assertEquals("Player progress should not change", 0, player1.getWinProgress());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testTimeUpScenario() {
        try {
            // 1. Add players
            gameController.addPlayer("Player 1");
            gameController.addPlayer("Player 2");

            // 2. Set player properties
            Player player1 = gameController.getGameState().getPlayer1();
            Player player2 = gameController.getGameState().getPlayer2();

            player1.setConnectionType("genre");
            player1.setTargetCount(3);
            player2.setConnectionType("genre");
            player2.setTargetCount(3);

            // Manually set state to SETTING_WIN_CONDITIONS
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);

            // 3. Start game
            Movie startMovie = new Movie(1, "Start Movie", "2023",
                    Arrays.asList("Action"), "A start movie", 8.0);
            gameController.getGameState().startGame(startMovie);

            // 4. Simulate time running out
            gameController.handleTimeUp();

            // 5. Verify game over and player 2 wins
            assertEquals(GameState.State.GAME_OVER, gameController.
                    getGameState().getCurrentState());
            assertEquals(0, player1.getWinProgress());
            assertTrue(player2.getWinProgress() > 0);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}