package game.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.AbstractBorder;

import game.Game;
import myutil.Localization;

public final class GameGUI extends JFrame {
    private Game game;
    private JButton[][] buttons;
    private JPanel mainPanel;
    private JLabel playerTurnLabel;
    private JLabel player1ScoreLabel;
    private JLabel player2ScoreLabel;
    private boolean updating = false;
    private final int BOARD_SIZE = 9;
    private final Color PLAYER1_COLOR = new Color(70, 130, 180); // Blue
    private final Color PLAYER2_COLOR = new Color(220, 20, 60); // Red
    private final Color PLAYER1_SELECTED_HIGHLIGHT = new Color(100, 149, 237); // Light blue (selected)
    private final Color PLAYER2_SELECTED_HIGHLIGHT = new Color(255, 160, 122); // Light pink (selected)
    private final Color MARKED_COLOR = new Color(255, 215, 0); // Gold (marking)
    private final Color BG_COLOR = new Color(255, 255, 255); // White
    private final Color BOARD_BACKGROUND = new Color(240, 240, 240); // Light gray
    private final Color GRID_COLOR = new Color(120, 120, 120); // Dark gray
    private final Color ERROR_COLOR = new Color(255, 0, 0); // Error message color
    private final int CORNER_RADIUS = 15; // Corner radius
    
    private JPanel glassPane;// Used for displaying floating error messages

    private class showFloatingMessage {
        private JLabel messageLabel;
        private String message;
        private int row;
        private int col;
        private Color color;
        private Timer floatingTimer;
        private final GameGUI parentFrame; // Stores reference to outer class
        private int animationType; // Animation type: 0=fade up, 1=zoom out

        // Show floating message (default fade up animation)
        private showFloatingMessage(String message, int row, int col, Color color) {
            this(message, row, col, color, 0);
        }

        // Show floating message (with specified animation type)
        private showFloatingMessage(String message, int row, int col, Color color, int animationType) {
            this.message = message;
            this.row = row;
            this.col = col;
            this.color = color;
            this.parentFrame = GameGUI.this; // Save reference to outer class
            this.animationType = animationType;

            // Initialize floating message label - use transparent panel as background
            JPanel transparentPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    // Don't call super.paintComponent to keep fully transparent
                }

                @Override
                public boolean isOpaque() {
                    return false;
                }
            };
            transparentPanel.setLayout(new BorderLayout());

            // Create label
            messageLabel = new JLabel(message, JLabel.CENTER);
            messageLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            messageLabel.setForeground(color);
            messageLabel.setOpaque(false);

            // Add label to transparent panel
            transparentPanel.add(messageLabel, BorderLayout.CENTER);
            transparentPanel.setOpaque(false);
            transparentPanel.setVisible(false);

            // Add to glassPane
            glassPane.add(transparentPanel);

