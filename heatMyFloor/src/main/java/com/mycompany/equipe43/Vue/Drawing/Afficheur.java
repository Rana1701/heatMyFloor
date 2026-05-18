package com.mycompany.equipe43.Vue.Drawing;

import com.mycompany.equipe43.Domaine.DTO.MeubleDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import com.mycompany.equipe43.Domaine.DTO.PieceIrreguliereDTO;
import com.mycompany.equipe43.Domaine.Piece;
import java.awt.*;

public class Afficheur {
    
    // Ratio de conversion: 1 pouce = 10 pixels (AJUSTABLE)
    private double echelle = 5.0;// Échelle de base (pixels par pouce au zoom 1.0
    
    
  
    
    public double getPixelsParPouce (){
        return echelle;
    }
    //Methodes de conversion
    //convertir pouces -> pixels (avec zoom)
    public int poucesVersPixelsX(double pouces){
        return (int)(pouces * echelle);
    }
    
    public int poucesVersPixelsY(double pouces){
        return (int)(pouces * echelle);
    }
    
    //convertir pixels -> pouces (avec zoom)
    public double pixelsVersPoucesX(int pixels, double zoom){
        return pixels / (echelle * zoom);
    }
    
    public double pixelsVersPoucesY(int pixels, double zoom){
        return pixels / (echelle * zoom);
       
    }
    
    // Dessine la pièce (rectangle blanc)
    public void afficherPiece(Graphics2D g2, PieceDTO pieceDTO) {
        // Récupérer les valeurs en POUCES
        double xPouces = pieceDTO.getX();
        double yPouces = pieceDTO.getY();
        double largeurPouces = pieceDTO.getLargeur();
        double longueurPouces = pieceDTO.getLongueur();
        
        // CONVERTIR pouces -> pixels
        int xPixels = poucesVersPixelsX(xPouces);
        int yPixels = poucesVersPixelsY(yPouces);
        int largeurPixels = poucesVersPixelsX(largeurPouces);
        int longueurPixels = poucesVersPixelsY(longueurPouces);
        
        //g2.setColor(Color.WHITE);
        g2.setColor(new Color(180, 220, 255, 100));
        g2.fillRect(xPixels, yPixels, largeurPixels, longueurPixels);
        //g2.setColor(Color.BLACK);
        g2.setColor(new Color(40, 120, 200));
        g2.drawRect(xPixels, yPixels, largeurPixels, longueurPixels);
    }
    
