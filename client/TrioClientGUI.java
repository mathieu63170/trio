package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

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
    private JButton[] boutonsCartes;
    private Set<Integer> cartesSelectionnees;
    private JLabel labelMessage;
    private JLabel labelJoueur;
    
    public TrioClientGUI(TrioClient client) {
        this.client = client;
        this.cartesSelectionnees = new HashSet<>();
        client.setGUI(this);
        
        setTitle("Trio - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
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
        JPanel panelHaut = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        panelHaut.setBackground(new Color(200, 220, 200));
        labelJoueur = new JLabel("Joueur actuel: -");
        labelJoueur.setFont(new Font("Arial", Font.BOLD, 14));
        panelHaut.add(labelJoueur);
        panelJeu.add(panelHaut, BorderLayout.NORTH);
        
        // Panel central: cartes avec GridLayout
        JPanel panelCartes = new JPanel(new GridLayout(3, 4, 5, 5));
        panelCartes.setBackground(new Color(200, 220, 200));
        panelCartes.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        boutonsCartes = new JButton[12];
        for (int i = 0; i < 12; i++) {
            final int index = i;
            JButton btn = new JButton("Carte " + i);
            btn.setFont(new Font("Arial", Font.BOLD, 12));
            btn.setBackground(new Color(100, 150, 200));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.addActionListener(e -> selectionnerCarte(index));
            boutonsCartes[i] = btn;
            panelCartes.add(btn);
        }
        panelJeu.add(panelCartes, BorderLayout.CENTER);
        
        // Panel bas: boutons et messages
        JPanel panelBas = new JPanel(new BorderLayout());
        panelBas.setBackground(new Color(200, 220, 200));
        
        // Boutons d'action
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panelBoutons.setBackground(new Color(200, 220, 200));
        
        JButton btnVerifier = new JButton("Vérifier Trio");
        btnVerifier.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerifier.addActionListener(e -> verifierTrio());
        
        JButton btnAnnuler = new JButton("Annuler");
        btnAnnuler.setFont(new Font("Arial", Font.BOLD, 12));
        btnAnnuler.addActionListener(e -> annulerSelection());
        
        panelBoutons.add(btnVerifier);
        panelBoutons.add(btnAnnuler);
        panelBas.add(panelBoutons, BorderLayout.CENTER);
        
        // Message
        labelMessage = new JLabel("Sélectionnez 3 cartes pour vérifier un trio");
        labelMessage.setFont(new Font("Arial", Font.PLAIN, 12));
        labelMessage.setHorizontalAlignment(SwingConstants.CENTER);
        panelBas.add(labelMessage, BorderLayout.SOUTH);
        
        panelJeu.add(panelBas, BorderLayout.SOUTH);
    }
    
    private void selectionnerCarte(int index) {
        if (cartesSelectionnees.contains(index)) {
            cartesSelectionnees.remove(index);
            boutonsCartes[index].setBackground(new Color(100, 150, 200));
        } else if (cartesSelectionnees.size() < 3) {
            cartesSelectionnees.add(index);
            boutonsCartes[index].setBackground(new Color(255, 255, 0));
        } else {
            afficherMessage("Vous avez déjà sélectionné 3 cartes!");
            return;
        }
        
        afficherMessage("Cartes sélectionnées: " + cartesSelectionnees.size() + "/3");
        
        // Envoyer la sélection au serveur
        client.selectionnerCarte(index);
    }
    
    private void verifierTrio() {
        if (cartesSelectionnees.size() != 3) {
            afficherMessage("Vous devez sélectionner exactement 3 cartes!");
            return;
        }
        client.verifierTrio();
    }
    
    private void annulerSelection() {
        for (int index : cartesSelectionnees) {
            boutonsCartes[index].setBackground(new Color(100, 150, 200));
        }
        cartesSelectionnees.clear();
        afficherMessage("Sélection annulée");
        client.annulerSelection();
    }
    
    private void connecter() {
        String nomJoueur = fieldNomJoueur.getText().trim();
        if (nomJoueur.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer un nom de joueur!");
            return;
        }
        
        if (client.connecter(nomJoueur)) {
            cardLayout.show(panelPrincipal, "JEU");
            labelJoueur.setText("Joueur: " + nomJoueur);
        } else {
            JOptionPane.showMessageDialog(this, "Erreur: Impossible de se connecter au serveur!");
        }
    }
    
    public void afficherMessage(String data) {
        SwingUtilities.invokeLater(() -> {
            labelMessage.setText(data);
        });
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
    
    public void afficherGagnant(String data) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this, "Partie terminée: " + data);
        });
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TrioClient client = new TrioClient("localhost", 5000);
            new TrioClientGUI(client);
        });
    }
}
