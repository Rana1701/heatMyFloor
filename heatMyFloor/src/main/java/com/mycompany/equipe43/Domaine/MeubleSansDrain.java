
package com.mycompany.equipe43.Domaine;

public class MeubleSansDrain extends Meuble {
    private final TypeMeubleSansDrain type;
    
    public MeubleSansDrain(int id, double xEnPouces, double yEnPouces, 
                           double largeurEnPouces, double hauteurEnPouces, 
                           TypeMeubleSansDrain type) {
        super(id, xEnPouces, yEnPouces, largeurEnPouces, hauteurEnPouces);
        this.type = type;
    }
    
    public TypeMeubleSansDrain getType() {
        return type;
    }
}