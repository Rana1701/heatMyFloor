
package com.mycompany.equipe43.Domaine;

import java.util.ArrayList;
import java.util.List;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;

/**
 * Représente une pièce rectangulaire ou irrégulière.
 * Toutes les mesures sont en pouces.
 * 
 * @author hanaw
 */
public class Piece {
    private double xEnPouces = 0;
    private double yEnPouces = 0;
    private double largeurEnPouces;
    private double longueurEnPouces;
    
    private List<ElementChauffant> chauffages = new ArrayList<>();
    private List<MeubleSansDrain> meubles = new ArrayList<>();
    private List<MeubleDrain> meublesDrain = new ArrayList<>();
    private List<Fil> fils = new ArrayList<>();
    private int nextId = 1;  // ID reste int!
    private ThermostatDTO thermostat;
    
    
    
    public void setThermostat(ThermostatDTO t) {
    this.thermostat = t;
}

public ThermostatDTO getThermostat() {
    return thermostat;
}
    public Piece(double xEnPouces, double yEnPouces, 
                 double largeurEnPouces, double longueurEnPouces) {
        this.xEnPouces = xEnPouces;
        this.yEnPouces = yEnPouces;
        this.largeurEnPouces = largeurEnPouces;
        this.longueurEnPouces = longueurEnPouces;
    }
    
    // --- Getters ---
    public double getX() {
        return xEnPouces;
    }
    
    public double getY() {
        return yEnPouces;
    }
    
    public double getLargeur() {
        return largeurEnPouces;
    }
    
    public double getLongueur() {
        return longueurEnPouces;
    }
    
    public List<MeubleSansDrain> getMeubles() {
        return meubles;
    }
    
    public List<MeubleDrain> getMeublesDrain() {
        return meublesDrain;
    }
    
    public List<ElementChauffant> getElementsChauffants(){
        return chauffages;
    }
    
    public List<Fil> getFils() {
    return fils;
    }

    // --- Setters ---
    public void setTaille(double largeur, double longueur) {
        this.largeurEnPouces = largeur;
        this.longueurEnPouces = longueur;
    }
    
    public void setLargeur(double largeur) {
        this.largeurEnPouces = largeur;
    }
    
    public void setLongueur(double longueur) {
        this.longueurEnPouces = longueur;
    }
    
    public void redimensionner(double nouvelleLargeur, double nouvelleLongueur) {
        this.largeurEnPouces = nouvelleLargeur;
        this.longueurEnPouces = nouvelleLongueur;
    }
    
    // --- Gestion des meubles ---
    public void ajouterMeuble(MeubleSansDrain meuble) {
        meubles.add(meuble);
    }
    
    public MeubleSansDrain ajouterMeuble(double x, double y,
                                         double largeur, double hauteur,
                                         TypeMeubleSansDrain type) {
        MeubleSansDrain m = new MeubleSansDrain(
            nextId++,  // ✅ int
            x, y,
            largeur, hauteur,
            type
        );
        meubles.add(m);
        return m;
    }
    
    public MeubleDrain ajouterMeubleDrain(double x, double y,
                                      double largeur, double hauteur,
                                      TypeMeubleDrain type,
                                      double xDrainRelatif, double yDrainRelatif,
                                      double diametreDrain) {
    MeubleDrain m = new MeubleDrain(
        nextId++,
        x, y,
        largeur, hauteur,
        type,
        xDrainRelatif, yDrainRelatif,
        diametreDrain
    );
    meublesDrain.add(m);
    return m;
}

    public MeubleSansDrain trouverParId(int id) {  // ✅ int, pas double!
        for (MeubleSansDrain m : meubles) {
            if (m.getId() == id) return m;
        }
        return null;
    }
    
    public boolean supprimerMeubleParId(int id) {
    boolean removed = meubles.removeIf(m -> m.getId() == id);
    if (removed) return true;
    
    return meublesDrain.removeIf(m -> m.getId() == id);
}

    
    public boolean supprimerMeuble(MeubleSansDrain m) {
        return meubles.remove(m);
    }
    
    // --- Gestion des fils ---
    public void ajouterFil(Fil fil) {
    fils.add(fil);
    }


    public boolean supprimerFil(Fil fil) {
    return fils.remove(fil);
    }
    
    // --- Copie profonde (clone) ---
    public Piece copier() {
        Piece copie = new Piece(this.xEnPouces, this.yEnPouces, 
                                this.largeurEnPouces, this.longueurEnPouces);
        copie.nextId = this.nextId;  // ✅ Copier aussi le compteur d'ID
        
        for (MeubleSansDrain meuble : this.meubles) {
            MeubleSansDrain clone = new MeubleSansDrain(
                meuble.getId(),
                meuble.getX(),
                meuble.getY(),
                meuble.getLargeur(),
                meuble.getHauteur(),
                meuble.getType()
            );
            copie.ajouterMeuble(clone);
        }
        // Copier les MeubleDrain
        for (MeubleDrain meuble : this.meublesDrain) {
            MeubleDrain clone = new MeubleDrain(
                meuble.getId(),
                meuble.getX(),
                meuble.getY(),
                meuble.getLargeur(),
                meuble.getHauteur(),
                meuble.getType(),
                meuble.getXDrainRelatif(),
                meuble.getYDrainRelatif(),
                meuble.getDiametreDrain()
            );
            copie.meublesDrain.add(clone);
        }
        
        for (ElementChauffant e : this.chauffages) {
            ElementChauffant clone = new ElementChauffant(
            e.getX(),
            e.getY(),
            e.getLargeur(),
            e.getHauteur(),
            e.isHorizontal()
            );
            copie.chauffages.add(clone);
        }
        for (Fil f : this.fils) {
            Fil clone = new Fil(
                f.getX(),
                f.getY(),
                f.getLongueur(),
                f.getEpaisseur()
            );
            copie.fils.add(clone);
        }
        return copie;
    }
    
    // Dans Piece.java, ajoutez :
public double getMinX() { return getX(); }
public double getMinY() { return getY(); }
public double getMaxX() { return getX() + getLargeur(); }
public double getMaxY() { return getY() + getLongueur(); }
}