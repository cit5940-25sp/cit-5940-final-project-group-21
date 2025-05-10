
package main.controller;

import main.model.Movie;
import main.model.MovieDatabase;
import main.view.AutocompleteView;

import java.util.List;

/**
 * Controller for the autocomplete functionality.
 * Manages user input and suggestions for movie titles.
 */
public class AutocompleteController {
    private MovieDatabase movieDatabase;
    private AutocompleteView autocompleteView;
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
     * Set the autocomplete view
     *
     * @param autocompleteView Autocomplete view
     */
    public void setAutocompleteView(AutocompleteView autocompleteView) {
        this.autocompleteView = autocompleteView;
    }

    /**
     * Handle user input for autocomplete
     *
     * @param prefix Current user input prefix
     */
    public void handleUserInput(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            autocompleteView.clearSuggestions();
            return;
        }

        List<Movie> suggestions = getSuggestions(prefix);
        autocompleteView.updateSuggestions(suggestions);
    }

    /**
     * Get movie title suggestions based on the prefix
     *
     * @param prefix Title prefix
     * @return List of matching movies
     */
    public List<Movie> getSuggestions(String prefix) {
        return movieDatabase.findMoviesByTitlePrefix(prefix, MAX_SUGGESTIONS);
    }

    /**
     * Handle selection of a suggestion
     *
     * @param selectedMovie Selected movie
     */
    public void handleSuggestionSelected(Movie selectedMovie) {
        autocompleteView.notifyMovieSelected(selectedMovie);
    }
}