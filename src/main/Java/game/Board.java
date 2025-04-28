package game;
public class Board {
    private final int[][] grid; // 9x9 game board, 0 represents empty, 1 represents player 1, 2 represents player 2
    private final int BOARD_SIZE;

    // Stores coordinates that need to be highlighted and score information
    private final java.util.List<int[]> markedPositions; // Each element is [row, col]
    private final java.util.Map<Integer, java.util.List<int[]>> directionMarks; // Stores markers by direction: direction -> coordinates list
    private final java.util.Map<Integer, Integer> directionScores; // Stores scores for each direction: direction -> score
    private final java.util.List<int[]> empty;

    public Board(int BOARD_SIZE) {
        this.BOARD_SIZE = BOARD_SIZE;
        grid = new int[BOARD_SIZE][BOARD_SIZE];
        markedPositions = new java.util.ArrayList<>();
        directionMarks = new java.util.HashMap<>();
        directionScores = new java.util.HashMap<>();
        empty = new java.util.ArrayList<>();
        for (int i = 0; i < BOARD_SIZE; i++) {  
            for (int j = 0; j < BOARD_SIZE; j++) {
                empty.add(new int[]{i, j});
            }
        }
        // Initialize marker lists for four directions
        for (int i = 0; i < 4; i++) {
            directionMarks.put(i, new java.util.ArrayList<>());
        }
    }

    public int[][] getBoard() {
        return grid;
    }

    public int getCell(int row, int col) {
        return grid[row][col];
    }

    // Get all marked coordinates
    public java.util.List<int[]> getMarkedPositions() {
        return markedPositions;
    }

    // Get marked coordinates grouped by direction
    public java.util.Map<Integer, java.util.List<int[]>> getDirectionMarks() {
        return directionMarks;
    }

    // Get the score for each direction
    public java.util.Map<Integer, Integer> getDirectionScores() {
        return directionScores;
    }

