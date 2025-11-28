package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Interface graphique client Trio
 */
public class TrioClientGUI extends JFrame {
    
    private TrioClient client;
    private JLabel labelServeur;
    private JTextField fieldNomJoueur;
    private JButton btnConnecter;
    private JPanel panelConnexion;
    private JPanel panelJeu;
    private CardLayout cardLayout;
    private JPanel panelPrincipal;
    
    public TrioClientGUI(TrioClient client) {
        this.client = client;
        client.setGUI(this);
        
        setTitle("Trio - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // CardLayout pour basculer entre connexion et jeu
        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);
        
        // Panel de connexion
        creerPanelConnexion();
        panelPrincipal.add(panelConnexion, "CONNEXION");
        
        // Panel de jeu
        creerPanelJeu();
        panelPrincipal.add(panelJeu, "JEU");
        
        add(panelPrincipal);
        setVisible(true);
    }
    
    private void creerPanelConnexion() {
        panelConnexion = new JPanel(new GridBagLayout());
        panelConnexion.setBackground(new Color(240, 240, 240));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Titre
        JLabel labelTitre = new JLabel("Connexion au serveur Trio");
        labelTitre.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panelConnexion.add(labelTitre, gbc);
        
        // Label serveur
        labelServeur = new JLabel("Serveur: localhost:5000");
        labelServeur.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        panelConnexion.add(labelServeur, gbc);
        
        // Champ nom joueur
        JLabel labelNom = new JLabel("Nom du joueur:");
        labelNom.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 1;
        panelConnexion.add(labelNom, gbc);
        
        fieldNomJoueur = new JTextField(20);
        fieldNomJoueur.setText("Joueur1");
        gbc.gridx = 1; gbc.gridy = 2;
        panelConnexion.add(fieldNomJoueur, gbc);
        
        // Bouton connecter
        btnConnecter = new JButton("Se connecter");
        btnConnecter.setFont(new Font("Arial", Font.BOLD, 12));
        btnConnecter.addActionListener(e -> connecter());
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        panelConnexion.add(btnConnecter, gbc);
    }
    
    private void creerPanelJeu() {
        panelJeu = new JPanel(new BorderLayout());
        panelJeu.setBackground(new Color(200, 220, 200));
        
        // Panel haut: informations
        JPanel panelHaut = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelHaut.add(new JLabel("Bienvenue au jeu Trio!"));
        panelJeu.add(panelHaut, BorderLayout.NORTH);
        
        // Panel central: cartes
        JPanel panelCartes = new JPanel(new GridLayout(3, 4, 5, 5));
        panelCartes.setBackground(new Color(200, 220, 200));
        for (int i = 0; i < 12; i++) {
            JButton btn = new JButton("Carte " + i);
            panelCartes.add(btn);
        }
        panelJeu.add(new JScrollPane(panelCartes), BorderLayout.CENTER);
        
        // Panel bas: boutons
        JPanel panelBas = new JPanel(new FlowLayout());
        JButton btnVerifier = new JButton("Vérifier Trio");
        JButton btnAnnuler = new JButton("Annuler");
        panelBas.add(btnVerifier);
        panelBas.add(btnAnnuler);
        panelJeu.add(panelBas, BorderLayout.SOUTH);
    }
    
    private void connecter() {
        String nomJoueur = fieldNomJoueur.getText().trim();
        if (nomJoueur.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nom de joueur!");
            return;
        }
        
        if (client.connecter(nomJoueur)) {
            cardLayout.show(panelPrincipal, "JEU");
        } else {
            JOptionPane.showMessageDialog(this, "Erreur: Impossible de se connecter au serveur!");
        }
    }
    
    public void mettreAJourEtat(String data) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("État mis à jour: " + data);
        });
    }
    
    public void afficherCartes(String data) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Cartes: " + data);
        });
    }
    
    public void afficherScores(String data) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Scores: " + data);
        });
    }
    
    public void afficherMessage(String data) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("Message: " + data);
        });
    }
    
    public void afficherGagnant(String data) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, data);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrioClient client = new TrioClient("localhost", 5000);
            new TrioClientGUI(client);
        });
    }
}
