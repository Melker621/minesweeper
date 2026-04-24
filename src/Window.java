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

    public Window(boolean firstTime) {

        if (firstTime) {
            JOptionPane.showMessageDialog(
                    null,
                    """
                            💣 Välkommen till Minesweeper!
                            
                            🖱️ Vänsterklick – avslöja en ruta
                            🚩 Högerklick – markera en ruta som misstänkt bomb
                            💥 Klickar du på en bomb förlorar du!
                            ⭐ Indikerar att du hittat en power-up!
                            
                            1 = En bomb i närheten
                            2 = Två bomber i närheten
                            osv...
                            """,
                    "Hur man spelar",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }

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

        JFrame gameWindow = new JFrame("Minesweeper");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);

        Constructors bombs = new Constructors(choice);
        flagsLeft = bombs.getBombCount();

        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setBackground(Color.DARK_GRAY);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        timerLabel = new JLabel("Tid: 0 sek", SwingConstants.CENTER);
        timerLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        timerLabel.setForeground(Color.RED);

        flagLabel = new JLabel("Flaggor: " + flagsLeft, SwingConstants.CENTER);
        flagLabel.setFont(new Font("Monospaced", Font.BOLD, 24));
        flagLabel.setForeground(Color.RED);

        topPanel.add(timerLabel);
        topPanel.add(flagLabel);

        gameTimer = new Timer(1000, e -> {
            secondsPassed++;
            timerLabel.setText("Tid: " + secondsPassed + " sek");
        });
        gameTimer.start();

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.DARK_GRAY);
        mainPanel.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, bombs.getCols()));

        int boardWidth = bombs.getCols() * 40;
        int boardHeight = 400;
        panel.setPreferredSize(new Dimension(boardWidth, boardHeight));

        JButton[] buttons = new JButton[bombs.getTotalCells()];
        for (int i = 0; i < bombs.getTotalCells(); i++) {
            JButton button = new JButton();
            button.setBackground(new Color(180, 180, 180));
            button.setOpaque(true);
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            buttons[i] = button;
            final int index = i;

            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    if (SwingUtilities.isRightMouseButton(e)) {
                        if (button.isEnabled()) {
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
                        return;
                    }

                    else if (SwingUtilities.isLeftMouseButton(e)) {
                        if (!button.isEnabled() || "🚩".equals(button.getText())) {
                            return;
                        }

                        if (bombs.isPowerUp(index)) {
                            java.util.List<Integer> available = new java.util.ArrayList<>();
                            for (int i = 0; i < bombs.getTotalCells(); i++) {
                                if (bombs.isBomb(i) && !"🚩".equals(buttons[i].getText()) && buttons[i].isEnabled()) {
                                    available.add(i);
                                }
                            }

                            if (!available.isEmpty()) {
                                int revealIndex = available.get((int)(Math.random() * available.size()));
                                buttons[revealIndex].setText("\uD83D\uDCA3");
                                buttons[revealIndex].setEnabled(false);
                                buttons[revealIndex].setBackground(new Color(255,180,180));
                                flagsLeft--;
                                flagLabel.setText("Flaggor: " + flagsLeft);
                            }

                            int num = bombs.countAdjacentBombs(index);
                            if (num >= 1) {
                                button.setText(num + "⭐");
                                button.setFont(new Font("Monospaced", Font.BOLD, 18));
                            } else {
                                bombs.revealZeros(buttons, index);
                                button.setText("⭐");
                            }
                            button.setBackground(new Color(230, 230, 230));
                            button.setEnabled(false);
                            if (checkWin(buttons, bombs)) {
                                triggerWin(gameWindow);
                            } else {
                                JOptionPane.showMessageDialog(null, "Du fick en power-up! En bomb har avslöjats.");
                            }
                            return;
                        }

                        if (bombs.isBomb(index)) {
                            gameTimer.stop();
                            for (int j = 0; j < bombs.getTotalCells(); j++) {
                                if (bombs.isBomb(j)) buttons[j].setText("\uD83D\uDCA3");
                                buttons[j].setEnabled(false);
                            }
                            Timer timer = new Timer(1000, a -> {
                                int res = JOptionPane.showConfirmDialog(gameWindow, "BOOM! Vill du spela igen?", "Game Over", JOptionPane.YES_NO_OPTION);
                                if (res == JOptionPane.YES_OPTION) {
                                    restart(gameWindow);
                                } else {
                                    System.exit(0);
                                }
                            });

                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            int num = bombs.countAdjacentBombs(index);
                            if (num == 0) {
                                bombs.revealZeros(buttons, index);
                            } else {
                                button.setText(String.valueOf(num));
                                button.setBackground(new Color(230, 230, 230));
                                button.setFont(new Font("Monospaced", Font.BOLD, 18));
                                button.setEnabled(false);
                            }

                            if (checkWin(buttons, bombs)) {
                                triggerWin(gameWindow);
                            }
                        }
                    }
                }
            });
            panel.add(button);
        }

        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(Color.DARK_GRAY);
        gameContainer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        gameContainer.add(topPanel, BorderLayout.NORTH);
        gameContainer.add(panel, BorderLayout.CENTER);
        gameContainer.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setBackground(new Color(78, 78, 78));
        boardWrapper.add(gameContainer);

        mainPanel.add(boardWrapper, BorderLayout.CENTER);
        gameWindow.add(mainPanel, BorderLayout.CENTER);
        gameWindow.setVisible(true);
    }
    private boolean checkWin(JButton[] buttons, Constructors bombs) {
        for (int i = 0; i < buttons.length; i++) {
            if (!bombs.isBomb(i) && buttons[i].isEnabled() && !bombs.isPowerUp(i)) {
                return false;
            }
        }
        return true;
    }

    private void triggerWin(JFrame gameWindow) {
        gameTimer.stop();
        Timer winTimer = new Timer(500, a -> {
            int res = JOptionPane.showConfirmDialog(gameWindow, "🎉 Grattis! Du vann!\nTid: " + secondsPassed + " sekunder\nVill du spela igen?", "Vinst", JOptionPane.YES_NO_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                restart(gameWindow);
            } else {
                System.exit(0);
            }
        });
        winTimer.setRepeats(false);
        winTimer.start();
    }

    private void restart(JFrame currentWindow) {
        currentWindow.dispose();
        new Window(false);
    }
}