    // Get the marked grid positions
    public boolean[][] getMarkedGrid() {
        boolean[][] markedGrid = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int[] pos : markedPositions) {
            markedGrid[pos[0]][pos[1]] = true;
        }
        return markedGrid;
    }

    // Get the middle position of the marked positions in a specific direction
    public int[] getDirectionMiddlePosition(int direction) {
        java.util.List<int[]> positions = directionMarks.get(direction);
        if (positions.isEmpty()) {
            return null;
        }

        int size = positions.size();
        // Return the middle position [row, col]
        return positions.get(size / 2);
    }

    public java.util.List<int[]> getEmpty() {
        return empty;
    }

    public boolean isFull() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (grid[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    // Clear all marks
    public void clearMarks() {
        markedPositions.clear();
        for (int i = 0; i < 4; i++) {
            directionMarks.get(i).clear();
        }
        directionScores.clear();
    }

    // Mark consecutive pieces
    private void markConsecutive(int[][] Positions, int[] count) {
        // Mark left-right direction
        if (count[0] % 3 == 0 && count[0] > 0) { // If there are consecutive pieces in left-right direction
            java.util.List<int[]> leftRightMarks = new java.util.ArrayList<>();
            for (int jx = Positions[0][1]; jx < Positions[0][1] + count[0]; jx++) {
                int[] pos = { Positions[0][0], jx };
                leftRightMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(0, leftRightMarks);
            directionScores.put(0, count[0]);
        }

        // Mark up-down direction
        if (count[1] % 3 == 0 && count[1] > 0) { // If there are consecutive pieces in up-down direction
            java.util.List<int[]> upDownMarks = new java.util.ArrayList<>();
            for (int iy = Positions[1][0]; iy < Positions[1][0] + count[1]; iy++) {
                int[] pos = { iy, Positions[1][1] };
                upDownMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(1, upDownMarks);
            directionScores.put(1, count[1]);
        }

        // Mark top-left to bottom-right diagonal
        if (count[2] % 3 == 0 && count[2] > 0) { // If there are consecutive pieces in top-left to bottom-right direction
            java.util.List<int[]> diagonalMarks = new java.util.ArrayList<>();
            for (int i = 0; i < count[2]; i++) {
                int[] pos = { Positions[2][0] + i, Positions[2][1] + i };
                diagonalMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(2, diagonalMarks);
            directionScores.put(2, count[2]);
        }

        // Mark bottom-left to top-right diagonal
        if (count[3] % 3 == 0 && count[3] > 0) { // If there are consecutive pieces in bottom-left to top-right direction
            java.util.List<int[]> antiDiagonalMarks = new java.util.ArrayList<>();
            for (int i = 0; i < count[3]; i++) {
                int[] pos = { Positions[3][0] - i, Positions[3][1] + i };
                antiDiagonalMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(3, antiDiagonalMarks);
            directionScores.put(3, count[3]);
        }
    }

    // Method to check consecutive pieces
    public int handleConsecutive(int row, int col, int currentPlayer) {
        clearMarks(); // Clear previous marks
        // Remove current move position from empty list
        empty.removeIf(pos -> pos[0] == row && pos[1] == col);
        // Convert all grid values to 0-2 range (mod 3)
        for (int[] grid1 : grid) {
            for (int j = 0; j < grid1.length; j++) {
                grid1[j] = grid1[j] % 3;
            }
        }
        grid[row][col] = currentPlayer+3;
        // Check for consecutive pieces
        int[][] positions = findStartPosition(row, col);
        int[] count = countConsecutive(positions);
        int score = checkMultiples(count);
        if (score > 0) {
            markConsecutive(positions, count);
        }
        return score;
    }

    private int[][] findStartPosition(int row, int col/* , int direction */) {
        int[][] Positions = new int[4][2]; // [0]: left, [1]: up, [2]: top-left, [3]: bottom-left; [i][0]: y, [i][1]: x
        Positions[0][0] = row; // left-right y remains same
        Positions[1][1] = col; // up-down x remains same
        // Check left direction start position
        for (int jx = col - 1; jx >= -1; jx--) {
            if (jx == -1) {
                Positions[0][1] = 0; // left x
                break;
            }
            if (grid[row][jx] == 0) {
                Positions[0][1] = jx + 1; // left x
                break;
            }
        }
        // Check up direction start position
        for (int iy = row - 1; iy >= -1; iy--) {
            if (iy == -1) {
                Positions[1][0] = 0; // up y
                break;
            }
            if (grid[iy][col] == 0) {
                Positions[1][0] = iy + 1; // up y
                break;
            }
        }
        // Check top-left direction start position
        for (int iy = row - 1, jx = col - 1; iy >= -1 && jx >= -1; iy--, jx--) {
            if (iy == -1 || jx == -1) {
                Positions[2][0] = iy == -1 ? 0 : iy + 1; // top-left y
                Positions[2][1] = jx == -1 ? 0 : jx + 1; // top-left x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[2][0] = iy + 1; // top-left y
                Positions[2][1] = jx + 1; // top-left x
                break;
            }
        }
        // Check bottom-left direction start position
        for (int iy = row + 1, jx = col - 1; iy <= BOARD_SIZE && jx >= -1; iy++, jx--) {
            if (iy == BOARD_SIZE || jx == -1) {
                Positions[3][0] = iy == BOARD_SIZE ? BOARD_SIZE - 1 : iy - 1; // bottom-left y
                Positions[3][1] = jx == -1 ? 0 : jx + 1; // bottom-left x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[3][0] = iy - 1; // bottom-left y
                Positions[3][1] = jx + 1; // bottom-left x
                break;
            }
        }
        return Positions;
    }

    private int[] countConsecutive(int[][] Positions/* , int direction */) {
        int[] count = new int[4];
        // From start position to opposite direction to find end position
        // Return total number of consecutive pieces
        // left-right, up-down, top-left to bottom-right, bottom-left to top-right

        // from left to right
        for (int jx = Positions[0][1]; jx < BOARD_SIZE && grid[Positions[0][0]][jx] != 0; jx++) {
            count[0]++;
        }

        // from up to down
        for (int iy = Positions[1][0]; iy < BOARD_SIZE && grid[iy][Positions[1][1]] != 0; iy++) {
            count[1]++;
        }

        // from top-left to bottom-right
        for (int iy = Positions[2][0], jx = Positions[2][1]; iy < BOARD_SIZE && jx < BOARD_SIZE
                && grid[iy][jx] != 0; iy++, jx++) {
            count[2]++;
        }

        // from bottom-left to top-right
        for (int iy = Positions[3][0], jx = Positions[3][1]; iy >= 0 && jx < BOARD_SIZE
                && grid[iy][jx] != 0; iy--, jx++) {
            count[3]++;
        }

        return count;
    }

    // Check multiples of 3 in all directions
    private int checkMultiples(int[] count) {
        // Check 4 directions (left, up, top-left, bottom-left)
        int score = 0;
        for (int i = 0; i < 4; i++) {
            if (count[i] % 3 == 0 && count[i] > 0) {
                score += count[i];
            }
        }
        return score;
    }
}