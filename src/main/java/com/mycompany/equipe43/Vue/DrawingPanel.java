package com.mycompany.equipe43.Vue;

import com.mycompany.equipe43.Domaine.Controleur;
import com.mycompany.equipe43.Vue.Drawing.Afficheur;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import java.awt.*;
import javax.swing.*;

public class DrawingPanel extends JPanel {

    private Controleur controleur;

    public DrawingPanel(Controleur controleur) {
        this.controleur = controleur;

    }
    //Constructeur secondaire pour test
    public DrawingPanel() {
        this(new Controleur()); // crée un contrôleur temporaire par défaut
    }

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
        Graphics2D g2 = (Graphics2D) g;

        // Appel à l'afficheur
        PieceDTO pieceDTO = controleur.getPiece();
        if (pieceDTO != null) {
            Afficheur.afficherPiece(g2, pieceDTO);
            Afficheur.afficherMeublesSansDrain(g2, pieceDTO);
        }

    }
}
