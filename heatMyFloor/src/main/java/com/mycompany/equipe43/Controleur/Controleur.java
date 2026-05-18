package com.mycompany.equipe43.Controleur;

import com.mycompany.equipe43.Domaine.DTO.MeubleDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceIrreguliereDTO;
import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;
import com.mycompany.equipe43.Domaine.DTO.ElementChauffantDTO;
import com.mycompany.equipe43.Domaine.ElementChauffant;
import com.mycompany.equipe43.Domaine.Fil;
import com.mycompany.equipe43.Domaine.Intersection;
import com.mycompany.equipe43.Domaine.MeubleDrain;
import com.mycompany.equipe43.Domaine.MeubleSansDrain;
import com.mycompany.equipe43.Domaine.Piece;
import com.mycompany.equipe43.Domaine.TypeMeubleDrain;
import com.mycompany.equipe43.Domaine.TypeMeubleSansDrain;
import com.mycompany.equipe43.Domaine.PieceIrreguliere;
import com.mycompany.equipe43.Domaine.Thermostat;
import com.mycompany.equipe43.Domaine.Meuble;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class Controleur {
    private Piece piece;
    private MeubleSansDrain meubleSelectionne = null;
    private MeubleDrain meubleAvecDrainSelectionne = null;
    private boolean drainSelectionne = false; // true si on a cliqué sur le drain, false si sur le meuble
    // Classe interne pour stocker l'état complet (pièce régulière ou irrégulière)
    private static class EtatPiece {
        Piece pieceReguliere;
        PieceIrreguliere pieceIrreguliere;
        boolean estPieceIrreguliere;
        double distanceGrillePouces;
        double longueurFilMax;
        
        EtatPiece(Piece pieceReg, PieceIrreguliere pieceIrreg, boolean estIrreg, double distanceGrille, double longueurFil) {
            this.pieceReguliere = pieceReg;
            this.pieceIrreguliere = pieceIrreg;
            this.estPieceIrreguliere = estIrreg;
            this.distanceGrillePouces = distanceGrille;
            this.longueurFilMax = longueurFil;
        }
        
        EtatPiece copier() {
            Piece copieReg = (pieceReguliere != null) ? pieceReguliere.copier() : null;
            PieceIrreguliere copieIrreg = (pieceIrreguliere != null) ? pieceIrreguliere.copier() : null;
            return new EtatPiece(copieReg, copieIrreg, estPieceIrreguliere, distanceGrillePouces, longueurFilMax);
        }
    }
    
    private final Stack<EtatPiece> undos = new Stack<>();
    private final Stack<EtatPiece> redos = new Stack<>();
    private ElementChauffant elementSelectionne;
    //variables pour piece irreguliere
    private PieceIrreguliere pieceIrreguliere = null;
    private PieceIrreguliere pieceIrregEnCours = null;
    private boolean estPieceIrreguliere = false;
    //variables pour modification piece irreg
    private int pointSelectionneIndex = -1;
    private List<Fil> fils = new ArrayList<>();
    private Fil filSelectionne = null;
    private List<Point2D.Double> pointsInitiauxPourRedim = null;
    // Added list for validation errors
    private List<String> erreurs = new ArrayList<>();
    private List<Double> meublesXInitiaux = null;
    private List<Double> meublesYInitiaux = null;
    private List<Double> meublesDrainXInitiaux = null;
    private List<Double> meublesDrainYInitiaux = null;
    // Sauvegarde des éléments chauffants pour redimensionnement
    private List<ElementChauffant> elementsChauffantsInitiaux = null;
    // Mode pour ajout d'élément chauffant dans pièce irrégulière
    private boolean modeAjoutElementChauffant = false;
    private double largeurElementChauffantEnAttente = 30.0;
    private double hauteurElementChauffantEnAttente = 1.0;
    // Mode pour déplacement d'élément chauffant dans pièce irrégulière
    private boolean modeDeplacementElementChauffant = false;
    private ElementChauffant thermostatInitial = null;

    
    
private static class ClosestSeg {
    Point2D.Double a, b;
    Point2D.Double proj;
    double angleRad;
    double dist2;
}

private ClosestSeg trouverSegmentLePlusProche(PieceIrreguliere piece, double px, double py) {
    List<Point2D.Double> pts = piece.getPoints();
    if (pts == null || pts.size() < 2) return null;

    ClosestSeg best = null;

    for (int i = 0; i < pts.size(); i++) {
        Point2D.Double a = pts.get(i);
        Point2D.Double b = (i == pts.size() - 1) ? pts.get(0) : pts.get(i + 1);

        Point2D.Double proj = projeterSurSegment(px, py, a, b);
        double dx = px - proj.x;
        double dy = py - proj.y;
        double d2 = dx * dx + dy * dy;

        if (best == null || d2 < best.dist2) {
            best = new ClosestSeg();
            best.a = a;
            best.b = b;
            best.proj = proj;
            best.dist2 = d2;
            best.angleRad = Math.atan2(b.y - a.y, b.x - a.x);
        }
    }
    return best;
}

private Point2D.Double projeterSurSegment(double px, double py, Point2D.Double a, Point2D.Double b) {
    double vx = b.x - a.x;
    double vy = b.y - a.y;

    double wx = px - a.x;
    double wy = py - a.y;

    double len2 = vx * vx + vy * vy;
    if (len2 == 0) return new Point2D.Double(a.x, a.y);

    double t = (wx * vx + wy * vy) / len2;
    if (t < 0) t = 0;
    if (t > 1) t = 1;

    return new Point2D.Double(a.x + t * vx, a.y + t * vy);
}

private Point2D.Double inwardOffset(PieceIrreguliere piece, ClosestSeg seg, double offset) {
    double nx = -Math.sin(seg.angleRad);
    double ny =  Math.cos(seg.angleRad);

    double testX = seg.proj.x + nx * 0.5;
    double testY = seg.proj.y + ny * 0.5;

    if (!piece.contientPoint(testX, testY)) {
        nx = -nx;
        ny = -ny;
    }

    return new Point2D.Double(nx * offset, ny * offset);
}

    public Controleur() {
        this.piece = new Piece(0.0, 0.0, 120.0, 120.0); // Pièce par défaut de 10 pieds par 10 pieds à l'ouverture.
        // Sauvegarder l'état initial (pièce par défaut) pour permettre de revenir avec undo
        sauvegarderEtat();
    }
    
    //Gestion des modes
    public enum Mode {
        EDITION,
        MODELISATION
    }
    
    private Mode modeActuel = Mode.EDITION; //Mode édition par defaut
    
    public Mode getModeActuel() {
        return modeActuel;
    }
    public void setMode(Mode mode){
        this.modeActuel = mode;
    }

    public void sauvegarderEtat() {
        // Sauvegarder l'état complet (pièce régulière ou irrégulière + distance grille + longueur fil)
        Piece copieReg = (piece != null) ? piece.copier() : null;
        PieceIrreguliere copieIrreg = (pieceIrreguliere != null) ? pieceIrreguliere.copier() : null;
        EtatPiece etat = new EtatPiece(copieReg, copieIrreg, estPieceIrreguliere, distanceGrillePouces, longueurFilMax);
        undos.push(etat);
        redos.clear();
    }
    public Piece getPieceActive() {
        if (estPieceIrreguliere && pieceIrreguliere != null) {
            return pieceIrreguliere;
        }
        return piece;
    }
    public void undo() {
        if (undos.isEmpty()) return;
        
        // Sauvegarder l'état actuel dans redo
        Piece copieReg = (piece != null) ? piece.copier() : null;
        PieceIrreguliere copieIrreg = (pieceIrreguliere != null) ? pieceIrreguliere.copier() : null;
        EtatPiece etatActuel = new EtatPiece(copieReg, copieIrreg, estPieceIrreguliere, distanceGrillePouces, longueurFilMax);
        redos.push(etatActuel);
        
        // Restaurer l'état précédent
        EtatPiece etatPrecedent = undos.pop();
        restaurerEtat(etatPrecedent);
    }

    public void redo() {
        if (redos.isEmpty()) return;
        
        // Sauvegarder l'état actuel dans undo
        Piece copieReg = (piece != null) ? piece.copier() : null;
        PieceIrreguliere copieIrreg = (pieceIrreguliere != null) ? pieceIrreguliere.copier() : null;
        EtatPiece etatActuel = new EtatPiece(copieReg, copieIrreg, estPieceIrreguliere, distanceGrillePouces, longueurFilMax);
        undos.push(etatActuel);
        
        // Restaurer l'état suivant
        EtatPiece etatSuivant = redos.pop();
        restaurerEtat(etatSuivant);
    }
    
    private void restaurerEtat(EtatPiece etat) {
        // Restaurer le type de pièce
        estPieceIrreguliere = etat.estPieceIrreguliere;
        
        // Restaurer les pièces
        if (etat.estPieceIrreguliere) {
            // Pièce irrégulière
            pieceIrreguliere = (etat.pieceIrreguliere != null) ? etat.pieceIrreguliere.copier() : null;
            piece = null;
        } else {
            // Pièce régulière
            piece = (etat.pieceReguliere != null) ? etat.pieceReguliere.copier() : null;
            pieceIrreguliere = null;
        }
        
        // Restaurer la distance de la grille et la longueur du fil
        distanceGrillePouces = etat.distanceGrillePouces;
        longueurFilMax = etat.longueurFilMax;
        
        // Réinitialiser les sélections
        meubleSelectionne = null;
        meubleAvecDrainSelectionne = null;
        elementSelectionne = null;
        pointSelectionneIndex = -1;
        drainSelectionne = false;
    }
/**
    public void creerPieceReguliere(double x, double y, double largeur, double longueur) {
        sauvegarderEtat();
        
        //supprimer la piece irreg si elle existe 
        pieceIrreguliere = null;
        pieceIrregEnCours = null;
        estPieceIrreguliere = false;
        
        this.piece = new Piece(x, y, largeur, longueur);
        meubleSelectionne = null;
        elementSelectionne = null;
    }
 */
    public void creerPieceReguliere(double x, double y, double largeur, double longueur) {
    sauvegarderEtat();
    
    // Supprimer la pièce irrégulière si elle existe
    pieceIrreguliere = null;
    pieceIrregEnCours = null;
    estPieceIrreguliere = false;
    
    // Création de la pièce rectangulaire
    this.piece = new Piece(x, y, largeur, longueur);

    // Réinitialiser les sélections
    meubleSelectionne = null;
    elementSelectionne = null;

    // -------------------------------------------------
    // 🔹 CRÉATION AUTOMATIQUE DU THERMOSTAT COLLÉ AU MUR GAUCHE
    // -------------------------------------------------
    double thLargeur = 8.0;   // largeur thermostat en pouces
    double thHauteur = 6.0;   // hauteur thermostat

    // Collé au mur OUEST (x = mur gauche)
    double thX = x; 
    // Centré verticalement sur le mur
    double thY = y + (longueur / 2.0) - (thHauteur / 2.0);

    ThermostatDTO thermostat = new ThermostatDTO(thX, thY, thLargeur, thHauteur);
    this.piece.setThermostat(thermostat);
}

    public void definirTaillePiece(double largeur, double longueur) {
        sauvegarderEtat();
        piece.setTaille(largeur, longueur);
    }
    
    public void demarrerRedimensionnementPieceIrreguliere() {
    if (pieceIrreguliere != null) {
        // Sauvegarder les points initiaux
        sauvegarderEtat();
        pointsInitiauxPourRedim = new ArrayList<>(pieceIrreguliere.getPoints());
        // Sauvegarder les positions X et Y
        meublesXInitiaux = new ArrayList<>();
        meublesYInitiaux = new ArrayList<>();
        for (MeubleSansDrain m : pieceIrreguliere.getMeubles()) {
            meublesXInitiaux.add(m.getX());
            meublesYInitiaux.add(m.getY());
        }
        
        meublesDrainXInitiaux = new ArrayList<>();
        meublesDrainYInitiaux = new ArrayList<>();
        for (MeubleDrain m : pieceIrreguliere.getMeublesDrain()) {
            meublesDrainXInitiaux.add(m.getX());
            meublesDrainYInitiaux.add(m.getY());
        }
        
        // Sauvegarder les éléments chauffants
        elementsChauffantsInitiaux = new ArrayList<>();
        for (ElementChauffant e : pieceIrreguliere.getElementsChauffants()) {
            // Créer une copie de l'élément
            ElementChauffant copie = new ElementChauffant(
                e.getX(), e.getY(), 
                e.getLargeur(), e.getHauteur(), 
                e.isHorizontal(), e.getAngle()
            );
            elementsChauffantsInitiaux.add(copie);
        }
        //SAUVEGARDER LE THERMOSTAT SÉPARÉMENT
        Thermostat thermo = pieceIrreguliere.getThermostatIrregulier();
        if (thermo != null) {
            // Créer une copie du thermostat
            thermostatInitial = new ElementChauffant (
            thermo.getX(), thermo.getY(), 
            thermo.getLargeur(), thermo.getHauteur(), 
            thermo.isHorizontal(), thermo.getAngle()
        );
        } else {
            thermostatInitial = null;
        }
    }
}

    //redim piece irreg
   
    public void redimensionnerPieceIrreguliere(double facteurX, double facteurY){
    if (pieceIrreguliere != null && pointsInitiauxPourRedim != null) {
        
        // Vérifier que les facteurs sont valides
        if (facteurX <= 0 || facteurY <= 0) {
            return;
        }
        
        // Calculer la bounding box des points initiaux
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;
        
        for (Point2D.Double p : pointsInitiauxPourRedim) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
        }
        
        double largeurInitiale = maxX - minX;
        double longueurInitiale = maxY - minY;
        
        // Vérifier qu'on n'a pas une pièce dégénérée
        if (largeurInitiale <= 0 || longueurInitiale <= 0) {
            return;
        }
        
        // Créer les nouveaux points en appliquant le redimensionnement
        // basé sur la position relative dans la bounding box
        List<Point2D.Double> nouveauxPoints = new ArrayList<>();
        
        for (Point2D.Double p : pointsInitiauxPourRedim) {
            // Position relative du point dans la bounding box (valeur entre 0 et 1)
            double ratioX = (p.x - minX) / largeurInitiale;
            double ratioY = (p.y - minY) / longueurInitiale;
            
            // Appliquer le facteur de redimensionnement
            // Le coin (minX, minY) reste fixe, on agrandit à partir de là
            double nouveauX = minX + (ratioX * largeurInitiale * facteurX);
            double nouveauY = minY + (ratioY * longueurInitiale * facteurY);
            
            nouveauxPoints.add(new Point2D.Double(nouveauX, nouveauY));
        }
        //Sauvegarder les meubles actuels
        List<MeubleSansDrain> meubles = new ArrayList<>(pieceIrreguliere.getMeubles());
        List<MeubleDrain> meublesDrain = new ArrayList<>(pieceIrreguliere.getMeublesDrain());

        
        // Remplacer les points de la pièce
        pieceIrreguliere = new PieceIrreguliere(nouveauxPoints);
        pieceIrreguliere.fermerPolygone();
        // Repositionner les meubles avec positions INITIALES
        double nouvelleLargeur = largeurInitiale * facteurX;
        double nouvelleLongueur = longueurInitiale * facteurY;
        
        for (int i = 0; i < meubles.size() && i < meublesXInitiaux.size(); i++) {
            MeubleSansDrain m = meubles.get(i);
            double nouveauX = minX + ((meublesXInitiaux.get(i) - minX) * facteurX);
            double nouveauY = minY + ((meublesYInitiaux.get(i) - minY) * facteurY);
            
            // Ajuster limites
            if (nouveauX < minX) nouveauX = minX;
            if (nouveauY < minY) nouveauY = minY;
            if (nouveauX + m.getLargeur() > minX + nouvelleLargeur) 
                nouveauX = minX + nouvelleLargeur - m.getLargeur();
            if (nouveauY + m.getHauteur() > minY + nouvelleLongueur) 
                nouveauY = minY + nouvelleLongueur - m.getHauteur();
            
            pieceIrreguliere.ajouterMeuble(nouveauX, nouveauY, 
                m.getLargeur(), m.getHauteur(), m.getType());
        }
        
        for (int i = 0; i < meublesDrain.size() && i < meublesDrainXInitiaux.size(); i++) {
            MeubleDrain m = meublesDrain.get(i);
            double nouveauX = minX + ((meublesDrainXInitiaux.get(i) - minX) * facteurX);
            double nouveauY = minY + ((meublesDrainYInitiaux.get(i) - minY) * facteurY);
            
            // Ajuster limites
            if (nouveauX < minX) nouveauX = minX;
            if (nouveauY < minY) nouveauY = minY;
            if (nouveauX + m.getLargeur() > minX + nouvelleLargeur) 
                nouveauX = minX + nouvelleLargeur - m.getLargeur();
            if (nouveauY + m.getHauteur() > minY + nouvelleLongueur) 
                nouveauY = minY + nouvelleLongueur - m.getHauteur();
            
            pieceIrreguliere.ajouterMeubleDrain(nouveauX, nouveauY, 
                m.getLargeur(), m.getHauteur(), m.getType(),
                m.getXDrainRelatif(), m.getYDrainRelatif(), m.getDiametreDrain());
        }
        
        // Repositionner les éléments chauffants (SANS le thermostat)
if (elementsChauffantsInitiaux != null) {
    for (ElementChauffant elementInitial : elementsChauffantsInitiaux) {
        repositionnerElementChauffantSurMur(elementInitial, nouveauxPoints, pointsInitiauxPourRedim);
    }
}

// 🔹 REPOSITIONNER LE THERMOSTAT SÉPARÉMENT en utilisant this.thermostatInitial
if (this.thermostatInitial != null) {
    // Recréer le thermostat par défaut
    pieceIrreguliere.creerThermostatParDefaut();
    
    // Repositionner le thermostat sauvegardé
    repositionnerElementChauffantSurMur(this.thermostatInitial, nouveauxPoints, pointsInitiauxPourRedim);
    
    // Récupérer le dernier élément ajouté (le thermostat repositionné)
    if (!pieceIrreguliere.getElementsChauffants().isEmpty()) {
        ElementChauffant dernierElement = pieceIrreguliere.getElementsChauffants()
            .get(pieceIrreguliere.getElementsChauffants().size() - 1);
        
        // Le retirer de la liste générale
        pieceIrreguliere.getElementsChauffants().remove(dernierElement);
        
        // Mettre à jour le thermostat singleton
        Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
        if (thermostat != null) {
            thermostat.setX(dernierElement.getX());
            thermostat.setY(dernierElement.getY());
            thermostat.setAngle(dernierElement.getAngle());
            thermostat.setHorizontal(dernierElement.isHorizontal());
        }
    }
}
}
    }
    public void terminerRedimensionnementPieceIrreguliere() {
        pointsInitiauxPourRedim = null;
        meublesXInitiaux = null;
        meublesYInitiaux = null;
        meublesDrainXInitiaux = null;
        meublesDrainYInitiaux = null;
        elementsChauffantsInitiaux = null;
}
    //redimensionner piece irreg avec longueur et largeur
    public boolean redimensionnerPieceIrregAbsolu(double nouvelleLargeur, double nouvelleLongueur) {
    if (pieceIrreguliere == null) return false;
    if (nouvelleLargeur <= 0 || nouvelleLongueur <= 0) return false;
    
    // Calculer les dimensions actuelles
    double largeurActuelle = pieceIrreguliere.getLargeur();
    double longueurActuelle = pieceIrreguliere.getLongueur();
    
    // Calculer les facteurs de redimensionnement
    double facteurX = nouvelleLargeur / largeurActuelle;
    double facteurY = nouvelleLongueur / longueurActuelle;
    
    // Utiliser la méthode existante
    demarrerRedimensionnementPieceIrreguliere();
    redimensionnerPieceIrreguliere(facteurX, facteurY);
    terminerRedimensionnementPieceIrreguliere();
    
    return true;
}
    public boolean redimensionnerPiece(double nouvelleLargeur, double nouvelleLongueur) {
        return redimensionnerPiece(nouvelleLargeur, nouvelleLongueur, true);
    }
    
    public boolean redimensionnerPiece(double nouvelleLargeur, double nouvelleLongueur, boolean sauvegarder) {
        if (sauvegarder) {
            sauvegarderEtat();
        }
        if (piece == null) return false;
        
        double ancienneLargeur = piece.getLargeur();
        double ancienneLongueur = piece.getLongueur();

        double facteurX = nouvelleLargeur / piece.getLargeur();
        double facteurY = nouvelleLongueur / piece.getLongueur();

        piece.redimensionner(nouvelleLargeur, nouvelleLongueur);

        boolean avertissementAffiche = false;
        boolean meublerepositionne = false;
        //REDIMENSIONNER LES MEUBLES AVEC DRAIN
        
        for (MeubleDrain meuble : piece.getMeublesDrain()) {
        double nouveauX = meuble.getX() * facteurX;
        double nouveauY = meuble.getY() * facteurY;

        if (!estDansPiece(nouveauX, nouveauY, meuble.getLargeur(), meuble.getHauteur())) {
            meublerepositionne = true;
            
            if (!avertissementAffiche) {
                /*JOptionPane.showMessageDialog(
                    null,
                    "Certains meubles dépassent les limites de la pièce après le redimensionnement.\n" +
                    "Ils ont été replacés automatiquement à l'intérieur.",
                    "Avertissement",
                    JOptionPane.WARNING_MESSAGE
                );*/
               
                
                avertissementAffiche = true;
            }
            double[] posAjustee = ajusterPositionDansPiece(nouveauX, nouveauY, meuble.getLargeur(), meuble.getHauteur());
            nouveauX = posAjustee[0];
            nouveauY = posAjustee[1];
            
        }
        meuble.setPosition(nouveauX, nouveauY);
    }
    //REDIMENSIONNER LES MEUBLES SANS DRAIN
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            double nouveauX = meuble.getX() * facteurX;
            double nouveauY = meuble.getY() * facteurY;

            if (!estDansPiece(nouveauX, nouveauY, meuble.getLargeur(), meuble.getHauteur())) {
                meublerepositionne = true;
                
                if (!avertissementAffiche ) {
                    /*JOptionPane.showMessageDialog(
                        null,
                        "Certains meubles dépassent les limites de la pièce après le redimensionnement.\n" +
                        "Ils ont été replacés automatiquement à l'intérieur.",
                        "Avertissement",
                        JOptionPane.WARNING_MESSAGE
                    );*/
                    
                    
                    avertissementAffiche = true;
                }
                double[] posAjustee = ajusterPositionDansPiece(nouveauX, nouveauY, meuble.getLargeur(), meuble.getHauteur());
            nouveauX = posAjustee[0];
            nouveauY = posAjustee[1];
            }
            meuble.setPosition(nouveauX, nouveauY);
        }
            // REPOSITIONNER LES ÉLÉMENTS CHAUFFANTS
        for (ElementChauffant element : piece.getElementsChauffants()) {
            double nouveauX = element.getX();
            double nouveauY = element.getY();

            if (element.isHorizontal()) {
                // Élément horizontal (Nord ou Sud)
                // Déterminer si c'est Nord ou Sud en regardant la position Y
                boolean estNord = Math.abs(element.getY() - (piece.getY() + ancienneLongueur - 1)) < 5.0;

                if (estNord) {
                    // Mur Nord : rester collé en haut
                    nouveauY = piece.getY() + nouvelleLongueur - 1;
                } else {
                    // Mur Sud : rester collé en bas
                    nouveauY = piece.getY() + 1;
                }

                // Proportionner la position X le long du mur
                nouveauX = piece.getX() + (element.getX() - piece.getX()) * facteurX;

            } else {
                // Élément vertical (Est ou Ouest)
                // Déterminer si c'est Est ou Ouest en regardant la position X
                boolean estEst = Math.abs(element.getX() - (piece.getX() + ancienneLargeur - 1)) < 5.0;

                if (estEst) {
                    // Mur Est : rester collé à droite
                    nouveauX = piece.getX() + nouvelleLargeur - 1;
                } else {
                    // Mur Ouest : rester collé à gauche
                    nouveauX = piece.getX() + 1;
                }

                // Proportionner la position Y le long du mur
                nouveauY = piece.getY() + (element.getY() - piece.getY()) * facteurY;
            }

            // Appliquer les nouvelles positions
            element.setX(nouveauX);
            element.setY(nouveauY);
        }
        return meublerepositionne;
    }
    private double[] ajusterPositionDansPiece(double x, double y, double largeur, double hauteur) {
        double minX = piece.getX();
        double minY = piece.getY();
        double maxX = piece.getX() + piece.getLargeur() - largeur;
        double maxY = piece.getY() + piece.getLongueur() - hauteur;

        if (x < minX) x = minX;
        if (y < minY) y = minY;
        if (x > maxX) x = maxX;
        if (y > maxY) y = maxY;

        return new double[]{x, y};
    }
    
    //gestion d'une piece irreguliere
    public void demarrerCreationPieceIrreguliere() {
        //supprimer l'ancienne piece rectangulaire
        piece = null;
        pieceIrreguliere = null;
        estPieceIrreguliere = false;
        
        //demarrer la creation 
        pieceIrregEnCours = new PieceIrreguliere();
        sauvegarderEtat();
    }
    public void deplacerThermostatSnap(double xPouces, double yPouces) {
    if (piece == null) return;
    ThermostatDTO th = piece.getThermostat();
    if (th == null) return;

    double largeurTh = th.getLargeur();
    double hauteurTh = th.getHauteur();

    double pieceX = piece.getX();
    double pieceY = piece.getY();
    double pieceLargeur = piece.getLargeur();
    double pieceLongueur = piece.getLongueur();

    // Limites possibles pour le thermostat le long des murs
    double leftX   = pieceX;
    double rightX  = pieceX + pieceLargeur - largeurTh;
    double bottomY = pieceY;
    double topY    = pieceY + pieceLongueur - hauteurTh;

    // Distances du point demandé aux 4 murs
    double distGauche = Math.abs(xPouces - pieceX);
    double distDroite = Math.abs(xPouces - (pieceX + pieceLargeur));
    double distBas    = Math.abs(yPouces - pieceY);
    double distHaut   = Math.abs(yPouces - (pieceY + pieceLongueur));

    double minDist = distGauche;
    String mur = "GAUCHE";

    if (distDroite < minDist) {
        minDist = distDroite;
        mur = "DROITE";
    }
    if (distBas < minDist) {
        minDist = distBas;
        mur = "BAS";
    }
    if (distHaut < minDist) {
        minDist = distHaut;
        mur = "HAUT";
    }

    double newX = th.getX();
    double newY = th.getY();

    switch (mur) {
        case "GAUCHE":
            newX = leftX;
            newY = yPouces - hauteurTh / 2.0;
            if (newY < bottomY) newY = bottomY;
            if (newY > topY)    newY = topY;
            break;

        case "DROITE":
            newX = rightX;
            newY = yPouces - hauteurTh / 2.0;
            if (newY < bottomY) newY = bottomY;
            if (newY > topY)    newY = topY;
            break;

        case "BAS":
            newY = bottomY;
            newX = xPouces - largeurTh / 2.0;
            if (newX < leftX)  newX = leftX;
            if (newX > rightX) newX = rightX;
            break;

        case "HAUT":
            newY = topY;
            newX = xPouces - largeurTh / 2.0;
            if (newX < leftX)  newX = leftX;
            if (newX > rightX) newX = rightX;
            break;
    }

    // On réutilise la logique existante pour mettre à jour le thermostat
    piece.setThermostat(new ThermostatDTO(newX, newY, largeurTh, hauteurTh));
}

