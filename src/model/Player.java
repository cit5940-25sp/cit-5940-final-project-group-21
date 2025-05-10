package main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the game.
 * Contains player's name, selected movies, and win conditions.
 */
public class Player {
    private String name;
    private List<Movie> selectedMovies;
    private WinCondition winCondition;
    private int winProgress;

    /**
     * Constructor for the Player class
     *
     * @param name Player's name
     */
    public Player(String name) {
        this.name = name;
        this.selectedMovies = new ArrayList<>();
        this.winCondition = null;
        this.winProgress = 0;
    }

    /**
     * Get the player's name
     *
     * @return Player's name
     */
    public String getName() {
        return name;
    }

    /**
     * Set player's win condition
     *
     * @param winCondition Win condition
     */
    public void setWinCondition(WinCondition winCondition) {
        this.winCondition = winCondition;
        this.winProgress = 0;
    }

    /**
     * Get player's win condition
     *
     * @return Win condition
     */
    public WinCondition getWinCondition() {
        return winCondition;
    }

    /**
     * Add a selected movie
     *
     * @param movie Selected movie
     * @return true if added successfully, false if the movie was already selected
     */
    public boolean addSelectedMovie(Movie movie) {
        if (selectedMovies.contains(movie)) {
            return false;
        }

        selectedMovies.add(movie);

        // Check if it meets win condition
        if (winCondition != null && winCondition.checkMovie(movie)) {
            winProgress++;
        }

        return true;
    }

    /**
     * Get all movies selected by the player
     *
     * @return List of selected movies
     */
    public List<Movie> getSelectedMovies() {
        return new ArrayList<>(selectedMovies);
    }

    /**
     * Get the N most recently selected movies
     *
     * @param count Number of movies to retrieve
     * @return List of recent movies
     */
    public List<Movie> getRecentMovies(int count) {
        int size = selectedMovies.size();
        int startIndex = Math.max(0, size - count);
        return new ArrayList<>(selectedMovies.subList(startIndex, size));
    }

    /**
     * Check if player has met win condition
     *
     * @return true if player has won, false otherwise
     */
    public boolean hasWon() {
        if (winCondition == null) {
            return false;
        }
        return winProgress >= winCondition.getRequiredCount();
    }

    /**
     * Get current win progress
     *
     * @return Current progress
     */
    public int getWinProgress() {
        return winProgress;
    }

    /**
     * Reset player state
     */
    public void reset() {
        selectedMovies.clear();
        winProgress = 0;
    }

    /**
     * Abstract win condition class
     */
    public static abstract class WinCondition {
        private String description;
        private int requiredCount;

        /**
         * Constructor
         *
         * @param description Condition description
         * @param requiredCount Number of movies needed to satisfy condition
         */
        public WinCondition(String description, int requiredCount) {
            this.description = description;
            this.requiredCount = requiredCount;
        }

        /**
         * Get condition description
         *
         * @return Condition description
         */
        public String getDescription() {
            return description;
        }

        /**
         * Get required movie count
         *
         * @return Required count
         */
        public int getRequiredCount() {
            return requiredCount;
        }

        /**
         * Check if movie satisfies condition
         *
         * @param movie Movie to check
         * @return true if satisfied, false otherwise
         */
        public abstract boolean checkMovie(Movie movie);
    }

    /**
     * Genre-based win condition
     */
    public static class GenreWinCondition extends WinCondition {
        private String genre;

        /**
         * Constructor
         *
         * @param genre Target genre
         * @param count Required movie count
         */
        public GenreWinCondition(String genre, int count) {
            super("Need to name " + count + " " + genre + " movies", count);
            this.genre = genre;
        }

        /**
         * Role-based person win condition (e.g., actor, director, etc.)
         */
        public static class PersonWinCondition extends WinCondition {
            private String role;  // "actor", "director", "writer", "composer"
            private String personName;

            public PersonWinCondition(String role, String personName, int count) {
                super("Need to name " + count + " movies with " + role + ": " + personName, count);
                this.role = role;
                this.personName = personName;
            }

            @Override
            public boolean checkMovie(Movie movie) {
                List<String> people = switch (role) {
                    case "actor" -> movie.getActors();
                    case "director" -> movie.getDirectors();
                    case "writer" -> movie.getWriters();
                    case "composer" -> movie.getComposers();
                    default -> new ArrayList<>();
                };
                return people.contains(personName);
            }

            public String getRole() {
                return role;
            }

            public String getPersonName() {
                return personName;
            }
        }


        @Override
        public boolean checkMovie(Movie movie) {
            return movie.hasGenre(genre);
        }

        /**
         * Get target genre
         *
         * @return Target genre
         */
        public String getGenre() {
            return genre;
        }
    }


}
