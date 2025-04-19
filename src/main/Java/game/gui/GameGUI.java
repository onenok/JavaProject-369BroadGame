package game.gui;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.*;

import game.Game;

public final class GameGUI extends JFrame {
    private Game game;
    private JButton[][] buttons;
    private JPanel mainPanel;
    private JLabel playerTurnLabel;
    private JLabel player1ScoreLabel;
    private JLabel player2ScoreLabel;
    private boolean updating = false;
    private final int BOARD_SIZE = 9;
    private final Color PLAYER1_COLOR = new Color(70, 130, 180); // 藍色
    private final Color PLAYER2_COLOR = new Color(220, 20, 60); // 紅色
    private final Color PLAYER1_SELECTED_HIGHLIGHT = new Color(100, 149, 237); // 淺藍色 (選中)
    private final Color PLAYER2_SELECTED_HIGHLIGHT = new Color(255, 160, 122); // 淺粉紅色 (選中)
    private final Color MARKED_COLOR = new Color(255, 215, 0); // 金色 (標記3倍數)
    private final Color BG_COLOR = new Color(255, 255, 255); // 白色
    private final Color BOARD_BACKGROUND = new Color(240, 240, 240); // 淺灰色
    private final Color GRID_COLOR = new Color(120, 120, 120); // 深灰色
    private final Color ERROR_COLOR = new Color(255, 0, 0); // 錯誤訊息顏色
    private final int CORNER_RADIUS = 15; // 圓角半徑
    // 用於顯示浮動錯誤訊息
    private JPanel glassPane;

    private class showFloatingMessage {
        private JLabel messageLabel;
        private String message;
        private int row;
        private int col;
        private Color color;
        private Timer floatingTimer;
        private final GameGUI parentFrame; // 存儲外部類引用
        private int animationType; // 動畫類型：0=上升淡出，1=放大淡出

        // 顯示浮動訊息（默認上升淡出動畫）
        private showFloatingMessage(String message, int row, int col, Color color) {
            this(message, row, col, color, 0);
        }

        // 顯示浮動訊息（可指定動畫類型）
        private showFloatingMessage(String message, int row, int col, Color color, int animationType) {
            this.message = message;
            this.row = row;
            this.col = col;
            this.color = color;
            this.parentFrame = GameGUI.this; // 保存對外部類的引用
            this.animationType = animationType;

            // 初始化浮動訊息標籤 - 使用透明面板作為背景
            JPanel transparentPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    // 不調用super.paintComponent，保持完全透明
                }

                @Override
                public boolean isOpaque() {
                    return false;
                }
            };
            transparentPanel.setLayout(new BorderLayout());

            // 創建標籤
            messageLabel = new JLabel(message, JLabel.CENTER);
            messageLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            messageLabel.setForeground(color);
            messageLabel.setOpaque(false);

            // 將標籤添加到透明面板
            transparentPanel.add(messageLabel, BorderLayout.CENTER);
            transparentPanel.setOpaque(false);
            transparentPanel.setVisible(false);

            // 添加到glassPane
            glassPane.add(transparentPanel);

