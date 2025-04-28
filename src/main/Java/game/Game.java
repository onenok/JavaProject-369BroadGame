package game;
import java.util.Arrays;
import java.util.Random;

import javax.swing.SwingWorker;

import game.ai.GameAI;
import game.gui.GameGUI;
import myutil.Localization;
import myutil.PrintUtils;
import myutil.PrintUtils.PEnd;
import myutil.PrintUtils.PVo;

@SuppressWarnings("unused")
public final class Game {
    private Board board;
    private GameAI gameAI1;
    private GameAI gameAI2;
    private int currentPlayer; // 1 or 2
    private final int[] isBot; // whether player is bot
    private int[] playerScores; // player scores
    private boolean gameOver;
    private int winner;
    private final boolean isGUI;
    private final int BOARD_SIZE = 9;
    private GameGUI gui;

    public Game(boolean isGUI, boolean isAutoPlay) {
        board = new Board(BOARD_SIZE);
        gameAI1 = null;
        gameAI2 = null;
        playerScores = new int[2];
        gameOver = false;
        currentPlayer = new Random().nextInt(2) + 1;
        this.isGUI = isGUI;
        isBot = new int[2];
        if (isAutoPlay) {
            play();
        }
    }
    
    public Game(boolean isGUI, GameAI gameAI1, GameAI gameAI2, boolean isAutoPlay) {
        board = new Board(BOARD_SIZE);
        this.gameAI1 = gameAI1;
        this.gameAI2 = gameAI2;
        playerScores = new int[2];
        gameOver = false;
        currentPlayer = new Random().nextInt(2) + 1;
        this.isGUI = isGUI;
        isBot = new int[2];
        if (isAutoPlay) {
            play();
        }
    }

    public Game(boolean isGUI, GameAI gameAI1, GameAI gameAI2, GameGUI gui, boolean isAutoPlay) {
        board = new Board(BOARD_SIZE);
        this.gameAI1 = gameAI1;
        this.gameAI2 = gameAI2;
        this.gui = gui;
        playerScores = new int[2];
        gameOver = false;
        currentPlayer = new Random().nextInt(2) + 1;
        this.isGUI = isGUI;
        isBot = new int[2];
        if (isAutoPlay) {
            play();
        }
    }

    public void initGame() {
        board = new Board(BOARD_SIZE);
        if (gameAI1 != null) {
            gameAI1.checkGame(this);
        }
        if (gameAI2 != null) {
            gameAI2.checkGame(this);
        }
        playerScores = new int[2];
        gameOver = false;
        currentPlayer = new Random().nextInt(2) + 1;
    }

    public void play() {
        if (isGUI) {
            guiPlay();
        } else {
            // Console mode game loop
            consolePlay();
            while (true) {
                String isPlay = PrintUtils.input(Localization.getString("game.play_again"));/*"是否再來一局?(true:是, false:否)"*/
                if (!isPlay.equals("y")) {
                    break;
                }
                Game game = new Game(false, (gameAI1 != null) ? gameAI1 : null, (gameAI2 != null) ? gameAI2 : null, true);
            }
            PrintUtils.print(Localization.getString("game.thank_you"), new PVo(10)); /*"感謝你的游玩。"*/
            PrintUtils.print(Localization.getString("game.see_you_next_time"), new PVo(10)); /*"下次見~"*/
        }
    }    
    
