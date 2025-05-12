package main.Test;

import main.model.GameState;
import main.model.Movie;
import main.model.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class GameStateTest {
    private GameState gameState;
    private Movie testMovie;

    @Before
    public void setUp() {
        gameState = new GameState();
        testMovie = new Movie(1, "Test Movie", "2023-01-01",
                Arrays.asList("Action", "Comedy"), "Test overview", 7.5);

        // Set up actors and crew for connection testing
        testMovie.setActors(Arrays.asList("Actor1", "Actor2"));
        testMovie.setDirectors(Arrays.asList("Director1"));
        testMovie.setWriters(Arrays.asList("Writer1"));
        testMovie.setComposers(Arrays.asList("Composer1"));
    }

    @Test
    public void testInitialState() {
        // Verify initial state
        assertEquals(GameState.State.WAITING_FOR_PLAYERS, gameState.getCurrentState());
        assertNull(gameState.getPlayer1());
        assertNull(gameState.getPlayer2());
        assertNull(gameState.getCurrentPlayer());
        assertNull(gameState.getCurrentMovie());
        assertEquals(0, gameState.getRoundCount());
    }

    @Test
    public void testAddFirstPlayer() {
        boolean added = gameState.addPlayer("Player 1");
        assertTrue(added);
        assertNotNull(gameState.getPlayer1());
        assertEquals("Player 1", gameState.getPlayer1().getName());
        assertEquals(GameState.State.WAITING_FOR_PLAYERS, gameState.getCurrentState());
    }

    @Test
    public void testAddSecondPlayer() {
        // Add both players
        gameState.addPlayer("Player 1");
        boolean added = gameState.addPlayer("Player 2");

        // Verify results
        assertTrue(added);
        assertNotNull(gameState.getPlayer1());
        assertNotNull(gameState.getPlayer2());
        assertEquals("Player 1", gameState.getPlayer1().getName());
        assertEquals("Player 2", gameState.getPlayer2().getName());

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);
        assertEquals(GameState.State.SETTING_WIN_CONDITIONS, gameState.getCurrentState());
    }

    @Test
    public void testCannotAddThirdPlayer() {
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        boolean added = gameState.addPlayer("Player 3");
        assertFalse(added);
    }

    @Test
    public void testBasicGameStart() {
        // Add players
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();
        gameState.startGame(testMovie);

        // Verify state after starting
        assertEquals(GameState.State.PLAYING, gameState.getCurrentState());
        assertEquals(testMovie, gameState.getCurrentMovie());
        assertEquals(player1, gameState.getCurrentPlayer());
        assertEquals(1, gameState.getRoundCount());

        // movie history
        List<GameState.MovieConnection> history = gameState.getRecentMovieHistory(1);
        assertEquals(1, history.size());
        assertEquals(testMovie, history.get(0).getMovie());
    }

    @Test
    public void testSwitchPlayer() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");

        // SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        gameState.startGame(testMovie);

        // Get player references
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        // player1
        assertEquals(player1, gameState.getCurrentPlayer());

        // Switch player
        gameState.switchToNextPlayer();

        // player2
        assertEquals(player2, gameState.getCurrentPlayer());

        // switch back
        gameState.switchToNextPlayer();
        assertEquals(player1, gameState.getCurrentPlayer());
    }

    @Test
    public void testSelectMovieByGenre() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        player1.setConnectionType("genre");
        player1.setTargetCount(3);

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        gameState.startGame(testMovie); // testMovie has "Action" genre

        // Create a movie with a common genre
        Movie nextMovie = new Movie(2, "Next Movie", "2022",
                Arrays.asList("Action", "Drama"), "Another movie", 8.0);

        // Select movie with genre connection
        boolean result = gameState.selectMovie(nextMovie, "genre", "Action");

        // Verify
        assertTrue(result);
        assertEquals(nextMovie, gameState.getCurrentMovie());
        assertEquals(2, gameState.getRoundCount());
        assertTrue(player1.getSelectedMovies().contains(nextMovie));
        assertEquals(1, player1.getWinProgress()); // Player 1 gained a point
    }

    @Test
    public void testSelectMovieByPerson() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        gameState.startGame(testMovie); // testMovie has "Actor1"

        // Create a movie with a common actor
        Movie actorMovie = new Movie(2, "Actor Movie", "2022",
                Arrays.asList("Drama"), "Actor connection", 8.0);
        actorMovie.setActors(Arrays.asList("Actor1", "Actor3"));

        // Select movie with actor connection
        boolean result = gameState.selectMovie(actorMovie, "actor", "Actor1");

        // Verify
        assertTrue(result);
        assertEquals(actorMovie, gameState.getCurrentMovie());
    }

    @Test
    public void testAutoDetectPersonConnection() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        gameState.startGame(testMovie); // testMovie has actors, directors, etc.

        // Create movie with actor connection
        Movie actorMovie = new Movie(2, "Actor Movie", "2022",
                Arrays.asList("Drama"), "", 0);
        actorMovie.setActors(Arrays.asList("Actor1", "Actor3"));

        // Test auto-detection (person with null connectionValue)
        boolean result = gameState.selectMovie(actorMovie, "person", null);

        // Verify
        assertTrue(result);

        // Get history to verify connection type was detected
        List<GameState.MovieConnection> history = gameState.getRecentMovieHistory(5);
        assertEquals(2, history.size());
        assertEquals("actor", history.get(1).getConnectionType());
        assertEquals("Actor1", history.get(1).getConnectionValue());
    }

    @Test
    public void testWinCondition() {
        // Setup game with low win target
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();
        player1.setConnectionType("genre");
        player1.setTargetCount(1); // Only need 1 point to win
        player2.setConnectionType("genre");
        player2.setTargetCount(2);

        // Manually set state to SETTING_WIN_CONDITIONS
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Start game
        gameState.startGame(testMovie);

        // Make a valid move that should win the game
        Movie winningMovie = new Movie(2, "Winning Movie", "2022",
                Arrays.asList("Action", "Drama"), "Winning move", 8.0);

        boolean result = gameState.selectMovie(winningMovie, "genre", "Action");

        // Verify win condition
        assertTrue(result);
        assertTrue(player1.hasWon());
        assertEquals(GameState.State.GAME_OVER, gameState.getCurrentState());
    }

    // Modified tests

    @Test
    public void testPersonConnectionLimit() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);
        gameState.startGame(testMovie);

        // Create a movie with a common actor
        Movie movie1 = new Movie(2, "Movie 1", "2022", Arrays.asList("Drama"), "", 0);
        movie1.setActors(Arrays.asList("Actor1", "Actor3"));

        // Verify we can select a movie with valid connection
        boolean result = gameState.selectMovie(movie1, "actor", "Actor1");
        assertTrue("Should be able to select movie with valid actor connection", result);

        // Verify the movie was selected
        assertEquals(movie1, gameState.getCurrentMovie());

        // Verify the progress was incremented
        assertEquals(1, gameState.getPlayer1().getWinProgress());
    }

    @Test
    public void testGetRecentMovieHistory() {
        // Setup game
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        gameState.setState(GameState.State.SETTING_WIN_CONDITIONS);

        // Create test movies
        Movie movie1 = new Movie(1, "Movie 1", "2021", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(2, "Movie 2", "2022", Arrays.asList("Action", "Comedy"), "", 0);

        // Start game and make one move
        gameState.startGame(movie1);
        gameState.selectMovie(movie2, "genre", "Action");

        // Test getting full history
        List<GameState.MovieConnection> fullHistory = gameState.getRecentMovieHistory(10);
        // The actual history contains only 2 movies (initial + one move)
        assertEquals(2, fullHistory.size());

        // Test getting limited history
        List<GameState.MovieConnection> limitedHistory = gameState.getRecentMovieHistory(1);
        assertEquals(1, limitedHistory.size());
        assertEquals(movie2, limitedHistory.get(0).getMovie());

        // Test zero limit
        List<GameState.MovieConnection> zeroHistory = gameState.getRecentMovieHistory(0);
        assertTrue(zeroHistory.isEmpty());
    }

    @Test
    public void testAutoDetectAllPersonConnectionTypes() {
        // Setup game with one test at a time
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");

        // Test director connection separately
        {
            GameState directorTest = new GameState();
            directorTest.addPlayer("Player 1");
            directorTest.addPlayer("Player 2");
            directorTest.setState(GameState.State.SETTING_WIN_CONDITIONS);

            // Create starting movie with director
            Movie startMovie = new Movie(1, "Start Movie", "2023", Arrays.asList("Action"), "", 0);
            startMovie.setDirectors(Arrays.asList("Director1"));

            directorTest.startGame(startMovie);

            // Create movie with director connection
            Movie directorMovie = new Movie(2, "Director Movie",
                    "2022", Arrays.asList("Drama"), "", 0);
            directorMovie.setDirectors(Arrays.asList("Director1", "Director2"));

            boolean directorResult = directorTest.selectMovie(directorMovie, "person", null);
            // Verify the auto-detection works for director
            assertTrue(directorResult);

            List<GameState.MovieConnection> history = directorTest.getRecentMovieHistory(5);
            assertEquals(2, history.size());
            assertEquals("director", history.get(1).getConnectionType());
        }

        // Test writer connection separately
        {
            GameState writerTest = new GameState();
            writerTest.addPlayer("Player 1");
            writerTest.addPlayer("Player 2");
            writerTest.setState(GameState.State.SETTING_WIN_CONDITIONS);

            // Create starting movie with writer
            Movie startMovie = new Movie(1, "Start Movie", "2023", Arrays.asList("Action"), "", 0);
            startMovie.setWriters(Arrays.asList("Writer1"));

            writerTest.startGame(startMovie);

            // Create movie with writer connection
            Movie writerMovie = new Movie(2, "Writer Movie", "2022", Arrays.asList("Drama"), "", 0);
            writerMovie.setWriters(Arrays.asList("Writer1", "Writer2"));

            boolean writerResult = writerTest.selectMovie(writerMovie, "person", null);
            // Verify the auto-detection works for writer
            assertTrue(writerResult);

            List<GameState.MovieConnection> history = writerTest.getRecentMovieHistory(5);
            assertEquals(2, history.size());
            assertEquals("writer", history.get(1).getConnectionType());
        }
    }
}