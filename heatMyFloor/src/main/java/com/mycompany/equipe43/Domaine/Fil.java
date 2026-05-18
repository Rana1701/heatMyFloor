/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.equipe43.Domaine;


/**
 *
 * @author hanaw
 */



public class Fil {
    private double x;
    private double y;
    private double longueur;
    private double epaisseur;  
    private int id;
    private static int compteur = 0;

    public Fil(double x, double y, double longueur, double epaisseur) {
        this.x = x;
        this.y = y;
        this.longueur = longueur;
        this.epaisseur = epaisseur;
        this.id = ++compteur;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getLongueur() {
        return longueur;
    }

    public void setLongueur(double longueur) {
        this.longueur = longueur;
    }

    public double getEpaisseur() {
        return epaisseur;
    }

    public void setEpaisseur(double epaisseur) {
        this.epaisseur = epaisseur;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "FilChauffant #" + id + " (x=" + x + ", y=" + y + ", L=" + longueur + ")";
    }
} 