    public void guiPlay() {
        gui = new GameGUI(this);
    }
    public void setBot(int player, int difficulty) {
        if (player == 1) {
            if (gameAI1 == null || gameAI1.difficulty != difficulty) {  
                gameAI1 = new GameAI(this, difficulty, 1);
            }
            isBot[0] = 1;
        } else {
            if (gameAI2 == null || gameAI2.difficulty != difficulty) {
                gameAI2 = new GameAI(this, difficulty, 2);
            }
            isBot[1] = 1;
        }
    }
    @SuppressWarnings("CallToPrintStackTrace")
    public void consolePlay() {
        int mode = PrintUtils.inputAs(Integer.class, Localization.getString("game.choose_mode")).get(0);
        if (mode == 1) {
            int diff1;
            while (true) {
                diff1 = PrintUtils.inputAs(Integer.class, Localization.getString("game.choose_difficulty", "")).get(0);
                if (diff1 == 1 || diff1 == 2 || diff1 == 3) {
                    break;  
                }
            }
            setBot(1, diff1);
        } 
        else if (mode == 3) {
            int diff1;
            while (true) {
                diff1 = PrintUtils.inputAs(Integer.class, Localization.getString("game.choose_difficulty", 1)).get(0);
                if (diff1 == 1 || diff1 == 2 || diff1 == 3) {
                    break;
                }   
            }   
            int diff2;
            while (true) {
                diff2 = PrintUtils.inputAs(Integer.class, Localization.getString("game.choose_difficulty", 2)).get(0);
                if (diff2 == 1 || diff2 == 2 || diff2 == 3) {
                    break;
                }
            }
            setBot(1, diff1);
            setBot(2, diff2);
        }
        while (!gameOver) {
            // Display current game board status
            displayGameStatus();
            // Check if game is over (is the board full)
            if (isBoardFull()) {
                gameOver = true;
                break;
            }
            int[] move;
            // Get player input
            if (isBot(currentPlayer)) {
                if (currentPlayer == 1) {
                    move = gameAI1.getBotMove(false);
                } else {
                    move = gameAI2.getBotMove(false);
                }
                try {
                    Thread.sleep(700);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            else {
                move = getPlayerMove();
            }
            int row = move[0];
            int col = move[1];

            // Process player move and calculate score
            int score = board.handleConsecutive(row, col, currentPlayer);
            playerScores[currentPlayer - 1] += score;

            // Switch players
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }

        // Display game result
        winner = maxScorePlayer();
        displayGameResult();
    }

    public int[] getPlayerMove() {
        while (true) {
            String input = PrintUtils.input(Localization.getString("game.input_coordinate", currentPlayer));
            // Remove all spaces and convert to uppercase
            input = input.replaceAll("\\s+", "").toUpperCase();

            // Check input format: letter followed by number
            if (!input.matches("[A-Z]\\d+")) {
                PrintUtils.print(Localization.getString("game.invalid_input"), new PVo(10));
                continue;
            }

            // Extract letter and number
            char letter = input.charAt(0);
            int number = Integer.parseInt(input.substring(1));

            // Check number range
            if (number < 1 || number > BOARD_SIZE || letter < 'A' || letter > (BOARD_SIZE - 1) + 'A') {
                PrintUtils.print(Localization.getString("game.input_out_of_range"), new PVo(10));
                continue;
            }

            // Convert to array index
            int col = letter - 'A';
            int row = number - 1;
            if (board.getCell(row, col) != 0) {
                if (board.getCell(row, col) == currentPlayer) {
                    PrintUtils.print(Localization.getString("game.already_placed"), new PVo(10));
                    continue;
                } else {
                    PrintUtils.print(Localization.getString("game.cannot_cover"), new PVo(10));
                    continue;
                }
            }
            return new int[] { row, col };
        }
    }

    public boolean isBoardFull() {
        return board.isFull();
    }

    public boolean isBot(int player) {
        return isBot[player - 1] == 1;
    }

    public void displayGameStatus() {
        // Display scores
        for (int i = 1; i <= 2; i++) {
            if (isBot(i)) {
                PrintUtils.print(Localization.getString("game.display_score_ai", (i == 1) ? "1(O)" : "2(X)", playerScores[i - 1]), new PEnd(""));
            } else {
                PrintUtils.print(Localization.getString("game.display_score", (i == 1) ? "1(O)" : "2(X)", playerScores[i - 1]), new PEnd(""));
            }
            System.out.print(" ");
        }
        System.out.println();
        // Display game board
        int[][] DBoard = board.getBoard();
        boolean[][] markedGrid = board.getMarkedGrid();
        // End of selection
        String output = "";
        for (int j = 0; j < DBoard[0].length; j++) {
            output += "   " + (char) ('A' + j);
        }
        output += "\n";
        output += " -";
        for (int j = 0; j < DBoard[0].length; j++) {
            output += "----";
        }
        output += "\n";
        for (int i = 0; i < DBoard.length; i++) {
            for (int j = -1; j < DBoard[i].length; j++) {
                if (j == -1) {
                    output += String.valueOf(i + 1) + '|';
                    continue;
                }
                int cell = DBoard[i][j];
                switch (cell) {
                    case 0 -> output += "   ";
                    case 1 -> {
                        if (markedGrid[i][j]) {
                            output += "*O*";
                        } else {
                            output += " O ";
                        }
                    }
                    case 2 -> {
                        if (markedGrid[i][j]) {
                            output += "*X*";
                        } else {
                            output += " X ";
                        }
                    }
                    case 4 -> {
                        output += "#O#";
                    }
                    case 5 -> {
                        output += "#X#";
                    }
                    default -> {
                    }
                }
                output += "|";
            }
            output += "\n";
            if (i < DBoard.length - 1) {
                output += " |";
                for (int j = 0; j < DBoard[i].length; j++) {
                    if (j < DBoard[i].length - 1) {
                        output += "---+";
                    } else {
                        output += "---|";
                    }
                }
            } else {
                output += " |";
                for (int j = 0; j < DBoard[i].length; j++) {
                    if (j < DBoard[i].length - 1) {
                        output += "----";
                    } else {
                        output += "---|";
                    }
                }
            }
            output += "\n";
        }
        PrintUtils.print(output);
    }

    public void displayGameResult() {
        if (winner == -1) {
            PrintUtils.print(Localization.getString("game.draw"), new PVo(10));
        } else {
            if (isBot(winner)) {
                PrintUtils.print(Localization.getString("game.winner_ai", (winner == 1) ? "1(O)" : "2(X)"), new PVo(10));
            } else {
                PrintUtils.print(Localization.getString("game.winner", (winner == 1) ? "1(O)" : "2(X)"), new PVo(10));
            }
        }
    }

    private int maxScorePlayer() {
        int maxScore = 0;
        int maxScorePlayer = -1;
        System.out.println(Localization.getString("game.win_playerScores") + Arrays.toString(playerScores));
        for (int i = 0; i < 2; i++) {
            if (playerScores[i] == maxScore) {
                maxScorePlayer = -1;
            } else if (playerScores[i] > maxScore) {
                maxScore = playerScores[i];
                maxScorePlayer = i+1;
            }
        }
        return maxScorePlayer;
    }

    public java.util.List<int[]> getEmpty() {
        return board.getEmpty();
    }

    // Functions for GUI

    // Make a move
    public boolean makeMove(int row, int col) {
        // Check if this position is already occupied
        if (board.getCell(row, col) != 0) {
            return false;
        }

        // Process the move and calculate score
        int score = board.handleConsecutive(row, col, currentPlayer);
        playerScores[currentPlayer - 1] += score;
        displayGameStatus();
        // Check if game is over
        if (isBoardFull()) {
            gameOver = true;
            winner = maxScorePlayer();
        }

        // Switch players
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        return true;
    }

    // Get current board state
    public int[][] getBoardState() {
        return board.getBoard();
    }

    // Get marked grid
    public boolean[][] getMarkedState() {
        return board.getMarkedGrid();
    }

    // Check if game is over
    public boolean isGameOver() {
        return gameOver;
    }

    // Get current player
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // Get player scores
    public int[] getPlayerScores() {
        return playerScores;
    }

    // Get winner
    public int getWinner() {
        return winner;
    }

    // Get directionally classified marked coordinates
    public java.util.Map<Integer, java.util.List<int[]>> getDirectionMarks() {
        return board.getDirectionMarks();
    }

    // Get scores for each direction
    public java.util.Map<Integer, Integer> getDirectionScores() {
        return board.getDirectionScores();
    }

    // Get middle position coordinates for specific direction marks
    public int[] getDirectionMiddlePosition(int direction) {
        return board.getDirectionMiddlePosition(direction);
    }

    // Get Board object
    public Board getBoard() {
        return board;
    }

    // Clear marks
    public void clearMarked() {
        board.clearMarks();
    }

    public void guiGetBotMove(int player) {
        if (player == 1) {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int[] move = gameAI1.getBotMove(true);
                    gui.handleButtonClick(move[0], move[1]);
                    return null;
                }
            }.execute();
            
        } else {
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    int[] move = gameAI2.getBotMove(true);
                    gui.handleButtonClick(move[0], move[1]);
                    return null;
                }
            }.execute();
        }
    }
    public GameAI getGameAI1() {
        return gameAI1;
    }
    public GameAI getGameAI2() {
        return gameAI2;
    }
    
    public int getScoreDiff(int player) {
        if (player == 1) {
            return playerScores[0] - playerScores[1];
        } else {
            return playerScores[1] - playerScores[0];
        }
    }
    public int getPlayerScore(int player) {
        return playerScores[player - 1];
    }
}