public void ajouterThermostat(double x, double y, double largeur, double hauteur) {
    ajouterThermostat(x, y, largeur, hauteur, true);
}

public void ajouterThermostat(double x, double y, double largeur, double hauteur, double angle) {
    ajouterThermostat(x, y, largeur, hauteur, angle, true);
}

public void ajouterThermostat(double x, double y, double largeur, double hauteur, boolean sauvegarder) {
    ajouterThermostat(x, y, largeur, hauteur, 0.0, sauvegarder);
}

public void ajouterThermostat(double x, double y, double largeur, double hauteur, double angle, boolean sauvegarder) {
    if (piece == null && pieceIrreguliere == null) return;

    if (sauvegarder) sauvegarderEtat();

    // Si c'est une pièce irrégulière, utiliser Thermostat (hérite de ElementChauffant)
    if (pieceIrreguliere != null) {
        // Créer ou obtenir le thermostat (singleton)
        Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
        if (thermostat == null) {
            pieceIrreguliere.creerThermostatParDefaut();
            thermostat = pieceIrreguliere.getThermostatIrregulier();
        }
        
        // Définir la position et l'angle du thermostat
        thermostat.setPosition(x, y);
        thermostat.setAngle(angle);
    } else {
        // Pour les pièces régulières, utiliser ThermostatDTO
        ThermostatDTO t = new ThermostatDTO(x, y, largeur, hauteur, angle);
        piece.setThermostat(t);
    }
}

public void ajouterPointPieceIrreguliere(double xPouces, double yPouces) {
    if (pieceIrregEnCours == null) return;

    // Ajouter simplement le point, NE PAS fermer ici
    pieceIrregEnCours.ajouterPoint(xPouces, yPouces);
}

public void ajouterElementChauffantDepuisLoad(double x, double y,
                                              double largeur,
                                              double hauteur,
                                              boolean horizontal) {
    ajouterElementChauffantDepuisLoad(x, y, largeur, hauteur, horizontal, 0.0);
}

