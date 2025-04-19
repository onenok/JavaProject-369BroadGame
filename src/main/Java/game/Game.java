package game;
import game.ai.GameAI;
import game.gui.GameGUI;
import java.util.Arrays;
import java.util.Random;
import javax.swing.SwingWorker;
import myutil.PrintUtils;
import myutil.PrintUtils.PVo;

@SuppressWarnings("unused")
public final class Game {
    private Board board;
    private GameAI gameAI1;
    private GameAI gameAI2;
    private int currentPlayer; // 1或2
    private final int[] isBot; // 玩家是否為bot
    private int[] playerScores; // 玩家得分
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
            // 控制台模式的遊戲循環
            consolePlay();
            while (true) {
                boolean isPlay = PrintUtils.inputAs(Boolean.class, "是否再來一局?(true:是, false:否)").get(0);
                if (!isPlay) {
                    break;
                }
                Game game = new Game(false, (gameAI1 != null) ? gameAI1 : null, (gameAI2 != null) ? gameAI2 : null, true);
            }
            PrintUtils.print("感謝你的游玩。", new PVo(10));
            PrintUtils.print("下次見~", new PVo(10));
        }
    }    
    
    public void guiPlay() {
        gui = new GameGUI(this);
    }
    public void setBot(int player, int difficulty) {
        if (player == 1) {
            if (gameAI1 == null || gameAI1.difficulty != difficulty) {  
                gameAI1 = new GameAI(this, difficulty);
            }
            isBot[0] = 1;
        } else {
            if (gameAI2 == null || gameAI2.difficulty != difficulty) {
                gameAI2 = new GameAI(this, difficulty);
            }
            isBot[1] = 1;
        }
    }
    @SuppressWarnings("CallToPrintStackTrace")
    public void consolePlay() {
        int mode = PrintUtils.inputAs(Integer.class, "請選擇遊玩模式(1: 和Bot對戰、2: 和朋友一起玩、3: 觀看ai互鬥): ").get(0);
        if (mode == 1) {
            int diff1;
            while (true) {
                diff1 = PrintUtils.inputAs(Integer.class, "請選擇Bot的難度(1: 簡單、2: 中等、3: 困難): ").get(0);
                if (diff1 == 1 || diff1 == 2 || diff1 == 3) {
                    break;  
                }
            }
            setBot(1, diff1);
        } 
        else if (mode == 3) {
            int diff1;
            while (true) {
                diff1 = PrintUtils.inputAs(Integer.class, "請選擇Bot1的難度(1: 簡單、2: 中等、3: 困難): ").get(0);
                if (diff1 == 1 || diff1 == 2 || diff1 == 3) {
                    break;
                }   
            }   
            int diff2;
            while (true) {
                diff2 = PrintUtils.inputAs(Integer.class, "請選擇Bot2的難度(1: 簡單、2: 中等、3: 困難): ").get(0);
                if (diff2 == 1 || diff2 == 2 || diff2 == 3) {
                    break;
                }
            }
            setBot(1, diff1);
            setBot(2, diff2);
        }
        while (!gameOver) {
            // 顯示當前棋盤狀態
            displayGameStatus();
            // 檢查遊戲是否結束（棋盤是否已滿）
            if (isBoardFull()) {
                gameOver = true;
                break;
            }
            int[] move;
            // 獲取玩家輸入
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

            // 處理玩家移動並計算得分
            int score = board.handleConsecutive(row, col, currentPlayer);
            playerScores[currentPlayer - 1] += score;

            

            // 切換玩家
            currentPlayer = (currentPlayer == 1) ? 2 : 1;
        }

        // 顯示遊戲結果;
        winner = maxScorePlayer();
        displayGameResult();
    }

    public int[] getPlayerMove() {
        while (true) {
            String input = PrintUtils.input("玩家" + currentPlayer + "請輸入目標格仔的坐標, e.g:A1(不限大小寫): ");
            // 移除所有空格並轉換為大寫
            input = input.replaceAll("\\s+", "").toUpperCase();

            // 檢查輸入格式：字母後跟數字
            if (!input.matches("[A-Z]\\d+")) {
                PrintUtils.print("格式不正確,請輸入有效的坐標(e.g:A1)", new PVo(10));
                continue;
            }

            // 提取字母和數字部分
            char letter = input.charAt(0);
            int number = Integer.parseInt(input.substring(1));

            // 檢查數字範圍
            if (number < 1 || number > BOARD_SIZE || letter < 'A' || letter > (BOARD_SIZE - 1) + 'A') {
                PrintUtils.print("輸入坐標超出範圍,請輸入有效的坐標(e.g:A1)", new PVo(10));
                continue;
            }

            // 轉換為陣列索引
            int col = letter - 'A';
            int row = number - 1;
            if (board.getCell(row, col) != 0) {
                if (board.getCell(row, col) == currentPlayer) {
                    PrintUtils.print("你已經在這裏擺過棋子了,請輸入有效的坐標(e.g:A1)", new PVo(10));
                    continue;
                } else {
                    PrintUtils.print("你不能覆蓋別人的棋子,請輸入有效的坐標(e.g:A1)", new PVo(10));
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
        // 顯示分數
        PrintUtils.print((isBot(1) ? "AI" : "玩家") + "1(O)得分: " + playerScores[0] + " " + (isBot(2) ? "AI" : "玩家") + "2(X)得分: " + playerScores[1]);
        // 顯示棋盤
        int[][] DBoard = board.getBoard();
        boolean[][] markedGrid = board.getMarkedGrid();
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
            PrintUtils.print("遊戲結束，平手！", new PVo(10));
        } else {
            PrintUtils.print("遊戲結束，玩家" + (winner) + "獲勝！", new PVo(10));
        }
    }

    private int maxScorePlayer() {
        int maxScore = 0;
        int maxScorePlayer = -1;
        System.out.println("playerScores: " + Arrays.toString(playerScores));
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

    // 以下是GUI用的func

    // 進行一步移動
    public boolean makeMove(int row, int col) {
        // 檢查這個位置是否已經有棋子
        if (board.getCell(row, col) != 0) {
            return false;
        }

        // 處理移動並計算得分
        int score = board.handleConsecutive(row, col, currentPlayer);
        playerScores[currentPlayer - 1] += score;
        displayGameStatus();
        // 檢查遊戲是否結束
        if (isBoardFull()) {
            gameOver = true;
            winner = maxScorePlayer();
        }

        // 切換玩家
        currentPlayer = (currentPlayer == 1) ? 2 : 1;

        return true;
    }

    // 獲取當前棋盤狀態
    public int[][] getBoardState() {
        return board.getBoard();
    }

    // 獲取標記網格
    public boolean[][] getMarkedState() {
        return board.getMarkedGrid();
    }

    // 檢查遊戲是否結束
    public boolean isGameOver() {
        return gameOver;
    }

    // 獲取當前玩家
    public int getCurrentPlayer() {
        return currentPlayer;
    }

    // 獲取玩家分數
    public int[] getPlayerScores() {
        return playerScores;
    }

    // 獲取勝者
    public int getWinner() {
        return winner;
    }

    // 獲取按方向分類的標記座標
    public java.util.Map<Integer, java.util.List<int[]>> getDirectionMarks() {
        return board.getDirectionMarks();
    }

    // 獲取每個方向的得分
    public java.util.Map<Integer, Integer> getDirectionScores() {
        return board.getDirectionScores();
    }

    // 獲取特定方向標記的中點座標
    public int[] getDirectionMiddlePosition(int direction) {
        return board.getDirectionMiddlePosition(direction);
    }

    // 獲取Board對象
    public Board getBoard() {
        return board;
    }

    // 清除標記
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
    
}