package com.mycompany.equipe43.Vue;

import com.mycompany.equipe43.Controleur.Controleur;
import com.mycompany.equipe43.Domaine.DTO.MeubleDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceIrreguliereDTO;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;
import com.mycompany.equipe43.Domaine.ElementChauffant;
import com.mycompany.equipe43.Domaine.Thermostat;
import com.mycompany.equipe43.Domaine.Fil;
import com.mycompany.equipe43.Domaine.Intersection;
import com.mycompany.equipe43.Domaine.PathFinder;
import com.mycompany.equipe43.Domaine.TypeMeubleDrain;

import com.mycompany.equipe43.Vue.Drawing.Afficheur;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.awt.event.ActionEvent;

public class DrawingPanel extends JPanel {

    private Controleur controleur;
    private MainWindow mainWindow;
    private Afficheur afficheur;

    private boolean isResizing = false;
    private Point startPoint;
    private double startLargeur;
    private double startLongueur;
    private boolean isDraggingThermostatRect = false;
    private boolean isDraggingThermostat = false;
    private double startPoucesX;
    private double startPoucesY;

    private double dragOffsetX = 0.0; // pouces
    private double dragOffsetY = 0.0; // pouces

    private double thermostatOffsetX = 0.0;
    private double thermostatOffsetY = 0.0;
    // Position initiale du thermostat avant le drag (pour restauration en cas de
    // besoin)
    private double thermostatXAvantDrag = 0;
    private double thermostatYAvantDrag = 0;
    private double thermostatAngleAvantDrag = 0;

    private boolean isDraggingElement = false;
    private boolean isDraggingHeatingElement = false;
    // Position initiale de l'élément chauffant avant le drag (pour restauration en
    // cas de collision)
    private double elementChauffantXAvantDrag = 0;
    private double elementChauffantYAvantDrag = 0;
    private double elementChauffantAngleAvantDrag = 0;

    private boolean isDraggingFilPoint = false;
    private int filPointIndex = -1;
    private static final int FIL_POINT_RADIUS = 8;
    private static final double GRID_SPACING = 4.0; // Doit correspondre à l'espacement du PathFinder

    private static final int POIGNEE_SIZE = 10;

    private double zoom = 1.0;
    private int zoomPointX = 0;
    private int zoomPointY = 0;

    // distanceGrillePouces est maintenant dans le Controleur
    private List<Intersection> graphe = new ArrayList<>();
    private List<Intersection> cheminFil = new ArrayList<>();
    // Déplacement au clavier (en pouces)
    private static final double TH_STEP = 1.0; // 1 pouce par touche
    private static final double TH_STEP_FAST = 4.0; // Shift + flèche

