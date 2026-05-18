package com.mycompany.equipe43.Domaine;

/**
 * Représente un meuble avec drain (douche, toilette, bain, vanité).
 * Toutes les positions et dimensions sont exprimées en pouces.
 * 
 * @author hanaw
 */
public class MeubleDrain extends Meuble {
    private final TypeMeubleDrain type;
    
    // Position du drain RELATIVE au meuble (en pouces par rapport au coin inférieur gauche du meuble)
    private double xDrainRelatif;
    private double yDrainRelatif;
    
    // Diamètre du drain (en pouces)
    private double diametreDrain;
    
    public MeubleDrain(int id,
                       double xEnPouces,
                       double yEnPouces,
                       double largeurEnPouces,
                       double hauteurEnPouces,
                       TypeMeubleDrain type,
                       double xDrainRelatif,
                       double yDrainRelatif,
                       double diametreDrain) {
        super(id, xEnPouces, yEnPouces, largeurEnPouces, hauteurEnPouces);
        this.type = type;
        this.xDrainRelatif = xDrainRelatif;
        this.yDrainRelatif = yDrainRelatif;
        this.diametreDrain = diametreDrain;
    }
    
    public TypeMeubleDrain getType() {
        return type;
    }
    
    // Getters pour position relative du drain (par rapport au meuble)
    public double getXDrainRelatif() {
        
        return xDrainRelatif;
    }
    
    public double getYDrainRelatif() {
        return yDrainRelatif;
    }
    
    // Getters pour position ABSOLUE du drain (dans la pièce)
    public double getXDrainAbsolu() {
        return this.getX() + xDrainRelatif;
    }
    
    public double getYDrainAbsolu() {
        return this.getY() + yDrainRelatif;
    }
    
    public double getDiametreDrain() {
        return diametreDrain;
    }
    
    // Setters - l'utilisateur peut modifier le drain
    public void setPositionDrain(double xRelatif, double yRelatif) {
        this.xDrainRelatif = xRelatif;
        this.yDrainRelatif = yRelatif;
    }
    
    public void setDiametreDrain(double diametre) {
        this.diametreDrain = diametre;
    }
    
    public void setTaille(double largeur, double hauteur) {
        // largeur et hauteur viennent de Meuble (classe mère)
        this.largeurEnPouces = largeur;
        this.hauteurEnPouces = hauteur;
    }
    
    public void setXDrainRelatif(double xRelatif) {
        this.xDrainRelatif = xRelatif;
    }

    public void setYDrainRelatif(double yRelatif) {
        this.yDrainRelatif = yRelatif;
    }
}