package com.mycompany.equipe43.Domaine.DTO;

import com.mycompany.equipe43.Domaine.TypeMeubleSansDrain;
import java.awt.Dimension;
import java.awt.Point;

public class MeubleSansDrainDTO {
    private Point position;
    private Dimension taille;
    private TypeMeubleSansDrain type;

    public MeubleSansDrainDTO(Point position, Dimension taille, TypeMeubleSansDrain type) {
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