public void ajouterElementChauffantDepuisLoad(double x, double y,
                                              double largeur,
                                              double hauteur,
                                              boolean horizontal,
                                              double angle) {
    // Gérer les pièces irrégulières
    if (pieceIrreguliere != null) {
        ElementChauffant e = new ElementChauffant(x, y, largeur, hauteur, horizontal, angle);
        pieceIrreguliere.getElementsChauffants().add(e);
        return;
    }
    
    // Gérer les pièces régulières
    if (piece == null) return;

    ElementChauffant e = new ElementChauffant(x, y, largeur, hauteur, horizontal, angle);
    piece.getElementsChauffants().add(e);
}

public void chargerPieceIrreguliere(List<Point2D.Double> pts, boolean fermee,
                                    double minX, double minY, double maxX, double maxY) {
    this.piece = null;
    this.pieceIrreguliere = new PieceIrreguliere(pts);
    if (fermee) pieceIrreguliere.fermerPolygone();
    this.estPieceIrreguliere = true;
}
public void fermerPieceIrreguliere() {
    if (pieceIrregEnCours != null && pieceIrregEnCours.getNombrePoints() >= 3) {

        // 1️⃣ Fermer le polygone
        pieceIrregEnCours.fermerPolygone();

        piece = null;
        pieceIrreguliere = pieceIrregEnCours;
        pieceIrregEnCours = null;
        estPieceIrreguliere = true;

        // 2️⃣ CRÉER LE THERMOSTAT (singleton) automatiquement sur le premier mur
        // Le thermostat est déjà créé avec la bonne orientation dans creerThermostatParDefaut()
        pieceIrreguliere.creerThermostatParDefaut();

        // 4️⃣ Sauvegarde finale
        sauvegarderEtat();
    }
}

     private boolean estPieceIrreguliereActive() {
    return pieceIrreguliere != null || pieceIrregEnCours != null;
}
     private boolean verifierCollisionIrreguliere(double x, double y, double largeur, double hauteur, int idIgnore) {
    if (pieceIrreguliere == null) return false;
    
    double x1 = x;
    double y1 = y;
    double x2 = x + largeur;
    double y2 = y + hauteur;
    
    // 1) Meubles sans drain
    for (MeubleSansDrain meuble : pieceIrreguliere.getMeubles()) {
        if (meuble.getId() == idIgnore) continue;
        
        double mx1 = meuble.getX();
        double my1 = meuble.getY();
        double mx2 = meuble.getX() + meuble.getLargeur();
        double my2 = meuble.getY() + meuble.getHauteur();
        
        if (x1 < mx2 && x2 > mx1 && y1 < my2 && y2 > my1) {
            return true;
        }
    }
    
    // 2) Meubles avec drain
    for (MeubleDrain meuble : pieceIrreguliere.getMeublesDrain()) {
        if (meuble.getId() == idIgnore) continue;
        
        double mx1 = meuble.getX();
        double my1 = meuble.getY();
        double mx2 = meuble.getX() + meuble.getLargeur();
        double my2 = meuble.getY() + meuble.getHauteur();
        
        if (x1 < mx2 && x2 > mx1 && y1 < my2 && y2 > my1) {
            return true;
        }
    }
    
    return false;
}
     public void annulerCreationPieceIrreguliere(){
         pieceIrregEnCours = null;
     }
     
     //Vérifie si on est en train de créer une pièce irrégulière
     public boolean estEnCreationPieceIrreguliere(){
         return pieceIrregEnCours != null;
     }
     
     public PieceIrreguliereDTO getPieceIrreguliereEnCours(){
         if (pieceIrregEnCours == null) return null;
         
         return new PieceIrreguliereDTO(
            pieceIrregEnCours.getPoints(),
            pieceIrregEnCours.estFermee(),
            new ArrayList<>(),
            new ArrayList<>(),
            pieceIrregEnCours.getMinX(),
            pieceIrregEnCours.getMinY(),
            pieceIrregEnCours.getMaxX(),
            pieceIrregEnCours.getMaxY() ,
    null       
         );
     }
     
public PieceIrreguliereDTO getPieceIrreguliere() {
    if (pieceIrreguliere == null) return null;
    
    // CONVERTIR LES MEUBLES EN DTOs
    List<MeubleSansDrainDTO> meublesDTO = new ArrayList<>();
    for (MeubleSansDrain meuble : pieceIrreguliere.getMeubles()) {
        meublesDTO.add(new MeubleSansDrainDTO(
            meuble.getId(),
            meuble.getX(),
            meuble.getY(),
            meuble.getLargeur(),
            meuble.getHauteur(),
            meuble.getType()
        ));
    }
    
    List<MeubleDrainDTO> meublesDrainDTO = new ArrayList<>();
    for (MeubleDrain meuble : pieceIrreguliere.getMeublesDrain()) {
        meublesDrainDTO.add(new MeubleDrainDTO(
            meuble.getId(),
            meuble.getX(),
            meuble.getY(),
            meuble.getLargeur(),
            meuble.getHauteur(),
            meuble.getType(),
            meuble.getXDrainRelatif(),
            meuble.getYDrainRelatif(),
            meuble.getDiametreDrain()
        ));
    }
    
    // Convertir le Thermostat en ThermostatDTO pour le DTO
    ThermostatDTO thermostatDTO = null;
    Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
    if (thermostat != null) {
        thermostatDTO = new ThermostatDTO(
            thermostat.getX(),
            thermostat.getY(),
            thermostat.getLargeur(),
            thermostat.getHauteur(),
            thermostat.getAngle()
        );
    }
    
    // Convertir les éléments chauffants en DTOs
    List<ElementChauffantDTO> elementsChauffantsDTO = new ArrayList<>();
    for (ElementChauffant e : pieceIrreguliere.getElementsChauffants()) {
        elementsChauffantsDTO.add(new ElementChauffantDTO(
            e.getX(),
            e.getY(),
            e.getLargeur(),
            e.getHauteur(),
            e.isHorizontal(),
            e.getAngle()
        ));
    }
    
    return new PieceIrreguliereDTO(
        pieceIrreguliere.getPoints(),
        pieceIrreguliere.estFermee(),
        meublesDTO,
        meublesDrainDTO,
        pieceIrreguliere.getMinX(),
        pieceIrreguliere.getMinY(),
        pieceIrreguliere.getMaxX(),
        pieceIrreguliere.getMaxY(),
        thermostatDTO,
        elementsChauffantsDTO
    );
}
     
     public boolean estPieceIrreguliere(){
         return estPieceIrreguliere;
     }
     public ThermostatDTO getThermostat() {
    if (piece == null) return null;
    return piece.getThermostat();
}
     //gestion de la modification d'une piece irreg
     public boolean selectionnerPointPieceIrreguliere(double xPouces, double yPouces){
         if(pieceIrreguliere == null || !pieceIrreguliere.estFermee() ) {
             return false;
            
         }
         double tolerance = 2.0;// 10 pouces de tolérance pour cliquer sur un point
         List<Point2D.Double> points = pieceIrreguliere.getPoints();
         
         for(int i =0; i<points.size(); i++){
             Point2D.Double point = points.get(i);
             double distance = Math.sqrt(Math.pow(xPouces - point.x, 2)+ 
                                                  Math.pow(yPouces - point.y, 2));
             
             if(distance <= tolerance){
                 pointSelectionneIndex = i;
                 return true;
             }
             
         }
         pointSelectionneIndex = -1;
         return false;
         
     }
     
     //deplacer le point selectionné vers une nouvelle pos
     public boolean deplacerPointSelectionne(double nouveauX, double nouveauY){
         if(pieceIrreguliere == null || pointSelectionneIndex < 0){
             return false;
         }
         pieceIrreguliere.deplacerPoint(pointSelectionneIndex, nouveauX, nouveauY);
         return true;
     }

     //deselctionne le point actuellement selectionné
     public void deselectionnerPointPieceIrreguliere(){
         pointSelectionneIndex = -1;
     }

    public int getPointSelectionneIndex() {
        return pointSelectionneIndex;
    }
     
     // supprime le point selectionné si au mois 3 pts restent
    public boolean supprimerPointSelectionne(){
        if(pieceIrreguliere == null || pointSelectionneIndex <0){
            return false;
        }
        
        if(pieceIrreguliere.getNombrePoints()<= 3){
            return false;
        }
        
        pieceIrreguliere.supprimerPoint(pointSelectionneIndex);
        pointSelectionneIndex = -1;
        return true;
    }
public void ajouterMeubleSansDrain(double x, double y, double largeur, double hauteur, 
                                   TypeMeubleSansDrain type) {
    sauvegarderEtat();
    
    // PIÈCE IRREGULIÈRE
    if (pieceIrreguliere != null) {
        double posX = pieceIrreguliere.getMinX() + x;
        double posY = pieceIrreguliere.getMinY() + y;
        
        // VÉRIFIER COLLISION
        if (verifierCollisionIrreguliere(posX, posY, largeur, hauteur, -1)) {
            // Chercher une position libre
            double[] posLibre = trouverPositionLibreIrreguliere(largeur, hauteur);
            if (posLibre != null) {
                posX = posLibre[0];
                posY = posLibre[1];
            }
        }
        
        pieceIrreguliere.ajouterMeuble(posX, posY, largeur, hauteur, type);
        return;
    }
    
    // PIÈCE RÉGULIÈRE (votre code existant)
    if (piece != null) {
        double[] positionValide = trouverPositionValide(piece.getX() + x, piece.getY() + y, 
                                                        largeur, hauteur);
        piece.ajouterMeuble(positionValide[0], positionValide[1], largeur, hauteur, type);
    }
}

public void ajouterMeubleAvecDrain(double x, double y, double largeur, double hauteur,
                                   TypeMeubleDrain type, double xDrainRelatif, 
                                   double yDrainRelatif, double diametreDrain) {
    sauvegarderEtat();
    
    // PIÈCE IRREGULIÈRE
    if (pieceIrreguliere != null) {
        double posX = pieceIrreguliere.getMinX() + x;
        double posY = pieceIrreguliere.getMinY() + y;
        
        // Vérifier collision
        if (verifierCollisionIrreguliere(posX, posY, largeur, hauteur, -1)) {
            double[] posLibre = trouverPositionLibreIrreguliere(largeur, hauteur);
            if (posLibre != null) {
                posX = posLibre[0];
                posY = posLibre[1];
            }
        }
        
        pieceIrreguliere.ajouterMeubleDrain(posX, posY, largeur, hauteur, type,
                                          xDrainRelatif, yDrainRelatif, diametreDrain);
        return;
    }
    
    // PIÈCE RÉGULIÈRE (votre code existant)
    if (piece != null) {
        double posX = piece.getX() + x;
        double posY = piece.getY() + y;

        double[] positionValide = trouverPositionValide(posX, posY, largeur, hauteur);
        double xFinal = positionValide[0];
        double yFinal = positionValide[1];

        piece.ajouterMeubleDrain(xFinal, yFinal, largeur, hauteur, type,
                                xDrainRelatif, yDrainRelatif, diametreDrain);
    }
}


private double[] trouverPositionLibreIrreguliere(double largeur, double hauteur) {
    if (pieceIrreguliere == null) return null;
    
    double minX = pieceIrreguliere.getMinX();
    double minY = pieceIrreguliere.getMinY();
    double maxX = pieceIrreguliere.getMaxX() - largeur;
    double maxY = pieceIrreguliere.getMaxY() - hauteur;
    
    double step = Math.max(largeur, hauteur);
    
    // Parcourir la bounding box
    for (double y = minY; y <= maxY; y += step) {
        for (double x = minX; x <= maxX; x += step) {
            if (!verifierCollisionIrreguliere(x, y, largeur, hauteur, -1) &&
                estDansPieceIrreguliere(x, y, largeur, hauteur)) {
                return new double[]{x, y};
            }
        }
    }
    
    return null; // Pas de place
}

private boolean estDansPieceIrreguliere(double x, double y, double largeur, double hauteur) {
    if (pieceIrreguliere == null) return false;
    
    // Utiliser la vraie vérification de polygone (point-in-polygon)
    return pieceIrreguliere.contientRectangle(x, y, largeur, hauteur);
}
    public boolean supprimerMeubleSelectionne() {
        sauvegarderEtat();

        if (meubleSelectionne != null) {
            boolean ok = false;
            if (estPieceIrreguliere && pieceIrreguliere != null) {
                ok = pieceIrreguliere.supprimerMeubleParId(meubleSelectionne.getId());
            } else if (piece != null) {
                ok = piece.supprimerMeubleParId(meubleSelectionne.getId());
            }
            if (ok) {
                meubleSelectionne = null;
            }
            return ok;
        }

        if (meubleAvecDrainSelectionne != null) {
            boolean ok = false;
            if (estPieceIrreguliere && pieceIrreguliere != null) {
                ok = pieceIrreguliere.supprimerMeubleParId(meubleAvecDrainSelectionne.getId());
            } else if (piece != null) {
                ok = piece.supprimerMeubleParId(meubleAvecDrainSelectionne.getId());
            }
            if (ok) {
                meubleAvecDrainSelectionne = null;
                drainSelectionne = false;
            }
            return ok;
        }

        // Rien sélectionné
        return false;
    }

    public boolean supprimerMeubleParId(int id) {
        sauvegarderEtat();

        if (meubleSelectionne != null && meubleSelectionne.getId() == id) {
            meubleSelectionne = null;
        }

        if (meubleAvecDrainSelectionne != null && meubleAvecDrainSelectionne.getId() == id) {
            meubleAvecDrainSelectionne = null;
            drainSelectionne = false;
        }

        // Vérifier le type de pièce et utiliser la bonne méthode
        if (estPieceIrreguliere && pieceIrreguliere != null) {
            return pieceIrreguliere.supprimerMeubleParId(id);
        } else if (piece != null) {
            return piece.supprimerMeubleParId(id);
        }
        return false;
    }
