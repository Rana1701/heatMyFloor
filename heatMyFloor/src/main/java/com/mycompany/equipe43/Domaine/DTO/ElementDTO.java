/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.equipe43.Domaine.DTO;

/**
 *
 * @author hanaw
 */

public class ElementDTO {
private final double x;
private final double y;
private final double largeur;
private final double hauteur;


public ElementDTO(double x, double y, double largeur, double hauteur) {
this.x = x;
this.y = y;
this.largeur = largeur;
this.hauteur = hauteur;
}


public double getX() { return x; }
public double getY() { return y; }
public double getLargeur() { return largeur; }
public double getHauteur() { return hauteur; }
}
    

