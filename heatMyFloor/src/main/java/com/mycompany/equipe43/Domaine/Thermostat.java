package com.mycompany.equipe43.Domaine;

/**
 * Classe Thermostat qui hérite de ElementChauffant
 * Singleton : une seule instance par pièce irrégulière
 */
public class Thermostat extends ElementChauffant {
    // Dimensions par défaut du thermostat
    private static final double LARGEUR_DEFAUT = 6.0;
    private static final double HAUTEUR_DEFAUT = 4.0;
    
    /**
     * Constructeur privé pour le singleton
     */
    private Thermostat(double x, double y, double largeur, double hauteur, boolean horizontal, double angle) {
        super(x, y, largeur, hauteur, horizontal, angle);
    }
    
    /**
     * Crée une instance de thermostat avec les dimensions par défaut
     * @param x position X
     * @param y position Y
     * @param angle angle de rotation
     * @return nouvelle instance de Thermostat
     */
    public static Thermostat creer(double x, double y, double angle) {
        // Le thermostat est toujours horizontal par défaut (mais peut être roté)
        return new Thermostat(x, y, LARGEUR_DEFAUT, HAUTEUR_DEFAUT, true, angle);
    }
    
    /**
     * Crée une instance de thermostat avec les dimensions par défaut et angle 0
     * @param x position X
     * @param y position Y
     * @return nouvelle instance de Thermostat
     */
    public static Thermostat creer(double x, double y) {
        return creer(x, y, 0.0);
    }
    
    /**
     * Vérifie si un ElementChauffant est un thermostat
     */
    public static boolean estThermostat(ElementChauffant element) {
        return element instanceof Thermostat;
    }
}