/*
    public boolean modifierDrainSelectionne(double nouveauDiametre, double nouveauXAbsolu, double nouveauYAbsolu) {
        sauvegarderEtat();

        // Il faut que ce soit bien le drain qui soit sélectionné
        if (meubleAvecDrainSelectionne == null || !drainSelectionne) {
            return false;
        }
        if (nouveauDiametre <= 0) return false;

        // Coordonnées relatives = (absolu - position du meuble)
        double relX = nouveauXAbsolu - meubleAvecDrainSelectionne.getX();
        double relY = nouveauYAbsolu - meubleAvecDrainSelectionne.getY();

        // On s'assure que le drain reste dans le meuble (on le "clampe" dans le rectangle)
        if (relX < 0) relX = 0;
        if (relY < 0) relY = 0;
        if (relX > meubleAvecDrainSelectionne.getLargeur()) relX = meubleAvecDrainSelectionne.getLargeur();
        if (relY > meubleAvecDrainSelectionne.getHauteur()) relY = meubleAvecDrainSelectionne.getHauteur();

        meubleAvecDrainSelectionne.setDiametreDrain(nouveauDiametre);
        meubleAvecDrainSelectionne.setXDrainRelatif(relX);
        meubleAvecDrainSelectionne.setYDrainRelatif(relY);

        return true;
    }
*/
    public boolean modifierDrainSelectionne(double nouveauDiametre,
                                        double nouveauXRelatif,
                                        double nouveauYRelatif) {
    // Il faut qu'un meuble AVEC drain soit sélectionné ET que le drain soit sélectionné
    if (!drainSelectionne || meubleAvecDrainSelectionne == null) {
        return false;
    }

    // On enregistre l'état AVANT de modifier
    sauvegarderEtat();

    // 1. Utiliser les valeurs RELATIVES directement
    double xRelatif = nouveauXRelatif;
    double yRelatif = nouveauYRelatif;

    // 2. Clamp pour rester DANS le meuble
    if (xRelatif < 0) xRelatif = 0;
    if (yRelatif < 0) yRelatif = 0;
    if (xRelatif > meubleAvecDrainSelectionne.getLargeur()) {
        xRelatif = meubleAvecDrainSelectionne.getLargeur();
    }
    if (yRelatif > meubleAvecDrainSelectionne.getHauteur()) {
        yRelatif = meubleAvecDrainSelectionne.getHauteur();
    }

    // 3. Appliquer les nouvelles valeurs
    meubleAvecDrainSelectionne.setXDrainRelatif(xRelatif);
    meubleAvecDrainSelectionne.setYDrainRelatif(yRelatif);

    if (nouveauDiametre > 0) {
        meubleAvecDrainSelectionne.setDiametreDrain(nouveauDiametre);
    }

    return true;
}
public void deplacerThermostatIrregulierSnap(double x, double y, boolean sauvegarder) {
    if (pieceIrreguliere == null) return;
    
    Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
    if (thermostat == null) {
        // Créer le thermostat s'il n'existe pas (singleton)
        pieceIrreguliere.creerThermostatParDefaut();
        thermostat = pieceIrreguliere.getThermostatIrregulier();
    }

    if (sauvegarder) sauvegarderEtat();

    // Utiliser la même logique que pour les éléments chauffants
    // Sélectionner temporairement le thermostat comme élément sélectionné
    ElementChauffant elementSelectionnePrecedent = elementSelectionne;
    elementSelectionne = thermostat;
    
    // Utiliser la méthode de drag des éléments chauffants
    double cx = x + thermostat.getLargeur() / 2.0;
    double cy = y + thermostat.getHauteur() / 2.0;
    deplacerElementChauffantDragIrregulier(cx, cy);
    
    // Restaurer l'élément sélectionné précédent
    elementSelectionne = elementSelectionnePrecedent;
}

/**
 * Déplace le thermostat en temps réel pendant le drag dans une pièce irrégulière
 * Utilise exactement la même logique que deplacerElementChauffantDragIrregulier
 * @param xPouces coordonnée X du point de clic
 * @param yPouces coordonnée Y du point de clic
 */
public void deplacerThermostatDragIrregulier(double xPouces, double yPouces) {
    if (pieceIrreguliere == null) return;
    
    Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
    if (thermostat == null) {
        // Créer le thermostat s'il n'existe pas (singleton)
        pieceIrreguliere.creerThermostatParDefaut();
        thermostat = pieceIrreguliere.getThermostatIrregulier();
    }
    
    // Utiliser la même logique que pour les éléments chauffants
    // Sélectionner temporairement le thermostat comme élément sélectionné
    ElementChauffant elementSelectionnePrecedent = elementSelectionne;
    elementSelectionne = thermostat;
    
    // Utiliser la méthode de drag des éléments chauffants
    deplacerElementChauffantDragIrregulier(xPouces, yPouces);
    
    // Restaurer l'élément sélectionné précédent
    elementSelectionne = elementSelectionnePrecedent;
}

    public boolean redimensionnerMeubleSelectionne(double largeur, double longueur) {
        sauvegarderEtat();

        if (largeur <= 0 || longueur <= 0) return false;

        // 1) Meuble SANS drain sélectionné
        if (meubleSelectionne != null) {
            meubleSelectionne.setTaille(largeur, longueur);
            return true;
        }

        // 2) Meuble AVEC drain sélectionné (mais pas le drain lui-même)
        if (meubleAvecDrainSelectionne != null && !drainSelectionne) {
            double ancienneLargeur = meubleAvecDrainSelectionne.getLargeur();
            double ancienneHauteur = meubleAvecDrainSelectionne.getHauteur();
            
            double facteurX = largeur / ancienneLargeur;
            double facteurY = longueur / ancienneHauteur;
            
            // Get current relative drain position
            double ancienXDrainRelatif = meubleAvecDrainSelectionne.getXDrainRelatif();
            double ancienYDrainRelatif = meubleAvecDrainSelectionne.getYDrainRelatif();
            
            // Calculate new relative drain position (proportional scaling)
            double nouveauXDrainRelatif = ancienXDrainRelatif * facteurX;
            double nouveauYDrainRelatif = ancienYDrainRelatif * facteurY;
            
            // Resize the furniture
            meubleAvecDrainSelectionne.setTaille(largeur, longueur);
            
            // Update drain position to maintain relative position
            meubleAvecDrainSelectionne.setXDrainRelatif(nouveauXDrainRelatif);
            meubleAvecDrainSelectionne.setYDrainRelatif(nouveauYDrainRelatif);
            
            return true;
        }

        // 3) Rien de pertinent à redimensionner
        return false;
    }

    public boolean redimensionnerMeubleParId(int id, double largeur, double longueur) {
        sauvegarderEtat();
        for (MeubleSansDrain m : piece.getMeubles()) {
            if (m.getId() == id) {
                if (largeur <= 0 || longueur <= 0) return false;
                m.setTaille(largeur, longueur);
                if (meubleSelectionne != null && meubleSelectionne.getId() == id) meubleSelectionne = m;
                return true;
            }
        }
        return false;
    }

    public PieceDTO getPiece() {
        // Si on a une pièce irrégulière, retourner null (on utilisera getPieceIrreguliere())
        if(estPieceIrreguliere && pieceIrreguliere != null){
            return null;
            // La vue devra utiliser getPieceIrreguliere() à la place
        }
        if(piece == null) return null;
        List<MeubleSansDrainDTO> meublesSansDrainDTO = new ArrayList<>();
        for (MeubleSansDrain m : piece.getMeubles()) {
            meublesSansDrainDTO.add(new MeubleSansDrainDTO(m.getId(), m.getX(), m.getY(), m.getLargeur(), m.getHauteur(), m.getType()));
        }

        List<MeubleDrainDTO> meublesDrainDTO = new ArrayList<>();
        for (MeubleDrain m : piece.getMeublesDrain()) {
            meublesDrainDTO.add(new MeubleDrainDTO(m.getId(), m.getX(), m.getY(), m.getLargeur(), m.getHauteur(),
                                                   m.getType(), m.getXDrainRelatif(), m.getYDrainRelatif(), m.getDiametreDrain()));
        }

        // Convertir les éléments chauffants en DTOs
        List<ElementChauffantDTO> elementsChauffantsDTO = new ArrayList<>();
        for (ElementChauffant e : piece.getElementsChauffants()) {
            elementsChauffantsDTO.add(new ElementChauffantDTO(
                e.getX(),
                e.getY(),
                e.getLargeur(),
                e.getHauteur(),
                e.isHorizontal(),
                e.getAngle()
            ));
        }

     PieceDTO dto = new PieceDTO(
            piece.getX(),
            piece.getY(),
            piece.getLargeur(),
            piece.getLongueur(),
            meublesSansDrainDTO,
            meublesDrainDTO,
            elementsChauffantsDTO
    );

    
    dto.setThermostat(piece.getThermostat());

    return dto;
}

public Meuble trouverMeubleParId(int id) {
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        for (MeubleSansDrain m : pieceIrreguliere.getMeubles()) {
            if (m.getId() == id) return m;
        }
        for (MeubleDrain m : pieceIrreguliere.getMeublesDrain()) {
            if (m.getId() == id) return m;
        }
    } else if (piece != null) {
        for (MeubleSansDrain m : piece.getMeubles()) {
            if (m.getId() == id) return m;
        }
        for (MeubleDrain m : piece.getMeublesDrain()) {
            if (m.getId() == id) return m;
        }
    }
    return null;
}

public MeubleSansDrainDTO getMeubleSelectionne() {
    if (meubleSelectionne == null) return null;
    
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        // Chercher dans la pièce irrégulière
        for (MeubleSansDrain m : pieceIrreguliere.getMeubles()) {
            if (m.getId() == meubleSelectionne.getId()) {
                return new MeubleSansDrainDTO(m.getId(), m.getX(), m.getY(),
                                              m.getLargeur(), m.getHauteur(), m.getType());
            }
        }
    } else if (piece != null) {
        // Chercher dans la pièce rectangulaire
        for (MeubleSansDrain m : piece.getMeubles()) {
            if (m.getId() == meubleSelectionne.getId()) {
                return new MeubleSansDrainDTO(m.getId(), m.getX(), m.getY(),
                                              m.getLargeur(), m.getHauteur(), m.getType());
            }
        }
    }
   return null;
}

    public MeubleDrainDTO getMeubleAvecDrainSelectionne() {
        if (meubleAvecDrainSelectionne == null) return null;
        return new MeubleDrainDTO(
            meubleAvecDrainSelectionne.getId(),
            meubleAvecDrainSelectionne.getX(),
            meubleAvecDrainSelectionne.getY(),
            meubleAvecDrainSelectionne.getLargeur(),
            meubleAvecDrainSelectionne.getHauteur(),
            meubleAvecDrainSelectionne.getType(),
            meubleAvecDrainSelectionne.getXDrainRelatif(),
            meubleAvecDrainSelectionne.getYDrainRelatif(),
            meubleAvecDrainSelectionne.getDiametreDrain()
        );
    }

    public boolean isDrainSelectionne() {
        return drainSelectionne;
    }

    public void deselectionnerMeuble() {
        meubleSelectionne = null;
        meubleAvecDrainSelectionne = null;
        drainSelectionne = false;
    }

    public String deplacerMeubleSelectionne(double x, double y) {
        return deplacerMeubleSelectionne(x, y, true);
    }
    
    public String deplacerMeubleSelectionne(double x, double y, boolean sauvegarder) {
        if (sauvegarder) {
            sauvegarderEtat();
        }

        // 0) CAS : c'est le DRAIN qui est sélectionné
        if (meubleAvecDrainSelectionne != null && drainSelectionne) {
            // x et y sont en pouces, coord. ABSOLUES dans la pièce
            double relX = x - meubleAvecDrainSelectionne.getX();
            double relY = y - meubleAvecDrainSelectionne.getY();

            double rayon = meubleAvecDrainSelectionne.getDiametreDrain() / 2.0;
            double largeurMeuble = meubleAvecDrainSelectionne.getLargeur();
            double hauteurMeuble = meubleAvecDrainSelectionne.getHauteur();
            
            // Calculer le point le plus proche sur le rectangle du meuble
            double closestX = Math.max(0, Math.min(relX, largeurMeuble));
            double closestY = Math.max(0, Math.min(relY, hauteurMeuble));
            
            // Calculer la distance du centre du drain au point le plus proche du rectangle
            double dx = relX - closestX;
            double dy = relY - closestY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            
            // Si la distance est supérieure au rayon, ramener le drain à la limite
            if (distance > rayon) {
                // Normaliser le vecteur et ramener le centre du drain à distance = rayon
                if (distance > 0) {
                    double facteur = rayon / distance;
                    relX = closestX + dx * facteur;
                    relY = closestY + dy * facteur;
                }
            }

            // On met à jour la position RELATIVE du drain (toujours en pouces)
            meubleAvecDrainSelectionne.setPositionDrain(relX, relY);
            return "OK";
        }

        // 1) Meuble SANS drain sélectionné
if (meubleSelectionne != null) {
    // Vérifier selon le type de pièce
    boolean dansPiece;
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        dansPiece = estDansPieceIrreguliere(x, y, meubleSelectionne.getLargeur(), meubleSelectionne.getHauteur());
    } else {
        dansPiece = estDansPiece(x, y, meubleSelectionne.getLargeur(), meubleSelectionne.getHauteur());
    }
    
    if (!dansPiece) {
        return "ERREUR: Le meuble ne peut pas être placé en dehors de la pièce.";
    }

            if (verifierCollision(x, y, meubleSelectionne.getLargeur(), meubleSelectionne.getHauteur(),
                                  meubleSelectionne.getId())) {
                double[] posValide = trouverPositionValide(
                        x, y,
                        meubleSelectionne.getLargeur(), meubleSelectionne.getHauteur(),
                        meubleSelectionne.getId()
                );
                if (!estDansPiece(posValide[0], posValide[1], meubleSelectionne.getLargeur(), meubleSelectionne.getHauteur())) {
                    return "ERREUR: Collision détectée et aucune autre position valide disponible.";
                }
                meubleSelectionne.setPosition(posValide[0], posValide[1]);
                return "ERREUR: Impossible de placer le meuble : un autre meuble occupe déjà cette position.";

            }

            meubleSelectionne.setPosition(x, y);
            return "OK";
        }

        // 2) Meuble AVEC drain sélectionné (mais PAS le drain)
if (meubleAvecDrainSelectionne != null && !drainSelectionne) {
    // Vérifier selon le type de pièce
    boolean dansPiece;
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        dansPiece = estDansPieceIrreguliere(x, y, meubleAvecDrainSelectionne.getLargeur(), meubleAvecDrainSelectionne.getHauteur());
    } else {
        dansPiece = estDansPiece(x, y, meubleAvecDrainSelectionne.getLargeur(), meubleAvecDrainSelectionne.getHauteur());
    }
    
    if (!dansPiece) {
        return "ERREUR: Le meuble ne peut pas être placé en dehors de la pièce.";
    }

            if (verifierCollision(x, y, meubleAvecDrainSelectionne.getLargeur(), meubleAvecDrainSelectionne.getHauteur(),
                                  meubleAvecDrainSelectionne.getId())) {
                double[] posValide = trouverPositionValide(
                        x, y,
                        meubleAvecDrainSelectionne.getLargeur(), meubleAvecDrainSelectionne.getHauteur(),
                        meubleAvecDrainSelectionne.getId()
                );
                if (!estDansPiece(posValide[0], posValide[1], meubleAvecDrainSelectionne.getLargeur(), meubleAvecDrainSelectionne.getHauteur())) {
                    return "ERREUR: Collision détectée et aucune autre position valide disponible.";
                }
                meubleAvecDrainSelectionne.setPosition(posValide[0], posValide[1]);
                return "ERREUR: Impossible de placer le meuble : un autre meuble occupe déjà cette position.";

            }

            meubleAvecDrainSelectionne.setPosition(x, y);
            return "OK";
        }

        // 3) Rien de pertinent à déplacer
        return "ERREUR: Aucun meuble sélectionné";
    }

