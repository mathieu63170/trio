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

    public Vue(Modele modele, Controleur controleur) {
        this.modele = modele;
        this.controleur = controleur;

        setTitle("Jeu du Trio");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 700);
        setLayout(new BorderLayout(20, 20)); // Espaces entre les régions

        // Panneau gauche pour joueur gauche
        panneauGauche = new JPanel();
        panneauGauche.setLayout(new BoxLayout(panneauGauche, BoxLayout.Y_AXIS));
        panneauGauche.setBorder(BorderFactory.createTitledBorder("Joueur Gauche"));
        add(panneauGauche, BorderLayout.WEST);

        // Panneau haut pour joueur haut
        panneauHaut = new JPanel();
        panneauHaut.setLayout(new FlowLayout());
        panneauHaut.setBorder(BorderFactory.createTitledBorder("Joueur Haut"));
        add(panneauHaut, BorderLayout.NORTH);

        // Panneau droite pour joueur droite
        panneauDroite = new JPanel();
        panneauDroite.setLayout(new BoxLayout(panneauDroite, BoxLayout.Y_AXIS));
        panneauDroite.setBorder(BorderFactory.createTitledBorder("Joueur Droite"));
        add(panneauDroite, BorderLayout.EAST);

        // Panneau central pour les cartes au centre
        panneauCentre = new JPanel();
        panneauCentre.setLayout(new GridLayout(3, 4, 10, 10)); // Grille 3x4 avec espaces
        panneauCentre.setBorder(BorderFactory.createTitledBorder("Cartes au Centre"));
        add(panneauCentre, BorderLayout.CENTER);

        // Panneau inférieur pour les cartes du joueur actuel et boutons
        panneauBas = new JPanel();
        panneauBas.setLayout(new BorderLayout());
        JPanel panneauCartesJoueur = new JPanel();
        panneauCartesJoueur.setLayout(new FlowLayout());
        panneauCartesJoueur.setBorder(BorderFactory.createTitledBorder("Vos Cartes"));
        panneauBas.add(panneauCartesJoueur, BorderLayout.CENTER);

        JPanel panneauBoutons = new JPanel();
        boutonVerifier = new JButton("Vérifier Trio");
        panneauBoutons.add(boutonVerifier);
        panneauBas.add(panneauBoutons, BorderLayout.SOUTH);

        add(panneauBas, BorderLayout.SOUTH);

        // Ajouter les listeners
        boutonVerifier.addActionListener((e) -> controleur.boutonVerifier());

        setVisible(true);
    }

    public void afficherJoueurs(List<Joueur> joueurs) {
        // Vider tous les panneaux
        panneauGauche.removeAll();
        panneauHaut.removeAll();
        panneauDroite.removeAll();

        int nbJoueurs = joueurs.size();
        int idJoueur = modele.getMonJoueur().getId();

        if (nbJoueurs >= 1) {
            if (nbJoueurs == 4) {
                // 4 joueurs : 1 en haut, 1 à gauche, 1 à droite
            	ajouterBoutonsJoueur(panneauGauche, 1, "gauche", joueurs.get((idJoueur + 1) % nbJoueurs));
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs), 0);
                ajouterBoutonsJoueur(panneauDroite, 2, "droite", joueurs.get((idJoueur + 3) % nbJoueurs));
            }
            else if (nbJoueurs == 5) {
                // 5 joueurs : 2 en haut, 1 à gauche, 1 à droite
            	ajouterBoutonsJoueur(panneauGauche, 2, "gauche", joueurs.get((idJoueur + 1) % nbJoueurs));
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs), 0);
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 3) % nbJoueurs), 1);
                ajouterBoutonsJoueur(panneauDroite, 3, "droite", joueurs.get((idJoueur + 4) % nbJoueurs));
            } else if (nbJoueurs == 6) {
                // 6 joueurs : 3 en haut, 1 à gauche, 1 à droite
            	ajouterBoutonsJoueur(panneauGauche, 3, "gauche", joueurs.get((idJoueur + 1) % nbJoueurs));
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs), 0);
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 3) % nbJoueurs), 1);
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 4) % nbJoueurs), 2);
                ajouterBoutonsJoueur(panneauDroite, 4, "droite", joueurs.get((idJoueur + 5) % nbJoueurs));
            } else {
                // Par défaut pour 3 joueurs
            	ajouterPanelJoueurHaut(joueurs.get((idJoueur + 1) % nbJoueurs), 0);
                ajouterPanelJoueurHaut(joueurs.get((idJoueur + 2) % nbJoueurs), 1);
            }
        }

        // Revalider et repeindre tous les panneaux
        panneauGauche.revalidate();
        panneauGauche.repaint();
        panneauHaut.revalidate();
        panneauHaut.repaint();
        panneauDroite.revalidate();
        panneauDroite.repaint();
    }

    private void ajouterPanelJoueurHaut(Joueur joueur, int index) {
        JPanel panelJoueur = new JPanel();
        panelJoueur.setLayout(new FlowLayout());
        ajouterBoutonsJoueur(panelJoueur, index, "haut", joueur);
        panneauHaut.add(panelJoueur);
    }

    private void ajouterBoutonsJoueur(JPanel panneau, int indexJoueur, String position, Joueur joueur) {
        panneau.setBorder(BorderFactory.createTitledBorder(joueur.getNom()));
        JButton btnPlusPetite = new JButton("Révéler + petite");
        btnPlusPetite.addActionListener((e) -> controleur.revelerCarteAutreJoueur(joueur.getId(),true));
        panneau.add(btnPlusPetite);

        JButton btnPlusHaute = new JButton("Révéler + haute");
        btnPlusHaute.addActionListener((e) -> controleur.revelerCarteAutreJoueur(joueur.getId(),false));
        panneau.add(btnPlusHaute);
    }

    public void afficherCartesCentre(List<Carte> cartes) {
    	panneauCentre.removeAll();
        for (int i = 0; i < cartes.size(); i++) {
            JButton boutonCarte = new JButton("?"); // Valeur cachée
            boutonCarte.setActionCommand("centre_" + i);
            int index = i;
            boutonCarte.addActionListener((e) -> controleur.revelerCarteCentre(index));
            panneauCentre.add(boutonCarte);
        }
        panneauCentre.revalidate();
        panneauCentre.repaint();
    }

    public void afficherCartesJoueur(List<Carte> cartes) {
        JPanel panneauCartesJoueur = (JPanel) panneauBas.getComponent(0);
        panneauCartesJoueur.removeAll();
        for (int i = 0; i < cartes.size(); i++) {
            JButton boutonCarte = new JButton(String.valueOf(cartes.get(i).getValeur()));
            boutonCarte.setActionCommand("joueur_" + i);
            int index = i;
            boutonCarte.addActionListener((e) -> controleur.revelerCarteJoueur(cartes.get(index).getValeur()));
            panneauCartesJoueur.add(boutonCarte);
        }
        panneauCartesJoueur.revalidate();
        panneauCartesJoueur.repaint();
    }

    public void revelerCarteCentre(int index, int valeur) {
        Component[] components = panneauCentre.getComponents();
        if (index < components.length && components[index] instanceof JButton) {
            // Changer l'affichage de la carte concernée pour lui donner la carte de valeur : valeur
        }
    }

    public void afficherMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
}
