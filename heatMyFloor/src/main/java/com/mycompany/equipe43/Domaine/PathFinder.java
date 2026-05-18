package com.mycompany.equipe43.Domaine;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * PathFinder avec algorithme de remplissage en serpentin optimisé
 * - Remplit l'espace de manière systématique en serpentin (snake pattern)
 * - Priorité aux mouvements horizontaux pour couvrir les lignes
 * - Change de ligne intelligemment pour maximiser la couverture
 * - Évite les obstacles et respecte la longueur de fil
 */
public class PathFinder {

    private enum Direction {
        RIGHT(1, 0),
        LEFT(-1, 0),
        DOWN(0, 1),
        UP(0, -1);
        // Pas de directions diagonales - le fil ne peut jamais aller en diagonale

        final int dx;
        final int dy;

        Direction(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        Point2D.Double apply(Point2D.Double p, double espacement) {
            // Calculer directement en ajoutant l'espacement (les points sont déjà
            // translatés)
            return new Point2D.Double(
                    p.x + dx * espacement,
                    p.y + dy * espacement);
        }

        private static int coordToIndex(double coord, double espacement) {
            return (int) Math.round(coord / espacement);
        }

        private static double indexToCoord(int index, double espacement) {
            return index * espacement;
        }
    }

    /**
     * Classe pour évaluer et comparer les chemins
     */
    private static class EvaluationChemin {
        List<Point2D.Double> chemin;
        int nombreNoeuds;
        double longueurUtilisee;
        double score; // Score combiné pour la sélection

        EvaluationChemin(List<Point2D.Double> chemin, int nombreNoeuds, double longueurUtilisee, double longueurFilMax,
                int totalNoeuds) {
            this.chemin = new ArrayList<>(chemin);
            this.nombreNoeuds = nombreNoeuds;
            this.longueurUtilisee = longueurUtilisee;

            // Calcul du score: priorité au nombre de nœuds, puis à l'utilisation du fil
            // Score = (nombreNoeuds * 1000) + (longueurUtilisee / longueurFilMax * 100)
            // Cela favorise les chemins avec plus de nœuds, et en cas d'égalité, ceux qui
            // utilisent plus de fil
            double ratioLongueur = longueurUtilisee / longueurFilMax;
            double ratioCouverture = (double) nombreNoeuds / totalNoeuds;
            this.score = nombreNoeuds * 1000.0 + ratioLongueur * 100.0 + ratioCouverture * 50.0;
        }

        boolean estMeilleurQue(EvaluationChemin autre) {
            return this.score > autre.score;
        }
    }

    private static class Node {
        Point2D.Double position;
        Node parent;
        double gCost;
        double hCost;
        double fCost;