private boolean verifierCollision(double x, double y, double largeur, double hauteur, int idIgnore) {
    double x1 = x;
    double y1 = y;
    double x2 = x + largeur;
    double y2 = y + hauteur;

    // Déterminer quelle pièce vérifier
    List<MeubleSansDrain> meublesSansDrain;
    List<MeubleDrain> meublesDrain;
    
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        meublesSansDrain = pieceIrreguliere.getMeubles();
        meublesDrain = pieceIrreguliere.getMeublesDrain();
    } else {
        meublesSansDrain = piece.getMeubles();
        meublesDrain = piece.getMeublesDrain();
    }

    // 1) Vérifier les meubles SANS drain
    for (MeubleSansDrain meuble : meublesSansDrain) {
        if (meuble.getId() == idIgnore) continue;

        double mx1 = meuble.getX();
        double my1 = meuble.getY();
        double mx2 = meuble.getX() + meuble.getLargeur();
        double my2 = meuble.getY() + meuble.getHauteur();

        if (x1 < mx2 && x2 > mx1 && y1 < my2 && y2 > my1) {
            return true;
        }
    }

    // 2) Vérifier les meubles AVEC drain
    for (MeubleDrain meuble : meublesDrain) {
        if (meuble.getId() == idIgnore) continue;

        double mx1 = meuble.getX();
        double my1 = meuble.getY();
        double mx2 = meuble.getX() + meuble.getLargeur();
        double my2 = meuble.getY() + meuble.getHauteur();

        if (x1 < mx2 && x2 > mx1 && y1 < my2 && y2 > my1) {
            return true;
        }
    }

    return false;
}
    private boolean estDansPiece(double x, double y, double largeur, double hauteur) {
        return x >= piece.getX() && y >= piece.getY() &&
               x + largeur <= piece.getX() + piece.getLargeur() &&
               y + hauteur <= piece.getY() + piece.getLongueur();
    }

private double[] trouverPositionValide(double x, double y, double largeur, double hauteur) {
    return trouverPositionValide(x, y, largeur, hauteur, -1);
}

private double[] trouverPositionValide(double x, double y, double largeur, double hauteur, int idIgnore) {
    // Si pas de collision, on garde la position de départ
    if (!verifierCollision(x, y, largeur, hauteur, idIgnore) && 
        estDansPiece(x, y, largeur, hauteur, idIgnore)) {  // Utiliser la méthode adaptée
        return new double[]{x, y};
    }

    // On va balayer la pièce en « grille »
    double stepX = Math.max(largeur, 6.0);
    double stepY = Math.max(hauteur, 6.0);

    // Déterminer les limites selon le type de pièce
    double debutX, debutY, finX, finY;
    
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        // Pour pièce irrégulière
        debutX = pieceIrreguliere.getMinX();
        debutY = pieceIrreguliere.getMinY();
        finX = pieceIrreguliere.getMaxX() - largeur;
        finY = pieceIrreguliere.getMaxY() - hauteur;
    } else if (piece != null) {
        // Pour pièce rectangulaire
        debutX = piece.getX();
        debutY = piece.getY();
        finX = piece.getX() + piece.getLargeur() - largeur;
        finY = piece.getY() + piece.getLongueur() - hauteur;
    } else {
        // Pas de pièce
        return new double[]{x, y};
    }

    for (double yy = debutY; yy <= finY; yy += stepY) {
        for (double xx = debutX; xx <= finX; xx += stepX) {
            if (!verifierCollision(xx, yy, largeur, hauteur, idIgnore) && 
                estDansPiece(xx, yy, largeur, hauteur, idIgnore)) {
                // trouvé une case libre
                return new double[]{xx, yy};
            }
        }
    }

    // Si vraiment aucun endroit libre trouvé, on retourne la position demandée (cas extrême)
    return new double[]{x, y};
}

// Ajoutez cette méthode pour vérifier si dans la pièce (gère les deux types)
private boolean estDansPiece(double x, double y, double largeur, double hauteur, int idIgnore) {
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        return estDansPieceIrreguliere(x, y, largeur, hauteur);
    } else if (piece != null) {
        return estDansPiece(x, y, largeur, hauteur);
    }
    return false;
}
    // ---- Éléments chauffants ----

public void selectionnerElementChauffant(double x, double y) {
    elementSelectionne = null;
    double tolerance = 5.0;
    
    // Vérifier selon le type de pièce
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        // Pièce irrégulière : vérifier d'abord le thermostat (singleton, priorité)
        Thermostat thermostat = pieceIrreguliere.getThermostatIrregulier();
        if (thermostat != null) {
            if (Math.abs(thermostat.getAngle()) > 0.001) {
                // Thermostat avec rotation : vérifier si le point est dans le rectangle tourné
                if (estPointDansElementTourne(x, y, thermostat, tolerance)) {
                    elementSelectionne = thermostat;
                    return; // Thermostat sélectionné, on s'arrête
                }
            } else {
                // Thermostat sans rotation : vérification normale
                if (x >= thermostat.getX() && x <= thermostat.getX() + thermostat.getLargeur() &&
                    y >= thermostat.getY() && y <= thermostat.getY() + thermostat.getHauteur()) {
                    elementSelectionne = thermostat;
                    return; // Thermostat sélectionné, on s'arrête
                }
            }
        }
        
        // Ensuite, vérifier les autres éléments chauffants
        for (ElementChauffant element : pieceIrreguliere.getElementsChauffants()) {
            if (Math.abs(element.getAngle()) > 0.001) {
                // Élément avec rotation : vérifier si le point est dans le rectangle tourné
                if (estPointDansElementTourne(x, y, element, tolerance)) {
                    elementSelectionne = element;
                    break;
                }
            } else {
                // Élément sans rotation : vérification normale
                if (element.isHorizontal()) {
                    if (x >= element.getX() && x <= element.getX() + element.getLargeur() &&
                        Math.abs(y - element.getY()) <= tolerance) { 
                        elementSelectionne = element; 
                        break; 
                    }
                } else {
                    if (y >= element.getY() && y <= element.getY() + element.getLargeur() &&
                        Math.abs(x - element.getX()) <= tolerance) { 
                        elementSelectionne = element; 
                        break; 
                    }
                }
            }
        }
    } else if (piece != null) {
        // Pièce rectangulaire
        for (ElementChauffant element : piece.getElementsChauffants()) {
            if (element.isHorizontal()) {
                if (x >= element.getX() && x <= element.getX() + element.getLargeur() &&
                    Math.abs(y - element.getY()) <= tolerance) { 
                    elementSelectionne = element; 
                    break; 
                }
            } else {
                if (y >= element.getY() && y <= element.getY() + element.getLargeur() &&
                    Math.abs(x - element.getX()) <= tolerance) { 
                    elementSelectionne = element; 
                    break; 
                }
            }
        }
    }
}

/**
 * Vérifie si un point est dans un élément chauffant tourné
 */
private boolean estPointDansElementTourne(double x, double y, ElementChauffant element, double tolerance) {
    // Calculer le centre de l'élément de manière cohérente avec le dessin
    // Le coin supérieur gauche est (element.getX(), element.getY())
    // Le centre dans le repère local est (largeur/2, hauteur/2)
    // On applique la rotation pour obtenir le centre dans le repère global
    double cosAngle = Math.cos(element.getAngle());
    double sinAngle = Math.sin(element.getAngle());
    
    // Vecteur du coin supérieur gauche vers le centre dans le repère local
    double dxLocal = element.getLargeur() / 2.0;
    double dyLocal = element.getHauteur() / 2.0;
    
    // Appliquer la rotation pour obtenir le vecteur dans le repère global
    double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
    double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
    
    // Centre réel dans le repère global
    double centreX = element.getX() + dxGlobal;
    double centreY = element.getY() + dyGlobal;
    
    // Appliquer la rotation inverse pour ramener le point dans le repère local de l'élément
    double cosAngleInv = Math.cos(-element.getAngle());
    double sinAngleInv = Math.sin(-element.getAngle());
    
    // Vecteur du centre vers le point
    double dx = x - centreX;
    double dy = y - centreY;
    
    // Appliquer la rotation inverse
    double dxLocalPoint = dx * cosAngleInv - dy * sinAngleInv;
    double dyLocalPoint = dx * sinAngleInv + dy * cosAngleInv;
    
    // Vérifier si le point est dans le rectangle local (avec tolérance)
    double demiLargeur = element.getLargeur() / 2.0 + tolerance;
    double demiHauteur = element.getHauteur() / 2.0 + tolerance;
    
    return Math.abs(dxLocalPoint) <= demiLargeur && Math.abs(dyLocalPoint) <= demiHauteur;
}

    public void modifierElementChauffant(double largeur, double hauteur) {
        if (elementSelectionne == null) return;
        
        // Pour les pièces irrégulières, utiliser la logique spéciale
        if (estPieceIrreguliere && pieceIrreguliere != null) {
            modifierElementChauffantIrregulier(largeur, hauteur);
        } else {
            // Pour les pièces régulières, logique existante
            sauvegarderEtat();
            elementSelectionne.setLargeur(largeur);
            elementSelectionne.setHauteur(hauteur);
        }
    }
    
    /**
     * Modifie les dimensions d'un élément chauffant dans une pièce irrégulière
     * en le recréant avec les nouvelles dimensions tout en gardant sa position sur le mur
     */
    private void modifierElementChauffantIrregulier(double nouvelleLargeur, double nouvelleHauteur) {
        if (pieceIrreguliere == null || !pieceIrreguliere.estFermee() || elementSelectionne == null) {
            return;
        }
        
        // Sauvegarder l'état avant la modification
        sauvegarderEtat();
        
        // 1. Stocker les informations de l'élément actuel
        ElementChauffant elementActuel = elementSelectionne;
        double ancienX = elementActuel.getX();
        double ancienY = elementActuel.getY();
        double ancienAngle = elementActuel.getAngle();
        double ancienneLargeur = elementActuel.getLargeur();
        double ancienneHauteur = elementActuel.getHauteur();
        
        // 2. Calculer le centre de l'élément actuel
        // Le centre dans le repère local est (largeur/2, hauteur/2)
        // On applique la rotation pour obtenir le vecteur du coin supérieur gauche au centre
        double cosAngle = Math.cos(ancienAngle);
        double sinAngle = Math.sin(ancienAngle);
        
        double dxLocal = ancienneLargeur / 2.0;
        double dyLocal = ancienneHauteur / 2.0;
        
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        double centreX = ancienX + dxGlobal;
        double centreY = ancienY + dyGlobal;
        
        // 3. Trouver le mur le plus proche du centre de l'élément
        int indexMur = trouverMurLePlusProche(centreX, centreY);
        if (indexMur < 0) {
            // Si on ne trouve pas de mur, utiliser la méthode simple
            elementSelectionne.setLargeur(nouvelleLargeur);
            elementSelectionne.setHauteur(nouvelleHauteur);
            return;
        }
        
        // 4. Calculer le point sur le mur le plus proche du centre
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        int n = points.size();
        
        Point2D.Double p1 = points.get(indexMur);
        // Utiliser modulo pour gérer le dernier mur (qui ferme le polygone)
        Point2D.Double p2 = points.get((indexMur + 1) % n);
        
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double longueurMur = Math.sqrt(dx * dx + dy * dy);
        
        if (longueurMur < 0.001) {
            // Segment dégénéré, utiliser la méthode simple
            elementSelectionne.setLargeur(nouvelleLargeur);
            elementSelectionne.setHauteur(nouvelleHauteur);
            return;
        }
        
        // Calculer la projection du centre sur le segment
        double t = ((centreX - p1.x) * dx + (centreY - p1.y) * dy) / (longueurMur * longueurMur);
        t = Math.max(0.0, Math.min(1.0, t));
        
        double murX = p1.x + t * dx;
        double murY = p1.y + t * dy;
        
        // 5. Calculer l'angle du mur
        double angleMur = Math.atan2(dy, dx);
        
        // 6. Calculer le centre de la pièce pour déterminer la direction vers l'intérieur
        double centrePieceX = 0, centrePieceY = 0;
        for (Point2D.Double p : points) {
            centrePieceX += p.x;
            centrePieceY += p.y;
        }
        centrePieceX /= points.size();
        centrePieceY /= points.size();
        
        // 7. Calculer le vecteur normal au mur (vers l'intérieur)
        double normX = -dy / longueurMur;
        double normY = dx / longueurMur;
        
        double dot = normX * (centrePieceX - murX) + normY * (centrePieceY - murY);
        if (dot < 0) {
            normX = -normX;
            normY = -normY;
        }
        
        // 8. Calculer le nouveau centre de l'élément : sur le mur, décalé perpendiculairement vers l'intérieur
        double nouveauCentreX = murX + normX * (nouvelleHauteur / 2.0);
        double nouveauCentreY = murY + normY * (nouvelleHauteur / 2.0);
        
        // 9. Calculer le nouveau coin supérieur gauche
        double cosAngleNouveau = Math.cos(angleMur);
        double sinAngleNouveau = Math.sin(angleMur);
        
        double dxLocalNouveau = -nouvelleLargeur / 2.0;
        double dyLocalNouveau = -nouvelleHauteur / 2.0;
        
        double dxGlobalNouveau = dxLocalNouveau * cosAngleNouveau - dyLocalNouveau * sinAngleNouveau;
        double dyGlobalNouveau = dxLocalNouveau * sinAngleNouveau + dyLocalNouveau * cosAngleNouveau;
        
        double nouveauX = nouveauCentreX + dxGlobalNouveau;
        double nouveauY = nouveauCentreY + dyGlobalNouveau;
        
        // 10. Vérifier les collisions avec d'autres éléments chauffants (sauf celui qu'on modifie)
        boolean horizontal = Math.abs(dy) < Math.abs(dx);
        if (verifierCollisionElementChauffantIrregulierDeplacement(nouveauX, nouveauY, nouvelleLargeur, horizontal, elementActuel)) {
            // Collision détectée : restaurer les dimensions précédentes
            elementSelectionne.setLargeur(ancienneLargeur);
            elementSelectionne.setHauteur(ancienneHauteur);
            return;
        }
        
        // 11. Supprimer l'ancien élément de la liste
        pieceIrreguliere.getElementsChauffants().remove(elementActuel);
        
        // 12. Créer le nouvel élément avec les nouvelles dimensions
        ElementChauffant nouvelElement = new ElementChauffant(nouveauX, nouveauY, nouvelleLargeur, nouvelleHauteur, horizontal, angleMur);
        pieceIrreguliere.getElementsChauffants().add(nouvelElement);
        
        // 13. Mettre à jour la sélection
        elementSelectionne = nouvelElement;
    }

    public void deselectionnerElementChauffant() {
        elementSelectionne = null;
    }

