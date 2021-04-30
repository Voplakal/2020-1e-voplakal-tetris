/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.effect.Reflection;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

/**
 *
 * @author marti
 *
 * Main logic of the game
 */
public class Tetris extends Application {

    public static final int MOVE = 25;
    public static final int BLOCKSIZE = 24;
    public static final int HEIGH = 24 * MOVE;
    public static final int WIDTH = 12 * MOVE;
    public static Rectangle[][] MESH = new Rectangle[WIDTH / BLOCKSIZE][HEIGH / BLOCKSIZE];
    public static Pane pane = new Pane();
    public static Scene scene = new Scene(pane, WIDTH - 10, HEIGH - 10);    //je aplikována korekce, která je nutná při použití neškálovatelného okna
    public static Piece piece;
    public static Faces faces = new Faces();
    private static boolean play = false;
    private static Timer fall;
    private static Text linesText;
    private static Text scoreText;
    private static Rectangle contrastBox;
    private static int lines;
    private static int score;
    private static int delay = 300;
    private static TimerTask task;
    private static ImageView img;
    private static Clip clip;
    private static boolean playSound = false;
    private static ImageView soundImg;

    @Override
    public void start(Stage stage) throws InterruptedException {
        stage.setTitle("Tetris 1.0.5 by Martin Voplakal");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        scoreText = new Text();
        addScore(0);
        scoreText.setStyle("-fx-font: 20 arial;");
        scoreText.setY(30);
        scoreText.setX(5);
        linesText = new Text();
        addLines(0);
        linesText.setStyle("-fx-font: 20 arial;");
        linesText.setY(70);
        linesText.setX(5);
        linesText.setFill(Color.GREEN);
        linesText.toFront();
        contrastBox = new Rectangle(WIDTH, 80);
        contrastBox.setFill(Color.WHITE);
        Text intro = new Text("Press any key to continue \nESC pause\nSPACE hard drop\n              +");
        intro.setX(60);
        intro.setY(250);
        intro.setStyle("-fx-font: 20 arial;");
        img = new ImageView(new Image("File:image/keys.png"));
        img.setPreserveRatio(true);
        img.setFitHeight(150);
        img.setX(70);
        img.setY(320);
        scene.setOnKeyPressed((KeyEvent event) -> {
            pane.getChildren().removeAll(intro, img);
            initialize();
        });
        soundImg = new ImageView(new Image("File:image/lound.png"));
        soundImg.setPreserveRatio(true);
        soundImg.setFitHeight(30);
        soundImg.setX(WIDTH - 40);
        soundImg.setY(7);
        soundImg.setOnMouseClicked((event) -> {
            pauseSound();
        });
        pane.getChildren().addAll(contrastBox, linesText, scoreText, intro, img, soundImg);
        playSound();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        System.exit(0);
    }

    static void playSound() {
        try {
            File f = new File("./sound.wav");
            AudioInputStream audioIn = null;
            audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
            clip = AudioSystem.getClip();
            clip.open(audioIn);
            FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = volume.getMaximum() - volume.getMinimum();
            float gain = (range * (float) 0.85) + volume.getMinimum();
            volume.setValue(gain);
            pauseSound();
        } catch (Exception r) {
            System.out.println("Soubor se zvukem nemohl být spuštěn.\n"+r);
        }
    }

    private static void pauseSound() {
        try {
            if (playSound) {
                clip.stop();
                soundImg.setImage(new Image("File:image/mute.png"));
                playSound = false;
            } else {
                soundImg.setImage(new Image("File:image/lound.png"));
                clip.loop(Integer.MAX_VALUE);
                clip.setLoopPoints(0, -1);
                playSound = true;
            }
        } catch (Exception e) {
            System.out.println("Zvuk nelze spustit");
        }
    }

    private static void initialize() {
        next();
        lines = 0;
        score = 0;
        fall = new Timer();
        contrastBox.setOpacity(0.5);
        img = new ImageView(new Image("File:image/pause.png"));
        img.setPreserveRatio(true);
        img.setFitHeight(50);
        img.setX(WIDTH - 52);
        img.setY(50);
        pause();
    }

// this method add new piece, set the default position and show it
    public static void next() {
        Tetris.piece = new Piece();
        piece.movePieceMESH(5, 0);
        if (!piece.canMoveInMASH(0, 0)) {
            endGame();
        } else {
            for (int j = 0; j < 4; j++) {
                pane.getChildren().add(0, piece.piece[j]);
            }
        }
        organizeMASH();
    }

    public static void restartGame() {
        for (int i = 0; i < MESH.length; i++) {
            for (int j = 0; j < MESH[0].length; j++) {
                pane.getChildren().remove(MESH[i][j]);
                MESH[i][j] = null;
            }
            for (int j = 0; j < piece.piece.length; j++) {
                pane.getChildren().remove(piece.piece[j]);
            }
        }
        contrastBox.setHeight(80);
        initialize();
    }

