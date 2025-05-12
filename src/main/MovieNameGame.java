package main;

import main.controller.AutocompleteController;
import main.controller.GameController;
import main.model.MovieDatabase;
import main.view.TerminalGameUI;

import java.io.IOException;

/**
 * Main application class for the Movie Name Game.
 * Initializes game components and starts the terminal-based UI.
 */
public class MovieNameGame {
    private static final String MOVIE_FILE_PATH   = "data/tmdb_5000_movies.csv";
    private static final String CREDITS_FILE_PATH = "data/tmdb_5000_credits.csv";

    private final MovieDatabase movieDatabase;
    private final GameController gameController;
    private final AutocompleteController autocompleteController;

    /**
     * Constructor: 初始化模型和控制器，并加载数据。
     */
    public MovieNameGame() {
        movieDatabase = new MovieDatabase();
        gameController = new GameController(movieDatabase);
        autocompleteController = new AutocompleteController(movieDatabase);

        // 加载电影与演员表数据
        gameController.initialize(MOVIE_FILE_PATH, CREDITS_FILE_PATH);

        // 正确方式: 游戏初始状态为等待玩家
        // 将这行移到 GameController 的初始化中，它已经在构造函数中创建了 GameState
        // 不需要额外设置
    }

    /**
     * 启动 TUI 界面，接管整个游戏流程。
     */
    public void start() throws IOException {
        TerminalGameUI tui = new TerminalGameUI(gameController, autocompleteController);
        tui.run();
    }

    /**
     * 程序入口。
     */
    public static void main(String[] args) throws IOException {
        MovieNameGame app = new MovieNameGame();
        app.start();
    }
}