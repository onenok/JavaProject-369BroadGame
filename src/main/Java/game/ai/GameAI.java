package game.ai;
import game.Game;
import java.util.List;
import java.util.Random;
public class GameAI {
    private Game game;
    public final int difficulty;

    public GameAI(Game game, int difficulty) {
        this.game = game;
        this.difficulty = difficulty;
    }   

    @SuppressWarnings("CallToPrintStackTrace")
    public int[] getBotMove(boolean isGUI) {
        List<int[]> empty = game.getEmpty();
        int[] position = new int[3]; //[0] = row, [1] = col, [2] = moveType(1 = random, 2 = best)
        switch (difficulty) {
            case 1 -> position = easyMode(empty);
            case 2 -> position = mediumMode(empty);
            case 3 -> position = hardMode(empty, isGUI);
            default -> {
            }
        }
        if (isGUI) {
            System.out.println("AI difficulty: " + difficulty);
            System.out.println("AI move: " + position[0] + ", " + position[1] + ", mode:" + (position[2] == 1 ? "random" : "bestMove") + "\n");
        }
        return position;
    }

    
    @SuppressWarnings("UseSpecificCatch")
    private int[] easyMode(List<int[]> empty) {
        try {
            Thread.sleep(1000);
            int[] position = empty.get(new Random().nextInt(empty.size()));
            return new int[]{position[0], position[1], 1};
        } catch (Exception e) {
            System.out.println("Error in easyMode: " + e.getMessage());
            return null;
        }
    }
    private int[] mediumMode(List<int[]> empty) {
        // 1. 尋找能得最高分的位置
        int[] bestMove = null;
        int maxScore = 0;
        
        for (int[] cell : empty) {
            int[][] tempBoard = copyBoard(game.getBoardState());
            int score = calculatePotentialScore(cell[0], cell[1], 2, tempBoard); //此player數值代表AI
            if (score > maxScore) {
                maxScore = score;
                bestMove = cell;
            }
        }
        
        // 2. 如果沒有得分機會，隨機選擇
        if (bestMove != null) {
            return new int[]{bestMove[0], bestMove[1], 2};
        } else {
            int[] randomMove = empty.get(new Random().nextInt(empty.size()));
            return new int[]{randomMove[0], randomMove[1], 1};
        }
    }

    private int[] hardMode(List<int[]> empty, boolean isGUI) {
        // 尋找能得最高分的位置
        int[] bestMove = null;
        double maxScore = -99;
        for (int[] cell : empty) {
            int[][] tempBoard = copyBoard(game.getBoardState());
            int score = calculatePotentialScore(cell[0], cell[1], 2, tempBoard); //此player數值代表AI
            int opponentScore = 0;
            // 考慮對手可能得分的機會
            for (int[] cell2 : empty) {
                int[][] opponentTempBoard = copyBoard(tempBoard);
                int tempScore = calculatePotentialScore(cell2[0], cell2[1], 1, opponentTempBoard); //此player數值代表玩家
                if (tempScore > opponentScore) {
                    opponentScore = tempScore;
                }
            }
            // 加權評估
            double totalScore = score - (opponentScore * (empty.size() > 81/2 ? 0.3 : 0.7));
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestMove = cell;
                if (isGUI) {
                    System.out.println("maxScore: " + maxScore);
                }
            }
        }
        return new int[]{bestMove[0], bestMove[1], 2};
    }

    private int[][] copyBoard(int[][] board) {
        int[][] copy = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }
    
    private int calculatePotentialScore(int row, int col, int player, int[][] tempBoard) {
        tempBoard[row][col] = player;
        
        int totalScore = 0;
        // 檢查四個方向
        totalScore += checkLineForScore(tempBoard, row, 0, 0, 1); // 水平
        totalScore += checkLineForScore(tempBoard, 0, col, 1, 0); // 垂直
        totalScore += checkLineForScore(tempBoard, row - Math.min(row, col), col - Math.min(row, col), 1, 1); // 主對角
        totalScore += checkLineForScore(tempBoard, row - Math.min(row, 8 - col), col + Math.min(row, 8 - col), 1, -1); // 副對角
        
        return totalScore;
    }
    private int checkLineForScore(int[][] board, int startRow, int startCol, int rowInc, int colInc) {
        int maxScore = 0;
        int currentLength = 0;
        
        for (int i = 0; i < 9; i++) {
            int r = startRow + i * rowInc;
            int c = startCol + i * colInc;
            
            if (r < 0 || r >= 9 || c < 0 || c >= 9) break;
            
            if (board[r][c] != 0) { // 只要不是空位
                currentLength++;
                if (currentLength % 3 == 0) {
                    maxScore += currentLength;
                }
            } else {
                currentLength = 0;
            }
        }
        
        return maxScore;
    }
    public void checkGame(Game game) {
        if (game != this.game) {
            this.game = game;
        }
    }
}

        