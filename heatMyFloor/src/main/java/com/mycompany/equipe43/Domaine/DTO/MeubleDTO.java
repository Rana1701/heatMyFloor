/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.equipe43.Domaine.DTO;

/**
 *
 * @author hanaw
 */
public class MeubleDTO extends ElementDTO {
    private final int id;


    public MeubleDTO(int id, double x, double y, double largeur, double hauteur) {
        super(x, y, largeur, hauteur);
        this.id = id;
    }


    public int getId() { 
        return id; 
    }
}
