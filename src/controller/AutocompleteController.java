package main.controller;

import main.model.Movie;
import main.model.MovieDatabase;
import java.util.List;

/**
 * Controller for the autocomplete functionality.
 * Provides movie title suggestions based on a prefix.
 */
public class AutocompleteController {
    private final MovieDatabase movieDatabase;
    private static final int MAX_SUGGESTIONS = 10;

    /**
     * Constructor for the AutocompleteController
     *
     * @param movieDatabase Movie database
     */
    public AutocompleteController(MovieDatabase movieDatabase) {
        this.movieDatabase = movieDatabase;
    }

    /**
     * Get movie title suggestions based on the prefix.
     *
     * @param prefix Title prefix
     * @return List of matching movies (up to MAX_SUGGESTIONS)
     */
    public List<Movie> getSuggestions(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return List.of();
        }
        return movieDatabase.findMoviesByTitlePrefix(prefix, MAX_SUGGESTIONS);
    }

    /**
     * Get movie title suggestions based on the prefix and a custom limit.
     *
     * @param prefix   Title prefix
     * @param maxLimit Maximum number of suggestions
     * @return List of matching movies (up to maxLimit)
     */
    public List<Movie> getSuggestions(String prefix, int maxLimit) {
        if (prefix == null || prefix.trim().isEmpty() || maxLimit <= 0) {
            return List.of();
        }
        return movieDatabase.findMoviesByTitlePrefix(prefix, maxLimit);
    }
}
