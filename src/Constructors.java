import java.util.ArrayList;

public class Constructors {
    private ArrayList<Integer> bombPositions = new ArrayList<>();
    private int[] numbers = new int[100];
    private int powerUpPosition = -1;

    //Slumpar platser för bomber
    public Constructors() {
        while (bombPositions.size() < 10) {
            int pos = (int) (Math.random() * 100);

            if (!bombPositions.contains(pos)) {
                bombPositions.add(pos);
            }

        }
        //Debugging
        System.out.println(bombPositions);

        for (int i = 0; i < 100; i++) {
            if (!bombPositions.contains(i)) {
                numbers[i] = countAdjacentBombs(i);
            } else {
                numbers[i] = -1; // -1 = bomb
            }
        }

        //Skapar powerup
        do {
            powerUpPosition = (int) (Math.random() * 100);
        } while (bombPositions.contains(powerUpPosition));

        //Debugging
        System.out.println(powerUpPosition);
    }



    public boolean isBomb(int index) {
        return bombPositions.contains(index);
    }


    public int getNumberAt(int index) {
        return numbers[index];
    }

    //Räknar antalet bomber runt en ruta
    public int countAdjacentBombs(int index) {
        int count = 0;
        int[] neighbors = {-11, -10, -9, -1, 1, 9, 10, 11};

        for (int offset : neighbors) {
            int n = index + offset;


            if (n < 0 || n >= 100) {
                continue;
            }

            //Kolumn 0
            if (index % 10 == 0 && (offset == -11 || offset == -1 || offset == 9)) {
                continue;
            }

            //Kolumn 9
            if (index % 10 == 9 && (offset == -9 || offset == 1 || offset == 11)) {
                continue;
            }

            if (bombPositions.contains(n)) {
                count++;
            }
        }

        return count;
    }

    public int getPowerUpPosition() {
        return powerUpPosition;
    }

}