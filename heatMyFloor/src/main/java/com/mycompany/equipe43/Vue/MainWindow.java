package com.mycompany.equipe43.Vue;

import com.mycompany.equipe43.Controleur.Controleur;
import com.mycompany.equipe43.Domaine.DTO.MeubleDrainDTO;
import com.mycompany.equipe43.Domaine.DTO.MeubleSansDrainDTO;
import com.mycompany.equipe43.Domaine.TypeMeubleSansDrain;
import com.mycompany.equipe43.Domaine.DTO.PieceDTO;
import com.mycompany.equipe43.Domaine.TypeMeubleDrain;
import com.mycompany.equipe43.Domaine.UniteImperiale;
import com.mycompany.equipe43.Domaine.ElementChauffant;
import com.mycompany.equipe43.Domaine.DTO.PieceIrreguliereDTO;
import com.mycompany.equipe43.Domaine.DTO.ElementChauffantDTO;
import com.mycompany.equipe43.Domaine.DTO.ThermostatDTO;

import java.awt.BorderLayout;

import java.awt.FlowLayout;
import javax.swing.JFrame;
import java.awt.Dimension;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.awt.geom.Point2D;
import java.util.Locale;




public class MainWindow extends javax.swing.JFrame {
    private Controleur controleur;
    private DrawingPanel drawing;
    private javax.swing.JLabel messageLabel;  // Zone de message

    public MainWindow() {
        
        initComponents();
        
            // Quand on clique sur le menu "Quit", on appelle notre code de fermeture
    quitMenu.addMenuListener(new javax.swing.event.MenuListener() {
        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            // on réutilise ton code existant
            quitMenuActionPerformed(null);
        }

        @Override
        public void menuDeselected(javax.swing.event.MenuEvent e) { }

        @Override
        public void menuCanceled(javax.swing.event.MenuEvent e) { }
    });

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1000, 600));
        
        

        controleur = new Controleur();
        drawing = new DrawingPanel(controleur);
        //drawing.setBackground(new java.awt.Color(51, 51, 51));
        drawing.setBackground(new java.awt.Color(250, 250, 250));
        
        DrawingPanel.setLayout(new BorderLayout());
        DrawingPanel.add(drawing, BorderLayout.CENTER);
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        
        drawing.setMainWindow(this);
        
        //créer la zone de messages
        messageLabel = new javax.swing.JLabel(" ");
        messageLabel.setOpaque(true);
        messageLabel.setBackground(new java.awt.Color(240, 240, 240));
        messageLabel.setForeground(new java.awt.Color(0, 0, 0));
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        messageLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messageLabel.setFont(new java.awt.Font("Liberation Sans", java.awt.Font.BOLD, 12));
        
        //ajouter au bas de la fenetre
        getContentPane().add(messageLabel, java.awt.BorderLayout.SOUTH );
        
        // Initialiser les champs avec les dimensions de la pièce par défaut (10' x 10' = 120" x 120")
        PieceDTO pieceInitiale = controleur.getPiece();
        if (pieceInitiale != null) {
            // Remplir les champs de création de pièce formaté en imperial
            largeur1.setText(UniteImperiale.formaterPouces(pieceInitiale.getLargeur()));
            longueur1.setText(UniteImperiale.formaterPouces(pieceInitiale.getLongueur()));
            
        }
        //Initialiser en mode édition
        ModeApp.setSelectedItem("Edition");
        controleur.setMode(Controleur.Mode.EDITION);

    }
    // Afficher un message d'erreur (rouge)
public void afficherErreur(String message) {
    messageLabel.setText("❌ " + message);
    messageLabel.setForeground(new java.awt.Color(200, 0, 0));
    messageLabel.setBackground(new java.awt.Color(255, 230, 230));
}

// Afficher un avertissement (orange)
public void afficherAvertissement(String message) {
    messageLabel.setText("⚠ " + message);
    messageLabel.setForeground(new java.awt.Color(150, 100, 0));
    messageLabel.setBackground(new java.awt.Color(255, 245, 220));
}

// Afficher une info (bleu)
public void afficherInfo(String message) {
    messageLabel.setText("ℹ " + message);
    messageLabel.setForeground(new java.awt.Color(0, 100, 200));
    messageLabel.setBackground(new java.awt.Color(230, 240, 255));
}

// Afficher un succès (vert)
public void afficherSucces(String message) {
    messageLabel.setText("✓ " + message);
    messageLabel.setForeground(new java.awt.Color(0, 150, 0));
    messageLabel.setBackground(new java.awt.Color(230, 255, 230));
}

