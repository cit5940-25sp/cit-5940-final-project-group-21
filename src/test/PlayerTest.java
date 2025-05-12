package main.test;

import main.model.Movie;
import main.model.Player;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class PlayerTest {
    private Player player;
    private Movie movie1;
    private Movie movie2;

    @Before
    public void setUp() {
        player = new Player("TestPlayer");
        movie1 = new Movie(1, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        movie2 = new Movie(2, "Movie 2", "2022", Arrays.asList("Comedy"), "", 0);
    }

    @Test
    public void testPlayerInitialization() {
        assertEquals("TestPlayer", player.getName());
        assertNull(player.getConnectionType());
        assertEquals(0, player.getTargetCount());
        assertEquals(0, player.getWinProgress());
        assertTrue(player.getSelectedMovies().isEmpty());
    }

    @Test
    public void testSetAndGetConnectionType() {
        assertNull(player.getConnectionType());

        player.setConnectionType("genre");
        assertEquals("genre", player.getConnectionType());

        player.setConnectionType("person");
        assertEquals("person", player.getConnectionType());
    }

    @Test
    public void testSetAndGetTargetCount() {
        assertEquals(0, player.getTargetCount());

        player.setTargetCount(3);
        assertEquals(3, player.getTargetCount());

        player.setTargetCount(5);
        assertEquals(5, player.getTargetCount());
    }

    @Test
    public void testAddSelectedMovie() {
        assertTrue(player.getSelectedMovies().isEmpty());

        // Add first movie
        boolean result1 = player.addSelectedMovie(movie1);
        assertTrue(result1);
        assertEquals(1, player.getSelectedMovies().size());
        assertTrue(player.getSelectedMovies().contains(movie1));

        // Add second movie
        boolean result2 = player.addSelectedMovie(movie2);
        assertTrue(result2);
        assertEquals(2, player.getSelectedMovies().size());
        assertTrue(player.getSelectedMovies().contains(movie2));

        // Try to add duplicate movie
        boolean result3 = player.addSelectedMovie(movie1);
        assertFalse(result3);
        assertEquals(2, player.getSelectedMovies().size()); // Count should not change
    }

    @Test
    public void testHasWon() {
        player.setTargetCount(3);

        // Initially player has not won
        assertFalse(player.hasWon());

        // Increment progress
        player.incrementProgress();
        assertFalse(player.hasWon());

        player.incrementProgress();
        assertFalse(player.hasWon());

        // After reaching target count, player should win
        player.incrementProgress();
        assertTrue(player.hasWon());
    }

    @Test
    public void testReset() {
        // Set up some state
        player.setConnectionType("genre");
        player.setTargetCount(3);
        player.incrementProgress();
        player.incrementProgress();
        player.addSelectedMovie(movie1);
        player.addSelectedMovie(movie2);

        // Verify state before reset
        assertEquals(2, player.getWinProgress());
        assertEquals(2, player.getSelectedMovies().size());

        // Reset player
        player.reset();

        // Verify reset state
        assertEquals(0, player.getWinProgress());
        assertTrue(player.getSelectedMovies().isEmpty());
    }
}