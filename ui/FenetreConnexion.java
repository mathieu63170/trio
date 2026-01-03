package ui;

import game.Trio;
import commun.plateau.*;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe FenetreConnexion : fenêtre de login pour le jeu Trio
 */
public class FenetreConnexion extends JFrame {
    private JTextField[] champJoueurs;
    private JPanel[] panelJoueurs;
    private JSpinner spinnerNbJoueurs;
    private JButton btnDemarrer;
    private TrioGUI jeuGui;

    public FenetreConnexion() {
        setTitle("Jeu du Trio - Connexion");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(220, 240, 250));

        // Panneau haut - Titre
        JPanel panelTitre = new JPanel();
        panelTitre.setBackground(new Color(30, 60, 120));
        JLabel labelTitre = new JLabel("BIENVENUE AU JEU TRIO");
        labelTitre.setForeground(Color.WHITE);
        labelTitre.setFont(new Font("Arial", Font.BOLD, 20));
        panelTitre.add(labelTitre);
        add(panelTitre, BorderLayout.NORTH);

        // Panneau central
        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(new BoxLayout(panelCentral, BoxLayout.Y_AXIS));
        panelCentral.setBackground(new Color(220, 240, 250));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Sélection nombre de joueurs
        JPanel panelNbJoueurs = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelNbJoueurs.setBackground(new Color(220, 240, 250));
        panelNbJoueurs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        JLabel labelNb = new JLabel("Nombre de joueurs (2-6): ");
        labelNb.setFont(new Font("Arial", Font.BOLD, 12));
        panelNbJoueurs.add(labelNb);

        spinnerNbJoueurs = new JSpinner(new SpinnerNumberModel(3, 2, 6, 1));
        spinnerNbJoueurs.setPreferredSize(new Dimension(50, 30));
        spinnerNbJoueurs.addChangeListener(e -> actualiserChamps());
        panelNbJoueurs.add(spinnerNbJoueurs);

        panelCentral.add(panelNbJoueurs);
        panelCentral.add(Box.createVerticalStrut(20));

        // Champs pour les noms des joueurs
        champJoueurs = new JTextField[6];
        panelJoueurs = new JPanel[6];
        String[] nomsParDefaut = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"};

        for (int i = 0; i < 6; i++) {
            JPanel panelJoueur = new JPanel(new FlowLayout(FlowLayout.LEFT));
            panelJoueur.setBackground(new Color(220, 240, 250));
            panelJoueur.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            panelJoueurs[i] = panelJoueur;

            JLabel labelJoueur = new JLabel("Joueur " + (i + 1) + ": ");
            labelJoueur.setFont(new Font("Arial", Font.BOLD, 11));
            labelJoueur.setPreferredSize(new Dimension(100, 25));
            panelJoueur.add(labelJoueur);

            champJoueurs[i] = new JTextField(20);
            champJoueurs[i].setPreferredSize(new Dimension(200, 30));
            champJoueurs[i].setText(nomsParDefaut[i]);
            panelJoueur.add(champJoueurs[i]);

            panelCentral.add(panelJoueur);
        }

        panelCentral.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(panelCentral);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Panneau bas - Bouton démarrer
        JPanel panelBas = new JPanel();
        panelBas.setBackground(new Color(220, 240, 250));
        btnDemarrer = new JButton("Demarrer la Partie");
        btnDemarrer.setFont(new Font("Arial", Font.BOLD, 14));
        btnDemarrer.setBackground(new Color(50, 150, 50));
        btnDemarrer.setForeground(Color.WHITE);
        btnDemarrer.setFocusPainted(false);
        btnDemarrer.setPreferredSize(new Dimension(200, 50));
        btnDemarrer.addActionListener(e -> demarrerPartie());
        panelBas.add(btnDemarrer);
        add(panelBas, BorderLayout.SOUTH);

        actualiserChamps();
        setVisible(true);
    }

    /**
     * Actualise la visibilité des champs selon le nombre de joueurs sélectionné
     */
    private void actualiserChamps() {
        int nbJoueurs = (Integer) spinnerNbJoueurs.getValue();
        for (int i = 0; i < 6; i++) {
            panelJoueurs[i].setVisible(i < nbJoueurs);
        }
    }

    /**
     * Démarre la partie avec les joueurs saisis
     */
    private void demarrerPartie() {
        int nbJoueurs = (Integer) spinnerNbJoueurs.getValue();
        List<Joueur> joueurs = new ArrayList<>();

        for (int i = 0; i < nbJoueurs; i++) {
            String nom = champJoueurs[i].getText().trim();
            if (nom.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez entrer un nom pour le joueur " + (i + 1),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            joueurs.add(new Joueur(i + 1, nom, new ArrayList<>(), new ArrayList<>()));
        }

        // Lancer le jeu
        this.dispose();
        jeuGui = new TrioGUI();
        jeuGui.demarrer(joueurs);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FenetreConnexion());
    }
}
