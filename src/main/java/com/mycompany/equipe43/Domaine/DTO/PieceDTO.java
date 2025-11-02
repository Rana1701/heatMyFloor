package com.mycompany.equipe43.Domaine.DTO;

import java.util.List;

public class PieceDTO {
    private int largeur;
    private int longueur;
    private List<MeubleSansDrainDTO> meubles;

    public PieceDTO(int largeur, int longueur, List<MeubleSansDrainDTO> meubles) {
        this.largeur = largeur;
        this.longueur = longueur;
        this.meubles = meubles;
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