public List<ElementChauffant> getElementsChauffants() {
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        return pieceIrreguliere.getElementsChauffants();
    } else if (piece != null) {
        return piece.getElementsChauffants();
    }
    return new ArrayList<>();
}

    public ElementChauffant getElementSelectionne() {
        return elementSelectionne;
    }
    
    public List<Fil> getFils() {
        return fils;
    }


    public boolean supprimerElementChauffant() {
        sauvegarderEtat();
        if (elementSelectionne == null) return false;
        
        boolean ok = false;
        if (estPieceIrreguliere && pieceIrreguliere != null) {
            ok = pieceIrreguliere.getElementsChauffants().remove(elementSelectionne);
        } else if (piece != null) {
            ok = piece.getElementsChauffants().remove(elementSelectionne);
        }
        
        elementSelectionne = null;
        return ok;
    }
public String deplacerElementChauffant(double x, double y) {
    return deplacerElementChauffant(x, y, true);
}

public String deplacerElementChauffant(double x, double y, boolean sauvegarder) {
    if (sauvegarder) {
        sauvegarderEtat();
    }
    if (elementSelectionne == null) return "ERREUR: Aucun élément sélectionné";

    boolean horizontal = elementSelectionne.isHorizontal();
    double nouvelleX = x;
    double nouvelleY = y;

    if (horizontal) {
        double distanceNord = Math.abs(y - (piece.getY() + piece.getLongueur() - elementSelectionne.getHauteur()));
        double distanceSud = Math.abs(y - piece.getY());
        
        if (distanceNord <= distanceSud) {
         
            nouvelleY = piece.getY() + piece.getLongueur() - elementSelectionne.getHauteur();
        } else {
           
            nouvelleY = piece.getY();
        }
        
        // Ajuster X
        if (nouvelleX < piece.getX()) nouvelleX = piece.getX();
        if (nouvelleX + elementSelectionne.getLargeur() > piece.getX() + piece.getLargeur()) {
            nouvelleX = piece.getX() + piece.getLargeur() - elementSelectionne.getLargeur();
        }
    } else {
        double distanceEst = Math.abs(x - (piece.getX() + piece.getLargeur() - elementSelectionne.getHauteur()));
        double distanceOuest = Math.abs(x - piece.getX());
        
        if (distanceEst <= distanceOuest) {
            nouvelleX = piece.getX() + piece.getLargeur() - elementSelectionne.getHauteur();
        } else {
            nouvelleX = piece.getX();
        }
        if (nouvelleY < piece.getY()) nouvelleY = piece.getY();
        if (nouvelleY + elementSelectionne.getLargeur() > piece.getY() + piece.getLongueur()) {
            nouvelleY = piece.getY() + piece.getLongueur() - elementSelectionne.getLargeur();
        }
    }

    // Vérifier les collisions
    if (verifierCollisionElementChauffantAvecAutres(nouvelleX, nouvelleY, elementSelectionne.getLargeur(), horizontal)) {
        return "ERREUR: Collision avec un autre élément chauffant";
    }
    
    elementSelectionne.setX(nouvelleX);
    elementSelectionne.setY(nouvelleY);
    
    return "OK";
}
    private boolean verifierCollisionElementChauffantAvecAutres(
            double x, double y, double taille, boolean horizontal) {

        for (ElementChauffant element : piece.getElementsChauffants()) {

            // Skip the currently selected element
            if (element == elementSelectionne) continue;
            
            double ex = element.getX();
            double ey = element.getY();
            double et = element.getLargeur();

            if (horizontal && element.isHorizontal()) {
                // Both horizontal, check if on same Y and overlapping X
                if (Math.abs(y - ey) < 2.0 && x < ex + et && x + taille > ex) return true;
            }
            else if (!horizontal && !element.isHorizontal()) {
                // Both vertical, check if on same X and overlapping Y
                if (Math.abs(x - ex) < 2.0 && y < ey + et && y + taille > ey) return true;
            }
        }
        return false;
    }

    public boolean peutAjouterElementChauffant(ElementChauffant nouveau) {
        sauvegarderEtat();
        for (ElementChauffant element : piece.getElementsChauffants()) {
            if (intersectent(element, nouveau)) {
                return false; // collision détectée
            }
        }
        return true;
    }

    // Vérifie si deux éléments chauffants se chevauchent
    private boolean intersectent(ElementChauffant a, ElementChauffant b) {
        double ax1 = a.getX();
        double ay1 = a.getY();
        double ax2 = a.isHorizontal() ? a.getX() + a.getLargeur() : a.getX() + a.getHauteur();
        double ay2 = a.isHorizontal() ? a.getY() + a.getHauteur() : a.getY() + a.getLargeur();

        double bx1 = b.getX();
        double by1 = b.getY();
        double bx2 = b.isHorizontal() ? b.getX() + b.getLargeur() : b.getX() + b.getHauteur();
        double by2 = b.isHorizontal() ? b.getY() + b.getHauteur() : b.getY() + b.getLargeur();

        return !(ax2 <= bx1 || ax1 >= bx2 || ay2 <= by1 || ay1 >= by2);
    }

    // Calcule les coordonnées et l'orientation d'un élément chauffant selon le mur choisi

 public boolean ajouterElementChauffant(double largeur, double hauteur, String mur) {
    sauvegarderEtat();

    // Vérifier si c'est une pièce irrégulière
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        // Pour les pièces irrégulières, on active le mode sélection de mur
        modeAjoutElementChauffant = true;
        largeurElementChauffantEnAttente = largeur;
        hauteurElementChauffantEnAttente = hauteur;
        return true; // Le mode est activé, l'ajout se fera au clic sur le mur
    }

    // Pour les pièces régulières, logique existante
    // 1) Calcul de la position initiale selon le mur
    double[] pos = calculerCoordonneesSelonMur(mur, largeur, hauteur);
    double x = pos[0];
    double y = pos[1];
    boolean horizontal = pos[2] == 1;

    // 2) Vérifier si collision
    if (verifierCollisionElementChauffant(x, y, largeur, horizontal)) {
        // 3) Si collision → trouver position libre
        double[] libre = trouverPositionLibre(mur, largeur, hauteur);
        if (libre == null) return false;  // mur plein

        x = libre[0];
        y = libre[1];
        horizontal = libre[2] == 1;
    }

    // 4) Ajouter l'élément
    ElementChauffant e = new ElementChauffant(x, y, largeur, hauteur, horizontal);
    piece.getElementsChauffants().add(e);
    elementSelectionne = e;
    return true;
}

private double[] calculerCoordonneesSelonMur(String mur, double largeur, double hauteur) {
    double x = 0, y = 0;
    boolean horizontal = true;

    switch (mur) {
        case "Nord":
            horizontal = true;
            y = piece.getY() + piece.getLongueur() - hauteur; // Vers l'intérieur (bas)
            x = piece.getX() + (piece.getLargeur() - largeur) / 2;
            break;

        case "Sud":
            horizontal = true;
            y = piece.getY(); // Vers l'intérieur (haut)
            x = piece.getX() + (piece.getLargeur() - largeur) / 2;
            break;

        case "Ouest":
            horizontal = false;
            x = piece.getX(); // Vers l'intérieur (droite)
            y = piece.getY() + (piece.getLongueur() - largeur) / 2;
            break;

        case "Est":
            horizontal = false;
            x = piece.getX() + piece.getLargeur() - hauteur; // Vers l'intérieur (gauche)
            y = piece.getY() + (piece.getLongueur() - largeur) / 2;
            break;
    }

    return new double[]{ x, y, horizontal ? 1 : 0 };
}

private boolean verifierCollisionElementChauffant(
            double x, double y, double taille, boolean horizontal) {

        for (ElementChauffant element : piece.getElementsChauffants()) {
            double ex = element.getX();
            double ey = element.getY();
            double et = element.getLargeur();

            if (horizontal && element.isHorizontal()) {
                if (y == ey && x < ex + et && x + taille > ex) return true;
            }
            else if (!horizontal && !element.isHorizontal()) {
                if (x == ex && y < ey + et && y + taille > ey) return true;
            }
        }
        return false;
    }

private double[] trouverPositionLibre(String mur, double largeur, double hauteur) {
    boolean horizontal = mur.equals("Nord") || mur.equals("Sud");
    double xx = 0, yy = 0;

    switch (mur) {
        case "Nord":
            yy = piece.getY() + piece.getLongueur() - hauteur;
            for (double offset = 0; offset + largeur <= piece.getLargeur(); offset += 1) {
                xx = piece.getX() + offset;
                if (!verifierCollisionElementChauffant(xx, yy, largeur, true)) {
                    return new double[]{ xx, yy, 1 };
                }
            }
            break;

        case "Sud":
            yy = piece.getY();
            for (double offset = 0; offset + largeur <= piece.getLargeur(); offset += 1) {
                xx = piece.getX() + offset;
                if (!verifierCollisionElementChauffant(xx, yy, largeur, true)) {
                    return new double[]{ xx, yy, 1 };
                }
            }
            break;

        case "Ouest":
            xx = piece.getX();
            for (double offset = 0; offset + largeur <= piece.getLongueur(); offset += 1) {
                yy = piece.getY() + offset;
                if (!verifierCollisionElementChauffant(xx, yy, largeur, false)) {
                    return new double[]{ xx, yy, 0 };
                }
            }
            break;

        case "Est":
            xx = piece.getX() + piece.getLargeur() - hauteur;
            for (double offset = 0; offset + largeur <= piece.getLongueur(); offset += 1) {
                yy = piece.getY() + offset;
                if (!verifierCollisionElementChauffant(xx, yy, largeur, false)) {
                    return new double[]{ xx, yy, 0 };
                }
            }
            break;
    }
    return null; // mur plein
}
public void selectionnerMeubleUnifie(double x, double y) {
    // Réinitialiser toutes les sélections
    meubleSelectionne = null;
    meubleAvecDrainSelectionne = null;
    elementSelectionne = null;
    drainSelectionne = false;
    pointSelectionneIndex = -1;

    // Si on a une pièce irrégulière
    if (estPieceIrreguliere && pieceIrreguliere != null) {
        selectionnerMeubleDansPieceIrreguliere(x, y);
    } 
    // Si on a une pièce rectangulaire
    else if (piece != null) {
        selectionnerMeubleDansPieceRectangulaire(x, y);
    }
}

private void selectionnerMeubleDansPieceRectangulaire(double x, double y) {
    double tolerance = 5.0; // tolérance en pouces pour cliquer sur le drain

    // 1) D'abord vérifier si on clique sur un DRAIN dans pièce rectangulaire
    for (MeubleDrain m : piece.getMeublesDrain()) {
        double xDrainAbsolu = m.getXDrainAbsolu();
        double yDrainAbsolu = m.getYDrainAbsolu();
        double rayonDrain = m.getDiametreDrain() / 2.0;

        double distance = Math.sqrt(Math.pow(x - xDrainAbsolu, 2) + Math.pow(y - yDrainAbsolu, 2));

        if (distance <= rayonDrain + tolerance) {
            meubleAvecDrainSelectionne = m;
            drainSelectionne = true;
            return;
        }
    }

    // 2) Vérifier si on clique sur un meuble AVEC drain (rectangulaire)
    for (MeubleDrain m : piece.getMeublesDrain()) {
        if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
            y >= m.getY() && y <= m.getY() + m.getHauteur()) {
            meubleAvecDrainSelectionne = m;
            drainSelectionne = false;
            return;
        }
    }

    // 3) Vérifier si on clique sur un meuble SANS drain (rectangulaire)
    for (MeubleSansDrain m : piece.getMeubles()) {
        if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
            y >= m.getY() && y <= m.getY() + m.getHauteur()) {
            meubleSelectionne = m;
            return;
        }
    }

    // 4) Vérifier si on clique sur un élément chauffant (rectangulaire)
    selectionnerElementChauffant(x, y);
}

private void selectionnerMeubleDansPieceIrreguliere(double x, double y) {
    double tolerance = 5.0; // tolérance en pouces pour cliquer sur le drain

    // 1) D'abord vérifier si on clique sur un DRAIN dans pièce irrégulière
    for (MeubleDrain m : pieceIrreguliere.getMeublesDrain()) {
        double xDrainAbsolu = m.getXDrainAbsolu();
        double yDrainAbsolu = m.getYDrainAbsolu();
        double rayonDrain = m.getDiametreDrain() / 2.0;

        double distance = Math.sqrt(Math.pow(x - xDrainAbsolu, 2) + Math.pow(y - yDrainAbsolu, 2));

        if (distance <= rayonDrain + tolerance) {
            meubleAvecDrainSelectionne = m;
            drainSelectionne = true;
            return;
        }
    }

    // 2) Vérifier si on clique sur un meuble AVEC drain (irrégulière)
    for (MeubleDrain m : pieceIrreguliere.getMeublesDrain()) {
        if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
            y >= m.getY() && y <= m.getY() + m.getHauteur()) {
            meubleAvecDrainSelectionne = m;
            drainSelectionne = false;
            return;
        }
    }

    // 3) Vérifier si on clique sur un meuble SANS drain (irrégulière)
    for (MeubleSansDrain m : pieceIrreguliere.getMeubles()) {
        if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
            y >= m.getY() && y <= m.getY() + m.getHauteur()) {
            meubleSelectionne = m;
            return;
        }
    }

    // 4) Vérifier si on clique sur un élément chauffant (irrégulière)
    double toleranceElement = 5.0;
    for (ElementChauffant element : pieceIrreguliere.getElementsChauffants()) {
        if (element.isHorizontal()) {
            if (x >= element.getX() && x <= element.getX() + element.getLargeur() &&
                Math.abs(y - element.getY()) <= toleranceElement) {
                elementSelectionne = element;
                break;
            }
        } else {
            if (y >= element.getY() && y <= element.getY() + element.getLargeur() &&
                Math.abs(x - element.getX()) <= toleranceElement) {
                elementSelectionne = element;
                break;
            }
        }
    }
}

// Méthodes pour garder la compatibilité (optionnelles)
public void selectionnerMeuble(double x, double y) {
    selectionnerMeubleUnifie(x, y);
}

public void selectionnerMeubleAvecDrain(double x, double y) {
    selectionnerMeubleUnifie(x, y);
}  
    
public List<Intersection> filtrerIntersectionsSelonObstacles(List<Intersection> intersections) {
    List<Intersection> intersectionsValides = new ArrayList<>();

    for (Intersection i : intersections) {
        if (validerPoint(i.getX(), i.getY())) {
            intersectionsValides.add(i);
        }
    }

    return intersectionsValides;
}

