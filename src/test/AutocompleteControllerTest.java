package main.test;

import main.controller.AutocompleteController;
import main.model.Movie;
import main.model.MovieDatabase;
import org.junit.Before;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for the AutocompleteController class.
 * These tests validate the suggestion functionality based on prefix matching.
 *
 * Uses a temporary CSV dataset to simulate movie data.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class AutocompleteControllerTest {

    private MovieDatabase movieDatabase;
    private AutocompleteController autocompleteController;
    private final String testMoviesCsv = "test_movies_ac.csv";

    /**
     * Sets up the MovieDatabase and AutocompleteController before each test.
     */
    @Before
    public void setUp() {
        movieDatabase = new MovieDatabase();
        autocompleteController = new AutocompleteController(movieDatabase);
        createTestCSVFile();
    }

    /**
     * Creates a small CSV file used for unit testing autocomplete logic.
     */
    private void createTestCSVFile() {
        try (FileWriter writer = new FileWriter(testMoviesCsv)) {
            writer.write("budget,genres,homepage,id,keywords,original_language,original_title," +
                    "overview,popularity, production_companies,production_countries,release_date," +
                    "revenue,runtime,spoken_languages,status," +
                    "tagline,title,vote_average,vote_count\n");
            writer.write("1000000,\"[{\"\"name\"\":\"\"Action\"\"}]\",http://test.com,1,\"\"" +
                    ",en,Test Movie,\"Test overview\",10.0,\"\",\"\",2023-01-01,2000000,120,\"\"" +
                    ",Released,\"Test tagline\",Test Movie,7.5,100\n");
            writer.write("2000000,\"[{\"\"name\"\":\"\"Comedy\"\"}]\",http://test2.com,2,\"" +
                    ",en,The Adventure,\"Test overview 2\",8.0,\"\",\"\"," +
                    "2023-02-01,3000000,110,\"" +
                    ",Released,\"Test tagline 2\",The Adventure,8.0,200\n");
            writer.write("3000000,\"[{\"\"name\"\":\"\"Drama\"\"}]\"," +
                    "http://test3.com,3,\"\"" +
                    ",en,Another Title,\"Test overview 3\",9.0,\"\",\"\"," +
                    "2023-03-01,4000000,130,\"\"" +
                    ",Released,\"Test tagline 3\",Another Title,8.5,300\n");
            writer.write("4000000,\"[{\"\"name\"\":\"\"Thriller\"\"}]\"," +
                    "http://test4.com,4,\"\"" +
                    ",en,Test Another,\"Test overview 4\",7.0,\"\",\"\"," +
                    "2023-04-01,5000000,100,\"\"" +
                    ",Released,\"Test tagline 4\",Test Another,7.0,400\n");
        } catch (IOException e) {
            System.err.println("Failed to create test movies CSV: " + e.getMessage());
        }
    }

    /**
     * Tests suggestion behavior when given an empty string.
     */
    @Test
    public void testGetSuggestionsWithEmptyPrefix() {
        List<Movie> suggestions = autocompleteController.getSuggestions("");
        assertTrue(suggestions.isEmpty());
    }

    /**
     * Tests suggestion behavior when given a null prefix.
     */
    @Test
    public void testGetSuggestionsWithNullPrefix() {
        List<Movie> suggestions = autocompleteController.getSuggestions(null);
        assertTrue(suggestions.isEmpty());
    }

    /**
     * Tests behavior when provided with invalid limits (0 or negative).
     */
    @Test
    public void testGetSuggestionsWithInvalidLimit() {
        List<Movie> suggestions = autocompleteController.getSuggestions("The", 0);
        assertTrue(suggestions.isEmpty());

        suggestions = autocompleteController.getSuggestions("The", -5);
        assertTrue(suggestions.isEmpty());
    }

    /**
     * Tests behavior when provided with whitespace-only prefix.
     */
    @Test
    public void testGetSuggestionsWithWhitespacePrefix() {
        List<Movie> suggestions = autocompleteController.getSuggestions("  ");
        assertTrue(suggestions.isEmpty());
    }

    /**
     * Tests that valid suggestions are returned for a common prefix.
     */
    @Test
    public void testGetSuggestionsWithValidPrefix() {
        try {
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);
            List<Movie> suggestions = autocompleteController.getSuggestions("Test");

            assertFalse("Should return suggestions", suggestions.isEmpty());
            assertEquals("Should return 2 movies", 2, suggestions.size());

            boolean hasTestMovie = false;
            boolean hasTestAnother = false;

            for (Movie movie : suggestions) {
                if (movie.getTitle().equals("Test Movie")) {
                    hasTestMovie = true;
                } else if (movie.getTitle().equals("Test Another")) {
                    hasTestAnother = true;
                }
            }

            assertTrue("Should contain 'Test Movie'", hasTestMovie);
            assertTrue("Should contain 'Test Another'", hasTestAnother);

        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests suggestion functionality when a custom result limit is set.
     */
    @Test
    public void testGetSuggestionsWithCustomLimit() {
        try {
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);
            List<Movie> suggestions = autocompleteController.getSuggestions("T", 1);
            assertFalse("Should return suggestions", suggestions.isEmpty());
            assertEquals("Should respect limit of 1", 1, suggestions.size());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests suggestion behavior when no movies match the given prefix.
     */
    @Test
    public void testGetSuggestionsWithNonMatchingPrefix() {
        try {
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);
            List<Movie> suggestions =
                    autocompleteController.getSuggestions("XYZ");
            assertTrue("Should not return suggestions for non-matching prefix",
                    suggestions.isEmpty());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }

    /**
     * Tests that suggestions are case-insensitive.
     */
    @Test
    public void testGetSuggestionsCaseInsensitive() {
        try {
            movieDatabase.loadMoviesFromCSV(testMoviesCsv);
            List<Movie> suggestions1 = autocompleteController.getSuggestions("test");
            List<Movie> suggestions2 = autocompleteController.getSuggestions("TEST");
            assertEquals("Should be case insensitive", suggestions1.size(), suggestions2.size());
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
}
