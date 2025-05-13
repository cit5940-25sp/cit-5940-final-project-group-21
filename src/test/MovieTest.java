package main.test;

import main.model.Movie;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
/**
 * Unit tests for the Movie class.
 * Tests cover movie property accessors, mutators,
 * and behaviors like genre checking, equality, and string formatting.
 *
 * Each test ensures individual Movie fields and methods function as expected.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class MovieTest {

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

    @Test
    public void testGetReleaseYear() {
        // Arrange & Act
        Movie movie1 = new Movie(1, "Movie 1", "2023-04-15", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(2, "Movie 2", "20", Arrays.asList("Comedy"), "", 0);
        Movie movie3 = new Movie(3, "Movie 3", null, Arrays.asList("Drama"), "", 0);

        // Assert
        assertEquals("2023", movie1.getReleaseYear());
        assertEquals("Unknown", movie2.getReleaseYear());
        assertEquals("Unknown", movie3.getReleaseYear());
    }

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

    @Test
    public void testActorsMethods() {
        // Arrange
        Movie movie = new Movie(1, "Test Movie", "2023", Arrays.asList("Action"), "", 0);

        // Initial state
        assertTrue(movie.getActors().isEmpty());

        // Add actors
        List<String> actors = Arrays.asList("Actor1", "Actor2", "Actor3");
        movie.setActors(actors);

        // Verify
        assertEquals(actors.size(), movie.getActors().size());
        assertTrue(movie.getActors().contains("Actor1"));
        assertTrue(movie.getActors().contains("Actor2"));
        assertTrue(movie.getActors().contains("Actor3"));
    }

    @Test
    public void testDirectorsMethods() {
        // Arrange
        Movie movie = new Movie(1, "Test Movie", "2023", Arrays.asList("Action"), "", 0);

        // Initial state
        assertTrue(movie.getDirectors().isEmpty());

        // Add directors
        List<String> directors = Arrays.asList("Director1", "Director2");
        movie.setDirectors(directors);

        // Verify
        assertEquals(directors.size(), movie.getDirectors().size());
        assertTrue(movie.getDirectors().contains("Director1"));
        assertTrue(movie.getDirectors().contains("Director2"));
    }

    @Test
    public void testWritersMethods() {
        // Arrange
        Movie movie = new Movie(1, "Test Movie", "2023", Arrays.asList("Action"), "", 0);

        // Initial state
        assertTrue(movie.getWriters().isEmpty());

        // Add writers
        List<String> writers = Arrays.asList("Writer1", "Writer2");
        movie.setWriters(writers);

        // Verify
        assertEquals(writers.size(), movie.getWriters().size());
        assertTrue(movie.getWriters().contains("Writer1"));
        assertTrue(movie.getWriters().contains("Writer2"));
    }

    @Test
    public void testComposersMethods() {
        // Arrange
        Movie movie = new Movie(1, "Test Movie", "2023", Arrays.asList("Action"), "", 0);

        // Initial state
        assertTrue(movie.getComposers().isEmpty());

        // Add composers
        List<String> composers = Arrays.asList("Composer1", "Composer2");
        movie.setComposers(composers);

        // Verify
        assertEquals(composers.size(), movie.getComposers().size());
        assertTrue(movie.getComposers().contains("Composer1"));
        assertTrue(movie.getComposers().contains("Composer2"));
    }

    @Test
    public void testEquals() {
        // Arrange
        Movie movie1 = new Movie(1, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(1, "Different Title", "2020", Arrays.asList("Comedy"), "", 0);
        Movie movie3 = new Movie(2, "Movie 1", "2023", Arrays.asList("Action"), "", 0);

        // Act & Assert
        assertTrue(movie1.equals(movie1)); // Same instance
        assertTrue(movie1.equals(movie2)); // Same ID, different properties
        assertFalse(movie1.equals(movie3)); // Different ID, same properties
        assertFalse(movie1.equals(null)); // Null check
        assertFalse(movie1.equals("Not a movie")); // Different type
    }

    @Test
    public void testHashCode() {
        // Arrange
        Movie movie1 = new Movie(1, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        Movie movie2 = new Movie(1, "Different Title", "2020", Arrays.asList("Comedy"), "", 0);

        // Act & Assert
        assertEquals(movie1.hashCode(), movie2.hashCode()); // Same ID should have same hash

        // Different ID should have different hash
        Movie movie3 = new Movie(2, "Movie 1", "2023", Arrays.asList("Action"), "", 0);
        assertNotEquals(movie1.hashCode(), movie3.hashCode());
    }

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