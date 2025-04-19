# 369 Board Game 設計文檔

## 1. 遊戲規則
- 遊戲在9x9的棋盤上進行
- 玩家輪流在空位下棋
- 計分規則：
  - 當形成3的倍數（3、6、9）的連續棋子時可得分
  - 每次落子後，檢查4個方向（左、上、左上、左下）的連續棋子
  - 對每個方向：
    1. 從落子位置向一個方向找到連續棋子的起始位置
    2. 從起始位置向相反方向找到連續棋子的結束位置
    3. 計算連續棋子的數量
    4. 如果是3的倍數，則得1*棋子數分(3分, 6分, 9分)
  - 不同方向的3倍數會分別計分（例如：一個方向形成3個連續，另一個方向形成6個連續，總共得3+6=9分）

## 2. 核心數據結構
```
   A   B   C   D   E   F   G   H   I
 _____________________________________
1| X | O |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
2|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
3|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
4|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
5|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
6|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|  
7|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
8|   |   |   |   |   |   |   |   |   |
 |---+---+---+---+---+---+---+---+---|
9|   |   |   |   |   |   |   |   |   |
 |-----------------------------------|
```
### 2.1 棋盤類 (Board)
```java
class Board {
    private int[][] grid;  // 9x9的棋盤，0表示空位，1表示玩家1，2表示玩家2
    private static final int BOARD_SIZE = 9;
    
    public Board() {
        grid = new int[BOARD_SIZE][BOARD_SIZE];
    }

    // 檢查連續棋子的方法
    private int handleConsecutive(int row, int col, int currentPlayer) {
        grid[row][col] = currentPlayer;
        // 檢查連續棋子
        int score = 0;
        int[][] Positions = new int[4][2]; // [0]: 左, [1]: 上, [2]: 左上, [3]: 左下; [i][0]: x, [i][1]: y
        int[] count = new int[4]; // [0]: 左右, [1]: 上下, [2]: 左上右下, [3]: 左下右上
        // 向一個方向找到連續棋子的起始位置
        Positions = findStartPosition(row, col);
        // 從起始位置向相反方向找到結束位置
        // 返回連續棋子的總數
        count = countConsecutive(Positions);
        score = checkMultiples(count);
        return score;
    }

    private int[][] findStartPosition(int row, int col/*, int direction*/) {
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
                Positions[0][1] = jx; // 左 x
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
                Positions[1][0] = iy; // 上 y
                break;
            }
        }
        // 檢查左上方向起始位置
        for (int iy = row - 1, jx = col - 1; iy >= -1 && jx >= -1; iy--, jx--) {
            if (iy == -1 || jx == -1) {
                Positions[2][0] = iy == -1 ? 0 : iy+1; // 左上 y
                Positions[2][1] = jx == -1 ? 0 : jx+1; // 左上 x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[2][0] = iy; // 左上 y
                Positions[2][1] = jx; // 左上 x
                break;
            }
        }
        // 檢查左下方向起始位置
        for (int iy = row + 1, jx = col - 1; iy <= BOARD_SIZE && jx >= -1; iy++, jx--) {
            if (iy == BOARD_SIZE || jx == -1) {
                Positions[3][0] = iy == BOARD_SIZE ? BOARD_SIZE-1 : iy-1; // 左下 y
                Positions[3][1] = jx == -1 ? 0 : jx+1; // 左下 x
                break;
            }
            if (grid[iy][jx] == 0) {
                Positions[3][0] = iy; // 左下 y
                Positions[3][1] = jx; // 左下 x
                break;
            }
        }
        return Positions;
    }

    private int[] countConsecutive(int[][] Positions/*, int direction*/) {
        int[] count = new int[4];
        // 從起始位置向相反方向找到結束位置
        // 返回連續棋子的總數
        // 左右, 上下, 左上右下, 左下右上

        // from左to右
        for (int jx = Positions[0][1]; jx <= BOARD_SIZE && grid[Positions[0][0]][jx] != 0; jx++) {
            count[0]++;
        }

        // from上to下
        for (int iy = Positions[1][0]; iy <= BOARD_SIZE && grid[iy][Positions[1][1]] != 0; iy++) {
            count[1]++;
        }

        // from左上to右下
        for (int iy = Positions[2][0], jx = Positions[2][1]; iy <= BOARD_SIZE && jx <= BOARD_SIZE && grid[iy][jx] != 0; iy++, jx++) {
            count[2]++;
        }

        // from左下to右上
        for (int iy = Positions[3][0], jx = Positions[3][1]; iy >= 0 && jx <= BOARD_SIZE && grid[iy][jx] != 0; iy--, jx++) {
            count[3]++;
        }

        return count;
    }
    
    // 檢查所有方向的3倍數
    private int checkMultiples(int[] count) {
        // 檢查4個方向（左、上、左上、右上）
        int score = 0;
        for (int i = 0; i < 4; i++) {
            if (count[i] % 3 == 0) {
                score++;
            }
        }
        return score;
    }
}
```

