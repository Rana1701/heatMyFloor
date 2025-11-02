package com.mycompany.equipe43.Vue.Drawing;

import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import java.awt.*;

public class Afficheur {

    // Dessine la pièce (rectangle blanc)
    public static void afficherPiece(Graphics2D g2, PieceDTO pieceDTO) {
        g2.setColor(Color.WHITE);
        g2.fillRect(320, 120, pieceDTO.getLargeur(), pieceDTO.getLongueur());
    }

    // Dessine tous les meubles sans drain
    public static void afficherMeublesSansDrain(Graphics2D g2, PieceDTO pieceDTO) {
        for (MeubleSansDrainDTO meuble : pieceDTO.getMeubles()) {
            Point pos = meuble.getPosition();
            Dimension taille = meuble.getTaille();

            g2.setColor(Color.BLUE);
            g2.fillRect(pos.x, pos.y, taille.width, taille.height);

            g2.setColor(Color.WHITE);
            g2.drawString(meuble.getType().toString(), pos.x + 5, pos.y + 15);
        }
    }

}
