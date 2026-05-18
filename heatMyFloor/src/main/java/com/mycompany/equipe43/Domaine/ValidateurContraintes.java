package com.mycompany.equipe43.Domaine;

import com.mycompany.equipe43.Domaine.DTO.MeubleDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe pour valider les contraintes du fil chauffant selon les spécifications du projet.
 * 
 * Contraintes:
 * - Le fil doit être à au moins 8 pouces d'un autre élément chauffant
 * - Le fil doit être à au moins 6 pouces d'un drain
 * - Le fil doit être à au moins 10 pouces d'un drain de toilette
 * - Le fil ne peut pas avoir des segments droits de plus de 10 pieds (120 pouces)
 * - Le fil doit être à au moins 3 pouces des murs et des meubles
 * - Le fil ne doit pas se croiser
 * - Le fil doit être à au moins 3 pouces d'un autre fil
 * - Le fil ne peut pas passer dans une zone interdite
 */
public class ValidateurContraintes {
    
    private static final double DISTANCE_MIN_ELEMENT_CHAUFFANT = 8.0; // pouces
    private static final double DISTANCE_MIN_DRAIN = 6.0; // pouces
    private static final double DISTANCE_MIN_DRAIN_TOILETTE = 10.0; // pouces
    private static final double LONGUEUR_MAX_SEGMENT = 120.0; // pouces (10 pieds)
    private static final double DISTANCE_MIN_MURS_MEUBLES = 3.0; // pouces
    private static final double DISTANCE_MIN_ENTRE_FILS = 3.0; // pouces
    
    private Piece piece;
    private List<String> erreurs;
    
    public ValidateurContraintes(Piece piece) {
        this.piece = piece;
        this.erreurs = new ArrayList<>();
    }
    
    /**
     * Valide toutes les contraintes du chemin de fil.
     * @param chemin Liste des points formant le chemin du fil
     * @return true si toutes les contraintes sont respectées, false sinon
     */
    public boolean validerChemin(List<Point2D.Double> chemin) {
        erreurs.clear();
        
        if (chemin == null || chemin.size() < 2) {
            erreurs.add("Le chemin doit contenir au moins 2 points");
            return false;
        }
        
        boolean valide = true;
        
        // Valider chaque segment
        for (int i = 0; i < chemin.size() - 1; i++) {
            Point2D.Double p1 = chemin.get(i);
            Point2D.Double p2 = chemin.get(i + 1);
            
            if (!validerSegment(p1, p2, i)) {
                valide = false;
            }
        }
        
        // Vérifier les croisements entre segments
        if (!verifierCroisements(chemin)) {
            valide = false;
        }
        
        // Vérifier la distance entre les différents segments du fil
        if (!verifierDistanceEntreFils(chemin)) {
            valide = false;
        }
        
        return valide;
    }
    
    /**
     * Valide un segment de fil individuel.
     */
    private boolean validerSegment(Point2D.Double p1, Point2D.Double p2, int segmentIndex) {
        boolean valide = true;
        
        // 1. Vérifier la longueur du segment
        double longueur = distance(p1, p2);
        if (longueur > LONGUEUR_MAX_SEGMENT) {
            erreurs.add(String.format("Segment %d: longueur %.1f pouces dépasse la limite de %.1f pouces (%.1f pieds)", 
                segmentIndex + 1, longueur, LONGUEUR_MAX_SEGMENT, LONGUEUR_MAX_SEGMENT / 12.0));
            valide = false;
        }
        
        // 2. Vérifier la distance avec les murs
        if (!verifierDistanceMurs(p1) || !verifierDistanceMurs(p2)) {
            erreurs.add(String.format("Segment %d: trop près d'un mur (min %s pouces)", 
                segmentIndex + 1, DISTANCE_MIN_MURS_MEUBLES));
            valide = false;
        }
        
        // 3. Vérifier la distance avec les meubles sans drain
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            if (distanceSegmentRectangle(p1, p2, meuble.getX(), meuble.getY(), 
                meuble.getLargeur(), meuble.getHauteur()) < DISTANCE_MIN_MURS_MEUBLES) {
                erreurs.add(String.format("Segment %d: trop près d'un meuble (min %s pouces)", 
                    segmentIndex + 1, DISTANCE_MIN_MURS_MEUBLES));
                valide = false;
                break;
            }
        }
        
