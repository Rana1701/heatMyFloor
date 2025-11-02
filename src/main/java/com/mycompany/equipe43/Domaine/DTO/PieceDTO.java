package com.mycompany.equipe43.Domaine.DTO;

import java.util.List;

public class PieceDTO {
    private int x;
    private int y;
    private int largeur;
    private int longueur;
    private List<MeubleSansDrainDTO> meubles;

    public PieceDTO(int x, int y, int largeur, int longueur, List<MeubleSansDrainDTO> meubles) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.longueur = longueur;
        this.meubles = meubles;
    }
    
    public int getX() { 
        return x; 
    }
    public int getY() { 
        return y; 
    }

    public int getLargeur() {
        return largeur;
    }

    public int getLongueur() {
        return longueur;
    }

    public List<MeubleSansDrainDTO> getMeubles() {
        return meubles;
    }
}
