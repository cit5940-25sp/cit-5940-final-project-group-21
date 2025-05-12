package main.test;

import main.model.Movie;
import main.model.MovieDatabase;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class MovieDatabaseTest {

    private MovieDatabase movieDatabase;
    private final String testMoviesCsv = "test_movies.csv";
    private final String testCreditsCsv = "test_credits.csv";

    @Before
    public void setUp() {
        movieDatabase = new MovieDatabase();
        createTestCSVFiles();
    }

    private void createTestCSVFiles() {
        // Create a simple movies CSV file for testing
        try (FileWriter writer = new FileWriter(testMoviesCsv)) {
            // Header
            writer.write("budget,genres,homepage,id,keywords,original_language," +
                    "original_title,overview,popularity,production_companies," +
                    "production_countries,release_date,revenue,runtime,spoken_languages," +
                    "status,tagline,title,vote_average,vote_count\n");
            // Test movie 1
            writer.write("1000000,\"[{\"\"name\"\":\"\"Action\"\"},{\"\"name\"\":\"\"" +
                    "Adventure\"\"}]\",http://test.com,1,\"\",en,Test Movie 1,\"Test overview 1\"" +
                    ",10.0,\"\",\"\",2023-01-01,2000000,120,\"\",Released,\"Test tagline\"" +
                    ",Test Movie 1,7.5,100\n");
            // Test movie 2
            writer.write("2000000,\"[{\"\"name\"\":\"\"Comedy\"\"}," +
                    "{\"\"name\"\":\"\"Drama\"\"}]\",http://test2.com,2,\"\",en," +
                    "Test Movie 2,\"Test overview 2\",8.0,\"\",\"\",2023-02-01,3000000,110,\"" +
                    "\",Released,\"Test tagline 2\",Test Movie 2,8.0,200\n");
        } catch (IOException e) {
            System.err.println("Failed to create test movies CSV: " + e.getMessage());
        }

        // Create a simple credits CSV file for testing
        try (FileWriter writer = new FileWriter(testCreditsCsv)) {
            // Header
            writer.write("movie_id,title,cast,crew\n");
            // Test movie 1 credits
            writer.write("1,Test Movie 1,\"[{\"\"name\"\":\"\"Actor1\"\"}," +
                    "{\"\"name\"\":\"\"Actor2\"\"}]\",\"[{\"\"name\"\":\"\"Director1\"\"," +
                    "\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer1\"\"," +
                    "\"\"job\"\":\"\"Writer\"\"}]\"\n");
            // Test movie 2 credits
            writer.write("2,Test Movie 2,\"[{\"\"name\"\":\"\"Actor3\"\"}," +
                    "{\"\"name\"\":\"\"Actor4\"\"}]\",\"[{\"\"name\"\":\"\"Director2\"\"," +
                    "\"\"job\"\":\"\"Director\"\"},{\"\"name\"\":\"\"Writer2\"\",\"\"job\"" +
                    "\":\"\"Writer\"\"},{\"\"name\"\":\"\"Composer1\"\",\"\"job\"\":\"\"" +
                    "Original Music Composer\"\"}]\"\n");
        } catch (IOException e) {
            System.err.println("Failed to create test credits CSV: " + e.getMessage());
        }
    }

    @Test
    public void testInitialization() {
        List<Movie> allMovies = movieDatabase.getAllMovies();
        assertTrue(allMovies.isEmpty());
    }

    @Test
    public void testFindMovieByIdNotFound() {
        Movie result = movieDatabase.findMovieById(999);
        assertNull(result);
    }

    @Test
    public void testFindMoviesByTitleEmptyDatabase() {
        List<Movie> results = movieDatabase.findMoviesByTitle("Test");
        assertTrue(results.isEmpty());
    }

    @Test
    public void testFindMoviesByTitlePrefixEmptyDatabase() {
        List<Movie> results = movieDatabase.findMoviesByTitlePrefix("Test", 10);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testGetAllGenresEmptyDatabase() {
        assertTrue(movieDatabase.getAllGenres().isEmpty());
    }

    @Test
    public void testGetRandomMovieEmptyDatabase() {
        Movie movie = movieDatabase.getRandomMovie();
        assertNull(movie);
    }

    @Test
    public void testLoadMoviesFromCSV() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Verify movies are loaded
            List<Movie> allMovies = movieDatabase.getAllMovies();
            assertFalse("Movie database should not be empty after loading", allMovies.isEmpty());
            assertEquals("Should have loaded 2 test movies", 2, allMovies.size());

            // Verify movie properties
            Movie movie = allMovies.stream()
                    .filter(m -> m.getId() == 1)
                    .findFirst()
                    .orElse(null);

            assertNotNull("Movie with ID 1 should exist", movie);
            assertEquals("Test Movie 1", movie.getTitle());
            assertEquals("2023-01-01", movie.getReleaseDate());
            assertEquals("2023", movie.getReleaseYear());
            assertTrue("Movie should have Action genre", movie.getGenres().contains("Action"));
            assertTrue("Movie should have Adventure genre", movie.getGenres().
                    contains("Adventure"));
            assertEquals("Test overview 1", movie.getOverview());
            assertEquals(7.5, movie.getVoteAverage(), 0.001);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testLoadCreditsFromCSV() {
        try {
            // First load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Then load credits
            movieDatabase.loadCreditsFromCSV(testCreditsCsv);

            // Verify credits are loaded
            List<Movie> allMovies = movieDatabase.getAllMovies();

            // Get first movie
            Movie movie1 = allMovies.stream()
                    .filter(m -> m.getId() == 1)
                    .findFirst()
                    .orElse(null);

            assertNotNull(movie1);
            assertFalse("Movie should have actors", movie1.getActors().isEmpty());
            assertTrue("Movie should have Actor1", movie1.getActors().contains("Actor1"));
            assertTrue("Movie should have Actor2", movie1.getActors().contains("Actor2"));

            assertFalse("Movie should have directors", movie1.getDirectors().isEmpty());
            assertTrue("Movie should have Director1", movie1.getDirectors().contains("Director1"));

            assertFalse("Movie should have writers", movie1.getWriters().isEmpty());
            assertTrue("Movie should have Writer1", movie1.getWriters().contains("Writer1"));

            // Get second movie
            Movie movie2 = allMovies.stream()
                    .filter(m -> m.getId() == 2)
                    .findFirst()
                    .orElse(null);

            assertNotNull(movie2);
            assertTrue("Movie should have Composer1", movie2.getComposers().contains("Composer1"));

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetAllGenres() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Get all genres
            Set<String> genres = movieDatabase.getAllGenres();

            // Verify genres
            assertFalse("Genres should not be empty", genres.isEmpty());
            assertTrue("Should contain Action genre", genres.contains("Action"));
            assertTrue("Should contain Adventure genre", genres.contains("Adventure"));
            assertTrue("Should contain Comedy genre", genres.contains("Comedy"));
            assertTrue("Should contain Drama genre", genres.contains("Drama"));

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testFindMoviesByTitle() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Find movies by title
            List<Movie> movies = movieDatabase.findMoviesByTitle("Test Movie 1");

            // Verify results
            assertFalse("Should find movies", movies.isEmpty());
            assertEquals("Should find 1 movie", 1, movies.size());
            assertEquals("Test Movie 1", movies.get(0).getTitle());

            // Test partial title
            movies = movieDatabase.findMoviesByTitle("Test");
            assertEquals("Should find 2 movies", 2, movies.size());

            // Test non-existent title
            movies = movieDatabase.findMoviesByTitle("NonExistent");
            assertTrue("Should not find any movies", movies.isEmpty());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testFindMoviesByTitlePrefix() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Find movies by title prefix
            List<Movie> movies = movieDatabase.findMoviesByTitlePrefix("Test", 10);

            // Verify results
            assertFalse("Should find movies", movies.isEmpty());
            assertEquals("Should find 2 movies", 2, movies.size());

            // Test with limit
            movies = movieDatabase.findMoviesByTitlePrefix("Test", 1);
            assertEquals("Should respect limit", 1, movies.size());

            // Test non-existent prefix
            movies = movieDatabase.findMoviesByTitlePrefix("NonExistent", 10);
            assertTrue("Should not find any movies", movies.isEmpty());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testFindMoviesByGenre() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Find movies by genre
            List<Movie> movies = movieDatabase.findMoviesByGenre("Action");

            // Verify results
            assertFalse("Should find movies", movies.isEmpty());
            assertEquals("Should find 1 movie", 1, movies.size());
            assertEquals("Test Movie 1", movies.get(0).getTitle());

            // Test non-existent genre
            movies = movieDatabase.findMoviesByGenre("NonExistent");
            assertTrue("Should not find any movies", movies.isEmpty());

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testGetRandomMovie() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Get random movie
            Movie movie = movieDatabase.getRandomMovie();

            // Verify result
            assertNotNull("Should return a movie", movie);
            assertTrue("Should be one of the test movies",
                    movie.getTitle().equals("Test Movie 1") ||
                            movie.getTitle().equals("Test Movie 2"));

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    @Test
    public void testFindMovieById() {
        try {
            // Load movies
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);

            // Find movie by ID
            Movie movie = movieDatabase.findMovieById(1);

            // Verify result
            assertNotNull("Should find movie with ID 1", movie);
            assertEquals("Test Movie 1", movie.getTitle());

            // Test non-existent ID
            movie = movieDatabase.findMovieById(999);
            assertNull("Should not find movie with non-existent ID", movie);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}