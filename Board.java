public class Board {
    private final int[][] grid; // 9x9的棋盤，0表示空位，1表示玩家1，2表示玩家2
    private final int BOARD_SIZE;

    // 存儲需要高亮的座標以及得分信息
    private final java.util.List<int[]> markedPositions; // 每個元素是 [row, col]
    private final java.util.Map<Integer, java.util.List<int[]>> directionMarks; // 按方向存儲標記：方向 -> 座標列表
    private final java.util.Map<Integer, Integer> directionScores; // 每個方向的得分：方向 -> 分數
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
        // 初始化四個方向的標記列表
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

    // 獲取所有標記座標
    public java.util.List<int[]> getMarkedPositions() {
        return markedPositions;
    }

    // 獲取按方向分類的標記座標
    public java.util.Map<Integer, java.util.List<int[]>> getDirectionMarks() {
        return directionMarks;
    }

    // 獲取每個方向的得分
    public java.util.Map<Integer, Integer> getDirectionScores() {
        return directionScores;
    }

    // 獲取標記的位置網格
    public boolean[][] getMarkedGrid() {
        boolean[][] markedGrid = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int[] pos : markedPositions) {
            markedGrid[pos[0]][pos[1]] = true;
        }
        return markedGrid;
    }

    // 獲取特定方向標記的中點座標
    public int[] getDirectionMiddlePosition(int direction) {
        java.util.List<int[]> positions = directionMarks.get(direction);
        if (positions.isEmpty()) {
            return null;
        }

        int size = positions.size();
        // 返回中點座標 [row, col]
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

    // 清除所有標記
    public void clearMarks() {
        markedPositions.clear();
        for (int i = 0; i < 4; i++) {
            directionMarks.get(i).clear();
        }
        directionScores.clear();
    }

    // 標記連續的棋子
    private void markConsecutive(int[][] Positions, int[] count) {
        // 標記左右方向
        if (count[0] % 3 == 0 && count[0] > 0) { // 如果左右方向有連續的棋子
            java.util.List<int[]> leftRightMarks = new java.util.ArrayList<>();
            for (int jx = Positions[0][1]; jx < Positions[0][1] + count[0]; jx++) {
                int[] pos = { Positions[0][0], jx };
                leftRightMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(0, leftRightMarks);
            directionScores.put(0, count[0]);
        }

        // 標記上下方向
        if (count[1] % 3 == 0 && count[1] > 0) { // 如果上下方向有連續的棋子
            java.util.List<int[]> upDownMarks = new java.util.ArrayList<>();
            for (int iy = Positions[1][0]; iy < Positions[1][0] + count[1]; iy++) {
                int[] pos = { iy, Positions[1][1] };
                upDownMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(1, upDownMarks);
            directionScores.put(1, count[1]);
        }

        // 標記左上到右下
        if (count[2] % 3 == 0 && count[2] > 0) { // 如果左上到右下方向有連續的棋子
            java.util.List<int[]> diagonalMarks = new java.util.ArrayList<>();
            for (int i = 0; i < count[2]; i++) {
                int[] pos = { Positions[2][0] + i, Positions[2][1] + i };
                diagonalMarks.add(pos);
                markedPositions.add(pos);
            }
            directionMarks.put(2, diagonalMarks);
            directionScores.put(2, count[2]);
        }

        // 標記左下到右上
        if (count[3] % 3 == 0 && count[3] > 0) { // 如果左下到右上方向有連續的棋子
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

    // 檢查連續棋子的方法
    public int handleConsecutive(int row, int col, int currentPlayer) {
        clearMarks(); // 清除之前的標記
        // 從空位列表中移除當前落子的位置
        empty.removeIf(pos -> pos[0] == row && pos[1] == col);
        // 將grid中所有元素的值轉換為0-2範圍（取模3）
        for (int[] grid1 : grid) {
            for (int j = 0; j < grid1.length; j++) {
                grid1[j] = grid1[j] % 3;
            }
        }
        grid[row][col] = currentPlayer+3;
        // 檢查連續棋子
        int[][] positions = findStartPosition(row, col);
        int[] count = countConsecutive(positions);
        int score = checkMultiples(count);
        if (score > 0) {
            markConsecutive(positions, count);
        }
        return score;
    }

    private int[][] findStartPosition(int row, int col/* , int direction */) {
        int[][] Positions = new int[4][2]; // [0]: 左, [1]: 上, [2]: 左上, [3]: 左下; [i][0]: y, [i][1]: x
        Positions[0][0] = row; // 左右 y不變
        Positions[1][1] = col; // 上下 x不變
        // 檢查左方向起始位置
        for (int jx = col - 1; jx >= -1; jx--) {
            if (jx == -1) {
                Positions[0][1] = 0; // 左 x
                break;
            }
            if (grid[row][jx] == 0) {
                Positions[0][1] = jx + 1; // 左 x
                break;
            }
        }
        // 檢查上方向起始位置
        for (int iy = row - 1; iy >= -1; iy--) {
            if (iy == -1) {
                Positions[1][0] = 0; // 上 y
                break;
            }
            if (grid[iy][col] == 0) {
                Positions[1][0] = iy + 1; // 上 y
                break;
            }
        }
        // 檢查左上方向起始位置
        for (int iy = row - 1, jx = col - 1; iy >= -1 && jx >= -1; iy--, jx--) {
            if (iy == -1 || jx == -1) {
                Positions[2][0] = iy == -1 ? 0 : iy + 1; // 左上 y
                Positions[2][1] = jx == -1 ? 0 : jx + 1; // 左上 x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[2][0] = iy + 1; // 左上 y
                Positions[2][1] = jx + 1; // 左上 x
                break;
            }
        }
        // 檢查左下方向起始位置
        for (int iy = row + 1, jx = col - 1; iy <= BOARD_SIZE && jx >= -1; iy++, jx--) {
            if (iy == BOARD_SIZE || jx == -1) {
                Positions[3][0] = iy == BOARD_SIZE ? BOARD_SIZE - 1 : iy - 1; // 左下 y
                Positions[3][1] = jx == -1 ? 0 : jx + 1; // 左下 x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[3][0] = iy - 1; // 左下 y
                Positions[3][1] = jx + 1; // 左下 x
                break;
            }
        }
        return Positions;
    }

    private int[] countConsecutive(int[][] Positions/* , int direction */) {
        int[] count = new int[4];
        // 從起始位置向相反方向找到結束位置
        // 返回連續棋子的總數
        // 左右, 上下, 左上右下, 左下右上

        // from左to右
        for (int jx = Positions[0][1]; jx < BOARD_SIZE && grid[Positions[0][0]][jx] != 0; jx++) {
            count[0]++;
        }

        // from上to下
        for (int iy = Positions[1][0]; iy < BOARD_SIZE && grid[iy][Positions[1][1]] != 0; iy++) {
            count[1]++;
        }

        // from左上to右下
        for (int iy = Positions[2][0], jx = Positions[2][1]; iy < BOARD_SIZE && jx < BOARD_SIZE
                && grid[iy][jx] != 0; iy++, jx++) {
            count[2]++;
        }

        // from左下to右上
        for (int iy = Positions[3][0], jx = Positions[3][1]; iy >= 0 && jx < BOARD_SIZE
                && grid[iy][jx] != 0; iy--, jx++) {
            count[3]++;
        }

        return count;
    }

    // 檢查所有方向的3倍數
    private int checkMultiples(int[] count) {
        // 檢查4個方向（左、上、左上、左下）
        int score = 0;
        for (int i = 0; i < 4; i++) {
            if (count[i] % 3 == 0 && count[i] > 0) {
                score += count[i];
            }
        }
        return score;
    }
}