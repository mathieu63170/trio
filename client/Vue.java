package client;

import commun.plateau.Carte;
import commun.plateau.Joueur;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class Vue extends JFrame {
    private Modele modele;
    private Controleur controleur;
    private JPanel panneauCentre;
    private JPanel panneauBas;
    private JPanel panneauGauche;
    private JPanel panneauHaut;
    private JPanel panneauDroite;
    private JButton boutonVerifier;
    private JLabel labelEtat;

    public Vue(Modele modele, Controleur controleur) {
        this.modele = modele;
        this.controleur = controleur;

        setTitle("🎮 Jeu du Trio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        
        // Couleur de fond
        getContentPane().setBackground(new Color(220, 240, 250));

        // Panneau haut - Titre et état
        JPanel panneauTitre = new JPanel();
        panneauTitre.setBackground(new Color(30, 60, 120));
        labelEtat = new JLabel("🎯 Trio - Jeu en cours");
        labelEtat.setForeground(Color.WHITE);
        labelEtat.setFont(new Font("Arial", Font.BOLD, 16));
        panneauTitre.add(labelEtat);
        add(panneauTitre, BorderLayout.NORTH);

        // Panneau gauche pour joueur gauche
        panneauGauche = new JPanel();
        panneauGauche.setLayout(new BoxLayout(panneauGauche, BoxLayout.Y_AXIS));
        panneauGauche.setBorder(BorderFactory.createTitledBorder("👤 Joueur Gauche"));
        panneauGauche.setBackground(new Color(240, 250, 255));
        add(panneauGauche, BorderLayout.WEST);

        // Panneau haut pour joueur haut
        panneauHaut = new JPanel();
        panneauHaut.setLayout(new FlowLayout());
        panneauHaut.setBorder(BorderFactory.createTitledBorder("👥 Autres Joueurs"));
        panneauHaut.setBackground(new Color(240, 250, 255));

        // Panneau droite pour joueur droite
        panneauDroite = new JPanel();
        panneauDroite.setLayout(new BoxLayout(panneauDroite, BoxLayout.Y_AXIS));
        panneauDroite.setBorder(BorderFactory.createTitledBorder("👤 Joueur Droite"));
        panneauDroite.setBackground(new Color(240, 250, 255));
        add(panneauDroite, BorderLayout.EAST);

        // Panneau central pour les cartes au centre
        panneauCentre = new JPanel();
        panneauCentre.setLayout(new GridLayout(3, 4, 15, 15));
        panneauCentre.setBorder(BorderFactory.createTitledBorder("🎴 Cartes au Centre"));
        panneauCentre.setBackground(new Color(200, 220, 240));
        JPanel containerCentre = new JPanel(new BorderLayout());
        containerCentre.add(panneauCentre, BorderLayout.CENTER);
        add(containerCentre, BorderLayout.CENTER);

        // Panneau inférieur pour les cartes du joueur actuel et boutons
        panneauBas = new JPanel();
        panneauBas.setLayout(new BorderLayout());
        panneauBas.setBackground(new Color(240, 250, 255));
        
        JPanel panneauCartesJoueur = new JPanel();
        panneauCartesJoueur.setLayout(new FlowLayout());
        panneauCartesJoueur.setBorder(BorderFactory.createTitledBorder("🎯 Vos Cartes"));
        panneauCartesJoueur.setBackground(new Color(240, 250, 255));
        panneauBas.add(panneauCartesJoueur, BorderLayout.CENTER);

        JPanel panneauBoutons = new JPanel();
        panneauBoutons.setBackground(new Color(240, 250, 255));
        
        boutonVerifier = new JButton("✓ Vérifier Trio");
        boutonVerifier.setFont(new Font("Arial", Font.BOLD, 12));
        boutonVerifier.setBackground(new Color(50, 150, 50));
        boutonVerifier.setForeground(Color.WHITE);
        boutonVerifier.setPreferredSize(new Dimension(150, 40));
        boutonVerifier.setFocusPainted(false);
        
        panneauBoutons.add(boutonVerifier);
        panneauBas.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauBas, BorderLayout.SOUTH);

        // Ajouter les listeners
        if (controleur != null) {
            boutonVerifier.addActionListener((e) -> controleur.boutonVerifier());
        }

        setVisible(true);
    }
    
    public void setControleur(Controleur controleur) {
        this.controleur = controleur;
        boutonVerifier.addActionListener((e) -> controleur.boutonVerifier());
    }

    public void afficherJoueurs(List<Joueur> joueurs) {
        // Vider tous les panneaux
        panneauGauche.removeAll();
        panneauHaut.removeAll();
        panneauDroite.removeAll();

        int nbJoueurs = joueurs.size();
        Joueur monJoueur = modele.getMonJoueur();
        if (monJoueur == null || nbJoueurs == 0) {
            return;
        }
        int idJoueur = monJoueur.getId();

        if (nbJoueurs >= 1) {
            if (nbJoueurs <= 3) {
                // 2-3 joueurs
                for (int i = 0; i < nbJoueurs; i++) {
                    if (joueurs.get(i).getId() != idJoueur) {
                        ajouterPanelJoueurHaut(joueurs.get(i));
                    }
                }
            } else if (nbJoueurs == 4) {
                // 4 joueurs : 1 en haut, 1 à gauche, 1 à droite
                ajouterBoutonsJoueur(panneauGauche, joueurs.get((idJoueur + 1) % nbJoueurs));
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs));
                ajouterBoutonsJoueur(panneauDroite, joueurs.get((idJoueur + 3) % nbJoueurs));
            } else if (nbJoueurs >= 5) {
                // 5+ joueurs : 2 en haut, 1 à gauche, 1 à droite
                ajouterBoutonsJoueur(panneauGauche, joueurs.get((idJoueur + 1) % nbJoueurs));
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs));
                if (nbJoueurs > 5) {
                    ajouterPanelJoueurHaut(joueurs.get((idJoueur + 3) % nbJoueurs));
                }
                ajouterBoutonsJoueur(panneauDroite, joueurs.get((idJoueur + nbJoueurs - 1) % nbJoueurs));
            }
        }

        // Ajouter panneauHaut au layout si pas encore fait
        if (panneauHaut.getParent() == null) {
            add(panneauHaut, BorderLayout.NORTH);
        }

        // Revalider et repeindre tous les panneaux
        panneauGauche.revalidate();
        panneauGauche.repaint();
        panneauHaut.revalidate();
        panneauHaut.repaint();
        panneauDroite.revalidate();
        panneauDroite.repaint();
    }

    private void ajouterPanelJoueurHaut(Joueur joueur) {
        JPanel panelJoueur = new JPanel();
        panelJoueur.setLayout(new BoxLayout(panelJoueur, BoxLayout.Y_AXIS));
        panelJoueur.setBackground(new Color(255, 250, 240));
        panelJoueur.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        ajouterBoutonsJoueur(panelJoueur, joueur);
        panneauHaut.add(panelJoueur);
    }

    private void ajouterBoutonsJoueur(JPanel panneau, Joueur joueur) {
        JLabel labelNom = new JLabel("👤 " + joueur.getNom());
        labelNom.setFont(new Font("Arial", Font.BOLD, 11));
        panneau.add(labelNom);
        
        JLabel labelScore = new JLabel("Trios: " + (joueur.getTrios() != null ? joueur.getTrios().size() : 0));
        labelScore.setFont(new Font("Arial", Font.PLAIN, 10));
        panneau.add(labelScore);
        
        JButton btnPlusPetite = new JButton("🔽 Min");
        btnPlusPetite.setFont(new Font("Arial", Font.PLAIN, 10));
        btnPlusPetite.setBackground(new Color(100, 150, 200));
        btnPlusPetite.setForeground(Color.WHITE);
        btnPlusPetite.setFocusPainted(false);
        btnPlusPetite.addActionListener((e) -> controleur.revelerCarteAutreJoueur(joueur.getId(), true));
        panneau.add(btnPlusPetite);

        JButton btnPlusHaute = new JButton("🔼 Max");
        btnPlusHaute.setFont(new Font("Arial", Font.PLAIN, 10));
        btnPlusHaute.setBackground(new Color(100, 150, 200));
        btnPlusHaute.setForeground(Color.WHITE);
        btnPlusHaute.setFocusPainted(false);
        btnPlusHaute.addActionListener((e) -> controleur.revelerCarteAutreJoueur(joueur.getId(), false));
        panneau.add(btnPlusHaute);
    }

    public void afficherCartesCentre(List<Carte> cartes) {
        panneauCentre.removeAll();
        for (int i = 0; i < cartes.size(); i++) {
            JButton boutonCarte = creerBoutonCarte(cartes.get(i), i, true);
            panneauCentre.add(boutonCarte);
        }
        panneauCentre.revalidate();
        panneauCentre.repaint();
    }

    public void afficherCartesJoueur(List<Carte> cartes) {
        JPanel panneauCartesJoueur = (JPanel) panneauBas.getComponent(0);
        panneauCartesJoueur.removeAll();
        for (int i = 0; i < cartes.size(); i++) {
            JButton boutonCarte = creerBoutonCarte(cartes.get(i), i, false);
            panneauCartesJoueur.add(boutonCarte);
        }
        panneauCartesJoueur.revalidate();
        panneauCartesJoueur.repaint();
    }

    private JButton creerBoutonCarte(Carte carte, int index, boolean estCentre) {
        JButton boutonCarte = new JButton();
        boutonCarte.setPreferredSize(new Dimension(80, 120));
        boutonCarte.setFont(new Font("Arial", Font.BOLD, 18));
        
        if (carte.isRevelee()) {
            boutonCarte.setText(String.valueOf(carte.getValeur()));
            boutonCarte.setBackground(new Color(100, 200, 100));
            boutonCarte.setForeground(Color.WHITE);
        } else {
            boutonCarte.setText("?");
            boutonCarte.setBackground(new Color(200, 100, 100));
            boutonCarte.setForeground(Color.WHITE);
            if (estCentre) {
                int finalIndex = index;
                boutonCarte.addActionListener((e) -> controleur.revelerCarteCentre(finalIndex));
            } else {
                int valeur = carte.getValeur();
                boutonCarte.addActionListener((e) -> controleur.revelerCarteJoueur(valeur));
            }
        }
        
        boutonCarte.setFocusPainted(false);
        boutonCarte.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        return boutonCarte;
    }

    public void revelerCarteCentre(int index, int valeur) {
        Component[] components = panneauCentre.getComponents();
        if (index < components.length && components[index] instanceof JButton) {
            JButton btn = (JButton) components[index];
            btn.setText(String.valueOf(valeur));
            btn.setBackground(new Color(100, 200, 100));
        }
    }

    public void afficherMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Trio", JOptionPane.INFORMATION_MESSAGE);
    }
}
