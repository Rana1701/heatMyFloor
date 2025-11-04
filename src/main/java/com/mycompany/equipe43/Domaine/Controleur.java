package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Controleur {
    private Piece piece;
    private MeubleSansDrain meubleSelectionne = null;
    private Stack<Piece> undos = new Stack<>(); 
    private Stack<Piece> redos = new Stack<>();

    public Controleur() {
        this.piece = new Piece(320, 120, 300, 300); // Dimensions par défaut
    }

    private Piece clonePiece(Piece original) {
        Piece copie = new Piece(original.getX(), original.getY(), original.getLargeur(), original.getLongueur());

        for (MeubleSansDrain meuble : original.getMeubles()) {
            MeubleSansDrain clone = new MeubleSansDrain(
                new Point(meuble.getPosition().x, meuble.getPosition().y),
                new Dimension(meuble.getTaille().width, meuble.getTaille().height),
                meuble.getType()
            );
            copie.ajouterMeuble(clone);
        }

        return copie;
    }
 
    private void sauvegarderEtat() {
        undos.push(clonePiece(piece));
        redos.clear(); // dès qu’on fait une nouvelle action, l’historique redo est invalidé
    }

    
    public void undo() {
        if (!undos.isEmpty()) {
            redos.push(clonePiece(piece));
            piece = undos.pop();
        }
    }

    public void redo() {
        if (!redos.isEmpty()) {
            undos.push(clonePiece(piece));
            piece = redos.pop();
        }
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
        sauvegarderEtat();
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
