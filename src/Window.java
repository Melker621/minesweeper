import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;

public class Window {

    private int secondsPassed = 0;
    private JLabel timerLabel;
    private JLabel flagLabel;
    private Timer gameTimer;
    private int flagsLeft;

    public Window() {

        //Start skärm som säger hur spelet funkar och hur man kör
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
        // choice blir nu:
// 0 om man trycker på "Lätt"
// 1 om man trycker på "Mellan"
// 2 om man trycker på "Svår"
// -1 om man stänger rutan

        //Skapa fönstret
        JFrame gameWindow = new JFrame("Minesweeper");
        gameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameWindow.setSize(800, 800);

        Constructors bombs = new Constructors();
        flagsLeft = 10;

        //Timer + flaglayout
        JPanel topPanel = new JPanel(new GridLayout(1, 2));
        topPanel.setBackground(Color.DARK_GRAY);

        // Lägger till en ram och lite luft runt texten
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

        //Skapar panels
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.DARK_GRAY);
        mainPanel.setLayout(new BorderLayout());

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(10, 10));
        panel.setPreferredSize(new Dimension(600, 600));
        panel.setMaximumSize(new Dimension(600, 600));
        panel.setMinimumSize(new Dimension(600, 600));

        JButton[] buttons = new JButton[100];
        for (int i = 0; i < 100; i++) {
            JButton button = new JButton();
            button.setBackground(new Color(180, 180, 180));
            button.setOpaque(true);
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

            buttons[i] = button;
            final int index = i;

            // Klick-hantering
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {

                    // Högerklick - flagg
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

                    // Vänsterklick avslöjar ruta
                    else if (SwingUtilities.isLeftMouseButton(e)) {

                        if (!button.isEnabled() || "🚩".equals(button.getText())) {
                            return;
                        }

                        if (index == bombs.getPowerUpPosition()) {

                            //Hitta bomb utan flagga
                            java.util.List<Integer> available = new java.util.ArrayList<>();

                            for (int i = 0; i < 100; i++) {
                                if (bombs.isBomb(i) && !"🚩".equals(buttons[i].getText())) {
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
                            } else if (num == 0) {
                                revealZeros(buttons, bombs, index);
                                button.setText("⭐");

                            }
                            button.setBackground(new Color(230, 230, 230));
                            button.setEnabled(false);

                            JOptionPane.showMessageDialog(
                                    null,
                                    "Du fick en power-up! En bomb har avslöjats åt dig."
                            );

                            return;
                        }

                        // Klick på redan avslöjad/flagga
                        if (!button.isEnabled() || "🚩".equals(button.getText())) {
                            return;
                        }

                        // Klickar på bomb
                        if (bombs.isBomb(index)) {

                            gameTimer.stop();

                            for (int j = 0; j < 100; j++) {
                                if (bombs.isBomb(j)) {
                                    buttons[j].setText("\uD83D\uDCA3");
                                }
                                buttons[j].setEnabled(false);
                            }

                            Timer timer = new Timer(1000, a -> {
                                JOptionPane.showMessageDialog(gameWindow, "BOOM! Du hittade en bomb!");
                            });
                            timer.setRepeats(false);
                            timer.start();

                        } else {
                            int num = bombs.countAdjacentBombs(index);

                            if (num == 0) {
                                revealZeros(buttons, bombs, index);
                            } else {
                                button.setText(String.valueOf(num));
                                button.setBackground(new Color(230, 230, 230));
                                button.setFont(new Font("Monospaced", Font.BOLD, 18));
                                button.setEnabled(false);
                            }

                            //Vinst beräkning
                            if (checkWin(buttons, bombs)) {

                                gameTimer.stop();

                                Timer winTimer = new Timer(500, a -> {
                                    JOptionPane.showMessageDialog(gameWindow, "🎉 Grattis! Du vann spelet!" +
                                            "\nTid: " + secondsPassed + " sekunder");
                                });
                                winTimer.setRepeats(false);
                                winTimer.start();

                                for (JButton b : buttons) {
                                    b.setEnabled(false);
                                }
                            }
                        }
                    }

                }
            });

            panel.add(button);
        }

        // Här skapar vi en samlad container för både info och spelplan
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.setBackground(Color.DARK_GRAY);
        gameContainer.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

        gameContainer.add(topPanel, BorderLayout.NORTH);
        gameContainer.add(panel, BorderLayout.CENTER);

        Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        gameContainer.setBorder(padding);

        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setBackground(new Color(78, 78, 78));
        boardWrapper.add(gameContainer);

        mainPanel.add(boardWrapper, BorderLayout.CENTER);

        gameWindow.add(mainPanel, BorderLayout.CENTER);
        gameWindow.setVisible(true);
    }

    //Funktionen som beräknar alla nollor
    private void revealZeros(JButton[] buttons, Constructors bombs, int index) {
        int[] neighbors = {-11, -10, -9, -1, 1, 9, 10, 11};

        if (index < 0 || index >= 100) return;
        if (!buttons[index].isEnabled()) return;

        int num = bombs.getNumberAt(index);
        if (num > 0) {
            buttons[index].setText(String.valueOf(num));
            buttons[index].setFont(new Font("Monospaced", Font.BOLD, 18));
            buttons[index].setBackground(new Color(230, 230, 230));
            buttons[index].setEnabled(false);
            return;
        }

        buttons[index].setText("");
        buttons[index].setBackground(new Color(230, 230, 230));
        buttons[index].setEnabled(false);

        for (int offset : neighbors) {
            int n = index + offset;
            if (n < 0 || n >= 100) continue;

            if (index % 10 == 0 && (offset == -11 || offset == -1 || offset == 9)) continue;
            if (index % 10 == 9 && (offset == -9 || offset == 1 || offset == 11)) continue;

            revealZeros(buttons, bombs, n);
        }
    }

    //Funktionen som kollar om man vunnit
    private boolean checkWin(JButton[] buttons, Constructors bombs) {
        for (int i = 0; i < buttons.length; i++) {
            if (!bombs.isBomb(i) && buttons[i].isEnabled()) {
                return false;
            }
        }
        return true;
    }
}
