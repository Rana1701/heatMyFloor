package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Controleur {
    private Piece piece;

    public Controleur() {
        this.piece = new Piece(320, 120, 400, 300); // Dimensions par défaut
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
        piece.redimensionner(nouvelleLongueur, nouvelleLargeur);
    }

    public void ajouterMeubleSansDrain(int x, int y, int largeur, int hauteur, TypeMeubleSansDrain type) {
        int posX = piece.getX() + x;
        int posY = piece.getY() + y;
        MeubleSansDrain meuble = new MeubleSansDrain(new Point(posX, posY), new Dimension(largeur, hauteur), type);
        piece.ajouterMeuble(meuble);
    }

    public PieceDTO getPiece() {
        List<MeubleSansDrainDTO> meublesDTO = new ArrayList<>();
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            MeubleSansDrainDTO dto = new MeubleSansDrainDTO(meuble.getPosition(), meuble.getTaille(), meuble.getType());
            meublesDTO.add(dto);
        }
        return new PieceDTO(piece.getX(), piece.getY(), piece.getLargeur(), piece.getLongueur(), meublesDTO);
    }
}