// Effacer le message
private void effacerMessage() {
    messageLabel.setText(" ");
    messageLabel.setBackground(new java.awt.Color(240, 240, 240));
}

    // Afficher les coordonnées de la souris
    public void afficherCoordonnees(double xPouces, double yPouces) {  
        // Formater en impérial
        String xFormate = UniteImperiale.formaterPouces(xPouces);
        String yFormate = UniteImperiale.formaterPouces(yPouces);
        coordonnéesLabel.setText("Coordonnées (" + xFormate + ", " + yFormate + ")");
    }

    public void afficherCoordonneesPixels(int xPixels, int yPixels) {
    coordonnéesLabel.setText("Coordonnées (" + xPixels + " px, " + yPixels + " px)");
}

    // Méthode publique pour mettre à jour les champs longueur et largeur de la pièce
    public void updateTailleFields() {
        PieceDTO piece = controleur.getPiece();
        
        if (piece != null) {
            //Formater en impérial
            largeur1.setText(UniteImperiale.formaterPouces(piece.getLargeur()));
            longueur1.setText(UniteImperiale.formaterPouces(piece.getLongueur()));
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();  // Rafraîchit toute la fenêtre
    }

    // Méthode publique pour mettre à jour les champs AxeX et AxeY de meuble
    public void updatePositionFields() {
        MeubleSansDrainDTO meuble = controleur.getMeubleSelectionne();
        if (meuble != null) {
            //Formater en imperial
            AxeX.setText(UniteImperiale.formaterPouces(meuble.getX()));
        AxeY.setText(UniteImperiale.formaterPouces(meuble.getY()));
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();
    }

    public void afficherMeubleSelectionne() {
        largeurLabel2.setText("Largeur");
        longueurLabel4.setText("Longueur");
        largeur2.setVisible(true);
        longueur2.setVisible(true);
        largeurLabel2.setVisible(true);
        longueurLabel4.setVisible(true);
        MeubleSansDrainDTO meuble = controleur.getMeubleSelectionne();
        if (meuble != null) {
            //Formater en impérial
            largeur2.setText(UniteImperiale.formaterPouces(meuble.getHauteur()));
            longueur2.setText(UniteImperiale.formaterPouces(meuble.getLargeur()));
            AxeX.setText(UniteImperiale.formaterPouces(meuble.getX()));
            AxeY.setText(UniteImperiale.formaterPouces(meuble.getY()));
            nomMeuble.setText(meuble.getType() + " " + meuble.getId());
            
            String info = String.format("Meuble sélectionné: %s (Longueur: %s, Largeur: %s)",
            meuble.getType() + " " + meuble.getId(),
            UniteImperiale.formaterPouces(meuble.getLargeur()),
            UniteImperiale.formaterPouces(meuble.getHauteur())
        );
        System.out.println(info);
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();  
    }

    public void afficherElementChauffantSelectionne() {
        ElementChauffant element = controleur.getElementSelectionne();
        if (element != null) {
            // Formater en impérial
            largeurElementChauffant.setText(UniteImperiale.formaterPouces(element.getLargeur()));
            hauteurElementChauffant.setText(UniteImperiale.formaterPouces(element.getHauteur()));
            axeXElementChauffant.setText(UniteImperiale.formaterPouces(element.getX()));
            axeYElementChauffant.setText(UniteImperiale.formaterPouces(element.getY()));
            jTextField3.setText("Element Chauffant");
            
            String murActuel = determinerMurElement(element);
            comboBoxMurElementChauffant.setSelectedItem(murActuel);
            
            String info = String.format("Element chauffant sélectionné (Largeur: %s)",
                UniteImperiale.formaterPouces(element.getLargeur())
            );
            System.out.println(info);
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();
    }

    private String determinerMurElement(ElementChauffant element) {
        PieceDTO piece = controleur.getPiece();
        double tolerance = 5.0;
        
        if (element.isHorizontal()) {
            // Élément horizontal : vérifier Nord ou Sud
            double yNord = piece.getY() + piece.getLongueur() - 1;
            double ySud = piece.getY() + 1;
            
            if (Math.abs(element.getY() - yNord) <= tolerance) {
                return "Nord";
            } else if (Math.abs(element.getY() - ySud) <= tolerance) {
                return "Sud";
            }
        } else {
            // Élément vertical : vérifier Est ou Ouest
            double xOuest = piece.getX() + 1;
            double xEst = piece.getX() + piece.getLargeur() - 1;
            
            if (Math.abs(element.getX() - xOuest) <= tolerance) {
                return "Ouest";
            } else if (Math.abs(element.getX() - xEst) <= tolerance) {
                return "Est";
            }
        }
        return "Nord"; // Par défaut
    }
    
    public void afficherMeubleAvecDrainSelectionne() {
        MeubleDrainDTO meuble = controleur.getMeubleAvecDrainSelectionne();
        if (meuble != null) {
            if (controleur.isDrainSelectionne()) {
                nomMeuble.setText(meuble.getType() + " #" + meuble.getId() + " (Drain)");
                largeurLabel2.setText("Diamètre");
                longueurLabel4.setText("");
                largeur2.setText(UniteImperiale.formaterPouces(meuble.getDiametreDrain()));
                longueur2.setText("");
                //AxeX.setText(UniteImperiale.formaterPouces(meuble.getX() + meuble.getXDrainRelatif()));
                //AxeY.setText(UniteImperiale.formaterPouces(meuble.getY() + meuble.getYDrainRelatif()));
                  AxeX.setText(UniteImperiale.formaterPouces(meuble.getXDrainRelatif()));
                  AxeY.setText(UniteImperiale.formaterPouces(meuble.getYDrainRelatif()));

                
                String info = String.format("Drain sélectionné: %s #%d (Diamètre: %s)",
                    meuble.getType(),
                    meuble.getId(),
                    UniteImperiale.formaterPouces(meuble.getDiametreDrain())
                );
                System.out.println(info);
            } else {
                nomMeuble.setText(meuble.getType() + " #" + meuble.getId());
                largeurLabel2.setText("Largeur");
                longueurLabel4.setText("Longueur");
                largeur2.setText(UniteImperiale.formaterPouces(meuble.getHauteur()));
                longueur2.setText(UniteImperiale.formaterPouces(meuble.getLargeur()));
                AxeX.setText(UniteImperiale.formaterPouces(meuble.getX()));
                AxeY.setText(UniteImperiale.formaterPouces(meuble.getY()));
                
                String info = String.format("Meuble avec drain sélectionné: %s #%d (Longueur: %s, Largeur: %s)",
                    meuble.getType(),
                    meuble.getId(),
                    UniteImperiale.formaterPouces(meuble.getLargeur()),
                    UniteImperiale.formaterPouces(meuble.getHauteur())
                );
                System.out.println(info);
            }
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();
    }
    
    public void updatePositionElementChauffant() {
        ElementChauffant element = controleur.getElementSelectionne();
        if (element != null) {
            // Formater en impérial
            axeXElementChauffant.setText(UniteImperiale.formaterPouces(element.getX()));
            axeYElementChauffant.setText(UniteImperiale.formaterPouces(element.getY()));
        }
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();
    }
    
    public void reinitialiserPanneauEdition() {
        // Réinitialiser les labels à leur état par défaut
        largeurLabel2.setText("Largeur");
        longueurLabel4.setText("Longueur");
        
        // Valeur impériale "0 pouce"
        String zeroImperial = UniteImperiale.formaterPouces(0.0);
        
        // Réinitialiser tous les champs
        nomMeuble.setText("");
        largeur2.setText(zeroImperial);
        longueur2.setText(zeroImperial);
        AxeX.setText(zeroImperial);
        AxeY.setText(zeroImperial);
        
        DrawingPanel.revalidate();
        DrawingPanel.repaint();
        this.revalidate();
    }
    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        nomMeuble = new javax.swing.JTextField();
        editionTailleElementSelectionne = new javax.swing.JLabel();
        largeurLabel2 = new javax.swing.JLabel();
        longueur2 = new javax.swing.JTextField();
        jButton4 = new javax.swing.JButton();
        buttonSupprimerElement = new javax.swing.JButton();
        jButtonDeplacer = new javax.swing.JButton();
        AxeX = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        AxeY = new javax.swing.JTextField();
        ajouterMeubleSansDrain = new javax.swing.JButton();
        ajouterMeubleAvecDrain = new javax.swing.JButton();
        comboBoxSansDrain = new javax.swing.JComboBox<>();
        comboBoxAvecDrain = new javax.swing.JComboBox<>();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jSeparator3 = new javax.swing.JSeparator();
        jSeparator4 = new javax.swing.JSeparator();
        longueurLabel4 = new javax.swing.JLabel();
        largeur2 = new javax.swing.JTextField();
        EditionPanel = new javax.swing.JPanel();
        NomElementSelectionne = new javax.swing.JLabel();
        jButton3 = new javax.swing.JButton();
        largeur1 = new javax.swing.JTextField();
        largeurLabel1 = new javax.swing.JLabel();
        longueur1 = new javax.swing.JTextField();
        longueurLabel1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton5 = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jTextField8 = new javax.swing.JTextField();
        longueurLabel2 = new javax.swing.JLabel();
        largeurLabel4 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jSeparator6 = new javax.swing.JSeparator();
        jButton11 = new javax.swing.JButton();
        jLabel20 = new javax.swing.JLabel();
        jSeparator12 = new javax.swing.JSeparator();
        boutonValiderGrille = new javax.swing.JButton();
        inputTranslationX = new javax.swing.JTextField();
        jLabel21 = new javax.swing.JLabel();
        boutonGenererFil = new javax.swing.JButton();
        LongueurFil = new javax.swing.JTextField();
        jLabel22 = new javax.swing.JLabel();
        inputDistanceIntersection1 = new javax.swing.JTextField();
        inputTranslationY = new javax.swing.JTextField();
        jLabel25 = new javax.swing.JLabel();
        boutonValiderTranslation = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        longueurLabel3 = new javax.swing.JLabel();
        largeurLabel3 = new javax.swing.JLabel();
        editionTailleElementSelectionne1 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jSeparator5 = new javax.swing.JSeparator();
        largeurElementChauffant = new javax.swing.JTextField();
        hauteurElementChauffant = new javax.swing.JTextField();
        buttonModifierElementChauffant = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        axeXElementChauffant = new javax.swing.JTextField();
        axeYElementChauffant = new javax.swing.JTextField();
        buttonDeplacerElementChauffant = new javax.swing.JButton();
        buttonAjouterElementChauffant = new javax.swing.JButton();
        buttonSupprimerElementChauffant = new javax.swing.JButton();
        comboBoxMurElementChauffant = new javax.swing.JComboBox<>();
        jLabel19 = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        largeurZone = new javax.swing.JTextField();
        longueurZone = new javax.swing.JTextField();
        largeurLabel9 = new javax.swing.JLabel();
        longueurLabel9 = new javax.swing.JLabel();
        modifierZone = new javax.swing.JButton();
        deplacerZone = new javax.swing.JButton();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        AxeXZone = new javax.swing.JTextField();
        AxeYZone = new javax.swing.JTextField();
        editionTailleElementSelectionne4 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        SuprrimerZone = new javax.swing.JButton();
        AjouterZone = new javax.swing.JButton();
        ModePanel = new javax.swing.JPanel(new FlowLayout(FlowLayout.LEFT));
        ModeActive = new javax.swing.JLabel();
        ModeApp = new javax.swing.JComboBox<>();
        coordonnéesLabel = new javax.swing.JLabel();
        DrawingPanel = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        nomMeuble1 = new javax.swing.JTextField();
        editionTailleElementSelectionne2 = new javax.swing.JLabel();
        largeurLabel5 = new javax.swing.JLabel();
        longueur3 = new javax.swing.JTextField();
        jComboBox6 = new javax.swing.JComboBox<>();
        jButton6 = new javax.swing.JButton();
        buttonSupprimerElement1 = new javax.swing.JButton();
        jButtonDeplacer1 = new javax.swing.JButton();
        AxeX1 = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        AxeY1 = new javax.swing.JTextField();
        ajouterMeubleSansDrain1 = new javax.swing.JButton();
        ajouterMeubleAvecDrain1 = new javax.swing.JButton();
        comboBoxSansDrain1 = new javax.swing.JComboBox<>();
        comboBoxAvecDrain1 = new javax.swing.JComboBox<>();
        jSeparator7 = new javax.swing.JSeparator();
        jLabel12 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        longueurLabel5 = new javax.swing.JLabel();
        largeur3 = new javax.swing.JTextField();
        EditionPanel1 = new javax.swing.JPanel();
        NomElementSelectionne1 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        largeur4 = new javax.swing.JTextField();
        jComboBox7 = new javax.swing.JComboBox<>();
        largeurLabel6 = new javax.swing.JLabel();
        longueur4 = new javax.swing.JTextField();
        longueurLabel6 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        jComboBox8 = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jLabel16 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        longueurLabel7 = new javax.swing.JLabel();
        largeurLabel7 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton9 = new javax.swing.JButton();
        jComboBox9 = new javax.swing.JComboBox<>();
        jTextField13 = new javax.swing.JTextField();
        jSeparator10 = new javax.swing.JSeparator();
        jPanel4 = new javax.swing.JPanel();
        longueurLabel8 = new javax.swing.JLabel();
        largeurLabel8 = new javax.swing.JLabel();
        editionTailleElementSelectionne3 = new javax.swing.JLabel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jSeparator11 = new javax.swing.JSeparator();
        jTextField14 = new javax.swing.JTextField();
        jTextField15 = new javax.swing.JTextField();
        jButton10 = new javax.swing.JButton();
        jTextField16 = new javax.swing.JTextField();
        jTextField17 = new javax.swing.JTextField();
        jTextField18 = new javax.swing.JTextField();
        jTextField19 = new javax.swing.JTextField();
        jTextField20 = new javax.swing.JTextField();
        jComboBox10 = new javax.swing.JComboBox<>();
        jMenuBar1 = new javax.swing.JMenuBar();
        SaveProjectMenu = new javax.swing.JMenu();
        saveProject = new javax.swing.JMenuItem();
        LoadProjectMenu = new javax.swing.JMenuItem();
        exportPNG = new javax.swing.JMenuItem();
        nouvellePiece = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        zoomIn = new javax.swing.JMenuItem();
        zoomOut = new javax.swing.JMenuItem();
        ZoomReset = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        undoMenu = new javax.swing.JMenuItem();
        redoMenu = new javax.swing.JMenuItem();
        quitMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(102, 102, 102));

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        nomMeuble.setEditable(false);
        nomMeuble.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        nomMeuble.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nomMeubleActionPerformed(evt);
            }
        });
        jPanel1.add(nomMeuble, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 150, -1));

        editionTailleElementSelectionne.setBackground(new java.awt.Color(0, 0, 0));
        editionTailleElementSelectionne.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        editionTailleElementSelectionne.setText("Element séléctionné :");
        jPanel1.add(editionTailleElementSelectionne, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 25, -1, -1));

        largeurLabel2.setText("Largeur");
        jPanel1.add(largeurLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, -1, -1));

        longueur2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longueur2ActionPerformed(evt);
            }
        });
        jPanel1.add(longueur2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 90, 80, -1));

        jButton4.setBackground(new java.awt.Color(0, 51, 255));
        jButton4.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("Modifier");
        jButton4.setAlignmentX(0.5F);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 100, -1));

        buttonSupprimerElement.setBackground(new java.awt.Color(255, 0, 0));
        buttonSupprimerElement.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonSupprimerElement.setForeground(new java.awt.Color(255, 255, 255));
        buttonSupprimerElement.setText("Supprimer");
        buttonSupprimerElement.setMaximumSize(new java.awt.Dimension(139, 10));
        buttonSupprimerElement.setMinimumSize(new java.awt.Dimension(139, 14));
        buttonSupprimerElement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSupprimerElementActionPerformed(evt);
            }
        });
        jPanel1.add(buttonSupprimerElement, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 194, 240, -1));

        jButtonDeplacer.setBackground(new java.awt.Color(0, 51, 255));
        jButtonDeplacer.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButtonDeplacer.setForeground(new java.awt.Color(255, 255, 255));
        jButtonDeplacer.setText("Déplacer");
        jButtonDeplacer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeplacerActionPerformed(evt);
            }
        });
        jPanel1.add(jButtonDeplacer, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, 100, -1));

        AxeX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxeXActionPerformed(evt);
            }
        });
        jPanel1.add(AxeX, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, 60, -1));

        jLabel2.setText("Axe X");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 129, -1, -1));

        jLabel3.setText("Axe Y");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 157, -1, -1));

        AxeY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxeYActionPerformed(evt);
            }
        });
        jPanel1.add(AxeY, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 60, -1));

        ajouterMeubleSansDrain.setBackground(new java.awt.Color(0, 51, 255));
        ajouterMeubleSansDrain.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        ajouterMeubleSansDrain.setForeground(new java.awt.Color(255, 255, 255));
        ajouterMeubleSansDrain.setText("Ajouter");
        ajouterMeubleSansDrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ajouterMeubleSansDrainActionPerformed(evt);
            }
        });
        jPanel1.add(ajouterMeubleSansDrain, new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 303, 100, -1));

        ajouterMeubleAvecDrain.setBackground(new java.awt.Color(0, 51, 255));
        ajouterMeubleAvecDrain.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        ajouterMeubleAvecDrain.setForeground(new java.awt.Color(255, 255, 255));
        ajouterMeubleAvecDrain.setText("Ajouter");
        ajouterMeubleAvecDrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ajouterMeubleAvecDrainActionPerformed(evt);
            }
        });
        jPanel1.add(ajouterMeubleAvecDrain, new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 352, 100, -1));

        comboBoxSansDrain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Placard", "Armoire" }));
        jPanel1.add(comboBoxSansDrain, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 303, 142, -1));

        comboBoxAvecDrain.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vanité", "Toilette ", "Bain", "Douche" }));
        comboBoxAvecDrain.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxAvecDrainActionPerformed(evt);
            }
        });
        jPanel1.add(comboBoxAvecDrain, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 352, 142, -1));

        jSeparator2.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator2, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 239, 234, -1));

        jLabel5.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel5.setText("Ajouter un meuble");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 254, -1, -1));

        jLabel6.setFont(new java.awt.Font("Liberation Sans", 2, 12)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(102, 102, 102));
        jLabel6.setText("Sans drain:");
        jPanel1.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 283, -1, -1));

        jLabel7.setFont(new java.awt.Font("Liberation Sans", 2, 12)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(102, 102, 102));
        jLabel7.setText("Avec drain:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 332, -1, -1));
        jPanel1.add(jSeparator3, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 380, -1, -1));

        jSeparator4.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator4.setForeground(new java.awt.Color(0, 0, 0));
        jPanel1.add(jSeparator4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 393, 230, -1));

        longueurLabel4.setText("Longueur");
        jPanel1.add(longueurLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 58, -1));

        largeur2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeur2ActionPerformed(evt);
            }
        });
        jPanel1.add(largeur2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 60, 80, -1));

        jTabbedPane1.addTab("Meuble", jPanel1);

        EditionPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        NomElementSelectionne.setForeground(new java.awt.Color(255, 255, 255));

        jButton3.setBackground(new java.awt.Color(0, 51, 255));
        jButton3.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("Modifier");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        largeur1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeur1ActionPerformed(evt);
            }
        });

        largeurLabel1.setText("Longueur");

        longueur1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longueur1ActionPerformed(evt);
            }
        });

        longueurLabel1.setText("Largeur");

        jLabel1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jLabel1.setText("Type de Pièce");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "régulière", "irrégulière" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton5.setBackground(new java.awt.Color(0, 153, 51));
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("Créer la pièce");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jLabel4.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel4.setText("Thermostat");

        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });

        longueurLabel2.setText("Axe Y");

        largeurLabel4.setText("Axe X");

        jTextField9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField9ActionPerformed(evt);
            }
        });

        jSeparator6.setForeground(new java.awt.Color(51, 51, 51));

        jButton11.setBackground(new java.awt.Color(0, 51, 255));
        jButton11.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton11.setForeground(new java.awt.Color(255, 255, 255));
        jButton11.setText("Modifier");
        jButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton11ActionPerformed(evt);
            }
        });

        jLabel20.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel20.setText("Grille");

        jSeparator12.setForeground(new java.awt.Color(0, 0, 0));

        boutonValiderGrille.setBackground(new java.awt.Color(0, 153, 51));
        boutonValiderGrille.setForeground(new java.awt.Color(255, 255, 255));
        boutonValiderGrille.setText("Valider Intersection");
        boutonValiderGrille.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boutonValiderGrilleActionPerformed(evt);
            }
        });

        inputTranslationX.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTranslationXActionPerformed(evt);
            }
        });

        jLabel21.setText("Longueur du fil");

        boutonGenererFil.setText("Generer fil");
        boutonGenererFil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boutonGenererFilActionPerformed(evt);
            }
        });

        LongueurFil.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LongueurFilChoisi(evt);
            }
        });

        jLabel22.setText("Distance intersection ");

        inputDistanceIntersection1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputDistanceIntersection1ActionPerformed(evt);
            }
        });

        inputTranslationY.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputTranslationYActionPerformed(evt);
            }
        });

        jLabel25.setText("Translation X Y");

        boutonValiderTranslation.setBackground(new java.awt.Color(0, 153, 51));
        boutonValiderTranslation.setForeground(new java.awt.Color(255, 255, 255));
        boutonValiderTranslation.setText("Valider translation");
        boutonValiderTranslation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boutonValiderTranslationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout EditionPanelLayout = new javax.swing.GroupLayout(EditionPanel);
        EditionPanel.setLayout(EditionPanelLayout);
        EditionPanelLayout.setHorizontalGroup(
            EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditionPanelLayout.createSequentialGroup()
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditionPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditionPanelLayout.createSequentialGroup()
                                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(EditionPanelLayout.createSequentialGroup()
                                        .addComponent(longueurLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(25, 25, 25))
                                    .addComponent(largeurLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(167, 167, 167))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditionPanelLayout.createSequentialGroup()
                                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator6, javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(EditionPanelLayout.createSequentialGroup()
                                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(EditionPanelLayout.createSequentialGroup()
                                                .addComponent(largeurLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                            .addGroup(EditionPanelLayout.createSequentialGroup()
                                                .addComponent(longueurLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(4, 4, 4)))
                                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(largeur1, javax.swing.GroupLayout.DEFAULT_SIZE, 112, Short.MAX_VALUE)
                                            .addComponent(longueur1))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton3)))
                                .addGap(42, 42, 42))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditionPanelLayout.createSequentialGroup()
                                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jSeparator12)
                                    .addGroup(EditionPanelLayout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jLabel4)
                                        .addGap(46, 46, 46)
                                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(EditionPanelLayout.createSequentialGroup()
                                                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton11))
                                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGap(3, 3, 3)))
                                .addGap(38, 38, 38))
                            .addGroup(EditionPanelLayout.createSequentialGroup()
                                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addComponent(jLabel21, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel22, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(jLabel25, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(EditionPanelLayout.createSequentialGroup()
                                        .addComponent(inputTranslationX, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(inputTranslationY, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                                    .addComponent(LongueurFil, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(inputDistanceIntersection1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(NomElementSelectionne)))))
                .addContainerGap())
            .addGroup(EditionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(boutonGenererFil, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(EditionPanelLayout.createSequentialGroup()
                                .addComponent(boutonValiderTranslation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(boutonValiderGrille, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(38, 38, 38))
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        EditionPanelLayout.setVerticalGroup(
            EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EditionPanelLayout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addComponent(largeur1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(longueur1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton3)))
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addComponent(largeurLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(longueurLabel1)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton5)
                .addGap(18, 18, 18)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(9, 9, 9)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(largeurLabel4)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(longueurLabel2)
                    .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton11))
                .addGap(35, 35, 35)
                .addComponent(jSeparator12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(LongueurFil, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel21))
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EditionPanelLayout.createSequentialGroup()
                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel22)
                            .addComponent(inputDistanceIntersection1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(inputTranslationX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(inputTranslationY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 15, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EditionPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(NomElementSelectionne)
                        .addGap(35, 35, 35)))
                .addGroup(EditionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(boutonValiderGrille)
                    .addComponent(boutonValiderTranslation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(boutonGenererFil)
                .addGap(27, 27, 27))
        );

        jTabbedPane1.addTab("Pièce", EditionPanel);

        jPanel2.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        longueurLabel3.setText("Hauteur");
        jPanel2.add(longueurLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 58, -1));

        largeurLabel3.setText("Largeur");
        jPanel2.add(largeurLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 60, -1));

        editionTailleElementSelectionne1.setBackground(new java.awt.Color(0, 0, 0));
        editionTailleElementSelectionne1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        editionTailleElementSelectionne1.setText("Element séléctionné :");
        jPanel2.add(editionTailleElementSelectionne1, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 25, -1, -1));

        jLabel9.setText("Axe X");
        jPanel2.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 129, -1, -1));

        jLabel10.setText("Axe Y");
        jPanel2.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 157, -1, -1));

        jSeparator5.setForeground(new java.awt.Color(0, 0, 0));
        jPanel2.add(jSeparator5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, 234, -1));

        largeurElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeurElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(largeurElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 60, -1));

        hauteurElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hauteurElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(hauteurElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 60, -1));

        buttonModifierElementChauffant.setBackground(new java.awt.Color(0, 51, 255));
        buttonModifierElementChauffant.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonModifierElementChauffant.setForeground(new java.awt.Color(255, 255, 255));
        buttonModifierElementChauffant.setText("Modifier");
        buttonModifierElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonModifierElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(buttonModifierElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 90, -1));

        jTextField3.setEditable(false);
        jPanel2.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 20, 160, -1));
        jPanel2.add(axeXElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, 60, -1));

        axeYElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                axeYElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(axeYElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 60, -1));

        buttonDeplacerElementChauffant.setBackground(new java.awt.Color(0, 51, 255));
        buttonDeplacerElementChauffant.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonDeplacerElementChauffant.setForeground(new java.awt.Color(255, 255, 255));
        buttonDeplacerElementChauffant.setText("Déplacer");
        buttonDeplacerElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonDeplacerElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(buttonDeplacerElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 190, 90, -1));

        buttonAjouterElementChauffant.setBackground(new java.awt.Color(0, 51, 255));
        buttonAjouterElementChauffant.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonAjouterElementChauffant.setForeground(new java.awt.Color(255, 255, 255));
        buttonAjouterElementChauffant.setText("Ajouter element chauffant");
        buttonAjouterElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonAjouterElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(buttonAjouterElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 240, -1));

        buttonSupprimerElementChauffant.setBackground(new java.awt.Color(255, 0, 0));
        buttonSupprimerElementChauffant.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonSupprimerElementChauffant.setForeground(new java.awt.Color(255, 255, 255));
        buttonSupprimerElementChauffant.setText("Supprimer");
        buttonSupprimerElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSupprimerElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(buttonSupprimerElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 250, 240, -1));

        comboBoxMurElementChauffant.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nord", "Sud", "Est", "Ouest" }));
        comboBoxMurElementChauffant.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboBoxMurElementChauffantActionPerformed(evt);
            }
        });
        jPanel2.add(comboBoxMurElementChauffant, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 190, 90, -1));

        jLabel19.setText("mur : ");
        jPanel2.add(jLabel19, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, -1));

        jTabbedPane1.addTab("Element chauffant", jPanel2);

        largeurZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeurZoneActionPerformed(evt);
            }
        });

        longueurZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longueurZoneActionPerformed(evt);
            }
        });

        largeurLabel9.setText("Largeur");

        longueurLabel9.setText("Longueur");

        modifierZone.setBackground(new java.awt.Color(0, 51, 255));
        modifierZone.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        modifierZone.setForeground(new java.awt.Color(255, 255, 255));
        modifierZone.setText("Modifier");
        modifierZone.setAlignmentX(0.5F);
        modifierZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modifierZoneActionPerformed(evt);
            }
        });

        deplacerZone.setBackground(new java.awt.Color(0, 51, 255));
        deplacerZone.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        deplacerZone.setForeground(new java.awt.Color(255, 255, 255));
        deplacerZone.setText("Déplacer");
        deplacerZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deplacerZoneActionPerformed(evt);
            }
        });

        jLabel23.setText("Axe X");

        jLabel24.setText("Axe Y");

        AxeXZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxeXZoneActionPerformed(evt);
            }
        });

        AxeYZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxeYZoneActionPerformed(evt);
            }
        });

        editionTailleElementSelectionne4.setBackground(new java.awt.Color(0, 0, 0));
        editionTailleElementSelectionne4.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        editionTailleElementSelectionne4.setText("Zone séléctionnée :");

        jTextField4.setEditable(false);

        SuprrimerZone.setBackground(new java.awt.Color(255, 0, 0));
        SuprrimerZone.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        SuprrimerZone.setForeground(new java.awt.Color(255, 255, 255));
        SuprrimerZone.setText("Supprimer");
        SuprrimerZone.setMaximumSize(new java.awt.Dimension(139, 10));
        SuprrimerZone.setMinimumSize(new java.awt.Dimension(139, 14));
        SuprrimerZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SuprrimerZoneActionPerformed(evt);
            }
        });

        AjouterZone.setBackground(new java.awt.Color(0, 51, 255));
        AjouterZone.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        AjouterZone.setForeground(new java.awt.Color(255, 255, 255));
        AjouterZone.setText("Ajouter");
        AjouterZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AjouterZoneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(editionTailleElementSelectionne4)
                        .addGap(18, 18, 18)
                        .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(27, 27, 27)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel24)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(largeurLabel9)
                                        .addGap(26, 26, 26)
                                        .addComponent(largeurZone, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                                                .addComponent(longueurLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                            .addGroup(jPanel5Layout.createSequentialGroup()
                                                .addComponent(jLabel23)
                                                .addGap(41, 41, 41)))
                                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                            .addComponent(longueurZone, javax.swing.GroupLayout.DEFAULT_SIZE, 80, Short.MAX_VALUE)
                                            .addComponent(AxeXZone)
                                            .addComponent(AxeYZone))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(modifierZone, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(deplacerZone, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(SuprrimerZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(AjouterZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editionTailleElementSelectionne4)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(largeurZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(largeurLabel9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(longueurLabel9)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(longueurZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(modifierZone)))
                .addGap(14, 14, 14)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel23)
                    .addComponent(AxeXZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deplacerZone))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel24)
                    .addComponent(AxeYZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(SuprrimerZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(AjouterZone)
                .addContainerGap(293, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Zones", jPanel5);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.WEST);

        ModePanel.setBackground(new java.awt.Color(102, 102, 102));
        ModePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        ModePanel.setPreferredSize(new java.awt.Dimension(400, 35));

        ModeActive.setForeground(new java.awt.Color(255, 255, 255));
        ModeActive.setText("Mode  activé :");

        ModeApp.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Modélisation", "Edition" }));
        ModeApp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ModeAppActionPerformed(evt);
            }
        });

        coordonnéesLabel.setForeground(new java.awt.Color(255, 255, 255));
        coordonnéesLabel.setText("Coordonnées : ( , )");

        javax.swing.GroupLayout ModePanelLayout = new javax.swing.GroupLayout(ModePanel);
        ModePanel.setLayout(ModePanelLayout);
        ModePanelLayout.setHorizontalGroup(
            ModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ModePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ModeActive)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ModeApp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(261, 261, 261)
                .addComponent(coordonnéesLabel))
        );
        ModePanelLayout.setVerticalGroup(
            ModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ModePanelLayout.createSequentialGroup()
                .addGroup(ModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ModePanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addGroup(ModePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(ModeApp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ModeActive)))
                    .addGroup(ModePanelLayout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(coordonnéesLabel)))
                .addGap(6, 6, 6))
        );

        getContentPane().add(ModePanel, java.awt.BorderLayout.NORTH);

        DrawingPanel.setBackground(new java.awt.Color(51, 51, 51));

        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        nomMeuble1.setHorizontalAlignment(javax.swing.JTextField.LEFT);
        nomMeuble1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nomMeubleActionPerformed(evt);
            }
        });
        jPanel3.add(nomMeuble1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, 90, -1));

        editionTailleElementSelectionne2.setBackground(new java.awt.Color(0, 0, 0));
        editionTailleElementSelectionne2.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        editionTailleElementSelectionne2.setText("Element séléctionné :");
        jPanel3.add(editionTailleElementSelectionne2, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 25, -1, -1));

        largeurLabel5.setText("Longueur");
        jPanel3.add(largeurLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, -1, -1));

        longueur3.setText("500");
        longueur3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longueur2ActionPerformed(evt);
            }
        });
        jPanel3.add(longueur3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 60, -1));

        jComboBox6.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "inch", "px" }));
        jComboBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox5ActionPerformed(evt);
            }
        });
        jPanel3.add(jComboBox6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, 90, -1));

        jButton6.setBackground(new java.awt.Color(0, 51, 255));
        jButton6.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("Modifier");
        jButton6.setAlignmentX(0.5F);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel3.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 90, -1));

        buttonSupprimerElement1.setBackground(new java.awt.Color(255, 0, 0));
        buttonSupprimerElement1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        buttonSupprimerElement1.setForeground(new java.awt.Color(255, 255, 255));
        buttonSupprimerElement1.setText("Supprimer");
        buttonSupprimerElement1.setMaximumSize(new java.awt.Dimension(139, 10));
        buttonSupprimerElement1.setMinimumSize(new java.awt.Dimension(139, 14));
        buttonSupprimerElement1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSupprimerElementActionPerformed(evt);
            }
        });
        jPanel3.add(buttonSupprimerElement1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 194, 240, -1));

        jButtonDeplacer1.setBackground(new java.awt.Color(0, 51, 255));
        jButtonDeplacer1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButtonDeplacer1.setForeground(new java.awt.Color(255, 255, 255));
        jButtonDeplacer1.setText("Déplacer");
        jButtonDeplacer1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDeplacerActionPerformed(evt);
            }
        });
        jPanel3.add(jButtonDeplacer1, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, 90, -1));

        AxeX1.setText("0");
        AxeX1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AxeXActionPerformed(evt);
            }
        });
        jPanel3.add(AxeX1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, 60, -1));

        jLabel8.setText("Axe X");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 129, -1, -1));

        jLabel11.setText("Axe Y");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 157, -1, -1));

        AxeY1.setText("0");
        jPanel3.add(AxeY1, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 60, -1));

        ajouterMeubleSansDrain1.setBackground(new java.awt.Color(0, 51, 255));
        ajouterMeubleSansDrain1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        ajouterMeubleSansDrain1.setForeground(new java.awt.Color(255, 255, 255));
        ajouterMeubleSansDrain1.setText("Ajouter");
        ajouterMeubleSansDrain1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ajouterMeubleSansDrainActionPerformed(evt);
            }
        });
        jPanel3.add(ajouterMeubleSansDrain1, new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 303, 100, -1));

        ajouterMeubleAvecDrain1.setBackground(new java.awt.Color(0, 51, 255));
        ajouterMeubleAvecDrain1.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        ajouterMeubleAvecDrain1.setForeground(new java.awt.Color(255, 255, 255));
        ajouterMeubleAvecDrain1.setText("Ajouter");
        jPanel3.add(ajouterMeubleAvecDrain1, new org.netbeans.lib.awtextra.AbsoluteConstraints(154, 352, 100, -1));

        comboBoxSansDrain1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Placard", "Armoire" }));
        jPanel3.add(comboBoxSansDrain1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 303, 142, -1));

        comboBoxAvecDrain1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Vanité", "Toilette ", "Bain", " " }));
        jPanel3.add(comboBoxAvecDrain1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 352, 142, -1));

        jSeparator7.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.add(jSeparator7, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 239, 234, -1));

        jLabel12.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel12.setText("Ajouter un meuble");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 254, -1, -1));

        jLabel13.setFont(new java.awt.Font("Liberation Sans", 2, 12)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(102, 102, 102));
        jLabel13.setText("Sans drain:");
        jPanel3.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 283, -1, -1));

        jLabel14.setFont(new java.awt.Font("Liberation Sans", 2, 12)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(102, 102, 102));
        jLabel14.setText("Avec drain:");
        jPanel3.add(jLabel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 332, -1, -1));
        jPanel3.add(jSeparator8, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 380, -1, -1));

        jSeparator9.setBackground(new java.awt.Color(0, 0, 0));
        jSeparator9.setForeground(new java.awt.Color(0, 0, 0));
        jPanel3.add(jSeparator9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 393, 230, -1));

        longueurLabel5.setText("Largeur");
        jPanel3.add(longueurLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 58, -1));

        largeur3.setText("500");
        largeur3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeur2ActionPerformed(evt);
            }
        });
        jPanel3.add(largeur3, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 60, -1));

        jTabbedPane2.addTab("Meuble", jPanel3);

        EditionPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        NomElementSelectionne1.setForeground(new java.awt.Color(255, 255, 255));
        EditionPanel1.add(NomElementSelectionne1);

        jButton7.setBackground(new java.awt.Color(0, 51, 255));
        jButton7.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton7.setForeground(new java.awt.Color(255, 255, 255));
        jButton7.setText("Modifier");
        jButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        EditionPanel1.add(jButton7);

        largeur4.setText("500");
        largeur4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeur1ActionPerformed(evt);
            }
        });
        EditionPanel1.add(largeur4);

        jComboBox7.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "inch", "px" }));
        jComboBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox4ActionPerformed(evt);
            }
        });
        EditionPanel1.add(jComboBox7);

        largeurLabel6.setText("Longueur");
        EditionPanel1.add(largeurLabel6);

        longueur4.setText("500");
        longueur4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                longueur1ActionPerformed(evt);
            }
        });
        EditionPanel1.add(longueur4);

        longueurLabel6.setText("Largeur");
        EditionPanel1.add(longueurLabel6);

        jLabel15.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jLabel15.setText("Type de Pièce");
        EditionPanel1.add(jLabel15);

        jComboBox8.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "régulière", "irrégulière" }));
        EditionPanel1.add(jComboBox8);

        jButton8.setBackground(new java.awt.Color(0, 153, 51));
        jButton8.setForeground(new java.awt.Color(255, 255, 255));
        jButton8.setText("Créer la pièce");
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        EditionPanel1.add(jButton8);

        jLabel16.setFont(new java.awt.Font("Liberation Sans", 1, 14)); // NOI18N
        jLabel16.setText("Thermostat");
        EditionPanel1.add(jLabel16);

        jTextField11.setText("0");
        jTextField11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });
        EditionPanel1.add(jTextField11);

        longueurLabel7.setText("Axe Y");
        EditionPanel1.add(longueurLabel7);

        largeurLabel7.setText("Axe X");
        EditionPanel1.add(largeurLabel7);

        jTextField12.setText("0");
        EditionPanel1.add(jTextField12);

        jButton9.setText("Créer thermostat");
        jButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        EditionPanel1.add(jButton9);

        jComboBox9.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "inch", "px" }));
        EditionPanel1.add(jComboBox9);

        jTextField13.setBackground(new java.awt.Color(0, 51, 255));
        jTextField13.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jTextField13.setForeground(new java.awt.Color(255, 255, 255));
        jTextField13.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField13.setText("Modifier");
        EditionPanel1.add(jTextField13);

        jSeparator10.setForeground(new java.awt.Color(51, 51, 51));
        EditionPanel1.add(jSeparator10);

        jTabbedPane2.addTab("Pièce", EditionPanel1);

        jPanel4.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        longueurLabel8.setText("Longueur");
        jPanel4.add(longueurLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 90, 58, -1));

        largeurLabel8.setText("Largeur");
        jPanel4.add(largeurLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 60, 60, -1));

        editionTailleElementSelectionne3.setBackground(new java.awt.Color(0, 0, 0));
        editionTailleElementSelectionne3.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        editionTailleElementSelectionne3.setText("Element séléctionné :");
        jPanel4.add(editionTailleElementSelectionne3, new org.netbeans.lib.awtextra.AbsoluteConstraints(7, 25, -1, -1));

        jLabel17.setText("Axe X");
        jPanel4.add(jLabel17, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 129, -1, -1));

        jLabel18.setText("Axe Y");
        jPanel4.add(jLabel18, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 157, -1, -1));

        jSeparator11.setForeground(new java.awt.Color(0, 0, 0));
        jPanel4.add(jSeparator11, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 239, 234, -1));

        jTextField14.setText("500");
        jTextField14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                largeurElementChauffantActionPerformed(evt);
            }
        });
        jPanel4.add(jTextField14, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 60, 60, -1));

        jTextField15.setText("500");
        jPanel4.add(jTextField15, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 90, 60, -1));

        jButton10.setBackground(new java.awt.Color(0, 51, 255));
        jButton10.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jButton10.setForeground(new java.awt.Color(255, 255, 255));
        jButton10.setText("Modifier");
        jPanel4.add(jButton10, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 90, 90, -1));
        jPanel4.add(jTextField16, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, 90, -1));

        jTextField17.setText("0");
        jPanel4.add(jTextField17, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 130, 60, -1));

        jTextField18.setText("0");
        jPanel4.add(jTextField18, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 160, 60, -1));

        jTextField19.setBackground(new java.awt.Color(0, 51, 255));
        jTextField19.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jTextField19.setForeground(new java.awt.Color(255, 255, 255));
        jTextField19.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField19.setText("Déplacer");
        jPanel4.add(jTextField19, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 130, 90, -1));

        jTextField20.setBackground(new java.awt.Color(255, 0, 0));
        jTextField20.setFont(new java.awt.Font("Liberation Sans", 1, 13)); // NOI18N
        jTextField20.setForeground(new java.awt.Color(255, 255, 255));
        jTextField20.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        jTextField20.setText("Supprimer");
        jPanel4.add(jTextField20, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 200, 240, -1));

        jComboBox10.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "inch", "px" }));
        jPanel4.add(jComboBox10, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 60, 90, -1));

        jTabbedPane2.addTab("Element chauffant", jPanel4);

        javax.swing.GroupLayout DrawingPanelLayout = new javax.swing.GroupLayout(DrawingPanel);
        DrawingPanel.setLayout(DrawingPanelLayout);
        DrawingPanelLayout.setHorizontalGroup(
            DrawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 486, Short.MAX_VALUE)
        );
        DrawingPanelLayout.setVerticalGroup(
            DrawingPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 580, Short.MAX_VALUE)
        );

        getContentPane().add(DrawingPanel, java.awt.BorderLayout.CENTER);

        jMenuBar1.setBackground(new java.awt.Color(0, 0, 0));
        jMenuBar1.setForeground(new java.awt.Color(102, 102, 102));

        SaveProjectMenu.setForeground(new java.awt.Color(255, 255, 255));
        SaveProjectMenu.setText("File");

        saveProject.setText("Save Project");
        saveProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveProjectActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(saveProject);

        LoadProjectMenu.setText("Load Project");
        LoadProjectMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LoadProjectMenuActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(LoadProjectMenu);

        exportPNG.setText("Export PNG");
        exportPNG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPNGActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(exportPNG);

        nouvellePiece.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        nouvellePiece.setText("Nouvelle pièce");
        nouvellePiece.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nouvellePieceActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(nouvellePiece);
        SaveProjectMenu.add(jSeparator1);

        zoomIn.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ADD, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        zoomIn.setText("Zoom in");
        zoomIn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(zoomIn);

        zoomOut.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SUBTRACT, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        zoomOut.setText("Zoom out");
        zoomOut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(zoomOut);

        ZoomReset.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_NUMPAD0, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        ZoomReset.setText("Zoom Reset");
        ZoomReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ZoomResetActionPerformed(evt);
            }
        });
        SaveProjectMenu.add(ZoomReset);

        jMenuBar1.add(SaveProjectMenu);

        editMenu.setForeground(new java.awt.Color(255, 255, 255));
        editMenu.setText("Edit");

        undoMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        undoMenu.setText("undo");
        undoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoMenuActionPerformed(evt);
            }
        });
        editMenu.add(undoMenu);

        redoMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        redoMenu.setText("redo");
        redoMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                redoMenuActionPerformed(evt);
            }
        });
        editMenu.add(redoMenu);

        jMenuBar1.add(editMenu);

        quitMenu.setForeground(new java.awt.Color(242, 242, 242));
        quitMenu.setText("Quit");
        quitMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitMenuActionPerformed(evt);
            }
        });
        jMenuBar1.add(quitMenu);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void redoMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_redoMenuActionPerformed
            controleur.redo();
            // Régénérer la grille avec la distance restaurée
            if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
                drawing.genererEtDessinerGraphe();
            }
            DrawingPanel.repaint();
    }//GEN-LAST:event_redoMenuActionPerformed
    
    

    private void saveProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveProjectActionPerformed
    PieceDTO piece = controleur.getPiece();
    PieceIrreguliereDTO pieceIrreguliere = controleur.getPieceIrreguliere();

    if (piece == null && pieceIrreguliere == null) {
        afficherErreur("Il n'y a aucun projet à sauvegarder.");
        return;
    }

    // Choisir un fichier où sauvegarder
    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Sauvegarder le projet");
    chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Projet HeatMyFloor (*.hmf)", "hmf"));

    int resultat = chooser.showSaveDialog(this);
    if (resultat != JFileChooser.APPROVE_OPTION) {
        return; // l'utilisateur a annulé
    }

    java.io.File fichier = chooser.getSelectedFile();
    // Forcer l'extension .hmf
    if (!fichier.getName().toLowerCase().endsWith(".hmf")) {
        fichier = new java.io.File(fichier.getParentFile(), fichier.getName() + ".hmf");
    }

    try (java.io.PrintWriter out = new java.io.PrintWriter(
            new java.io.FileWriter(fichier))) {

        // 🔹 CAS PIÈCE RECTANGULAIRE
        if (piece != null) {
            out.println("TYPE=RECTANGULAIRE");
            out.printf(java.util.Locale.US,
                    "PIECE %.3f %.3f %.3f %.3f%n",
                    piece.getX(),
                    piece.getY(),
                    piece.getLargeur(),
                    piece.getLongueur());

            // 🔸 THERMOSTAT
            ThermostatDTO th = piece.getThermostat();
            if (th != null) {
                out.printf(java.util.Locale.US,
                        "THERMOSTAT %.3f %.3f %.3f %.3f %.3f%n",
                        th.getX(),
                        th.getY(),
                        th.getLargeur(),
                        th.getHauteur(),
                        th.getAngle());
            }

            // Meubles sans drain
            for (MeubleSansDrainDTO m : piece.getMeubles()) {
                out.printf(java.util.Locale.US,
                        "MEUBLE_SANS_DRAIN %d %s %.3f %.3f %.3f %.3f%n",
                        m.getId(),
                        m.getType().name(),
                        m.getX(),
                        m.getY(),
                        m.getLargeur(),
                        m.getHauteur());
            }

            // Meubles avec drain
            for (MeubleDrainDTO m : piece.getMeublesDrain()) {
                out.printf(java.util.Locale.US,
                        "MEUBLE_DRAIN %d %s %.3f %.3f %.3f %.3f %.3f %.3f %.3f%n",
                        m.getId(),
                        m.getType().name(),
                        m.getX(),
                        m.getY(),
                        m.getLargeur(),
                        m.getHauteur(),
                        m.getXDrainRelatif(),
                        m.getYDrainRelatif(),
                        m.getDiametreDrain());
            }

            // Éléments chauffants
            for (ElementChauffantDTO e : piece.getElementsChauffants()) {
                out.printf(java.util.Locale.US,
                        "ELEMENT_CHAUFFANT %.3f %.3f %.3f %.3f %b %.3f%n",
                        e.getX(),
                        e.getY(),
                        e.getLargeur(),
                        e.getHauteur(),
                        e.isHorizontal(),
                        e.getAngle());
            }
        }
        // 🔹 CAS PIÈCE IRRÉGULIÈRE
        else if (pieceIrreguliere != null) {
            out.println("TYPE=IRREGULIERE");

            // Points du contour
            for (java.awt.geom.Point2D.Double p : pieceIrreguliere.getPoints()) {
                out.printf(java.util.Locale.US,
                        "POINT %.3f %.3f%n",
                        p.getX(),
                        p.getY());
            }

            out.printf(java.util.Locale.US,
                    "FERMEE %b%n",
                    pieceIrreguliere.estFermee());
            out.printf(java.util.Locale.US,
                    "BBOX %.3f %.3f %.3f %.3f%n",
                    pieceIrreguliere.getMinX(),
                    pieceIrreguliere.getMinY(),
                    pieceIrreguliere.getMaxX(),
                    pieceIrreguliere.getMaxY());

            // 🔸 THERMOSTAT
            ThermostatDTO th = pieceIrreguliere.getThermostat();
            if (th != null) {
                out.printf(java.util.Locale.US,
                        "THERMOSTAT %.3f %.3f %.3f %.3f %.3f%n",
                        th.getX(),
                        th.getY(),
                        th.getLargeur(),
                        th.getHauteur(),
                        th.getAngle());
            }

            // Meubles sans drain (sauvegarder en coordonnées relatives pour pièce irrégulière)
            double minX = pieceIrreguliere.getMinX();
            double minY = pieceIrreguliere.getMinY();
            for (MeubleSansDrainDTO m : pieceIrreguliere.getMeubles()) {
                // Convertir coordonnées absolues en relatives
                double xRelatif = m.getX() - minX;
                double yRelatif = m.getY() - minY;
                out.printf(java.util.Locale.US,
                        "MEUBLE_SANS_DRAIN %d %s %.3f %.3f %.3f %.3f%n",
                        m.getId(),
                        m.getType().name(),
                        xRelatif,
                        yRelatif,
                        m.getLargeur(),
                        m.getHauteur());
            }

            // Meubles avec drain (sauvegarder en coordonnées relatives pour pièce irrégulière)
            for (MeubleDrainDTO m : pieceIrreguliere.getMeublesDrain()) {
                // Convertir coordonnées absolues en relatives
                double xRelatif = m.getX() - minX;
                double yRelatif = m.getY() - minY;
                out.printf(java.util.Locale.US,
                        "MEUBLE_DRAIN %d %s %.3f %.3f %.3f %.3f %.3f %.3f %.3f%n",
                        m.getId(),
                        m.getType().name(),
                        xRelatif,
                        yRelatif,
                        m.getLargeur(),
                        m.getHauteur(),
                        m.getXDrainRelatif(),
                        m.getYDrainRelatif(),
                        m.getDiametreDrain());
            }

            // Éléments chauffants
            for (ElementChauffantDTO e : pieceIrreguliere.getElementsChauffants()) {
                out.printf(java.util.Locale.US,
                        "ELEMENT_CHAUFFANT %.3f %.3f %.3f %.3f %b %.3f%n",
                        e.getX(),
                        e.getY(),
                        e.getLargeur(),
                        e.getHauteur(),
                        e.isHorizontal(),
                        e.getAngle());
            }
        }

        afficherSucces("Projet sauvegardé dans " + fichier.getName());
    } catch (java.io.IOException ex) {
        ex.printStackTrace();
        afficherErreur("Erreur lors de la sauvegarde : " + ex.getMessage());
    }
    }//GEN-LAST:event_saveProjectActionPerformed

    private void zoomInActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInActionPerformed
            drawing.zoomIn();
            
      
    }//GEN-LAST:event_zoomInActionPerformed

    private void ModeAppActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ModeAppActionPerformed
            String selected = (String) ModeApp.getSelectedItem();
            if("Edition".equals(selected)){
                controleur.setMode(Controleur.Mode.EDITION);
                //Activer les onglets d'édition
                jTabbedPane1.setEnabledAt(0, true);//meuble
                jTabbedPane1.setEnabledAt(1, true);//pièce
                jTabbedPane1.setEnabledAt(2, true);//element chauffant
                jTabbedPane1.setSelectedIndex(1);//selectionner l'onglet pièce
                //reactiver les menus d'edition
                nouvellePiece.setEnabled(true);
                undoMenu.setEnabled(true);
                redoMenu.setEnabled(true);
                drawing.clearGraphe();
            } else if ("Modélisation".equals(selected)){
                controleur.setMode(Controleur.Mode.MODELISATION);
                //Desactiver l'édition des onglets
                jTabbedPane1.setEnabledAt(0, false);
                jTabbedPane1.setEnabledAt(1, false);
                jTabbedPane1.setEnabledAt(2, false);
                //Desactiver l'édition des menus
                nouvellePiece.setEnabled(false);
                undoMenu.setEnabled(false);
                redoMenu.setEnabled(false);
                drawing.genererEtDessinerGraphe();
                
                // Si un fil a déjà été généré précédemment, le régénérer automatiquement avec les nouvelles positions des meubles
                if (drawing.aUnFilGenere()) {
                    double longueurFilMax = controleur.getLongueurFilMax();
                    drawing.genererEtAfficherFilAutomatique(longueurFilMax);
                }
            }
            DrawingPanel.repaint();
    }//GEN-LAST:event_ModeAppActionPerformed

    
    //on crée une nouvelle pièce avec des valeurs par défaut
    private void nouvellePieceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nouvellePieceActionPerformed
        
        // Crée une pièce avec des dimensions par défaut
        controleur.creerPieceReguliere(0.0, 0.0, 120.0, 120.0);
        DrawingPanel.repaint();
    
        //met à jour les champs pour refléter les nouvelles valeurs
        //Afficher en format impérial
        largeur1.setText("10' 0\"");
        longueur1.setText("10' 0\"");

        
    }//GEN-LAST:event_nouvellePieceActionPerformed

    private void undoMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_undoMenuActionPerformed
        controleur.undo();
        // Régénérer la grille avec la distance restaurée
        if (controleur.getModeActuel() == Controleur.Mode.MODELISATION) {
            drawing.genererEtDessinerGraphe();
        }
        DrawingPanel.repaint();
    }//GEN-LAST:event_undoMenuActionPerformed

    private void largeur2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeur2ActionPerformed

    }//GEN-LAST:event_largeur2ActionPerformed

    private void ajouterMeubleSansDrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ajouterMeubleSansDrainActionPerformed
                                                         String valeur = (String) comboBoxSansDrain.getSelectedItem();
    
    // DÉTERMINER LA TAILLE DE LA PIÈCE ACTIVE
    double pieceLargeur = 0.0;
    double pieceLongueur = 0.0;
    
    // Vérifier si c'est une pièce irrégulière
    PieceIrreguliereDTO pieceIrregDTO = controleur.getPieceIrreguliere();
    if (pieceIrregDTO != null) {
        // Pièce irrégulière
        pieceLargeur = pieceIrregDTO.getLargeur();  // Utilise getLargeur() du DTO
        pieceLongueur = pieceIrregDTO.getLongueur();
    } else {
        // Pièce régulière
        PieceDTO pieceDTO = controleur.getPiece();
        if (pieceDTO != null) {
            pieceLargeur = pieceDTO.getLargeur();
            pieceLongueur = pieceDTO.getLongueur();
        } else {
            // Aucune pièce créée
            afficherAvertissement("Veuillez d'abord créer une pièce.");
            return;
        }
    }
    
    double largeur = 24.0;
    double hauteur = 24.0;
    double x = 12.0;
    double y = 12.0;

    // Ajuster si hors de la pièce (même logique)
    if (x + largeur > pieceLargeur) {
        x = Math.max(0, pieceLargeur - largeur);
    }
    if (y + hauteur > pieceLongueur) {
        y = Math.max(0, pieceLongueur - hauteur);
    }
    
    TypeMeubleSansDrain type;
    if ("Placard".equals(valeur)) {
        type = TypeMeubleSansDrain.PLACARD;
    } else {
        type = TypeMeubleSansDrain.ARMOIRE;
    }
    
    // Vérifier qu'on reste dans les limites
    if (x < 0 || y < 0 || x + largeur > pieceLargeur || y + hauteur > pieceLongueur) {
        afficherAvertissement("Impossible d'ajouter le meuble : il ne rentre pas dans la pièce.");
        return;
    }
    
    controleur.ajouterMeubleSansDrain(x, y, largeur, hauteur, type);
    DrawingPanel.repaint();

    }//GEN-LAST:event_ajouterMeubleSansDrainActionPerformed
    
    
    private void AxeXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxeXActionPerformed

    }//GEN-LAST:event_AxeXActionPerformed

    private void jButtonDeplacerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDeplacerActionPerformed
       // effacerMessage();
       // try {
                //Parser le format impérial
              //  double nouvelXPouces = UniteImperiale.parseVersPouces(AxeX.getText().trim());
              //  double nouvelYPouces = UniteImperiale.parseVersPouces(AxeY.getText().trim());

              //  String resultat = controleur.deplacerMeubleSelectionne(nouvelXPouces, nouvelYPouces);

              //  if (resultat.startsWith("ERREUR")) {
                    /*javax.swing.JOptionPane.showMessageDialog(
                        this,
                        resultat.substring(8), // Remove "ERREUR: " prefix
                        "Déplacement",
                        javax.swing.JOptionPane.WARNING_MESSAGE*/
                  //  afficherAvertissement(resultat.substring(8));
                    
                  //  return;
              //  } else if (resultat.startsWith("COLLISION")) {
                    /*javax.swing.JOptionPane.showMessageDialog(
                        this,
                        resultat.substring(11), // Remove "COLLISION: " prefix
                        "Position ajustée",
                        javax.swing.JOptionPane.INFORMATION_MESSAGE
                    );*/
                //    afficherAvertissement(resultat.substring(11));
                //    afficherMeubleSelectionne(); // Update fields with new position
             //   }

            //    DrawingPanel.repaint();

           // } catch (IllegalArgumentException e) {
                /*javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Format invalide!\nFormat: X' Y\" W/Z\nExemples: 4', 3\" 1/2",
                    "Erreur de format",
                    javax.swing.JOptionPane.ERROR_MESSAGE
            //    );*/
           //     afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");
           // }
            effacerMessage();
    try {
        // CAS 1 : c'est le DRAIN qui est sélectionné
        if (controleur.isDrainSelectionne()
                && controleur.getMeubleAvecDrainSelectionne() != null) {

            double xRel = UniteImperiale.parseVersPouces(AxeX.getText().trim());
            double yRel = UniteImperiale.parseVersPouces(AxeY.getText().trim());
            
            // on garde le même diamètre
            double diam = controleur
                    .getMeubleAvecDrainSelectionne()
                    .getDiametreDrain();

            boolean ok = controleur.modifierDrainSelectionne(diam, xRel, yRel);

            if (!ok) {
                afficherAvertissement("Aucun drain sélectionné ou valeurs invalides.");
                return;
            }

            DrawingPanel.repaint();
            afficherMeubleAvecDrainSelectionne();
            return; // ne pas continuer vers le reste
        }

        // CAS 2 : meuble (avec OU sans drain) sélectionné → comme avant
        double nouvelXPouces = UniteImperiale.parseVersPouces(AxeX.getText().trim());
        double nouvelYPouces = UniteImperiale.parseVersPouces(AxeY.getText().trim());

        String resultat = controleur.deplacerMeubleSelectionne(nouvelXPouces, nouvelYPouces);

        if (resultat.startsWith("ERREUR")) {
            afficherAvertissement(resultat.substring(8));
            return;
        } else if (resultat.startsWith("COLLISION")) {
            afficherAvertissement(resultat.substring(11));
        }

        DrawingPanel.repaint();
        afficherMeubleSelectionne();
        afficherMeubleAvecDrainSelectionne();

    } catch (IllegalArgumentException e) {
        afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");}
        
    }//GEN-LAST:event_jButtonDeplacerActionPerformed

    private void buttonSupprimerElementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSupprimerElementActionPerformed
        effacerMessage();
        boolean ok = controleur.supprimerMeubleSelectionne();
        if (!ok) {
            /*javax.swing.JOptionPane.showMessageDialog(
                this,
                "Aucun meuble sélectionné.",
                "Suppression",
                javax.swing.JOptionPane.WARNING_MESSAGE
            );*/
            afficherAvertissement("Aucun meuble sélectionné.");
            
            return;
        }

        // Nettoie l’UI
        nomMeuble.setText("");
        largeur2.setText("");
        longueur2.setText("");
        AxeX.setText("0'");
        AxeY.setText("0'");

        DrawingPanel.repaint();
    }//GEN-LAST:event_buttonSupprimerElementActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        effacerMessage(); 
        try {
        if (controleur.isDrainSelectionne()) {
            /*
            double nouveauDiametre = UniteImperiale.parseVersPouces(largeur2.getText().trim());
            double xAbs = UniteImperiale.parseVersPouces(AxeX.getText().trim());
            double yAbs = UniteImperiale.parseVersPouces(AxeY.getText().trim());

            boolean ok = controleur.modifierDrainSelectionne(nouveauDiametre, xAbs, yAbs); */
            double nouveauDiametre = UniteImperiale.parseVersPouces(largeur2.getText().trim());

// maintenant AxeX / AxeY sont RELATIFS au meuble
            double xRel = UniteImperiale.parseVersPouces(AxeX.getText().trim());
            double yRel = UniteImperiale.parseVersPouces(AxeY.getText().trim());

            boolean ok = controleur.modifierDrainSelectionne(nouveauDiametre, xRel, yRel);

            if (!ok) {
                /*javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Aucun drain sélectionné ou valeurs invalides.",
                    "Modification du drain",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );*/
                afficherAvertissement("Aucun drain sélectionné ou valeurs invalides.");
                return;
            }

            DrawingPanel.repaint();
            afficherMeubleAvecDrainSelectionne();

        } else if (controleur.getMeubleAvecDrainSelectionne() != null) {
            double nouvelleLongueur = UniteImperiale.parseVersPouces(longueur2.getText().trim());
            double nouvelleLargeur = UniteImperiale.parseVersPouces(largeur2.getText().trim());

            boolean ok = controleur.redimensionnerMeubleSelectionne(nouvelleLongueur, nouvelleLargeur);
            if (!ok) {
                /*javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Valeurs invalides pour les dimensions du meuble.",
                    "Redimensionnement",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );*/
                afficherAvertissement("Valeurs invalides pour les dimensions du meuble.");
                return;
            }

            DrawingPanel.repaint();
            afficherMeubleAvecDrainSelectionne();
            
        } else if (controleur.getMeubleSelectionne() != null) {
            double nouvelleLongueur = UniteImperiale.parseVersPouces(longueur2.getText().trim());
            double nouvelleLargeur = UniteImperiale.parseVersPouces(largeur2.getText().trim());

            boolean ok = controleur.redimensionnerMeubleSelectionne(nouvelleLongueur, nouvelleLargeur);
            if (!ok) {
                /*javax.swing.JOptionPane.showMessageDialog(
                    this,
                    "Aucun meuble sélectionné ou valeurs invalides.",
                    "Redimensionnement",
                    javax.swing.JOptionPane.WARNING_MESSAGE
                );*/
                afficherAvertissement("Aucun meuble sélectionné ou valeurs invalides.");
                return;
            }

            DrawingPanel.repaint();
            afficherMeubleSelectionne();
        }else{
            afficherAvertissement("Aucun meuble sélectionné.");
        }

    } catch (IllegalArgumentException e) {
        /*javax.swing.JOptionPane.showMessageDialog(
            this,
            "Format invalide!\nFormat: X' Y\" W/Z\nExemples: 4', 3\" 1/2",
            "Erreur de format",
            javax.swing.JOptionPane.ERROR_MESSAGE
        );*/
        afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");
    }
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jComboBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox5ActionPerformed
        
    }//GEN-LAST:event_jComboBox5ActionPerformed

    private void longueur2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longueur2ActionPerformed
        
    }//GEN-LAST:event_longueur2ActionPerformed

    private void nomMeubleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nomMeubleActionPerformed
        
    }//GEN-LAST:event_nomMeubleActionPerformed

    private void largeurElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeurElementChauffantActionPerformed
        
    }//GEN-LAST:event_largeurElementChauffantActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
