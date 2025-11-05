package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Controleur {
    private Piece piece;
    private MeubleSansDrain meubleSelectionne = null;

    public Controleur() {
        this.piece = new Piece(320, 120, 300, 300); // Dimensions par défaut
    }
    
    //Creation d'une pièce régulière
    public void creerPieceReguliere(int x, int y, int largeur, int longueur) {
    this.piece = new Piece(x, y, largeur, longueur);
    }

    //cette méthode sert aussi à redimensionner une pièce via le panneau
    public void definirTaillePiece(int largeur, int longueur) {
        piece.setTaille(largeur, longueur);

    }
    
    //redimensionner une pièce
    public void redimensionnerPiece(int nouvelleLargeur, int nouvelleLongueur) {
        //piece.redimensionner(nouvelleLongueur, nouvelleLargeur);
        if (piece == null) return;

        // Calcul du facteur d'échelle
        double facteurX = (double) nouvelleLargeur / piece.getLargeur();
        double facteurY = (double) nouvelleLongueur / piece.getLongueur();

        // Redimensionner la pièce
        piece.redimensionner(nouvelleLargeur, nouvelleLongueur);

        // Adapter la position et la taille de chaque meuble
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            Point pos = meuble.getPosition();
            Dimension taille = meuble.getTaille();

            int nouveauX = (int) Math.round(pos.x * facteurX);
            int nouveauY = (int) Math.round(pos.y * facteurY);
            int nouvelleLargeurMeuble = (int) Math.round(taille.width * facteurX);
            int nouvelleHauteurMeuble = (int) Math.round(taille.height * facteurY);

            meuble.setPosition(new Point(nouveauX, nouveauY));
            meuble.setTaille(new Dimension(nouvelleLargeurMeuble, nouvelleHauteurMeuble));
    }
        
    }

    public void ajouterMeubleSansDrain(int x, int y, int largeur, int hauteur, TypeMeubleSansDrain type) {
    // combien de meubles existent déjà ?
    int count = piece.getMeubles().size();             // AJOUT

    // décalage automatique pour éviter la superposition
    int offset = 15 * count;                           // AJOUT (ajuste si tu veux plus/moins d’écart)

    // position de base + décalage
    int posX = piece.getX() + x + offset;              // CHANGÉ: + offset
    int posY = piece.getY() + y + offset;              // CHANGÉ: + offset (ou laisse sans offset en Y si tu préfères)

    // id “simple”
    int newId = count + 1;
    MeubleSansDrain meuble = new MeubleSansDrain(newId, new Point(posX, posY), new Dimension(largeur, hauteur), type);
    piece.ajouterMeuble(meuble);
    
    System.out.println("Meubles dans la pièce: " + piece.getMeubles().size());

    // (alternative propre si tu as Piece.ajouterMeuble(Point,Dimension,Type) avec nextId++):
    // piece.ajouterMeuble(new Point(posX, posY), new Dimension(largeur, hauteur), type);
}


    public PieceDTO getPiece() {
        List<MeubleSansDrainDTO> meublesDTO = new ArrayList<>();
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            MeubleSansDrainDTO dto = new MeubleSansDrainDTO(meuble.getId(), meuble.getPosition(), meuble.getTaille(), meuble.getType());
            meublesDTO.add(dto);
        }
        return new PieceDTO(piece.getX(), piece.getY(), piece.getLargeur(), piece.getLongueur(), meublesDTO);
    }
    
    public void selectionnerMeuble(int x, int y) {
        meubleSelectionne = null;
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            int mx = meuble.getPosition().x;
            int my = meuble.getPosition().y;
            int mw = meuble.getTaille().width;
            int mh = meuble.getTaille().height;
            
            if (x >= mx && x <= mx + mw && y >= my && y <= my + mh) {
                meubleSelectionne = meuble;
                break;
            }
            
        }
    }

// Obtenir le meuble sélectionné
    public MeubleSansDrain getMeubleSelectionne() {
        return meubleSelectionne;
    }

// Désélectionner le meuble
    public void deselectionnerMeuble() {
        meubleSelectionne = null;
    }
}