public List<String> getErreursValidation() {
    return new ArrayList<>(erreurs);
}

public void clearErreursValidation() {
    erreurs.clear();
}

private double longueurFilMax = 10000.0 ; // par defaut
private double distanceGrillePouces = 6.0; // valeur par défaut (6 pouces)
private double distanceMinEntreFils = 3.0; // valeur par défaut (3 pouces)
private double translationX = 0.0;
private double translationY = 0.0;

public double getLongueurFilMax() {
        return longueurFilMax;
    }

public void setLongueurFilMax(double longueurPouces) {
        // Sauvegarder l'état seulement si la valeur change
        if (Math.abs(this.longueurFilMax - longueurPouces) > 0.01) {
            sauvegarderEtat();
        }
        this.longueurFilMax = longueurPouces;
    }
    
    public double getDistanceGrillePouces() {
        return distanceGrillePouces;
    }
    
    public void setDistanceGrillePouces(double distance) {
        this.distanceGrillePouces = distance;
    }
    
    public double getDistanceMinEntreFils() {
        return distanceMinEntreFils;
    }
    
    public void setDistanceMinEntreFils(double distance) {
        this.distanceMinEntreFils = distance;
    }
    
    public double getTranslationX() {
        return translationX;
    }
    
    public void setTranslationX(double translationX) {
        this.translationX = translationX;
    }
    
    public double getTranslationY() {
        return translationY;
    }
    
    public void setTranslationY(double translationY) {
        this.translationY = translationY;
    }

public void setCheminFil(List<Point2D.Double> chemin) {
    this.fils.clear();
    this.erreurs.clear();

    for (int i = 0; i < chemin.size() - 1; i++) {
        Point2D.Double p1 = chemin.get(i);
        Point2D.Double p2 = chemin.get(i + 1);

        // Validate this segment before creating it
        if (!validerSegment(p1, p2)) {
            this.erreurs.add("Le fil traverse un obstacle entre (" + 
                Math.round(p1.getX()) + "," + Math.round(p1.getY()) + ") et (" + 
                Math.round(p2.getX()) + "," + Math.round(p2.getY()) + ")");
            continue; // Skip this invalid segment
        }

        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double longueur = Math.sqrt(dx * dx + dy * dy);
        
        // Check maximum segment length (10 feet = 120 inches)
        if (longueur > 120) {
            this.erreurs.add("Segment trop long: " + Math.round(longueur) + 
                " pouces (maximum 120 pouces)");
            continue;
        }
        
        double epaisseur = 0.5; 
        boolean estVertical = Math.abs(dx) < 0.01;

        double x = p1.getX();
        double y = p1.getY();

        Fil fil = new Fil(x, y, estVertical ? epaisseur : longueur, estVertical ? longueur : epaisseur);
        this.fils.add(fil);
    }
}

private boolean validerSegment(Point2D.Double p1, Point2D.Double p2) {
    // Check multiple points along the segment
    int numChecks = (int)Math.ceil(Point2D.distance(p1.x, p1.y, p2.x, p2.y) / 2.0);
    numChecks = Math.max(5, numChecks); // At least 5 checks per segment
    
    for (int i = 0; i <= numChecks; i++) {
        double t = (double)i / numChecks;
        double x = p1.x + t * (p2.x - p1.x);
        double y = p1.y + t * (p2.y - p1.y);
        
        if (!validerPoint(x, y)) {
            return false;
        }
    }
    return true;
}

private boolean validerPoint(double x, double y) {
    Piece pieceActive = getPieceActive();
    if (pieceActive == null) return false;
    
    // Pour les pièces irrégulières, utiliser une logique différente
    if (estPieceIrreguliere && pieceIrreguliere != null && pieceIrreguliere.estFermee()) {
        // 1. Vérifier si le point est à l'intérieur du polygone
        if (!pieceIrreguliere.contientPoint(x, y)) {
            return false;
        }
        
        // 2. Vérifier la distance aux murs (segments du polygone) - au moins 3 pouces
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        if (points.size() < 2) return false;
        
        double distanceMinAuMur = Double.MAX_VALUE;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get((i + 1) % n);
            double distance = distancePointSegment(x, y, p1.x, p1.y, p2.x, p2.y);
            if (distance < distanceMinAuMur) {
                distanceMinAuMur = distance;
            }
        }
        if (distanceMinAuMur < 3.0) {
            return false;
        }
    } else {
        // Pour les pièces régulières, utiliser la logique originale
        // Check distance from walls (3 inches minimum)
        if (x < pieceActive.getX() + 3 || 
            x > pieceActive.getX() + pieceActive.getLargeur() - 3 ||
            y < pieceActive.getY() + 3 || 
            y > pieceActive.getY() + pieceActive.getLongueur() - 3) {
            return false;
        }
    }

    // Check distance from furniture without drains (3 inches minimum)
    for (MeubleSansDrain meuble : pieceActive.getMeubles()) {
        if (distancePouces(x, y, meuble.getX(), meuble.getY(),
                          meuble.getLargeur(), meuble.getHauteur()) < 3) {
            return false;
        }
    }

    // Check distance from heating elements (8 inches minimum)
    for (ElementChauffant e : pieceActive.getElementsChauffants()) {
        if (distancePouces(x, y, e.getX(), e.getY(),
                          e.getLargeur(), e.getHauteur()) < 8) {
            return false;
        }
    }

    // Check distance from furniture with drains
    for (MeubleDrain meubleDrain : pieceActive.getMeublesDrain()) {
        // Distance from furniture body (3 inches)
        if (distancePouces(x, y, meubleDrain.getX(), meubleDrain.getY(),
                          meubleDrain.getLargeur(), meubleDrain.getHauteur()) < 3) {
            return false;
        }
        
        // Distance from drain itself (6 or 10 inches depending on type)
        double xDrain = meubleDrain.getX() + meubleDrain.getXDrainRelatif();
        double yDrain = meubleDrain.getY() + meubleDrain.getYDrainRelatif();
        double diam = meubleDrain.getDiametreDrain();
        double distanceMin = meubleDrain.getType() == TypeMeubleDrain.TOILETTE ? 10 : 6;

        if (distance2D(x, y, xDrain, yDrain) < distanceMin + diam/2) {
            return false;
        }
    }
    
    return true;
}

// Calculate minimum distance between a point and a rectangle
private double distancePouces(double px, double py, double rx, double ry, double largeur, double hauteur) {
    double dx = Math.max(Math.max(rx - px, 0), px - (rx + largeur));
    double dy = Math.max(Math.max(ry - py, 0), py - (ry + hauteur));
    return Math.sqrt(dx*dx + dy*dy);
}


// Classic 2D distance
private double distance2D(double x1, double y1, double x2, double y2) {
    return Math.hypot(x1 - x2, y1 - y2);
}