        // 4. Vérifier la distance avec les meubles avec drain
        for (MeubleDrain meuble : piece.getMeublesDrain()) {
            // Distance du meuble lui-même
            if (distanceSegmentRectangle(p1, p2, meuble.getX(), meuble.getY(), 
                meuble.getLargeur(), meuble.getHauteur()) < DISTANCE_MIN_MURS_MEUBLES) {
                erreurs.add(String.format("Segment %d: trop près d'un meuble avec drain (min %s pouces)", 
                    segmentIndex + 1, DISTANCE_MIN_MURS_MEUBLES));
                valide = false;
                break;
            }
            
            // Distance du drain
            double xDrain = meuble.getX() + meuble.getXDrainRelatif();
            double yDrain = meuble.getY() + meuble.getYDrainRelatif();
            double distanceMinDrain = (meuble.getType() == TypeMeubleDrain.TOILETTE) 
                ? DISTANCE_MIN_DRAIN_TOILETTE 
                : DISTANCE_MIN_DRAIN;
            
            double distDrain = distanceSegmentPoint(p1, p2, xDrain, yDrain);
            if (distDrain < distanceMinDrain + meuble.getDiametreDrain() / 2) {
                erreurs.add(String.format("Segment %d: trop près d'un drain de %s (min %s pouces)", 
                    segmentIndex + 1, 
                    meuble.getType() == TypeMeubleDrain.TOILETTE ? "toilette" : "meuble",
                    distanceMinDrain));
                valide = false;
                break;
            }
        }
        
        // 5. Vérifier la distance avec les éléments chauffants
        for (ElementChauffant element : piece.getElementsChauffants()) {
            if (distanceSegmentRectangle(p1, p2, element.getX(), element.getY(), 
                element.getLargeur(), element.getHauteur()) < DISTANCE_MIN_ELEMENT_CHAUFFANT) {
                erreurs.add(String.format("Segment %d: trop près d'un élément chauffant (min %s pouces)", 
                    segmentIndex + 1, DISTANCE_MIN_ELEMENT_CHAUFFANT));
                valide = false;
                break;
            }
        }
        
