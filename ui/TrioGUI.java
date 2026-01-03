package ui;

import game.Trio;
import commun.plateau.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Classe TrioGUI : interface graphique Swing pour le jeu Trio
 */
public class TrioGUI extends JFrame {
    private Trio jeu;
    private CartePanel[] cartesPanels;
    private JLabel labelJoueur;
    private JLabel labelScore;
    private JButton btnVerifier;
    private JButton btnAnnuler;
    private JButton btnNouvelle;
    private JPanel panelCartes;
    private JPanel panelJoueurs;

    public TrioGUI() {
        setTitle("Jeu du Trio - UTBM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(220, 240, 250));

        // Panneau haut (titre et info)
        JPanel panelHaut = creerPanelHaut();
        add(panelHaut, BorderLayout.NORTH);

        // Panneau cartes central
        panelCartes = new JPanel(new GridLayout(3, 4, 10, 10));
        panelCartes.setBackground(new Color(200, 220, 240));
        panelCartes.setBorder(BorderFactory.createTitledBorder("Cartes au Centre"));
        cartesPanels = new CartePanel[12];
        for (int i = 0; i < 12; i++) {
            cartesPanels[i] = new CartePanel(i);
            panelCartes.add(cartesPanels[i]);
        }
        add(panelCartes, BorderLayout.CENTER);

        // Panneau bas (boutons)
        JPanel panelBas = creerPanelBas();
        add(panelBas, BorderLayout.SOUTH);

        // Panneau droite (joueurs et score)
        panelJoueurs = new JPanel();
        panelJoueurs.setLayout(new BoxLayout(panelJoueurs, BoxLayout.Y_AXIS));
        panelJoueurs.setBackground(new Color(240, 250, 255));
        panelJoueurs.setBorder(BorderFactory.createTitledBorder("Scores"));
        add(new JScrollPane(panelJoueurs), BorderLayout.EAST);

        setVisible(true);
    }

    private JPanel creerPanelHaut() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 60, 120));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        labelJoueur = new JLabel("Au tour de : ");
        labelJoueur.setForeground(Color.WHITE);
        labelJoueur.setFont(new Font("Arial", Font.BOLD, 16));

        labelScore = new JLabel("Score: 0");
        labelScore.setForeground(Color.WHITE);
        labelScore.setFont(new Font("Arial", Font.BOLD, 14));

        panel.add(labelJoueur, BorderLayout.WEST);
        panel.add(labelScore, BorderLayout.EAST);

        return panel;
    }

    private JPanel creerPanelBas() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(240, 250, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        btnVerifier = new JButton("Verifier Trio");
        btnVerifier.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerifier.setBackground(new Color(50, 150, 50));
        btnVerifier.setForeground(Color.WHITE);
        btnVerifier.setFocusPainted(false);
        btnVerifier.setPreferredSize(new Dimension(150, 40));
        btnVerifier.addActionListener(e -> verifierTrio());
        panel.add(btnVerifier);

        btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 12));
        btnAnnuler.setBackground(new Color(150, 150, 50));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.setFocusPainted(false);
        btnAnnuler.setPreferredSize(new Dimension(150, 40));
        btnAnnuler.addActionListener(e -> annulerSelection());
        panel.add(btnAnnuler);

        btnNouvelle = new JButton("Nouvelle Partie");
        btnNouvelle.setFont(new Font("Arial", Font.BOLD, 12));
        btnNouvelle.setBackground(new Color(50, 100, 150));
        btnNouvelle.setForeground(Color.WHITE);
        btnNouvelle.setFocusPainted(false);
        btnNouvelle.setPreferredSize(new Dimension(150, 40));
        btnNouvelle.addActionListener(e -> nouvellePartie());
        panel.add(btnNouvelle);

        return panel;
    }

    public void demarrer() {
        // Créer les joueurs
        java.util.List<Joueur> joueurs = new java.util.ArrayList<>();
        joueurs.add(new Joueur(1, "Joueur 1", new java.util.ArrayList<>(), new java.util.ArrayList<>()));
        joueurs.add(new Joueur(2, "Joueur 2", new java.util.ArrayList<>(), new java.util.ArrayList<>()));
        joueurs.add(new Joueur(3, "Joueur 3", new java.util.ArrayList<>(), new java.util.ArrayList<>()));

        demarrer(joueurs);
    }

    public void demarrer(java.util.List<Joueur> joueurs) {
        jeu = new Trio(joueurs);
        jeu.demarrerPartie();

        actualiserAffichage();
    }

    private void verifierTrio() {
        boolean valide = jeu.validerTrio();
        actualiserAffichage();
        
        if (valide) {
            JOptionPane.showMessageDialog(this, "✓ Trio valide! +1 point", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "❌ Trio invalide! Essayez à nouveau.", "Erreur", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void annulerSelection() {
        jeu.annulerSelection();
        actualiserAffichage();
    }

    private void nouvellePartie() {
        demarrer();
        actualiserAffichage();
    }

    private void actualiserAffichage() {
        // Actualiser les cartes
        Plateau plateau = jeu.getPlateau();
        List<Carte> cartes = plateau.getMillieu();
        for (int i = 0; i < cartes.size() && i < 12; i++) {
            cartesPanels[i].setCarte(cartes.get(i), jeu.getSelection().contains(i));
        }

        // Actualiser l'info joueur
        Joueur joueur = jeu.getJoueurCourant();
        int score = (joueur.getTrios() != null) ? joueur.getTrios().size() : 0;
        labelJoueur.setText("🎯 Au tour de : " + joueur.getNom());
        labelScore.setText("Trios: " + score);

        // Actualiser les scores
        actualiserScores();
    }

    private void actualiserScores() {
        panelJoueurs.removeAll();
        for (Joueur joueur : jeu.getJoueurs()) {
            JLabel label = new JLabel(joueur.getNom() + " : " + 
                                     ((joueur.getTrios() != null) ? joueur.getTrios().size() : 0) + " trios");
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            panelJoueurs.add(label);
        }
        panelJoueurs.revalidate();
        panelJoueurs.repaint();
    }

    /**
     * Inner class pour représenter une carte graphiquement
     */
    private class CartePanel extends JPanel {
        private Carte carte;
        private int index;
        private boolean selectionnee;

        public CartePanel(int index) {
            this.index = index;
            this.selectionnee = false;
            setBackground(new Color(200, 100, 100));
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    jeu.selectionnerCarte(index);
                    actualiserAffichage();
                }
            });
        }

        public void setCarte(Carte carte, boolean sel) {
            this.carte = carte;
            this.selectionnee = sel;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            // Couleur de fond selon sélection
            if (selectionnee) {
                setBackground(new Color(255, 255, 100));
            } else if (carte != null && carte.isRevelee()) {
                setBackground(new Color(150, 200, 150));
            } else {
                setBackground(new Color(200, 100, 100));
            }

            setOpaque(true);
            super.paintComponent(g);

            // Afficher la carte
            if (carte != null && carte.isRevelee()) {
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.BOLD, 12));
                
                int y_pos = 20;
                
                // Valeur
                String valeur = "x" + carte.getValeur();
                FontMetrics fm = g2d.getFontMetrics();
                int x = (width - fm.stringWidth(valeur)) / 2;
                g2d.drawString(valeur, x, y_pos);
                
                // Forme
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String forme = carte.getForme().getSymbole();
                fm = g2d.getFontMetrics();
                x = (width - fm.stringWidth(forme)) / 2;
                g2d.drawString(forme, x, y_pos + 25);
                
                // Couleur
                String couleur = carte.getCouleur().getEmoji();
                fm = g2d.getFontMetrics();
                x = (width - fm.stringWidth(couleur)) / 2;
                g2d.drawString(couleur, x, y_pos + 45);
                
                // Remplissage
                String remplissage = carte.getRemplissage().getSymbole();
                fm = g2d.getFontMetrics();
                x = (width - fm.stringWidth(remplissage)) / 2;
                g2d.drawString(remplissage, x, y_pos + 65);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 24));
                String texte = "?";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (width - fm.stringWidth(texte)) / 2;
                int y = (height - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(texte, x, y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new FenetreConnexion();
            System.out.println("✓ Fenêtre de connexion lancée!");
        });
    }
}
