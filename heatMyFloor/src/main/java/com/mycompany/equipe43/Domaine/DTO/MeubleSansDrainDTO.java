
package com.mycompany.equipe43.Domaine.DTO;

import com.mycompany.equipe43.Domaine.TypeMeubleSansDrain;

public class MeubleSansDrainDTO {

    private final int id;

    private final double x;
    private final double y;

    private final double largeur;
    private final double hauteur;

    private final TypeMeubleSansDrain type;

    public MeubleSansDrainDTO(int id,
                              double x,
                              double y,
                              double largeur,
                              double hauteur,
                              TypeMeubleSansDrain type) {

        this.id = id;
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.type = type;
    }

    // --- Getters ---
    public int getId() { 
        return id; 
    }

    public double getX() { 
        return x; 
    }

    public double getY() { 
        return y; 
    }

    public double getLargeur() { 
        return largeur; 
    }

    public double getHauteur() { 
        return hauteur; 
    }

    public TypeMeubleSansDrain getType() { 
        return type; 
    }
}