        return valide;
    }
    
    /**
     * Vérifie que le fil ne se croise pas lui-même.
     */
    private boolean verifierCroisements(List<Point2D.Double> chemin) {
        for (int i = 0; i < chemin.size() - 1; i++) {
            for (int j = i + 2; j < chemin.size() - 1; j++) {
                // Ne pas vérifier les segments adjacents
                if (Math.abs(i - j) <= 1) continue;
                
                Point2D.Double p1 = chemin.get(i);
                Point2D.Double p2 = chemin.get(i + 1);
                Point2D.Double p3 = chemin.get(j);
                Point2D.Double p4 = chemin.get(j + 1);
                
                if (segmentsSeCroisent(p1, p2, p3, p4)) {
                    erreurs.add(String.format("Le fil se croise entre les segments %d et %d", i + 1, j + 1));
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Vérifie que les segments du fil sont à au moins 3 pouces les uns des autres.
     */
    private boolean verifierDistanceEntreFils(List<Point2D.Double> chemin) {
        for (int i = 0; i < chemin.size() - 1; i++) {
            for (int j = i + 2; j < chemin.size() - 1; j++) {
                // Ne pas vérifier les segments adjacents
                if (Math.abs(i - j) <= 1) continue;
                
                Point2D.Double p1 = chemin.get(i);
                Point2D.Double p2 = chemin.get(i + 1);
                Point2D.Double p3 = chemin.get(j);
                Point2D.Double p4 = chemin.get(j + 1);
                
                double dist = distanceEntreSegments(p1, p2, p3, p4);
                if (dist < DISTANCE_MIN_ENTRE_FILS) {
                    erreurs.add(String.format("Segments %d et %d sont trop proches (%.1f pouces, min %s pouces)", 
                        i + 1, j + 1, dist, DISTANCE_MIN_ENTRE_FILS));
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Vérifie qu'un point est à au moins 3 pouces des murs.
     */
    private boolean verifierDistanceMurs(Point2D.Double p) {
        double minX = piece.getMinX();
        double minY = piece.getMinY();
        double maxX = piece.getMaxX();
        double maxY = piece.getMaxY();
        
        return p.x >= minX + DISTANCE_MIN_MURS_MEUBLES &&
               p.x <= maxX - DISTANCE_MIN_MURS_MEUBLES &&
               p.y >= minY + DISTANCE_MIN_MURS_MEUBLES &&
               p.y <= maxY - DISTANCE_MIN_MURS_MEUBLES;
    }
    
    /**
     * Calcule la distance entre deux points.
     */
    private double distance(Point2D.Double p1, Point2D.Double p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Calcule la distance minimale entre un segment et un point.
     */
    private double distanceSegmentPoint(Point2D.Double p1, Point2D.Double p2, double px, double py) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        
        if (dx == 0 && dy == 0) {
            return distance(p1, new Point2D.Double(px, py));
        }
        
        double t = ((px - p1.x) * dx + (py - p1.y) * dy) / (dx * dx + dy * dy);
        t = Math.max(0, Math.min(1, t));
        
        double closestX = p1.x + t * dx;
        double closestY = p1.y + t * dy;
        
        return Math.sqrt(Math.pow(px - closestX, 2) + Math.pow(py - closestY, 2));
    }
    
    /**
     * Calcule la distance minimale entre un segment et un rectangle.
     */
    private double distanceSegmentRectangle(Point2D.Double p1, Point2D.Double p2, 
                                           double rx, double ry, double largeur, double hauteur) {
        // Coins du rectangle
        double[][] coins = {
            {rx, ry},
            {rx + largeur, ry},
            {rx + largeur, ry + hauteur},
            {rx, ry + hauteur}
        };
        
        double minDist = Double.MAX_VALUE;
        
        // Distance aux coins
        for (double[] coin : coins) {
            double dist = distanceSegmentPoint(p1, p2, coin[0], coin[1]);
            minDist = Math.min(minDist, dist);
        }
        
        // Distance aux bords du rectangle
        Point2D.Double[] bordsRect = {
            new Point2D.Double(rx, ry),
            new Point2D.Double(rx + largeur, ry),
            new Point2D.Double(rx + largeur, ry + hauteur),
            new Point2D.Double(rx, ry + hauteur)
        };
        
        for (int i = 0; i < 4; i++) {
            Point2D.Double b1 = bordsRect[i];
            Point2D.Double b2 = bordsRect[(i + 1) % 4];
            double dist = distanceEntreSegments(p1, p2, b1, b2);
            minDist = Math.min(minDist, dist);
        }
        
        return minDist;
    }
    
    /**
     * Calcule la distance minimale entre deux segments.
     */
    private double distanceEntreSegments(Point2D.Double p1, Point2D.Double p2, 
                                        Point2D.Double p3, Point2D.Double p4) {
        double dist1 = distanceSegmentPoint(p1, p2, p3.x, p3.y);
        double dist2 = distanceSegmentPoint(p1, p2, p4.x, p4.y);
        double dist3 = distanceSegmentPoint(p3, p4, p1.x, p1.y);
        double dist4 = distanceSegmentPoint(p3, p4, p2.x, p2.y);
        
        return Math.min(Math.min(dist1, dist2), Math.min(dist3, dist4));
    }
    
    /**
     * Vérifie si deux segments se croisent.
     */
    private boolean segmentsSeCroisent(Point2D.Double p1, Point2D.Double p2, 
                                       Point2D.Double p3, Point2D.Double p4) {
        double d1 = direction(p3, p4, p1);
        double d2 = direction(p3, p4, p2);
        double d3 = direction(p1, p2, p3);
        double d4 = direction(p1, p2, p4);
        
        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
            ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Calcule la direction du point p par rapport au segment (p1, p2).
     */
    private double direction(Point2D.Double p1, Point2D.Double p2, Point2D.Double p) {
        return (p.x - p1.x) * (p2.y - p1.y) - (p.y - p1.y) * (p2.x - p1.x);
    }
    
    /**
     * Retourne la liste des erreurs de validation.
     */
    public List<String> getErreurs() {
        return new ArrayList<>(erreurs);
    }
}
