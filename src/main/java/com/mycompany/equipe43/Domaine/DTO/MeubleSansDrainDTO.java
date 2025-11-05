package com.mycompany.equipe43.Domaine.DTO;

import com.mycompany.equipe43.Domaine.TypeMeubleSansDrain;
import java.awt.Dimension;
import java.awt.Point;

public class MeubleSansDrainDTO {
    private final int id;
    private Point position;
    private Dimension taille;
    private TypeMeubleSansDrain type;

    public MeubleSansDrainDTO(int id, Point position, Dimension taille, TypeMeubleSansDrain type) {
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
}