            // Display message
            displayMessage(transparentPanel);
        }

        @SuppressWarnings("CallToPrintStackTrace")
        private void displayMessage(JPanel messagePanel) {
            try {
                // Get button's absolute screen position
                Point buttonLocationOnScreen = buttons[row][col].getLocationOnScreen();
                // Get window's absolute screen position
                Point frameLocationOnScreen = parentFrame.getLocationOnScreen(); // Use outer class reference

                // Calculate button position relative to window
                int buttonX = buttonLocationOnScreen.x - frameLocationOnScreen.x;
                int buttonY = buttonLocationOnScreen.y - frameLocationOnScreen.y;

                // Set message label position (centered above button)
                FontMetrics fm = messageLabel.getFontMetrics(messageLabel.getFont());
                int textWidth = fm.stringWidth(message);

                // Set message label position (centered above button)
                int labelWidth = Math.max(textWidth + 20, 200);
                int labelHeight = 30;
                messagePanel.setBounds(
                        buttonX - (labelWidth - buttons[row][col].getWidth()) / 2,
                        buttonY - labelHeight,
                        labelWidth,
                        labelHeight);

                // Set message and display
                messageLabel.setText(message);
                messageLabel.setForeground(color);
                messagePanel.setVisible(true);

                // Choose different animation effects based on animation type
                if (animationType == 1) {
                    // Create animation effect - zoom out
                    createZoomOutAnimation(messagePanel);
                } else {
                    // Create animation effect - fade up (default)
                    createFadeUpAnimation(messagePanel);
                }

            } catch (Exception e) {
                System.out.println(Localization.getString("gui.show_message_error", e.getMessage()));
                e.printStackTrace();
            }
        }

        // Fade up animation
        private void createFadeUpAnimation(JPanel messagePanel) {
            final int startY = messagePanel.getY();
            final float[] alpha = { 1.0f };

            floatingTimer = new Timer(50, new ActionListener() {
                int steps = 0;

                @Override
                public void actionPerformed(ActionEvent e) {
                    steps++;

                    // Move up animation
                    messagePanel.setLocation(messagePanel.getX(), startY - steps * 2);

                    // Fade out
                    alpha[0] = Math.max(0, 1.0f - (steps * 0.05f));
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            (int) (alpha[0] * 255));
                    messageLabel.setForeground(newColor);

                    // Animation end, hide label
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

        // Zoom out animation (for score notifications)
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

                    // Scale animation
                    scale[0] = 1.0f + (steps * 0.1f);
                    int newSize = (int) (originalFont.getSize() * scale[0]);
                    messageLabel.setFont(new Font(originalFont.getFamily(), originalFont.getStyle(), newSize));

                    // Recalculate position to keep message centered
                    int newWidth = (int) (startWidth * scale[0]);
                    int newHeight = (int) (startHeight * scale[0]);
                    int newX = startX - (newWidth - startWidth) / 2;
                    int newY = startY - (newHeight - startHeight) / 2;
                    messagePanel.setBounds(newX, newY, newWidth, newHeight);

                    // Fade out
                    alpha[0] = Math.max(0, 1.0f - (steps * 0.05f));
                    Color newColor = new Color(
                            color.getRed(),
                            color.getGreen(),
                            color.getBlue(),
                            (int) (alpha[0] * 255));
                    messageLabel.setForeground(newColor);

                    // Animation end, hide label
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
    // Custom rounded corner border class
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

            // Create rounded rectangle path
            Path2D path = createRoundRectPath(x, y, width, height);

            // First, clear the entire area (make it transparent)
            if (c.getParent() != null) {
                // Get parent background color for correct background drawing
                Color parentBackground = c.getParent().getBackground();
                g2d.setColor(parentBackground);
                g2d.fillRect(x, y, width, height);
            }

            // Draw rounded fill area
            g2d.setColor(c.getBackground());
            g2d.fill(path);

            // Draw border
            g2d.setColor(color);
            g2d.draw(path);

            g2d.dispose();
        }

        // Create rounded rectangle path
        private Path2D createRoundRectPath(int x, int y, int width, int height) {
            Path2D path = new Path2D.Double();

            // Top-left corner
            if (topLeftRadius > 0) {
                path.moveTo(x, y + topLeftRadius);
                path.quadTo(x, y, x + topLeftRadius, y);
            } else {
                path.moveTo(x, y);
            }

            // Top-right corner
            if (topRightRadius > 0) {
                // Top line to top-right corner (leave space for curve)
                path.lineTo(x + width - topRightRadius, y);
                path.quadTo(x + width, y, x + width, y + topRightRadius);
            } else {
                // Top line to top-right corner
                path.lineTo(x + width, y);
            }

            // Bottom-right corner
            if (bottomRightRadius > 0) {
                // Right line to bottom-right corner (leave space for curve)
                path.lineTo(x + width, y + height - bottomRightRadius);
                path.quadTo(x + width, y + height, x + width - bottomRightRadius, y + height);
            } else {
                // Right line to bottom-right corner
                path.lineTo(x + width, y + height);
            }

            // Bottom-left corner
            if (bottomLeftRadius > 0) {
                // Bottom line to bottom-left corner (leave space for curve)
                path.lineTo(x + bottomLeftRadius, y + height);
                path.quadTo(x, y + height, x, y + height - bottomLeftRadius);
            } else {
                // Bottom line to bottom-left corner
                path.lineTo(x, y + height);
            }

            // Left line back to start
            path.lineTo(x, y + topLeftRadius);

            // Close path
            path.closePath();

            return path;
        }

        @Override
        public boolean isBorderOpaque() {
            return false; // Set to non-opaque for proper transparency handling
        }

        @Override
        public Insets getBorderInsets(Component c) {
            // Return appropriate insets to ensure enough space for rounded corners
            return new Insets(
                    Math.max(topLeftRadius, topRightRadius), // Top needs to consider larger of top-left and top-right
                    Math.max(topLeftRadius, bottomLeftRadius), // Left needs to consider larger of top-left and bottom-left
                    Math.max(bottomLeftRadius, bottomRightRadius), // Bottom needs to consider larger of bottom-left and bottom-right
                    Math.max(topRightRadius, bottomRightRadius) // Right needs to consider larger of top-right and bottom-right
            );
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            // Update existing insets object
            insets.top = Math.max(topLeftRadius, topRightRadius);
            insets.left = Math.max(topLeftRadius, bottomLeftRadius);
            insets.bottom = Math.max(bottomLeftRadius, bottomRightRadius);
            insets.right = Math.max(topRightRadius, bottomRightRadius);
            return insets;
        }
    }
    private void setAI() {
        // Show mode selection dialog
        String[] options = {Localization.getString("game.play_with_ai"), Localization.getString("game.play_with_friend"), Localization.getString("game.watch_ai")};
        int mode = JOptionPane.showOptionDialog(this,
                Localization.getString("gui.choose_mode"),
                Localization.getString("gui.mode_selection"),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);

        // Handle selected mode
        if (mode == 0) { // Play with AI
            // Show difficulty selection dialog
            String[] difficulties = {Localization.getString("game.easy"), Localization.getString("game.medium"), Localization.getString("game.hard")};
            int difficulty = JOptionPane.showOptionDialog(this,
                    Localization.getString("gui.choose_difficulty", ""),
                    Localization.getString("gui.difficulty_selection", ""),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // Set AI difficulty
            game.setBot(1, difficulty + 1); // Difficulty option index +1 corresponds to actual difficulty value
        } else if (mode == 2) { // Watch AI vs AI
            // Show first AI difficulty selection
            String[] difficulties = {Localization.getString("game.easy"), Localization.getString("game.medium"), Localization.getString("game.hard")};
            int difficulty1 = JOptionPane.showOptionDialog(this,
                    Localization.getString("gui.choose_difficulty", "1"),
                    Localization.getString("gui.difficulty_selection", "1"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // Show second AI difficulty selection
            int difficulty2 = JOptionPane.showOptionDialog(this,
                    Localization.getString("gui.choose_difficulty", "2"),
                    Localization.getString("gui.difficulty_selection", "2"),
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficulties,
                    difficulties[0]);

            // Set both AI difficulties
            game.setBot(1, difficulty1 + 1);
            game.setBot(2, difficulty2 + 1);
        }
    }
    public GameGUI(Game game) {
        // Initialize game
        this.game = game;
        setAI();
        setTitle(Localization.getString("title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 600));
        setPreferredSize(new Dimension(800, 800));

        // Set up glassPane for floating messages - using fully transparent panel
        glassPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
            }

            @Override
            public boolean isOpaque() {
                return false; // Ensure panel is fully transparent
            }
        };
        glassPane.setOpaque(false); // Set to non-opaque
        glassPane.setLayout(null); // Use absolute layout
        setGlassPane(glassPane);
        glassPane.setVisible(true);

        // Create main panel
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        mainPanel.setBackground(PLAYER1_COLOR);

        // Create status panel
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.NORTH);

        // Create title panel
        JPanel titlePanel = createTitlePanel();
        mainPanel.add(titlePanel, BorderLayout.SOUTH);

        // Create board with coordinate labels panel
        JPanel boardWithLabelsPanel = new JPanel(new BorderLayout(5, 5));
        boardWithLabelsPanel.setBackground(new Color(245, 245, 245, 0));

        // Create column labels (A-I)
        JPanel colLabelsPanel = new JPanel(new GridLayout(1, BOARD_SIZE));
        colLabelsPanel.setBackground(new Color(245, 245, 245, 0));
        for (int j = 0; j < BOARD_SIZE; j++) {
            JLabel colLabel = new JLabel(String.valueOf((char) ('A' + j)), JLabel.CENTER);
            colLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            colLabel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, j < BOARD_SIZE - 1 ? 0 : 1, Color.BLACK));
            colLabelsPanel.add(colLabel);
        }

        // Create top-left corner filler panel
        JPanel cornerPanel = new JPanel();
        cornerPanel.setBackground(new Color(245, 245, 245, 0));
        JLabel corner = new JLabel("  ", JLabel.RIGHT);
        corner.setFont(new Font("微軟正黑體", Font.BOLD, 16));
        cornerPanel.add(corner);

        // Create a left container panel to hold corner panel and row labels
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(245, 245, 245, 0));
        topPanel.add(cornerPanel, BorderLayout.WEST);
        topPanel.add(colLabelsPanel, BorderLayout.CENTER);

        // Create row labels (1-9)
        JPanel rowLabelsPanel = new JPanel(new GridLayout(BOARD_SIZE, 1));
        rowLabelsPanel.setBackground(new Color(0, 0, 0, 0));
        for (int i = 0; i < BOARD_SIZE; i++) {
            JLabel rowLabel = new JLabel(String.valueOf(i + 1) + " ", JLabel.RIGHT);
            rowLabel.setFont(new Font("微軟正黑體", Font.BOLD, 16));
            rowLabel.setBorder(BorderFactory.createMatteBorder(1, 0, i < BOARD_SIZE - 1 ? 0 : 1, 0, Color.BLACK));
            rowLabelsPanel.add(rowLabel);
        }

        // Create board panel
        JPanel boardPanel = createBoardPanel();

        // Add all panels
        boardWithLabelsPanel.add(topPanel, BorderLayout.NORTH);
        boardWithLabelsPanel.add(rowLabelsPanel, BorderLayout.WEST);
        boardWithLabelsPanel.add(boardPanel, BorderLayout.CENTER);
        // Add board to main panel
        mainPanel.add(boardWithLabelsPanel, BorderLayout.CENTER);

        // Add component resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // Recalculate button sizes
                updateButtonSizes();
            }
        });

        // Add to window
        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // Initial UI update
        updateBoard();
    }

    // Update button sizes to fit window
    private void updateButtonSizes() {
        // Get current window size
        int windowWidth = getWidth();
        int windowHeight = getHeight();

        // Set minimum button size
        int minButtonSize = 30;

        // Calculate available space
        int availableWidth = windowWidth - 100; // Subtract margins and label space
        int availableHeight = windowHeight - 200; // Subtract top and bottom space

        // Calculate appropriate button size
        int buttonSize = Math.min(availableWidth / BOARD_SIZE, availableHeight / BOARD_SIZE);
        buttonSize = Math.max(buttonSize, minButtonSize); // Ensure not smaller than minimum size

        // Update all button sizes on board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (buttons[i][j] != null) {
                    buttons[i][j].setPreferredSize(new Dimension(buttonSize, buttonSize));
                }
            }
        }

        // Re-layout
        revalidate();
    }

    private JPanel createTitlePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new RoundedCornerBorder(CORNER_RADIUS, CORNER_RADIUS, 0, 0, BG_COLOR));
        JLabel titleLabel = new JLabel(Localization.getString("title"), JLabel.CENTER);
        titleLabel.setFont(new Font("微軟正黑體", Font.BOLD, 24));

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(BG_COLOR);

        // Create rules button
        JButton rulesButton = new JButton(Localization.getString("gui.rules"));
        rulesButton.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        rulesButton.setFocusPainted(false);
        rulesButton.addActionListener(e -> showGameRules());

        // Create restart button
        JButton restartButton = new JButton(Localization.getString("gui.restart"));
        restartButton.setFont(new Font("微軟正黑體", Font.BOLD, 14));
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> restartGame());

        buttonPanel.add(rulesButton);
        buttonPanel.add(Box.createHorizontalStrut(10)); // Add spacing
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

        playerTurnLabel = new JLabel(Localization.getString("gui.current_player") + game.getCurrentPlayer(), JLabel.CENTER);
        playerTurnLabel.setFont(new Font("微軟正黑體", Font.BOLD, 18));

        JPanel scorePanel = new JPanel(new GridLayout(1, 2));
        scorePanel.setBackground(BG_COLOR);

        player1ScoreLabel = new JLabel(Localization.getString("gui.player_score", "1", "0"), JLabel.CENTER);
        player1ScoreLabel.setFont(new Font("微軟正黑體", Font.PLAIN, 16));
        player1ScoreLabel.setForeground(PLAYER1_COLOR);

        player2ScoreLabel = new JLabel(Localization.getString("gui.player_score", "2", "0"), JLabel.CENTER);
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

                // Set button coordinates as name (for easy identification)
                buttons[i][j].setName(i + "," + j);

                // Add piece placement event
                final int row = i;
                final int col = j;
                buttons[i][j].addActionListener((ActionEvent e) -> {
                    if (updating || game.isBot(game.getCurrentPlayer())) {
                        return;
                    }
                    handleButtonClick(row, col);
                });

                // Add mouse hover effect
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
            // Use modified showFloatingMessage class to display error (using default rise animation)
            showFM(Localization.getString("gui.already_placed"), row, col, ERROR_COLOR);
            return;
        }
        updating = true;

        // Update board and status after animation ends
        updateBoard();
    }

    public void updateBoard() {
        // Get positions and scores that need to be marked
        Map<Integer, List<int[]>> directionMarks = game.getDirectionMarks();
        Map<Integer, Integer> directionScores = game.getDirectionScores();
        int[][] board = game.getBoardState();

        // Reset all button backgrounds and text
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

        // If there are positions to mark, start animation and update status after it ends
        if (!directionMarks.isEmpty()) {
            startMarkingAnimation(directionMarks, directionScores);
        }
    }

    // Start marking animation
    private void startMarkingAnimation(
            Map<Integer, List<int[]>> directionMarks,
            Map<Integer, Integer> directionScores
        ){
        Timer markingTimer = new Timer(100, null); // 100ms interval
        final int[] currentDirectionIdx = { 0 };
        final int[] currentPositionIdx = { 0 };

        // Get list of all directions
        Integer[] directions = directionMarks.keySet().toArray(new Integer[0]);

        // Return directly if no marking is needed
        if (directions.length == 0) {
            updateStatus();
            return;
        }

        ActionListener animationListener = (ActionEvent e) -> {
            // Check if all directions are completed
            if (currentDirectionIdx[0] >= directions.length) {
                markingTimer.stop();
                
                // Call updateStatus after animation completes
                SwingUtilities.invokeLater(() -> {
                    updateStatus();
                });
                return;
            }
            
            int currentDirection = directions[currentDirectionIdx[0]];
            List<int[]> positions = directionMarks.get(currentDirection);
            
            // Check if all positions in current direction are completed
            if (currentPositionIdx[0] >= positions.size()) {
                // Show score animation
                int score = directionScores.getOrDefault(currentDirection, 0);
                if (score > 0) {
                    int[] midPoint = game.getDirectionMiddlePosition(currentDirection);
                    if (midPoint != null) {
                        // Current player (since player has switched, need to invert player)
                        int player = (game.getCurrentPlayer() == 1) ? 2 : 1;
                        Color scoreColor = (player == 1) ? PLAYER1_COLOR : PLAYER2_COLOR;
                        
                        // Show score with zoom animation effect
                        showFM("+" + score + Localization.getString("gui.score"), midPoint[0], midPoint[1], scoreColor);
                    }
                }
                
                // Move to next direction
                currentDirectionIdx[0]++;
                currentPositionIdx[0] = 0;
                return;
            }
            
            // Highlight current position
            int[] pos = positions.get(currentPositionIdx[0]);
            highlightButton(pos[0], pos[1]);
            
            // Move to next position
            currentPositionIdx[0]++;
        };

        markingTimer.addActionListener(animationListener);
        markingTimer.start();
    }

    // Highlight button
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
        if (game.isBot(currentPlayer)) {
            playerTurnLabel.setText(Localization.getString("gui.current_player") + "AI" + currentPlayer + "(" + (currentPlayer == 1 ? "O" : "X") + ")");
        } else {
            playerTurnLabel.setText(Localization.getString("gui.current_player") + Localization.getString("game.player") + currentPlayer + "(" + (currentPlayer == 1 ? "O" : "X") + ")");
        }
        if (game.isBot(1)) {
            player1ScoreLabel.setText(Localization.getString("gui.ai_score", "1", scores[0]));
        } else {
            player1ScoreLabel.setText(Localization.getString("gui.player_score", "1", scores[0]));
        }
        if (game.isBot(2)) {
            player2ScoreLabel.setText(Localization.getString("gui.ai_score", "2", scores[1]));
        } else {
            player2ScoreLabel.setText(Localization.getString("gui.player_score", "2", scores[1]));
        }

        // Update player turn indicator (change text color)
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
        String message = Localization.getString("gui.game_over") 
        + "\n\n" 
        + (game.isBot(1) ? Localization.getString("gui.ai_score", "1", game.getPlayerScores()[0]) : Localization.getString("gui.player_score", "1", game.getPlayerScores()[0])) 
        + "\n" 
        + (game.isBot(2) ? Localization.getString("gui.ai_score", "2", game.getPlayerScores()[1]) : Localization.getString("gui.player_score", "2", game.getPlayerScores()[1])) 
        + "\n\n" 
        + (winner == -1 ? Localization.getString("gui.draw") : (game.isBot(winner) ? Localization.getString("gui.ai_win", winner) : Localization.getString("gui.player_win", winner)));

        int option = JOptionPane.showConfirmDialog(this,
                message + "\n\n" + Localization.getString("gui.play_again"),
                Localization.getString("gui.game_over"), JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            restartGame();
        } else {
            System.exit(0);
        }
    }

    // Restart game
    private void restartGame() {
        int option = JOptionPane.YES_OPTION;

        // If game hasn't ended, confirm restart
        if (!game.isGameOver()) {
            option = JOptionPane.showConfirmDialog(this,
                    Localization.getString("gui.issure_to_restart"),
                    Localization.getString("gui.restart"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }

        if (option == JOptionPane.YES_OPTION) {
            // Create new game instance
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
            // Update interface
        }
    }

    // Show game rules
    private void showGameRules() {
        String rules = Localization.getString("gui.rules.text");

        JTextArea textArea = new JTextArea(rules);
        textArea.setEditable(false);
        textArea.setFont(new Font("微軟正黑體", Font.PLAIN, 14));
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, Localization.getString("gui.rules"), JOptionPane.INFORMATION_MESSAGE);
    }
}