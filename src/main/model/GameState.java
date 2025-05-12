package main.model;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Represents the game state and manages game logic and rules.
 */
public class GameState {
    public enum State {
        WAITING_FOR_PLAYERS,
        SETTING_WIN_CONDITIONS,
        PLAYING,
        GAME_OVER
    }

    private State currentState;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Movie currentMovie;
    private int roundCount;
    private Map<String, Integer> personConnectionCounts;
    private boolean DEBUG_MODE = false;

    // 添加电影历史和连接方式的跟踪
    private List<MovieConnection> movieHistory;

    // 电影连接类，存储两部电影之间的连接信息
    public static class MovieConnection {
        private Movie movie;
        private String connectionType; // "genre", "actor", "director", etc.
        private String connectionValue; // 具体的连接值，如 "Action" 或 "Tom Hanks"

        public MovieConnection(Movie movie, String connectionType, String connectionValue) {
            this.movie = movie;
            this.connectionType = connectionType;
            this.connectionValue = connectionValue;
        }

        public Movie getMovie() {
            return movie;
        }

        public String getConnectionType() {
            return connectionType;
        }

        public String getConnectionValue() {
            return connectionValue;
        }
    }

    public GameState() {
        currentState = State.WAITING_FOR_PLAYERS;
        roundCount = 0;
        personConnectionCounts = new HashMap<>();
        movieHistory = new ArrayList<>(); // 初始化电影历史列表
    }

    /**
     * Add a player (up to 2 players).
     */
    public boolean addPlayer(String name) {
        if (player1 == null) {
            player1 = new Player(name);
            return true;
        } else if (player2 == null) {
            player2 = new Player(name);
            return true;
        }
        return false;
    }

    /**
     * Start a new game: only allowed when in SETTING_WIN_CONDITIONS.
     */
    public void startGame(Movie startingMovie) {
        if (currentState != State.SETTING_WIN_CONDITIONS) {
            throw new IllegalStateException(
                    "Cannot start game, current state: " + currentState);
        }
        this.currentMovie = startingMovie;
        this.roundCount = 1;
        this.currentPlayer = player1;
        player1.reset();
        player2.reset();
        personConnectionCounts.clear();
        movieHistory.clear(); // 清空电影历史

        // 添加第一部电影到历史中，没有连接信息
        movieHistory.add(new MovieConnection(startingMovie, null, null));

        this.currentState = State.PLAYING;
    }

