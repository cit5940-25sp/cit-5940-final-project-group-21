package main.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.*;


/**
 * Movie database class responsible for loading and managing movie data.
 * Provides movie search and filtering capabilities.
 */
public class MovieDatabase {
    private List<Movie> movies;
    private Map<String, List<Movie>> genreIndex;
    private Map<String, List<Movie>> titlePrefixIndex;

    /**
     * Constructor to initialize the movie database
     */
    public MovieDatabase() {
        movies = new ArrayList<>();
        genreIndex = new HashMap<>();
        titlePrefixIndex = new HashMap<>();
    }

    /**
     * Load movie data from a CSV file
     *
     * @param filePath CSV file path
     * @throws IOException If file reading fails
     */
    public void loadMoviesFromCSV(String filePath) throws IOException {
        movies.clear();
        genreIndex.clear();
        titlePrefixIndex.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            // Skip header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                try {
                    Movie movie = parseMovieLine(line);
                    if (movie != null) {
                        movies.add(movie);

                        // Update genre index
                        for (String genre : movie.getGenres()) {
                            genreIndex.computeIfAbsent(genre, k -> new ArrayList<>()).add(movie);
                        }

                        // Update title prefix index
                        String title = movie.getTitle().toLowerCase();
                        for (int i = 1; i <= Math.min(5, title.length()); i++) {
                            String prefix = title.substring(0, i);
                            titlePrefixIndex.computeIfAbsent(prefix, k -> new ArrayList<>()).add(movie);
                        }
                    }
                } catch (Exception e) {
                    // Skip lines with parsing errors
                    System.err.println("Error parsing line: " + line);
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Loaded " + movies.size() + " movies");
    }

    /**
     * Parse a CSV line and create a Movie object
     *
     * @param line CSV line
     * @return Movie object, or null if parsing fails
     */
    private Movie parseMovieLine(String line) {
        // Simple CSV parsing, doesn't handle commas within quotes
        // A real project should use a robust CSV parser
        String[] parts = line.split(",");
        if (parts.length < 19) return null;

        int id = Integer.parseInt(parts[3]);
        String title = parts[17];
        String releaseDate = parts[11];

        // Parse genres JSON field
        String genresJson = parts[1];
        List<String> genres = parseGenres(genresJson);

        String overview = parts[7];
        double voteAverage = Double.parseDouble(parts[18]);

        return new Movie(id, title, releaseDate, genres, overview, voteAverage);
    }

    /**
     * Parse the genres JSON field
     *
     * @param genresJson JSON string of genres field
     * @return List of genre names
     */
    private List<String> parseGenres(String genresJson) {
        List<String> genres = new ArrayList<>();

        // Simplified JSON parsing
        // A real project should use a JSON library
        if (genresJson != null && !genresJson.isEmpty()) {
            String[] genreParts = genresJson.split("\"name\":\\s*\"");
            for (int i = 1; i < genreParts.length; i++) {
                int endIndex = genreParts[i].indexOf("\"");
                if (endIndex > 0) {
                    genres.add(genreParts[i].substring(0, endIndex));
                }
            }
        }

        return genres;
    }

    /**
     * Get all movies in the database
     *
     * @return List of all movies
     */
    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    /**
     * Find movie by ID
     *
     * @param id Movie ID
     * @return Found movie, or null if not found
     */
    public Movie findMovieById(int id) {
        for (Movie movie : movies) {
            if (movie.getId() == id) {
                return movie;
            }
        }
        return null;
    }

    /**
     * Find movies by title
     *
     * @param title Movie title
     * @return List of movies matching the title
     */
    public List<Movie> findMoviesByTitle(String title) {
        String searchTitle = title.toLowerCase();
        return movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(searchTitle))
                .collect(Collectors.toList());
    }

    /**
     * Find movies by title prefix (for autocomplete)
     *
     * @param prefix Title prefix
     * @param maxResults Maximum number of results
     * @return List of movies matching the prefix
     */
    public List<Movie> findMoviesByTitlePrefix(String prefix, int maxResults) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>();
        }

        String searchPrefix = prefix.toLowerCase();
        List<Movie> results = titlePrefixIndex.getOrDefault(searchPrefix.substring(0, Math.min(searchPrefix.length(), 5)), new ArrayList<>())
                .stream()
                .filter(m -> m.getTitle().toLowerCase().startsWith(searchPrefix))
                .limit(maxResults)
                .collect(Collectors.toList());

        return results;
    }

    /**
     * Find movies by genre
     *
     * @param genre Movie genre
     * @return List of movies in that genre
     */
    public List<Movie> findMoviesByGenre(String genre) {
        return genreIndex.getOrDefault(genre, new ArrayList<>());
    }

    /**
     * Get all genres in the database
     *
     * @return Set of all genres
     */
    public Set<String> getAllGenres() {
        return new HashSet<>(genreIndex.keySet());
    }

    /**
     * Get a random movie
     *
     * @return Randomly selected movie
     */
    public Movie getRandomMovie() {
        if (movies.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * movies.size());
        return movies.get(randomIndex);
    }

    //for information regarding identity: actors/writers/directors/compositors

    public void loadCreditsFromCSV(String filePath) throws IOException {
        Map<Integer, Movie> movieMap = movies.stream()
                .collect(Collectors.toMap(Movie::getId, m -> m));

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line = reader.readLine(); // skip header

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 4);
                if (parts.length < 4) continue;

                int movieId = Integer.parseInt(parts[0]);
                Movie movie = movieMap.get(movieId);
                if (movie == null) continue;

                try {
                    JSONArray castArray = new JSONArray(parts[2]);
                    List<String> actors = new ArrayList<>();
                    for (int i = 0; i < Math.min(castArray.length(), 5); i++) {
                        JSONObject castMember = castArray.getJSONObject(i);
                        actors.add(castMember.getString("name"));
                    }
                    movie.setActors(actors);

                    JSONArray crewArray = new JSONArray(parts[3]);
                    List<String> directors = new ArrayList<>();
                    List<String> writers = new ArrayList<>();
                    List<String> composers = new ArrayList<>();

                    for (int i = 0; i < crewArray.length(); i++) {
                        JSONObject crewMember = crewArray.getJSONObject(i);
                        String job = crewMember.getString("job");
                        String name = crewMember.getString("name");
                        switch (job) {
                            case "Director" -> directors.add(name);
                            case "Writer" -> writers.add(name);
                            case "Original Music Composer" -> composers.add(name);
                        }
                    }

                    movie.setDirectors(directors);
                    movie.setWriters(writers);
                    movie.setComposers(composers);
                } catch (JSONException e) {
                    System.err.println("JSON error for movie ID: " + movieId);
                }
            }
        }
    }

}
