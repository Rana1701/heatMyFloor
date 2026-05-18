package com.mycompany.equipe43.Domaine;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classe utilitaire pour gérer les conversions d'unités impériales.
 * Format accepté: X' Y" W/Z
 * Exemples: "10'", "3\"", "12' 3\" 1/4"
 * 
 * Précision: jusqu'au 1/32ème de pouce
 */
public class UniteImperiale {
    
    /**
     * Parse une chaîne au format impérial et retourne la valeur en pouces.
     * 
     * Formats acceptés:
     * - "10'" → 120.0 pouces (10 pieds)
     * - "3\"" → 3.0 pouces
     * - "3\" 3/4" → 3.75 pouces
     * - "12' 3\"" → 147.0 pouces
     * - "12' 3\" 1/4" → 147.25 pouces
     * - "1/32\"" → 0.03125 pouces
     * 
     * @param input Chaîne au format impérial
     * @return Valeur totale en pouces
     * @throws IllegalArgumentException Si le format est invalide
     */
    public static double parseVersPouces(String input) throws IllegalArgumentException {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Entrée vide");
        }
        
        input = input.trim();
        double totalPouces = 0;
        
        
        Pattern pattern = Pattern.compile(
            "(-)?(?:(\\d+)')?\\s*(?:(\\d+)\")?\\s*(?:(\\d+)/(\\d+)\"?)?"
        );
        
        Matcher matcher = pattern.matcher(input);
        
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                "Format invalide. Utilisez: X' Y\" W/Z\n" +
                "Exemples: 10', 3\", 12' 3\" 1/4"
            );
        }
        // Capturer le signe 
        String signeStr = matcher.group(1);
        boolean negatif = (signeStr != null);
        
        // Parser pieds
        String piedsStr = matcher.group(2);
        if (piedsStr != null) {
            int pieds = Integer.parseInt(piedsStr);
            totalPouces += pieds * 12.0;
        }
        
        // Parser pouces
        String poucesStr = matcher.group(3);
        if (poucesStr != null) {
            int pouces = Integer.parseInt(poucesStr);
            totalPouces += pouces;
        }
        
        // Parser fraction
        String numStr = matcher.group(4);
        String denStr = matcher.group(5);
        if (numStr != null && denStr != null) {
            int numerateur = Integer.parseInt(numStr);
            int denominateur = Integer.parseInt(denStr);
            
            if (denominateur == 0) {
                throw new IllegalArgumentException("Division par zéro dans la fraction");
            }
            
            // Valider que le dénominateur est une puissance de 2 (standard impérial)
            if (!estPuissanceDe2(denominateur)) {
                throw new IllegalArgumentException(
                    "Le dénominateur doit être une puissance de 2 (2, 4, 8, 16, 32)"
                );
            }
            
            totalPouces += (double) numerateur / denominateur;
        }
        
        // Au moins une valeur doit être présente
        if (totalPouces == 0 && piedsStr == null && poucesStr == null && numStr == null) {
            throw new IllegalArgumentException("Au moins une valeur doit être spécifiée");
        }
        // Application du signe 
        if(negatif) {
            totalPouces = -totalPouces;
        }
        return totalPouces;
    }
    
    /**
     * Formate une valeur en pouces vers le format impérial.
     * 
     * Exemples:
     * - 120.0 → "10' 0\""
     * - 3.75 → "3\" 3/4"
     * - 147.25 → "12' 3\" 1/4"
     * - 0.03125 → "1/32\""
     * 
     * @param poucesTotal Valeur en pouces
     * @return Chaîne formatée au format impérial
     */
    public static String formaterPouces(double poucesTotal) {
        if (poucesTotal < 0) {
            return "-" + formaterPouces(-poucesTotal);
        }
        
        // Extraire les pieds
        int pieds = (int)(poucesTotal / 12.0);
        double reste = poucesTotal - (pieds * 12.0);
        
        // Extraire les pouces entiers
        int pouces = (int)reste;
        double fraction = reste - pouces;
        
        StringBuilder sb = new StringBuilder();
        
        // Ajouter les pieds
        if (pieds > 0) {
            sb.append(pieds).append("'");
        }
        
        // Ajouter les pouces
        if (pouces > 0 || fraction > 0) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(pouces).append("\"");
        }
        
        // Ajouter la fraction si nécessaire
        if (fraction > 0.001) {  // Tolérance pour erreurs de float
            String fractionStr = formatFraction(fraction);
            if (!fractionStr.isEmpty()) {
                sb.append(" ").append(fractionStr);
            }
        }
        
        // Si tout est à zéro
        if (sb.length() == 0) {
            return "0\"";
        }
        
        return sb.toString();
    }
    
    /**
     * Formate une fraction décimale en fraction impériale simplifiée.
     * Précision: 1/32ème de pouce
     * 
     * @param decimal Valeur décimale entre 0 et 1
     * @return Fraction formatée (ex: "3/4")
     */
    private static String formatFraction(double decimal) {
        // Convertir en 32èmes
        int trentieDeux = (int)Math.round(decimal * 32.0);
        
        if (trentieDeux == 0) {
            return "";
        }
        
        if (trentieDeux == 32) {
            return "";  // Ça fait 1 pouce entier, déjà géré
        }
        
        // Simplifier la fraction
        int numerateur = trentieDeux;
        int denominateur = 32;
        
        // Trouver le PGCD pour simplifier
        int pgcd = pgcd(numerateur, denominateur);
        numerateur /= pgcd;
        denominateur /= pgcd;
        
        return numerateur + "/" + denominateur;
    }
    
    /**
     * Calcule le Plus Grand Commun Diviseur (PGCD).
     */
    private static int pgcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }
    
    /**
     * Vérifie si un nombre est une puissance de 2.
     */
    private static boolean estPuissanceDe2(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }
    
}

