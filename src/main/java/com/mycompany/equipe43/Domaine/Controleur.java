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
            MeubleSansDrain clone = new MeubleSansDrain(meuble.getId() ,
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
    int count = piece.getMeubles().size();             

    // décalage automatique pour éviter la superposition
    int offset = 15 * count;                          

    // position de base + décalage
    int posX = piece.getX() + x + offset;              
    int posY = piece.getY() + y + offset;              

    // id “simple”
    int newId = count + 1;
    MeubleSansDrain meuble = new MeubleSansDrain(newId, new Point(posX, posY), new Dimension(largeur, hauteur), type);
    piece.ajouterMeuble(meuble);
    
    System.out.println("Meubles dans la pièce: " + piece.getMeubles().size());

    // (alternative propre si tu as Piece.ajouterMeuble(Point,Dimension,Type) avec nextId++):
    // piece.ajouterMeuble(new Point(posX, posY), new Dimension(largeur, hauteur), type);
}

    //  supprimer le meuble sélectionné (si aucun → false)
    public boolean supprimerMeubleSelectionne() {
        if (meubleSelectionne == null) return false;
        boolean ok = piece.supprimerMeubleParId(meubleSelectionne.getId());
        meubleSelectionne = null;
        return ok;
}

//  supprimer un meuble par id
    public boolean supprimerMeubleParId(int id) {
    // si on supprime celui qui est sélectionné, on désélectionne
        if (meubleSelectionne != null && meubleSelectionne.getId() == id) {
            meubleSelectionne = null;
        }
        return piece.supprimerMeubleParId(id);
}

    //  redimensionner le meuble sélectionné (retourne false si rien n'est sélectionné)
    public boolean redimensionnerMeubleSelectionne(int nouvelleLargeur, int nouvelleLongueur) {
        if (meubleSelectionne == null) return false;
        if (nouvelleLargeur <= 0 || nouvelleLongueur <= 0) return false;

   
        meubleSelectionne.setTaille(new Dimension(nouvelleLargeur, nouvelleLongueur));
        return true;
}

//  redimensionner par id (utile si tu veux plus tard un formulaire par id)
    public boolean redimensionnerMeubleParId(int id, int nouvelleLargeur, int nouvelleLongueur) {
        for (MeubleSansDrain m : piece.getMeubles()) {
            if (m.getId() == id) {
               if (nouvelleLargeur <= 0 || nouvelleLongueur <= 0) return false;
               m.setTaille(new Dimension(nouvelleLargeur, nouvelleLongueur));
            // si c'était le sélectionné on garde la sélection cohérente
               if (meubleSelectionne != null && meubleSelectionne.getId() == id) {
                    meubleSelectionne = m;
            }
                return true;
        }
    }
        return false;
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

    public boolean deplacerMeubleSelectionne(int nouvelX, int nouvelY) {
        if (meubleSelectionne == null) {
            return false;
        }
        meubleSelectionne.setPosition(new Point(nouvelX, nouvelY));
        return true;
    }    
}