            // 顯示訊息
            displayMessage(transparentPanel);
        }

        @SuppressWarnings("CallToPrintStackTrace")
        private void displayMessage(JPanel messagePanel) {
            try {
                // 取得按鈕的在螢幕上的絕對位置
                Point buttonLocationOnScreen = buttons[row][col].getLocationOnScreen();
                // 取得視窗在螢幕上的絕對位置
                Point frameLocationOnScreen = parentFrame.getLocationOnScreen(); // 使用外部類引用

                // 計算按鈕相對於視窗的位置
                int buttonX = buttonLocationOnScreen.x - frameLocationOnScreen.x;
                int buttonY = buttonLocationOnScreen.y - frameLocationOnScreen.y;

                // 設置訊息標籤位置（顯示在按鈕上方中央）
                FontMetrics fm = messageLabel.getFontMetrics(messageLabel.getFont());
                int textWidth = fm.stringWidth(message);

                // 設置訊息標籤位置（顯示在按鈕上方中央）
                int labelWidth = Math.max(textWidth + 20, 200);
                int labelHeight = 30;
                messagePanel.setBounds(
                        buttonX - (labelWidth - buttons[row][col].getWidth()) / 2,
                        buttonY - labelHeight,
                        labelWidth,
                        labelHeight);

                // 設置訊息和顯示
                messageLabel.setText(message);
                messageLabel.setForeground(color);
                messagePanel.setVisible(true);

                // 根據動畫類型選擇不同的動畫效果
                if (animationType == 1) {
                    // 創建動畫效果 - 放大淡出
                    createZoomOutAnimation(messagePanel);
                } else {
                    // 創建動畫效果 - 上升淡出（默認）
                    createFadeUpAnimation(messagePanel);
                }

            } catch (Exception e) {
                System.out.println("顯示訊息時發生錯誤: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // 上升淡出動畫
        private void createFadeUpAnimation(JPanel messagePanel) {
            final int startY = messagePanel.getY();
            final float[] alpha = { 1.0f };

            floatingTimer = new Timer(50, new ActionListener() {
                int steps = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    steps++;

                    // 上移動畫
                    messagePanel.setLocation(messagePanel.getX(), startY - steps * 2);

                    // 透明度漸減
                    alpha[0] = Math.max(0, 1.0f - (steps * 0.05f));
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            (int) (alpha[0] * 255));
                    messageLabel.setForeground(newColor);

                    // 動畫結束，隱藏標籤
                    if (steps >= 20) {
                        messagePanel.setVisible(false);
                        ((Timer) e.getSource()).stop();
                        glassPane.remove(messagePanel);
                        glassPane.repaint();
                    }
                }
            });

            floatingTimer.start();
        }

        // 放大淡出動畫（適用於得分提示）
        private void createZoomOutAnimation(JPanel messagePanel) {
            final float[] alpha = { 1.0f };
            final float[] scale = { 1.0f };
            final Font originalFont = messageLabel.getFont();
            final int startX = messagePanel.getX();
            final int startY = messagePanel.getY();
            final int startWidth = messagePanel.getWidth();
            final int startHeight = messagePanel.getHeight();

            floatingTimer = new Timer(50, new ActionListener() {
                int steps = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    steps++;

                    // 縮放動畫
                    scale[0] = 1.0f + (steps * 0.1f);
                    int newSize = (int) (originalFont.getSize() * scale[0]);
                    messageLabel.setFont(new Font(originalFont.getFamily(), originalFont.getStyle(), newSize));

                    // 重新計算位置，確保訊息保持在中央
                    int newWidth = (int) (startWidth * scale[0]);
                    int newHeight = (int) (startHeight * scale[0]);
                    int newX = startX - (newWidth - startWidth) / 2;
                    int newY = startY - (newHeight - startHeight) / 2;
                    messagePanel.setBounds(newX, newY, newWidth, newHeight);

                    // 透明度漸減
                    alpha[0] = Math.max(0, 1.0f - (steps * 0.05f));
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            (int) (alpha[0] * 255));
                    messageLabel.setForeground(newColor);

                    // 動畫結束，隱藏標籤
                    if (steps >= 20) {
                        messagePanel.setVisible(false);
                        ((Timer) e.getSource()).stop();
                        glassPane.remove(messagePanel);
                        glassPane.repaint();
                    }
                }
            });

            floatingTimer.start();
        }
    }
    
    @SuppressWarnings("unused")
    public void showFM(String message, int row, int col, Color color) {
        showFloatingMessage a = new showFloatingMessage(message, row, col, color);
    }
    // 自定義圓角邊框類
    private class RoundedCornerBorder extends AbstractBorder {
        private final int topLeftRadius;
        private final int topRightRadius;
        private final int bottomLeftRadius;
        private final int bottomRightRadius;
        private final Color color;

        public RoundedCornerBorder(int topLeftRadius, int topRightRadius, int bottomLeftRadius, int bottomRightRadius,
                Color color) {
            this.topLeftRadius = topLeftRadius;
            this.topRightRadius = topRightRadius;
            this.bottomLeftRadius = bottomLeftRadius;
            this.bottomRightRadius = bottomRightRadius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 創建圓角矩形路徑
            Path2D path = createRoundRectPath(x, y, width, height);

            // 首先，清除整個區域（使區域變透明）
            if (c.getParent() != null) {
                // 獲取父級背景色，用於在正確的背景上繪製
                Color parentBackground = c.getParent().getBackground();
                g2d.setColor(parentBackground);
                g2d.fillRect(x, y, width, height);
            }

            // 繪製圓角填充區域
            g2d.setColor(c.getBackground());
            g2d.fill(path);

            // 繪製邊框
            g2d.setColor(color);
            g2d.draw(path);

            g2d.dispose();
        }

        // 創建圓角矩形路徑
        private Path2D createRoundRectPath(int x, int y, int width, int height) {
            Path2D path = new Path2D.Double();

            // 左上角
            if (topLeftRadius > 0) {
                path.moveTo(x, y + topLeftRadius);
                path.quadTo(x, y, x + topLeftRadius, y);
            } else {
                path.moveTo(x, y);
            }

            // 右上角
            if (topRightRadius > 0) {
                // 頂部線條到右上角(預留弧線位置)
                path.lineTo(x + width - topRightRadius, y);
                path.quadTo(x + width, y, x + width, y + topRightRadius);
            } else {
                // 頂部線條到右上角
                path.lineTo(x + width, y);
            }

            // 右下角
            if (bottomRightRadius > 0) {
                // 右側線條到右下角(預留弧線位置)
                path.lineTo(x + width, y + height - bottomRightRadius);
                path.quadTo(x + width, y + height, x + width - bottomRightRadius, y + height);
            } else {
                // 右側線條到右下角
                path.lineTo(x + width, y + height);
            }

            // 左下角
            if (bottomLeftRadius > 0) {
                // 底部線條到左下角(預留弧線位置)
                path.lineTo(x + bottomLeftRadius, y + height);
                path.quadTo(x, y + height, x, y + height - bottomLeftRadius);
            } else {
                // 底部線條到左下角
                path.lineTo(x, y + height);
            }

            // 左側線條回到起點
            path.lineTo(x, y + topLeftRadius);

            // 關閉路徑
            path.closePath();

            return path;
        }

        @Override
        public boolean isBorderOpaque() {
            return false; // 設置為非不透明，以便正確處理透明度
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // 返回適當的內邊距，確保每個方向有足夠空間顯示圓角
            return new Insets(
                    Math.max(topLeftRadius, topRightRadius), // 頂部需要考慮左上角和右上角的較大值
                    Math.max(topLeftRadius, bottomLeftRadius), // 左側需要考慮左上角和左下角的較大值
                    Math.max(bottomLeftRadius, bottomRightRadius), // 底部需要考慮左下角和右下角的較大值
                    Math.max(topRightRadius, bottomRightRadius) // 右側需要考慮右上角和右下角的較大值
            );
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // 更新現有的內邊距對象
            insets.top = Math.max(topLeftRadius, topRightRadius);
            insets.left = Math.max(topLeftRadius, bottomLeftRadius);
            insets.bottom = Math.max(bottomLeftRadius, bottomRightRadius);
            insets.right = Math.max(topRightRadius, bottomRightRadius);
            return insets;
        }
    }
    private void setAI() {
        // 顯示模式選擇對話框
        String[] options = {"與AI遊玩", "與朋友遊玩", "觀看AI互鬥"};
        int mode = JOptionPane.showOptionDialog(this,
                "請選擇遊戲模式",
                "模式選擇",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // 根據選擇的模式進行處理
        if (mode == 0) { // 與AI遊玩
            // 顯示難度選擇對話框
            String[] difficulties = {"簡單", "中等", "困難"};
            int difficulty = JOptionPane.showOptionDialog(this,
                    "請選擇AI難度",
                    "難度選擇",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // 設置AI難度
            game.setBot(1, difficulty + 1); // 難度選項索引+1對應實際難度值
        } else if (mode == 2) { // 觀看AI互鬥
            // 顯示第一個AI的難度選擇
            String[] difficulties = {"簡單", "中等", "困難"};
            int difficulty1 = JOptionPane.showOptionDialog(this,
                    "請選擇第一個AI的難度",
                    "AI1難度選擇",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // 顯示第二個AI的難度選擇
            int difficulty2 = JOptionPane.showOptionDialog(this,
                    "請選擇第二個AI的難度",
                    "AI2難度選擇",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // 設置兩個AI的難度
            game.setBot(1, difficulty1 + 1);
            game.setBot(2, difficulty2 + 1);
        }
    }
    public GameGUI(Game game) {
        // 初始化遊戲
        this.game = game;
        setAI();
        setTitle("369 棋盤遊戲");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        setPreferredSize(new Dimension(800, 800));

        // 設置glassPane用於顯示浮動訊息 - 使用完全透明的面板
        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
            }

            @Override
            public boolean isOpaque() {
                return false; // 確保面板完全透明
            }
        };
        glassPane.setOpaque(false); // 設為非不透明
        glassPane.setLayout(null); // 使用絕對佈局
        setGlassPane(glassPane);
        glassPane.setVisible(true);

        // 創建主面板
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        mainPanel.setBackground(PLAYER1_COLOR);

        // 創建狀態面板
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        // 創建標題面板
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.SOUTH);

        // 創建 棋盤及坐標標籤 面板
        JPanel boardWithLabelsPanel = new JPanel(new BorderLayout(5, 5));
        boardWithLabelsPanel.setBackground(new Color(245, 245, 245, 0));

        // 創建欄標籤 (A-I)
        JPanel colLabelsPanel = new JPanel(new GridLayout(1, BOARD_SIZE));
        colLabelsPanel.setBackground(new Color(245, 245, 245, 0));
        for (int j = 0; j < BOARD_SIZE; j++) {
            JLabel colLabel = new JLabel(String.valueOf((char) ('A' + j)), JLabel.CENTER);
            colLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            colLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, j < BOARD_SIZE - 1 ? 0 : 1, Color.BLACK));
            colLabelsPanel.add(colLabel);
        }

        // 創建左上角的填充面板
        JPanel cornerPanel = new JPanel();
        cornerPanel.setBackground(new Color(245, 245, 245, 0));
        JLabel corner = new JLabel("  ", JLabel.RIGHT);
        corner.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        cornerPanel.add(corner);

        // 創建一個左側容器面板來包含角落面板和行標籤
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245, 0));
        topPanel.add(cornerPanel, BorderLayout.WEST);
        topPanel.add(colLabelsPanel, BorderLayout.CENTER);

        // 創建列標籤 (1-9)
        JPanel rowLabelsPanel = new JPanel(new GridLayout(BOARD_SIZE, 1));
        rowLabelsPanel.setBackground(new Color(0, 0, 0, 0));
        for (int i = 0; i < BOARD_SIZE; i++) {
            JLabel rowLabel = new JLabel(String.valueOf(i + 1) + " ", JLabel.RIGHT);
            rowLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            rowLabel.setBorder(BorderFactory.createMatteBorder(1, 0, i < BOARD_SIZE - 1 ? 0 : 1, 0, Color.BLACK));
            rowLabelsPanel.add(rowLabel);
        }

        // 創建棋盤面板
        JPanel boardPanel = createBoardPanel();

        // 添加所有面板
        boardWithLabelsPanel.add(topPanel, BorderLayout.NORTH);
        boardWithLabelsPanel.add(rowLabelsPanel, BorderLayout.WEST);
        boardWithLabelsPanel.add(boardPanel, BorderLayout.CENTER);
        // 添加棋盤到主面板
        mainPanel.add(boardWithLabelsPanel, BorderLayout.CENTER);

        // 添加組件調整監聽器
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 重新計算按鈕的尺寸
                updateButtonSizes();
            }
        });

        // 添加到窗口
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        if (game.isBot(game.getCurrentPlayer())) {
            updating = true;
        }
        setVisible(true);

        // 初始更新UI
        updateBoard();
    }

    // 更新按鈕尺寸以適應窗口大小
    private void updateButtonSizes() {
        // 獲取窗口的當前大小
        int windowWidth = getWidth();
        int windowHeight = getHeight();

        // 設置棋盤格子的最小尺寸
        int minButtonSize = 30;

        // 計算可用空間
        int availableWidth = windowWidth - 100; // 減去邊距和標籤空間
        int availableHeight = windowHeight - 200; // 減去頂部和底部空間

        // 計算按鈕的合適尺寸
        int buttonSize = Math.min(availableWidth / BOARD_SIZE, availableHeight / BOARD_SIZE);
        buttonSize = Math.max(buttonSize, minButtonSize); // 確保不小於最小尺寸

        // 更新棋盤上的所有按鈕尺寸
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (buttons[i][j] != null) {
                    buttons[i][j].setPreferredSize(new Dimension(buttonSize, buttonSize));
                }
            }
        }

        // 重新佈局
        revalidate();
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new RoundedCornerBorder(CORNER_RADIUS, CORNER_RADIUS, 0, 0, BG_COLOR));
        JLabel titleLabel = new JLabel("369 棋盤遊戲", JLabel.CENTER);
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 24));

        // 創建按鈕面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_COLOR);

        // 創建規則說明按鈕
        JButton rulesButton = new JButton("遊戲規則");
        rulesButton.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        rulesButton.setFocusPainted(false);
        rulesButton.addActionListener(e -> showGameRules());

        // 創建重新開始按鈕
        JButton restartButton = new JButton("重新開始");
        restartButton.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> restartGame());

        buttonPanel.add(rulesButton);
        buttonPanel.add(Box.createHorizontalStrut(10)); // 添加間距
        buttonPanel.add(restartButton);

        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStatusPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 5, 5));
        panel.setBorder(new RoundedCornerBorder(0, 0, CORNER_RADIUS, CORNER_RADIUS, BG_COLOR));
        panel.setBackground(BG_COLOR);

        playerTurnLabel = new JLabel("當前玩家: 玩家1", JLabel.CENTER);
        playerTurnLabel.setFont(new Font("微軟正黑體", Font.BOLD, 18));

        JPanel scorePanel = new JPanel(new GridLayout(1, 2));
        scorePanel.setBackground(BG_COLOR);

        player1ScoreLabel = new JLabel("玩家1得分: 0", JLabel.CENTER);
        player1ScoreLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        player1ScoreLabel.setForeground(PLAYER1_COLOR);

        player2ScoreLabel = new JLabel("玩家2得分: 0", JLabel.CENTER);
        player2ScoreLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        player2ScoreLabel.setForeground(PLAYER2_COLOR);

        scorePanel.add(player1ScoreLabel);
        scorePanel.add(player2ScoreLabel);

        panel.add(playerTurnLabel);
        panel.add(scorePanel);

        return panel;
    }

    private JPanel createBoardPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(BOARD_SIZE, BOARD_SIZE, 2, 2));
        panel.setBackground(GRID_COLOR);

        buttons = new JButton[BOARD_SIZE][BOARD_SIZE];

        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                buttons[i][j] = new JButton();
                buttons[i][j].setPreferredSize(new Dimension(60, 60));
                buttons[i][j].setFont(new Font("微軟正黑體", Font.BOLD, 24));
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].setBackground(BOARD_BACKGROUND);
                buttons[i][j].setBorder(BorderFactory.createLineBorder(GRID_COLOR, 1));

                // 設置按鈕坐標作為名稱 (方便區分)
                buttons[i][j].setName(i + "," + j);

                // 添加棋子放置事件
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener((ActionEvent e) -> {
                    if (updating || game.isBot(game.getCurrentPlayer())) {
                        return;
                    }
                    handleButtonClick(row, col);
                });

                // 添加滑鼠懸停效果
                buttons[i][j].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (buttons[row][col].getText().isEmpty()) {
                            buttons[row][col].setBackground(new Color(230, 230, 230));
                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (buttons[row][col].getText().isEmpty()) {
                            buttons[row][col].setBackground(BOARD_BACKGROUND);
                        }
                    }
                });

                panel.add(buttons[i][j]);
            }
        }

        return panel;
    }

    public void handleButtonClick(int row, int col/*, boolean isBot*/) {
        if (updating)
            return;
        if (!game.makeMove(row, col)) {
            // 使用修改後的showFloatingMessage類來顯示錯誤（使用默認上升動畫）
            showFM("這個位置已經有棋子了", row, col, ERROR_COLOR);
            return;
        }
        updating = true;

        // 更新棋盤並在動畫結束後更新狀態
        updateBoard();
    }

    public void updateBoard() {
        // 獲取需要標記的位置和分數
        Map<Integer, List<int[]>> directionMarks = game.getDirectionMarks();
        Map<Integer, Integer> directionScores = game.getDirectionScores();
        int[][] board = game.getBoardState();

        // 重置所有按鈕背景和文字
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                int cell = board[i][j];
                JButton button = buttons[i][j];

                switch (cell) {
                    case 0 -> {
                        button.setText("");
                        button.setBackground(BOARD_BACKGROUND);
                    }
                    case 1 -> {
                        button.setText("O");
                        button.setForeground(PLAYER1_COLOR);
                        button.setBackground(BOARD_BACKGROUND);
                    }
                    case 2 -> {
                        button.setText("X");
                        button.setForeground(PLAYER2_COLOR);
                        button.setBackground(BOARD_BACKGROUND);
                    }
                    case 4 -> {
                        button.setText("O");
                        button.setForeground(PLAYER1_COLOR);
                        button.setBackground(PLAYER1_SELECTED_HIGHLIGHT);
                    }
                    case 5 -> {
                        button.setText("X");
                        button.setForeground(PLAYER2_COLOR);
                        button.setBackground(PLAYER2_SELECTED_HIGHLIGHT);
                    }
                }
            }
        }

        // 如果有標記的位置，啟動動畫效果並在結束後更新狀態
        if (!directionMarks.isEmpty()) {
            startMarkingAnimation(directionMarks, directionScores);
        }
    }

    // 啟動標記動畫
    @SuppressWarnings("")
    private void startMarkingAnimation(
            Map<Integer, List<int[]>> directionMarks,
            Map<Integer, Integer> directionScores
        ){
        Timer markingTimer = new Timer(100, null); // 100毫秒間隔
        final int[] currentDirectionIdx = { 0 };
        final int[] currentPositionIdx = { 0 };

        // 獲取所有方向的列表
        Integer[] directions = directionMarks.keySet().toArray(new Integer[0]);

        // 毋須標記時直接返回
        if (directions.length == 0) {
            updateStatus();
            return;
        }

        ActionListener animationListener = (ActionEvent e) -> {
            // 檢查是否完成所有方向
            if (currentDirectionIdx[0] >= directions.length) {
                markingTimer.stop();
                
                // 動畫完成後調用updateStatus
                SwingUtilities.invokeLater(() -> {
                    updateStatus();
                });
                return;
            }
            
            int currentDirection = directions[currentDirectionIdx[0]];
            List<int[]> positions = directionMarks.get(currentDirection);
            
            // 檢查是否完成當前方向的所有位置
            if (currentPositionIdx[0] >= positions.size()) {
                // 顯示得分動畫
                int score = directionScores.getOrDefault(currentDirection, 0);
                if (score > 0) {
                    int[] midPoint = game.getDirectionMiddlePosition(currentDirection);
                    if (midPoint != null) {
                        // 當前玩家（因為已經切換了玩家，所以需要取反向的玩家）
                        int player = (game.getCurrentPlayer() == 1) ? 2 : 1;
                        Color scoreColor = (player == 1) ? PLAYER1_COLOR : PLAYER2_COLOR;
                        
                        // 使用放大動畫效果顯示得分
                        showFM("+" + score + "分", midPoint[0], midPoint[1], scoreColor);
                    }
                }
                
                // 進入下一個方向
                currentDirectionIdx[0]++;
                currentPositionIdx[0] = 0;
                return;
            }
            
            // 突出顯示當前位置
            int[] pos = positions.get(currentPositionIdx[0]);
            highlightButton(pos[0], pos[1]);
            
            // 移動到下一個位置
            currentPositionIdx[0]++;
        };

        markingTimer.addActionListener(animationListener);
        markingTimer.start();
    }

    // 突出顯示按鈕
    private void highlightButton(int row, int col) {
        int cell = game.getBoardState()[row][col];
        if (cell != 4 && cell != 5) {
            JButton button = buttons[row][col];
            button.setBackground(MARKED_COLOR);
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    public void updateStatus() {
        int currentPlayer = game.getCurrentPlayer();
        int[] scores = game.getPlayerScores();

        playerTurnLabel.setText("當前玩家: " + (game.isBot(currentPlayer) ? "AI" : "玩家") + currentPlayer + "(" + (currentPlayer == 1 ? "O" : "X") + ")");
        player1ScoreLabel.setText((game.isBot(1) ? "AI" : "玩家") + "1(O)得分: " + scores[0]);
        player2ScoreLabel.setText((game.isBot(2) ? "AI" : "玩家") + "2(X)得分: " + scores[1]);

        // 更新玩家回合指示 (更改文字顏色)
        if (currentPlayer == 1) {
            playerTurnLabel.setForeground(PLAYER1_COLOR);
            mainPanel.setBackground(PLAYER1_COLOR);
        } else {
            playerTurnLabel.setForeground(PLAYER2_COLOR);
            mainPanel.setBackground(PLAYER2_COLOR);
        }
        mainPanel.repaint();
        updating = false;
        if (game.isGameOver()) {
            showGameResult();
            return;
        }
        if (game.isBot(currentPlayer)) {
            game.guiGetBotMove(currentPlayer);
        }
    }
    @SuppressWarnings("UseTextBlock")
    public void showGameResult() {
        int winner = game.getWinner();
        System.out.println("winner: " + winner);
        String message = "遊戲結束！" 
        + "\n\n" 
        + (game.isBot(1) ? "AI" : "玩家") + "1得分: " + game.getPlayerScores()[0] 
        + "\n" 
        + (game.isBot(2) ? "AI" : "玩家") + "2得分: " + game.getPlayerScores()[1] 
        + "\n\n" 
        + (winner == -1 ? "平手！" : (game.isBot(winner) ? "AI" : "玩家") + (winner) + "獲勝！");

        int option = JOptionPane.showConfirmDialog(this,
                message + "\n\n是否要再玩一局？",
                "遊戲結束", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    // 重新開始遊戲
    private void restartGame() {
        int option = JOptionPane.YES_OPTION;

        // 如果遊戲尚未結束，確認是否重新開始
        if (!game.isGameOver()) {
            option = JOptionPane.showConfirmDialog(this,
                    "確定要重新開始遊戲嗎？目前的進度將會丟失。",
                    "重新開始", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }

        if (option == JOptionPane.YES_OPTION) {
            // 創建新的遊戲實例
            game = new Game(true, game.getGameAI1(), game.getGameAI2(), this, false);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    game.initGame();
                    setAI();
                    updateBoard();
                    return null;
                }
            }.execute();
            // 更新界面
        }
    }

    // 顯示遊戲規則
    private void showGameRules() {
        String rules = """
                遊戲規則：

                1. 遊戲在9x9的棋盤上進行
                2. 玩家輪流在空位放置棋子（玩家1為O，玩家2為X）
                3. 計分規則：
                   - 當形成3的倍數（3、6、9）的連續棋子時可得分
                   - 每次落子後，檢查4個方向（水平、垂直、左上至右下、右上至左下）的連續棋子
                   - 如果連續棋子數是3的倍數，則得到相應分數（即棋子數）
                   - 得分的棋子會被金色標記
                   - 可以同時在多個方向得分
                4. 當棋盤下滿後，得分最高的玩家獲勝,如果平手則平手
                """;

        JTextArea textArea = new JTextArea(rules);
        textArea.setEditable(false);
        textArea.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "遊戲規則", JOptionPane.INFORMATION_MESSAGE);
    }
}