    private static void pause() {
        if (play) {
            task.cancel();
            fall.purge();
            scene.setOnKeyPressed((KeyEvent event) -> {
                switch (event.getCode()) {
                    case ESCAPE:
                        pause();
                        break;
                }
            });
            pane.getChildren().add(img);
            play = false;
        } else {
            task = new TimerTask() {
                public void run() {
                    Platform.runLater(new Runnable() {
                        public void run() {
                            piece.moveDown();
                        }

                    }
                    );
                }
            };
            fall.schedule(task, 0, delay);
            pane.getChildren().remove(img);
            scene.setOnKeyPressed((KeyEvent event) -> {
                switch (event.getCode()) {
                    case RIGHT:
                        piece.moveRight();
                        break;
                    case DOWN:
                        piece.moveDown();
                        addScore(1);
                        break;
                    case LEFT:
                        piece.moveLeft();
                        break;
                    case UP:
                        piece.rotate();
                        break;
                    case SPACE:
                        piece.hardDrop();
                        break;
                    case ESCAPE:
                        pause();
                        break;
                }
            });
            play = true;
        }
    }

    public static void endGame() {
        contrastBox.setHeight(HEIGH);
        contrastBox.setOpacity(0.9);
        Text over = new Text("GAME OVER");
        over.setX(10.0f);
        over.setY(225.0f);
        over.setCache(true);
        over.setFill(Color.RED);
        over.setFont(Font.font(null, FontWeight.BOLD, 45));
        Reflection r = new Reflection();
        r.setFraction(0.6f);
        over.setEffect(r);
        Text heigh = new Text();
        Text restartText = new Text("restart R");
        restartText.setX(150);
        restartText.setY(30);
        restartText.setStyle("-fx-font: 20 arial;");

        pane.getChildren().addAll(over, heigh, restartText);
        task.cancel();
        fall.purge();
        play = false;
        int fileScoreNumber;
        String fileScoreText = "";
        File soubor = new File("score.txt");
        try {
            try (FileReader fr = new FileReader(soubor)) {
                int c;
                while ((c = fr.read()) != -1) {
                    fileScoreText += (char) c;
                }
                fileScoreNumber = Integer.parseInt(fileScoreText);
            }
            if (score > fileScoreNumber) {
                heigh.setText("New heigh score!");
                heigh.setFill(Color.BLUEVIOLET);
                heigh.setX(35.0f);
                heigh.setY(310.0f);
                heigh.setFont(Font.font(null, FontWeight.BOLD, 25));
                rewriteFile(score, soubor);
            }
        } catch (IOException e) {
            System.out.println("Soubor " + soubor + " nelze otevřít a bude vytvořen");
            try {
                Files.createFile(Paths.get("score.txt"));
                File newFile = new File("score.txt");
                rewriteFile(score, newFile);
            } catch (IOException e2) {
                System.out.println("Došlo k chybě při vytvaření souboru. \n" + e2);
            }
        } catch (NumberFormatException nfe) {
            try {
                rewriteFile(score, soubor);
            } catch (IOException e2) {
                System.out.println("Pokus o opravu souboru se nezdařil \n" + e2);
            }
        }
        scene.setOnKeyPressed((KeyEvent event) -> {
            switch (event.getCode()) {
                case R:
                    pane.getChildren().removeAll(over, heigh, restartText);
                    restartGame();
                    break;

            }
        });
    }

    private static void rewriteFile(int input, File soubor) throws IOException {
        try (FileWriter fw = new FileWriter(soubor)) {
            fw.write(String.valueOf(score));
        }
    }

    private static void organizeMASH() {
        int scoreLines = 0;
        for (int y = 0; y < MESH[0].length; y++) {
            boolean canOrganize = true;
            for (int x = 0; x < MESH.length; x++) {
                if (MESH[x][y] == null) {
                    canOrganize = false;
                }
            }
            if (canOrganize) {
                for (int i = 0; i < MESH.length; i++) {
                    pane.getChildren().remove(MESH[i][y]);
                }
                addLines(1);

                for (int i = y; i >= 1; i--) {
                    for (int j = 0; j < MESH.length; j++) {

                        MESH[j][i] = MESH[j][i - 1];
                        if (MESH[j][i] != null) {
                            MESH[j][i].setY(MESH[j][i].getY() + MOVE);
                        }
                    }
                }
                scoreLines++;
            }
        }
        addScore((scoreLines == 1) ? 100 : (scoreLines == 2) ? 300 : (scoreLines == 3) ? 500 : (scoreLines == 4) ? 800 : 0);

    }

    public static void addScore(int add) {
        score += add;
        scoreText.setText("Score: " + score);
    }

    public static void addLines(int add) {
        lines += add;
        linesText.setText("Lines: " + lines);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
