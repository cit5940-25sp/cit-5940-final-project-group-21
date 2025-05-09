
package test;

import model.GameState;
import model.Movie;
import model.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Test class for the GameState model.
 */
public class GameStateTest {

    private GameState gameState;
    private Movie testMovie;

    /**
     * Set up test environment before each test
     */
    @Before
    public void setUp() {
        gameState = new GameState();
        testMovie = new Movie(1, "Test Movie", "2023",
                Arrays.asList("Action", "Comedy"), "Test overview", 7.5);
    }

    /**
     * Test adding players
     */
    @Test
    public void testAddPlayer() {
        // Act & Assert
        assertTrue(gameState.addPlayer("Player 1"));
        assertTrue(gameState.addPlayer("Player 2"));
        assertFalse(gameState.addPlayer("Player 3")); // Game should be full

        assertEquals(GameState.State.SETTING_WIN_CONDITIONS, gameState.getCurrentState());
    }

    /**
     * Test setting win conditions
     */
    @Test
    public void testSetPlayerWinCondition() {
        // Arrange
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        Player.WinCondition condition1 = new Player.GenreWinCondition("Action", 3);
        Player.WinCondition condition2 = new Player.GenreWinCondition("Comedy", 2);

        // Act
        gameState.setPlayerWinCondition(player1, condition1);
        assertEquals(GameState.State.SETTING_WIN_CONDITIONS, gameState.getCurrentState());

        gameState.setPlayerWinCondition(player2, condition2);

        // Assert
        assertEquals(GameState.State.PLAYING, gameState.getCurrentState());
        assertEquals(player1, gameState.getCurrentPlayer()); // Player 1 should go first
    }

    /**
     * Test starting a game
     */
    @Test
    public void testStartGame() {
        // Arrange
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        Player.WinCondition condition1 = new Player.GenreWinCondition("Action", 3);
        Player.WinCondition condition2 = new Player.GenreWinCondition("Comedy", 2);

        gameState.setPlayerWinCondition(player1, condition1);
        gameState.setPlayerWinCondition(player2, condition2);

        // Act
        gameState.startGame(testMovie);

        // Assert
        assertEquals(testMovie, gameState.getCurrentMovie());
        assertEquals(1, gameState.getRoundCount());
        assertEquals(player1, gameState.getCurrentPlayer());
    }

    /**
     * Test selecting a movie during gameplay
     */
    @Test
    public void testSelectMovie() {
        // Arrange
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        Player.WinCondition condition1 = new Player.GenreWinCondition("Action", 3);
        Player.WinCondition condition2 = new Player.GenreWinCondition("Comedy", 2);

        gameState.setPlayerWinCondition(player1, condition1);
        gameState.setPlayerWinCondition(player2, condition2);

        gameState.startGame(testMovie);

        Movie nextMovie = new Movie(2, "Next Movie", "2022",
                Arrays.asList("Action", "Drama"), "Another movie", 8.0);

        // Act
        boolean result = gameState.selectMovie(nextMovie, "actor");

        // Assert
        assertTrue(result);
        assertEquals(nextMovie, gameState.getCurrentMovie());
        assertEquals(player2, gameState.getCurrentPlayer()); // Should switch to player 2
        assertEquals(2, gameState.getRoundCount());
        assertTrue(player1.getSelectedMovies().contains(nextMovie));
    }

    /**
     * Test checking for win conditions
     */
    @Test
    public void testWinCondition() {
        // Arrange
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        // Set player 1 to win with just 1 action movie
        Player.WinCondition condition1 = new Player.GenreWinCondition("Action", 1);
        Player.WinCondition condition2 = new Player.GenreWinCondition("Comedy", 2);

        gameState.setPlayerWinCondition(player1, condition1);
        gameState.setPlayerWinCondition(player2, condition2);

        // Start with a non-action movie
        Movie startMovie = new Movie(1, "Start Movie", "2023",
                Arrays.asList("Drama"), "Start movie", 7.0);
        gameState.startGame(startMovie);

        // Next movie is action
        Movie actionMovie = new Movie(2, "Action Movie", "2022",
                Arrays.asList("Action"), "Action movie", 8.0);

        // Act
        boolean result = gameState.selectMovie(actionMovie, "actor");

        // Assert
        assertTrue(result);
        assertEquals(GameState.State.GAME_OVER, gameState.getCurrentState());
        assertTrue(player1.hasWon());
    }

    /**
     * Test connection limits (maximum 3 uses)
     */
    @Test
    public void testConnectionLimits() {
        // Arrange
        gameState.addPlayer("Player 1");
        gameState.addPlayer("Player 2");
        Player player1 = gameState.getPlayer1();
        Player player2 = gameState.getPlayer2();

        Player.WinCondition condition1 = new Player.GenreWinCondition("Action", 5);
        Player.WinCondition condition2 = new Player.GenreWinCondition("Comedy", 5);

        gameState.setPlayerWinCondition(player1, condition1);
        gameState.setPlayerWinCondition(player2, condition2);

        gameState.startGame(testMovie);

        Movie movie1 = new Movie(2, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(3, "Movie 2", "2023", Arrays.asList("Action"), "", 0);
        Movie movie3 = new Movie(4, "Movie 3", "2023", Arrays.asList("Action"), "", 0);
        Movie movie4 = new Movie(5, "Movie 4", "2023", Arrays.asList("Action"), "", 0);
        Movie movie5 = new Movie(6, "Movie 5", "2023", Arrays.asList("Action"), "", 0);
        Movie movie6 = new Movie(7, "Movie 6", "2023", Arrays.asList("Action"), "", 0);
        Movie movie7 = new Movie(8, "Movie 7", "2023", Arrays.asList("Action"), "", 0);

        // Act & Assert - Use the same actor connection 3 times
        assertTrue(gameState.selectMovie(movie1, "actor"));
        assertEquals(player2, gameState.getCurrentPlayer());

        assertTrue(gameState.selectMovie(movie2, "actor"));
        assertEquals(player1, gameState.getCurrentPlayer());

        assertTrue(gameState.selectMovie(movie3, "actor"));
        assertEquals(player2, gameState.getCurrentPlayer());

        assertTrue(gameState.selectMovie(movie4, "actor"));
        assertEquals(player1, gameState.getCurrentPlayer());

        assertTrue(gameState.selectMovie(movie5, "actor"));
        assertEquals(player2, gameState.getCurrentPlayer());

        assertTrue(gameState.selectMovie(movie6, "actor"));
        assertEquals(player1, gameState.getCurrentPlayer());

        // This should fail as "actor" has been used 3 times
        assertFalse(gameState.selectMovie(movie7, "actor"));

        // But should succeed with a different connection
        assertTrue(gameState.selectMovie(movie7, "director"));
    }
}