package com.mycompany.equipe43.Domaine.DTO;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author douae
 */
public class PieceIrreguliereDTO {
    
    private final List<Point2D.Double> points;
    private final boolean estFermee;
    private final List <MeubleSansDrainDTO> meubles;
    private final List <MeubleDrainDTO> meublesDrain;
    private final List<ElementChauffantDTO> elementsChauffants;
    private ThermostatDTO thermostat;
    //bounding box
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    

    public PieceIrreguliereDTO(List<Point2D.Double> points,
                           boolean estFermee,
                           List<MeubleSansDrainDTO> meubles,
                           List<MeubleDrainDTO> meublesDrain,
                           double minX, double minY,
                           double maxX, double maxY,
                           ThermostatDTO thermostat) {
        this(points, estFermee, meubles, meublesDrain, minX, minY, maxX, maxY, thermostat, new ArrayList<>());
    }
    
    public PieceIrreguliereDTO(List<Point2D.Double> points,
                           boolean estFermee,
                           List<MeubleSansDrainDTO> meubles,
                           List<MeubleDrainDTO> meublesDrain,
                           double minX, double minY,
                           double maxX, double maxY,
                           ThermostatDTO thermostat,
                           List<ElementChauffantDTO> elementsChauffants) {

    this.points = new ArrayList<>(points);
    this.estFermee = estFermee;
    this.meubles = new ArrayList<>(meubles);
    this.meublesDrain = new ArrayList<>(meublesDrain);
    this.elementsChauffants = new ArrayList<>(elementsChauffants);
    this.minX = minX;
    this.minY = minY;
    this.maxX = maxX;
    this.maxY = maxY;

    this.thermostat = thermostat;
}


    public List<Point2D.Double> getPoints() {
        return new ArrayList<>(points);
    }

    public boolean estFermee() {
        return estFermee;
    }
    
    public int getNombrePoints() {
        return points.size();
    }

    public List<MeubleSansDrainDTO> getMeubles() {
        return new ArrayList<>(meubles);
    }

    public List<MeubleDrainDTO> getMeublesDrain() {
        return new ArrayList<>(meublesDrain);
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return maxX;
    }

    public double getMaxY() {
        return maxY;
    }
    
    public double getLargeur() {
        return maxX - minX;
    }
    
    public double getLongueur() {
        return maxY - minY;
    }
    public ThermostatDTO getThermostat() {
    return thermostat;
}

public void setThermostat(ThermostatDTO thermostat) {
    this.thermostat = thermostat;
}

    public List<ElementChauffantDTO> getElementsChauffants() {
        return new ArrayList<>(elementsChauffants);
    }

    /**
     * Vérifie si un point (x, y) est à l'intérieur du polygone
     * Utilise l'algorithme de ray casting
     */
    public boolean contientPoint(double x, double y) {
        if (points.isEmpty() || !estFermee) return false;
        
        int intersections = 0;
        int n = points.size();
        
        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get((i + 1) % n);
            
            // Vérifier si le rayon horizontal depuis le point croise ce segment
            if (((p1.y > y) != (p2.y > y)) &&
                (x < (p2.x - p1.x) * (y - p1.y) / (p2.y - p1.y) + p1.x)) {
                intersections++;
            }
        }
        
        // Si le nombre d'intersections est impair, le point est à l'intérieur
        return (intersections % 2) == 1;
    }
}
