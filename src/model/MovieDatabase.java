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
 * Movie database class responsible for loading and managing movie data.
 * Provides movie search and filtering capabilities.
 */
public class MovieDatabase {
    private List<Movie> movies;
    private Map<String, List<Movie>> genreIndex;
    private Map<String, List<Movie>> titlePrefixIndex;
    private boolean DEBUG_MODE = false; // 设置为false以关闭调试信息

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
    public void loadMoviesFromCSV(String filePath) throws IOException, CsvValidationException {
        movies.clear();
        genreIndex.clear();
        titlePrefixIndex.clear();

        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] line;
            reader.readNext(); // skip header

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
                    if (DEBUG_MODE) {
                        System.err.println("Error parsing line: " + Arrays.toString(line));
                        e.printStackTrace();
                    }
                }
            }
        }

        if (DEBUG_MODE) {
            System.out.println("Loaded " + movies.size() + " movies");
            System.out.println("Genres loaded: " + genreIndex.keySet());
        }
    }

    /**
     * Parse the genres JSON field
     *
     * @param genresJson JSON string of genres field
     * @return List of genre names
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
            if (DEBUG_MODE) {
                System.err.println("Failed to parse genres: " + genresJson);
            }
        }
        return genres;
    }

    /**
     * Load credits from CSV with CSVReader
     *
     * @param filePath Credits CSV file path
     * @throws IOException If file reading fails
     */
    public void loadCreditsFromCSV(String filePath) throws IOException {
        if (DEBUG_MODE) {
            System.out.println("DEBUG: Starting to load credits from " + filePath);
        }

        Map<Integer, Movie> movieMap = movies.stream()
                .collect(Collectors.toMap(Movie::getId, m -> m));

        if (DEBUG_MODE) {
            System.out.println("DEBUG: Created movie map with " + movieMap.size() + " movies");
        }

        int processed = 0;
        int successful = 0;
        int errorCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue; // Skip header
                }

                processed++;

                try {
                    // 简单的CSV解析 - 更健壮的方式
                    String[] parts = parseCSVLine(line);

                    if (parts.length < 4) {
                        errorCount++;
                        continue;
                    }

                    int movieId = Integer.parseInt(parts[0]);
                    Movie movie = movieMap.get(movieId);
                    if (movie == null) {
                        continue;
                    }

                    // Parse cast (column 2)
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
                            if (DEBUG_MODE) {
                                System.out.println("DEBUG: JSON error for cast: " + e.getMessage());
                            }
                        }
                    }

                    // Parse crew (column 3)
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
                                    case "Director" -> directors.add(name);
                                    case "Writer" -> writers.add(name);
                                    case "Original Music Composer" -> composers.add(name);
                                }
                            }

                            movie.setDirectors(directors);
                            movie.setWriters(writers);
                            movie.setComposers(composers);

                            successful++;
                        } catch (JSONException e) {
                            errorCount++;
                            if (DEBUG_MODE) {
                                System.out.println("DEBUG: JSON error for crew: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    errorCount++;
                    if (DEBUG_MODE) {
                        System.err.println("DEBUG: Error processing line " + processed + ": " + e.getMessage());
                    }
                }
            }
        }

        if (DEBUG_MODE) {
            System.out.println("DEBUG: Finished loading credits");
            System.out.println("DEBUG: Processed " + processed + " lines");
            System.out.println("DEBUG: Successfully processed " + successful + " movies");
            System.out.println("DEBUG: Errors: " + errorCount);
        }
    }

    /**
     * 简单的CSV行解析方法，更健壮地处理引号
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

        // 添加最后一个字段
        result.add(current.toString());

        return result.toArray(new String[0]);
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
}