double x = 20.0;  // ex : au début du mur
    double y = 20.0;
    double largeur = 10.0;
    double hauteur = 8.0;

    controleur.ajouterThermostat(x, y, largeur, hauteur);
    DrawingPanel.repaint();        
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        effacerMessage();
        
        String typePiece = (String) jComboBox1.getSelectedItem();
        if("irrégulière".equals(typePiece)){
            //mode cration piece irreg
            controleur.demarrerCreationPieceIrreguliere();
            afficherInfo("Cliquez pour placer les points du polygone. Double-clic pour terminer.");
            DrawingPanel.repaint();
            return;
        }
        try {
            // Récupérer les valeurs des champs et parser le format imperial
            double largeur = UniteImperiale.parseVersPouces(largeur1.getText().trim());
            double longueur = UniteImperiale.parseVersPouces(longueur1.getText().trim());
            
            // Validation: dimensions positives
        if (largeur <= 0 || longueur <= 0) {
            /*javax.swing.JOptionPane.showMessageDialog(
                this,
                "Les dimensions doivent être positives!",
                "Erreur de validation",
                javax.swing.JOptionPane.WARNING_MESSAGE
            );*/
            afficherAvertissement("Les dimensions doivent être positives!");
            return;
        }
            // Créer la nouvelle pièce via le contrôleur
            controleur.creerPieceReguliere(0.0, 0.0, largeur, longueur);

            // Redessiner
            DrawingPanel.repaint();

        } catch (IllegalArgumentException e) {
            /*javax.swing.JOptionPane.showMessageDialog(
                this,
                "Format invalide!\nFormat: X' Y\" W/Z\nExemples: 4', 3\" 1/2",
                "Erreur de format",
                javax.swing.JOptionPane.ERROR_MESSAGE
            );*/
            afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");
        }
    }//GEN-LAST:event_jButton5ActionPerformed

    private void longueur1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longueur1ActionPerformed
        
    }//GEN-LAST:event_longueur1ActionPerformed

    private void jComboBox4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox4ActionPerformed
        
    }//GEN-LAST:event_jComboBox4ActionPerformed

    private void largeur1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeur1ActionPerformed
        
    }//GEN-LAST:event_largeur1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        effacerMessage();
        try {
            // Récupérer les valeurs des champs et parser le format imperial
            double largeur = UniteImperiale.parseVersPouces(largeur1.getText().trim());
            double longueur = UniteImperiale.parseVersPouces(longueur1.getText().trim());
            // Validation: dimensions positives
            if (largeur <= 0 || longueur <= 0) {

                afficherAvertissement("Les dimensions doivent être positives!");
                return;
            }
            //Vérifier si c'est une pièce irrégulière
            if (controleur.estPieceIrreguliere()) {
                // REDIMENSIONNEMENT PIÈCE IRRÉGULIÈRE
                if (controleur.redimensionnerPieceIrregAbsolu(largeur, longueur)) {
                    DrawingPanel.repaint();
                    updateTailleFields(); // Mettre à jour l'affichage
                    afficherSucces("Pièce irrégulière redimensionnée avec succès!");
                } else {
                    afficherErreur("Impossible de redimensionner la pièce.");
                }
            } else {
            // Redimensionner la pièce 
            boolean meubleRepositionne = controleur.redimensionnerPiece(largeur, longueur);
            // Redessiner
            DrawingPanel.repaint();
            if (meubleRepositionne){
                afficherAvertissement("Certains meubles ont été replacés automatiquement à l'intérieur de la pièce.");
                //return ;
            }
          }  

        } catch (IllegalArgumentException e) {
           
            afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void zoomOutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutActionPerformed
        // TODO add your handling code here:
        drawing.zoomOut();
        
        
    }//GEN-LAST:event_zoomOutActionPerformed

    private void ZoomResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ZoomResetActionPerformed
        // TODO add your handling code here:
        drawing.resetZoom();
       
    }//GEN-LAST:event_ZoomResetActionPerformed

    private void buttonModifierElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonModifierElementChauffantActionPerformed
        // TODO add your handling code here:
        effacerMessage();
        if (controleur.getElementSelectionne() == null) return ;
        else {
        double largeur , hauteur ;
         largeur = controleur.getElementSelectionne().getLargeur();
         hauteur = controleur.getElementSelectionne().getHauteur();
        try {
     largeur = UniteImperiale.parseVersPouces(largeurElementChauffant.getText().trim());}
        catch (IllegalArgumentException e) { }
        
        try {
     hauteur = UniteImperiale.parseVersPouces(hauteurElementChauffant.getText().trim());
        } catch (IllegalArgumentException e) {}
        
    controleur.modifierElementChauffant(largeur, hauteur);
    DrawingPanel.repaint();
        }
    }//GEN-LAST:event_buttonModifierElementChauffantActionPerformed

    private void buttonDeplacerElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonDeplacerElementChauffantActionPerformed
        effacerMessage();
        if (controleur.getElementSelectionne() == null) {
            afficherAvertissement("Aucun élément chauffant sélectionné.");
            return;
        }
        
        // Vérifier si c'est une pièce irrégulière
        if (controleur.estPieceIrreguliere()) {
            // Pour les pièces irrégulières, activer le mode sélection de mur
            controleur.activerModeDeplacementElementChauffant();
            afficherInfo("Cliquez sur un mur de la pièce pour déplacer l'élément chauffant.");
            DrawingPanel.repaint();
            return;
        }
        
        // Pour les pièces régulières, logique existante
        try {
            // Parser le format impérial
            double nouvelX = UniteImperiale.parseVersPouces(axeXElementChauffant.getText().trim());
            double nouvelY = UniteImperiale.parseVersPouces(axeYElementChauffant.getText().trim());
            
            ElementChauffant element = controleur.getElementSelectionne();
            
            // Appliquer les contraintes de déplacement selon l'orientation
            if (element.isHorizontal()) {
                // Élément horizontal (Nord ou Sud) : ne peut bouger que sur l'axe X
                // On garde le Y actuel et on ne déplace que sur X
                nouvelY = element.getY();
                System.out.println("[v0] Élément horizontal détecté - déplacement uniquement sur X");
            } else {
                // Élément vertical (Est ou Ouest) : ne peut bouger que sur l'axe Y
                // On garde le X actuel et on ne déplace que sur Y
                nouvelX = element.getX();
                System.out.println("[v0] Élément vertical détecté - déplacement uniquement sur Y");
            }
            
            // Appeler le contrôleur pour déplacer l'élément
            String resultat = controleur.deplacerElementChauffant(nouvelX, nouvelY);
            
            if (resultat.startsWith("ERREUR")) {
                afficherErreur(resultat.substring(8));
                return;
            }
            
            // Mettre à jour l'affichage des champs avec la nouvelle position
            updatePositionElementChauffant();
            DrawingPanel.repaint();
            
        } catch (IllegalArgumentException e) {
            afficherErreur("Format invalide! Utilisez: X' Y\" W/Z (ex: 4', 3\" 1/2)");
        }
    }//GEN-LAST:event_buttonDeplacerElementChauffantActionPerformed

    private void buttonAjouterElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonAjouterElementChauffantActionPerformed
  effacerMessage();
        double largeur, hauteur;

    try {
        largeur = UniteImperiale.parseVersPouces(largeurElementChauffant.getText().trim());
    } catch (IllegalArgumentException e) {
        largeur = 30.0;
    }

    try {
        hauteur = UniteImperiale.parseVersPouces(hauteurElementChauffant.getText().trim());
    } catch (IllegalArgumentException e) {
        hauteur = 1.0;
    }

    // Vérifier si c'est une pièce irrégulière
    if (controleur.estPieceIrreguliere()) {
        // Pour les pièces irrégulières, activer le mode sélection de mur
        if (controleur.ajouterElementChauffant(largeur, hauteur, "")) {
            afficherInfo("Cliquez sur un mur de la pièce pour placer l'élément chauffant.");
            DrawingPanel.repaint();
        } else {
            afficherErreur("Impossible d'activer le mode d'ajout d'élément chauffant.");
        }
        return;
    }

    // Pour les pièces régulières, logique existante
    String murChoisi = comboBoxMurElementChauffant.getSelectedItem().toString();

    PieceDTO piece = controleur.getPiece();
    if (piece == null) {
        afficherAvertissement("Veuillez d'abord créer une pièce.");
        return;
    }
    
    if ((murChoisi.equals("Nord") || murChoisi.equals("Sud")) && largeur > piece.getLargeur()) {
        afficherAvertissement("Impossible d'ajouter l'élément chauffant : largeur dépasse la taille de la pièce.");
        return;
    }
    if ((murChoisi.equals("Est") || murChoisi.equals("Ouest")) && largeur > piece.getLongueur()) {
        afficherAvertissement("Impossible d'ajouter l'élément chauffant : hauteur dépasse la taille de la pièce.");
        return;
    }
    
    if (controleur.ajouterElementChauffant(largeur,hauteur, murChoisi)) {
        DrawingPanel.repaint();
    } 
    else {
       // JOptionPane.showMessageDialog(this, "Impossible d'ajouter : mur plein ou collision !");
       afficherErreur("Impossible d'ajouter : mur plein ou collision !");
    }
    
    }//GEN-LAST:event_buttonAjouterElementChauffantActionPerformed

    private void buttonSupprimerElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSupprimerElementChauffantActionPerformed
        // TODO add your handling code here:
    controleur.supprimerElementChauffant();
    DrawingPanel.repaint();
    }//GEN-LAST:event_buttonSupprimerElementChauffantActionPerformed



    private void ajouterMeubleAvecDrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ajouterMeubleAvecDrainActionPerformed
                                                      
    effacerMessage();
    
    // Récupère le type sélectionné dans le JComboBox
    String selected = (String) comboBoxAvecDrain.getSelectedItem();
    TypeMeubleDrain type = null;

    if ("Vanité".equalsIgnoreCase(selected)) {
        type = TypeMeubleDrain.VANITE;
    } 
    else if ("Toilette".equalsIgnoreCase(selected.trim())) {
        type = TypeMeubleDrain.TOILETTE;
    } 
    else if ("Bain".equalsIgnoreCase(selected)) {
        type = TypeMeubleDrain.BAIN;
    }
    else if ("Douche".equalsIgnoreCase(selected)) {
        type = TypeMeubleDrain.DOUCHE;
    }

    if (type == null) {
        afficherAvertissement("Veuillez choisir un type de meuble avec drain.");
        return;
    }

    // Valeurs par défaut
    double x = 30.0;
    double y = 30.0;
    double largeur = 24.0;
    double hauteur = 24.0;
    double xDrainRelatif = largeur / 2.0;
    double yDrainRelatif = hauteur / 2.0;
    double diametreDrain = 5.0;
    
    // DÉTERMINER LES DIMENSIONS DE LA PIÈCE ACTIVE
    double pieceLargeur = 0.0;
    double pieceLongueur = 0.0;
    
    // Vérifier si c'est une pièce irrégulière
    PieceIrreguliereDTO pieceIrregDTO = controleur.getPieceIrreguliere();
    if (pieceIrregDTO != null) {
        // Pièce irrégulière
        pieceLargeur = pieceIrregDTO.getLargeur();
        pieceLongueur = pieceIrregDTO.getLongueur();
        
        // Ajuster la position relative
        double posX = pieceIrregDTO.getMinX() + x;
        double posY = pieceIrregDTO.getMinY() + y;
        
        // Vérifier si dans les limites
        if (posX + largeur > pieceIrregDTO.getMaxX() || 
            posY + hauteur > pieceIrregDTO.getMaxY()) {
            afficherAvertissement("Impossible d'ajouter le meuble : il ne rentre pas dans la pièce.");
            return;
        }
    } else {
        // Pièce régulière
        PieceDTO pieceDTO = controleur.getPiece();
        if (pieceDTO == null) {
            afficherAvertissement("Veuillez d'abord créer une pièce.");
            return;
        }
        
        pieceLargeur = pieceDTO.getLargeur();
        pieceLongueur = pieceDTO.getLongueur();
        
        // Vérifier si dans les limites (votre code original)
        if (x + largeur > pieceLargeur || y + hauteur > pieceLongueur) {
            afficherAvertissement("Impossible d'ajouter le meuble : il ne rentre pas dans la pièce.");
            return;
        }
    }

    // Appel du contrôleur pour ajouter le meuble avec drain
    controleur.ajouterMeubleAvecDrain(
        x, y,
        largeur, hauteur,
        type,
        xDrainRelatif, yDrainRelatif,
        diametreDrain
    );
  
    // Rafraîchir l'affichage
    drawing.repaint();

    }//GEN-LAST:event_ajouterMeubleAvecDrainActionPerformed

    private void comboBoxMurElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxMurElementChauffantActionPerformed
        effacerMessage();
        ElementChauffant element = controleur.getElementSelectionne();
        if (element == null) {
            return; // Pas d'élément sélectionné
        }

        String nouveauMur = (String) comboBoxMurElementChauffant.getSelectedItem();
        String murActuel = determinerMurElement(element);

        // Si c'est le même mur, ne rien faire
        if (nouveauMur.equals(murActuel)) {
            return;
        }

        PieceDTO piece = controleur.getPiece();
        double nouvelX = element.getX();
        double nouvelY = element.getY();
        boolean nouvelleOrientation = element.isHorizontal();

        switch (nouveauMur) {
            case "Nord":
                nouvelY = piece.getY() + piece.getLongueur() - 1;
                nouvelleOrientation = true; // horizontal
                // Garder X mais s'assurer qu'il reste dans les limites
                if (nouvelX < piece.getX()) nouvelX = piece.getX();
                if (nouvelX + element.getLargeur() > piece.getX() + piece.getLargeur()) {
                    nouvelX = piece.getX() + piece.getLargeur() - element.getLargeur();
                }
                break;
            case "Sud":
                nouvelY = piece.getY() + 1;
                nouvelleOrientation = true; // horizontal
                // Garder X mais s'assurer qu'il reste dans les limites
                if (nouvelX < piece.getX()) nouvelX = piece.getX();
                if (nouvelX + element.getLargeur() > piece.getX() + piece.getLargeur()) {
                    nouvelX = piece.getX() + piece.getLargeur() - element.getLargeur();
                }
                break;
            case "Est":
                nouvelX = piece.getX() + piece.getLargeur() - 1;
                nouvelleOrientation = false; // vertical
                // Garder Y mais s'assurer qu'il reste dans les limites
                if (nouvelY < piece.getY()) nouvelY = piece.getY();
                if (nouvelY + element.getLargeur() > piece.getY() + piece.getLongueur()) {
                    nouvelY = piece.getY() + piece.getLongueur() - element.getLargeur();
                }
                break;
            case "Ouest":
                nouvelX = piece.getX() + 1;
                nouvelleOrientation = false; // vertical
                // Garder Y mais s'assurer qu'il reste dans les limites
                if (nouvelY < piece.getY()) nouvelY = piece.getY();
                if (nouvelY + element.getLargeur() > piece.getY() + piece.getLongueur()) {
                    nouvelY = piece.getY() + piece.getLongueur() - element.getLargeur();
                }
                break;
        }

        // Mettre à jour l'orientation
        element.setHorizontal(nouvelleOrientation);

        // Déplacer l'élément
        String resultat = controleur.deplacerElementChauffant(nouvelX, nouvelY);
        
        // Mettre à jour l'affichage
        updatePositionElementChauffant();
        DrawingPanel.repaint();
    }//GEN-LAST:event_comboBoxMurElementChauffantActionPerformed


    private void hauteurElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hauteurElementChauffantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_hauteurElementChauffantActionPerformed

    private void comboBoxAvecDrainActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboBoxAvecDrainActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_comboBoxAvecDrainActionPerformed

    private void AxeYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxeYActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AxeYActionPerformed

    private void axeYElementChauffantActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_axeYElementChauffantActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_axeYElementChauffantActionPerformed

    private void inputTranslationXActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTranslationXActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputTranslationXActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
   try {
        // 1) Lire X et Y dans tes champs texte (EN POUCES)
        // ⬇⬇⬇ ADAPTE ces noms aux tiens (ex: txtPosX, txtPosY, etc.)
        double x = UniteImperiale.parseVersPouces(jTextField8.getText().trim());
        double y = UniteImperiale.parseVersPouces(jTextField9.getText().trim());
        // si tu n'utilises pas UniteImperiale ici, tu peux faire :
        // double x = Double.parseDouble(txtPosX.getText().replace(",", "."));
        // double y = Double.parseDouble(txtPosY.getText().replace(",", "."));

        // 2) Si un meuble (avec ou sans drain) est sélectionné → comportement actuel
        if (controleur.getMeubleSelectionne() != null ||
            controleur.getMeubleAvecDrainSelectionne() != null) {

            String res = controleur.deplacerMeubleSelectionne(x, y);
            
            // ⬇⬇⬇ ADAPTE le nom du panel de dessin (drawing, drawingPanel, panelPlan, ...)
            drawing.repaint();

            if (!"OK".equals(res) && res != null) {
                afficherAvertissement(res);
            } else {
                afficherSucces("Meuble déplacé.");
            }
            return;
        }

       // 3) Aucun meuble sélectionné → on essaie de déplacer le thermostat

// --- CAS PIECE IRREGULIERE ---
if (controleur.estPieceIrreguliere()) {
    PieceIrreguliereDTO pir = controleur.getPieceIrreguliere();
    if (pir != null && pir.getThermostat() != null) {

        // IMPORTANT: ton snap irrégulier attend (x,y) = TOP-LEFT du thermostat
        controleur.deplacerThermostatIrregulierSnap(x, y, true);

        drawing.repaint();
        afficherInfo("Thermostat déplacé (pièce irrégulière, collé au mur).");
        return;
    }
}

// --- CAS PIECE REGULIERE ---
PieceDTO p = controleur.getPiece();
if (p != null && p.getThermostat() != null) {
    controleur.deplacerThermostatSnap(x, y);

    drawing.repaint();
    afficherInfo("Thermostat déplacé (collé au mur le plus proche).");
    return;
}


        // 4) Sinon : rien à déplacer
        afficherAvertissement("Aucun meuble ou thermostat à déplacer.");

    } catch (NumberFormatException ex) {
        afficherAvertissement("Veuillez entrer des valeurs X et Y valides.");
    }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void boutonGenererFilActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boutonGenererFilActionPerformed
        // TODO add your handling code here:   
        effacerMessage(); // Effacer les anciens messages
        // Valeur par défaut 
        double longueurFilMax = 10000.0;
        String valeur = LongueurFil.getText().trim();
        if (!valeur.isEmpty()) {
            try {
                longueurFilMax = UniteImperiale.parseVersPouces(valeur);
            } catch (Exception e) {}
        }
        controleur.setLongueurFilMax(longueurFilMax);
        drawing.repaint();
        


           String selected = (String) ModeApp.getSelectedItem();
            if ("Edition".equals(selected)) {} 
            else {
                drawing.genererEtAfficherFilAutomatique(longueurFilMax); 
                controleur.setMode(Controleur.Mode.MODELISATION);
                drawing.repaint();
                drawing.genererEtDessinerGraphe();            }
    }//GEN-LAST:event_boutonGenererFilActionPerformed

    private void boutonValiderGrilleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boutonValiderGrilleActionPerformed
        // TODO add your handling code here:
        try {
        double val = UniteImperiale.parseVersPouces(inputDistanceIntersection1.getText().trim());
        drawing.setDistanceGrillePouces(val);

        afficherSucces("Nouvelle distance appliquée : " + val + " pouces");
    } catch (NumberFormatException ex) {
        afficherAvertissement("Veuillez entrer une valeur numérique valide.");
    }
    }//GEN-LAST:event_boutonValiderGrilleActionPerformed

    private void boutonValiderTranslationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boutonValiderTranslationActionPerformed
        // TODO add your handling code here:
        effacerMessage();
        try {
            double translationX = 0.0;
            double translationY = 0.0;
            
            String valeurX = inputTranslationX.getText().trim();
            if (!valeurX.isEmpty()) {
                translationX = UniteImperiale.parseVersPouces(valeurX);
            }
            
            String valeurY = inputTranslationY.getText().trim();
            if (!valeurY.isEmpty()) {
                translationY = UniteImperiale.parseVersPouces(valeurY);
            }
            
            controleur.setTranslationX(translationX);
            controleur.setTranslationY(translationY);
            
            // Régénérer le graphe avec la nouvelle translation
            String selected = (String) ModeApp.getSelectedItem();
            if ("Modélisation".equals(selected)) {
                drawing.genererEtDessinerGraphe();
            }
            
            afficherSucces("Translation appliquée : X = " + translationX + " pouces, Y = " + translationY + " pouces");
        } catch (Exception ex) {
            afficherAvertissement("Veuillez entrer des valeurs numériques valides pour la translation.");
        }
    }//GEN-LAST:event_boutonValiderTranslationActionPerformed

    private void exportPNGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPNGActionPerformed
      // Exporter UNIQUEMENT la zone de dessin (le DrawingPanel "drawing")
    int w = drawing.getWidth();
    int h = drawing.getHeight();

    if (w <= 0 || h <= 0) {
        afficherErreur("Zone de dessin vide, rien à exporter.");
        return;
    }

    JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Exporter en PNG");
    chooser.setFileFilter(new FileNameExtensionFilter("Image PNG", "png"));

    int result = chooser.showSaveDialog(this);
    if (result != JFileChooser.APPROVE_OPTION) {
        return; // utilisateur a annulé
    }

    File fichier = chooser.getSelectedFile();
    if (!fichier.getName().toLowerCase().endsWith(".png")) {
        fichier = new File(fichier.getParentFile(), fichier.getName() + ".png");
    }

    try {
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        // Dessiner le contenu du panel de dessin dans l'image
        drawing.printAll(g2);   // ou drawing.paint(g2);
        g2.dispose();

        ImageIO.write(image, "png", fichier);
        afficherSucces("Image exportée dans " + fichier.getName());
    } catch (IOException ex) {
        ex.printStackTrace();
        afficherErreur("Erreur lors de l'export : " + ex.getMessage());
    }   
    }//GEN-LAST:event_exportPNGActionPerformed

    private void LoadProjectMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LoadProjectMenuActionPerformed
        JFileChooser chooser = new JFileChooser();
    chooser.setDialogTitle("Charger un projet");
    chooser.setFileFilter(new FileNameExtensionFilter("Projet HeatMyFloor (*.hmf)", "hmf"));

    int resultat = chooser.showOpenDialog(this);
    if (resultat != JFileChooser.APPROVE_OPTION) {
        return; // Annulé
    }

    File fichier = chooser.getSelectedFile();
    if (!fichier.exists()) {
        afficherErreur("Fichier introuvable !");
        return;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(fichier))) {

        String type = br.readLine();
        if (type == null) {
            afficherErreur("Fichier invalide.");
            return;
        }

       

        if (type.equals("TYPE=RECTANGULAIRE")) {
            // Lecture de la ligne pièce
            String lignePiece = br.readLine();
            String[] p = lignePiece.split(" ");
            double x = Double.parseDouble(p[1]);
            double y = Double.parseDouble(p[2]);
            double largeur = Double.parseDouble(p[3]);
            double longueur = Double.parseDouble(p[4]);

            controleur.creerPieceReguliere(x, y, largeur, longueur);

            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] t = ligne.split(" ");

                switch (t[0]) {
                    case "THERMOSTAT":
                      double tx = Double.parseDouble(t[1]);
                      double ty = Double.parseDouble(t[2]);
                      double tl = Double.parseDouble(t[3]); // largeur
                      double th = Double.parseDouble(t[4]); // hauteur
                      double tAngle = t.length > 5 ? Double.parseDouble(t[5]) : 0.0; // angle (optionnel pour compatibilité)

    controleur.ajouterThermostat(tx, ty, tl, th, tAngle);
    break;

                    case "MEUBLE_SANS_DRAIN":
                        int id = Integer.parseInt(t[1]);
                        TypeMeubleSansDrain typeMeuble =
                                TypeMeubleSansDrain.valueOf(t[2]);

                        double mx = Double.parseDouble(t[3]);
                        double my = Double.parseDouble(t[4]);
                        double ml = Double.parseDouble(t[5]);
                        double mh = Double.parseDouble(t[6]);

                        controleur.ajouterMeubleSansDrain(mx, my, ml, mh, typeMeuble);
                        break;

                    case "MEUBLE_DRAIN":
                        int id2 = Integer.parseInt(t[1]);
                        TypeMeubleDrain typeDrain =
                                TypeMeubleDrain.valueOf(t[2]);

                        double dx = Double.parseDouble(t[3]);
                        double dy = Double.parseDouble(t[4]);
                        double dl = Double.parseDouble(t[5]);
                        double dh = Double.parseDouble(t[6]);
                        double relX = Double.parseDouble(t[7]);
                        double relY = Double.parseDouble(t[8]);
                        double diam = Double.parseDouble(t[9]);

                        controleur.ajouterMeubleAvecDrain(dx, dy, dl, dh,
                                typeDrain, relX, relY, diam);
                        break;

                    case "ELEMENT_CHAUFFANT":
                        double ex = Double.parseDouble(t[1]);
                        double ey = Double.parseDouble(t[2]);
                        double el = Double.parseDouble(t[3]);
                        double eh = Double.parseDouble(t[4]);
                        boolean horizontal = Boolean.parseBoolean(t[5]);
                        double eAngle = t.length > 6 ? Double.parseDouble(t[6]) : 0.0; // angle (optionnel pour compatibilité)

                        controleur.ajouterElementChauffantDepuisLoad(ex, ey, el, eh, horizontal, eAngle);
                        break;
                }
            }
        }

        else if (type.equals("TYPE=IRREGULIERE")) {

            ArrayList<Point2D.Double> points = new ArrayList<>();
            String ligne;

            while ((ligne = br.readLine()) != null && ligne.startsWith("POINT")) {
                String[] t = ligne.split(" ");
                double px = Double.parseDouble(t[1]);
                double py = Double.parseDouble(t[2]);
                points.add(new Point2D.Double(px, py));
            }

            boolean fermee = false;
            double minX = 0, minY = 0, maxX = 0, maxY = 0;

            if (ligne.startsWith("FERMEE")) {
                fermee = Boolean.parseBoolean(ligne.split(" ")[1]);
                ligne = br.readLine(); // BBOX
            }

            if (ligne.startsWith("BBOX")) {
                String[] b = ligne.split(" ");
                minX = Double.parseDouble(b[1]);
                minY = Double.parseDouble(b[2]);
                maxX = Double.parseDouble(b[3]);
                maxY = Double.parseDouble(b[4]);
            }

            controleur.chargerPieceIrreguliere(points, fermee, minX, minY, maxX, maxY);
            
            // Lire les éléments (THERMOSTAT, MEUBLE, ELEMENT_CHAUFFANT) après la pièce irrégulière
            while ((ligne = br.readLine()) != null) {
                String[] t = ligne.split(" ");

                switch (t[0]) {
                    case "THERMOSTAT":
                      double tx = Double.parseDouble(t[1]);
                      double ty = Double.parseDouble(t[2]);
                      double tl = Double.parseDouble(t[3]); // largeur
                      double th = Double.parseDouble(t[4]); // hauteur
                      double tAngle = t.length > 5 ? Double.parseDouble(t[5]) : 0.0; // angle (optionnel pour compatibilité)

    controleur.ajouterThermostat(tx, ty, tl, th, tAngle);
    break;

                    case "MEUBLE_SANS_DRAIN":
                        int id = Integer.parseInt(t[1]);
                        TypeMeubleSansDrain typeMeuble =
                                TypeMeubleSansDrain.valueOf(t[2]);

                        double mx = Double.parseDouble(t[3]);
                        double my = Double.parseDouble(t[4]);
                        double ml = Double.parseDouble(t[5]);
                        double mh = Double.parseDouble(t[6]);

                        controleur.ajouterMeubleSansDrain(mx, my, ml, mh, typeMeuble);
                        break;

                    case "MEUBLE_DRAIN":
                        int id2 = Integer.parseInt(t[1]);
                        TypeMeubleDrain typeDrain =
                                TypeMeubleDrain.valueOf(t[2]);

                        double dx = Double.parseDouble(t[3]);
                        double dy = Double.parseDouble(t[4]);
                        double dl = Double.parseDouble(t[5]);
                        double dh = Double.parseDouble(t[6]);
                        double relX = Double.parseDouble(t[7]);
                        double relY = Double.parseDouble(t[8]);
                        double diam = Double.parseDouble(t[9]);

                        controleur.ajouterMeubleAvecDrain(dx, dy, dl, dh,
                                typeDrain, relX, relY, diam);
                        break;

                    case "ELEMENT_CHAUFFANT":
                        double ex = Double.parseDouble(t[1]);
                        double ey = Double.parseDouble(t[2]);
                        double el = Double.parseDouble(t[3]);
                        double eh = Double.parseDouble(t[4]);
                        boolean horizontal = Boolean.parseBoolean(t[5]);
                        double eAngle = t.length > 6 ? Double.parseDouble(t[6]) : 0.0; // angle (optionnel pour compatibilité)

                        controleur.ajouterElementChauffantDepuisLoad(ex, ey, el, eh, horizontal, eAngle);
                        break;
                }
            }
        }

        afficherSucces("Projet chargé !");
        DrawingPanel.repaint();

    } catch (Exception e) {
        e.printStackTrace();
        afficherErreur("Erreur lors du chargement : " + e.getMessage());
    }
    }//GEN-LAST:event_LoadProjectMenuActionPerformed

    private void quitMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitMenuActionPerformed
        int result = javax.swing.JOptionPane.showConfirmDialog(
            this,
            "Voulez-vous vraiment quitter l'application ?",
            "Quitter",
            javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE
    );

    if (result == javax.swing.JOptionPane.YES_OPTION) {
        System.exit(0);
    }
    }//GEN-LAST:event_quitMenuActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void LongueurFilChoisi(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LongueurFilChoisi
        // TODO add your handling code here:
        
    }//GEN-LAST:event_LongueurFilChoisi

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void longueurZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_longueurZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_longueurZoneActionPerformed

    private void modifierZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modifierZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modifierZoneActionPerformed

    private void deplacerZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deplacerZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_deplacerZoneActionPerformed

    private void AxeXZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxeXZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AxeXZoneActionPerformed

    private void AxeYZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AxeYZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AxeYZoneActionPerformed

    private void largeurZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_largeurZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_largeurZoneActionPerformed

    private void SuprrimerZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SuprrimerZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_SuprrimerZoneActionPerformed

    private void AjouterZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AjouterZoneActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_AjouterZoneActionPerformed

    private void inputDistanceIntersection1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputDistanceIntersection1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputDistanceIntersection1ActionPerformed

    private void inputTranslationYActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputTranslationYActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_inputTranslationYActionPerformed

    public static void main(String args[]) {
       
          java.awt.EventQueue.invokeLater(() -> {
            new MainWindow().setVisible(true); 
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AjouterZone;
    private javax.swing.JTextField AxeX;
    private javax.swing.JTextField AxeX1;
    private javax.swing.JTextField AxeXZone;
    private javax.swing.JTextField AxeY;
    private javax.swing.JTextField AxeY1;
    private javax.swing.JTextField AxeYZone;
    private javax.swing.JPanel DrawingPanel;
    private javax.swing.JPanel EditionPanel;
    private javax.swing.JPanel EditionPanel1;
    private javax.swing.JMenuItem LoadProjectMenu;
    private javax.swing.JTextField LongueurFil;
    private javax.swing.JLabel ModeActive;
    private javax.swing.JComboBox<String> ModeApp;
    private javax.swing.JPanel ModePanel;
    private javax.swing.JLabel NomElementSelectionne;
    private javax.swing.JLabel NomElementSelectionne1;
    private javax.swing.JMenu SaveProjectMenu;
    private javax.swing.JButton SuprrimerZone;
    private javax.swing.JMenuItem ZoomReset;
    private javax.swing.JButton ajouterMeubleAvecDrain;
    private javax.swing.JButton ajouterMeubleAvecDrain1;
    private javax.swing.JButton ajouterMeubleSansDrain;
    private javax.swing.JButton ajouterMeubleSansDrain1;
    private javax.swing.JTextField axeXElementChauffant;
    private javax.swing.JTextField axeYElementChauffant;
    private javax.swing.JButton boutonGenererFil;
    private javax.swing.JButton boutonValiderGrille;
    private javax.swing.JButton boutonValiderTranslation;
    private javax.swing.JButton buttonAjouterElementChauffant;
    private javax.swing.JButton buttonDeplacerElementChauffant;
    private javax.swing.JButton buttonModifierElementChauffant;
    private javax.swing.JButton buttonSupprimerElement;
    private javax.swing.JButton buttonSupprimerElement1;
    private javax.swing.JButton buttonSupprimerElementChauffant;
    private javax.swing.JComboBox<String> comboBoxAvecDrain;
    private javax.swing.JComboBox<String> comboBoxAvecDrain1;
    private javax.swing.JComboBox<String> comboBoxMurElementChauffant;
    private javax.swing.JComboBox<String> comboBoxSansDrain;
    private javax.swing.JComboBox<String> comboBoxSansDrain1;
    private javax.swing.JLabel coordonnéesLabel;
    private javax.swing.JButton deplacerZone;
    private javax.swing.JMenu editMenu;
    private javax.swing.JLabel editionTailleElementSelectionne;
    private javax.swing.JLabel editionTailleElementSelectionne1;
    private javax.swing.JLabel editionTailleElementSelectionne2;
    private javax.swing.JLabel editionTailleElementSelectionne3;
    private javax.swing.JLabel editionTailleElementSelectionne4;
    private javax.swing.JMenuItem exportPNG;
    private javax.swing.JTextField hauteurElementChauffant;
    private javax.swing.JTextField inputDistanceIntersection1;
    private javax.swing.JTextField inputTranslationX;
    private javax.swing.JTextField inputTranslationY;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JButton jButtonDeplacer;
    private javax.swing.JButton jButtonDeplacer1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox10;
    private javax.swing.JComboBox<String> jComboBox6;
    private javax.swing.JComboBox<String> jComboBox7;
    private javax.swing.JComboBox<String> jComboBox8;
    private javax.swing.JComboBox<String> jComboBox9;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField15;
    private javax.swing.JTextField jTextField16;
    private javax.swing.JTextField jTextField17;
    private javax.swing.JTextField jTextField18;
    private javax.swing.JTextField jTextField19;
    private javax.swing.JTextField jTextField20;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JTextField largeur1;
    private javax.swing.JTextField largeur2;
    private javax.swing.JTextField largeur3;
    private javax.swing.JTextField largeur4;
    private javax.swing.JTextField largeurElementChauffant;
    private javax.swing.JLabel largeurLabel1;
    private javax.swing.JLabel largeurLabel2;
    private javax.swing.JLabel largeurLabel3;
    private javax.swing.JLabel largeurLabel4;
    private javax.swing.JLabel largeurLabel5;
    private javax.swing.JLabel largeurLabel6;
    private javax.swing.JLabel largeurLabel7;
    private javax.swing.JLabel largeurLabel8;
    private javax.swing.JLabel largeurLabel9;
    private javax.swing.JTextField largeurZone;
    private javax.swing.JTextField longueur1;
    private javax.swing.JTextField longueur2;
    private javax.swing.JTextField longueur3;
    private javax.swing.JTextField longueur4;
    private javax.swing.JLabel longueurLabel1;
    private javax.swing.JLabel longueurLabel2;
    private javax.swing.JLabel longueurLabel3;
    private javax.swing.JLabel longueurLabel4;
    private javax.swing.JLabel longueurLabel5;
    private javax.swing.JLabel longueurLabel6;
    private javax.swing.JLabel longueurLabel7;
    private javax.swing.JLabel longueurLabel8;
    private javax.swing.JLabel longueurLabel9;
    private javax.swing.JTextField longueurZone;
    private javax.swing.JButton modifierZone;
    private javax.swing.JTextField nomMeuble;
    private javax.swing.JTextField nomMeuble1;
    private javax.swing.JMenuItem nouvellePiece;
    private javax.swing.JMenu quitMenu;
    private javax.swing.JMenuItem redoMenu;
    private javax.swing.JMenuItem saveProject;
    private javax.swing.JMenuItem undoMenu;
    private javax.swing.JMenuItem zoomIn;
    private javax.swing.JMenuItem zoomOut;
    // End of variables declaration//GEN-END:variables
}


