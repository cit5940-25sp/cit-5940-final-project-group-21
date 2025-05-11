
package main.view;

import main.model.Movie;
import main.controller.AutocompleteController;

import main.controller.GameController;

import java.util.List;

/**
 * View class for the autocomplete functionality.
 * Displays movie suggestions as user types.
 */
public class AutocompleteView {
    private AutocompleteController autocompleteController;
    private MovieSelectedListener movieSelectedListener;
    private GameController gameController;

    /**
     * Constructor for the AutocompleteView
     */
    public AutocompleteView() {
    }

    /**
     * Set the autocomplete controller
     *
     * @param autocompleteController Autocomplete controller
     */
    public void setAutocompleteController(AutocompleteController autocompleteController) {
        this.autocompleteController = autocompleteController;
    }

    /**
     * Set the game controller
     *
     * @param gameController Game controller
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }


    /**
     * Set the movie selected listener
     *
     * @param listener Movie selected listener
     */
    public void setMovieSelectedListener(MovieSelectedListener listener) {
        this.movieSelectedListener = listener;
    }

    /**
     * Update the displayed suggestions
     *
     * @param suggestions List of movie suggestions
     */
    public void updateSuggestions(List<Movie> suggestions) {
        clearDisplay();

        if (suggestions.isEmpty()) {
            System.out.println("No matching movies found");
            return;
        }

        System.out.println("\nSuggestions:");
        int i = 1;
        for (Movie movie : suggestions) {
            System.out.println(i + ". " + movie.getTitle() + " (" + movie.getReleaseYear() + ")");
            i++;
        }

        System.out.print("Select a movie (1-" + suggestions.size() + "): ");
    }

    /**
     * Clear the suggestions display
     */
    public void clearSuggestions() {
        clearDisplay();
    }

    /**
     * Clear the display area
     */
    private void clearDisplay() {
        // In a text-based UI, we would clear the console
        // But for simplicity, we'll just print a separator
        System.out.println("\n----------------------------------------");
    }

    /**
     * Handle user input in the autocomplete view
     *
     * @param input User input text
     */
    public void handleUserInput(String input) {
        autocompleteController.handleUserInput(input);
    }

    /**
     * Handle selection of a suggested movie
     *
     * @param index Index of the selected movie
     * @param suggestions Current list of suggestions
     */
    public void handleSelection(int index, List<Movie> suggestions) {
        if (index < 0 || index >= suggestions.size()) {
            System.out.println("Invalid selection");
            return;
        }

        Movie selectedMovie = suggestions.get(index);
        autocompleteController.handleSuggestionSelected(selectedMovie);
    }

    /**
     * Notify listeners that a movie has been selected
     *
     * @param movie Selected movie
     */
    public void notifyMovieSelected(Movie movie) {
        if (movieSelectedListener != null) {
            movieSelectedListener.onMovieSelected(movie);
        }
    }

    /**
     * Interface for movie selection listeners
     */
    public interface MovieSelectedListener {
        /**
         * Called when a movie is selected
         *
         * @param movie Selected movie
         */
        void onMovieSelected(Movie movie);
    }
}