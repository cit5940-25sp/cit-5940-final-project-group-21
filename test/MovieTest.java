package test;

import main.model.Movie;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Test class for the Movie model.
 */
public class MovieTest {

    /**
     * Test basic movie properties
     */
    @Test
    public void testMovieProperties() {
        // Arrange
        int id = 123;
        String title = "Test Movie";
        String releaseDate = "2023-04-15";
        List<String> genres = Arrays.asList("Action", "Comedy");
        String overview = "This is a test movie";
        double voteAverage = 7.5;

        // Act
        Movie movie = new Movie(id, title, releaseDate, genres, overview, voteAverage);

        // Assert
        assertEquals(id, movie.getId());
        assertEquals(title, movie.getTitle());
        assertEquals(releaseDate, movie.getReleaseDate());
        assertEquals(genres, movie.getGenres());
        assertEquals(overview, movie.getOverview());
        assertEquals(voteAverage, movie.getVoteAverage(), 0.001);
    }

    /**
     * Test release year extraction
     */
    @Test
    public void testGetReleaseYear() {
        // Arrange
        Movie movie1 = new Movie(1, "Movie 1", "2023-04-15", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(2, "Movie 2", "Invalid date", Arrays.asList("Comedy"), "", 0);
        Movie movie3 = new Movie(3, "Movie 3", null, Arrays.asList("Drama"), "", 0);

        // Act & Assert
        assertEquals("2023", movie1.getReleaseYear());
        assertEquals("Unknown", movie2.getReleaseYear());
        assertEquals("Unknown", movie3.getReleaseYear());
    }

    /**
     * Test genre checking
     */
    @Test
    public void testHasGenre() {
        // Arrange
        List<String> genres = Arrays.asList("Action", "Adventure", "Sci-Fi");
        Movie movie = new Movie(1, "Test Movie", "2023", genres, "", 0);

        // Act & Assert
        assertTrue(movie.hasGenre("Action"));
        assertTrue(movie.hasGenre("Adventure"));
        assertTrue(movie.hasGenre("Sci-Fi"));
        assertFalse(movie.hasGenre("Comedy"));
        assertFalse(movie.hasGenre("Drama"));
    }

    /**
     * Test movie equality
     */
    @Test
    public void testEquals() {
        // Arrange
        Movie movie1 = new Movie(1, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(1, "Different Title", "2020", Arrays.asList("Comedy"), "", 0);
        Movie movie3 = new Movie(2, "Movie 1", "2023", Arrays.asList("Action"), "", 0);

        // Act & Assert
        assertTrue(movie1.equals(movie2)); // Same ID, different properties
        assertFalse(movie1.equals(movie3)); // Different ID, same properties
        assertFalse(movie1.equals(null));
        assertFalse(movie1.equals("Not a movie"));
    }

    /**
     * Test toString implementation
     */
    @Test
    public void testToString() {
        // Arrange
        Movie movie = new Movie(1, "Test Movie", "2023-04-15", Arrays.asList("Action"), "", 0);

        // Act
        String result = movie.toString();

        // Assert
        assertEquals("Test Movie (2023)", result);
    }
}