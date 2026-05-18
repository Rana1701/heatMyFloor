/*
package com.mycompany.equipe43.Domaine;

import java.awt.Dimension;
import java.awt.Point;


public abstract class Meuble extends Element {
    private final int id;

    public Meuble(int id, Point position, Dimension taille) {
        super(position, taille);
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
*/
package com.mycompany.equipe43.Domaine;

/**
 * Classe abstraite représentant un meuble.
 * Chaque meuble possède un identifiant unique et des dimensions en pouces.
 * 
 * @author hanaw
 */
public abstract class Meuble extends Element {
    private final int id;  // ✅ ID reste int (c'est un compteur, pas une mesure!)
    
    public Meuble(int id,
                  double xEnPouces,
                  double yEnPouces,
                  double largeurEnPouces,
                  double hauteurEnPouces) {
        super(xEnPouces, yEnPouces, largeurEnPouces, hauteurEnPouces);
        this.id = id;
    }
    
    public int getId() {  
        return id;
    }
}