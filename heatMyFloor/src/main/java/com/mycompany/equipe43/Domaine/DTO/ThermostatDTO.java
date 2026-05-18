package com.mycompany.equipe43.Domaine.DTO;

public class ThermostatDTO {
    private double x;
    private double y;
    private double largeur;
    private double hauteur;
    private double angle; // Angle de rotation en radians (0 = horizontal)

    public ThermostatDTO(double x, double y, double largeur, double hauteur) {
        this(x, y, largeur, hauteur, 0.0);
    }

    public ThermostatDTO(double x, double y, double largeur, double hauteur, double angle) {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.angle = angle;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getLargeur() { return largeur; }
    public double getHauteur() { return hauteur; }
    public double getAngle() { return angle; }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }
}
