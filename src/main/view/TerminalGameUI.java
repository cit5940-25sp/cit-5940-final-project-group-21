package main.view;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import main.controller.GameController;
import main.controller.AutocompleteController;
import main.model.GameState;
import main.model.Movie;
import main.model.Player;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TerminalGameUI implements Runnable {
    private final Screen screen;
    private final GameController gameController;
    private final AutocompleteController autocompleteController;
    private List<Movie> suggestions = List.of();
    private String errorMessage = "";
    private int errorDisplayTimeRemaining = 0;

    // 输入缓冲与光标位置
    private StringBuilder currentInput = new StringBuilder();
    private int cursorPos = 0;
    private List<String> menuOptions = List.of();
    private int currentMenuSelection = 0;

    // 阶段机：0=P1 名字, 1=P1 类型, 2=P1 目标, 3=P2 名字, 4=P2 类型, 5=P2 目标
    private int setupPhase = 0;

    // 倒计时相关
    private volatile int secondsRemaining = GameController.TURN_TIME_SECONDS;
    private volatile boolean timerPaused = false;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TerminalGameUI(GameController gc, AutocompleteController ac) throws IOException {
        this.gameController = gc;
        this.autocompleteController = ac;

        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Terminal terminal = factory.createTerminalEmulator();
        this.screen = new TerminalScreen(terminal);
        screen.startScreen();
    }

    @Override
    public void run() {
        try {
            resetTimer();
            scheduler.scheduleAtFixedRate(() -> {
                if (!timerPaused && gameController.getGameState().getCurrentState() == GameState.State.PLAYING) {
                    if (secondsRemaining > 0) {
                        secondsRemaining--;
                    } else {
                        // Handle time up
                        handleTimeUp();
                    }

                    // Update error message timer
                    if (errorDisplayTimeRemaining > 0) {
                        errorDisplayTimeRemaining--;
                        if (errorDisplayTimeRemaining == 0) {
                            errorMessage = "";
                        }
                    }

                    try {
                        redrawScreen();
                    } catch (IOException ignored) {}
                }
            }, 1, 1, TimeUnit.SECONDS);

            while (true) {
                redrawScreen();

                // Use polling with timeout instead of blocking
                KeyStroke key = screen.pollInput();
                if (key != null) {
                    if (key.getKeyType() == KeyType.EOF || key.getKeyType() == KeyType.Escape) break;
                    dispatchKey(key);
                }

                // Handle timer expiration
                if (secondsRemaining <= 0 &&
                        gameController.getGameState().getCurrentState() == GameState.State.PLAYING) {
                    handleTimeUp();
                }

                // Short sleep to avoid excessive CPU usage
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scheduler.shutdownNow();
            try { screen.stopScreen(); } catch (IOException ignored) {}
        }
    }

    // Add this method to handle timer expiration
    private void handleTimeUp() {
        if (gameController.getGameState().getCurrentState() == GameState.State.PLAYING && secondsRemaining <= 0) {
            // Only handle time up once when it reaches zero
            gameController.handleTimeUp();
            // Reset timer for the next player (who just won by default)
            resetTimer();
        }
    }

    private void redrawScreen() throws IOException {
        screen.clear();
        GameState.State state = gameController.getGameState().getCurrentState();
        switch (state) {
            case WAITING_FOR_PLAYERS:
                redrawSetup();
                break;
            case SETTING_WIN_CONDITIONS:
                redrawReady();
                break;
            case PLAYING:
                redrawPlaying();
                break;
            case GAME_OVER:
                redrawGameOver();
                break;
            default:
        }
        screen.refresh();
    }

    private void redrawSetup() {
        TextGraphics tg = screen.newTextGraphics();
        int row = 1;
        tg.putString(0, 0, "======== MOVIE NAME GAME SETUP ========");

        switch (setupPhase) {
            case 0: // P1 名字
                tg.putString(0, row++, "Enter Player 1 name:");
                tg.putString(0, row++, "> " + currentInput);
                screen.setCursorPosition(new TerminalPosition(2 + cursorPos, row - 1));
                break;
            case 1: // P1 类型菜单
                Player p1 = gameController.getGameState().getPlayer1();
                tg.putString(0, row++, "Player 1: " + p1.getName());
                tg.putString(0, row++, "Select connection type:");
                menuOptions = List.of("Genre", "Person");
                for (int i = 0; i < menuOptions.size(); i++) {
                    String prefix = (i == currentMenuSelection) ? " > " : "   ";
                    tg.putString(0, row++, prefix + menuOptions.get(i));
                }
                break;
            case 2: // P1 目标数
                p1 = gameController.getGameState().getPlayer1();
                tg.putString(0, row++, "Player 1: " + p1.getName() + " (" + p1.getConnectionType() + ")");
                tg.putString(0, row++, "Enter target count (1-10):");
                tg.putString(0, row++, "> " + currentInput);
                screen.setCursorPosition(new TerminalPosition(2 + cursorPos, row - 1));
                break;
            case 3: // P2 名字
                p1 = gameController.getGameState().getPlayer1();
                tg.putString(0, row++, String.format("Player 1: %s - target %d", p1.getName(), p1.getTargetCount()));
                tg.putString(0, row++, "Enter Player 2 name:");
                tg.putString(0, row++, "> " + currentInput);
                screen.setCursorPosition(new TerminalPosition(2 + cursorPos, row - 1));
                break;
            case 4: // P2 类型菜单
                Player p2 = gameController.getGameState().getPlayer2();
                tg.putString(0, row++, "Player 2: " + p2.getName());
                tg.putString(0, row++, "Select connection type:");
                menuOptions = List.of("Genre", "Person");
                for (int i = 0; i < menuOptions.size(); i++) {
                    String prefix = (i == currentMenuSelection) ? " > " : "   ";
                    tg.putString(0, row++, prefix + menuOptions.get(i));
                }
                break;
            case 5: // P2 目标数
                p2 = gameController.getGameState().getPlayer2();
                tg.putString(0, row++, "Player 2: " + p2.getName() + " (" + p2.getConnectionType() + ")");
                tg.putString(0, row++, "Enter target count (1-10):");
                tg.putString(0, row++, "> " + currentInput);
                screen.setCursorPosition(new TerminalPosition(2 + cursorPos, row - 1));
                break;
        }
    }

    private void redrawReady() {
        TextGraphics tg = screen.newTextGraphics();
        int row = 1;
        tg.putString(0, 0, "======== GAME READY ========");
        Player p1 = gameController.getGameState().getPlayer1();
        Player p2 = gameController.getGameState().getPlayer2();
        tg.putString(0, row++, String.format("Player 1: %s (%s - %d)", p1.getName(), p1.getConnectionType(), p1.getTargetCount()));
        tg.putString(0, row++, String.format("Player 2: %s (%s - %d)", p2.getName(), p2.getConnectionType(), p2.getTargetCount()));
        tg.putString(0, row++, "");
        tg.putString(0, row++, "Press ENTER to start!");
    }

    private void redrawPlaying() {
        TextGraphics tg = screen.newTextGraphics();
        int row = 0;

        // 1. 绘制头部信息
        tg.putString(0, row++, "========================================");
        try {
            tg.putString(0, row++,
                    String.format(" ROUND %d | %s's TURN (%s - %d/%d)",
                            gameController.getGameState().getRoundCount(),
                            gameController.getCurrentPlayer().getName(),
                            gameController.getCurrentPlayer().getConnectionType().toUpperCase(),
                            gameController.getCurrentPlayer().getWinProgress(),
                            gameController.getCurrentPlayer().getTargetCount()
                    )
            );
        } catch (NullPointerException e) {
            tg.putString(0, row++, " Game not properly initialized!");
        }
        tg.putString(0, row++, "========================================");

        // 2. 显示当前电影，玩家需要连接到这个电影
        Movie currentMovie = gameController.getGameState().getCurrentMovie();
        if (currentMovie != null) {
            row++;
            tg.putString(0, row++, " Connect to: " + currentMovie.getTitle() + " (" + currentMovie.getReleaseYear() + ")");
            tg.putString(0, row++, " Genres: " + String.join(", ", currentMovie.getGenres()));

            // 显示主要演员和导演等，帮助玩家找到连接
            if (!currentMovie.getActors().isEmpty()) {
                tg.putString(0, row++, " Actors: " + String.join(", ",
                        currentMovie.getActors().subList(0, Math.min(3, currentMovie.getActors().size()))));
            }

            if (!currentMovie.getDirectors().isEmpty()) {
                tg.putString(0, row++, " Directors: " + String.join(", ", currentMovie.getDirectors()));
            }

            row++;
        }

        // 3. 显示最近五部电影历史
        tg.putString(0, row++, " --- Recent Movie History ---");
        List<GameState.MovieConnection> recentHistory = gameController.getGameState().getRecentMovieHistory(5);
        for (int i = 0; i < recentHistory.size(); i++) {
            GameState.MovieConnection mc = recentHistory.get(i);
            Movie m = mc.getMovie();

            String historyLine;
            if (i == 0) {
                // 第一部电影（起始电影）
                historyLine = String.format(" %d. %s (%s) - %s",
                        i + 1,
                        m.getTitle(),
                        m.getReleaseYear(),
                        String.join(", ", m.getGenres()));
            } else {
                // 后续电影，显示连接方式
                String connection = "via ";
                if ("genre".equals(mc.getConnectionType())) {
                    connection += "genre: " + mc.getConnectionValue();
                } else if (mc.getConnectionType() != null) {
                    connection += mc.getConnectionType() + ": " + mc.getConnectionValue();
                } else {
                    connection = "(starting movie)";
                }

                historyLine = String.format(" %d. %s (%s) - %s - %s",
                        i + 1,
                        m.getTitle(),
                        m.getReleaseYear(),
                        connection,
                        String.join(", ", m.getGenres()));
            }

            tg.putString(0, row++, historyLine);
        }

        row += 1;

        // 4. 显示游戏规则提示
        tg.putString(0, row++, " Name a movie connected by actor, director, writer, composer, or genre");

        // 5. 显示错误信息 (新增部分)
        if (!errorMessage.isEmpty()) {
            TextGraphics errorTg = screen.newTextGraphics();
            // 如果支持颜色，可以将错误消息设为红色
            // errorTg.setForegroundColor(TextColor.ANSI.RED);
            errorTg.putString(0, row++, " ERROR: " + errorMessage);
            row += 1; // 额外空间
        }

        // 6. 输入区域
        row = Math.max(row, 20); // 确保有足够空间
        tg.putString(0, row, "> " + currentInput.toString());
        screen.setCursorPosition(new TerminalPosition(2 + cursorPos, row));

        // 7. 自动完成建议
        row += 2;
        for (Movie m : suggestions) {
            tg.putString(2, row++, m.getTitle() + " (" + m.getReleaseYear() + ")");
        }

        // 8. 倒计时
        redrawTimer();
    }


    private void redrawGameOver() {
        TextGraphics tg = screen.newTextGraphics();
        tg.putString(0, 0, "========= GAME OVER =========");
        Player winner = gameController.getGameState().getPlayer1().hasWon()
                ? gameController.getGameState().getPlayer1()
                : gameController.getGameState().getPlayer2();
        tg.putString(0, 2, winner.getName() + " wins!");
    }


    private void dispatchKey(KeyStroke key) {
        GameState.State state = gameController.getGameState().getCurrentState();
        switch (state) {
            case WAITING_FOR_PLAYERS:
                // 在等待玩家阶段，用 setupPhase 来推进 P1/P2 名字、类型、目标数
                handleSetupKey(key);
                break;
            case SETTING_WIN_CONDITIONS:
                // 在“按 Enter 开始游戏”阶段，按回车真正调用 startGame(...)
                if (key.getKeyType() == KeyType.Enter) {
                    gameController.getGameState().startGame(
                            gameController.getRandomMovie()
                    );
                    secondsRemaining = GameController.TURN_TIME_SECONDS;
                }
                break;
            case PLAYING:
                // 游戏进行中，处理输入并更新补全建议
                handlePlayingKey(key);
                break;
            case GAME_OVER:
                // 游戏结束，按回车退出
                if (key.getKeyType() == KeyType.Enter) {
                    System.exit(0);
                }
                break;
        }
    }


    private void handlePlayingKey(KeyStroke key) {
        switch (key.getKeyType()) {
            case Character:
                // Clear any error message when user starts typing again
                errorMessage = "";
                errorDisplayTimeRemaining = 0;

                // 1. 在光标处插入字符
                currentInput.insert(cursorPos++, key.getCharacter());
                // 2. 调用 AutocompleteController 拿建议
                suggestions = autocompleteController.getSuggestions(currentInput.toString());
                break;

            case Backspace:
                // Clear any error message when user starts modifying input
                errorMessage = "";
                errorDisplayTimeRemaining = 0;

                if (cursorPos > 0) {
                    currentInput.deleteCharAt(--cursorPos);
                    // 删除字符后也要刷新建议
                    suggestions = autocompleteController.getSuggestions(currentInput.toString());
                }
                break;

            case Enter:
                String guess = currentInput.toString().trim();
                if (guess.isEmpty()) {
                    errorMessage = "Please enter a movie name";
                    errorDisplayTimeRemaining = 3; // Show error for 3 seconds
                    break;
                }

                // 根据当前连接类型调用对应的 select 方法
                boolean ok;
                if (gameController.getCurrentPlayer().getConnectionType().equals("genre")) {
                    ok = gameController.selectMovieByGenre(guess);
                } else {
                    ok = gameController.selectMovieByPersonAutoDetect(guess);
                }

                // 如果合法，清空输入、清空建议，界面会切换到下一个玩家
                if (ok) {
                    currentInput.setLength(0);
                    cursorPos = 0;
                    suggestions = List.of();
                    errorMessage = ""; // Clear any error message
                    errorDisplayTimeRemaining = 0;

                    // Reset timer for the next player
                    resetTimer();

                } else {
                    // Display appropriate error message
                    if (guess.length() < 3) {
                        errorMessage = "Movie name is too short";
                    } else if (suggestions.isEmpty()) {
                        errorMessage = "Movie not found in database";
                    } else {
                        errorMessage = "Invalid connection! Try a movie with a valid connection";
                    }
                    errorDisplayTimeRemaining = 3; // Show error for 3 seconds
                }
                break;

            // You can handle up/down arrows for suggestions...
            default:
                break;
        }
    }


    private void handleSetupKey(KeyStroke key) {
        switch (setupPhase) {
            case 0:
            case 2:
            case 3:
            case 5:
                if (key.getKeyType() == KeyType.Character) {
                    currentInput.insert(cursorPos++, key.getCharacter());
                } else if (key.getKeyType() == KeyType.Backspace && cursorPos > 0) {
                    currentInput.deleteCharAt(--cursorPos);
                } else if (key.getKeyType() == KeyType.Enter && currentInput.length() > 0) {
                    String s = currentInput.toString().trim();
                    switch (setupPhase) {
                        case 0:
                            gameController.getGameState().addPlayer(s);
                            break;
                        case 2:
                            gameController.getGameState().getPlayer1().setTargetCount(Integer.parseInt(s));
                            break;
                        case 3:
                            gameController.getGameState().addPlayer(s);
                            break;
                        case 5:
                            gameController.getGameState().getPlayer2().setTargetCount(Integer.parseInt(s));
                            break;
                    }
                    currentInput.setLength(0);
                    cursorPos = 0;
                    setupPhase++;
                }
                break;
            case 1:
            case 4:
                if (key.getKeyType() == KeyType.ArrowUp && currentMenuSelection > 0) {
                    currentMenuSelection--;
                } else if (key.getKeyType() == KeyType.ArrowDown && currentMenuSelection < menuOptions.size() - 1) {
                    currentMenuSelection++;
                } else if (key.getKeyType() == KeyType.Enter) {
                    String sel = menuOptions.get(currentMenuSelection).toLowerCase();
                    Player p = (setupPhase == 1)
                            ? gameController.getGameState().getPlayer1()
                            : gameController.getGameState().getPlayer2();
                    p.setConnectionType(sel);
                    currentMenuSelection = 0;
                    setupPhase++;
                }
                break;
        }
        if (setupPhase > 5) {
            gameController.getGameState().setState(GameState.State.SETTING_WIN_CONDITIONS);
        }
    }

    private void redrawTimer() {
        TextGraphics tg = screen.newTextGraphics();
        TerminalSize size = screen.getTerminalSize();
        String t = String.format("Time: %2ds", secondsRemaining);
        // 始终在第一行的最右侧显示
        tg.putString(size.getColumns() - t.length(), 0, t);
    }

    public void resetTimer() {
        secondsRemaining = GameController.TURN_TIME_SECONDS;
    }
}
