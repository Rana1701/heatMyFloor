package com.mycompany.equipe43.Domaine;

/**
 * Classe abstraite représentant un élément avec position et dimensions.
 * Toutes les mesures sont en pouces.
 * 
 * @author hanaw
 */
public abstract class Element {
    protected double xEnPouces;
    protected double yEnPouces;
    protected double largeurEnPouces;
    protected double hauteurEnPouces;
    
    public Element(double xEnPouces, double yEnPouces, 
                   double largeurEnPouces, double hauteurEnPouces) {
        this.xEnPouces = xEnPouces;
        this.yEnPouces = yEnPouces;
        this.largeurEnPouces = largeurEnPouces;
        this.hauteurEnPouces = hauteurEnPouces;
    }
    
    // Getters
    public double getX() {
        return xEnPouces;
    }
    
    public double getY() {
        return yEnPouces;
    }
    
    public double getLargeur() {
        return largeurEnPouces;
    }
    
    public double getHauteur() {
        return hauteurEnPouces;
    }
    
    // Setters
    public void setPosition(double x, double y) {
        this.xEnPouces = x;
        this.yEnPouces = y;
    }
    
    public void setX(double x) {
        this.xEnPouces = x;
    }
    
    public void setY(double y) {
        this.yEnPouces = y;
    }
    
    public void setTaille(double largeur, double hauteur) {
        this.largeurEnPouces = largeur;
        this.hauteurEnPouces = hauteur;
    }
    
    public void setLargeur(double largeur) {
        this.largeurEnPouces = largeur;
    }
    
    public void setHauteur(double hauteur) {
        this.hauteurEnPouces = hauteur;
    }
    
    /**
     * Vérifie si un point (en pouces) se trouve à l'intérieur de cet élément.
     * @param xPouces coordonnée X en pouces
     * @param yPouces coordonnée Y en pouces
     * @return true si le point est à l'intérieur
     */
    public boolean contientPoint(double xPouces, double yPouces) {
        return xPouces >= this.xEnPouces && 
               xPouces <= this.xEnPouces + this.largeurEnPouces &&
               yPouces >= this.yEnPouces && 
               yPouces <= this.yEnPouces + this.hauteurEnPouces;
    }
}