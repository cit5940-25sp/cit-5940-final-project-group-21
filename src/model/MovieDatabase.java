package main.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import org.json.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

/**
 * Handles loading and indexing of movie data from TMDB datasets.
 * Supports search by genre, title, and prefix for autocomplete features.
 * Provides access to movie metadata and associated credits.
 *
 * @author Group 21
 * @version May 12, 2025
 */
public class MovieDatabase {
    private List<Movie> movies;
    private Map<String, List<Movie>> genreIndex;
    private Map<String, List<Movie>> titlePrefixIndex;
    private boolean debugMode = false;

    /**
     * Constructs a new, empty movie database.
     */
    public MovieDatabase() {
        movies = new ArrayList<>();
        genreIndex = new HashMap<>();
        titlePrefixIndex = new HashMap<>();
    }

    /**
     * Loads movie metadata from the given CSV file.
     *
     * @param filePath Path to the TMDB 5000 movies CSV file
     * @throws IOException if the file cannot be read
     * @throws CsvValidationException if CSV is malformed
     */
    public void loadMoviesFromCSV(String filePath) throws IOException, CsvValidationException {
        movies.clear();
        genreIndex.clear();
        titlePrefixIndex.clear();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext(); // Skip header

            while ((line = reader.readNext()) != null) {
                try {
                    int id = Integer.parseInt(line[3]);
                    String title = line[17];
                    String releaseDate = line[11];
                    String genresJson = line[1];
                    String overview = line[7];
                    double voteAverage = Double.parseDouble(line[18]);

                    List<String> genres = parseGenres(genresJson);
                    Movie movie = new Movie(id, title, releaseDate, genres, overview, voteAverage);
                    movies.add(movie);

                    for (String genre : genres) {
                        genreIndex.computeIfAbsent(genre, k -> new ArrayList<>()).add(movie);
                    }

                    String lowerTitle = title.toLowerCase();
                    for (int i = 1; i <= Math.min(5, lowerTitle.length()); i++) {
                        String prefix = lowerTitle.substring(0, i);
                        titlePrefixIndex.computeIfAbsent(prefix, k -> new ArrayList<>()).add(movie);
                    }
                } catch (Exception e) {
                    if (debugMode) {
                        System.err.println("Error parsing line: " + Arrays.toString(line));
                        e.printStackTrace();
                    }
                }
            }
        }

