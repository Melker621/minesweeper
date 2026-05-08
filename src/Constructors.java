import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class Constructors {
    private ArrayList<Integer> bombPositions = new ArrayList<>();
    private ArrayList<Integer> powerUpPositions = new ArrayList<>(); // Nu en lista
    private int[] numbers;

    private int cols;
    private int totalCells;
    private int bombCount;

    public Constructors(int choice) {
        int powerUpCount;

        //Switch-case for difficulty
        switch (choice) {
            case 1 -> {
                cols = 20;
                totalCells = 200;
                bombCount = 25;
                powerUpCount = 2;
            }
            case 2 -> {
                cols = 40;
                totalCells = 400;
                bombCount = 50;
                powerUpCount = 4;
            }
            default -> {
                cols = 10;
                totalCells = 100;
                bombCount = 10;
                powerUpCount = 1;
            }
        }
        //gives cells a value based on how many bombs
        this.numbers = new int[totalCells];


        //random placement of bombs
        while (bombPositions.size() < bombCount) {
            int pos = (int) (Math.random() * totalCells);
            if (!bombPositions.contains(pos)) {
                bombPositions.add(pos);
            }
        }

        //value to numbers
        for (int i = 0; i < totalCells; i++) {
            if (!bombPositions.contains(i)) {
                numbers[i] = countAdjacentBombs(i);
            } else {
                numbers[i] = -1;
            }
        }

        //placement of powerups
        while (powerUpPositions.size() < powerUpCount) {
            int pos = (int) (Math.random() * totalCells);
            if (!bombPositions.contains(pos) && !powerUpPositions.contains(pos)) {
                powerUpPositions.add(pos);
                System.out.print(powerUpPositions);
            }
        }
    }

    //Checks if cell is a bomb or powerup
    public boolean isBomb(int index) {
        return bombPositions.contains(index);
    }

    public boolean isPowerUp(int index) {
        return powerUpPositions.contains(index);
    }

    //Returns the count for adjacent bombs
    public int getNumberAt(int index) {
        return numbers[index];
    }

    //Counts adjacent bombs around a cell
    public int countAdjacentBombs(int index) {
        int count = 0;
        int[] neighbors = {
                -cols - 1, -cols, -cols + 1,
                -1,                1,
                cols - 1,  cols,  cols + 1
        };

        //Check amount of bombs around a cell, same if it's beside a wall
        for (int offset : neighbors) {
            int n = index + offset;
            if (n < 0 || n >= totalCells) continue;
            if (index % cols == 0 && (offset == -cols - 1 || offset == -1 || offset == cols - 1)) continue;
            if (index % cols == cols - 1 && (offset == -cols + 1 || offset == 1 || offset == cols + 1)) continue;

            if (bombPositions.contains(n)) {
                count++;
            }
        }
        return count;
    }

    //clears cells with value 0
    public void revealZeros(JButton[] buttons, int index) {
        if (index < 0 || index >= totalCells) return;
        if (buttons[index].getBackground().equals(new Color(230, 230, 230)) || "🚩".equals(buttons[index].getText())) {
            return;
        }

        int num = getNumberAt(index);
        buttons[index].setBackground(new Color(230, 230, 230));

        for (java.awt.event.MouseListener ml : buttons[index].getMouseListeners()) {
            buttons[index].removeMouseListener(ml);
        }

        //Gives the numbers color depending on amount of adjacent bombs
        if (num > 0) {
            buttons[index].setText(String.valueOf(num));
            Color c = switch (num) {
                case 1 -> Color.BLUE;
                case 2 -> new Color(0, 128, 0);
                case 3 -> Color.RED;
                case 4 -> new Color(0, 0, 128);
                case 5 -> new Color(128, 0, 0);
                case 6 -> new Color(0, 128, 128);
                default -> Color.BLACK;
            };
            buttons[index].setForeground(c);
            buttons[index].setFont(new Font("Monospaced", Font.BOLD, 18));
            return;
        }

        //If a cell is 0, we clear it and check neighbouring cells
        buttons[index].setText("");
        int[] neighbors = {-cols - 1, -cols, -cols + 1, -1, 1, cols - 1, cols, cols + 1};
        for (int offset : neighbors) {
            int n = index + offset;
            if (index % cols == 0 && (offset == -cols - 1 || offset == -1 || offset == cols - 1)) continue;
            if (index % cols == cols - 1 && (offset == -cols + 1 || offset == 1 || offset == cols + 1)) continue;
            if (n >= 0 && n < totalCells) {
                revealZeros(buttons, n);
            }
        }
    }


    public int getTotalCells() { return totalCells; }
    public int getCols() { return cols; }
    public int getBombCount() { return bombCount; }
}