        Node(Point2D.Double position, Node parent, double gCost, double hCost) {
            this.position = position;
            this.parent = parent;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

    private static class VoisinScore {
        Point2D.Double point;
        int score;

        VoisinScore(Point2D.Double point, int score) {
            this.point = point;
            this.score = score;
        }
    }

    private Piece piece;
    private double espacement;
    private double longueurFilMax;
    private boolean allowManualCrossing = false;
    private Set<String> manualAllowedPoints = new HashSet<>();
    private double translationX = 0.0;
    private double translationY = 0.0;

    public PathFinder(Piece piece, double espacement) {
        this.piece = piece;
        this.espacement = espacement;
        this.longueurFilMax = Double.MAX_VALUE;
    }

    public PathFinder(Piece piece, double espacement, double longueurFilMax) {
        this(piece, espacement);
        this.longueurFilMax = longueurFilMax;
    }

    public void setTranslations(double translationX, double translationY) {
        this.translationX = translationX;
        this.translationY = translationY;
    }

    public void setLongueurFilMax(double longueurFilMax) {
        this.longueurFilMax = longueurFilMax;
    }

    public void setAllowManualCrossing(boolean allow) {
        this.allowManualCrossing = allow;
    }

    public void addManualAllowedPoint(Point2D.Double p) {
        manualAllowedPoints.add(keyFromPoint(p));
    }

    /**
     * Completely rewritten: Uses greedy snake-fill algorithm instead of exhaustive
     * DFS
     * Génère le chemin optimal via algorithme de remplissage en serpentin
     * Priorité: remplir horizontalement puis passer à la ligne suivante
     */
    public List<Point2D.Double> genererCheminSerpentinAvecEvitement() {
        // Générer la grille exactement comme DrawingPanel, avec translations
        Map<String, Point2D.Double> grille = genererGrilleMap();
        if (grille.isEmpty())
            return new ArrayList<>();

        System.out.println("[v0] ========================================");
        System.out.println("[v0] REMPLISSAGE GREEDY FLEXIBLE");
        System.out.println("[v0] ========================================");
        System.out.println("[v0] Grille totale: " + grille.size() + " nœuds");
        System.out.println("[v0] Longueur fil max: " + longueurFilMax);

        Point2D.Double pointThermostat = trouverPointThermostat(grille);

        // Si la pièce a un thermostat, commencer par sa position réelle
        Point2D.Double pointDepart = pointThermostat;
        if (piece instanceof PieceIrreguliere) {
            PieceIrreguliere pieceIrreg = (PieceIrreguliere) piece;
            Thermostat thermostat = pieceIrreg.getThermostatIrregulier();
            if (thermostat != null) {
                pointDepart = new Point2D.Double(thermostat.getX(), thermostat.getY());
            }
        } else if (piece.getThermostat() != null) {
            double thermostatX = piece.getThermostat().getX();
            double thermostatY = piece.getThermostat().getY();
            pointDepart = new Point2D.Double(thermostatX, thermostatY);
        }

        System.out.println("[v0] Point thermostat: (" +
                String.format("%.1f", pointDepart.x) + ", " +
                String.format("%.1f", pointDepart.y) + ")");

        List<Point2D.Double> chemin = remplissageGreedyFlexible(pointDepart, grille);

        double longueurUtilisee = calculerLongueurChemin(chemin);

        System.out.println("\n[v0] ========================================");
        System.out.println("[v0] RÉSULTAT FINAL");
        System.out.println("[v0] ========================================");
        System.out.println("[v0] Chemin trouvé: " + chemin.size() + " nœuds");
        System.out.println(
                "[v0] Longueur de fil utilisée: " + String.format("%.2f", longueurUtilisee) + " / " + longueurFilMax);
        System.out.println(
                "[v0] Couverture: " + String.format("%.1f", 100.0 * chemin.size() / grille.size()) + "%");

        return chemin;
    }

    /**
     * Nouveau algorithme greedy flexible
     * Remplit tout l'espace en choisissant toujours le meilleur voisin adjacent
     * - Priorité absolue à MONTER (UP) si possible
     * - Sinon, priorité à RIGHT ou LEFT
     * - En dernier recours, descendre (DOWN)
     * - Si aucun voisin adjacent, saute vers le nœud non visité le plus proche
     * Continue jusqu'à ce que TOUS les nœuds disponibles soient visités
     */
    private List<Point2D.Double> remplissageGreedyFlexible(Point2D.Double depart,
            Map<String, Point2D.Double> grille) {
        List<Point2D.Double> chemin = new ArrayList<>();
        Set<String> visites = new HashSet<>();
        double longueurAccumulee = 0.0;

        // Ajouter le point de départ (thermostat) au chemin
        Point2D.Double courant = depart;
        chemin.add(courant);

        // Si le point de départ n'est pas dans la grille, trouver le point de grille le
        // plus proche
        String departKey = keyFromPoint(courant);
        if (!grille.containsKey(departKey)) {
            // Le thermostat n'est pas exactement sur un point de grille
            // Trouver le point de grille le plus proche pour continuer
            Point2D.Double plusProche = null;
            double distanceMin = Double.MAX_VALUE;

            for (Point2D.Double p : grille.values()) {
                double dist = distance(courant, p);
                if (dist < distanceMin) {
                    distanceMin = dist;
                    plusProche = p;
                }
            }

            if (plusProche != null) {
                // Ajouter le segment du thermostat au point de grille le plus proche
                longueurAccumulee += distanceMin;
                chemin.add(plusProche); // Ajouter le point de grille au chemin
                courant = plusProche;
            }
        }

        visites.add(keyFromPoint(courant));

        System.out.println("[v0] Début du remplissage greedy flexible...");
        System.out.println("[v0] Objectif: visiter " + grille.size() + " nœuds au total");

        int iteration = 0;
        int iterationsSansProgres = 0;
        int maxIterationsSansProgres = 100; // Limiter pour éviter les boucles infinies
        int nombreBacktrackings = 0;
        int maxBacktrackings = 50; // Limiter le nombre total de backtrackings
        int tailleCheminPrecedente = chemin.size();

        while (visites.size() < grille.size()) {
            iteration++;

            // Vérifier la longueur de fil avant de continuer
            if (longueurAccumulee >= longueurFilMax) {
                System.out.println("[v0] Longueur de fil max atteinte. Arrêt à " +
                        visites.size() + "/" + grille.size() + " nœuds");
                break;
            }

            // Chercher le meilleur voisin adjacent selon la priorité: UP, RIGHT/LEFT, DOWN
            Point2D.Double suivant = choisirMeilleurVoisinAdjacent(courant, grille, visites,
                    chemin, longueurAccumulee);

            if (suivant == null) {
                // Aucun voisin adjacent disponible, essayer le backtracking : revenir en
                // arrière
                // et essayer une autre direction
                if (chemin.size() >= 2 && nombreBacktrackings < maxBacktrackings) {
                    System.out.println("[v0] Aucun voisin adjacent disponible, tentative de backtracking...");
                    double[] longueurRef = { longueurAccumulee };
                    suivant = essayerBacktracking(chemin, grille, visites, longueurRef);
                    longueurAccumulee = longueurRef[0];

                    if (suivant != null) {
                        nombreBacktrackings++;
                        // Mettre à jour le nœud courant si le backtracking a réussi
                        if (!chemin.isEmpty()) {
                            courant = chemin.get(chemin.size() - 1); // Le nœud avant-dernier devient le courant
                        }
                    }
                }

                if (suivant == null) {
                    // Backtracking n'a pas fonctionné ou limite atteinte, essayer de sauter vers un
                    // nœud proche en
                    // dernier recours
                    System.out.println(
                            "[v0] Backtracking échoué ou limite atteinte, recherche du nœud le plus proche... (" +
                                    visites.size() + "/" + grille.size() + " visités)");
                    suivant = sauterVersNoeudLePlusProche(courant, grille, visites,
                            chemin, longueurAccumulee);

                    if (suivant == null) {
                        // Aucun chemin trouvé, arrêter
                        System.out.println("[v0] Aucun chemin trouvé. Arrêt. " +
                                visites.size() + "/" + grille.size() + " nœuds visités");
                        break;
                    }
                }
            }

            // Vérifier si on a fait du progrès
            if (chemin.size() > tailleCheminPrecedente) {
                iterationsSansProgres = 0;
                tailleCheminPrecedente = chemin.size();
            } else {
                iterationsSansProgres++;
                if (iterationsSansProgres >= maxIterationsSansProgres) {
                    System.out.println("[v0] Arrêt: " + maxIterationsSansProgres +
                            " itérations sans progrès (boucle détectée). " +
                            visites.size() + "/" + grille.size() + " nœuds visités");
                    break;
                }
            }

            // Ajouter le point au chemin
            double dist = distance(courant, suivant);
            longueurAccumulee += dist;

            chemin.add(suivant);
            visites.add(keyFromPoint(suivant));
            courant = suivant;

            // Affichage périodique de progression
            if (iteration % 50 == 0) {
                double pourcentage = 100.0 * visites.size() / grille.size();
                System.out.println("[v0] Progression: " + visites.size() + "/" + grille.size() +
                        " nœuds (" + String.format("%.1f", pourcentage) + "%), " +
                        String.format("%.2f", longueurAccumulee) + "/" + longueurFilMax + " fil");
            }
        }

        System.out.println("[v0] Remplissage terminé après " + iteration + " itérations");

        return chemin;
    }

    /**
     * Choisit le meilleur voisin adjacent selon la priorité spécifiée:
     * 1. D'abord vérifier s'il y a un nœud libre à droite (RIGHT) ou à gauche
     * (LEFT)
     * - si oui, prendre cette direction
     * 2. Sinon, vérifier s'il y a un nœud libre au-dessus (UP) - si oui, monter
     * 3. Sinon, descendre (DOWN)
     * 
     * Critères de validation:
     * - Le voisin doit être valide (pas dans un meuble, pas déjà visité)
     * - Ne doit pas créer de croisement avec le chemin existant
     * - Respecte la longueur de fil maximale
     */
    private Point2D.Double choisirMeilleurVoisinAdjacent(Point2D.Double courant,
            Map<String, Point2D.Double> grille,
            Set<String> visites,
            List<Point2D.Double> chemin,
            double longueurAccumulee) {
        // PRIORITÉ 1: UP (en premier)
        Point2D.Double voisinUp = Direction.UP.apply(courant, espacement);
        Point2D.Double voisinValide = verifierVoisinValide(courant, voisinUp, grille, visites, chemin,
                longueurAccumulee);
        if (voisinValide != null) {
            return voisinValide;
        }

        // PRIORITÉ 2: RIGHT
        Point2D.Double voisinRight = Direction.RIGHT.apply(courant, espacement);
        voisinValide = verifierVoisinValide(courant, voisinRight, grille, visites, chemin, longueurAccumulee);
        if (voisinValide != null) {
            return voisinValide;
        }

        // PRIORITÉ 3: LEFT
        Point2D.Double voisinLeft = Direction.LEFT.apply(courant, espacement);
        voisinValide = verifierVoisinValide(courant, voisinLeft, grille, visites, chemin, longueurAccumulee);
        if (voisinValide != null) {
            return voisinValide;
        }

        // PRIORITÉ 4: DOWN (en dernier)
        Point2D.Double voisinDown = Direction.DOWN.apply(courant, espacement);
        voisinValide = verifierVoisinValide(courant, voisinDown, grille, visites, chemin, longueurAccumulee);
        if (voisinValide != null) {
            return voisinValide;
        }

        // Aucun voisin valide dans les directions prioritaires
        return null;
    }

    /**
     * Vérifie si un voisin est valide selon les contraintes
     */
    private Point2D.Double verifierVoisinValide(Point2D.Double courant,
            Point2D.Double voisin,
            Map<String, Point2D.Double> grille,
            Set<String> visites,
            List<Point2D.Double> chemin,
            double longueurAccumulee) {
        String key = keyFromPoint(voisin);

        // Vérifier que le voisin est valide
        if (!grille.containsKey(key))
            return null;
        if (visites.contains(key))
            return null;
        if (!isPointAllowedEvenIfManual(voisin) && !estPointValide(voisin))
            return null;

        // Vérifier la longueur de fil
        double dist = distance(courant, voisin);
        if (longueurAccumulee + dist > longueurFilMax)
            return null;

        // Vérifier qu'il n'y a pas de croisement
        if (!verifierPasDeCroisement(courant, voisin, chemin))
            return null;

        return voisin;
    }

    /**
     * Saute vers le nœud non visité qui permet d'atteindre le maximum
     * d'intersections
     * Utilisé quand il n'y a plus de voisins adjacents disponibles
     * Teste plusieurs candidats et choisit celui qui maximise le nombre de nœuds
     * atteignables
     */
    /**
     * Essaie le backtracking : retire le dernier nœud et essaie une autre direction
     * depuis l'avant-dernier nœud pour voir si on peut atteindre d'autres nœuds
     * 
     * @param chemin      Le chemin actuel (sera modifié si le backtracking réussit)
     * @param grille      La grille de tous les points valides
     * @param visites     L'ensemble des nœuds visités (sera modifié si le
     *                    backtracking réussit)
     * @param longueurRef Référence à la longueur accumulée (sera modifiée si le
     *                    backtracking réussit)
     * @return Le nouveau nœud trouvé, ou null si le backtracking n'a pas fonctionné
     */
    private Point2D.Double essayerBacktracking(List<Point2D.Double> chemin,
            Map<String, Point2D.Double> grille,
            Set<String> visites,
            double[] longueurRef) {

        if (chemin.size() < 2) {
            return null; // Pas assez de points pour faire du backtracking
        }

        // Essayer de reculer jusqu'à 5 nœuds en arrière pour trouver un nouveau chemin
        // (limité à 5 pour éviter les boucles infinies)
        int maxBacktrack = Math.min(5, chemin.size() - 1);
        List<Point2D.Double> pointsRetires = new ArrayList<>();
        List<String> keysRetires = new ArrayList<>();
        double longueurRetiree = 0.0;

        for (int backtrackLevel = 1; backtrackLevel <= maxBacktrack; backtrackLevel++) {
            if (chemin.size() < 2) {
                break; // Plus assez de points
            }

            // Retirer le dernier nœud temporairement
            Point2D.Double dernierPoint = chemin.remove(chemin.size() - 1);
            String dernierKey = keyFromPoint(dernierPoint);
            visites.remove(dernierKey);
            pointsRetires.add(dernierPoint);
            keysRetires.add(dernierKey);

            // Recalculer la longueur (retirer la distance du dernier segment)
            if (chemin.size() >= 1) {
                double distDernierSegment = distance(chemin.get(chemin.size() - 1), dernierPoint);
                longueurRetiree += distDernierSegment;
                longueurRef[0] -= distDernierSegment;
            }

            Point2D.Double pointActuel = chemin.get(chemin.size() - 1);

            // Essayer toutes les directions depuis ce point (sauf vers les points qu'on
            // vient de retirer)
            // PRIORITÉ: 1-UP, 2-RIGHT, 3-LEFT, 4-DOWN
            Direction[] directions = { Direction.UP, Direction.RIGHT, Direction.LEFT, Direction.DOWN };

            for (Direction dir : directions) {
                Point2D.Double nouveauPoint = dir.apply(pointActuel, espacement);

                // Ignorer si c'est un des points qu'on vient de retirer
                boolean estPointRetire = false;
                for (Point2D.Double pointRetire : pointsRetires) {
                    if (pointsEgaux(nouveauPoint, pointRetire)) {
                        estPointRetire = true;
                        break;
                    }
                }
                if (estPointRetire) {
                    continue;
                }

                // Vérifier que ce nouveau point est valide
                Point2D.Double voisinValide = verifierVoisinValide(pointActuel, nouveauPoint, grille, visites,
                        chemin, longueurRef[0]);
                if (voisinValide != null) {
                    // On a trouvé un nouveau chemin possible !
                    System.out.println("[v0] Backtracking réussi après " + backtrackLevel +
                            " nœud(s) : nouvelle direction trouvée");
                    return voisinValide;
                }
            }
        }

        // Aucune nouvelle direction trouvée, remettre tous les points retirés
        for (int i = pointsRetires.size() - 1; i >= 0; i--) {
            chemin.add(pointsRetires.get(i));
            visites.add(keysRetires.get(i));
        }
        longueurRef[0] += longueurRetiree;

        return null;
    }

    /**
     * Saute vers le nœud non visité le plus proche
     * Utilisé quand il n'y a plus de voisins adjacents disponibles
     * Trouve un chemin A* vers le nœud le plus proche et l'ajoute au chemin
     */
    private Point2D.Double sauterVersNoeudLePlusProche(Point2D.Double courant,
            Map<String, Point2D.Double> grille,
            Set<String> visites,
            List<Point2D.Double> chemin,
            double longueurAccumulee) {
        Point2D.Double plusProche = null;
        double distMin = Double.MAX_VALUE;
        List<Point2D.Double> meilleurCheminVers = null;

        // Chercher tous les nœuds non visités
        List<Point2D.Double> noeudsNonVisites = new ArrayList<>();
        for (Point2D.Double p : grille.values()) {
            String key = keyFromPoint(p);
            if (!visites.contains(key)) {
                noeudsNonVisites.add(p);
            }
        }

        if (noeudsNonVisites.isEmpty())
            return null;

        System.out.println("[v0] Recherche parmi " + noeudsNonVisites.size() + " nœuds non visités");

        // Trier par distance euclidienne pour optimiser la recherche
        noeudsNonVisites.sort((a, b) -> Double.compare(
                distance(courant, a),
                distance(courant, b)));

        int tentatives = Math.min(100, noeudsNonVisites.size());
        for (int i = 0; i < tentatives; i++) {
            Point2D.Double cible = noeudsNonVisites.get(i);

            // Utiliser A* pour trouver un chemin
            List<Point2D.Double> cheminVers = chercherCheminEvitantVisites(courant, cible, visites);

            if (cheminVers.isEmpty() || cheminVers.size() < 2)
                continue;

            // Calculer la longueur du chemin (exclure le premier point qui est déjà dans le
            // chemin)
            double longueurChemin = 0;
            for (int j = 1; j < cheminVers.size(); j++) {
                longueurChemin += distance(cheminVers.get(j - 1), cheminVers.get(j));
            }

            if (longueurAccumulee + longueurChemin > longueurFilMax)
                continue;

            // Vérifier que le chemin ne croise pas le chemin existant ET qu'il n'y a pas de
            // segments diagonaux
            boolean valide = true;
            for (int j = 0; j < cheminVers.size() - 1; j++) {
                // Vérifier qu'il n'y a pas de segment diagonal
                if (estSegmentDiagonal(cheminVers.get(j), cheminVers.get(j + 1))) {
                    valide = false;
                    break;
                }
                // Vérifier qu'il n'y a pas de croisement
                if (!verifierPasDeCroisement(cheminVers.get(j), cheminVers.get(j + 1), chemin)) {
                    valide = false;
                    break;
                }
            }
            if (!valide)
                continue;

            // Ce chemin est valide, vérifier s'il est meilleur
            if (longueurChemin < distMin) {
                distMin = longueurChemin;
                plusProche = cible;
                meilleurCheminVers = cheminVers;
            }
        }

        // Si on a trouvé un chemin, ajouter tous les points intermédiaires au chemin
        // principal
        if (meilleurCheminVers != null && meilleurCheminVers.size() > 1) {
            // Ajouter tous les points intermédiaires (sauf le premier qui est déjà dans le
            // chemin)
            for (int i = 1; i < meilleurCheminVers.size() - 1; i++) {
                Point2D.Double pointIntermediaire = meilleurCheminVers.get(i);
                chemin.add(pointIntermediaire);
                visites.add(keyFromPoint(pointIntermediaire));
            }
            System.out.println("[v0] Saut effectué via " + (meilleurCheminVers.size() - 2) +
                    " points intermédiaires vers nœud à distance " +
                    String.format("%.2f", distMin));
        }

        return plusProche;
    }

    /**
     * Nouvelle méthode: cherche n'importe quel nœud accessible sans restriction
     * stricte
     * Utilisé en dernier recours pour atteindre les zones isolées
     */
    private Point2D.Double trouverNimporteQuelNoeudAccessible(Point2D.Double courant,
            Map<String, Point2D.Double> grille,
            Set<String> visites,
            List<Point2D.Double> chemin,
            double longueurAccumulee) {
        System.out.println("[v0] Recherche en dernier recours...");

        // Chercher tous les nœuds non visités
        List<Point2D.Double> noeudsNonVisites = new ArrayList<>();
        for (Point2D.Double p : grille.values()) {
            String key = keyFromPoint(p);
            if (!visites.contains(key)) {
                noeudsNonVisites.add(p);
            }
        }

        if (noeudsNonVisites.isEmpty())
            return null;

        // Créer une référence finale pour la lambda
        final Point2D.Double courantFinal = courant;
        // Trier par distance
        noeudsNonVisites.sort((a, b) -> Double.compare(
                distance(courantFinal, a),
                distance(courantFinal, b)));

        // Essayer TOUS les nœuds disponibles
        for (Point2D.Double cible : noeudsNonVisites) {
            // D'abord essayer un segment direct si possible (plus rapide)
            // MAIS seulement si ce n'est pas diagonal
            double distDirecte = distance(courantFinal, cible);
            if (longueurAccumulee + distDirecte <= longueurFilMax &&
                    !estSegmentDiagonal(courantFinal, cible) &&
                    estSegmentValide(courantFinal, cible) &&
                    verifierPasDeCroisement(courantFinal, cible, chemin)) {
                System.out.println("[v0] Dernier recours: connexion directe trouvée vers nœud à distance " +
                        String.format("%.2f", distDirecte));
                return cible;
            }

            // Sinon, utiliser A* pour trouver un chemin
            List<Point2D.Double> cheminVers = chercherCheminEvitantVisites(courantFinal, cible, visites);

            if (cheminVers.isEmpty() || cheminVers.size() < 2)
                continue;

            // Calculer la longueur du chemin
            double longueurChemin = 0;
            for (int j = 1; j < cheminVers.size(); j++) {
                longueurChemin += distance(cheminVers.get(j - 1), cheminVers.get(j));
            }

            if (longueurAccumulee + longueurChemin > longueurFilMax)
                continue;

            // Vérifier STRICTEMENT qu'il n'y a AUCUN croisement avec le chemin existant ET
            // qu'il n'y a pas de segments diagonaux
            boolean valide = true;
            for (int j = 0; j < cheminVers.size() - 1; j++) {
                // Vérifier qu'il n'y a pas de segment diagonal
                if (estSegmentDiagonal(cheminVers.get(j), cheminVers.get(j + 1))) {
                    valide = false;
                    break;
                }
                // Vérifier qu'il n'y a pas de croisement
                if (!verifierPasDeCroisement(cheminVers.get(j), cheminVers.get(j + 1), chemin)) {
                    valide = false;
                    break;
                }
            }

            if (!valide)
                continue;

            // Ajouter tous les points intermédiaires avec vérification de croisement
            if (cheminVers.size() > 1) {
                Point2D.Double precedent = courantFinal;
                boolean croisementDetecte = false;
                for (int i = 1; i < cheminVers.size() - 1; i++) {
                    Point2D.Double pointIntermediaire = cheminVers.get(i);
                    // Vérifier qu'il n'y a pas de segment diagonal
                    if (estSegmentDiagonal(precedent, pointIntermediaire)) {
                        croisementDetecte = true;
                        break;
                    }
                    // Vérifier que ce segment ne croise pas le chemin existant
                    if (!verifierPasDeCroisement(precedent, pointIntermediaire, chemin)) {
                        croisementDetecte = true;
                        break;
                    }
                    chemin.add(pointIntermediaire);
                    visites.add(keyFromPoint(pointIntermediaire));
                    precedent = pointIntermediaire;
                }
                if (croisementDetecte) {
                    // Annuler les ajouts et continuer avec le prochain candidat
                    continue;
                }
                // Vérifier aussi le dernier segment vers la cible (pas diagonal et pas de
                // croisement)
                if (estSegmentDiagonal(precedent, cible) || !verifierPasDeCroisement(precedent, cible, chemin)) {
                    continue; // Rejeter ce candidat
                }
                System.out.println("[v0] Dernier recours: chemin trouvé via " +
                        (cheminVers.size() - 2) + " points intermédiaires vers nœud isolé");
                return cible;
            }
        }

        return null;
    }

    /**
     * Version modifiée de A* qui évite les nœuds déjà visités
     * Mais autorise les points intermédiaires pour créer un chemin de connexion
     */
    private List<Point2D.Double> chercherCheminEvitantVisites(Point2D.Double debut,
            Point2D.Double fin,
            Set<String> visites) {
        // Si c'est un voisin direct (adjacent, pas diagonal), retourner directement
        // Vérifier que ce n'est pas diagonal ET que c'est un segment valide
        if (distance(debut, fin) <= espacement * 1.5 &&
                !estSegmentDiagonal(debut, fin) &&
                estSegmentValide(debut, fin)) {
            List<Point2D.Double> chemin = new ArrayList<>();
            chemin.add(debut);
            chemin.add(fin);
            return chemin;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(debut, null, 0, distance(debut, fin));
        openSet.add(startNode);

        int maxIterations = 5000;
        int iterations = 0;

        while (!openSet.isEmpty() && iterations++ < maxIterations) {
            Node current = openSet.poll();
            String curKey = keyFromPoint(current.position);

            // Arrivé à destination
            if (distance(current.position, fin) < espacement * 1.5) {
                return reconstruireChemin(current);
            }

            closedSet.add(curKey);

            for (Point2D.Double voisin : obtenirVoisinsAdjacentsTous(current.position)) {
                String vKey = keyFromPoint(voisin);
                if (closedSet.contains(vKey))
                    continue;

                // Autoriser les points déjà visités pour créer un chemin de connexion
                // MAIS pénaliser fortement leur utilisation
                if (!isPointAllowedEvenIfManual(voisin) && !estPointValide(voisin))
                    continue;

                double dist = distance(current.position, voisin);
                double nouveauG = current.gCost + dist;

                // Ajouter un bonus selon la direction pour respecter la priorité
                // PRIORITÉ: 1-UP, 2-RIGHT, 3-LEFT, 4-DOWN
                // Plus le bonus est négatif, plus la direction est prioritaire
                double dx = voisin.x - current.position.x;
                double dy = voisin.y - current.position.y;
                if (Math.abs(dy) > Math.abs(dx)) {
                    // Mouvement vertical
                    if (dy < 0) {
                        // UP - priorité 1
                        nouveauG -= 0.001;
                    } else {
                        // DOWN - priorité 4
                        nouveauG += 0.001;
                    }
                } else {
                    // Mouvement horizontal
                    if (dx > 0) {
                        // RIGHT - priorité 2
                        nouveauG -= 0.0005;
                    } else {
                        // LEFT - priorité 3
                        nouveauG -= 0.0003;
                    }
                }

                // Pénaliser les points déjà visités (mais les autoriser quand même)
                if (visites.contains(vKey) && !pointsEgaux(voisin, fin)) {
                    nouveauG += 1000; // Grande pénalité
                }

                double h = distance(voisin, fin);

                boolean deja = false;
                for (Node n : openSet) {
                    if (pointsEgaux(n.position, voisin)) {
                        deja = true;
                        if (nouveauG < n.gCost) {
                            n.gCost = nouveauG;
                            n.parent = current;
                            n.fCost = n.gCost + n.hCost;
                        }
                        break;
                    }
                }
                if (!deja)
                    openSet.add(new Node(voisin, current, nouveauG, h));
            }
        }
        return new ArrayList<>();
    }

    /**
     * Trouve le point de départ du thermostat
     * Utilise la position réelle du thermostat de la pièce, ou le point le plus
     * proche dans la grille
     */
    private Point2D.Double trouverPointThermostat(Map<String, Point2D.Double> grille) {
        List<Point2D.Double> points = new ArrayList<>(grille.values());
        if (points.isEmpty()) {
            throw new RuntimeException("Aucun point valide dans la grille");
        }

        // Vérifier si la pièce a un thermostat
        double thermostatX = 0;
        double thermostatY = 0;
        boolean aThermostat = false;

        if (piece instanceof PieceIrreguliere) {
            PieceIrreguliere pieceIrreg = (PieceIrreguliere) piece;
            Thermostat thermostat = pieceIrreg.getThermostatIrregulier();
            if (thermostat != null) {
                thermostatX = thermostat.getX();
                thermostatY = thermostat.getY();
                aThermostat = true;
            }
        } else if (piece.getThermostat() != null) {
            thermostatX = piece.getThermostat().getX();
            thermostatY = piece.getThermostat().getY();
            aThermostat = true;
        }

        if (aThermostat) {
            // Trouver le point de grille le plus proche du thermostat
            Point2D.Double thermostatPoint = new Point2D.Double(thermostatX, thermostatY);
            Point2D.Double plusProche = points.get(0);
            double distanceMin = distance(thermostatPoint, plusProche);

            for (Point2D.Double p : points) {
                double dist = distance(thermostatPoint, p);
                if (dist < distanceMin) {
                    distanceMin = dist;
                    plusProche = p;
                }
            }

            // Si le thermostat est très proche d'un point de grille, utiliser ce point
            // Sinon, ajouter le thermostat lui-même au chemin (il sera le premier point)
            if (distanceMin < espacement * 0.5) {
                return plusProche;
            } else {
                // Le thermostat n'est pas exactement sur un point de grille
                // Retourner le point le plus proche quand même
                return plusProche;
            }
        }

        // Pas de thermostat? Trouver le point le plus en haut à gauche
        Point2D.Double thermostat = points.get(0);
        for (Point2D.Double p : points) {
            if (p.y < thermostat.y || (p.y == thermostat.y && p.x < thermostat.x)) {
                thermostat = p;
            }
        }

        return thermostat;
    }

    /**
     * Vérifie qu'un nouveau segment ne croise pas le chemin existant
     */
    private boolean verifierPasDeCroisement(Point2D.Double p1, Point2D.Double p2,
            List<Point2D.Double> chemin) {
        if (chemin.size() < 2)
            return true;

        for (int i = 0; i < chemin.size() - 1; i++) {
            Point2D.Double s1 = chemin.get(i);
            Point2D.Double s2 = chemin.get(i + 1);

            // Ignorer si les segments partagent un point
            if (pointsEgaux(p1, s1) || pointsEgaux(p1, s2) ||
                    pointsEgaux(p2, s1) || pointsEgaux(p2, s2))
                continue;

            if (segmentsSeCroisent(p1, p2, s1, s2)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Vérifie si deux segments se croisent
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

    private double direction(Point2D.Double p1, Point2D.Double p2, Point2D.Double p) {
        return (p.x - p1.x) * (p2.y - p1.y) - (p.y - p1.y) * (p2.x - p1.x);
    }

    /**
     * Génère la grille complète de tous les points valides
     * Utilise exactement la même logique que DrawingPanel.genererGraphe()
     */
    private Map<String, Point2D.Double> genererGrilleMap() {
        Map<String, Point2D.Double> map = new LinkedHashMap<>();

        // Générer la grille exactement comme DrawingPanel
        if (piece instanceof PieceIrreguliere) {
            // Pour les pièces irrégulières: utiliser minX/minY à maxX/maxY
            PieceIrreguliere pieceIrreg = (PieceIrreguliere) piece;
            double minX = pieceIrreg.getMinX();
            double minY = pieceIrreg.getMinY();
            double maxX = pieceIrreg.getMaxX();
            double maxY = pieceIrreg.getMaxY();

            // Générer la grille avec translations appliquées (comme DrawingPanel ligne
            // 331-340)
            for (double x = minX; x <= maxX; x += espacement) {
                for (double y = minY; y <= maxY; y += espacement) {
                    // Appliquer les translations comme dans DrawingPanel
                    Point2D.Double p = new Point2D.Double(x + translationX, y + translationY);

                    // Vérifier que le point est valide (dans la pièce, pas dans un meuble, etc.)
                    if (allowManualCrossing || estPointValide(p) || manualAllowedPoints.contains(keyFromPoint(p))) {
                        String key = keyFromPoint(p);
                        map.put(key, p);
                    }
                }
            }
        } else {
            // Pour les pièces régulières: utiliser 0 à largeur/longueur
            double largeur = piece.getLargeur();
            double longueur = piece.getLongueur();

            // Générer la grille avec translations appliquées (comme DrawingPanel ligne
            // 381-390)
            for (double x = 0; x <= largeur; x += espacement) {
                for (double y = 0; y <= longueur; y += espacement) {
                    // Appliquer les translations comme dans DrawingPanel
                    Point2D.Double p = new Point2D.Double(x + translationX, y + translationY);

                    // Vérifier que le point est valide (dans la pièce, pas dans un meuble, etc.)
                    if (allowManualCrossing || estPointValide(p) || manualAllowedPoints.contains(keyFromPoint(p))) {
                        String key = keyFromPoint(p);
                        map.put(key, p);
                    }
                }
            }
        }
        return map;
    }

    /**
     * Trouve le point le plus proche d'une coordonnée de référence
     */
    private Point2D.Double trouverPointProche(List<Point2D.Double> points, double refX, double refY) {
        Point2D.Double reference = new Point2D.Double(refX, refY);
        Point2D.Double meilleur = points.get(0);
        double meilleureDist = Double.MAX_VALUE;

        for (Point2D.Double p : points) {
            double d = distance(p, reference);
            if (d < meilleureDist) {
                meilleureDist = d;
                meilleur = p;
            }
        }
        return meilleur;
    }

    private List<Point2D.Double> obtenirVoisinsAdjacentsTous(Point2D.Double p) {
        List<Point2D.Double> voisins = new ArrayList<>();

        // Calculer les voisins en tenant compte des translations
        // Les points dans la grille sont déjà translatés, donc on ajoute juste
        // l'espacement
        // PRIORITÉ: 1-UP, 2-RIGHT, 3-LEFT, 4-DOWN
        // UP
        voisins.add(new Point2D.Double(p.x, p.y - espacement));
        // RIGHT
        voisins.add(new Point2D.Double(p.x + espacement, p.y));
        // LEFT
        voisins.add(new Point2D.Double(p.x - espacement, p.y));
        // DOWN
        voisins.add(new Point2D.Double(p.x, p.y + espacement));

        return voisins;
    }

    public List<Point2D.Double> chercherChemin(Point2D.Double debut, Point2D.Double fin) {
        // Vérifier que ce n'est pas diagonal ET que c'est un segment valide
        if (distance(debut, fin) <= espacement * 1.5 &&
                !estSegmentDiagonal(debut, fin) &&
                estSegmentValide(debut, fin)) {
            List<Point2D.Double> chemin = new ArrayList<>();
            chemin.add(debut);
            chemin.add(fin);
            return chemin;
        }

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(debut, null, 0, distance(debut, fin));
        openSet.add(startNode);

        int maxIterations = 20000;
        int iterations = 0;

        while (!openSet.isEmpty() && iterations++ < maxIterations) {
            Node current = openSet.poll();
            String curKey = keyFromPoint(current.position);
            if (distance(current.position, fin) < espacement * 1.5) {
                return reconstruireChemin(current);
            }
            closedSet.add(curKey);

            for (Point2D.Double voisin : obtenirVoisinsAdjacentsTous(current.position)) {
                String vKey = keyFromPoint(voisin);
                if (closedSet.contains(vKey))
                    continue;
                if (!isPointAllowedEvenIfManual(voisin) && !estPointValide(voisin))
                    continue;

                double dist = distance(current.position, voisin);
                double nouveauG = current.gCost + dist;

                // Ajouter un bonus selon la direction pour respecter la priorité
                // PRIORITÉ: 1-UP, 2-RIGHT, 3-LEFT, 4-DOWN
                // Plus le bonus est négatif, plus la direction est prioritaire
                double dx = voisin.x - current.position.x;
                double dy = voisin.y - current.position.y;
                if (Math.abs(dy) > Math.abs(dx)) {
                    // Mouvement vertical
                    if (dy < 0) {
                        // UP - priorité 1
                        nouveauG -= 0.001;
                    } else {
                        // DOWN - priorité 4
                        nouveauG += 0.001;
                    }
                } else {
                    // Mouvement horizontal
                    if (dx > 0) {
                        // RIGHT - priorité 2
                        nouveauG -= 0.0005;
                    } else {
                        // LEFT - priorité 3
                        nouveauG -= 0.0003;
                    }
                }

                double h = distance(voisin, fin);

                boolean deja = false;
                for (Node n : openSet) {
                    if (pointsEgaux(n.position, voisin)) {
                        deja = true;
                        if (nouveauG < n.gCost) {
                            n.gCost = nouveauG;
                            n.parent = current;
                            n.fCost = n.gCost + n.hCost;
                        }
                        break;
                    }
                }
                if (!deja)
                    openSet.add(new Node(voisin, current, nouveauG, h));
            }
        }
        return new ArrayList<>();
    }

    private List<Point2D.Double> reconstruireChemin(Node node) {
        List<Point2D.Double> chemin = new ArrayList<>();
        while (node != null) {
            chemin.add(0, node.position);
            node = node.parent;
        }
        return chemin;
    }

    /**
     * Vérifie si un segment est diagonal (les deux coordonnées changent)
     * Un segment est diagonal si les deux coordonnées changent significativement
     * Les segments adjacents (UP, RIGHT, LEFT, DOWN) ont toujours soit dx=0 soit
     * dy=0
     */
    private boolean estSegmentDiagonal(Point2D.Double p1, Point2D.Double p2) {
        double dx = Math.abs(p2.x - p1.x);
        double dy = Math.abs(p2.y - p1.y);
        // Un segment est diagonal si les deux coordonnées changent significativement
        // Tolérance pour les erreurs d'arrondi (plus grande pour éviter les faux
        // positifs)
        double tolerance = espacement * 0.1; // 10% de l'espacement
        // Un segment est diagonal si dx ET dy sont tous les deux significativement > 0
        return dx > tolerance && dy > tolerance;
    }

    private boolean estSegmentValide(Point2D.Double p1, Point2D.Double p2) {
        if (!estPointValide(p1) || !estPointValide(p2))
            return false;

        // Interdire les segments diagonaux
        if (estSegmentDiagonal(p1, p2))
            return false;

        int numTests = 10;
        for (int i = 1; i < numTests; i++) {
            double t = (double) i / numTests;
            double x = p1.x + t * (p2.x - p1.x);
            double y = p1.y + t * (p2.y - p1.y);
            if (!estPointValide(new Point2D.Double(x, y)))
                return false;
        }
        return true;
    }

    private boolean estPointValide(Point2D.Double p) {
        // Vérifier que le point est dans les limites de la pièce
        // Pour les pièces irrégulières, vérifier que le point est réellement dans le
        // polygone
        if (piece instanceof PieceIrreguliere) {
            PieceIrreguliere pieceIrreg = (PieceIrreguliere) piece;
            if (!pieceIrreg.contientPoint(p.x, p.y)) {
                return false;
            }

            // Vérifier la distance aux murs (au moins 3 pouces) pour les pièces
            // irrégulières
            double distanceMinAuMur = calculerDistanceMinAuMur(p.x, p.y, pieceIrreg.getPoints());
            if (distanceMinAuMur < 3.0) {
                return false;
            }
        } else {
            // Pour les pièces régulières, vérifier la bounding box avec marge de 3 pouces
            if (p.x < piece.getMinX() + 3 || p.x > piece.getMaxX() - 3)
                return false;
            if (p.y < piece.getMinY() + 3 || p.y > piece.getMaxY() - 3)
                return false;
        }

        // Vérifier que le point n'est pas dans un meuble sans drain
        for (MeubleSansDrain meuble : piece.getMeubles()) {
            if (estPointDansMeuble(p, meuble))
                return false;
        }

        // Vérifier que le point n'est pas dans un meuble avec drain
        for (MeubleDrain meuble : piece.getMeublesDrain()) {
            if (estPointDansMeubleDrain(p, meuble))
                return false;
        }

        return true;
    }

    /**
     * Calcule la distance minimale d'un point aux murs (segments) d'une pièce
     * irrégulière
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

    private boolean estPointDansMeuble(Point2D.Double p, MeubleSansDrain meuble) {
        return p.x >= meuble.getX() && p.x <= meuble.getX() + meuble.getLargeur() &&
                p.y >= meuble.getY() && p.y <= meuble.getY() + meuble.getHauteur();
    }

    private boolean estPointDansMeubleDrain(Point2D.Double p, MeubleDrain meuble) {
        return p.x >= meuble.getX() && p.x <= meuble.getX() + meuble.getLargeur() &&
                p.y >= meuble.getY() && p.y <= meuble.getY() + meuble.getHauteur();
    }

    private boolean isPointAllowedEvenIfManual(Point2D.Double p) {
        return allowManualCrossing || manualAllowedPoints.contains(keyFromPoint(p));
    }

    private boolean pointsEgaux(Point2D.Double p1, Point2D.Double p2) {
        return Math.abs(p1.x - p2.x) < 0.01 && Math.abs(p1.y - p2.y) < 0.01;
    }

    private double distance(Point2D.Double p1, Point2D.Double p2) {
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    private String keyFromPoint(Point2D.Double p) {
        return keyFromIndices(coordToIndex(p.x), coordToIndex(p.y));
    }

    private String keyFromIndices(int ix, int iy) {
        return ix + "," + iy;
    }

    private int coordToIndex(double coord) {
        return (int) Math.round(coord / espacement);
    }

    private double indexToCoord(int index) {
        return index * espacement;
    }

    /**
     * Compte le nombre de voisins non visités pour un point donné
     * Utilisé pour choisir intelligemment le prochain nœud à visiter
     */
    private int compterVoisinsNonVisites(Point2D.Double point,
            Map<String, Point2D.Double> grille,
            Set<String> visites) {
        int count = 0;

        // Vérifier les 8 directions
        for (Direction dir : Direction.values()) {
            Point2D.Double voisin = dir.apply(point, espacement);
            String key = keyFromPoint(voisin);

            if (grille.containsKey(key) && !visites.contains(key)) {
                // Ce voisin existe dans la grille et n'a pas été visité
                if (isPointAllowedEvenIfManual(voisin) || estPointValide(voisin)) {
                    count++;
                }
            }
        }

        return count;
    }

    /**
     * Calcule la longueur totale du chemin
     */
    private double calculerLongueurChemin(List<Point2D.Double> chemin) {
        if (chemin.size() < 2)
            return 0.0;

        double longueur = 0.0;
        for (int i = 0; i < chemin.size() - 1; i++) {
            longueur += distance(chemin.get(i), chemin.get(i + 1));
        }

        return longueur;
    }
}
