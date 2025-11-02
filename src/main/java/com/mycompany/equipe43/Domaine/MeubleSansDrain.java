package com.mycompany.equipe43.Domaine;

import java.awt.*;

public class MeubleSansDrain {
    private Point position;
    private Dimension taille;
    private TypeMeubleSansDrain type;

    public MeubleSansDrain(Point position, Dimension taille, TypeMeubleSansDrain type) {
        this.position = position;
        this.taille = taille;
        this.type = type;
    }

    public Point getPosition() {
        return position;
    }
    public Dimension getTaille() {
        return taille;
    }
    public TypeMeubleSansDrain getType() {
        return type;
    }
}
