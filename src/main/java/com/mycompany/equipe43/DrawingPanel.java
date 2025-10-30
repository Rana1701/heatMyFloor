package com.mycompany.equipe43;

import java.awt.*;
import javax.swing.*;

public class DrawingPanel extends JPanel {

    // Dimensions de la pièce (modifiable depuis MainWindow)
    private int pieceWidth = 400;
    private int pieceHeight = 300;

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la grille
        g.setColor(Color.LIGHT_GRAY);
        int cellSize = 10;
        for (int x = 0; x < getWidth(); x += cellSize) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += cellSize) {
            g.drawLine(0, y, getWidth(), y);
        }

        // Dessiner la pièce en rectangle
        g.setColor(Color.WHITE); // couleur du rectangle
        int x = 320; // position x du rectangle
        int y = 120; // position y du rectangle
        g.fillRect(x, y, pieceWidth, pieceHeight);
    }

    public void setPieceSize(int width, int height) {
        this.pieceWidth = width;
        this.pieceHeight = height;
        repaint(); // redessiner le panel
    }
}