    public DrawingPanel(Controleur controleur) {
        this.controleur = controleur;
        this.afficheur = new Afficheur();
        setPreferredSize(new Dimension(800, 600));

        setupMouseListeners();
        setFocusable(true);
        installerKeyBindingsThermostat();

        // Supprimer meuble sélectionné avec DELETE
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteSel");
        getActionMap().put("deleteSel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (controleur.estPieceIrreguliere() && controleur.getPointSelectionneIndex() >= 0) {
                    if (controleur.supprimerPointSelectionne()) {
                        repaint();
                        if (mainWindow != null) {
                            mainWindow.afficherSucces("point supprimé !");
                        }
                    } else {
                        if (mainWindow != null) {
                            mainWindow.afficherAvertissement("Impossible de supprimer: minimum 3 points requis.");
                        }
                    }
                    return;
                }
                if (controleur.supprimerMeubleSelectionne()) {
                    repaint();
                    if (mainWindow != null)
                        mainWindow.afficherMeubleSelectionne();
                }
            }
        });
    }

    public DrawingPanel() {
        this(new Controleur());
    }

    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public Afficheur getAfficheur() {
        return afficheur;
    }

    // Appeler une seule fois (dans le constructeur)
    private void installerKeyBindingsThermostat() {
        setFocusable(true);

        InputMap im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Flèches
        bindMove(im, am, "LEFT", -TH_STEP, 0);
        bindMove(im, am, "RIGHT", TH_STEP, 0);
        bindMove(im, am, "UP", 0, TH_STEP);
        bindMove(im, am, "DOWN", 0, -TH_STEP);

        // Shift + flèches
        bindMove(im, am, "shift LEFT", -TH_STEP_FAST, 0);
        bindMove(im, am, "shift RIGHT", TH_STEP_FAST, 0);
        bindMove(im, am, "shift UP", 0, TH_STEP_FAST);
        bindMove(im, am, "shift DOWN", 0, -TH_STEP_FAST);

        // WASD (optionnel)
        bindMove(im, am, "A", -TH_STEP, 0);
        bindMove(im, am, "D", TH_STEP, 0);
        bindMove(im, am, "W", 0, TH_STEP);
        bindMove(im, am, "S", 0, -TH_STEP);
    }

    private void bindMove(InputMap im, ActionMap am, String keyStroke, double dxPouces, double dyPouces) {
        String actionName = "MOVE_TH_" + keyStroke.replace(" ", "_");
        im.put(KeyStroke.getKeyStroke(keyStroke), actionName);

        am.put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deplacerThermostatAuClavier(dxPouces, dyPouces);
            }
        });
    }

    private void deplacerThermostatAuClavier(double dx, double dy) {

        // ---- CAS PIECE IRREGULIERE ----
        if (controleur.estPieceIrreguliere()) {
            // Le thermostat est maintenant un ElementChauffant, utiliser la même logique
            ElementChauffant element = controleur.getElementSelectionne();
            if (element == null || !Thermostat.estThermostat(element)) {
                // Sélectionner le thermostat s'il n'est pas déjà sélectionné
                PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                if (pieceIrreg != null && pieceIrreg.getThermostat() != null) {
                    ThermostatDTO t = pieceIrreg.getThermostat();
                    controleur.selectionnerElementChauffant(t.getX(), t.getY());
                    element = controleur.getElementSelectionne();
                }
            }

            if (element != null && Thermostat.estThermostat(element)) {
                double newX = element.getX() + dx;
                double newY = element.getY() + dy;

                // Utiliser la méthode de snap des éléments chauffants
                // Le thermostat est déjà sélectionné, utiliser deplacerThermostatIrregulierSnap
                controleur.deplacerThermostatIrregulierSnap(newX, newY, true);
                repaint();
                return;
            }
        }

        // ---- CAS PIECE REGULIERE ----
        PieceDTO piece = controleur.getPiece();
        if (piece == null || piece.getThermostat() == null)
            return;

        ThermostatDTO t = piece.getThermostat();

        double cx = (t.getX() + t.getLargeur() / 2.0) + dx;
        double cy = (t.getY() + t.getHauteur() / 2.0) + dy;

        controleur.deplacerThermostatSnap(cx, cy);
        repaint();
    }

    private Point2D screenToWorld(int screenX, int screenY) {
        try {
            AffineTransform at = new AffineTransform();

            at.translate(0, getHeight());
            at.scale(1, -1);

            int zoomPointYCartesian = getHeight() - zoomPointY;
            at.translate(zoomPointX, zoomPointYCartesian);
            at.scale(zoom, zoom);
            at.translate(-zoomPointX, -zoomPointYCartesian);

            at.invert();

            Point2D worldPoint = new Point2D.Double();
            at.transform(new Point2D.Double(screenX, screenY), worldPoint);

            return worldPoint;

        } catch (NoninvertibleTransformException e) {
            return new Point2D.Double(screenX, screenY);
        }
    }

    public void zoomIn() {
        zoom *= 1.2;
        if (zoom < 0.01) {
            zoom = 0.01;
        }
        repaint();
    }

    public void zoomOut() {
        zoom /= 1.2;
        if (zoom < 0.01) {
            zoom = 0.01;
        }
        repaint();
    }

    public void resetZoom() {
        zoom = 1.0;
        zoomPointX = getWidth() / 2; // centre de l'ecran
        zoomPointY = getHeight() / 2;
        repaint();
    }

    public void setDistanceGrillePouces(double distance) {
        if (distance > 0) {
            // Sauvegarder l'état avant de modifier la distance
            controleur.sauvegarderEtat();
            controleur.setDistanceGrillePouces(distance);
            // Régénérer automatiquement la grille et les intersections avec la nouvelle
            // distance
            // Seulement en mode modélisation
            if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
                genererEtDessinerGraphe();
            } else {
                repaint();
            }
        }
    }

    public void genererEtDessinerGraphe() {
        if (graphe != null) {
            graphe.clear();
        }
        graphe = genererGraphe();
        // Pour les pièces irrégulières, utiliser uniquement filtrerIntersections qui
        // gère tout
        if (controleur.estPieceIrreguliere()) {
            graphe = filtrerIntersections(graphe, 3); // margeMur = 3 pouces par défaut
        } else {
            // Pour les pièces régulières, utiliser l'algorithme original
            graphe = controleur.filtrerIntersectionsSelonObstacles(graphe); // seulement celles valides
        }
        repaint();
    }

    public void genererEtAfficherFilAutomatique(double longueurFilPouces) {
        graphe = genererGraphe();
        // Pour les pièces irrégulières, utiliser uniquement filtrerIntersections qui
        // gère tout
        if (controleur.estPieceIrreguliere()) {
            graphe = filtrerIntersections(graphe, 3);
        } else {
            // Pour les pièces régulières, utiliser l'algorithme original
            graphe = controleur.filtrerIntersectionsSelonObstacles(graphe);
        }
        // Utiliser la méthode originale genererCheminFil
        List<Intersection> chemin = genererCheminFil(graphe, longueurFilPouces);

        this.cheminFil = chemin;

        // Mettre à jour le chemin dans le contrôleur pour la validation
        if (!chemin.isEmpty()) {
            List<Point2D.Double> cheminPoints = new ArrayList<>();
            for (Intersection i : chemin) {
                cheminPoints.add(new Point2D.Double(i.getX(), i.getY()));
            }
            controleur.setCheminFil(cheminPoints);
        }

        repaint();
    }

    /**
     * Vérifie si un fil a déjà été généré
     * 
     * @return true si un fil existe (cheminFil n'est pas vide)
     */
    public boolean aUnFilGenere() {
        return cheminFil != null && !cheminFil.isEmpty();
    }

    public List<Intersection> genererGraphe() {
        List<Intersection> intersections = new ArrayList<>();

        // Vérifier si c'est une pièce irrégulière
        if (controleur.estPieceIrreguliere()) {
            PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
            if (pieceIrreg == null || !pieceIrreg.estFermee())
                return intersections;

            // Utiliser la bounding box pour générer la grille (comme une pièce régulière)
            double minX = pieceIrreg.getMinX();
            double minY = pieceIrreg.getMinY();
            double maxX = pieceIrreg.getMaxX();
            double maxY = pieceIrreg.getMaxY();

            Map<String, Intersection> map = new HashMap<>();

            // Génération des intersections
            double distanceGrillePouces = controleur.getDistanceGrillePouces();
            double translationX = controleur.getTranslationX();
            double translationY = controleur.getTranslationY();
            for (double x = minX; x <= maxX; x += distanceGrillePouces) {
                for (double y = minY; y <= maxY; y += distanceGrillePouces) {
                    // Appliquer la translation
                    Intersection i = new Intersection(x + translationX, y + translationY);
                    intersections.add(i);
                    // Utiliser les coordonnées translatées
                    String key = Math.round((x + translationX) * 100) + "_" + Math.round((y + translationY) * 100);
                    map.put(key, i);
                }
            }

            // Connexions haut/bas/gauche/droite
            for (Intersection i : intersections) {
                double x = i.getX();
                double y = i.getY();

                // Chercher voisin Haut
                String keyTop = Math.round(x * 100) + "_" + Math.round((y + distanceGrillePouces) * 100);
                Intersection top = map.get(keyTop);
                if (top != null) {
                    i.setTop(top);
                    top.setBottom(i);
                }

                // Chercher voisin Droite
                String keyRight = Math.round((x + distanceGrillePouces) * 100) + "_" + Math.round(y * 100);
                Intersection right = map.get(keyRight);
                if (right != null) {
                    i.setRight(right);
                    right.setLeft(i);
                }
            }

            return intersections;
        }

        // Code original pour les pièces régulières
        PieceDTO piece = controleur.getPiece();
        if (piece == null)
            return intersections;

        double largeur = piece.getLargeur();
        double longueur = piece.getLongueur();

        Map<String, Intersection> map = new HashMap<>();

        // Génération des intersections
        double distanceGrillePouces = controleur.getDistanceGrillePouces();
        double translationX = controleur.getTranslationX();
        double translationY = controleur.getTranslationY();
        for (double x = 0; x <= largeur; x += distanceGrillePouces) {
            for (double y = 0; y <= longueur; y += distanceGrillePouces) {
                // Appliquer la translation
                Intersection i = new Intersection(x + translationX, y + translationY);
                intersections.add(i);
                // Utiliser les coordonnées
                String key = Math.round((x + translationX) * 100) + "_" + Math.round((y + translationY) * 100);
                map.put(key, i);
            }
        }

        // Connexions haut/bas/gauche/droite
        for (Intersection i : intersections) {
            /*
             * for (Intersection j : intersections) {
             * if (Math.abs(i.getX() - j.getX()) < 0.001) { // même colonne
             * if (j.getY() > i.getY()) i.setTop(j);
             * else if (j.getY() < i.getY()) i.setBottom(j);
             * }
             * if (Math.abs(i.getY() - j.getY()) < 0.001) { // même ligne
             * if (j.getX() > i.getX()) i.setRight(j);
             * else if (j.getX() < i.getX()) i.setLeft(j);
             * }
             * }
             */
            // Connexions haut/bas/gauche/droite
            double x = i.getX();
            double y = i.getY();

            // Chercher voisin Haut
            String keyTop = Math.round(x * 100) + "_" + Math.round((y + distanceGrillePouces) * 100);
            Intersection top = map.get(keyTop);
            if (top != null) {
                i.setTop(top);
                top.setBottom(i);
            }

            // Chercher voisin Droite
            String keyRight = Math.round((x + distanceGrillePouces) * 100) + "_" + Math.round(y * 100);
            Intersection right = map.get(keyRight);
            if (right != null) {
                i.setRight(right);
                right.setLeft(i);
            }
        }

        return intersections;
    }

    private List<Intersection> filtrerIntersections(List<Intersection> intersections, double margeMur) {
        // Vérifier si c'est une pièce irrégulière
        if (controleur.estPieceIrreguliere()) {
            PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
            if (pieceIrreg == null || !pieceIrreg.estFermee())
                return intersections;

            List<Intersection> valides = new ArrayList<>();
            List<ElementChauffant> elementsChauffants = controleur.getElementsChauffants();

            for (Intersection i : intersections) {
                double x = i.getX();
                double y = i.getY();

                // 1. Vérifier si le point est à l'intérieur du polygone
                if (!pieceIrreg.contientPoint(x, y))
                    continue;

                // 2. Vérifier la distance aux murs (segments du polygone) - au moins 3 pouces
                double distanceMinAuMur = calculerDistanceMinAuMur(x, y, pieceIrreg.getPoints());
                if (distanceMinAuMur < 3.0)
                    continue;

                // 3. Vérifier distance aux éléments chauffants - au moins 8 pouces
                boolean tropProcheElementChauffant = false;
                for (ElementChauffant element : elementsChauffants) {
                    double distance = calculerDistancePointElementChauffant(x, y, element);
                    if (distance < 8.0) {
                        tropProcheElementChauffant = true;
                        break;
                    }
                }
                if (tropProcheElementChauffant)
                    continue;

                // 4. Vérifier distance aux drains - au moins 6 pouces (10 pour toilette)
                boolean tropProcheDrain = false;
                for (MeubleDrainDTO m : pieceIrreg.getMeublesDrain()) {
                    double xDrain = m.getX() + m.getXDrainRelatif();
                    double yDrain = m.getY() + m.getYDrainRelatif();
                    double distance = Math.sqrt(Math.pow(x - xDrain, 2) + Math.pow(y - yDrain, 2));

                    double distanceMin = (m.getType() == TypeMeubleDrain.TOILETTE) ? 10.0 : 6.0;
                    if (distance < distanceMin) {
                        tropProcheDrain = true;
                        break;
                    }
                }
                if (tropProcheDrain)
                    continue;

                // 5. Vérifier distance aux meubles (sans drain) - au moins 3 pouces
                boolean tropProcheMeuble = false;
                for (MeubleSansDrainDTO m : pieceIrreg.getMeubles()) {
                    double distance = calculerDistancePointRectangle(x, y, m.getX(), m.getY(),
                            m.getLargeur(), m.getHauteur());
                    if (distance < 3.0) {
                        tropProcheMeuble = true;
                        break;
                    }
                }
                if (tropProcheMeuble)
                    continue;

                // 6. Vérifier distance aux meubles avec drain (le meuble lui-même, pas le
                // drain) - au moins 3 pouces
                for (MeubleDrainDTO m : pieceIrreg.getMeublesDrain()) {
                    double distance = calculerDistancePointRectangle(x, y, m.getX(), m.getY(),
                            m.getLargeur(), m.getHauteur());
                    if (distance < 3.0) {
                        tropProcheMeuble = true;
                        break;
                    }
                }
                if (tropProcheMeuble)
                    continue;

                valides.add(i);
            }
            return valides;
        }

        // Code original pour les pièces régulières
        PieceDTO piece = controleur.getPiece();
        if (piece == null)
            return intersections;

        List<Intersection> valides = new ArrayList<>();
        for (Intersection i : intersections) {
            double x = i.getX();
            double y = i.getY();
            // trop proche des murs
            if (x < margeMur || x > piece.getLargeur() - margeMur)
                continue;
            if (y < margeMur || y > piece.getLongueur() - margeMur)
                continue;
            // TODO: ajouter conditions pour meubles, obstacles, etc.
            // Meubles
            boolean collision = false;
            for (MeubleSansDrainDTO m : piece.getMeubles()) {
                if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                        y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                    collision = true;
                    break;
                }
            }
            if (collision)
                continue;

            for (MeubleDrainDTO m : piece.getMeublesDrain()) {
                if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                        y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                    collision = true;
                    break;
                }
            }
            if (collision)
                continue;
            valides.add(i);
        }
        return valides;
    }

    /**
     * Calcule la distance minimale d'un point aux segments du polygone (murs)
     */
    private double calculerDistanceMinAuMur(double x, double y, List<Point2D.Double> points) {
        if (points.size() < 2)
            return Double.MAX_VALUE;

        double distanceMin = Double.MAX_VALUE;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            Point2D.Double p1 = points.get(i);
            Point2D.Double p2 = points.get((i + 1) % n);

            double distance = distancePointSegment(x, y, p1.x, p1.y, p2.x, p2.y);
            if (distance < distanceMin) {
                distanceMin = distance;
            }
        }

        return distanceMin;
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

    /**
     * Calcule la distance d'un point à un élément chauffant
     */
    private double calculerDistancePointElementChauffant(double x, double y, ElementChauffant element) {
        if (element.isHorizontal()) {
            // Élément horizontal (mur Nord ou Sud)
            // Distance verticale au segment horizontal
            double elementY = element.getY();
            double elementX1 = element.getX();
            double elementX2 = element.getX() + element.getLargeur();

            if (x >= elementX1 && x <= elementX2) {
                // Le point est au-dessus ou en-dessous de l'élément
                return Math.abs(y - elementY);
            } else {
                // Le point est à côté, calculer distance aux coins
                double dist1 = Math.sqrt(Math.pow(x - elementX1, 2) + Math.pow(y - elementY, 2));
                double dist2 = Math.sqrt(Math.pow(x - elementX2, 2) + Math.pow(y - elementY, 2));
                return Math.min(dist1, dist2);
            }
        } else {
            // Élément vertical (mur Est ou Ouest)
            // Distance horizontale au segment vertical
            double elementX = element.getX();
            double elementY1 = element.getY();
            double elementY2 = element.getY() + element.getLargeur();

            if (y >= elementY1 && y <= elementY2) {
                // Le point est à gauche ou à droite de l'élément
                return Math.abs(x - elementX);
            } else {
                // Le point est au-dessus ou en-dessous, calculer distance aux coins
                double dist1 = Math.sqrt(Math.pow(x - elementX, 2) + Math.pow(y - elementY1, 2));
                double dist2 = Math.sqrt(Math.pow(x - elementX, 2) + Math.pow(y - elementY2, 2));
                return Math.min(dist1, dist2);
            }
        }
    }

    /**
     * Calcule la distance d'un point à un rectangle (meuble)
     */
    private double calculerDistancePointRectangle(double px, double py, double rx, double ry,
            double largeur, double hauteur) {
        // Si le point est dans le rectangle, distance = 0
        if (px >= rx && px <= rx + largeur && py >= ry && py <= ry + hauteur) {
            return 0.0;
        }

        // Calculer la distance au rectangle
        double dx = Math.max(rx - px, Math.max(0, px - (rx + largeur)));
        double dy = Math.max(ry - py, Math.max(0, py - (ry + hauteur)));

        return Math.sqrt(dx * dx + dy * dy);
    }

    public List<Intersection> genererCheminFil(List<Intersection> graphe, double longueurFilMax) {
        List<Intersection> chemin = new ArrayList<>();
        if (graphe == null || graphe.isEmpty())
            return chemin;

        // vérification des obstacles
        Set<Intersection> validNodesSet = new HashSet<>(graphe);
        Set<Intersection> visited = new HashSet<>();

        // Le thermostat est le point de départ obligatoire
        Intersection current = null;
        ThermostatDTO thermostat = (controleur.getPieceActive() != null) ? controleur.getPieceActive().getThermostat()
                : null;

        if (thermostat != null) {
            // Créer une intersection exactement à la position du thermostat
            current = new Intersection(thermostat.getX(), thermostat.getY());

            // Trouver l'intersection de grille la plus proche pour continuer
            double minDst = Double.MAX_VALUE;
            Intersection nearestGridPoint = null;
            for (Intersection node : graphe) {
                double dst = Math.pow(node.getX() - thermostat.getX(), 2) +
                        Math.pow(node.getY() - thermostat.getY(), 2);
                if (dst < minDst) {
                    minDst = dst;
                    nearestGridPoint = node;
                }
            }

            // Ajouter le thermostat comme premier point
            chemin.add(current);
            visited.add(current);

            // Continuer avec le point de grille le plus proche
            current = nearestGridPoint;
        } else {
            // Pas de thermostat? Commencer en haut à gauche
            current = graphe.stream()
                    .sorted((a, b) -> {
                        int yComp = Double.compare(b.getY(), a.getY()); // Y en haut
                        if (yComp != 0)
                            return yComp;
                        return Double.compare(a.getX(), b.getX()); // X à gauche
                    })
                    .findFirst()
                    .orElse(null);
        }

        if (current == null)
            return chemin;

        double longueurActuelle = 0;
        int nombreBacktrackings = 0;
        int maxBacktrackings = 50; // Limiter pour éviter les boucles infinies
        int iterationsSansProgres = 0;
        int maxIterationsSansProgres = 100;
        int tailleCheminPrecedente = chemin.size();

        while (current != null) {
            chemin.add(current);
            visited.add(current);

            Intersection next = null;

            Intersection right = current.getRight();
            Intersection left = current.getLeft();
            Intersection down = current.getBottom();
            Intersection up = current.getTop();

            boolean canRight = estVoisinValide(right, validNodesSet, visited);
            boolean canLeft = estVoisinValide(left, validNodesSet, visited);
            boolean canDown = estVoisinValide(down, validNodesSet, visited);
            boolean canUp = estVoisinValide(up, validNodesSet, visited);

            // Priorité: UP -> RIGHT -> DOWN -> LEFT
            // PRIORITÉ 1: UP
            if (canUp) {
                next = up;
            }
            // PRIORITÉ 2: RIGHT
            else if (canRight) {
                next = right;
            }
            // PRIORITÉ 3: DOWN
            else if (canDown) {
                next = down;
            }
            // PRIORITÉ 4: LEFT (en dernier)
            else if (canLeft) {
                next = left;
            }

            // Si aucun voisin disponible, essayer le backtracking
            if (next == null && nombreBacktrackings < maxBacktrackings) {
                double[] longueurRef = { longueurActuelle };
                next = essayerBacktracking(chemin, validNodesSet, visited, longueurRef, longueurFilMax);
                longueurActuelle = longueurRef[0];

                if (next != null) {
                    nombreBacktrackings++;
                    // Mettre à jour le nœud courant si le backtracking a réussi
                    if (!chemin.isEmpty()) {
                        current = chemin.get(chemin.size() - 1);
                    }
                }
            }

            // Vérification distance et longueur
            if (next != null) {
                double dist = Math.abs(next.getX() - current.getX()) + Math.abs(next.getY() - current.getY());

                // Empêche les sauts diagonaux - vérifier que ce n'est pas diagonal
                double distanceGrillePouces = controleur.getDistanceGrillePouces();
                double dx = Math.abs(next.getX() - current.getX());
                double dy = Math.abs(next.getY() - current.getY());
                // Un segment est diagonal si les deux coordonnées changent significativement
                double tolerance = distanceGrillePouces * 0.1;
                if (dx > tolerance && dy > tolerance) {
                    // Segment diagonal détecté, essayer backtracking avant d'arrêter
                    if (nombreBacktrackings < maxBacktrackings) {
                        double[] longueurRef = { longueurActuelle };
                        Intersection backtrackNext = essayerBacktracking(chemin, validNodesSet, visited, longueurRef,
                                longueurFilMax);
                        if (backtrackNext != null) {
                            next = backtrackNext;
                            longueurActuelle = longueurRef[0];
                            nombreBacktrackings++;
                            if (!chemin.isEmpty()) {
                                current = chemin.get(chemin.size() - 1);
                            }
                            // Recalculer la distance après backtracking
                            dist = Math.abs(next.getX() - current.getX()) + Math.abs(next.getY() - current.getY());
                            dx = Math.abs(next.getX() - current.getX());
                            dy = Math.abs(next.getY() - current.getY());
                            if (dx > tolerance && dy > tolerance) {
                                break; // Toujours diagonal après backtracking
                            }
                        } else {
                            break; // Backtracking échoué
                        }
                    } else {
                        break; // Limite de backtracking atteinte
                    }
                }

                if (dist > distanceGrillePouces * 1.5)
                    break;

                if (longueurActuelle + dist > longueurFilMax)
                    break;

                longueurActuelle += dist;
            }

            // Vérifier si on a fait du progrès
            if (chemin.size() > tailleCheminPrecedente) {
                iterationsSansProgres = 0;
                tailleCheminPrecedente = chemin.size();
            } else {
                iterationsSansProgres++;
                if (iterationsSansProgres >= maxIterationsSansProgres) {
                    // Arrêt pour éviter les boucles infinies
                    break;
                }
            }

            current = next;
        }

        return chemin;
    }

    private boolean estVoisinValide(Intersection i, Set<Intersection> validNodes, Set<Intersection> visited) {
        return i != null && validNodes.contains(i) && !visited.contains(i);
    }

    /**
     * Essaie le backtracking : retire le dernier nœud et essaie une autre direction
     * depuis l'avant-dernier nœud pour voir si on peut atteindre d'autres nœuds
     * 
     * @param chemin         Le chemin actuel (sera modifié si le backtracking
     *                       réussit)
     * @param validNodesSet  L'ensemble des nœuds valides
     * @param visited        L'ensemble des nœuds visités (sera modifié si le
     *                       backtracking réussit)
     * @param longueurRef    Référence à la longueur accumulée (sera modifiée si le
     *                       backtracking réussit)
     * @param longueurFilMax La longueur maximale du fil
     * @return Le nouveau nœud trouvé, ou null si le backtracking n'a pas fonctionné
     */
    private Intersection essayerBacktracking(List<Intersection> chemin,
            Set<Intersection> validNodesSet,
            Set<Intersection> visited,
            double[] longueurRef,
            double longueurFilMax) {

        if (chemin.size() < 2) {
            return null; // Pas assez de points pour faire du backtracking
        }

        double distanceGrillePouces = controleur.getDistanceGrillePouces();

        // Essayer de reculer jusqu'à 5 nœuds en arrière pour trouver un nouveau chemin
        // (limité à 5 pour éviter les boucles infinies)
        int maxBacktrack = Math.min(5, chemin.size() - 1);
        List<Intersection> intersectionsRetirees = new ArrayList<>();
        double longueurRetiree = 0.0;

        for (int backtrackLevel = 1; backtrackLevel <= maxBacktrack; backtrackLevel++) {
            if (chemin.size() < 2) {
                break; // Plus assez de points
            }

            // Retirer le dernier nœud temporairement
            Intersection dernierIntersection = chemin.remove(chemin.size() - 1);
            visited.remove(dernierIntersection);
            intersectionsRetirees.add(dernierIntersection);

            // Recalculer la longueur (retirer la distance du dernier segment)
            if (chemin.size() >= 1) {
                Intersection avantDernier = chemin.get(chemin.size() - 1);
                double distDernierSegment = Math.abs(dernierIntersection.getX() - avantDernier.getX()) +
                        Math.abs(dernierIntersection.getY() - avantDernier.getY());
                longueurRetiree += distDernierSegment;
                longueurRef[0] -= distDernierSegment;
            }

            Intersection pointActuel = chemin.get(chemin.size() - 1);

            // Essayer toutes les directions depuis ce point (sauf vers les intersections
            // qu'on vient de retirer)
            // PRIORITÉ: 1-UP, 2-RIGHT, 3-DOWN, 4-LEFT
            Intersection[] voisins = {
                    pointActuel.getTop(), // UP - priorité 1
                    pointActuel.getRight(), // RIGHT - priorité 2
                    pointActuel.getBottom(), // DOWN - priorité 3
                    pointActuel.getLeft() // LEFT - priorité 4
            };

            for (Intersection nouveauVoisin : voisins) {
                if (nouveauVoisin == null) {
                    continue;
                }

                // Ignorer si c'est une des intersections qu'on vient de retirer
                boolean estIntersectionRetiree = false;
                for (Intersection intersectionRetiree : intersectionsRetirees) {
                    if (intersectionsEgales(nouveauVoisin, intersectionRetiree)) {
                        estIntersectionRetiree = true;
                        break;
                    }
                }
                if (estIntersectionRetiree) {
                    continue;
                }

                // Vérifier que ce nouveau voisin est valide
                if (estVoisinValide(nouveauVoisin, validNodesSet, visited)) {
                    // Vérifier la distance et la longueur
                    double dist = Math.abs(nouveauVoisin.getX() - pointActuel.getX()) +
                            Math.abs(nouveauVoisin.getY() - pointActuel.getY());

                    // Vérifier que ce n'est pas diagonal
                    double dx = Math.abs(nouveauVoisin.getX() - pointActuel.getX());
                    double dy = Math.abs(nouveauVoisin.getY() - pointActuel.getY());
                    double tolerance = distanceGrillePouces * 0.1;
                    if (dx > tolerance && dy > tolerance) {
                        continue; // Segment diagonal, ignorer
                    }

                    if (dist > distanceGrillePouces * 1.5) {
                        continue; // Distance trop grande
                    }

                    if (longueurRef[0] + dist > longueurFilMax) {
                        continue; // Dépasserait la longueur max
                    }

                    // On a trouvé un nouveau chemin possible !
                    return nouveauVoisin;
                }
            }
        }

        // Aucune nouvelle direction trouvée, remettre toutes les intersections retirées
        for (int i = intersectionsRetirees.size() - 1; i >= 0; i--) {
            chemin.add(intersectionsRetirees.get(i));
            visited.add(intersectionsRetirees.get(i));
        }
        longueurRef[0] += longueurRetiree;

        return null;
    }

    /**
     * Vérifie si deux intersections sont égales (même position)
     */
    private boolean intersectionsEgales(Intersection i1, Intersection i2) {
        if (i1 == null || i2 == null)
            return false;
        return Math.abs(i1.getX() - i2.getX()) < 0.01 && Math.abs(i1.getY() - i2.getY()) < 0.01;
    }

    private void translaterGraphe(List<Intersection> intersections, double dx, double dy) {
        for (Intersection i : intersections) {
            i.translate(dx, dy);
        }
    }

    public void dessinerGraphe() {
        List<Intersection> intersections = genererGraphe();
        intersections = filtrerIntersections(intersections, 3);
        Graphics2D g2 = null;
        g2.setColor(Color.BLUE);
        int rayon = 4;
        for (Intersection i : intersections) {
            int x = afficheur.poucesVersPixelsX(i.getX());
            int y = afficheur.poucesVersPixelsY(i.getY());
            g2.fillOval(x - rayon, y - rayon, rayon * 2, rayon * 2);

            // Lignes vers les voisins
            if (i.getTop() != null) {
                int yTop = afficheur.poucesVersPixelsY(i.getTop().getY());
                int xTop = afficheur.poucesVersPixelsX(i.getTop().getX());
                g2.drawLine(x, y, xTop, yTop);
            }
            if (i.getRight() != null) {
                int yRight = afficheur.poucesVersPixelsY(i.getRight().getY());
                int xRight = afficheur.poucesVersPixelsX(i.getRight().getX());
                g2.drawLine(x, y, xRight, yRight);
            }
        }
    }

    public void clearGraphe() {
        graphe.clear();
        repaint();
    }

    public List<Intersection> genererCheminFilSerpentin(List<Intersection> graphe, double longueurFilMax) {
        List<Intersection> chemin = new ArrayList<>();
        if (controleur.getPieceActive() == null)
            return chemin;

        // Utiliser le PathFinder pour générer un chemin qui évite les obstacles
        // Utiliser le même espacement que la grille (distanceGrillePouces)
        double distanceGrillePouces = controleur.getDistanceGrillePouces();
        PathFinder pathFinder = new PathFinder(controleur.getPieceActive(), distanceGrillePouces, longueurFilMax);
        List<Point2D.Double> cheminPoints = pathFinder.genererCheminSerpentinAvecEvitement();

        // Convertir les points en intersections
        for (Point2D.Double point : cheminPoints) {
            Intersection inter = new Intersection(point.x, point.y);
            chemin.add(inter);
        }

        // Mettre à jour le fil dans le contrôleur
        controleur.setCheminFil(cheminPoints);

        return chemin;
    }

    private MouseEvent toCartesian(MouseEvent e) {
        return new MouseEvent(
                e.getComponent(),
                e.getID(),
                e.getWhen(),
                e.getModifiersEx(),
                e.getX(),
                getHeight() - e.getY(),
                e.getClickCount(),
                e.isPopupTrigger(),
                e.getButton());
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();

                // mode creation piece irreg
                if (controleur.estEnCreationPieceIrreguliere()) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldPoint.getX(), 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldPoint.getY(), 1.0);

                    // double clic = fermer le polygone
                    if (e.getClickCount() == 2) {
                        controleur.fermerPieceIrreguliere();
                        if (mainWindow != null) {
                            mainWindow.afficherSucces("Pièce irrégulière créée avec succès!");

                        }
                        repaint();
                        return;
                    }

                    // simple clic = ajouter un point
                    controleur.ajouterPointPieceIrreguliere(xPouces, yPouces);
                    repaint();
                    return;
                }

                if (controleur.getModeActuel() == Controleur.Mode.MODELISATION && !cheminFil.isEmpty()) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double worldX = worldPoint.getX();
                    double worldY = worldPoint.getY();

                    // Vérifier si on clique sur un point du fil
                    for (int i = 0; i < cheminFil.size(); i++) {
                        Intersection point = cheminFil.get(i);
                        int pointX = afficheur.poucesVersPixelsX(point.getX());
                        int pointY = afficheur.poucesVersPixelsY(point.getY());

                        double distance = Math.sqrt(
                                Math.pow(worldX - pointX, 2) +
                                        Math.pow(worldY - pointY, 2));

                        if (distance <= FIL_POINT_RADIUS / zoom) {
                            isDraggingFilPoint = true;
                            filPointIndex = i;

                            afficherCheminsDisponibles(point);

                            if (mainWindow != null) {
                                mainWindow.afficherInfo(
                                        "Intersection sélectionnée. Choisissez un chemin parmi les options disponibles.");
                            }
                            repaint();
                            return;
                        }
                    }
                }

                // modofier piece irreg
                if (controleur.estPieceIrreguliere()) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double worldX = worldPoint.getX();
                    double worldY = worldPoint.getY();
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldPoint.getX(), 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldPoint.getY(), 1.0);

                    // VÉRIFIER SI ON EST EN MODE DÉPLACEMENT D'ÉLÉMENT CHAUFFANT
                    if (controleur.estModeDeplacementElementChauffant()) {
                        int indexMur = controleur.trouverMurLePlusProche(xPouces, yPouces);
                        if (indexMur >= 0) {
                            // Mur trouvé, déplacer l'élément chauffant
                            if (controleur.deplacerElementChauffantSurMurIrregulier(indexMur, xPouces, yPouces)) {
                                repaint();
                                if (mainWindow != null) {
                                    mainWindow.afficherSucces("Élément chauffant déplacé avec succès!");
                                    mainWindow.afficherElementChauffantSelectionne();
                                }
                            } else {
                                if (mainWindow != null) {
                                    mainWindow.afficherAvertissement(
                                            "Impossible de déplacer l'élément chauffant : collision détectée.");
                                }
                            }
                        } else {
                            if (mainWindow != null) {
                                mainWindow.afficherAvertissement("Veuillez cliquer sur un mur de la pièce.");
                            }
                        }
                        return;
                    }

                    // VÉRIFIER SI ON EST EN MODE AJOUT D'ÉLÉMENT CHAUFFANT
                    if (controleur.estModeAjoutElementChauffant()) {
                        int indexMur = controleur.trouverMurLePlusProche(xPouces, yPouces);
                        if (indexMur >= 0) {
                            // Mur trouvé, ajouter l'élément chauffant
                            if (controleur.ajouterElementChauffantSurMurIrregulier(indexMur, xPouces, yPouces)) {
                                repaint();
                                if (mainWindow != null) {
                                    mainWindow.afficherSucces("Élément chauffant ajouté avec succès!");
                                    mainWindow.afficherElementChauffantSelectionne();
                                }
                            } else {
                                if (mainWindow != null) {
                                    mainWindow.afficherAvertissement(
                                            "Impossible d'ajouter l'élément chauffant : collision détectée.");
                                }
                            }
                        } else {
                            if (mainWindow != null) {
                                mainWindow.afficherAvertissement("Veuillez cliquer sur un mur de la pièce.");
                            }
                        }
                        return;
                    }

                    // VÉRIFIER LA POIGNÉE EN PREMIER
                    PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                    if (pieceIrreg != null) {
                        double coinXPouces = pieceIrreg.getMaxX();
                        double coinYPouces = pieceIrreg.getMaxY();
                        int coinXPixels = afficheur.poucesVersPixelsX(coinXPouces);
                        int coinYPixels = afficheur.poucesVersPixelsY(coinYPouces);

                        // Si on clique sur la poignée, on commence le redimensionnement
                        if (Math.abs(worldX - coinXPixels) <= POIGNEE_SIZE &&
                                Math.abs(worldY - coinYPixels) <= POIGNEE_SIZE) {

                            System.out.println("POIGNÉE DÉTECTÉE !!!");
                            // Sauvegarder l'état au début du redimensionnement
                            controleur.sauvegarderEtat();
                            controleur.demarrerRedimensionnementPieceIrreguliere();
                            isResizing = true;
                            startPoint = new Point((int) worldX, (int) worldY);
                            startLargeur = pieceIrreg.getLargeur();
                            startLongueur = pieceIrreg.getLongueur();
                            return;
                        }

                        // Le thermostat sera vérifié dans selectionnerElementChauffant (priorité)
                    }

                    if (controleur.selectionnerPointPieceIrreguliere(xPouces, yPouces)) {
                        repaint();
                        if (mainWindow != null) {
                            mainWindow.afficherInfo("Point sélectionné. Glissez pour déplacer, DELETE pour supprimer.");

                        }
                        return;

                    }
                    // si on clique ailleurs, deselectionner et désactiver les modes
                    controleur.deselectionnerPointPieceIrreguliere();
                    controleur.desactiverModeAjoutElementChauffant();
                    controleur.desactiverModeDeplacementElementChauffant();
                    mainWindow.afficherInfo("");
                    repaint();

                }
                // e = toCartesian(e);
                // bloquer l'édition si en mode modelisation
                if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
                    return; // ne rien faire
                }
                Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                double worldX = worldPoint.getX();
                double worldY = worldPoint.getY();

                PieceDTO piece = controleur.getPiece();

                // Si pas de pièce rectangulaire, utiliser la bounding box de la pièce
                // irrégulière
                double coinXPouces, coinYPouces;
                if (piece != null) {
                    coinXPouces = piece.getX() + piece.getLargeur();
                    coinYPouces = piece.getY() + piece.getLongueur();
                    startLargeur = piece.getLargeur();
                    startLongueur = piece.getLongueur();
                } else if (controleur.estPieceIrreguliere()) {
                    PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                    if (pieceIrreg == null)
                        return;
                    coinXPouces = pieceIrreg.getMaxX();
                    coinYPouces = pieceIrreg.getMaxY();
                    startLargeur = pieceIrreg.getLargeur();
                    startLongueur = pieceIrreg.getLongueur();
                } else {
                    return;
                }

                int coinXPixels = afficheur.poucesVersPixelsX(coinXPouces);
                int coinYPixels = afficheur.poucesVersPixelsY(coinYPouces);

                // Redimensionnement (même logique pour les deux types)
                if (Math.abs(worldX - coinXPixels) <= POIGNEE_SIZE &&
                        Math.abs(worldY - coinYPixels) <= POIGNEE_SIZE) {

                    if (controleur.estPieceIrreguliere()) {
                        // Sauvegarder l'état au début du redimensionnement
                        controleur.sauvegarderEtat();
                        controleur.demarrerRedimensionnementPieceIrreguliere();
                    } else {
                        // Sauvegarder l'état au début du redimensionnement pour pièce régulière
                        controleur.sauvegarderEtat();
                    }

                    isResizing = true;
                    startPoint = new Point((int) worldX, (int) worldY);
                    return;
                }

                double xPouces = afficheur.pixelsVersPoucesX((int) worldX, 1.0);
                double yPouces = afficheur.pixelsVersPoucesY((int) worldY, 1.0);

                controleur.deselectionnerElementChauffant();
                controleur.deselectionnerMeuble();
                // Désactiver les modes d'ajout et de déplacement
                controleur.desactiverModeAjoutElementChauffant();
                controleur.desactiverModeDeplacementElementChauffant();
                // --- Sélection du thermostat s'il existe ---
                PieceDTO pieceDTO = controleur.getPiece();
                if (pieceDTO != null && pieceDTO.getThermostat() != null) {
                    ThermostatDTO th = pieceDTO.getThermostat();
                    double tx = th.getX();
                    double ty = th.getY();
                    double tw = th.getLargeur();
                    double thh = th.getHauteur();

                    // Test : clic dans le rectangle du thermostat (en pouces)
                    if (xPouces >= tx && xPouces <= tx + tw &&
                            yPouces >= ty && yPouces <= ty + thh) {
                        // Sauvegarder l'état au début du drag
                        controleur.sauvegarderEtat();
                        isDraggingThermostatRect = true;
                        thermostatOffsetX = xPouces - tx;
                        thermostatOffsetY = yPouces - ty;
                        thermostatXAvantDrag = tx;
                        thermostatYAvantDrag = ty;
                        return; // on ne gère pas les meubles, etc. dans ce cas
                    }
                }

                // Sélection élément chauffant (inclut le thermostat pour les pièces
                // irrégulières)
                // D'abord, vérifier les éléments chauffants (ils ont priorité, thermostat
                // inclus)
                // Ne pas sélectionner si on est en mode ajout ou déplacement
                if (!controleur.estModeAjoutElementChauffant() && !controleur.estModeDeplacementElementChauffant()) {
                    controleur.selectionnerElementChauffant(xPouces, yPouces);
                    if (controleur.getElementSelectionne() != null) {
                        controleur.deselectionnerMeuble(); // Désélectionner tout meuble sélectionné
                        // Désactiver les modes d'ajout et de déplacement lors de la sélection
                        controleur.desactiverModeAjoutElementChauffant();
                        controleur.desactiverModeDeplacementElementChauffant();
                        // Sauvegarder la position initiale pour restauration en cas de collision
                        ElementChauffant element = controleur.getElementSelectionne();
                        elementChauffantXAvantDrag = element.getX();
                        elementChauffantYAvantDrag = element.getY();
                        elementChauffantAngleAvantDrag = element.getAngle();

                        // Si c'est le thermostat, utiliser isDraggingThermostat au lieu de
                        // isDraggingHeatingElement
                        if (Thermostat.estThermostat(element)) {
                            isDraggingThermostat = true;
                            thermostatXAvantDrag = element.getX();
                            thermostatYAvantDrag = element.getY();
                            thermostatAngleAvantDrag = element.getAngle();
                        } else {
                            isDraggingHeatingElement = true;
                        }

                        // Ne PAS sauvegarder l'état au début du drag - on sauvegardera seulement à la
                        // fin si la position change
                        repaint();
                        if (mainWindow != null) {
                            if (Thermostat.estThermostat(element)) {
                                // Afficher les infos du thermostat si nécessaire
                            } else {
                                mainWindow.afficherElementChauffantSelectionne();
                            }
                        }
                        return;
                    }
                }

                // Ensuite, utiliser la méthode UNIFIÉE pour sélectionner les meubles
                controleur.selectionnerMeubleUnifie(xPouces, yPouces);

                // Vérifier si un meuble avec drain est sélectionné
                if (controleur.getMeubleAvecDrainSelectionne() != null) {
                    // Sauvegarder l'état au début du drag
                    controleur.sauvegarderEtat();
                    isDraggingElement = true;
                    repaint();
                    if (mainWindow != null) {
                        if (controleur.isDrainSelectionne()) {
                            mainWindow.afficherInfo("Drain sélectionné. Glissez pour déplacer.");
                        } else {
                            mainWindow.afficherMeubleAvecDrainSelectionne();
                        }
                    }
                    return;
                }

                // Vérifier si un meuble sans drain est sélectionné
                if (controleur.getMeubleSelectionne() != null) {
                    // Sauvegarder l'état au début du drag
                    controleur.sauvegarderEtat();
                    isDraggingElement = true;
                    repaint();
                    if (mainWindow != null)
                        mainWindow.afficherMeubleSelectionne();
                    return;
                }
                // Le thermostat sera vérifié dans selectionnerElementChauffant (priorité)

                if (controleur.getElementSelectionne() != null) {
                    controleur.deselectionnerMeuble();
                    // Sauvegarder la position initiale pour restauration en cas de collision
                    ElementChauffant element = controleur.getElementSelectionne();
                    elementChauffantXAvantDrag = element.getX();
                    elementChauffantYAvantDrag = element.getY();
                    elementChauffantAngleAvantDrag = element.getAngle();

                    // Si c'est le thermostat, utiliser isDraggingThermostat
                    if (Thermostat.estThermostat(element)) {
                        isDraggingThermostat = true;
                        thermostatXAvantDrag = element.getX();
                        thermostatYAvantDrag = element.getY();
                        thermostatAngleAvantDrag = element.getAngle();
                    } else {
                        isDraggingHeatingElement = true;
                    }

                    // Ne PAS sauvegarder l'état au début du drag - on sauvegardera seulement à la
                    // fin si la position change
                    repaint();
                    if (mainWindow != null) {
                        if (!Thermostat.estThermostat(element)) {
                            mainWindow.afficherElementChauffantSelectionne();
                        }
                    }
                    return;
                }
                repaint();
                if (mainWindow != null) {
                    mainWindow.reinitialiserPanneauEdition();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // 1) THERMOSTAT RÉGULIER
                if (isDraggingThermostatRect) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldPoint.getX(), 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldPoint.getY(), 1.0);

                    // Utiliser deplacerThermostatSnap pour que le thermostat snap sur les murs
                    controleur.deplacerThermostatSnap(xPouces, yPouces);

                    repaint();
                    return;
                }

                // 2) THERMOSTAT IRRÉGULIER (utilise la même logique que les éléments
                // chauffants)
                if (isDraggingThermostat && controleur.getElementSelectionne() != null &&
                        Thermostat.estThermostat(controleur.getElementSelectionne())) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldPoint.getX(), 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldPoint.getY(), 1.0);

                    // Utiliser la même méthode de drag que les éléments chauffants
                    if (controleur.estPieceIrreguliere()) {
                        controleur.deplacerElementChauffantDragIrregulier(xPouces, yPouces);
                    }

                    repaint();
                    return;
                }

                // e = toCartesian(e);
                if (isDraggingFilPoint && filPointIndex >= 0 && filPointIndex < cheminFil.size()) {
                    Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldPoint.getX(), 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldPoint.getY(), 1.0);

                    // Vérifier qu'on a une pièce (régulière ou irrégulière)
                    PieceDTO piece = controleur.getPiece();
                    PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                    if (piece == null && pieceIrreg == null)
                        return;

                    Intersection closestIntersection = trouverIntersectionLaPlusProche(xPouces, yPouces);

                    if (closestIntersection == null) {
                        if (mainWindow != null) {
                            mainWindow.afficherAvertissement("Aucune intersection valide trouvée!");
                        }
                        return;
                    }

                    // Utiliser les coordonnées de l'intersection existante
                    xPouces = closestIntersection.getX();
                    yPouces = closestIntersection.getY();

                    // Vérifier que c'est une nouvelle position
                    Intersection currentPoint = cheminFil.get(filPointIndex);
                    if (Math.abs(xPouces - currentPoint.getX()) < 0.1 &&
                            Math.abs(yPouces - currentPoint.getY()) < 0.1) {
                        return; // Pas de changement
                    }

                    List<Intersection> nouveauChemin = recalculerCheminAvecIntersection(
                            filPointIndex, closestIntersection);

                    if (nouveauChemin != null && !nouveauChemin.isEmpty()) {
                        cheminFil = nouveauChemin;

                        // Mettre à jour le chemin dans le contrôleur pour validation
                        List<Point2D.Double> cheminPoints = new ArrayList<>();
                        for (Intersection i : cheminFil) {
                            cheminPoints.add(new Point2D.Double(i.getX(), i.getY()));
                        }
                        controleur.setCheminFil(cheminPoints);

                        // Vérifier les erreurs de validation
                        List<String> erreurs = controleur.getErreursValidation();
                        if (!erreurs.isEmpty() && mainWindow != null) {
                            // Afficher les erreurs
                            StringBuilder messageErreurs = new StringBuilder("Erreurs du fil: ");
                            for (int i = 0; i < erreurs.size() && i < 3; i++) {
                                if (i > 0)
                                    messageErreurs.append("; ");
                                messageErreurs.append(erreurs.get(i));
                            }
                            if (erreurs.size() > 3) {
                                messageErreurs.append("... (").append(erreurs.size()).append(" erreurs totales)");
                            }
                            mainWindow.afficherErreur(messageErreurs.toString());
                        } else if (mainWindow != null) {
                            mainWindow.afficherInfo("Chemin recalculé avec succès!");
                        }
                    } else {
                        if (mainWindow != null) {
                            mainWindow.afficherAvertissement(
                                    "Impossible de recalculer le chemin avec cette intersection! Le chemin serait invalide.");
                        }
                    }

                    repaint();
                    return;
                }

                // bloquer l'édition si en mode modelisation
                if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
                    return; // ne rien faire
                }
                Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                double worldX = worldPoint.getX();
                double worldY = worldPoint.getY();
                // deplacer point piece irreg
                if (controleur.estPieceIrreguliere() && controleur.getPointSelectionneIndex() >= 0) {
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldX, 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldY, 1.0);
                    controleur.deplacerPointSelectionne(xPouces, yPouces);
                    repaint();
                    return;
                }

                if (isResizing) {
                    int deltaXPixels = (int) worldX - startPoint.x;
                    int deltaYPixels = (int) worldY - startPoint.y;

                    double deltaXPouces = afficheur.pixelsVersPoucesX(deltaXPixels, 1.0);
                    double deltaYPouces = afficheur.pixelsVersPoucesY(deltaYPixels, 1.0);

                    double nouvelleLargeur = startLargeur + deltaXPouces;
                    double nouvelleLongueur = startLongueur + deltaYPouces;

                    if (nouvelleLargeur >= 12.0 && nouvelleLongueur >= 12.0) {

                        // Appeler la bonne méthode selon le type de pièce (ne pas sauvegarder pendant
                        // le drag)
                        if (controleur.estPieceIrreguliere()) {
                            double facteurX = nouvelleLargeur / startLargeur;
                            double facteurY = nouvelleLongueur / startLongueur;
                            controleur.redimensionnerPieceIrreguliere(facteurX, facteurY);
                        } else {
                            boolean meubleRepositionne = controleur.redimensionnerPiece(nouvelleLargeur,
                                    nouvelleLongueur, false);
                            if (mainWindow != null) {
                                mainWindow.updateTailleFields();
                                mainWindow.updatePositionFields();
                                if (meubleRepositionne) {
                                    mainWindow.afficherAvertissement(
                                            "Certains meubles ont été replacés automatiquement à l'intérieur de la pièce.");
                                }
                            }
                        }

                        repaint();
                    }
                    return;
                }
                double xPouces = afficheur.pixelsVersPoucesX((int) worldX, 1.0);
                double yPouces = afficheur.pixelsVersPoucesY((int) worldY, 1.0);
                // --- Déplacement du thermostat, collé au mur le plus proche ---
                /**
                 * if (isDraggingThermostat) {
                 * PieceDTO pieceDTO = controleur.getPiece();
                 * if (pieceDTO != null && pieceDTO.getThermostat() != null) {
                 * ThermostatDTO th = pieceDTO.getThermostat();
                 * 
                 * double largeurTh = th.getLargeur();
                 * double hauteurTh = th.getHauteur();
                 * 
                 * double pieceX = pieceDTO.getX();
                 * double pieceY = pieceDTO.getY();
                 * double pieceLargeur = pieceDTO.getLargeur();
                 * double pieceLongueur = pieceDTO.getLongueur();
                 * 
                 * // Coordonnées des murs (limites POUR le thermostat)
                 * double leftX = pieceX;
                 * double rightX = pieceX + pieceLargeur - largeurTh;
                 * double bottomY = pieceY;
                 * double topY = pieceY + pieceLongueur - hauteurTh;
                 * 
                 * // Distances du curseur aux 4 murs
                 * double distGauche = Math.abs(xPouces - pieceX);
                 * double distDroite = Math.abs(xPouces - (pieceX + pieceLargeur));
                 * double distBas = Math.abs(yPouces - pieceY);
                 * double distHaut = Math.abs(yPouces - (pieceY + pieceLongueur));
                 * 
                 * // Choisir le mur le plus proche
                 * double minDist = distGauche;
                 * String mur = "GAUCHE";
                 * 
                 * if (distDroite < minDist) {
                 * minDist = distDroite;
                 * mur = "DROITE";
                 * }
                 * if (distBas < minDist) {
                 * minDist = distBas;
                 * mur = "BAS";
                 * }
                 * if (distHaut < minDist) {
                 * minDist = distHaut;
                 * mur = "HAUT";
                 * }
                 * 
                 * double newX = th.getX();
                 * double newY = th.getY();
                 * 
                 * switch (mur) {
                 * case "GAUCHE":
                 * newX = leftX;
                 * newY = yPouces - hauteurTh / 2.0;
                 * if (newY < bottomY) newY = bottomY;
                 * if (newY > topY) newY = topY;
                 * break;
                 * case "DROITE":
                 * newX = rightX;
                 * newY = yPouces - hauteurTh / 2.0;
                 * if (newY < bottomY) newY = bottomY;
                 * if (newY > topY) newY = topY;
                 * break;
                 * case "BAS":
                 * newY = bottomY;
                 * newX = xPouces - largeurTh / 2.0;
                 * if (newX < leftX) newX = leftX;
                 * if (newX > rightX) newX = rightX;
                 * break;
                 * case "HAUT":
                 * newY = topY;
                 * newX = xPouces - largeurTh / 2.0;
                 * if (newX < leftX) newX = leftX;
                 * if (newX > rightX) newX = rightX;
                 * break;
                 * }
                 * 
                 * // Mise à jour de la position du thermostat (ne pas sauvegarder pendant le
                 * drag)
                 * controleur.ajouterThermostat(newX, newY, largeurTh, hauteurTh, false);
                 * repaint();
                 * }
                 * return; // ✅ on ne gère pas les meubles si on déplace le thermostat
                 * }
                 */

                if (isDraggingHeatingElement && controleur.getElementSelectionne() != null) {
                    // Vérifier si c'est une pièce irrégulière
                    if (controleur.estPieceIrreguliere()) {
                        // Pour les pièces irrégulières, utiliser la méthode de drag spéciale
                        // qui détecte le mur le plus proche et adapte l'orientation
                        controleur.deplacerElementChauffantDragIrregulier(xPouces, yPouces);
                    } else {
                        // Pour les pièces régulières, utiliser la méthode existante
                        // Ne pas sauvegarder pendant le drag, seulement à la fin si la position change
                        controleur.deplacerElementChauffant(xPouces, yPouces, false);
                    }

                    repaint();
                    if (mainWindow != null) {
                        mainWindow.afficherElementChauffantSelectionne();
                    }
                    return;
                }

                if (isDraggingElement) {

                    // Ne pas sauvegarder pendant le drag, seulement au début
                    String resultat = controleur.deplacerMeubleSelectionne(xPouces, yPouces, false);

                    repaint();
                    if (mainWindow != null) {
                        mainWindow.afficherMeubleSelectionne();
                        mainWindow.afficherMeubleAvecDrainSelectionne();
                    }
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Valider la position finale de l'élément chauffant pour les pièces
                // irrégulières
                if (isDraggingHeatingElement && controleur.getElementSelectionne() != null
                        && controleur.estPieceIrreguliere()) {
                    ElementChauffant element = controleur.getElementSelectionne();
                    // Vérifier si la position a changé
                    boolean positionChangee = Math.abs(element.getX() - elementChauffantXAvantDrag) > 0.01 ||
                            Math.abs(element.getY() - elementChauffantYAvantDrag) > 0.01 ||
                            Math.abs(element.getAngle() - elementChauffantAngleAvantDrag) > 0.001;

                    if (!controleur.validerPositionElementChauffantDragIrregulier()) {
                        // Collision détectée : restaurer la position précédente
                        element.setX(elementChauffantXAvantDrag);
                        element.setY(elementChauffantYAvantDrag);
                        element.setAngle(elementChauffantAngleAvantDrag);
                        if (mainWindow != null) {
                            mainWindow.afficherAvertissement(
                                    "Impossible de déplacer l'élément chauffant : collision détectée.");
                        }
                    } else if (positionChangee) {
                        // Position valide et a changé : sauvegarder l'état (de la position initiale à
                        // la position finale)
                        controleur.sauvegarderEtat();
                        if (mainWindow != null) {
                            mainWindow.afficherSucces("Élément chauffant déplacé avec succès!");
                        }
                    }
                    // Si la position n'a pas changé, ne rien sauvegarder (pas besoin d'undo/redo)
                    repaint();
                } else if (isDraggingHeatingElement && controleur.getElementSelectionne() != null
                        && !controleur.estPieceIrreguliere()) {
                    // Pour les pièces régulières, vérifier si la position a changé
                    ElementChauffant element = controleur.getElementSelectionne();
                    boolean positionChangee = Math.abs(element.getX() - elementChauffantXAvantDrag) > 0.01 ||
                            Math.abs(element.getY() - elementChauffantYAvantDrag) > 0.01;

                    if (positionChangee) {
                        // Position a changé : sauvegarder l'état (de la position initiale à la position
                        // finale)
                        controleur.sauvegarderEtat();
                    }
                    // Si la position n'a pas changé, ne rien sauvegarder
                }

                // Valider la position finale du thermostat pour les pièces régulières
                if (isDraggingThermostatRect && !controleur.estPieceIrreguliere()) {
                    PieceDTO pieceDTO = controleur.getPiece();
                    if (pieceDTO != null && pieceDTO.getThermostat() != null) {
                        ThermostatDTO th = pieceDTO.getThermostat();
                        // Vérifier si la position a changé
                        boolean positionChangee = Math.abs(th.getX() - thermostatXAvantDrag) > 0.01 ||
                                Math.abs(th.getY() - thermostatYAvantDrag) > 0.01;

                        if (positionChangee) {
                            // Position a changé : sauvegarder l'état (de la position initiale à la position
                            // finale)
                            controleur.sauvegarderEtat();
                            if (mainWindow != null) {
                                mainWindow.afficherSucces("Thermostat déplacé avec succès!");
                            }
                        }
                        // Si la position n'a pas changé, ne rien sauvegarder (pas besoin d'undo/redo)
                    }
                }

                // Valider la position finale du thermostat pour les pièces irrégulières
                if (isDraggingThermostat && controleur.getElementSelectionne() != null &&
                        Thermostat.estThermostat(controleur.getElementSelectionne()) &&
                        controleur.estPieceIrreguliere()) {
                    ElementChauffant element = controleur.getElementSelectionne();
                    // Vérifier si la position a changé
                    boolean positionChangee = Math.abs(element.getX() - thermostatXAvantDrag) > 0.01 ||
                            Math.abs(element.getY() - thermostatYAvantDrag) > 0.01 ||
                            Math.abs(element.getAngle() - thermostatAngleAvantDrag) > 0.001;

                    if (positionChangee) {
                        // Position a changé : sauvegarder l'état (de la position initiale à la position
                        // finale)
                        controleur.sauvegarderEtat();
                        if (mainWindow != null) {
                            mainWindow.afficherSucces("Thermostat déplacé avec succès!");
                        }
                    }
                    // Si la position n'a pas changé, ne rien sauvegarder (pas besoin d'undo/redo)
                }

                isResizing = false;
                isDraggingElement = false;
                isDraggingHeatingElement = false;
                isDraggingThermostat = false;
                isDraggingThermostatRect = false;

                // Le message de succès pour le changement de direction est déjà géré dans
                // mouseDragged
                // Ici on nettoie juste les flags
                isDraggingFilPoint = false;
                filPointIndex = -1;
                cheminsDisponibles.clear();
                controleur.terminerRedimensionnementPieceIrreguliere();
                setCursor(Cursor.getDefaultCursor());
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                // e = toCartesian(e);
                Point2D worldPoint = screenToWorld(e.getX(), e.getY());
                double worldX = worldPoint.getX();
                double worldY = worldPoint.getY();

                if (mainWindow != null) {
                    double xPouces = afficheur.pixelsVersPoucesX((int) worldX, 1.0);
                    double yPouces = afficheur.pixelsVersPoucesY((int) worldY, 1.0);

                    // Si c'est une pièce irrégulière, calculer les coordonnées par rapport au coin
                    // bas gauche de la bounding box
                    if (controleur.estPieceIrreguliere()) {
                        PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                        if (pieceIrreg != null) {
                            double minX = pieceIrreg.getMinX();
                            double minY = pieceIrreg.getMinY();
                            // Coordonnées relatives au coin bas gauche de la bounding box
                            xPouces = xPouces - minX;
                            yPouces = yPouces - minY;
                        }
                    } else {
                        // Pour les pièces régulières, utiliser le coin bas gauche de la pièce comme
                        // origine
                        PieceDTO piece = controleur.getPiece();
                        if (piece != null) {
                            xPouces = xPouces - piece.getX();
                            yPouces = yPouces - piece.getY();
                        }
                    }

                    mainWindow.afficherCoordonnees(xPouces, yPouces);
                }

                // Modélisation manuelle: montrer le curseur de main si sur une intersection
                if (controleur.getModeActuel() == Controleur.Mode.MODELISATION && !cheminFil.isEmpty()) {
                    for (Intersection point : cheminFil) {
                        int pointX = afficheur.poucesVersPixelsX(point.getX());
                        int pointY = afficheur.poucesVersPixelsY(point.getY());

                        double distance = Math.sqrt(
                                Math.pow(worldX - pointX, 2) +
                                        Math.pow(worldY - pointY, 2));

                        if (distance <= FIL_POINT_RADIUS / zoom) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                            return;
                        }
                    }
                }

                // Curseur de redimensionnement pour pièce irrégulière
                if (controleur.estPieceIrreguliere()) {
                    PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
                    if (pieceIrreg != null) {
                        double coinXPouces = pieceIrreg.getMaxX();
                        double coinYPouces = pieceIrreg.getMaxY();
                        int coinXPixels = afficheur.poucesVersPixelsX(coinXPouces);
                        int coinYPixels = afficheur.poucesVersPixelsY(coinYPouces);

                        if (Math.abs(worldX - coinXPixels) <= POIGNEE_SIZE &&
                                Math.abs(worldY - coinYPixels) <= POIGNEE_SIZE) {
                            setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                            return;
                        }
                    }
                }

                PieceDTO piece = controleur.getPiece();
                if (piece == null)
                    return;

                double coinXPouces = piece.getX() + piece.getLargeur();
                double coinYPouces = piece.getY() + piece.getLongueur();
                int coinXPixels = afficheur.poucesVersPixelsX(coinXPouces);
                int coinYPixels = afficheur.poucesVersPixelsY(coinYPouces);

                if (Math.abs(worldX - coinXPixels) <= POIGNEE_SIZE &&
                        Math.abs(worldY - coinYPixels) <= POIGNEE_SIZE) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {

                zoomPointX = e.getX();
                zoomPointY = e.getY();

                if (e.getPreciseWheelRotation() < 0) {
                    zoom *= 1.1; // Zoom IN
                } else {
                    zoom /= 1.1; // Zoom OUT
                }

                // Pas de limite - zoom à l'infini
                if (zoom < 0.01) {
                    zoom = 0.01;
                }

                repaint();
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
        addMouseWheelListener(mouseAdapter);
    }

    private boolean verifierCollision(Intersection point, PieceDTO piece) {
        if (piece == null)
            return false;

        double x = point.getX();
        double y = point.getY();

        // Vérifier collision avec meubles sans drain
        for (MeubleSansDrainDTO m : piece.getMeubles()) {
            if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                    y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                return true;
            }
        }

        // Vérifier collision avec meubles avec drain
        for (MeubleDrainDTO m : piece.getMeublesDrain()) {
            if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                    y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                return true;
            }
        }

        return false;
    }

    private void dessinerThermostat(Graphics2D g2, PieceDTO pieceDTO) {
        if (pieceDTO == null)
            return;

        ThermostatDTO t = pieceDTO.getThermostat();
        if (t == null)
            return;

        // Coordonnées du thermostat en pixels
        int x = afficheur.poucesVersPixelsX(t.getX());
        int y = afficheur.poucesVersPixelsY(t.getY());
        int w = afficheur.poucesVersPixelsX(t.getLargeur());
        int h = afficheur.poucesVersPixelsY(t.getHauteur());

        int minW = 50;
        int minH = 30;
        if (w < minW)
            w = minW;
        if (h < minH)
            h = minH;

        // Fond blanc
        g2.setColor(Color.WHITE);
        g2.fillRect(x, y, w, h);

        // Contour noir
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2f));
        g2.drawRect(x, y, w, h);

        // Texte non inversé
        AffineTransform old = g2.getTransform();
        g2.setTransform(new AffineTransform());

        String label = "Thermostat";
        Font font = g2.getFont().deriveFont(8f);
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();

        int textWidth = fm.stringWidth(label);
        int textHeight = fm.getAscent();

        int textX = x + (w - textWidth) / 2;
        int textY = (getHeight() - y) - (h - textHeight) / 2;

        g2.setColor(Color.BLACK);
        g2.drawString(label, textX, textY);

        g2.setTransform(old);
    }

    private void dessinerThermostat(Graphics2D g2, PieceIrreguliereDTO pieceDTO) {
        if (pieceDTO == null)
            return;

        ThermostatDTO t = pieceDTO.getThermostat();
        if (t == null)
            return;

        // Utiliser la même logique de dessin que les éléments chauffants
        // Créer un ElementChauffant temporaire pour le dessin
        ElementChauffant thermostatTemp = new ElementChauffant(
                t.getX(), t.getY(), t.getLargeur(), t.getHauteur(), true, t.getAngle());

        boolean isSelected = (controleur.getElementSelectionne() != null &&
                Thermostat.estThermostat(controleur.getElementSelectionne()) &&
                controleur.getElementSelectionne().getX() == t.getX() &&
                controleur.getElementSelectionne().getY() == t.getY());

        // Le thermostat a toujours la couleur blanche avec bordure noire
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2 / (float) zoom));

        int xPixels = afficheur.poucesVersPixelsX(thermostatTemp.getX());
        int yPixels = afficheur.poucesVersPixelsY(thermostatTemp.getY());
        int largeurPixels = afficheur.poucesVersPixelsX(thermostatTemp.getLargeur());
        int hauteurPixels = afficheur.poucesVersPixelsY(thermostatTemp.getHauteur());

        // Sauvegarder la transformation actuelle
        AffineTransform oldTransform = g2.getTransform();

        // Si l'angle est significatif, appliquer la rotation (même logique que les
        // éléments chauffants)
        if (Math.abs(thermostatTemp.getAngle()) > 0.001) {
            double cosAngle = Math.cos(thermostatTemp.getAngle());
            double sinAngle = Math.sin(thermostatTemp.getAngle());

            double dxLocal = largeurPixels / 2.0;
            double dyLocal = hauteurPixels / 2.0;

            double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
            double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;

            double centreX = xPixels + dxGlobal;
            double centreY = yPixels + dyGlobal;

            g2.translate(centreX, centreY);
            g2.rotate(thermostatTemp.getAngle());
            g2.translate(-largeurPixels / 2.0, -hauteurPixels / 2.0);

            // Dessiner le rectangle
            g2.fillRect(0, 0, largeurPixels, hauteurPixels);

            // Bordure noire et texte
            g2.setColor(Color.BLACK);
            g2.drawRect(0, 0, largeurPixels, hauteurPixels);

            String label = "Thermostat";
            Font font = g2.getFont().deriveFont(8f);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int textX = (largeurPixels - textWidth) / 2;
            int textY = (hauteurPixels + textHeight) / 2;
            g2.drawString(label, textX, textY);

            g2.setTransform(oldTransform);
        } else {
            // Pas de rotation : dessiner normalement
            g2.fillRect(xPixels, yPixels, largeurPixels, hauteurPixels);

            g2.setColor(Color.BLACK);
            g2.drawRect(xPixels, yPixels, largeurPixels, hauteurPixels);

            // Texte
            AffineTransform old = g2.getTransform();
            g2.setTransform(new AffineTransform());
            String label = "Thermostat";
            Font font = g2.getFont().deriveFont(8f);
            g2.setFont(font);
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(label);
            int textHeight = fm.getAscent();
            int textX = xPixels + (largeurPixels - textWidth) / 2;
            int textY = (getHeight() - yPixels) - (hauteurPixels - textHeight) / 2;
            g2.drawString(label, textX, textY);
            g2.setTransform(old);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        /*
         * //AJOUTER LA TRANSLATION POUR LE ZOOM (AVANT L'INVERSION Y)
         * g2.translate(afficheur.getOffsetX(), afficheur.getOffsetY());
         * 
         * g2.translate(0, getHeight());
         * g2.scale(1, -1);
         */

        // -------------------GRILLE----------------
        g2.setColor(Color.LIGHT_GRAY);

        // Conversion pouces → pixels en utilisant ton afficheur
        double distanceGrillePouces = controleur.getDistanceGrillePouces();
        int cellSizeX = afficheur.poucesVersPixelsX(distanceGrillePouces);
        int cellSizeY = afficheur.poucesVersPixelsY(distanceGrillePouces);

        // version avec zoom de la grille :
        // int cellSizeX = afficheur.poucesVersPixelsX(distanceGrillePouces/zoom);
        // int cellSizeY = afficheur.poucesVersPixelsY(distanceGrillePouces/zoom);

        // Sécurité si zoom très petit
        if (cellSizeX < 5)
            cellSizeX = 5;
        if (cellSizeY < 5)
            cellSizeY = 5;

        // Dessin vertical
        for (int x = 0; x < getWidth(); x += cellSizeX) {
            g2.drawLine(x, 0, x, getHeight());
        }

        // Dessin horizontal
        for (int y = 0; y < getHeight(); y += cellSizeY) {
            g2.drawLine(0, y, getWidth(), y);
        }

        AffineTransform at = g2.getTransform();
        // Inversion Y
        at.translate(0, getHeight());
        at.scale(1, -1);

        // Zoom autour du point (adapter Y pour coordonnées cartésiennes)
        int zoomPointYCartesian = getHeight() - zoomPointY;
        at.translate(zoomPointX, zoomPointYCartesian);
        at.scale(zoom, zoom);
        at.translate(-zoomPointX, -zoomPointYCartesian);

        g2.setTransform(at);

        // afficher piece irreg en cours de creation
        PieceIrreguliereDTO pieceEnCours = controleur.getPieceIrreguliereEnCours();
        if (pieceEnCours != null && pieceEnCours.getNombrePoints() > 0) {
            List<Point2D.Double> points = pieceEnCours.getPoints();

            g2.setColor(new Color(100, 150, 255));
            g2.setStroke(new BasicStroke(2 / (float) zoom));

            for (int i = 0; i < points.size() - 1; i++) {
                int x1 = afficheur.poucesVersPixelsX(points.get(i).x);
                int y1 = afficheur.poucesVersPixelsY(points.get(i).y);
                int x2 = afficheur.poucesVersPixelsX(points.get(i + 1).x);
                int y2 = afficheur.poucesVersPixelsY(points.get(i + 1).y);
                g2.drawLine(x1, y1, x2, y2);
            }

            // ligne de fermeture (du dernier au 1er point)
            if (points.size() >= 2) {
                g2.setStroke(new BasicStroke(1 / (float) zoom, BasicStroke.CAP_BUTT,
                        BasicStroke.JOIN_MITER, 10.0f,
                        new float[] { 5.0f }, 0.0f)); // Pointillés
                g2.setColor(new Color(150, 150, 150, 150)); // Gris transparent

                int xFirst = afficheur.poucesVersPixelsX(points.get(0).x);
                int yFirst = afficheur.poucesVersPixelsY(points.get(0).y);
                int xLast = afficheur.poucesVersPixelsX(points.get(points.size() - 1).x);
                int yLast = afficheur.poucesVersPixelsY(points.get(points.size() - 1).y);
                g2.drawLine(xLast, yLast, xFirst, yFirst);
            }

            // dessiner les points
            g2.setColor(Color.RED);
            for (Point2D.Double point : points) {
                int x = afficheur.poucesVersPixelsX(point.x);
                int y = afficheur.poucesVersPixelsY(point.y);
                int taille = (int) (8 / zoom);
                g2.fillOval(x - taille / 2, y - taille / 2, taille, taille);
            }

            // cercle blanc au centre pour mieux voir
            g2.setColor(Color.WHITE);
            for (Point2D.Double point : points) {
                int x = afficheur.poucesVersPixelsX(point.x);
                int y = afficheur.poucesVersPixelsY(point.y);
                int taille = (int) (4 / zoom);
                g2.fillOval(x - taille / 2, y - taille / 2, taille, taille);
            }

        }

        // afficher piece irreg finalisée
        PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
        if (pieceIrreg != null && pieceIrreg.estFermee()) {
            List<Point2D.Double> points = pieceIrreg.getPoints();

            int[] xPoints = new int[points.size()];
            int[] yPoints = new int[points.size()];

            for (int i = 0; i < points.size(); i++) {
                xPoints[i] = afficheur.poucesVersPixelsX(points.get(i).x);
                yPoints[i] = afficheur.poucesVersPixelsY(points.get(i).y);

            }
            g2.setColor(new Color(200, 230, 255, 80));
            g2.fillPolygon(xPoints, yPoints, points.size());

            g2.setColor(new Color(50, 100, 180));
            g2.setStroke(new BasicStroke(2 / (float) zoom));
            g2.drawPolygon(xPoints, yPoints, points.size());

            int indexSelectionne = controleur.getPointSelectionneIndex();
            for (int i = 0; i < points.size(); i++) {
                Point2D.Double point = points.get(i);
                int x = afficheur.poucesVersPixelsX(point.x);
                int y = afficheur.poucesVersPixelsY(point.y);

                if (i == indexSelectionne) {
                    g2.setColor(Color.GREEN);
                    int taille = (int) (12 / zoom);
                    g2.fillOval(x - taille / 2, y - taille / 2, taille, taille);

                    g2.setColor(Color.WHITE);
                    int tailleInterieur = (int) (6 / zoom);
                    g2.fillOval(x - tailleInterieur / 2, y - tailleInterieur / 2, tailleInterieur, tailleInterieur);
                } else {
                    g2.setColor(Color.RED);
                    int taille = (int) (8 / zoom);
                    g2.fillOval(x - taille / 2, y - taille / 2, taille, taille);

                    // Centre blanc
                    g2.setColor(Color.WHITE);
                    int tailleInterieur = (int) (4 / zoom);
                    g2.fillOval(x - tailleInterieur / 2, y - tailleInterieur / 2, tailleInterieur, tailleInterieur);

                }

            }

            double sommeX = 0;
            double sommeY = 0;
            for (Point2D.Double p : points) {
                sommeX += p.x;
                sommeY += p.y;
            }
            double centreX = sommeX / points.size();
            double centreY = sommeY / points.size();

            AffineTransform transformActuelle = g2.getTransform();

            // Configurer la police
            g2.setFont(new Font("Arial", Font.BOLD, 12));
            FontMetrics fm = g2.getFontMetrics();

            int numeroMur = 1;

            // Pour une pièce fermée : le dernier point est une répétition du premier pour
            // fermer
            // Pour une pièce ouverte : le dernier mur n'existe pas
            // Dans les deux cas : nombre de murs = nombre de points - 1
            int nombreMurs = points.size() - 1;

            for (int i = 0; i < nombreMurs; i++) {
                Point2D.Double p1 = points.get(i);
                // Le point suivant (le dernier point de la liste est une répétition du premier)
                Point2D.Double p2 = points.get(i + 1);

                // Milieu du mur (en pouces)
                double midX = (p1.x + p2.x) / 2.0;
                double midY = (p1.y + p2.y) / 2.0;

                // Vecteur qui part du centre de la pièce vers le milieu du mur
                double vecX = midX - centreX;
                double vecY = midY - centreY;

                // Normaliser ce vecteur
                double longueurVec = Math.sqrt(vecX * vecX + vecY * vecY);
                if (longueurVec > 0) {
                    vecX /= longueurVec;
                    vecY /= longueurVec;
                }

                // Appliquer un décalage (offset) pour éloigner le texte du mur
                // 15 pouces de décalage vers l'extérieur (ajustable selon vos besoins)
                double distanceMur = 15.0;
                double texteX_Pouces = midX + (vecX * distanceMur);
                double texteY_Pouces = midY + (vecY * distanceMur);

                // On transforme les coordonnées "Monde" (pouces) en "Pixels bruts" via
                // l'afficheur
                double pixelBrutX = afficheur.poucesVersPixelsX(texteX_Pouces);
                double pixelBrutY = afficheur.poucesVersPixelsY(texteY_Pouces);

                Point2D.Double srcPt = new Point2D.Double(pixelBrutX, pixelBrutY);
                Point2D.Double dstPt = new Point2D.Double();
                transformActuelle.transform(srcPt, dstPt);

                g2.setTransform(new AffineTransform());

                String texte = "Mur " + numeroMur;
                int textLargeur = fm.stringWidth(texte);
                int textHauteur = fm.getAscent();

                g2.setColor(Color.BLACK);
                g2.drawString(texte, (int) dstPt.x - textLargeur / 2, (int) dstPt.y + textHauteur / 4);

                g2.setTransform(transformActuelle);

                numeroMur++;
            }

            /*
             * double maxX = pieceIrreg.getMaxX();
             * double maxY = pieceIrreg.getMaxY();
             * int coinXPixels = afficheur.poucesVersPixelsX(maxX);
             * int coinYPixels = afficheur.poucesVersPixelsY(maxY);
             * 
             * // Dessiner la poignée
             * g2.setColor(Color.RED);
             * int poigneeSize = (int)(10 / zoom);
             * g2.fillRect(coinXPixels - poigneeSize/2, coinYPixels - poigneeSize/2,
             * poigneeSize, poigneeSize);
             * 
             * // Contour blanc
             * g2.setColor(Color.WHITE);
             * g2.setStroke(new BasicStroke(1 / (float)zoom));
             * g2.drawRect(coinXPixels - poigneeSize/2, coinYPixels - poigneeSize/2,
             * poigneeSize, poigneeSize);
             * 
             * }
             */
            // poignée de redimensionnement
            // D'ABORD : Dessiner la bounding box en pointillés
            double minX = pieceIrreg.getMinX();
            double minY = pieceIrreg.getMinY();
            double maxX = pieceIrreg.getMaxX();
            double maxY = pieceIrreg.getMaxY();

            int bbX = afficheur.poucesVersPixelsX(minX);
            int bbY = afficheur.poucesVersPixelsY(minY);
            int bbLargeur = afficheur.poucesVersPixelsX(maxX - minX);
            int bbLongueur = afficheur.poucesVersPixelsY(maxY - minY);

            // Rectangle en pointillés (bounding box)
            g2.setColor(new Color(150, 150, 150, 150)); // Gris
            g2.setStroke(new BasicStroke(2 / (float) zoom, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 10.0f,
                    new float[] { 10.0f, 5.0f }, 0.0f));
            g2.drawRect(bbX, bbY, bbLargeur, bbLongueur);

            // ENSUITE : Dessiner la poignée rouge (coin bas-droit de la bbox)
            int coinXPixels = afficheur.poucesVersPixelsX(maxX);
            int coinYPixels = afficheur.poucesVersPixelsY(maxY);

            // Dessiner la poignée
            g2.setColor(Color.RED);
            int poigneeSize = (int) (10 / zoom);
            g2.fillRect(coinXPixels - poigneeSize / 2, coinYPixels - poigneeSize / 2,
                    poigneeSize, poigneeSize);

            // Contour blanc
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(1 / (float) zoom));
            g2.drawRect(coinXPixels - poigneeSize / 2, coinYPixels - poigneeSize / 2,
                    poigneeSize, poigneeSize);
        }

        // Après avoir dessiné pieceIrreg...
        if (pieceIrreg != null && pieceIrreg.estFermee() && pieceIrreg.getMeubles() != null) {

            afficheur.afficherMeublesSansDrainIrreg(g2, pieceIrreg);
            afficheur.afficherMeublesAvecDrainIrreg(g2, pieceIrreg);

        }

        PieceDTO pieceDTO = controleur.getPiece();
        if (pieceDTO != null) {
            afficheur.afficherPiece(g2, pieceDTO);
            afficheur.afficherMeublesSansDrain(g2, pieceDTO);
            afficheur.afficherMeublesAvecDrain(g2, pieceDTO);

        }

        // Meuble sans drain sélectionné
        MeubleSansDrainDTO meubleSelectionne = controleur.getMeubleSelectionne();
        if (meubleSelectionne != null) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3 / (float) zoom));
            int xPixels = afficheur.poucesVersPixelsX(meubleSelectionne.getX());
            int yPixels = afficheur.poucesVersPixelsY(meubleSelectionne.getY());
            int largeurPixels = afficheur.poucesVersPixelsX(meubleSelectionne.getLargeur());
            int hauteurPixels = afficheur.poucesVersPixelsY(meubleSelectionne.getHauteur());
            g2.drawRect(xPixels, yPixels, largeurPixels, hauteurPixels);
        }

        MeubleDrainDTO meubleAvecDrainSelectionne = controleur.getMeubleAvecDrainSelectionne();
        if (meubleAvecDrainSelectionne != null) {
            g2.setColor(Color.RED);
            g2.setStroke(new BasicStroke(3 / (float) zoom));

            // Rectangle rouge autour du meuble
            int xPixels = afficheur.poucesVersPixelsX(meubleAvecDrainSelectionne.getX());
            int yPixels = afficheur.poucesVersPixelsY(meubleAvecDrainSelectionne.getY());
            int largeurPixels = afficheur.poucesVersPixelsX(meubleAvecDrainSelectionne.getLargeur());
            int hauteurPixels = afficheur.poucesVersPixelsY(meubleAvecDrainSelectionne.getHauteur());
            g2.drawRect(xPixels, yPixels, largeurPixels, hauteurPixels);

            // Cercle rouge autour du drain
            int xDrain = afficheur.poucesVersPixelsX(
                    meubleAvecDrainSelectionne.getX() + meubleAvecDrainSelectionne.getXDrainRelatif());
            int yDrain = afficheur.poucesVersPixelsY(
                    meubleAvecDrainSelectionne.getY() + meubleAvecDrainSelectionne.getYDrainRelatif());
            int diametre = afficheur.poucesVersPixelsX(meubleAvecDrainSelectionne.getDiametreDrain());
            g2.drawOval(xDrain - diametre / 2, yDrain - diametre / 2, diametre, diametre);
        }

        // Affichage des fils
        if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {

            for (Fil fil : controleur.getFils()) {
                int x = afficheur.poucesVersPixelsX(fil.getX());
                int y = afficheur.poucesVersPixelsY(fil.getY());
                int longueur = afficheur.poucesVersPixelsX(fil.getLongueur());
                int epaisseur = Math.max((int) (afficheur.poucesVersPixelsY(fil.getEpaisseur())), 2);
                g2.setColor(Color.BLUE); // Couleur du fil
                g2.fillRect(x, y, longueur, epaisseur); // Dessin du fil
            }
        }

        // Afficher le fil en noir
        if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
            if (cheminFil != null && cheminFil.size() > 1) {
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(4 / (float) zoom));

                for (int i = 0; i < cheminFil.size() - 1; i++) {
                    Intersection a = cheminFil.get(i);
                    Intersection b = cheminFil.get(i + 1);
                    int x1 = afficheur.poucesVersPixelsX(a.getX());
                    int y1 = afficheur.poucesVersPixelsY(a.getY());
                    int x2 = afficheur.poucesVersPixelsX(b.getX());
                    int y2 = afficheur.poucesVersPixelsY(b.getY());
                    g2.drawLine(x1, y1, x2, y2);
                }

                // Dessiner les intersections du fil
                for (int i = 0; i < cheminFil.size(); i++) {
                    Intersection point = cheminFil.get(i);
                    int x = afficheur.poucesVersPixelsX(point.getX());
                    int y = afficheur.poucesVersPixelsY(point.getY());

                    // Mettre en évidence l'intersection sélectionnée
                    boolean isSelected = (isDraggingFilPoint && filPointIndex == i);

                    if (isSelected) {
                        // Cercle extérieur plus grand et plus visible pour l'intersection sélectionnée
                        g2.setColor(new Color(255, 255, 0, 200)); // Jaune vif
                        int rayonExterieur = (int) ((FIL_POINT_RADIUS + 4) / zoom);
                        g2.fillOval(x - rayonExterieur, y - rayonExterieur, rayonExterieur * 2, rayonExterieur * 2);

                        // Bordure pour mieux la voir
                        g2.setColor(Color.YELLOW);
                        g2.setStroke(new BasicStroke(3 / (float) zoom));
                        g2.drawOval(x - rayonExterieur, y - rayonExterieur, rayonExterieur * 2, rayonExterieur * 2);
                    }

                    // Cercle normal pour toutes les intersections
                    g2.setColor(new Color(255, 100, 100));
                    int rayon = (int) (FIL_POINT_RADIUS / zoom);
                    g2.fillOval(x - rayon, y - rayon, rayon * 2, rayon * 2);

                    // Cercle blanc au centre
                    g2.setColor(Color.WHITE);
                    int rayonInterieur = (int) (4 / zoom);
                    g2.fillOval(x - rayonInterieur, y - rayonInterieur, rayonInterieur * 2, rayonInterieur * 2);
                }
            }
        }

        // Éléments chauffants
        for (ElementChauffant e : controleur.getElementsChauffants()) {
            boolean isSelected = (controleur.getElementSelectionne() == e);
            boolean estThermostat = Thermostat.estThermostat(e);

            // Le thermostat a une couleur différente (blanc avec bordure noire)
            if (estThermostat) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(isSelected ? Color.RED : Color.ORANGE);
            }
            g2.setStroke(new BasicStroke(2 / (float) zoom));

            int xPixels = afficheur.poucesVersPixelsX(e.getX());
            int yPixels = afficheur.poucesVersPixelsY(e.getY());
            int largeurPixels = afficheur.poucesVersPixelsX(e.getLargeur());
            int hauteurPixels = afficheur.poucesVersPixelsY(e.getHauteur());

            // Sauvegarder la transformation actuelle
            AffineTransform oldTransform = g2.getTransform();

            // Si l'élément a un angle (pièce irrégulière), appliquer la rotation
            if (Math.abs(e.getAngle()) > 0.001) {
                // Le coin supérieur gauche (xPixels, yPixels) a été calculé pour que le centre
                // visuel
                // après rotation soit à la bonne position. Pour dessiner, on doit calculer le
                // centre
                // de manière cohérente avec le calcul dans Controleur.

                // Le coin supérieur gauche dans le repère local (non-roté) est (0, 0)
                // Le centre dans le repère local est (largeurPixels/2, hauteurPixels/2)
                // On applique la rotation pour obtenir le vecteur du coin vers le centre dans
                // le repère global
                double cosAngle = Math.cos(e.getAngle());
                double sinAngle = Math.sin(e.getAngle());

                // Vecteur du coin supérieur gauche vers le centre dans le repère local
                double dxLocal = largeurPixels / 2.0;
                double dyLocal = hauteurPixels / 2.0;

                // Appliquer la rotation pour obtenir le vecteur dans le repère global
                // Rotation : x' = x*cos - y*sin, y' = x*sin + y*cos
                double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
                double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;

                // Centre réel dans le repère global (coordonnées en pixels)
                double centreX = xPixels + dxGlobal;
                double centreY = yPixels + dyGlobal;

                // Appliquer la rotation autour du centre
                g2.translate(centreX, centreY);
                g2.rotate(e.getAngle());
                g2.translate(-largeurPixels / 2.0, -hauteurPixels / 2.0);

                // Dessiner l'élément horizontalement (il sera tourné par la transformation)
                g2.fillRect(0, 0, largeurPixels, hauteurPixels);

                // Pour le thermostat, dessiner la bordure noire et le texte
                if (estThermostat) {
                    g2.setColor(Color.BLACK);
                    g2.drawRect(0, 0, largeurPixels, hauteurPixels);

                    // Dessiner le texte "Thermostat"
                    String label = "Thermostat";
                    Font font = g2.getFont().deriveFont(8f);
                    g2.setFont(font);
                    FontMetrics fm = g2.getFontMetrics();
                    int textWidth = fm.stringWidth(label);
                    int textHeight = fm.getAscent();
                    int textX = (largeurPixels - textWidth) / 2;
                    int textY = (hauteurPixels + textHeight) / 2;
                    g2.setColor(Color.BLACK);
                    g2.drawString(label, textX, textY);
                } else {
                    g2.drawRect(0, 0, largeurPixels, hauteurPixels);
                }

                // Restaurer la transformation
                g2.setTransform(oldTransform);
            } else {
                // Pas de rotation : dessiner normalement
                if (e.isHorizontal()) {
                    // Élément horizontal : dessiner comme un rectangle horizontal
                    g2.fillRect(xPixels, yPixels, largeurPixels, hauteurPixels);

                    // Pour le thermostat, dessiner la bordure noire et le texte
                    if (estThermostat) {
                        g2.setColor(Color.BLACK);
                        g2.drawRect(xPixels, yPixels, largeurPixels, hauteurPixels);

                        // Dessiner le texte "Thermostat" (dans le repère normal)
                        AffineTransform old = g2.getTransform();
                        g2.setTransform(new AffineTransform());
                        String label = "Thermostat";
                        Font font = g2.getFont().deriveFont(8f);
                        g2.setFont(font);
                        FontMetrics fm = g2.getFontMetrics();
                        int textWidth = fm.stringWidth(label);
                        int textHeight = fm.getAscent();
                        int textX = xPixels + (largeurPixels - textWidth) / 2;
                        int textY = (getHeight() - yPixels) - (hauteurPixels - textHeight) / 2;
                        g2.setColor(Color.BLACK);
                        g2.drawString(label, textX, textY);
                        g2.setTransform(old);
                    } else {
                        g2.drawRect(xPixels, yPixels, largeurPixels, hauteurPixels);
                    }
                } else {
                    // Élément vertical : dessiner comme un rectangle vertical
                    g2.fillRect(xPixels, yPixels, hauteurPixels, largeurPixels);
                    g2.drawRect(xPixels, yPixels, hauteurPixels, largeurPixels);
                }
            }
        }

        if (graphe != null) {
            g2.setColor(new Color(0, 100, 255, 90));
            int rayon = 4;
            for (Intersection i : graphe) {
                int x = afficheur.poucesVersPixelsX(i.getX());
                int y = afficheur.poucesVersPixelsY(i.getY());
                g2.fillOval(x - rayon, y - rayon, rayon * 2, rayon * 2);

                if (i.getTop() != null) {
                    int yTop = afficheur.poucesVersPixelsY(i.getTop().getY());
                    int xTop = afficheur.poucesVersPixelsX(i.getTop().getX());
                    g2.drawLine(x, y, xTop, yTop);
                }
                if (i.getRight() != null) {
                    int yRight = afficheur.poucesVersPixelsY(i.getRight().getY());
                    int xRight = afficheur.poucesVersPixelsX(i.getRight().getX());
                    g2.drawLine(x, y, xRight, yRight);
                }
            }
        }

        if (pieceDTO != null && pieceDTO.getThermostat() != null) {
            dessinerThermostat(g2, pieceDTO);
        }
        if (pieceIrreg != null && pieceIrreg.getThermostat() != null) {
            dessinerThermostat(g2, pieceIrreg);
        }

        if (isDraggingFilPoint && !cheminsDisponibles.isEmpty()) {
            g2.setColor(new Color(0, 255, 0, 128)); // Vert semi-transparent
            g2.setStroke(new BasicStroke(3 / (float) zoom));

            for (Intersection option : cheminsDisponibles) {
                int x = afficheur.poucesVersPixelsX(option.getX());
                int y = afficheur.poucesVersPixelsY(option.getY());
                int rayonOption = (int) (8 / zoom);
                g2.fillOval(x - rayonOption, y - rayonOption, rayonOption * 2, rayonOption * 2);

                // Dessiner une ligne vers l'intersection sélectionnée
                if (filPointIndex >= 0 && filPointIndex < cheminFil.size()) {
                    Intersection selected = cheminFil.get(filPointIndex);
                    int sx = afficheur.poucesVersPixelsX(selected.getX());
                    int sy = afficheur.poucesVersPixelsY(selected.getY());
                    g2.drawLine(sx, sy, x, y);
                }
            }
        }

        g2.dispose();
    }

    // Nouvelle méthode pour trouver l'intersection de grille existante la plus
    // proche
    private Intersection trouverIntersectionLaPlusProche(double xPouces, double yPouces) {
        if (graphe == null || graphe.isEmpty()) {
            return null;
        }

        double minDistance = Double.MAX_VALUE;
        Intersection closest = null;

        for (Intersection intersection : graphe) {
            double dist = Math.sqrt(
                    Math.pow(intersection.getX() - xPouces, 2) +
                            Math.pow(intersection.getY() - yPouces, 2));

            if (dist < minDistance) {
                minDistance = dist;
                closest = intersection;
            }
        }

        // Seulement retourner si suffisamment proche (dans un rayon raisonnable)
        if (minDistance < GRID_SPACING * 1.5) {
            return closest;
        }

        return null;
    }

    private List<Intersection> cheminsDisponibles = new ArrayList<>();

    private void afficherCheminsDisponibles(Intersection point) {
        cheminsDisponibles.clear();

        // Ajouter tous les voisins valides comme options
        if (point.getTop() != null && validerPoint(point.getTop().getX(), point.getTop().getY())) {
            cheminsDisponibles.add(point.getTop());
        }
        if (point.getBottom() != null && validerPoint(point.getBottom().getX(), point.getBottom().getY())) {
            cheminsDisponibles.add(point.getBottom());
        }
        if (point.getLeft() != null && validerPoint(point.getLeft().getX(), point.getLeft().getY())) {
            cheminsDisponibles.add(point.getLeft());
        }
        if (point.getRight() != null && validerPoint(point.getRight().getX(), point.getRight().getY())) {
            cheminsDisponibles.add(point.getRight());
        }
    }

    private List<Intersection> recalculerCheminAvecIntersection(int indexModifie, Intersection nouvelleIntersection) {
        List<Intersection> nouveauChemin = new ArrayList<>();

        // Partie avant l'intersection modifiée reste identique
        for (int i = 0; i < indexModifie; i++) {
            nouveauChemin.add(cheminFil.get(i));
        }

        for (Intersection existante : nouveauChemin) {
            if (existante.getX() == nouvelleIntersection.getX() &&
                    existante.getY() == nouvelleIntersection.getY()) {
                if (mainWindow != null) {
                    mainWindow.afficherErreur("Le fil ne peut pas passer deux fois par la même intersection!");
                }
                return null; // Intersection déjà présente dans le chemin
            }
        }

        if (indexModifie > 0) {
            Intersection precedent = cheminFil.get(indexModifie - 1);

            // Vérifier qu'il n'y a pas de collision avec un meuble
            if (!estSegmentSansMeuble(precedent, nouvelleIntersection)) {
                if (mainWindow != null) {
                    mainWindow.afficherErreur("Le segment traverse un meuble!");
                }
                return null; // Segment traverse un meuble
            }

            // Vérifier qu'il n'y a pas de croisement avec le chemin existant
            if (!verifierPasDeCroisementAvecChemin(precedent, nouvelleIntersection, nouveauChemin)) {
                if (mainWindow != null) {
                    mainWindow.afficherErreur("Le fil se croise!");
                }
                return null; // Croisement détecté
            }
        }

        // Ajouter la nouvelle intersection
        nouveauChemin.add(nouvelleIntersection);

        // en utilisant le même algorithme serpentin
        Intersection pointPrecedent = indexModifie > 0 ? cheminFil.get(indexModifie - 1) : null;

        // Déterminer la direction depuis le point précédent
        boolean goingRight = true;
        if (pointPrecedent != null) {
            double dx = nouvelleIntersection.getX() - pointPrecedent.getX();
            goingRight = dx >= 0;
        }

        // Continuer le serpentin depuis la nouvelle intersection
        Set<Intersection> visited = new HashSet<>(nouveauChemin);
        Set<Intersection> validNodesSet = new HashSet<>(graphe);
        Intersection current = nouvelleIntersection;
        double longueurActuelle = calculerLongueur(nouveauChemin);
        double longueurFilMax = controleur.getLongueurFilMax();

        while (current != null && longueurActuelle < longueurFilMax) {
            Intersection next = null;

            Intersection right = current.getRight();
            Intersection left = current.getLeft();
            Intersection down = current.getBottom();
            Intersection up = current.getTop();

            boolean canRight = estVoisinValide(right, validNodesSet, visited);
            boolean canLeft = estVoisinValide(left, validNodesSet, visited);
            boolean canDown = estVoisinValide(down, validNodesSet, visited);
            boolean canUp = estVoisinValide(up, validNodesSet, visited);

            if (canRight && !estSegmentSansMeuble(current, right) ||
                    !verifierPasDeCroisementAvecChemin(current, right, nouveauChemin)) {
                canRight = false;
            }
            if (canLeft && !estSegmentSansMeuble(current, left) ||
                    !verifierPasDeCroisementAvecChemin(current, left, nouveauChemin)) {
                canLeft = false;
            }
            if (canDown && !estSegmentSansMeuble(current, down) ||
                    !verifierPasDeCroisementAvecChemin(current, down, nouveauChemin)) {
                canDown = false;
            }
            if (canUp && !estSegmentSansMeuble(current, up) ||
                    !verifierPasDeCroisementAvecChemin(current, up, nouveauChemin)) {
                canUp = false;
            }

            // Même logique serpentin que genererCheminFil
            if (goingRight) {
                if (canRight) {
                    next = right;
                } else if (canDown) {
                    next = down;
                    goingRight = false;
                } else if (canUp) {
                    next = up;
                    goingRight = false;
                } else if (canLeft) {
                    next = left;
                }
            } else {
                if (canLeft) {
                    next = left;
                } else if (canDown) {
                    next = down;
                    goingRight = true;
                } else if (canUp) {
                    next = up;
                    goingRight = true;
                } else if (canRight) {
                    next = right;
                }
            }

            if (next != null) {
                double dist = Math.abs(next.getX() - current.getX()) +
                        Math.abs(next.getY() - current.getY());

                double distanceGrillePouces = controleur.getDistanceGrillePouces();
                if (dist > distanceGrillePouces * 1.5)
                    break;
                if (longueurActuelle + dist > longueurFilMax)
                    break;

                nouveauChemin.add(next);
                visited.add(next);
                longueurActuelle += dist;
                current = next;
            } else {
                break;
            }
        }

        return nouveauChemin;
    }

    private double calculerLongueur(List<Intersection> chemin) {
        double longueur = 0;
        for (int i = 0; i < chemin.size() - 1; i++) {
            Intersection p1 = chemin.get(i);
            Intersection p2 = chemin.get(i + 1);
            longueur += Math.abs(p2.getX() - p1.getX()) + Math.abs(p2.getY() - p1.getY());
        }
        return longueur;
    }

    private boolean validerPoint(double x, double y) {
        PieceDTO piece = controleur.getPiece();
        if (piece == null)
            return false;

        // Vérifier les limites
        double minX = piece.getX() + 3;
        double maxX = piece.getX() + piece.getLargeur() - 3;
        double minY = piece.getY() + 3;
        double maxY = piece.getY() + piece.getLongueur() - 3;

        if (x < minX || x > maxX || y < minY || y > maxY) {
            return false;
        }

        // Vérifier collision avec meubles
        for (MeubleSansDrainDTO m : piece.getMeubles()) {
            if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                    y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                return false;
            }
        }

        for (MeubleDrainDTO m : piece.getMeublesDrain()) {
            if (x >= m.getX() && x <= m.getX() + m.getLargeur() &&
                    y >= m.getY() && y <= m.getY() + m.getHauteur()) {
                return false;
            }
        }

        return true;
    }

    private boolean verifierPasDeCroisementAvecChemin(Intersection p1, Intersection p2, List<Intersection> chemin) {
        if (chemin.size() < 2)
            return true;

        for (int i = 0; i < chemin.size() - 1; i++) {
            Intersection s1 = chemin.get(i);
            Intersection s2 = chemin.get(i + 1);

            // Ignorer si les segments partagent un point
            if (pointsEgaux(p1, s1) || pointsEgaux(p1, s2) ||
                    pointsEgaux(p2, s1) || pointsEgaux(p2, s2)) {
                continue;
            }

            // Vérifier si les segments se croisent
            if (segmentsSeCroisent(p1, p2, s1, s2)) {
                return false;
            }
        }

        return true;
    }

    private boolean segmentsSeCroisent(Intersection p1, Intersection p2, Intersection p3, Intersection p4) {
        double d1 = direction(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p3.getX(), p3.getY());
        double d2 = direction(p1.getX(), p1.getY(), p2.getX(), p2.getY(), p4.getX(), p4.getY());
        double d3 = direction(p3.getX(), p3.getY(), p4.getX(), p4.getY(), p1.getX(), p1.getY());
        double d4 = direction(p3.getX(), p3.getY(), p4.getX(), p4.getY(), p2.getX(), p2.getY());

        if (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
                ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0))) {
            return true;
        }

        return false;
    }

    private double direction(double x1, double y1, double x2, double y2, double x3, double y3) {
        return (x3 - x1) * (y2 - y1) - (y3 - y1) * (x2 - x1);
    }

    private boolean estSegmentSansMeuble(Intersection p1, Intersection p2) {
        // Vérifier pour les pièces régulières et irrégulières
        PieceDTO piece = controleur.getPiece();
        PieceIrreguliereDTO pieceIrreg = controleur.getPieceIrreguliere();
        if (piece == null && pieceIrreg == null)
            return true;

        // Vérifier plusieurs points le long du segment
        int numTests = 10;
        for (int i = 0; i <= numTests; i++) {
            double t = (double) i / numTests;
            double x = p1.getX() + t * (p2.getX() - p1.getX());
            double y = p1.getY() + t * (p2.getY() - p1.getY());

            // Vérifier collision avec meubles sans drain
            if (piece != null) {
                for (MeubleSansDrainDTO meuble : piece.getMeubles()) {
                    if (x >= meuble.getX() && x <= meuble.getX() + meuble.getLargeur() &&
                            y >= meuble.getY() && y <= meuble.getY() + meuble.getHauteur()) {
                        return false; // Le segment traverse ce meuble
                    }
                }

                // Vérifier collision avec meubles avec drain
                for (MeubleDrainDTO meuble : piece.getMeublesDrain()) {
                    if (x >= meuble.getX() && x <= meuble.getX() + meuble.getLargeur() &&
                            y >= meuble.getY() && y <= meuble.getY() + meuble.getHauteur()) {
                        return false; // Le segment traverse ce meuble
                    }
                }
            }

            // Vérifier pour les pièces irrégulières
            if (pieceIrreg != null) {
                for (MeubleSansDrainDTO meuble : pieceIrreg.getMeubles()) {
                    if (x >= meuble.getX() && x <= meuble.getX() + meuble.getLargeur() &&
                            y >= meuble.getY() && y <= meuble.getY() + meuble.getHauteur()) {
                        return false; // Le segment traverse ce meuble
                    }
                }

                // Vérifier collision avec meubles avec drain
                for (MeubleDrainDTO meuble : pieceIrreg.getMeublesDrain()) {
                    if (x >= meuble.getX() && x <= meuble.getX() + meuble.getLargeur() &&
                            y >= meuble.getY() && y <= meuble.getY() + meuble.getHauteur()) {
                        return false; // Le segment traverse ce meuble
                    }
                }
            }
        }

        return true;
    }

    private boolean pointsEgaux(Intersection p1, Intersection p2) {
        if (p1 == null || p2 == null)
            return false;
        double epsilon = 0.01; // Tolérance pour les comparaisons de flottants
        return Math.abs(p1.getX() - p2.getX()) < epsilon &&
                Math.abs(p1.getY() - p2.getY()) < epsilon;
    }
}
