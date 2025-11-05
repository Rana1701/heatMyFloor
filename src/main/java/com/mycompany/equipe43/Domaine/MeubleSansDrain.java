package com.mycompany.equipe43.Domaine;

import java.awt.*;
import java.awt.Point;
import java.awt.Dimension;

public class MeubleSansDrain {
    private final int id;
    private Point position;
    private Dimension taille;
    private TypeMeubleSansDrain type;

    public MeubleSansDrain(int id, Point position, Dimension taille, TypeMeubleSansDrain type) {
        this.id = id;
        this.position = position;
        this.taille = taille;
        this.type = type;
    }
    public int getId() { return id; }

    public Point getPosition() {
        return position;
    }
    public Dimension getTaille() {
        return taille;
    }
    public TypeMeubleSansDrain getType() {
        return type;
    }
    
    public void setPosition(Point nouvellePosition) {
    this.position = nouvellePosition;
    }

    public void setTaille(Dimension nouvelleTaille) {
        this.taille = nouvelleTaille;
    }

}
