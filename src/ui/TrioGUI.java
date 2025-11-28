package ui;

import game.Trio;
import model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * Interface graphique Swing pour le jeu Trio
 * Permet une interaction visuelle complète avec le jeu
 */
public class TrioGUI extends JFrame {
    private Trio jeu;
    private List<Joueur> joueurs;
    private CartePanel[] panneauxCartes;
    private JLabel labelJoueurActuel;
    private JLabel labelScores;
    private JButton btnVerifier;
    private JButton btnAnnuler;
    private JButton btnNouvelle;
    private JLabel labelMessage;
    private JLabel labelCarteRestantes;

    private static final int CARTES_PAR_LIGNE = 4;

    /**
     * Constructeur de l'interface GUI
     */
    public TrioGUI() {
        setTitle("Jeu Trio - UTBM");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 900);
        setLocationRelativeTo(null);
        setResizable(false);

        joueurs = new ArrayList<>();
        initialiserJoueurs();

        jeu = new Trio(joueurs);
        jeu.demarrerPartie();

        construireInterface();
        afficherCartes();
        afficherScores();
    }

    /**
     * Initialise les joueurs
     */
    private void initialiserJoueurs() {
        String[] nomsParDefaut = {"Alice", "Bob", "Charlie", "Diana"};
        for (int i = 0; i < 2; i++) {
            joueurs.add(new Joueur("joueur_" + i, nomsParDefaut[i]));
        }
    }

    /**
     * Construit l'interface utilisateur
     */
    private void construireInterface() {
        // Panneau principal
        JPanel panneau = new JPanel(new BorderLayout(10, 10));
        panneau.setBackground(new Color(240, 240, 240));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panneau haut - Titre et info
        panneau.add(creerPanneauHaut(), BorderLayout.NORTH);

        // Panneau central - Cartes
        panneau.add(creerPanneauCartes(), BorderLayout.CENTER);

        // Panneau bas - Actions
        panneau.add(creerPanneauBas(), BorderLayout.SOUTH);

        add(panneau);
    }

    /**
     * Crée le panneau haut (titre, joueur actuel, scores)
     */
    private JPanel creerPanneauHaut() {
        JPanel panneau = new JPanel(new BorderLayout());
        panneau.setBackground(new Color(100, 120, 200));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titre = new JLabel("JEU TRIO - UTBM");
        titre.setFont(new Font("Arial", Font.BOLD, 24));
        titre.setForeground(Color.WHITE);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 5));
        infoPanel.setBackground(new Color(100, 120, 200));

        labelJoueurActuel = new JLabel("Joueur: ");
        labelJoueurActuel.setFont(new Font("Arial", Font.BOLD, 14));
        labelJoueurActuel.setForeground(Color.WHITE);

        labelScores = new JLabel("Scores: ");
        labelScores.setFont(new Font("Arial", Font.PLAIN, 12));
        labelScores.setForeground(Color.WHITE);

        labelCarteRestantes = new JLabel("Cartes: ");
        labelCarteRestantes.setFont(new Font("Arial", Font.PLAIN, 12));
        labelCarteRestantes.setForeground(Color.WHITE);

        infoPanel.add(labelJoueurActuel);
        infoPanel.add(labelScores);
        infoPanel.add(labelCarteRestantes);

        panneau.add(titre, BorderLayout.WEST);
        panneau.add(infoPanel, BorderLayout.EAST);

        return panneau;
    }

    /**
     * Crée le panneau des cartes
     */
    private JPanel creerPanneauCartes() {
        JPanel panneau = new JPanel(new GridLayout(3, CARTES_PAR_LIGNE, 10, 10));
        panneau.setBackground(new Color(240, 240, 240));
        panneau.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panneauxCartes = new CartePanel[12];
        for (int i = 0; i < 12; i++) {
            panneauxCartes[i] = new CartePanel(i);
            panneau.add(panneauxCartes[i]);
        }

        return panneau;
    }

    /**
     * Crée le panneau bas (boutons et messages)
     */
    private JPanel creerPanneauBas() {
        JPanel panneau = new JPanel(new BorderLayout());
        panneau.setBackground(Color.WHITE);

        JPanel boutonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        boutonsPanel.setBackground(Color.WHITE);

        btnVerifier = new JButton("Vérifier Trio");
        btnVerifier.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerifier.setPreferredSize(new Dimension(120, 40));
        btnVerifier.setBackground(new Color(76, 175, 80));
        btnVerifier.setForeground(Color.WHITE);
        btnVerifier.addActionListener(e -> verifierTrio());

        btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 12));
        btnAnnuler.setPreferredSize(new Dimension(100, 40));
        btnAnnuler.setBackground(new Color(255, 152, 0));
        btnAnnuler.setForeground(Color.WHITE);
        btnAnnuler.addActionListener(e -> annulerSelection());

        btnNouvelle = new JButton("Nouvelle Partie");
        btnNouvelle.setFont(new Font("Arial", Font.BOLD, 12));
        btnNouvelle.setPreferredSize(new Dimension(120, 40));
        btnNouvelle.setBackground(new Color(100, 120, 200));
        btnNouvelle.setForeground(Color.WHITE);
        btnNouvelle.setVisible(false);
        btnNouvelle.addActionListener(e -> nouvellePartie());

        boutonsPanel.add(btnVerifier);
        boutonsPanel.add(btnAnnuler);
        boutonsPanel.add(btnNouvelle);

        labelMessage = new JLabel(" ");
        labelMessage.setFont(new Font("Arial", Font.BOLD, 14));
        labelMessage.setHorizontalAlignment(JLabel.CENTER);
        labelMessage.setPreferredSize(new Dimension(400, 30));

        panneau.add(boutonsPanel, BorderLayout.WEST);
        panneau.add(labelMessage, BorderLayout.CENTER);

        return panneau;
    }

    /**
     * Affiche les cartes du jeu
     */
    private void afficherCartes() {
        List<Carte> cartes = jeu.getCartesEnJeu();
        for (int i = 0; i < panneauxCartes.length; i++) {
            if (i < cartes.size()) {
                panneauxCartes[i].setCarte(cartes.get(i));
                panneauxCartes[i].setEnabled(true);
            } else {
                panneauxCartes[i].setCarte(null);
                panneauxCartes[i].setEnabled(false);
            }
            panneauxCartes[i].setSelectionnee(jeu.getCartesSelectionnees().contains(i));
            panneauxCartes[i].repaint();
        }
    }

    /**
     * Affiche les scores
     */
    private void afficherScores() {
        labelJoueurActuel.setText("Joueur: " + jeu.getJoueurActuel().getNom());

        StringBuilder scores = new StringBuilder("Scores: ");
        for (Joueur j : jeu.getJoueurs()) {
            scores.append(j.getNom()).append(" (").append(j.getScore()).append(") ");
        }
        labelScores.setText(scores.toString());

        labelCarteRestantes.setText("En jeu: " + jeu.getCartesEnJeu().size() + 
                                   " | Restantes: " + jeu.getNombreCartesRestantes());
    }

    /**
     * Vérifie le trio sélectionné
     */
    private void verifierTrio() {
        if (jeu.getCartesSelectionnees().size() != 3) {
            afficherMessage("Vous devez sélectionner 3 cartes!", Color.RED);
            return;
        }

        if (jeu.verifierTrio()) {
            afficherMessage("✓ Bravo! Trio valide pour " + jeu.getJoueurActuel().getNom() + "!", Color.GREEN);
            afficherCartes();
            afficherScores();

            if (jeu.estTerminee()) {
                terminerPartie();
            }
        } else {
            afficherMessage("✗ Ce n'est pas un trio valide!", Color.RED);
            afficherCartes();
        }

        jeu.joueurSuivant();
        afficherScores();
    }

    /**
     * Annule la sélection
     */
    private void annulerSelection() {
        List<Integer> selectionnees = jeu.getCartesSelectionnees();
        while (!selectionnees.isEmpty()) {
            jeu.selectionnerCarte(selectionnees.get(0));
        }
        afficherCartes();
        afficherMessage("Sélection annulée", Color.BLACK);
    }

    /**
     * Lance une nouvelle partie
     */
    private void nouvellePartie() {
        jeu.demarrerPartie();
        afficherCartes();
        afficherScores();
        afficherMessage(" ", Color.BLACK);
        btnVerifier.setEnabled(true);
        btnNouvelle.setVisible(false);
    }

    /**
     * Termine la partie
     */
    private void terminerPartie() {
        btnVerifier.setEnabled(false);
        btnNouvelle.setVisible(true);

        List<Joueur> joueurs = new ArrayList<>(jeu.getJoueurs());
        joueurs.sort((j1, j2) -> Integer.compare(j2.getScore(), j1.getScore()));

        StringBuilder resultat = new StringBuilder("🎉 Partie terminée! Gagnant: " + joueurs.get(0).getNom());
        afficherMessage(resultat.toString(), new Color(76, 175, 80));
    }

    /**
     * Affiche un message utilisateur
     */
    private void afficherMessage(String message, Color couleur) {
        labelMessage.setText(message);
        labelMessage.setForeground(couleur);
    }

    /**
     * Panneau représentant une carte
     */
    private class CartePanel extends JPanel {
        private int index;
        private Carte carte;
        private boolean selectionnee;

        CartePanel(int index) {
            this.index = index;
            this.carte = null;
            this.selectionnee = false;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (carte != null) {
                        jeu.selectionnerCarte(index);
                        afficherCartes();
                    }
                }
            });
        }

        void setCarte(Carte carte) {
            this.carte = carte;
        }

        void setSelectionnee(boolean selectionnee) {
            this.selectionnee = selectionnee;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (carte == null) {
                g2d.setColor(new Color(200, 200, 200));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            // Fond
            if (selectionnee) {
                g2d.setColor(new Color(255, 235, 59));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setColor(new Color(255, 193, 7));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
            }

            // Affiche les symboles
            String[] symboles = new String[carte.getValeur()];
            for (int i = 0; i < carte.getValeur(); i++) {
                symboles[i] = carte.getForme().getSymbole();
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.setColor(getCouleurGraphique(carte.getCouleur()));

            int y = getHeight() / 2 + 10;
            for (int i = 0; i < symboles.length; i++) {
                int x = getWidth() / 2 - 10;
                g2d.drawString(symboles[i], x, y + (i * 25));
            }

            // Info textuelle
            g2d.setFont(new Font("Arial", Font.PLAIN, 8));
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(carte.getCouleur().getNom(), 5, getHeight() - 5);
        }

        private Color getCouleurGraphique(Couleur couleur) {
            return switch (couleur) {
                case ROUGE -> new Color(220, 20, 60);
                case VERT -> new Color(34, 139, 34);
                case VIOLET -> new Color(138, 43, 226);
            };
        }
    }

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrioGUI frame = new TrioGUI();
            frame.setVisible(true);
        });
    }
}