    /**
     * Player selects the next movie with a given connection.
     *
     * @param movie           the movie chosen
     * @param connectionType  "genre" or one of "actor", "director", "writer", "composer"
     * @param connectionValue for genre: the genre string; for person: the person's name
     * @return true if the move was valid and applied, false otherwise
     */
    public boolean selectMovie(Movie movie, String connectionType, String connectionValue) {
        if (currentState != State.PLAYING) return false;
        // Prevent reuse
        if ((player1.getSelectedMovies().contains(movie)) ||
                (player2.getSelectedMovies().contains(movie))) {
            return false;
        }

        boolean validConnection = false;
        boolean shouldIncrement = false;
        String actualConnectionValue = connectionValue;

        if ("genre".equals(connectionType)) {
            // 尝试找到共同的流派
            List<String> commonGenres = new ArrayList<>(currentMovie.getGenres());
            commonGenres.retainAll(movie.getGenres());

            if (!commonGenres.isEmpty()) {
                validConnection = true;
                shouldIncrement = true;
                // 使用第一个共同流派作为连接值
                actualConnectionValue = commonGenres.get(0);
            }
        } else if ("person".equals(connectionType)) {
            // 自动检测共同的人物
            if (connectionValue == null) {
                // 寻找共同演员
                List<String> actorsA = currentMovie.getActors();
                List<String> actorsB = movie.getActors();
                for (String actor : actorsA) {
                    if (actorsB.contains(actor)) {
                        actualConnectionValue = actor;
                        connectionType = "actor";
                        validConnection = true;
                        break;
                    }
                }

                // 如果没找到共同演员，检查导演
                if (!validConnection) {
                    List<String> directorsA = currentMovie.getDirectors();
                    List<String> directorsB = movie.getDirectors();
                    for (String director : directorsA) {
                        if (directorsB.contains(director)) {
                            actualConnectionValue = director;
                            connectionType = "director";
                            validConnection = true;
                            break;
                        }
                    }
                }

                // 如果仍未找到，检查编剧
                if (!validConnection) {
                    List<String> writersA = currentMovie.getWriters();
                    List<String> writersB = movie.getWriters();
                    for (String writer : writersA) {
                        if (writersB.contains(writer)) {
                            actualConnectionValue = writer;
                            connectionType = "writer";
                            validConnection = true;
                            break;
                        }
                    }
                }

                // 最后检查作曲家
                if (!validConnection) {
                    List<String> composersA = currentMovie.getComposers();
                    List<String> composersB = movie.getComposers();
                    for (String composer : composersA) {
                        if (composersB.contains(composer)) {
                            actualConnectionValue = composer;
                            connectionType = "composer";
                            validConnection = true;
                            break;
                        }
                    }
                }
            } else {
                // 验证特定的人物连接
                validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);
            }

            if (validConnection) {
                String key = connectionType + ":" + actualConnectionValue;
                int used = personConnectionCounts.getOrDefault(key, 0);
                if (used < 3) {
                    personConnectionCounts.put(key, used + 1);
                    shouldIncrement = true;
                } else {
                    validConnection = false;
                }
            }
        } else {
            // 验证其他类型的连接
            validConnection = verifyConnection(currentMovie, movie, connectionType, connectionValue);
            if (validConnection) {
                String key = connectionType + ":" + connectionValue;
                int used = personConnectionCounts.getOrDefault(key, 0);
                if (used < 3) {
                    personConnectionCounts.put(key, used + 1);
                    shouldIncrement = true;
                } else {
                    validConnection = false;
                }
            }
        }

        if (!validConnection) return false;

        // 应用这一步
        currentPlayer.addSelectedMovie(movie);
        if (shouldIncrement) currentPlayer.incrementProgress();

        // 将这部电影添加到历史中
        movieHistory.add(new MovieConnection(movie, connectionType, actualConnectionValue));

        currentMovie = movie;

        // 检查胜利
        if (currentPlayer.hasWon()) {
            currentState = State.GAME_OVER;
        } else {
            roundCount++;
        }
        return true;
    }

    /**
     * 检查人物连接（actor/director/writer/composer）。
     */
    public boolean verifyConnection(
            Movie from, Movie to,
            String connectionType, String personName) {

        List<String> fromList;
        List<String> toList;
        switch (connectionType) {
            case "actor":
                fromList = from.getActors();    toList = to.getActors();    break;
            case "director":
                fromList = from.getDirectors(); toList = to.getDirectors(); break;
            case "writer":
                fromList = from.getWriters();   toList = to.getWriters();   break;
            case "composer":
                fromList = from.getComposers(); toList = to.getComposers(); break;
            default:
                return false;
        }
        return fromList.contains(personName) && toList.contains(personName);
    }

    /**
     * 获取最近的电影历史（最多limit部）
     */
    public List<MovieConnection> getRecentMovieHistory(int limit) {
        int size = movieHistory.size();
        int start = Math.max(0, size - limit);
        return new ArrayList<>(movieHistory.subList(start, size));
    }

    /* ------- 一系列 Getter / Setter / Helper ------- */

    public State getCurrentState() { return currentState; }

    public Player getPlayer1() { return player1; }

    public Player getPlayer2() { return player2; }

    public Player getCurrentPlayer() { return currentPlayer; }

    public Movie getCurrentMovie() { return currentMovie; }

    public int getRoundCount() { return roundCount; }

    public void setState(State state) { this.currentState = state; }

    public void setCurrentPlayer(Player p) { this.currentPlayer = p; }

    /** 切换到下一个玩家 */
    public void switchToNextPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
}