### 2.2 遊戲類 (Game)
```java
class Game {
    private Board board;
    private int currentPlayer;  // 1或2
    private int[] playerScores;  // 玩家得分
    private boolean gameOver;
    private int winner;
    private boolean isGUI;

    public void Game(boolean isGUI) {
        board = new Board();
        playerScores = new int[2];
        gameOver = false;
        currentPlayer = 1;
        this.isGUI = isGUI;
        if (isGUI) {
            guiPlay();
        } else {
            consolePlay();
        }
    }
    public void consolePlay() {
        while (!gameOver) {
            // 玩家輪流下棋
            // 檢查遊戲是否結束
            // 判斷勝者
        }
        winner = maxScorePlayer();
    }
    public void guiPlay() {
        // 使用Java Swing實現
        // 點擊下棋
        // 動畫效果
        // 分數顯示
    }

    private int maxScorePlayer() {
        int maxScore = 0;
        int maxScorePlayer = 0;
        for (int i = 0; i < 2; i++) {
            if (playerScores[i] > maxScore) {
                maxScore = playerScores[i];
                maxScorePlayer = i;
            }
        }
        return maxScorePlayer;
    }
}
```

## 3. 主要功能實現

### 3.1 檢查3倍數
- 從落子位置向4個方向檢查連續棋子
- 對每個方向：
  1. 向一個方向找到連續棋子的起始位置
  2. 從起始位置向相反方向找到結束位置
  3. 計算連續棋子的總數
  4. 判斷是否為3的倍數
- 計算總得分

### 3.2 計分系統
- 根據檢查結果更新玩家總分
- 處理多個方向同時得分的情況

### 3.3 遊戲流程控制
- 玩家輪流下棋
- 檢查遊戲是否結束
- 判斷勝者

## 4. 用戶界面設計

### 4.1 控制台版本
- 使用ASCII字符顯示棋盤
- 通過座標輸入下棋位置 (e.g:A1)
- 顯示當前分數和遊戲狀態

### 4.2 圖形界面版本
- 使用Java Swing實現
- 棋盤設計：
  - 9x9網格按鈕
  - 標籤 (A-I, 1-9)
  - 高亮標記得分的棋子（金色背景）
  - 棋子懸停效果
- 功能設計：
  - 玩家信息顯示（當前玩家和分數）
  - 重新開始按鈕
  - 遊戲規則說明
  - 結束時顯示勝利信息

## 5. 未來擴展功能

### 5.1 人機對戰
- AI難度等級：
  - **簡單模式**：AI隨機選擇空位下棋，並且會延遲1秒來模擬思考過程。
  - **中等模式**：AI會選擇能夠獲得最高分的位置下棋，如果沒有得分機會，則隨機選擇位置。
  - **困難模式**：AI會考慮對手的可能得分，並加權評估自己的得分和對手的得分，選擇最優位置下棋。加權權重會根據棋盤的空位數量動態調整。

### 5.2 遊戲擴展
- 不同大小的棋盤（例如6x6、12x12）：尚未實現
- 計時模式：尚未實現
