package com.mycompany.equipe43.Domaine;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author douae
 */
public class PieceIrreguliere extends Piece {
    private List <Point2D.Double> points;
    private boolean estFermee = false;
    
    //pour les meubles (meme logique que piece)
    private List<MeubleSansDrain> meubles = new ArrayList<>();
    private List<MeubleDrain> meublesDrain = new ArrayList<>();
    private List<ElementChauffant> chauffages = new ArrayList<>();
    private int nextId = 1;
    private Thermostat thermostat; // Singleton : une seule instance

public PieceIrreguliere() {
    super(0, 0, 0, 0); // Appelle le constructeur de Piece
    this.points = new ArrayList<>();
}
    
    public PieceIrreguliere(List<Point2D.Double> points){
        super(0,0,0,0);
        this.points = new ArrayList<>(points);
        if(points.size()>= 3){
            this.estFermee = true;
        }
    }
     //gestion des points
    public void ajouterPoint(double x, double y){
        points.add(new Point2D.Double(x,y));
    }
    
    public List<Point2D.Double> getPoints(){
        return new ArrayList<>(points);
    }
    
    public int getNombrePoints(){
        return points.size();
    }
    
    public void fermerPolygone (){
        if(points.size()>= 3){
            estFermee= true;
        }
    }
    
    public boolean estFermee(){
        return estFermee;
    }
    
    public void supprimerPoint(int index){
        if (index >= 0 && index < points.size()){
            points.remove(index);
        }
    }
    
    public void deplacerPoint(int index, double nouveauX, double nouveauY){
        if (index >= 0 && index < points.size()){
            points.get(index).setLocation(nouveauX, nouveauY);
        }
        
    }
    
    //bounding box
    public double getMinX(){
        if(points.isEmpty()) return 0 ;
        return points.stream().mapToDouble(p -> p.x).min().orElse(0);
    }
    
    public double getMinY(){
        if(points.isEmpty()) return 0 ;
        return points.stream().mapToDouble(p -> p.y).min().orElse(0);
    }
    
    public double getMaxX(){
        if(points.isEmpty()) return 0 ;
        return points.stream().mapToDouble(p -> p.x).max().orElse(0);
    }
    
    public double getMaxY(){
        if(points.isEmpty()) return 0 ;
        return points.stream().mapToDouble(p -> p.y).max().orElse(0);
    }
    
    public double getLargeur(){
        return getMaxX() - getMinX();
    }
    