    // Dessine tous les meubles sans drain
    public void afficherMeublesSansDrain(Graphics2D g2, PieceDTO pieceDTO) {
        for (MeubleSansDrainDTO meuble : pieceDTO.getMeubles()) {
            // Récupérer les valeurs en POUCES
            double xPouces = meuble.getX();
            double yPouces = meuble.getY();
            double largeurPouces = meuble.getLargeur();
            double hauteurPouces = meuble.getHauteur();
            
            // CONVERTIR pouces -> pixels
            int xPixels = poucesVersPixelsX(xPouces); 
            int yPixels = poucesVersPixelsY(yPouces); 
            int largeurPixels = poucesVersPixelsX(largeurPouces); 
            int hauteurPixels = poucesVersPixelsY(hauteurPouces); 
            
            // Dessiner le rectangle du meuble
            g2.setColor(Color.BLUE);
            g2.fillRect(xPixels, yPixels, largeurPixels, hauteurPixels);
            
            String texte = meuble.getType().toString() + " #" + meuble.getId();
            
            // Crée une copie du Graphics2D pour le texte et réinversion
            Graphics2D gTexte = (Graphics2D) g2.create();
            gTexte.scale(1, -1);
            
            // Y doit être négatif ici 
            int texteX = xPixels + 5;
            int texteY = -(yPixels + 15);
            
            gTexte.setColor(Color.WHITE);
            gTexte.drawString(texte, texteX, texteY);
            gTexte.dispose();
        }
    }
    
    
        // Dessine tous les meubles sans drain
    public void afficherMeublesSansDrainIrreg(Graphics2D g2, PieceIrreguliereDTO pieceDTO) {
        for (MeubleSansDrainDTO meuble : pieceDTO.getMeubles()) {
            // Récupérer les valeurs en POUCES
            double xPouces = meuble.getX();
            double yPouces = meuble.getY();
            double largeurPouces = meuble.getLargeur();
            double hauteurPouces = meuble.getHauteur();
            
            // CONVERTIR pouces -> pixels
            int xPixels = poucesVersPixelsX(xPouces); 
            int yPixels = poucesVersPixelsY(yPouces); 
            int largeurPixels = poucesVersPixelsX(largeurPouces); 
            int hauteurPixels = poucesVersPixelsY(hauteurPouces); 
            
            // Dessiner le rectangle du meuble
            g2.setColor(Color.BLUE);
            g2.fillRect(xPixels, yPixels, largeurPixels, hauteurPixels);
            
            String texte = meuble.getType().toString() + " #" + meuble.getId();
            
            // Crée une copie du Graphics2D pour le texte et réinversion
            Graphics2D gTexte = (Graphics2D) g2.create();
            gTexte.scale(1, -1);
            
            // Y doit être négatif ici 
            int texteX = xPixels + 5;
            int texteY = -(yPixels + 15);
            
            gTexte.setColor(Color.WHITE);
            gTexte.drawString(texte, texteX, texteY);
            gTexte.dispose();
        }
    }
    public void afficherMeublesAvecDrain(Graphics2D g2, PieceDTO piece) {
        for (MeubleDrainDTO meuble : piece.getMeublesDrain()) {
            int x = poucesVersPixelsX(meuble.getX());
            int y = poucesVersPixelsY(meuble.getY());
            int largeur = poucesVersPixelsX(meuble.getLargeur());
            int hauteur = poucesVersPixelsY(meuble.getHauteur());

            // Dessiner le meuble
            g2.setColor(new Color(180, 210, 255));  // bleu clair
            g2.fillRect(x, y, largeur, hauteur);
            

            // Dessiner le drain
            int xDrain = poucesVersPixelsX(meuble.getX() + meuble.getXDrainRelatif());
            int yDrain = poucesVersPixelsY(meuble.getY() + meuble.getYDrainRelatif());
            int diametre = poucesVersPixelsX(meuble.getDiametreDrain());

            g2.setColor(Color.BLUE);
            g2.fillOval(xDrain - diametre / 2, yDrain - diametre / 2, diametre, diametre);
            
            // Dessiner le titre du meuble
            String texte = meuble.getType().toString() + " #" + meuble.getId();
            Graphics2D gTexte = (Graphics2D) g2.create();
            gTexte.scale(1, -1);
            int texteX = x + 5;
            int texteY = -(y + 15);  // Affiché au-dessus
            gTexte.setColor(Color.BLACK);
            gTexte.drawString(texte, texteX, texteY);
            gTexte.dispose();
        }
    }
    
     public void afficherMeublesAvecDrainIrreg(Graphics2D g2, PieceIrreguliereDTO piece) {
        for (MeubleDrainDTO meuble : piece.getMeublesDrain()) {
            int x = poucesVersPixelsX(meuble.getX());
            int y = poucesVersPixelsY(meuble.getY());
            int largeur = poucesVersPixelsX(meuble.getLargeur());
            int hauteur = poucesVersPixelsY(meuble.getHauteur());

            // Dessiner le meuble
            g2.setColor(new Color(180, 210, 255));  // bleu clair
            g2.fillRect(x, y, largeur, hauteur);
            

            // Dessiner le drain
            int xDrain = poucesVersPixelsX(meuble.getX() + meuble.getXDrainRelatif());
            int yDrain = poucesVersPixelsY(meuble.getY() + meuble.getYDrainRelatif());
            int diametre = poucesVersPixelsX(meuble.getDiametreDrain());

            g2.setColor(Color.BLUE);
            g2.fillOval(xDrain - diametre / 2, yDrain - diametre / 2, diametre, diametre);
            
            // Dessiner le titre du meuble
            String texte = meuble.getType().toString() + " #" + meuble.getId();
            Graphics2D gTexte = (Graphics2D) g2.create();
            gTexte.scale(1, -1);
            int texteX = x + 5;
            int texteY = -(y + 15);  // Affiché au-dessus
            gTexte.setColor(Color.BLACK);
            gTexte.drawString(texte, texteX, texteY);
            gTexte.dispose();
        }
    }
   
}