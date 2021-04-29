/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tetris;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import static tetris.Tetris.faces;

/**
 *
 * @author marti
 *
 * This class represents one currently falling piece.
 */
public class Piece {

    public String name;
    public boolean[][] face;
    //  public boolean  speedyFalling;
    Rectangle piece[] = new Rectangle[4];
    private int faceX;
    private int faceY;
    public Color color;
    private static int lastNumber;

    public Piece() {
        int r = (int) Math.floor((Math.random() * 10) / (10.0 / 7.0));
        while (r == lastNumber) {
            r = (int) Math.floor((Math.random() * 10) / (10.0 / 7.0));
        }
        lastNumber = r;
        switch (r) {
            case 0:
                this.name = "O";
                this.face = faces.O;
                color = Color.YELLOW;
                break;
            case 1:
                this.name = "I";
                this.face = faces.I;
                color = Color.BLUE;
                break;
            case 2:
                this.name = "S";
                this.face = faces.S;
                color = Color.RED;
                break;
            case 3:
                this.name = "Z";
                this.face = faces.Z;
                color = Color.GREENYELLOW;
                break;
            case 4:
                this.name = "L";
                this.face = faces.L;
                color = Color.ORANGE;
                break;
            case 5:
                this.name = "J";
                this.face = faces.J;
                color = Color.PINK;
                break;
            case 6:
                this.name = "T";
                this.face = faces.T;
                color = Color.PURPLE;
                break;

        }

        for (int i = 0; i < piece.length; i++) {
            piece[i] = new Rectangle(Tetris.BLOCKSIZE, Tetris.BLOCKSIZE);
            piece[i].setFill(color);
        }
    }

    public void movePiece() {
        movePiece(faceX, faceY);
    }

    public void movePieceMESH(int x, int y) {
        x = x * Tetris.MOVE;
        y = y * Tetris.MOVE;
        movePiece(x, y);
    }

    //Souřadnice
    public void movePiece(int x, int y) {
        faceX = x;
        faceY = y;
        reDrawPiece();
    }

    public void reDrawPiece() {

        int i = 0;
        for (int xFace = 0; xFace < face.length; xFace++) {
            for (int yFace = 0; yFace < face[0].length; yFace++) { 
                if (face[xFace][yFace]) {
                    piece[i].setX(xFace * Tetris.MOVE + faceX);
                    piece[i].setY(yFace * Tetris.MOVE + faceY);
                    i++;
                }
            }
        }
    }

    public void rotate() {
        boolean[][] shadowFace = face;
        int shadowX = faceX;
        int shadowY = faceY;
        this.face = new boolean[face.length][face[0].length];
        for (int y = 0; y < face[0].length; y++) {
            for (int x = 0; x < face.length; x++) {
                face[x][y] = shadowFace[y][(face.length - 1) - x];
            }
        }
        reDrawPiece();
        boolean corect;
        do {
            corect = false;
            for (int i = 0; i < piece.length; i++) {
                if (piece[i].getX() < 0) {
                    faceX += Tetris.MOVE;
                    //moveRight();
                    corect = true;
                } else if (piece[i].getX() >= Tetris.WIDTH) {
                    faceX -= Tetris.MOVE;
                    moveLeft();
                    corect = true;
                }
            }
            reDrawPiece();
        } while (corect);

        if (!canMoveInMASH(0, 0)) {
            face = shadowFace;
            faceX = shadowX;
            faceY = shadowY;
            reDrawPiece();
        }
    }

    public void moveDown() {
        if (canMove("DOWN")) {
            movePiece(faceX, faceY + Tetris.MOVE);
        } else {
            writeToMash();
            Tetris.next();
        }
    }

    public void moveRight() {
        if (canMove("RIGHT")) {
            movePiece(faceX + Tetris.MOVE, faceY);
        }
    }

    public void moveLeft() {
        if (canMove("LEFT")) {
            movePiece(faceX - Tetris.MOVE, faceY);
        }
    }

    private boolean canMove(String direction) {
        for (Rectangle piece1 : piece) {
            if (direction == "LEFT" && (piece1.getX() == 0 || !canMoveInMASH(-1, 0))) {
                return false;
            } else if (direction == "RIGHT" && (piece1.getX() + Tetris.MOVE == Tetris.WIDTH || !canMoveInMASH(1, 0))) {
                return false;
            } else if (direction == "DOWN" && !canMoveInMASH(0, 1)) {
                return false;
            }
        }
        return true;
    }

    public boolean canMoveInMASH(int x, int y) {
        for (int i = 0; i < piece.length; i++) {
            if (piece[i].getY() + Tetris.MOVE * y >= Tetris.HEIGH || ((int) piece[i].getX() / Tetris.MOVE) + x < 0 || ((int) piece[i].getX() / Tetris.MOVE) + x >= Tetris.MESH.length) {
                return false;
            }
            if (Tetris.MESH[((int) piece[i].getX() / Tetris.MOVE) + x][((int) piece[i].getY() / Tetris.MOVE) + 1 + y] != null) {
                return false;
            }
        }
        return true;
    }

    public void writeToMash() {
        for (int i = 0; i < piece.length; i++) {
            Tetris.MESH[(int) (piece[i].getX() / Tetris.MOVE)][(int) (piece[i].getY() / Tetris.MOVE) + 1] = piece[i];
            piece[i] = null;
        }
    }

    public void hardDrop() {
        while (canMove("DOWN")) {
            moveDown();
            Tetris.addScore(2);
        }
        moveDown();
    }
}
