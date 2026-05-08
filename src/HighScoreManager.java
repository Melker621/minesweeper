import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class HighScoreManager {

    // Skapar filnamn baserat på svårighetsgrad (t.ex. highscores_easy.txt)
    private String getFileName(String difficulty) {
        return "highscores_" + difficulty.toLowerCase() + ".txt";
    }

    // Lägger till ett nytt resultat och sparar de tre bästa
    public void addScore(int seconds, String difficulty) {
        List<Integer> scores = getAllScores(difficulty);
        scores.add(seconds);

        // Sorterar (lägst tid först) och behåller endast de 3 bästa tiderna
        List<Integer> topThree = scores.stream()
                .sorted()
                .limit(3)
                .collect(Collectors.toList());

        saveToFile(topThree, getFileName(difficulty));
    }

    // Returnerar listan med de bästa resultaten
    public List<Integer> getTopThree(String difficulty) {
        return getAllScores(difficulty);
    }

    // Skriver listan med poäng till en textfil
    private void saveToFile(List<Integer> scores, String fileName) {
        System.out.println("Försöker spara highscore till: " + new File(fileName).getAbsolutePath());
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)))) {
            for (int score : scores) {
                out.println(score);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Läser in alla sparade poäng från rätt fil
    private List<Integer> getAllScores(String difficulty) {
        List<Integer> scores = new ArrayList<>();
        File file = new File(getFileName(difficulty));

        if (!file.exists()) return scores;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                scores.add(Integer.parseInt(line.trim())); // Omvandla textrad till siffror
            }
        } catch (IOException | NumberFormatException e) {

        }
        return scores;
    }
}