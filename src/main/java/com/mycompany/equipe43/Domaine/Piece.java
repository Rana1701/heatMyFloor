package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.MeubleSansDrain;
import java.util.ArrayList;
import java.util.List;
import java.awt.Point;
import java.awt.Dimension;

public class Piece {
    private int x = 0;
    private int y = 0;
    private int largeur;
    private int longueur;
    private List<MeubleSansDrain> meubles = new ArrayList<>();
    private int nextId = 1;
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
    
    public void setLongueur(int longueur) {
        this.longueur = longueur;
    }

    public void setLargeur(int largeur) {
        this.largeur = largeur;
    }
    
    public void redimensionner(int nouvelleLargeur, int nouvelleLongueur) {
        this.longueur = nouvelleLongueur;
        this.largeur = nouvelleLargeur;
    }

    public void ajouterMeuble(MeubleSansDrain meuble) {
        meubles.add(meuble);
    }
    public MeubleSansDrain ajouterMeuble(Point pos, Dimension dim, TypeMeubleSansDrain type) {
        MeubleSansDrain m = new MeubleSansDrain(nextId++, pos, dim, type);
        meubles.add(m);
        return m;
    }
}
