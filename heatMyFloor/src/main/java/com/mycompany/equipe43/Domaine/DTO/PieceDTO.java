package com.mycompany.equipe43.Domaine.DTO;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;

import java.util.List;

public class PieceDTO {

    private final double x;
    private final double y;
    private final double largeur;
    private final double longueur;

    private final List<MeubleSansDrainDTO> meubles;
    private final List<MeubleDrainDTO> meublesDrain;
    private final List<ElementChauffantDTO> elementsChauffants;
    private ThermostatDTO thermostat;

    public PieceDTO(double x,
                    double y,
                    double largeur,
                    double longueur,
                    List<MeubleSansDrainDTO> meubles,
                    List<MeubleDrainDTO> meublesDrain)
    {
        this(x, y, largeur, longueur, meubles, meublesDrain, null);
    }
    
    public PieceDTO(double x,
                    double y,
                    double largeur,
                    double longueur,
                    List<MeubleSansDrainDTO> meubles,
                    List<MeubleDrainDTO> meublesDrain,
                    List<ElementChauffantDTO> elementsChauffants)
    {
        this.x = x;
        this.y = y;
        this.largeur = largeur;
        this.longueur = longueur;
        this.meubles = meubles;
        this.meublesDrain = meublesDrain;
        this.elementsChauffants = elementsChauffants;
    }

    // --- Getters ---
    public double getX() { 
        return x; 
    }
public ThermostatDTO getThermostat() {
    return thermostat;
}

public void setThermostat(ThermostatDTO thermostat) {
    this.thermostat = thermostat;
}

    public double getY() { 
        return y; 
    }

    public double getLargeur() { 
        return largeur; 
    }

    public double getLongueur() { 
        return longueur; 
    }

    public List<MeubleSansDrainDTO> getMeubles() { 
        return meubles; 
    }
    
    public List<MeubleDrainDTO> getMeublesDrain() { 
        return meublesDrain; 
    }
    
    public List<ElementChauffantDTO> getElementsChauffants() {
        return elementsChauffants;
    }
}
