import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EtchedBorder;

public class Window {

    private int secondsPassed = 0;
    private JLabel timerLabel;
    private JLabel flagLabel;
    private Timer gameTimer;
    private int flagsLeft;
    private JLabel highscoreLabel;
    private String currentDifficulty;
    private HighScoreManager scoreManager = new HighScoreManager();
    private int cellSize = 40;
    private JButton[] buttons;
    private JPanel panel;
    private Constructors bombs;


    //Starts window, shows rules, and sets difficulty
    public Window(boolean firstTime) {

        //Show rules on first launch only
        if (firstTime) {
            JOptionPane.showMessageDialog(
                    null,
                    """
                            💣 Välkommen till Minesweeper!
                            
                            🖱️ Vänsterklick – avslöja en ruta
                            🚩 Högerklick – markera en ruta som misstänkt bomb
                            💥 Klickar du på en bomb förlorar du!
                            ⭐ Indikerar att du hittat en power-up, power-ups kan visa platsen på upp till 4 bomber!
                            
                            1 = En bomb i närheten
                            2 = Två bomber i närheten
                            osv...
                            """,
                    "Hur man spelar",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

        // Difficulty selection
        String[] options = {"Lätt", "Mellan", "Svår"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Välj svårighetsgrad:",
                "Inställningar",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == -1) choice = 0;
        currentDifficulty = options[choice];

        //Adjust size based on difficulty
        if (currentDifficulty.equals("Svår")) {
            cellSize = 25;
        } else if (currentDifficulty.equals("Mellan")) {
            cellSize = 35;
        }

        //Main window
        JFrame gameWindow = new JFrame("Minesweeper");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);

        this.bombs = new Constructors(choice);
        flagsLeft = bombs.getBombCount();


        //Top panel: timer, flags, and zoom
        JPanel topPanel = new JPanel(new GridLayout(1, 3));
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        timerLabel = new JLabel("Tid: 0 sek", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        timerLabel.setForeground(Color.RED);

        flagLabel = new JLabel("Flaggor: " + flagsLeft, SwingConstants.CENTER);
        flagLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        flagLabel.setForeground(Color.RED);

        //Zoom controls
        JPanel zoomPanel = new JPanel(new FlowLayout());
        zoomPanel.setBackground(Color.DARK_GRAY);

        JButton zoomOut = new JButton("-");
        zoomOut.setFocusPainted(false);
        zoomOut.setBackground(new Color(180, 180, 180));
        zoomOut.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        zoomOut.setFont(new Font("Monospaced", Font.BOLD, 20));
        zoomOut.setPreferredSize(new Dimension(45, 45));

        JButton zoomIn = new JButton("+");
        zoomIn.setFocusPainted(false);
        zoomIn.setBackground(new Color(180, 180, 180));
        zoomIn.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        zoomIn.setFont(new Font("Monospaced", Font.BOLD, 20));
        zoomIn.setPreferredSize(new Dimension(45, 45));

        zoomIn.addActionListener(e -> updateSize(2));
        zoomOut.addActionListener(e -> updateSize(-2));

        zoomPanel.add(zoomOut);
        zoomPanel.add(zoomIn);

        topPanel.add(timerLabel);
        topPanel.add(flagLabel);
        topPanel.add(zoomPanel);


        //Game grid part depending on difficulty
        this.panel = new JPanel();
        panel.setLayout(new GridLayout(0, bombs.getCols()));
        panel.setBackground(Color.DARK_GRAY);
        this.buttons = new JButton[bombs.getTotalCells()];

        //Starts timer
        gameTimer = new Timer(1000, e -> {
            secondsPassed++;
            timerLabel.setText("Tid: " + secondsPassed + " sek");
        });
        gameTimer.start();


        //Creates and configures all buttons
        for (int i = 0; i < bombs.getTotalCells(); i++) {
            JButton button = new JButton();
            button.setFocusPainted(false);
            button.setRolloverEnabled(false);
            button.setContentAreaFilled(false);
            button.setOpaque(true);
            button.setBackground(new Color(180, 180, 180));
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            button.setMargin(new Insets(0, 0, 0, 0));
            button.setPreferredSize(new Dimension(cellSize, cellSize));

            buttons[i] = button;
            final int index = i;

            //Makes so right/left clicks work
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    //Right click puts/removes flag
                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (button.getBackground().equals(new Color(180, 180, 180))) {
                            if (!"🚩".equals(button.getText())) {
                                if (flagsLeft > 0) {
                                    button.setText("🚩");
                                    flagsLeft--;
                                }
                            } else {
                                button.setText("");
                                flagsLeft++;
                            }
                            flagLabel.setText("Flaggor: " + flagsLeft);
                        }
                    }

                    //left click reveals cell
                    else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (button.getBackground().equals(new Color(230, 230, 230)) || "🚩".equals(button.getText())) {
                            return;
                        }

                        //Check for power-up
                        if (bombs.isPowerUp(index)) {
                            java.util.List<Integer> available = new java.util.ArrayList<>();
                            for (int k = 0; k < bombs.getTotalCells(); k++) {
                                if (bombs.isBomb(k) && !"🚩".equals(buttons[k].getText()) && buttons[k].getBackground().equals(new Color(180, 180, 180))) {
                                    available.add(k);
                                }
                            }
                            if (!available.isEmpty()) {
                                int revealIndex = available.get((int)(Math.random() * available.size()));
                                buttons[revealIndex].setText("💣");
                                buttons[revealIndex].setBackground(new Color(255,180,180));

                                //If bomb you can't interact
                                for (java.awt.event.MouseListener ml : buttons[revealIndex].getMouseListeners()) {
                                    buttons[revealIndex].removeMouseListener(ml);
                                }
                                flagsLeft--;
                                flagLabel.setText("Flaggor: " + flagsLeft);
                            }

                            //power-ups shows as a star
                            int num = bombs.countAdjacentBombs(index);
                            if (num >= 1) {
                                button.setText(num + "⭐");
                                button.setForeground(getNumberColor(num));
                                button.setFont(new Font("Monospaced", Font.BOLD, (int)(cellSize * 0.5)));
                            } else {
                                bombs.revealZeros(buttons, index);
                                button.setText("⭐");
                            }
                            button.setBackground(new Color(230, 230, 230));
                            for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
                                button.removeMouseListener(ml);
                            }
                            if (checkWin(buttons, bombs)) triggerWin(gameWindow);
                            return;
                        }

                        //Game over if bomb
                        if (bombs.isBomb(index)) {
                            gameTimer.stop();
                            for (int j = 0; j < bombs.getTotalCells(); j++) {
                                if (bombs.isBomb(j)) {
                                    buttons[j].setText("💣");
                                    buttons[j].setBackground(new Color(255, 100, 100));
                                }
                                for (java.awt.event.MouseListener ml : buttons[j].getMouseListeners()) {
                                    buttons[j].removeMouseListener(ml);
                                }
                            }
                            Timer timer = new Timer(1000, a -> {
                                int res = JOptionPane.showConfirmDialog(gameWindow, "BOOM! Vill du spela igen?", "Game Over", JOptionPane.YES_NO_OPTION);
                                if (res == JOptionPane.YES_OPTION) restart(gameWindow);
                                else System.exit(0);
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }

                        //reveals normal squares when selcted
                        else {
                            int num = bombs.countAdjacentBombs(index);
                            if (num == 0) {
                                bombs.revealZeros(buttons, index);
                            } else {
                                button.setText(String.valueOf(num));
                                button.setForeground(getNumberColor(num));
                                button.setBackground(new Color(230, 230, 230));
                                button.setFont(new Font("Monospaced", Font.BOLD, (int)(cellSize * 0.5)));
                                for (java.awt.event.MouseListener ml : button.getMouseListeners()) {
                                    button.removeMouseListener(ml);
                                }
                            }
                            if (checkWin(buttons, bombs)) triggerWin(gameWindow);
                        }
                    }
                }
            });
            panel.add(button);
        }


        //Side panel for highscores
        highscoreLabel = new JLabel(gethighscoreText(), SwingConstants.CENTER);
        highscoreLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        highscoreLabel.setForeground(Color.YELLOW);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.DARK_GRAY);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
        rightPanel.add(highscoreLabel, BorderLayout.CENTER);


        // GUI Assembly
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.DARK_GRAY);

        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(Color.DARK_GRAY);
        gameContainer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        gameContainer.add(topPanel, BorderLayout.NORTH);
        gameContainer.add(panel, BorderLayout.CENTER);
        gameContainer.add(rightPanel, BorderLayout.EAST);
        gameContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setBackground(new Color(78, 78, 78));
        boardWrapper.add(gameContainer);

        mainPanel.add(boardWrapper, BorderLayout.CENTER);
        gameWindow.add(mainPanel, BorderLayout.CENTER);
        gameWindow.setVisible(true);
    }


    //Changes zoom-limits depending on difficulty and updates size
    private void updateSize(int delta) {
        int newSize = cellSize + delta;
        int minLimit = 15;
        int maxLimit;

        switch(currentDifficulty) {
            case "Lätt" -> maxLimit = 100;
            case "Mellan" -> maxLimit = 65;
            case "Svår" -> maxLimit = 40;
            default -> maxLimit = 80;
        }

        if (newSize < minLimit || newSize > maxLimit) return;

        cellSize = newSize;
        int fontSize = (int) (cellSize * 0.5);
        for (JButton b : buttons) {
            b.setPreferredSize(new Dimension(cellSize, cellSize));
            b.setFont(new Font("Monospaced", Font.BOLD, fontSize));
        }
        panel.revalidate();
        panel.repaint();
    }


    //check win
    private boolean checkWin(JButton[] buttons, Constructors bombs) {
        for (int i = 0; i < buttons.length; i++) {
            if (!bombs.isBomb(i) && buttons[i].getBackground().equals(new Color(180, 180, 180))) {
                return false;
            }
        }
        return true;
    }


    //Shows if win and updates highscore, restart option
    private void triggerWin(JFrame gameWindow) {
        gameTimer.stop();
        scoreManager.addScore(secondsPassed, currentDifficulty);
        highscoreLabel.setText(gethighscoreText());

        Timer winTimer = new Timer(500, a -> {
            int res = JOptionPane.showConfirmDialog(gameWindow, "🎉 Grattis! Du vann!\nTid: " + secondsPassed + " sekunder\nVill du spela igen?", "Vinst", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) restart(gameWindow);
            else System.exit(0);
        });
        winTimer.setRepeats(false);
        winTimer.start();
    }


    //Restarts game
    private void restart(JFrame currentWindow) {
        currentWindow.dispose();
        new Window(false);
    }


    //Puts colors on adjacent bomb numbers
    private Color getNumberColor(int num) {
        return switch (num) {
            case 1 -> Color.BLUE;
            case 2 -> new Color(0, 128, 0);
            case 3 -> Color.RED;
            case 4 -> new Color(0, 0, 128);
            case 5 -> new Color(128, 0, 0);
            case 6 -> new Color(0, 128, 128);
            default -> Color.BLACK;
        };
    }


    //Shows high scores with HTML
    private String gethighscoreText() {
        java.util.List<Integer> topScores = scoreManager.getTopThree(currentDifficulty);
        StringBuilder sb = new StringBuilder("<html><div style='text-align: center;'>");
        sb.append("<b style='color: yellow;'>TOP 3 (" + currentDifficulty.toUpperCase() + "):</b><br><br>");

        if (topScores.isEmpty()) {
            sb.append("-<br>-<br>-");
        } else {
            for (int i = 0; i < topScores.size(); i++) {
                sb.append((i + 1) + ". " + topScores.get(i) + "s<br>");
            }
            for (int i = topScores.size(); i < 3; i++) {
                sb.append((i + 1) + ". -<br>");
            }
        }

        sb.append("</div></html>");
        return sb.toString();
    }
}