        if (debugMode) {
            System.out.println("Loaded " + movies.size() + " movies");
            System.out.println("Genres loaded: " + genreIndex.keySet());
        }
    }

    /**
     * Parses genre names from the JSON-formatted genre field.
     * The way to parse information from csv is suggested by ChatGPT.
     *
     * @param genresJson Raw JSON string containing genre objects
     * @return A list of genre names extracted from JSON
     */
    private List<String> parseGenres(String genresJson) {
        List<String> genres = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(genresJson);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.has("name")) {
                    genres.add(obj.getString("name"));
                }
            }
        } catch (Exception e) {
            if (debugMode) {
                System.err.println("Failed to parse genres: " + genresJson);
            }
        }
        return genres;
    }

    /**
     * Loads cast and crew data from a TMDB credits CSV file.
     * Updates existing movies with actor and crew information.
     *
     * @param filePath Path to the credits CSV file
     * @throws IOException if the file cannot be read
     */
    public void loadCreditsFromCSV(String filePath) throws IOException {
        if (debugMode) {
            System.out.println("DEBUG: Starting to load credits from " + filePath);
        }

        // Create a map from movie ID to Movie object for quick lookup
        Map<Integer, Movie> movieMap = movies.stream()
                .collect(Collectors.toMap(Movie::getId, m -> m));

        int processed = 0;
        int successful = 0;
        int errorCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip CSV header
                }

                processed++;

                try {
                    String[] parts = parseCSVLine(line);

                    if (parts.length < 4) {
                        errorCount++;
                        continue; // Skip malformed lines
                    }

                    int movieId = Integer.parseInt(parts[0]);
                    Movie movie = movieMap.get(movieId);
                    if (movie == null) {
                        continue; // Skip if movie not in our dataset
                    }

                    // Parse cast data and store top 5 actors
                    String castJson = parts[2];
                    if (castJson != null && !castJson.trim().isEmpty()) {
                        try {
                            JSONArray castArray = new JSONArray(castJson);
                            List<String> actors = new ArrayList<>();
                            for (int i = 0; i < Math.min(castArray.length(), 5); i++) {
                                JSONObject castMember = castArray.getJSONObject(i);
                                actors.add(castMember.getString("name"));
                            }
                            movie.setActors(actors);
                        } catch (JSONException e) {
                            errorCount++;
                            if (debugMode) {
                                System.out.println("DEBUG: JSON error for cast: " + e.getMessage());
                            }
                        }
                    }

                    // Parse crew data and categorize by job title
                    String crewJson = parts[3];
                    if (crewJson != null && !crewJson.trim().isEmpty()) {
                        try {
                            JSONArray crewArray = new JSONArray(crewJson);
                            List<String> directors = new ArrayList<>();
                            List<String> writers = new ArrayList<>();
                            List<String> composers = new ArrayList<>();

                            for (int i = 0; i < crewArray.length(); i++) {
                                JSONObject crewMember = crewArray.getJSONObject(i);
                                String job = crewMember.getString("job");
                                String name = crewMember.getString("name");
                                switch (job) {
                                    case "Director": {
                                        directors.add(name);
                                        break;
                                    }
                                    case "Writer": {
                                        writers.add(name);
                                        break;
                                    }
                                    case "Original Music Composer": {
                                        composers.add(name);
                                        break;
                                    }
                                    default:
                                        break;
                                }
                            }

                            // Assign crew data to the movie
                            movie.setDirectors(directors);
                            movie.setWriters(writers);
                            movie.setComposers(composers);

                            successful++;
                        } catch (JSONException e) {
                            errorCount++;
                            if (debugMode) {
                                System.out.println("DEBUG: JSON error for crew: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    if (debugMode) {
                        System.err.println("DEBUG: Error processing line "
                                + processed + ": " + e.getMessage());
                    }
                }
            }
        }

        if (debugMode) {
            System.out.println("DEBUG: Finished loading credits");
            System.out.println("DEBUG: Processed " + processed + " lines");
            System.out.println("DEBUG: Successfully processed " + successful + " movies");
            System.out.println("DEBUG: Errors: " + errorCount);
        }
    }

    /**
     * Parses a single CSV line with support for quoted strings.
     * CSV line parsing logic adapted from a common quoted-string handling
     * pattern suggested by ChatGPT.
     * @param line A raw CSV line
     * @return Array of fields split from the line
     */
    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Returns all movies in the database.
     *
     * @return List of all movies
     */
    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    /**
     * Finds a movie by its TMDB ID.
     *
     * @param id Movie ID
     * @return Movie if found, otherwise null
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
     * Searches for movies with titles containing the given string.
     *
     * @param title Partial or full movie title
     * @return List of matching movies
     */
    public List<Movie> findMoviesByTitle(String title) {
        String searchTitle = title.toLowerCase();
        return movies.stream()
                .filter(m -> m.getTitle().toLowerCase().contains(searchTitle))
                .collect(Collectors.toList());
    }

    /**
     * Searches for movies by title prefix (used in autocomplete).
     *
     * @param prefix Prefix of the movie title
     * @param maxResults Max number of suggestions to return
     * @return List of suggested movies
     */
    public List<Movie> findMoviesByTitlePrefix(String prefix, int maxResults) {
        if (prefix == null || prefix.isEmpty()) {
            return new ArrayList<>();
        }

        String searchPrefix = prefix.toLowerCase();
        List<Movie> results =
                titlePrefixIndex.getOrDefault(searchPrefix.substring(0,
                                        Math.min(searchPrefix.length(), 5)),
                                new ArrayList<>())
                .stream()
                .filter(m -> m.getTitle().toLowerCase().startsWith(searchPrefix))
                .limit(maxResults)
                .collect(Collectors.toList());

        return results;
    }

    /**
     * Retrieves all movies matching a given genre.
     *
     * @param genre Genre name
     * @return List of movies in that genre
     */
    public List<Movie> findMoviesByGenre(String genre) {
        return genreIndex.getOrDefault(genre, new ArrayList<>());
    }

    /**
     * Returns all unique genres found in the dataset.
     *
     * @return Set of genre strings
     */
    public Set<String> getAllGenres() {
        return new HashSet<>(genreIndex.keySet());
    }

    /**
     * Returns a random movie from the database.
     *
     * @return Random movie object, or null if no movies exist
     */
    public Movie getRandomMovie() {
        if (movies.isEmpty()) {
            return null;
        }
        int randomIndex = (int) (Math.random() * movies.size());
        return movies.get(randomIndex);
    }
}