/**
 * Calcule la distance d'un point à un segment
 */
    private double distancePointSegment(double px, double py, double x1, double y1, double x2, double y2) {
    double A = px - x1;
    double B = py - y1;
    double C = x2 - x1;
    double D = y2 - y1;
    
    double dot = A * C + B * D;
    double lenSq = C * C + D * D;
    double param = -1;
    
    if (lenSq != 0) {
        param = dot / lenSq;
    }
    
    double xx, yy;
    
    if (param < 0) {
        xx = x1;
        yy = y1;
    } else if (param > 1) {
        xx = x2;
        yy = y2;
    } else {
        xx = x1 + param * C;
        yy = y1 + param * D;
    }
    
    double dx = px - xx;
    double dy = py - yy;
    return Math.sqrt(dx * dx + dy * dy);
}
    
    // Méthodes pour gérer l'ajout d'éléments chauffants dans les pièces irrégulières
    public boolean estModeAjoutElementChauffant() {
        return modeAjoutElementChauffant;
    }
    
    public void desactiverModeAjoutElementChauffant() {
        modeAjoutElementChauffant = false;
    }
    
    // Méthodes pour gérer le déplacement d'éléments chauffants dans les pièces irrégulières
    public boolean estModeDeplacementElementChauffant() {
        return modeDeplacementElementChauffant;
    }
    
    public void activerModeDeplacementElementChauffant() {
        if (elementSelectionne != null && estPieceIrreguliere) {
            modeDeplacementElementChauffant = true;
        }
    }
    
    public void desactiverModeDeplacementElementChauffant() {
        modeDeplacementElementChauffant = false;
    }
    
  
    public int trouverMurLePlusProche(double xPouces, double yPouces) {
        if (pieceIrreguliere == null || !pieceIrreguliere.estFermee()) {
            return -1;
        }
        
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        if (points.size() < 2) return -1;
        
        double distanceMin = Double.MAX_VALUE;
        int indexMur = -1;
        double tolerance = 10.0; // Tolérance de 10 pouces pour cliquer sur un mur
        
        // Pour une pièce fermée avec n points, on a n murs (y compris le dernier qui ferme le polygone)
        int nombreMurs = points.size();
        
        for (int i = 0; i < nombreMurs; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get((i + 1) % nombreMurs); // Utiliser modulo pour le dernier mur
            
            double distance = distancePointSegment(xPouces, yPouces, p1.x, p1.y, p2.x, p2.y);
            
            if (distance < distanceMin && distance <= tolerance) {
                distanceMin = distance;
                indexMur = i;
            }
        }
        
        return indexMur;
    }
    
    /**
     * Ajoute un élément chauffant sur un mur spécifique d'une pièce irrégulière
     * @param indexMur l'index du segment (mur) sur lequel placer l'élément
     * @param xPouces coordonnée X du point de clic
     * @param yPouces coordonnée Y du point de clic
     * @return true si l'ajout a réussi
     */
    public boolean ajouterElementChauffantSurMurIrregulier(int indexMur, double xPouces, double yPouces) {
        if (pieceIrreguliere == null || !pieceIrreguliere.estFermee()) {
            return false;
        }
        
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        int n = points.size();
        
        // Pour une pièce fermée avec n points, on a n murs (y compris le dernier qui ferme le polygone)
        if (indexMur < 0 || indexMur >= n) {
            return false;
        }
        
        // Sauvegarder l'état avant d'ajouter
        sauvegarderEtat();
        
        Point2D.Double p1 = points.get(indexMur);
        Point2D.Double p2 = points.get((indexMur + 1) % n); // Utiliser modulo pour le dernier mur
        
        // Calculer le point le plus proche sur le segment (mur)
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double longueurMur = Math.sqrt(dx * dx + dy * dy);
        
        if (longueurMur < 0.001) return false; // Segment dégénéré
        
        // Calculer la projection du point de clic sur le segment
        double t = ((xPouces - p1.x) * dx + (yPouces - p1.y) * dy) / (longueurMur * longueurMur);
        t = Math.max(0.0, Math.min(1.0, t)); // Clamper entre 0 et 1
        
        double murX = p1.x + t * dx;
        double murY = p1.y + t * dy;
        
        // Calculer l'angle du mur en radians
        double angleMur = Math.atan2(dy, dx);
        
        // Déterminer l'orientation approximative (pour compatibilité)
        boolean horizontal = Math.abs(dy) < Math.abs(dx);
        
        double largeur = largeurElementChauffantEnAttente;
        double hauteur = hauteurElementChauffantEnAttente;
        
        // Calculer le centre de la pièce une seule fois
        double centreX = 0, centreY = 0;
        for (Point2D.Double p : points) {
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= points.size();
        centreY /= points.size();
        
        // Calculer le vecteur normal au mur (vers l'intérieur)
        double normX = -dy / longueurMur; // Vecteur perpendiculaire au mur
        double normY = dx / longueurMur;
        
        // Vérifier si le vecteur normal pointe vers l'intérieur (vers le centre)
        double dot = normX * (centreX - murX) + normY * (centreY - murY);
        if (dot < 0) {
            // Inverser le vecteur normal pour qu'il pointe vers l'intérieur
            normX = -normX;
            normY = -normY;
        }
        
        // Calculer le centre de l'élément : sur le mur, décalé perpendiculairement vers l'intérieur
        // Le centre doit être exactement à hauteur/2 du mur (pour que l'élément soit collé au mur)
        double centreElementX = murX + normX * (hauteur / 2.0);
        double centreElementY = murY + normY * (hauteur / 2.0);
        
        // Calculer le coin supérieur gauche dans le repère non-roté
        // L'élément est un rectangle de largeur x hauteur, centré à (centreElementX, centreElementY)
        // et tourné de angleMur. Le coin supérieur gauche dans le repère local de l'élément est (-largeur/2, -hauteur/2)
        // On applique la rotation normale pour obtenir sa position dans le repère global
        double cosAngle = Math.cos(angleMur);
        double sinAngle = Math.sin(angleMur);
        
        // Vecteur du centre vers le coin supérieur gauche dans le repère local (non-roté)
        double dxLocal = -largeur / 2.0;
        double dyLocal = -hauteur / 2.0;
        
        // Appliquer la rotation normale pour obtenir le coin dans le repère global
        // Rotation : x' = x*cos - y*sin, y' = x*sin + y*cos
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        // Position du coin supérieur gauche
        double x = centreElementX + dxGlobal;
        double y = centreElementY + dyGlobal;
        
        // Vérifier les collisions avec d'autres éléments chauffants
        if (verifierCollisionElementChauffantIrregulier(x, y, largeur, horizontal)) {
            return false; // Collision détectée
        }
        
        // Ajouter l'élément avec l'angle du mur
        ElementChauffant e = new ElementChauffant(x, y, largeur, hauteur, horizontal, angleMur);
        pieceIrreguliere.getElementsChauffants().add(e);
        elementSelectionne = e;
        modeAjoutElementChauffant = false;
        
        return true;
    }
    
    /**
     * Déplace un élément chauffant sur un nouveau mur dans une pièce irrégulière
     * @param indexMur l'index du segment (mur) sur lequel déplacer l'élément
     * @param xPouces coordonnée X du point de clic
     * @param yPouces coordonnée Y du point de clic
     * @return true si le déplacement a réussi
     */
    public boolean deplacerElementChauffantSurMurIrregulier(int indexMur, double xPouces, double yPouces) {
        if (pieceIrreguliere == null || !pieceIrreguliere.estFermee()) {
            return false;
        }
        
        if (elementSelectionne == null) {
            return false;
        }
        
        if (indexMur < 0 || indexMur >= pieceIrreguliere.getPoints().size()) {
            return false;
        }
        
        // Sauvegarder l'état avant le déplacement
        sauvegarderEtat();
        
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        int n = points.size();
        
        Point2D.Double p1 = points.get(indexMur);
        Point2D.Double p2 = points.get((indexMur + 1) % n);
        
        // Calculer le point le plus proche sur le segment (mur)
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double longueurMur = Math.sqrt(dx * dx + dy * dy);
        
        if (longueurMur < 0.001) return false; // Segment dégénéré
        
        // Calculer la projection du point de clic sur le segment
        double t = ((xPouces - p1.x) * dx + (yPouces - p1.y) * dy) / (longueurMur * longueurMur);
        t = Math.max(0.0, Math.min(1.0, t)); // Clamper entre 0 et 1
        
        double murX = p1.x + t * dx;
        double murY = p1.y + t * dy;
        
        // Calculer le nouvel angle du mur
        double angleMurNouveau = Math.atan2(dy, dx);
        
        // Déterminer l'orientation approximative (pour compatibilité)
        boolean horizontal = Math.abs(dy) < Math.abs(dx);
        
        double largeur = elementSelectionne.getLargeur();
        double hauteur = elementSelectionne.getHauteur();
        
        // Calculer le centre de la pièce pour déterminer la direction vers l'intérieur
        double centreX = 0, centreY = 0;
        for (Point2D.Double p : points) {
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= points.size();
        centreY /= points.size();
        
        // Calculer le vecteur normal au mur (vers l'intérieur)
        double normX = -dy / longueurMur;
        double normY = dx / longueurMur;
        
        // Vérifier si le vecteur normal pointe vers l'intérieur
        double dot = normX * (centreX - murX) + normY * (centreY - murY);
        if (dot < 0) {
            normX = -normX;
            normY = -normY;
        }
        
        // Calculer le centre de l'élément : sur le mur, décalé perpendiculairement vers l'intérieur
        double centreElementX = murX + normX * (hauteur / 2.0);
        double centreElementY = murY + normY * (hauteur / 2.0);
        
        // Calculer le coin supérieur gauche
        double cosAngle = Math.cos(angleMurNouveau);
        double sinAngle = Math.sin(angleMurNouveau);
        
        double dxLocal = -largeur / 2.0;
        double dyLocal = -hauteur / 2.0;
        
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        double x = centreElementX + dxGlobal;
        double y = centreElementY + dyGlobal;
        
        // Vérifier les collisions avec d'autres éléments chauffants (sauf celui qu'on déplace)
        if (verifierCollisionElementChauffantIrregulierDeplacement(x, y, largeur, horizontal, elementSelectionne)) {
            return false; // Collision détectée
        }
        
        // Mettre à jour l'élément avec la nouvelle position et le nouvel angle
        elementSelectionne.setX(x);
        elementSelectionne.setY(y);
        elementSelectionne.setAngle(angleMurNouveau);
        elementSelectionne.setHorizontal(horizontal);
        
        modeDeplacementElementChauffant = false;
        
        return true;
    }
    
    /**
     * Déplace un élément chauffant en temps réel pendant le drag dans une pièce irrégulière
     * Cette méthode est appelée pendant le drag pour mettre à jour la position et l'orientation
     * @param xPouces coordonnée X du point de clic
     * @param yPouces coordonnée Y du point de clic
     * @return true si le déplacement a réussi
     */
    public boolean deplacerElementChauffantDragIrregulier(double xPouces, double yPouces) {
        if (pieceIrreguliere == null || !pieceIrreguliere.estFermee()) {
            return false;
        }
        
        if (elementSelectionne == null) {
            return false;
        }
        
        // Trouver le mur le plus proche
        int indexMur = trouverMurLePlusProche(xPouces, yPouces);
        if (indexMur < 0) {
            return false;
        }
        
        List<Point2D.Double> points = pieceIrreguliere.getPoints();
        int n = points.size();
        
        Point2D.Double p1 = points.get(indexMur);
        Point2D.Double p2 = points.get((indexMur + 1) % n); // Utiliser modulo pour le dernier mur
        
        // Calculer le point le plus proche sur le segment (mur)
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double longueurMur = Math.sqrt(dx * dx + dy * dy);
        
        if (longueurMur < 0.001) return false; // Segment dégénéré
        
        // Calculer la projection du point de clic sur le segment
        double t = ((xPouces - p1.x) * dx + (yPouces - p1.y) * dy) / (longueurMur * longueurMur);
        t = Math.max(0.0, Math.min(1.0, t)); // Clamper entre 0 et 1
        
        double murX = p1.x + t * dx;
        double murY = p1.y + t * dy;
        
        // Calculer le centre de la pièce pour déterminer la direction vers l'intérieur
        double centreX = 0, centreY = 0;
        for (Point2D.Double p : points) {
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= points.size();
        centreY /= points.size();
        
        // Calculer le vecteur normal au mur (vers l'intérieur)
        double normX = -dy / longueurMur;
        double normY = dx / longueurMur;
        
        // Vérifier si le vecteur normal pointe vers l'intérieur
        double dot = normX * (centreX - murX) + normY * (centreY - murY);
        if (dot < 0) {
            normX = -normX;
            normY = -normY;
        }
        
        // Déterminer l'angle à utiliser selon le type d'élément
        // Pour le thermostat : angle de la normale (perpendiculaire au mur)
        // Pour les éléments chauffants : angle du mur (parallèle au mur)
        boolean estThermostat = Thermostat.estThermostat(elementSelectionne);
        double angleMurNouveau = Math.atan2(dy, dx);
        double angleFinal;
        
        if (estThermostat) {
            // Le thermostat doit être perpendiculaire au mur, donc utiliser l'angle de la normale
            angleFinal = Math.atan2(normY, normX);
        } else {
            // Les éléments chauffants sont parallèles au mur
            angleFinal = angleMurNouveau;
        }
        
        // Déterminer l'orientation approximative (pour compatibilité)
        boolean horizontal = Math.abs(dy) < Math.abs(dx);
        
        double largeur = elementSelectionne.getLargeur();
        double hauteur = elementSelectionne.getHauteur();
        
        // Calculer le centre de l'élément : sur le mur, décalé perpendiculairement vers l'intérieur
        double centreElementX = murX + normX * (hauteur / 2.0);
        double centreElementY = murY + normY * (hauteur / 2.0);
        
        // Calculer le coin supérieur gauche
        double cosAngle = Math.cos(angleFinal);
        double sinAngle = Math.sin(angleFinal);
        
        double dxLocal = -largeur / 2.0;
        double dyLocal = -hauteur / 2.0;
        
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        double x = centreElementX + dxGlobal;
        double y = centreElementY + dyGlobal;
        
        // Mettre à jour l'élément avec la nouvelle position et le nouvel angle (sans vérifier les collisions pendant le drag)
        elementSelectionne.setX(x);
        elementSelectionne.setY(y);
        elementSelectionne.setAngle(angleFinal);
        elementSelectionne.setHorizontal(horizontal);
        
        return true;
    }
    
    /**
     * Valide la position finale d'un élément chauffant après le drag dans une pièce irrégulière
     * Vérifie les collisions et restaure la position précédente si nécessaire
     * @return true si la position est valide
     */
    public boolean validerPositionElementChauffantDragIrregulier() {
        if (pieceIrreguliere == null || elementSelectionne == null) {
            return false;
        }
        
        double x = elementSelectionne.getX();
        double y = elementSelectionne.getY();
        double largeur = elementSelectionne.getLargeur();
        boolean horizontal = elementSelectionne.isHorizontal();
        
        // Vérifier les collisions avec d'autres éléments chauffants
        if (verifierCollisionElementChauffantIrregulierDeplacement(x, y, largeur, horizontal, elementSelectionne)) {
            return false; // Collision détectée
        }
        
        return true;
    }
    
    /**
     * Vérifie s'il y a collision avec d'autres éléments chauffants lors du déplacement
     */
    private boolean verifierCollisionElementChauffantIrregulierDeplacement(double x, double y, double taille, boolean horizontal, ElementChauffant elementADeplacer) {
        if (pieceIrreguliere == null) return false;
        
        for (ElementChauffant element : pieceIrreguliere.getElementsChauffants()) {
            // Ignorer l'élément qu'on déplace
            if (element == elementADeplacer) continue;
            
            double ex = element.getX();
            double ey = element.getY();
            double et = element.getLargeur();
            
            if (horizontal && element.isHorizontal()) {
                // Les deux sont horizontaux : vérifier si sur même Y et chevauchement X
                if (Math.abs(y - ey) < 2.0 && x < ex + et && x + taille > ex) {
                    return true;
                }
            } else if (!horizontal && !element.isHorizontal()) {
                // Les deux sont verticaux : vérifier si sur même X et chevauchement Y
                if (Math.abs(x - ex) < 2.0 && y < ey + et && y + taille > ey) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Vérifie s'il y a collision avec d'autres éléments chauffants dans une pièce irrégulière
     */
    private boolean verifierCollisionElementChauffantIrregulier(double x, double y, double taille, boolean horizontal) {
        if (pieceIrreguliere == null) return false;
        
        for (ElementChauffant element : pieceIrreguliere.getElementsChauffants()) {
            double ex = element.getX();
            double ey = element.getY();
            double et = element.getLargeur();
            
            if (horizontal && element.isHorizontal()) {
                // Les deux sont horizontaux : vérifier si sur même Y et chevauchement X
                if (Math.abs(y - ey) < 2.0 && x < ex + et && x + taille > ex) {
                    return true;
                }
            } else if (!horizontal && !element.isHorizontal()) {
                // Les deux sont verticaux : vérifier si sur même X et chevauchement Y
                if (Math.abs(x - ex) < 2.0 && y < ey + et && y + taille > ey) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Repositionne un élément chauffant sur son mur après redimensionnement
     */
    private void repositionnerElementChauffantSurMur(ElementChauffant elementInitial, 
                                                      List<Point2D.Double> nouveauxPoints,
                                                      List<Point2D.Double> pointsInitiaux) {
        if (pieceIrreguliere == null || nouveauxPoints == null || pointsInitiaux == null) {
            return;
        }
        
        // Calculer le centre réel de l'élément initial en tenant compte de la rotation
        double centreXInitial, centreYInitial;
        if (Math.abs(elementInitial.getAngle()) > 0.001) {
            // Élément avec rotation : calculer le centre réel
            double cosAngle = Math.cos(elementInitial.getAngle());
            double sinAngle = Math.sin(elementInitial.getAngle());
            
            // Vecteur du coin supérieur gauche vers le centre dans le repère local
            double dxLocal = elementInitial.getLargeur() / 2.0;
            double dyLocal = elementInitial.getHauteur() / 2.0;
            
            // Appliquer la rotation pour obtenir le vecteur dans le repère global
            double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
            double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
            
            // Centre réel dans le repère global
            centreXInitial = elementInitial.getX() + dxGlobal;
            centreYInitial = elementInitial.getY() + dyGlobal;
        } else {
            // Élément sans rotation : centre simple
            centreXInitial = elementInitial.getX() + elementInitial.getLargeur() / 2.0;
            centreYInitial = elementInitial.getY() + elementInitial.getHauteur() / 2.0;
        }
        
        // Trouver le mur (segment) le plus proche du centre de l'élément initial
        int indexMur = -1;
        double distanceMin = Double.MAX_VALUE;
        double positionRelativeSurMur = 0.0;
        
        int n = pointsInitiaux.size();
        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = pointsInitiaux.get(i);
            Point2D.Double p2 = pointsInitiaux.get((i + 1) % n);
            
            // Calculer la distance du centre au segment et la position relative
            double dx = p2.x - p1.x;
            double dy = p2.y - p1.y;
            double longueurMur = Math.sqrt(dx * dx + dy * dy);
            
            if (longueurMur < 0.001) continue;
            
            // Projection du centre sur le segment
            double t = ((centreXInitial - p1.x) * dx + (centreYInitial - p1.y) * dy) / (longueurMur * longueurMur);
            t = Math.max(0.0, Math.min(1.0, t));
            
            double projX = p1.x + t * dx;
            double projY = p1.y + t * dy;
            
            double distance = Math.sqrt(Math.pow(centreXInitial - projX, 2) + Math.pow(centreYInitial - projY, 2));
            
            if (distance < distanceMin && distance < 10.0) { // Tolérance de 10 pouces
                distanceMin = distance;
                indexMur = i;
                positionRelativeSurMur = t;
            }
        }
        
        if (indexMur < 0) {
            // Mur non trouvé, ne pas repositionner
            return;
        }
        
        // Calculer la nouvelle position sur le mur redimensionné
        Point2D.Double p1Nouveau = nouveauxPoints.get(indexMur);
        Point2D.Double p2Nouveau = nouveauxPoints.get((indexMur + 1) % n);
        
        double dxNouveau = p2Nouveau.x - p1Nouveau.x;
        double dyNouveau = p2Nouveau.y - p1Nouveau.y;
        double longueurMurNouveau = Math.sqrt(dxNouveau * dxNouveau + dyNouveau * dyNouveau);
        
        if (longueurMurNouveau < 0.001) return;
        
        // Point sur le nouveau mur à la même position relative
        double murXNouveau = p1Nouveau.x + positionRelativeSurMur * dxNouveau;
        double murYNouveau = p1Nouveau.y + positionRelativeSurMur * dyNouveau;
        
        // Calculer le nouvel angle du mur
        double angleMurNouveau = Math.atan2(dyNouveau, dxNouveau);
        
        // Calculer le centre de la pièce pour déterminer la direction vers l'intérieur
        double centreX = 0, centreY = 0;
        for (Point2D.Double p : nouveauxPoints) {
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= nouveauxPoints.size();
        centreY /= nouveauxPoints.size();
        
        // Calculer le vecteur normal au mur (vers l'intérieur)
        double normX = -dyNouveau / longueurMurNouveau;
        double normY = dxNouveau / longueurMurNouveau;
        
        // Vérifier si le vecteur normal pointe vers l'intérieur
        double dot = normX * (centreX - murXNouveau) + normY * (centreY - murYNouveau);
        if (dot < 0) {
            normX = -normX;
            normY = -normY;
        }
        
        // Calculer le centre de l'élément : sur le mur, décalé perpendiculairement vers l'intérieur
        double centreElementX = murXNouveau + normX * (elementInitial.getHauteur() / 2.0);
        double centreElementY = murYNouveau + normY * (elementInitial.getHauteur() / 2.0);
        
        // Calculer le coin supérieur gauche
        double cosAngle = Math.cos(angleMurNouveau);
        double sinAngle = Math.sin(angleMurNouveau);
        
        double dxLocal = -elementInitial.getLargeur() / 2.0;
        double dyLocal = -elementInitial.getHauteur() / 2.0;
        
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        double x = centreElementX + dxGlobal;
        double y = centreElementY + dyGlobal;
        
        // Créer le nouvel élément avec le nouvel angle
        ElementChauffant nouvelElement = new ElementChauffant(
            x, y, 
            elementInitial.getLargeur(), 
            elementInitial.getHauteur(), 
            Math.abs(dyNouveau) < Math.abs(dxNouveau), // horizontal approximatif
            angleMurNouveau
        );
        
        pieceIrreguliere.getElementsChauffants().add(nouvelElement);
    }
    
}
