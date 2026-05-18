package com.mycompany.equipe43.Domaine;


public class ElementChauffant extends Element {
    private boolean horizontal; // true = horizontal, false = vertical
    private double angle; // Angle de rotation en radians (0 = horizontal, PI/2 = vertical)
    
    public ElementChauffant(double x, double y, double largeur, double hauteur, boolean horizontal) {
        this(x, y, largeur, hauteur, horizontal, 0.0);
    }
    
    public ElementChauffant(double x, double y, double largeur, double hauteur, boolean horizontal, double angle) {
        super(x, y, largeur, hauteur);
        this.horizontal = horizontal;
        this.angle = angle;
    }

    public boolean isHorizontal() { return horizontal; }
    public void setHorizontal(boolean horizontal) { this.horizontal = horizontal; }
    
    public double getAngle() { return angle; }
    public void setAngle(double angle) { this.angle = angle; }
}
