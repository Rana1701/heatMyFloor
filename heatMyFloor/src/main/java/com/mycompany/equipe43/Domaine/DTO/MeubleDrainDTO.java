/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.equipe43.Domaine.DTO;

import com.mycompany.equipe43.Domaine.TypeMeubleDrain;
/**
 *
 * @author hanaw
 */
public class MeubleDrainDTO extends MeubleDTO {
    private final TypeMeubleDrain type;
    private final double xDrainRelatif;
    private final double yDrainRelatif;
    private final double diametreDrain;


    public MeubleDrainDTO(int id, double x, double y, double largeur, double hauteur,
    TypeMeubleDrain type, double xDrainRelatif, double yDrainRelatif, double diametreDrain) {
        super(id, x, y, largeur, hauteur);
        this.type = type;
        this.xDrainRelatif = xDrainRelatif;
        this.yDrainRelatif = yDrainRelatif;
        this.diametreDrain = diametreDrain;
    }


    public TypeMeubleDrain getType() { 
        return type; 
    }
    public double getXDrainRelatif() { 
        return xDrainRelatif; 
    }
    public double getYDrainRelatif() {
        return yDrainRelatif; 
    }
    public double getDiametreDrain() {
        return diametreDrain; 
    }
}