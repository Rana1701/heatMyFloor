package com.mycompany.equipe43.Vue;

import com.mycompany.equipe43.Domaine.Controleur;
import com.mycompany.equipe43.Vue.Drawing.Afficheur;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import com.mycompany.equipe43.Domaine.MeubleSansDrain;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class DrawingPanel extends JPanel {

    private Controleur controleur;
    private MainWindow mainWindow;
    
    // Variables pour le redimensionnement
    private boolean isResizing = false;
    private Point startPoint;
    private int startLargeur;
    private int startLongueur;
    
    // Taille de la poignée
    private static final int POIGNEE_SIZE = 10;

    public DrawingPanel(Controleur controleur) {
        this.controleur = controleur;
        setupMouseListeners();

    }
    //Constructeur secondaire pour test
    public DrawingPanel() {
        this(new Controleur()); // crée un contrôleur temporaire par défaut
    }
    
    public void setMainWindow(MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }
    
    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                controleur.selectionnerMeuble(e.getX(), e.getY());
                repaint();
                if (mainWindow != null) {
                    mainWindow.afficherMeubleSelectionne();
                }
                PieceDTO piece = controleur.getPiece();
                if (piece == null) return;
                
                // Vérifie si on clique sur la poignée (coin bas-droite)
                int coinX = piece.getX() + piece.getLargeur();
                int coinY = piece.getY() + piece.getLongueur();
                
                if (Math.abs(e.getX() - coinX) <= POIGNEE_SIZE && 
                    Math.abs(e.getY() - coinY) <= POIGNEE_SIZE) {
                    isResizing = true;
                    startPoint = e.getPoint();
                    startLargeur = piece.getLargeur();
                    startLongueur = piece.getLongueur();
                }
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isResizing) return;
                
                PieceDTO piece = controleur.getPiece();
                if (piece == null) return;
                
                // Calcule la nouvelle taille
                int deltaX = e.getX() - startPoint.x;
                int deltaY = e.getY() - startPoint.y;
                
                int nouvelleLargeur = startLargeur + deltaX;
                int nouvelleLongueur = startLongueur + deltaY;
                
                // Taille minimum
                if (nouvelleLargeur >= 100 && nouvelleLongueur >= 100) {
                    controleur.redimensionnerPiece(nouvelleLargeur, nouvelleLongueur);
                    repaint();
                    if (mainWindow != null) {
                        mainWindow.updateTailleFields();
                    }
                    
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isResizing = false;
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                PieceDTO piece = controleur.getPiece();
                if (piece == null) return;
                
                // Change le curseur sur la poignée
                int coinX = piece.getX() + piece.getLargeur();
                int coinY = piece.getY() + piece.getLongueur();
                
                if (Math.abs(e.getX() - coinX) <= POIGNEE_SIZE && 
                    Math.abs(e.getY() - coinY) <= POIGNEE_SIZE) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };
        
        addMouseListener(mouseAdapter);
        addMouseMotionListener(mouseAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dessiner la grille
        g.setColor(Color.LIGHT_GRAY);
        int cellSize = 10;
        for (int x = 0; x < getWidth(); x += cellSize) {
            g.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += cellSize) {
            g.drawLine(0, y, getWidth(), y);
        }
        Graphics2D g2 = (Graphics2D) g;

        // Appel à l'afficheur
        PieceDTO pieceDTO = controleur.getPiece();
        if (pieceDTO != null) {
            Afficheur.afficherPiece(g2, pieceDTO);
            Afficheur.afficherMeublesSansDrain(g2, pieceDTO);
            MeubleSansDrain meubleSelectionne = controleur.getMeubleSelectionne();
            if (meubleSelectionne != null) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(3));
                g2.drawRect(
                    meubleSelectionne.getPosition().x,
                    meubleSelectionne.getPosition().y,
                    meubleSelectionne.getTaille().width,
                    meubleSelectionne.getTaille().height
                );
            }
        }
    }
}
