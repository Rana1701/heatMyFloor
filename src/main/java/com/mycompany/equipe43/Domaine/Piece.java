package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.MeubleSansDrain;
import java.util.ArrayList;
import java.util.List;

public class Piece {
    private int x = 0;
    private int y = 0;
    private int largeur;
    private int longueur;
    private List<MeubleSansDrain> meubles = new ArrayList<>();

    public Piece(int x, int y, int largeur, int longueur) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.longueur = longueur;
        meubles = new ArrayList<>();
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
    public List<MeubleSansDrain> getMeubles() {
        return meubles;
    }

    public void setTaille(int largeur, int longueur) {
        this.largeur = largeur;
        this.longueur = longueur;

    }

    public void ajouterMeuble(MeubleSansDrain meuble) {
        meubles.add(meuble);
    }
}