    public double getLongueur(){
        return getMaxY() - getMinY();
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
    
    /**
     * Vérifie si un rectangle (meuble) est complètement à l'intérieur du polygone
     */
    public boolean contientRectangle(double x, double y, double largeur, double hauteur) {
        if (points.isEmpty() || !estFermee) return false;
        
        // Vérifier que les 4 coins du rectangle sont dans le polygone
        boolean coinHautGauche = contientPoint(x, y);
        boolean coinHautDroit = contientPoint(x + largeur, y);
        boolean coinBasGauche = contientPoint(x, y + hauteur);
        boolean coinBasDroit = contientPoint(x + largeur, y + hauteur);
        
        // Tous les coins doivent être à l'intérieur
        return coinHautGauche && coinHautDroit && coinBasGauche && coinBasDroit;
    }
    
    //gestion des meubles
     public List<MeubleSansDrain> getMeubles() {
        return meubles;
    }
    
    public List<MeubleDrain> getMeublesDrain() {
        return meublesDrain;
    }
    
    public List<ElementChauffant> getElementsChauffants() {
        return chauffages;
    }
    
    public MeubleSansDrain ajouterMeuble(double x, double y,
                                         double largeur, double hauteur,
                                         TypeMeubleSansDrain type) {
        MeubleSansDrain m = new MeubleSansDrain(nextId++, x, y, largeur, hauteur, type);
        meubles.add(m);
        return m;
    }
    
    public MeubleDrain ajouterMeubleDrain(double x, double y,
                                          double largeur, double hauteur,
                                          TypeMeubleDrain type,
                                          double xDrainRelatif, double yDrainRelatif,
                                          double diametreDrain) {
        MeubleDrain m = new MeubleDrain(nextId++, x, y, largeur, hauteur, type,
                                        xDrainRelatif, yDrainRelatif, diametreDrain);
        meublesDrain.add(m);
        return m;
    }
    
    public boolean supprimerMeubleParId(int id) {
        boolean removed = meubles.removeIf(m -> m.getId() == id);
        if (removed) return true;
        return meublesDrain.removeIf(m -> m.getId() == id);
    }
    
    //Redimensionnement proportionnel
    public void redimensionnerProportionnel(double facteurX, double facteurY){
        if (points.isEmpty()) return;
        
        //calculer centre de polygone
        double centreX = 0, centreY = 0;
        for(Point2D.Double p : points){
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= points.size();
        centreY /= points.size();
        
        //redimensionner chaque points par rapport au centre
        for(Point2D.Double p : points){
            double deltaX = p.x - centreX;
            double deltaY = p.y - centreY;
            p.x = centreX + deltaX * facteurX;
            p.y = centreY + deltaY * facteurY;
            
        }
    }
    
    //copie
    public PieceIrreguliere copier() {
        PieceIrreguliere copie = new PieceIrreguliere(this.points);
        copie.estFermee = this.estFermee;
        copie.nextId = this.nextId;
        
        // Copier les meubles
        for (MeubleSansDrain m : this.meubles) {
            copie.meubles.add(new MeubleSansDrain(m.getId(), m.getX(), m.getY(),
                                                  m.getLargeur(), m.getHauteur(), m.getType()));
        }
        
        for (MeubleDrain m : this.meublesDrain) {
            copie.meublesDrain.add(new MeubleDrain(m.getId(), m.getX(), m.getY(),
                                                   m.getLargeur(), m.getHauteur(), m.getType(),
                                                   m.getXDrainRelatif(), m.getYDrainRelatif(),
                                                   m.getDiametreDrain()));
        }
        
        // Copier les éléments chauffants
        for (ElementChauffant e : this.chauffages) {
            // Ne pas copier le thermostat s'il est dans la liste (il sera copié séparément)
            if (!Thermostat.estThermostat(e)) {
                copie.chauffages.add(new ElementChauffant(
                    e.getX(), e.getY(),
                    e.getLargeur(), e.getHauteur(),
                    e.isHorizontal(), e.getAngle()
                ));
            }
        }
        
        // Copier le thermostat (singleton)
        if (this.thermostat != null) {
            copie.thermostat = Thermostat.creer(
                this.thermostat.getX(),
                this.thermostat.getY(),
                this.thermostat.getAngle()
            );
        }
        
        return copie;
    }
    
    /**
     * Retourne le thermostat (singleton) pour les pièces irrégulières
     * Note: Ne peut pas surcharger getThermostat() car le type de retour est différent
     * Utiliser cette méthode au lieu de getThermostat() pour les pièces irrégulières
     */
    public Thermostat getThermostatIrregulier() {
        return thermostat;
    }
    
    /**
     * Pour compatibilité avec Piece.getThermostat(), retourne null
     * Utiliser getThermostatIrregulier() à la place
     */
    @Override
    public ThermostatDTO getThermostat() {
        if (thermostat == null) return null;
        return new ThermostatDTO(
            thermostat.getX(),
            thermostat.getY(),
            thermostat.getLargeur(),
            thermostat.getHauteur(),
            thermostat.getAngle()
        );
    }
    
    /**
     * Surcharge de setThermostat() pour accepter Thermostat
     * Assure qu'il n'y a qu'une seule instance (singleton)
     */
    public void setThermostat(Thermostat t) {
        this.thermostat = t;
    }
    
    /**
     * Crée le thermostat par défaut lors de la création de la pièce irrégulière
     * Positionné sur le premier mur (segment entre le premier et le deuxième point)
     */
    public void creerThermostatParDefaut() {
        if (thermostat != null) return; // Déjà créé (singleton)
        
        if (points == null || points.size() < 2) {
            // Pas assez de points, créer au centre par défaut
            double cx = (getMinX() + getMaxX()) / 2.0;
            double cy = (getMinY() + getMaxY()) / 2.0;
            thermostat = Thermostat.creer(cx, cy, 0.0);
            return;
        }
        
        // Premier mur : segment entre le premier point (index 0) et le deuxième point (index 1)
        Point2D.Double p1 = points.get(0);
        Point2D.Double p2 = points.get(1);
        
        // Calculer le point milieu du premier segment
        double murX = (p1.x + p2.x) / 2.0;
        double murY = (p1.y + p2.y) / 2.0;
        
        // Calculer le vecteur du mur
        double dx = p2.x - p1.x;
        double dy = p2.y - p1.y;
        double longueurMur = Math.sqrt(dx * dx + dy * dy);
        
        if (longueurMur < 0.001) {
            // Segment dégénéré, créer au centre
            double cx = (getMinX() + getMaxX()) / 2.0;
            double cy = (getMinY() + getMaxY()) / 2.0;
            thermostat = Thermostat.creer(cx, cy, 0.0);
            return;
        }
        
        // Calculer le centre de la pièce pour déterminer la direction vers l'intérieur
        double centreX = 0, centreY = 0;
        for (Point2D.Double p : points) {
            centreX += p.x;
            centreY += p.y;
        }
        centreX /= points.size();
        centreY /= points.size();
        
        // Calculer le vecteur normal au mur (perpendiculaire)
        double normX = -dy / longueurMur;
        double normY = dx / longueurMur;
        
        // Vérifier si le vecteur normal pointe vers l'intérieur
        double dot = normX * (centreX - murX) + normY * (centreY - murY);
        if (dot < 0) {
            normX = -normX;
            normY = -normY;
        }
        
        // Dimensions du thermostat
        double largeurTh = 6.0;
        double hauteurTh = 4.0;
        
        // Calculer le centre du thermostat : sur le mur, décalé perpendiculairement vers l'intérieur
        double centreThermostatX = murX + normX * (hauteurTh / 2.0);
        double centreThermostatY = murY + normY * (hauteurTh / 2.0);
        
        // Calculer l'angle de la normale (perpendiculaire au mur, vers l'intérieur)
        // Utiliser la même logique que dans deplacerElementChauffantDragIrregulier
        // Pour le thermostat, on utilise directement Math.atan2(normY, normX) car la normale pointe déjà vers l'intérieur
        double angle = Math.atan2(normY, normX);
        
        // Calculer le coin supérieur gauche
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        double dxLocal = -largeurTh / 2.0;
        double dyLocal = -hauteurTh / 2.0;
        double dxGlobal = dxLocal * cosAngle - dyLocal * sinAngle;
        double dyGlobal = dxLocal * sinAngle + dyLocal * cosAngle;
        
        double finalX = centreThermostatX + dxGlobal;
        double finalY = centreThermostatY + dyGlobal;
        
        // Créer le thermostat avec la position et l'angle calculés
        thermostat = Thermostat.creer(finalX, finalY, angle);
    }
    
}
