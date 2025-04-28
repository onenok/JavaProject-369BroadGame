package game.ai;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import game.Game;
public class GameAI {
    private Game game;
    private final int id;
    public final int difficulty;

    public GameAI(Game game, int difficulty, int id) {
        this.game = game;
        this.difficulty = difficulty;
        this.id = id;
    }   

    @SuppressWarnings("CallToPrintStackTrace")
    public int[] getBotMove(boolean isGUI) {
        List<int[]> empty = game.getEmpty();
        int[] position = new int[3]; //[0] = row, [1] = col, [2] = moveType(1 = random, 2 = best)
        switch (difficulty) {
            case 1 -> position = easyMode(empty);
            case 2 -> position = mediumMode(empty, isGUI);
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
    private int[] mediumMode(List<int[]> empty, boolean isGUI) {
        // 1. Find the position with the highest score
        int[] bestMove = null;
        int maxScore = 0;
        
        for (int[] cell : empty) {
            int[][] tempBoard = copyBoard(game.getBoardState());
            int score = calculatePotentialScore(cell[0], cell[1], 2, tempBoard)[0][0]; //This player value represents AI
            if (score > maxScore) {
                maxScore = score;
                bestMove = cell;
                if (isGUI) {
                    System.out.println("maxScore: " + maxScore);
                }
            }
        }
        
        // 2. If no scoring opportunity, choose randomly
        if (bestMove != null) {
            return new int[]{bestMove[0], bestMove[1], 2};
        } else {
            int[] randomMove = empty.get(new Random().nextInt(empty.size()));
            return new int[]{randomMove[0], randomMove[1], 1};
        }
    }

    private int[] hardMode(List<int[]> empty, boolean isGUI) {
        // Find the position with the highest score
        List<int[]> bestMoves = new ArrayList<>();
        int currentScoreDiff = game.getScoreDiff(id);
        int myScore = game.getPlayerScore(id);
        double maxScore = -99;
        for (int[] cell : empty) {
            int[][] tempBoard = copyBoard(game.getBoardState());
            int[][] result = calculatePotentialScore(cell[0], cell[1], 2, tempBoard); //This player value represents AI
            /* ai thinking */
            int score = result[0][0];
            //ai thinking show
            int horizontal = result[1][0];
            int vertical = result[2][0];
            int mainDiagonal = result[3][0];
            int subDiagonal = result[4][0];
            String horizontalStart = (char)(result[1][2]+65) + "" + (result[1][1]+1);
            String verticalStart = (char)(result[2][2]+65) + "" + (result[2][1]+1);
            String mainDiagonalStart = (char)(result[3][2]+65) + "" + (result[3][1]+1);
            String subDiagonalStart = (char)(result[4][2]+65) + "" + (result[4][1]+1);
            /* ai opponent thinking */
            int opponentScore = 0;
            //ai thinking show
            int opponentHorizontal = 0;
            int opponentVertical = 0;
            int opponentMainDiagonal = 0;
            int opponentSubDiagonal = 0;
            String opponentHorizontalStart = "";
            String opponentVerticalStart = "";
            String opponentMainDiagonalStart = "";
            String opponentSubDiagonalStart = "";
            // Consider opponent's potential scoring opportunities
            for (int[] cell2 : empty) {
                if (cell2[0] == cell[0] && cell2[1] == cell[1]) {
                    continue;
                }
                int[][] opponentTempBoard = copyBoard(tempBoard);
                int[][] tempResult = calculatePotentialScore(cell2[0], cell2[1], 1, opponentTempBoard); //This player value represents opponent
                int tempScore = tempResult[0][0];
                int tempHorizontal = tempResult[1][0];
                int tempVertical = tempResult[2][0];
                int tempMainDiagonal = tempResult[3][0];
                int tempSubDiagonal = tempResult[4][0];
                String tempHorizontalStart = (char)(tempResult[1][2]+65) + "" + (tempResult[1][1]+1);
                String tempVerticalStart = (char)(tempResult[2][2]+65) + "" + (tempResult[2][1]+1);
                String tempMainDiagonalStart = (char)(tempResult[3][2]+65) + "" + (tempResult[3][1]+1);
                String tempSubDiagonalStart = (char)(tempResult[4][2]+65) + "" + (tempResult[4][1]+1);
                if (tempScore > opponentScore) {
                    opponentScore = tempScore;
                    opponentHorizontal = tempHorizontal;
                    opponentVertical = tempVertical;
                    opponentMainDiagonal = tempMainDiagonal;
                    opponentSubDiagonal = tempSubDiagonal;
                    opponentHorizontalStart = tempHorizontalStart;
                    opponentVerticalStart = tempVerticalStart;
                    opponentMainDiagonalStart = tempMainDiagonalStart;
                    opponentSubDiagonalStart = tempSubDiagonalStart;
                }
            }
            // Weighted evaluation
            double weights = .5;
            if (currentScoreDiff > 0) {
                weights /= Math.min(1.2 + (0.15 * currentScoreDiff)/myScore, 0.1);  // When leading, reduce weight, strengthen offense
            } else if (currentScoreDiff < 0) {
                weights *= Math.min(1.2 - (0.15 * currentScoreDiff)/myScore, 0.1);  // When trailing, increase weight, strengthen defense
            }
            if (empty.size() > 81/2) {
                weights /= 0.7;
            } else {
                weights *= 0.7;
            }
            double totalScore = score - (opponentScore * weights);
            if (isGUI) {
                System.out.println("--------------------------------");
                System.out.println("cell: " + (char)(cell[1]+65) + (cell[0]+1));
                System.out.println("-----------");
                System.out.println("H: " + horizontal + " " + horizontalStart + " V: " + vertical + " " + verticalStart + " MD: " + mainDiagonal + " " + mainDiagonalStart + " SD: " + subDiagonal + " " + subDiagonalStart);    
                System.out.println("OH: " + opponentHorizontal + " " + opponentHorizontalStart + " OV: " + opponentVertical + " " + opponentVerticalStart + " OMD: " + opponentMainDiagonal + " " + opponentMainDiagonalStart + " OSD: " + opponentSubDiagonal + " " + opponentSubDiagonalStart);
                System.out.println("-----------");
                System.out.println("score: " + score + " opponentScore: " + opponentScore);
                System.out.println("-----------");
                System.out.println("totalScore: " + totalScore);
            }
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestMoves.clear();
                bestMoves.add(cell);
                if (isGUI) {
                    System.out.println("$$$$");
                    System.out.println("maxScore: " + maxScore);
                    System.out.println("$$$$");
                }
            }
            else if (totalScore == maxScore) {
                bestMoves.add(cell);
            }
        }
        int[] bestMove = bestMoves.get(new Random().nextInt(bestMoves.size()));
        if (isGUI) {
            System.out.println("bestMove: " + bestMove[0] + " " + bestMove[1]);
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
    
    private int[][] calculatePotentialScore(int row, int col, int player, int[][] tempBoard) {
        tempBoard[row][col] = player;
        
        int totalScore = 0;
        // Check four directions
        int[] horizontal = checkLineForScore(tempBoard, row, col, 0, 1); // Horizontal
        totalScore += horizontal[0];
        int[] vertical = checkLineForScore(tempBoard, row, col, 1, 0); // Vertical
        totalScore += vertical[0];
        int[] mainDiagonal = checkLineForScore(tempBoard, row, col, 1, 1); // Main diagonal
        totalScore += mainDiagonal[0];
        int[] subDiagonal = checkLineForScore(tempBoard, row, col, -1, 1); // Sub diagonal
        totalScore += subDiagonal[0];
        return new int[][]{{totalScore}, horizontal, vertical, mainDiagonal, subDiagonal};
    }
    private int[] checkLineForScore(int[][] board, int Row, int Col, int rowInc, int colInc) {
        int maxScore = 0;
        int currentLength = 0;
        int startRow = Row;
        int startCol = Col;
        for (int i = 1; i <= 8; i++) {
            int tempRow = Row + i * -rowInc;
            int tempCol = Col + i * -colInc;
            if (tempRow < 0 || tempRow > 8 || tempCol < 0 || tempCol > 8) break;
            if (board[tempRow][tempCol] == 0) break;
            startRow = tempRow;
            startCol = tempCol;
        }

        for (int i = 0; i <= 9; i++) {
            int r = startRow + i * rowInc;
            int c = startCol + i * colInc;
            
            if (r < 0 || r >= 9 || c < 0 || c >= 9) {
                if (currentLength % 3 == 0) {
                    maxScore += currentLength;
                }
                break;
            }
            if (board[r][c] != 0) { // As long as it's not empty
                currentLength++;
            } else {
                if (currentLength % 3 == 0) {
                    maxScore += currentLength;
                }
                currentLength = 0;
            }
        }
        
        return new int[]{maxScore, startRow, startCol};
    }
    public void checkGame(Game game) {
        if (game != this.game) {
            this.game = game;
        }
    }
}

        