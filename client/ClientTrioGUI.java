package client;

import commun.action.*;
import commun.plateau.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import javax.swing.*;

/**
 * ClientTrioGUI - Interface graphique complète pour le jeu Trio
 * Gère la connexion au serveur et l'affichage du jeu avec Swing
 */
public class ClientTrioGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Composants de connexion
    private JTextField hostField;
    private JTextField portField;
    private JButton connectButton;
    private JPanel connectionPanel;
    
    // Composants du jeu
    private JPanel gamePanel;
    private CartePanel[] cartesMilieuPanels;
    private JPanel mesCartesPanelUI;
    private JPanel autrJoueursPanelUI;
    private JLabel labelJoueurActuel;
    private JLabel labelPhaseActuelle;
    private JLabel labelScore;
    private JButton btnVerifierTrio;
    private JButton btnDemanderMax;
    private JButton btnDemanderMin;
    private JButton btnPrendreCartePers;
    private JButton btnAnnulerSelection;
    private JButton btnAfficherAutresJoueurs;
    private JComboBox<String> joueursCibleCombo;
    private JLabel labelSelection;
    private JTextArea textAreaInfo;
    
    // Client socket et communication
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private ClientReceiver clientReceiver;
    private Thread receiverThread;
    
    // Données du jeu
    private Plateau plateauActuel;
    private List<Integer> cartesSelectionnees;  // Indices des cartes du milieu sélectionnées
    private Set<Integer> cartesMostRevealedFromHand;  // Indices des cartes de la main révélées
    private Map<Integer, Carte> cartesReveleesAdversaires;  // ID adversaire -> Carte révélée
    private Map<Integer, Integer> proprietairesCartesRevele; // Indice de carte révélée -> ID du propriétaire
    private Joueur monJoueur;
    private List<Joueur> autresJoueurs;
    private int monID;
    private boolean jeuEnCours;
    private int cartePersSelectionnee = -1;
    private String modeAction = null;  // "trio", "milieu", "max", "min", "pers"
    
    public ClientTrioGUI() {
        super("Trio - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        setResizable(false);
        
        this.cartesSelectionnees = new ArrayList<>();
        this.cartesMostRevealedFromHand = new HashSet<>();
        this.cartesReveleesAdversaires = new HashMap<>();
        this.proprietairesCartesRevele = new HashMap<>();
        this.autresJoueurs = new ArrayList<>();
        this.jeuEnCours = false;
        
        initConnectionUI();
        setVisible(true);
    }
    
    /**
     * Définit le serveur (pour test automatisé)
     */
    public void setHost(String host) {
        hostField.setText(host);
    }
    
    /**
     * Définit le port (pour test automatisé)
     */
    public void setPort(int port) {
        portField.setText(String.valueOf(port));
    }
    
    /**
     * Interface de connexion initiale
     */
    private void initConnectionUI() {
        connectionPanel = new JPanel();
        connectionPanel.setLayout(new java.awt.GridBagLayout());
        connectionPanel.setBackground(new java.awt.Color(240, 240, 240));
        
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(10, 10, 10, 10);
        
        // Titre
        JLabel titleLabel = new JLabel("Connexion au Serveur Trio", SwingConstants.CENTER);
        titleLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 24));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        connectionPanel.add(titleLabel, gbc);
        
        // Host
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        connectionPanel.add(new JLabel("Serveur:"), gbc);
        
        hostField = new JTextField("localhost", 15);
        gbc.gridx = 1;
        connectionPanel.add(hostField, gbc);
        
        // Port
        gbc.gridx = 0;
        gbc.gridy = 2;
        connectionPanel.add(new JLabel("Port:"), gbc);
        
        portField = new JTextField("5000", 15);
        gbc.gridx = 1;
        connectionPanel.add(portField, gbc);
        
        // Bouton connexion
        connectButton = new JButton("Se Connecter");
        connectButton.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        connectionPanel.add(connectButton, gbc);
        
        connectButton.addActionListener(e -> connecterAuServeur());
        
        add(connectionPanel);
    }
    
    /**
     * Établit la connexion au serveur
     */
    private void connecterAuServeur() {
        String host = hostField.getText();
        int port = Integer.parseInt(portField.getText());
        
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            afficherMessage("✓ Connecté au serveur " + host + ":" + port);
            
            // Recevoir l'ID du client
            Object msgReception = in.readObject();
            if (msgReception instanceof String) {
                String msg = (String) msgReception;
                if (msg.startsWith("ID:")) {
                    monID = Integer.parseInt(msg.substring(3));
                    afficherMessage("✓ ID attribué: " + monID);
                }
            }
            
            // Démarrer le thread de réception
            clientReceiver = new ClientReceiver();
            receiverThread = new Thread(clientReceiver);
            receiverThread.setDaemon(true);
            receiverThread.start();
            
            // Passer à l'interface de jeu
            remove(connectionPanel);
            initGameUI();
            revalidate();
            repaint();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Erreur de connexion: " + e.getMessage(), 
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Initialise l'interface du jeu
     */
    private void initGameUI() {
        gamePanel = new JPanel();
        gamePanel.setLayout(new java.awt.BorderLayout(10, 10));
        gamePanel.setBackground(new java.awt.Color(34, 139, 34));
        
        // --- PANEL HAUT (Infos) ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 20, 10));
        topPanel.setBackground(new java.awt.Color(220, 220, 220));
        
        labelJoueurActuel = new JLabel("Joueur: -");
        labelJoueurActuel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        topPanel.add(labelJoueurActuel);
        
        labelPhaseActuelle = new JLabel("Phase: -");
        labelPhaseActuelle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        topPanel.add(labelPhaseActuelle);
        
        labelScore = new JLabel("Score: -");
        labelScore.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
        topPanel.add(labelScore);
        
        gamePanel.add(topPanel, java.awt.BorderLayout.NORTH);
        
        // --- PANEL CENTRE (Cartes du milieu) ---
        JPanel centrePanel = new JPanel();
        centrePanel.setLayout(new java.awt.GridLayout(3, 3, 15, 15));
        centrePanel.setBackground(new java.awt.Color(34, 139, 34));
        centrePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        cartesMilieuPanels = new CartePanel[9];
        for (int i = 0; i < 9; i++) {
            cartesMilieuPanels[i] = new CartePanel(i);
            final int index = i;
            cartesMilieuPanels[i].addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    selectionnerCarte(index);
                }
            });
            centrePanel.add(cartesMilieuPanels[i]);
        }
        
        gamePanel.add(centrePanel, java.awt.BorderLayout.CENTER);
        
        // --- PANEL BAS (Boutons et mes cartes) ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new java.awt.BorderLayout(10, 10));
        bottomPanel.setBackground(new java.awt.Color(200, 200, 200));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // === PANEL ACTIONS (TOP du bottom) ===
        JPanel actionsTopPanel = new JPanel();
        actionsTopPanel.setLayout(new java.awt.BorderLayout(5, 5));
        actionsTopPanel.setBackground(new java.awt.Color(200, 200, 200));
        
        // Label instructions
        labelSelection = new JLabel("Sélectionnez une action et cliquez sur une carte du milieu");
        labelSelection.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        labelSelection.setForeground(new java.awt.Color(0, 0, 150));
        actionsTopPanel.add(labelSelection, java.awt.BorderLayout.NORTH);
        
        // === Ligne 1: Actions trio et milieu ===
        JPanel actionRow1 = new JPanel();
        actionRow1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        actionRow1.setBackground(new java.awt.Color(200, 200, 200));
        
        btnVerifierTrio = new JButton("📋 Vérifier Trio (3)");
        btnVerifierTrio.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        btnVerifierTrio.setEnabled(false);
        btnVerifierTrio.addActionListener(e -> {
            // Comportement dynamique: si 3 cartes sélectionnées, envoyer le trio
            // Sinon, activer le mode trio
            int totalCartes = cartesSelectionnees.size() + cartesMostRevealedFromHand.size() + cartesReveleesAdversaires.size();
            if (totalCartes == 3 && modeAction != null && modeAction.equals("trio")) {
                verifierTrio();
            } else {
                activerModeAction("trio");
            }
        });
        actionRow1.add(btnVerifierTrio);
        
        btnAnnulerSelection = new JButton("❌ Annuler");
        btnAnnulerSelection.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        btnAnnulerSelection.addActionListener(e -> annulerSelection());
        actionRow1.add(btnAnnulerSelection);
        
        actionsTopPanel.add(actionRow1, java.awt.BorderLayout.CENTER);
        
        // === Ligne 2: Actions autres joueurs ===
        JPanel actionRow2 = new JPanel();
        actionRow2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        actionRow2.setBackground(new java.awt.Color(200, 200, 200));
        
        actionRow2.add(new JLabel("Demander à :"));
        
        joueursCibleCombo = new JComboBox<>();
        joueursCibleCombo.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 11));
        actionRow2.add(joueursCibleCombo);
        
        btnDemanderMax = new JButton("📈 + Grande");
        btnDemanderMax.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        btnDemanderMax.addActionListener(e -> demanderMax());
        actionRow2.add(btnDemanderMax);
        
        btnDemanderMin = new JButton("📉 + Petite");
        btnDemanderMin.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        btnDemanderMin.addActionListener(e -> demanderMin());
        actionRow2.add(btnDemanderMin);
        
        btnAfficherAutresJoueurs = new JButton("👁️ Voir Cartes");
        btnAfficherAutresJoueurs.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 11));
        btnAfficherAutresJoueurs.addActionListener(e -> demanderCartesAutresJoueurs());
        actionRow2.add(btnAfficherAutresJoueurs);
        
        actionsTopPanel.add(actionRow2, java.awt.BorderLayout.SOUTH);
        
        bottomPanel.add(actionsTopPanel, java.awt.BorderLayout.NORTH);
        
        // Mes cartes
        mesCartesPanelUI = new JPanel();
        mesCartesPanelUI.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 5));
        mesCartesPanelUI.setBackground(new java.awt.Color(200, 200, 200));
        mesCartesPanelUI.setBorder(BorderFactory.createTitledBorder("Mes Cartes"));
        
        // Autres joueurs
        autrJoueursPanelUI = new JPanel();
        autrJoueursPanelUI.setLayout(new BoxLayout(autrJoueursPanelUI, BoxLayout.Y_AXIS));
        autrJoueursPanelUI.setBackground(new java.awt.Color(200, 200, 200));
        autrJoueursPanelUI.setBorder(BorderFactory.createTitledBorder("Cartes des Autres Joueurs"));
        
        JScrollPane scrollOthers = new JScrollPane(autrJoueursPanelUI);
        scrollOthers.setPreferredSize(new java.awt.Dimension(0, 100));
        
        JPanel mesCartesEtAutres = new JPanel();
        mesCartesEtAutres.setLayout(new java.awt.BorderLayout());
        mesCartesEtAutres.setBackground(new java.awt.Color(200, 200, 200));
        mesCartesEtAutres.add(mesCartesPanelUI, java.awt.BorderLayout.NORTH);
        mesCartesEtAutres.add(scrollOthers, java.awt.BorderLayout.CENTER);
        
        bottomPanel.add(mesCartesEtAutres, java.awt.BorderLayout.CENTER);
        
        gamePanel.add(bottomPanel, java.awt.BorderLayout.SOUTH);
        
        // --- PANEL DROIT (Info et logs) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new java.awt.BorderLayout(5, 5));
        rightPanel.setBackground(new java.awt.Color(220, 220, 220));
        rightPanel.setPreferredSize(new java.awt.Dimension(250, 0));
        rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        textAreaInfo = new JTextArea();
        textAreaInfo.setEditable(false);
        textAreaInfo.setLineWrap(true);
        textAreaInfo.setWrapStyleWord(true);
        textAreaInfo.setFont(new java.awt.Font("Courier", java.awt.Font.PLAIN, 10));
        JScrollPane scrollPane = new JScrollPane(textAreaInfo);
        rightPanel.add(new JLabel("Informations"), java.awt.BorderLayout.NORTH);
        rightPanel.add(scrollPane, java.awt.BorderLayout.CENTER);
        
        gamePanel.add(rightPanel, java.awt.BorderLayout.EAST);
        
        add(gamePanel);
    }
    
    /**
     * Sélectionne/désélectionne une carte du milieu
     */
    private void selectionnerCarte(int index) {
        if (plateauActuel == null || index >= plateauActuel.getMillieu().size()) {
            return;
        }
        
        // Si aucun mode action activé, prendre la carte du milieu
        if (modeAction == null || modeAction.isEmpty()) {
            try {
                // Envoyer l'action pour prendre la carte du milieu
                commun.action.Action action = new ActionMillieu(monID, index);
                out.writeObject(action);
                out.flush();
                afficherMessage("📤 Carte du milieu (index: " + index + ") prise");
            } catch (IOException e) {
                afficherMessage("❌ Erreur lors de la prise de carte: " + e.getMessage());
            }
            return;
        }
        
        if (modeAction.equals("trio")) {
            // Mode trio: sélectionner jusqu'à 3 cartes
            if (cartesSelectionnees.contains(index)) {
                cartesSelectionnees.remove((Integer) index);
            } else {
                if (cartesSelectionnees.size() < 3) {
                    cartesSelectionnees.add(index);
                }
            }
            updateCartesPanels();
            updateButtonsState();
            
        }
    }
    
    /**
     * Active un mode d'action
     */
    private void activerModeAction(String mode) {
        modeAction = mode;
        cartesSelectionnees.clear();
        
        if (mode.equals("trio")) {
            labelSelection.setText("📋 Mode TRIO : Sélectionnez 3 cartes du milieu et cliquez sur une");
            labelSelection.setForeground(new java.awt.Color(0, 0, 200));
        } else if (mode.equals("milieu")) {
            labelSelection.setText("🎯 Mode MILIEU : Cliquez sur la carte que vous voulez prendre");
            labelSelection.setForeground(new java.awt.Color(200, 100, 0));
        }
        
        updateCartesPanels();
    }
    
    /**
     * Annule la sélection actuelle
     */
    private void annulerSelection() {
        cartesSelectionnees.clear();
        modeAction = null;
        cartePersSelectionnee = -1;
        labelSelection.setText("Sélectionnez une action et cliquez sur une carte du milieu");
        labelSelection.setForeground(new java.awt.Color(0, 0, 150));
        updateCartesPanels();
        updateButtonsState();
    }
    
    /**
     * Prend une carte du milieu
     */
    private void prendreCarteMillieu(int index) {
        try {
            commun.action.Action action = new ActionMillieu(monID, index);
            out.writeObject(action);
            out.flush();
            afficherMessage("📤 Prise de carte milieu envoyée (index: " + index + ")");
        } catch (IOException e) {
            afficherMessage("❌ Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Vérifie le trio sélectionné
     */
    private void verifierTrio() {
        // Sécurité: réinitialiser les sélections invalides
        cartesSelectionnees.removeIf(idx -> idx >= plateauActuel.getMillieu().size());
        
        int totalCartes = cartesSelectionnees.size() + cartesMostRevealedFromHand.size() + cartesReveleesAdversaires.size();
        if (totalCartes != 3) {
            JOptionPane.showMessageDialog(this, "Sélectionnez/révélez exactement 3 cartes (" + totalCartes + " sélectionnées)");
            return;
        }
        
        try {
            List<Integer> idsCartes = new ArrayList<>();
            List<Integer> proprietaires = new ArrayList<>();
            List<Carte> cartesDebug = new ArrayList<>();  // Pour affichage seulement
            
            // 1. Ajouter les IDs des cartes du milieu sélectionnées (propriétaire = -1 pour milieu)
            for (Integer idx : cartesSelectionnees) {
                if (idx >= 0 && idx < plateauActuel.getMillieu().size()) {
                    Carte carte = plateauActuel.getMillieu().get(idx);
                    idsCartes.add(carte.getId());
                    proprietaires.add(-1);  // -1 = carte du milieu
                    cartesDebug.add(carte);
                } else {
                    afficherMessage("⚠️  Index de carte invalide: " + idx + " (milieu: " + plateauActuel.getMillieu().size() + ")");
                }
            }
            
            // 2. Ajouter les IDs des cartes de la main révélées (propriétaire = monID)
            if (monJoueur != null && monJoueur.getDeck() != null) {
                int handIdx = 0;
                for (Carte carte : monJoueur.getDeck()) {
                    if (cartesMostRevealedFromHand.contains(handIdx)) {
                        idsCartes.add(carte.getId());
                        proprietaires.add(monID);  // Propriétaire = moi
                        cartesDebug.add(carte);
                    }
                    handIdx++;
                }
            }
            
            // 3. Ajouter les IDs des cartes révélées des adversaires (propriétaire = l'ID de l'adversaire)
            for (Map.Entry<Integer, Carte> entry : cartesReveleesAdversaires.entrySet()) {
                int indexRevel = entry.getKey();
                Carte carteRevel = entry.getValue();
                idsCartes.add(carteRevel.getId());
                int idProprietaire = proprietairesCartesRevele.get(indexRevel);
                proprietaires.add(idProprietaire);  // Propriétaire = l'adversaire d'origine
                cartesDebug.add(carteRevel);
            }
            
            if (idsCartes.size() != 3) {
                afficherMessage("❌ Sélectionnez exactement 3 cartes (seulement " + idsCartes.size() + " sélectionnées)");
                return;
            }
            
            // ✅ VIDER LES SÉLECTIONS IMMÉDIATEMENT APRÈS LES AVOIR COPIÉES
            // Ceci AVANT l'envoi, pour éviter une race condition
            cartesSelectionnees.clear();
            cartesMostRevealedFromHand.clear();
            cartesReveleesAdversaires.clear();
            proprietairesCartesRevele.clear();
            modeAction = null;
            cartePersSelectionnee = -1;
            
            // Créer l'action AVEC les IDs (pas les cartes!)
            ActionTrio action = new ActionTrio(monID, idsCartes, proprietaires);
            
            // Afficher les détails des cartes envoyées (pour debug)
            afficherMessage("📋 === TRIO À ENVOYER ===");
            for (int i = 0; i < cartesDebug.size(); i++) {
                Carte carte = cartesDebug.get(i);
                int prop = proprietaires.get(i);
                String source = (prop == -1) ? "MILIEU" : (prop == monID) ? "MA MAIN" : "Joueur " + prop;
                afficherMessage("   Carte " + (i + 1) + ": Valeur=" + carte.getValeur() + " | ID=" + carte.getId() + " | Source=" + source);
            }
            afficherMessage("📋 =====================");
            
            // Envoyer au serveur (les sélections sont déjà vides!)
            out.writeObject(action);
            out.flush();
            
            afficherMessage("📤 Trio envoyé au serveur: " + idsCartes.size() + " cartes");
            
            // Mettre à jour l'UI
            updateCartesPanels();
            updateButtonsState();
            
        } catch (IOException e) {
            afficherMessage("❌ Erreur lors de l'envoi: " + e.getMessage());
        }
    }
    
    /**
     * Demande la plus grande carte d'un adversaire
     */
    private void demanderMax() {
        if (joueursCibleCombo.getSelectedIndex() < 0) {
            afficherMessage("⚠️ Sélectionnez un adversaire");
            return;
        }
        
        String selected = (String) joueursCibleCombo.getSelectedItem();
        int idCible = Integer.parseInt(selected.replaceAll("\\D", ""));
        
        if (idCible == monID) {
            afficherMessage("⚠️ Vous ne pouvez pas demander votre propre carte!");
            return;
        }
        
        try {
            commun.action.Action action = new ActionMax(monID, idCible);
            out.writeObject(action);
            out.flush();
            afficherMessage("📤 Demande de + grande carte au joueur " + idCible);
        } catch (IOException e) {
            afficherMessage("❌ Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Demande la plus petite carte d'un adversaire
     */
    private void demanderMin() {
        if (joueursCibleCombo.getSelectedIndex() < 0) {
            afficherMessage("⚠️ Sélectionnez un adversaire");
            return;
        }
        
        String selected = (String) joueursCibleCombo.getSelectedItem();
        int idCible = Integer.parseInt(selected.replaceAll("\\D", ""));
        
        if (idCible == monID) {
            afficherMessage("⚠️ Vous ne pouvez pas demander votre propre carte!");
            return;
        }
        
        try {
            commun.action.Action action = new ActionMin(monID, idCible);
            out.writeObject(action);
            out.flush();
            afficherMessage("📤 Demande de + petite carte au joueur " + idCible);
        } catch (IOException e) {
            afficherMessage("❌ Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Demande les cartes des autres joueurs
     */
    private void demanderCartesAutresJoueurs() {
        try {
            out.writeObject("GET_OTHER_PLAYERS_CARDS");
            out.flush();
            afficherMessage("📤 Demande des cartes des autres joueurs envoyée");
        } catch (IOException e) {
            afficherMessage("❌ Erreur: " + e.getMessage());
        }
    }
    
    /**
     * Met à jour l'affichage des cartes du milieu
     */
    private void updateCartesPanels() {
        if (plateauActuel == null) return;
        
        List<Carte> cartes = plateauActuel.getMillieu();
        for (int i = 0; i < cartesMilieuPanels.length; i++) {
            if (i < cartes.size()) {
                boolean selected = cartesSelectionnees.contains(i);
                cartesMilieuPanels[i].setCarte(cartes.get(i), selected);
            } else {
                cartesMilieuPanels[i].setCarte(null, false);
            }
        }
        
        // Afficher mes cartes (de la main) - CLIQUABLES ET REVEALABLES
        if (monJoueur != null) {
            mesCartesPanelUI.removeAll();
            int handIdx = 0;
            for (Carte carte : monJoueur.getDeck()) {
                boolean isRevealed = cartesMostRevealedFromHand.contains(handIdx);
                
                JButton btnCarte = new JButton(carte.getValeur() + "");
                btnCarte.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
                btnCarte.setPreferredSize(new java.awt.Dimension(50, 50));
                
                // Style selon état (révélée ou cachée)
                if (isRevealed) {
                    btnCarte.setBackground(new java.awt.Color(100, 200, 100));  // Vert
                    btnCarte.setForeground(java.awt.Color.WHITE);
                } else {
                    btnCarte.setBackground(java.awt.Color.LIGHT_GRAY);
                    btnCarte.setForeground(java.awt.Color.BLACK);
                }
                btnCarte.setFocusPainted(false);
                
                final int idx = handIdx;
                btnCarte.addActionListener(e -> {
                    // Basculer l'état révélé/caché
                    if (cartesMostRevealedFromHand.contains(idx)) {
                        cartesMostRevealedFromHand.remove(idx);
                    } else {
                        // Max 3 cartes révélées pour le trio
                        if (cartesMostRevealedFromHand.size() < 3) {
                            cartesMostRevealedFromHand.add(idx);
                        } else {
                            afficherMessage("⚠️  Max 3 cartes peuvent être révélées");
                        }
                    }
                    updateCartesPanels();
                    updateButtonsState();
                });
                
                mesCartesPanelUI.add(btnCarte);
                handIdx++;
            }
            mesCartesPanelUI.revalidate();
            mesCartesPanelUI.repaint();
        }
        
        // Afficher cartes des autres joueurs
        updateOtherPlayersDisplay();
    }
    
    /**
     * Met à jour l'affichage des cartes des autres joueurs
     */
    private void updateOtherPlayersDisplay() {
        autrJoueursPanelUI.removeAll();
        
        // Aussi mettre à jour le combo box des cibles
        joueursCibleCombo.removeAllItems();
        
        if (autresJoueurs.isEmpty()) {
            JLabel label = new JLabel("(Cliquez sur 'Voir Cartes' pour afficher)");
            label.setFont(new java.awt.Font("Arial", java.awt.Font.ITALIC, 11));
            autrJoueursPanelUI.add(label);
        } else {
            for (Joueur joueur : autresJoueurs) {
                if (joueur.getId() != monID) {  // Pas afficher nos cartes deux fois
                    // Ajouter au combo box
                    joueursCibleCombo.addItem("Joueur " + joueur.getId());
                    
                    JPanel playerPanel = new JPanel();
                    playerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));
                    playerPanel.setBackground(new java.awt.Color(230, 230, 230));
                    playerPanel.setBorder(BorderFactory.createTitledBorder(joueur.getNom() + " (" + joueur.getDeck().size() + " cartes)"));
                    
                    // CORRECTION: Afficher juste le nombre de cartes, pas les valeurs!
                    JLabel countLabel = new JLabel(joueur.getDeck().size() + " carte(s) cachées");
                    countLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
                    countLabel.setOpaque(true);
                    countLabel.setBackground(new java.awt.Color(180, 180, 180));
                    countLabel.setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK));
                    countLabel.setPreferredSize(new java.awt.Dimension(150, 30));
                    countLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    playerPanel.add(countLabel);
                    
                    autrJoueursPanelUI.add(playerPanel);
                }
            }
        }
        
        autrJoueursPanelUI.revalidate();
        autrJoueursPanelUI.repaint();
        updateButtonsState();
    }
    
    /**
     * Met à jour l'état des boutons
     */
    private void updateButtonsState() {
        // Bouton trio: actif si 3 cartes TOTALES (milieu + main + adversaires) sélectionnées ET jeu en cours
        int totalCartes = cartesSelectionnees.size() + cartesMostRevealedFromHand.size() + cartesReveleesAdversaires.size();
        btnVerifierTrio.setEnabled(totalCartes == 3 && jeuEnCours);
        
        // Afficher le mode actuel
        if (modeAction != null && !modeAction.isEmpty()) {
            if (modeAction.equals("trio")) {
                btnVerifierTrio.setText("📋 Vérifier Trio (" + totalCartes + "/3)");
            } else {
                btnVerifierTrio.setText("📋 Vérifier Trio");
            }
        } else {
            btnVerifierTrio.setText("📋 Vérifier Trio (" + totalCartes + "/3)");
        }
        
        // Boutons max/min actifs si jeu en cours
        btnDemanderMax.setEnabled(jeuEnCours && joueursCibleCombo.getItemCount() > 0);
        btnDemanderMin.setEnabled(jeuEnCours && joueursCibleCombo.getItemCount() > 0);
    }
    
    /**
     * Affiche un message dans la zone info
     */
    private void afficherMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (textAreaInfo != null) {
                textAreaInfo.append(message + "\n");
                textAreaInfo.setCaretPosition(textAreaInfo.getDocument().getLength());
            }
        });
    }
    
    /**
     * Affiche l'écran de fin de partie avec le gagnant
     */
    private void afficherEcranVictoire(int gannantID, String gagnantNom) {
        SwingUtilities.invokeLater(() -> {
            // Créer un panneau pour afficher le gagnant
            JPanel victoryPanel = new JPanel();
            victoryPanel.setLayout(new BoxLayout(victoryPanel, BoxLayout.Y_AXIS));
            victoryPanel.setBackground(new java.awt.Color(30, 30, 30));
            
            // Message principal
            if (gannantID == monID) {
                JLabel victoryLabel = new JLabel("🎉 VICTOIRE! 🎉");
                victoryLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 60));
                victoryLabel.setForeground(new java.awt.Color(0, 255, 0));
                victoryLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                victoryPanel.add(Box.createVerticalStrut(100));
                victoryPanel.add(victoryLabel);
                
                JLabel messageLabel = new JLabel("Vous avez gagné avec 3 trios!");
                messageLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 40));
                messageLabel.setForeground(java.awt.Color.WHITE);
                messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                victoryPanel.add(Box.createVerticalStrut(30));
                victoryPanel.add(messageLabel);
            } else {
                JLabel defeatLabel = new JLabel("DÉFAITE");
                defeatLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 60));
                defeatLabel.setForeground(new java.awt.Color(255, 100, 100));
                defeatLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                victoryPanel.add(Box.createVerticalStrut(80));
                victoryPanel.add(defeatLabel);
                
                JLabel winnerLabel = new JLabel(gagnantNom);
                winnerLabel.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 50));
                winnerLabel.setForeground(new java.awt.Color(255, 215, 0));
                winnerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                victoryPanel.add(Box.createVerticalStrut(20));
                victoryPanel.add(winnerLabel);
                
                JLabel hasWonLabel = new JLabel("a gagné avec 3 trios!");
                hasWonLabel.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 40));
                hasWonLabel.setForeground(java.awt.Color.WHITE);
                hasWonLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
                victoryPanel.add(Box.createVerticalStrut(20));
                victoryPanel.add(hasWonLabel);
            }
            
            victoryPanel.add(Box.createVerticalGlue());
            
            // Remplacer le contenu de gamePanel
            gamePanel.removeAll();
            gamePanel.add(victoryPanel, java.awt.BorderLayout.CENTER);
            gamePanel.revalidate();
            gamePanel.repaint();
            
            afficherMessage("✅ Écran de victoire affiché!");
        });
    }
    
    /**
     * Thread de réception des messages du serveur
     */
    private class ClientReceiver implements Runnable {
        @Override
        public void run() {
            System.out.println("[DEBUG] ClientReceiver COMMENCÉ");
            try {
                System.out.println("[DEBUG] Entrée dans la boucle de réception");
                while (true) {
                    System.out.println("[DEBUG] Attente de readObject()...");
                    Object obj = in.readObject();
                    System.out.println("[DEBUG] REÇU: " + obj.getClass().getSimpleName());
                    afficherMessage("📨 REÇU: " + obj.getClass().getSimpleName());
                    
                    if (obj instanceof Plateau) {
                        plateauActuel = (Plateau) obj;
                        jeuEnCours = plateauActuel.getPhaseActuelle() != Phase.FIN_PARTIE;
                        
                        afficherMessage("📋 PLATEAU REÇU - Phase: " + plateauActuel.getPhaseActuelle());
                        
                        // ✅ VÉRIFIER LA VICTOIRE
                        if (plateauActuel.getPhaseActuelle() == Phase.FIN_PARTIE) {
                            afficherMessage("🎯 PHASE FIN_PARTIE DÉTECTÉE!");
                            int gagnantID = plateauActuel.getGagnant();
                            afficherMessage("🎯 Gagnant ID: " + gagnantID + ", MonID: " + monID);
                            String nomGagnant = "Joueur " + gagnantID;
                            
                            // Chercher le nom du gagnant
                            if (plateauActuel.getJoueurs() != null) {
                                for (Joueur j : plateauActuel.getJoueurs()) {
                                    if (j.getId() == gagnantID) {
                                        nomGagnant = j.getNom();
                                        break;
                                    }
                                }
                            }
                            
                            final String gagnantNom = nomGagnant;
                            final int gagnantID_final = gagnantID;
                            afficherMessage("🎯 Affichage de l'écran de fin...");
                            SwingUtilities.invokeLater(() -> {
                                afficherMessage("🎯 SwingUtilities exécuté!");
                                afficherEcranVictoire(gagnantID_final, gagnantNom);
                            });
                        }
                        
                        // ✅ METTRE À JOUR monJoueur AVEC LA COPIE DU PLATEAU
                        // Ceci garantit que monJoueur reflète l'état actuel du serveur
                        afficherMessage("🔄 PLATEAU REÇU - cherche joueur " + monID + " parmi " + (plateauActuel.getJoueurs() != null ? plateauActuel.getJoueurs().size() : "null") + " joueurs");
                        
                        if (plateauActuel.getJoueurs() != null) {
                            for (Joueur j : plateauActuel.getJoueurs()) {
                                afficherMessage("   → Joueur ID: " + j.getId() + ", Nom: " + j.getNom() + ", Trios: " + (j.getTrios() != null ? j.getTrios().size() : "null"));
                                if (j.getId() == monID) {
                                    monJoueur = j;  // Remplacer par la copie du serveur (à jour!)
                                    afficherMessage("   ✅ TROUVÉ! MonJoueur mis à jour");
                                    break;
                                }
                            }
                        }
                        
                        SwingUtilities.invokeLater(() -> {
                            // Réinitialiser les sélections locales quand on reçoit un nouveau plateau
                            // Ceci évite que les anciennes sélections soient renvoyées lors du prochain trio
                            cartesSelectionnees.clear();
                            cartesMostRevealedFromHand.clear();
                            cartesReveleesAdversaires.clear();
                            proprietairesCartesRevele.clear();
                            modeAction = null;
                            cartePersSelectionnee = -1;
                            
                            updateCartesPanels();
                            updateButtonsState();
                            updateInfoLabels();
                        });
                        
                    } else if (obj instanceof Joueur) {
                        monJoueur = (Joueur) obj;
                        afficherMessage("✓ Données joueur reçues");
                        
                    } else if (obj instanceof ActionRevealCarte) {
                        // Carte révélée par demande MAX/MIN
                        ActionRevealCarte reveal = (ActionRevealCarte) obj;
                        int idSource = reveal.getIdJoueurSource();
                        Carte carteRevele = reveal.getCarte();
                        
                        // Enregistrer la carte révélée avec son propriétaire
                        int indexCarte = cartesReveleesAdversaires.size();
                        cartesReveleesAdversaires.put(indexCarte, carteRevele);
                        proprietairesCartesRevele.put(indexCarte, idSource);
                        
                        afficherMessage("📸 Carte révélée de Joueur " + idSource + 
                                       " (" + reveal.getType() + "): " + carteRevele.getValeur());
                        afficherMessage("   → Vous pouvez utiliser cette carte dans un trio");
                        

                    } else if (obj instanceof java.util.ArrayList) {
                        // Reçu liste de joueurs des autres
                        @SuppressWarnings("unchecked")
                        List<Joueur> joueurs = (List<Joueur>) obj;
                        autresJoueurs = joueurs;
                        afficherMessage("✓ Cartes de " + joueurs.size() + " autres joueur(s) reçues");
                        
                        SwingUtilities.invokeLater(() -> {
                            updateOtherPlayersDisplay();
                        });
                        
                    } else if (obj instanceof String) {
                        String msg = (String) obj;
                        afficherMessage("📨 " + msg);
                        
                    } else {
                        afficherMessage("? Message reçu: " + obj.getClass().getSimpleName());
                    }
                }
            } catch (EOFException e) {
                afficherMessage("❌ Serveur fermé");
            } catch (IOException | ClassNotFoundException e) {
                afficherMessage("❌ Erreur: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                afficherMessage("❌ ERREUR INCONNUE: " + e.getClass().getSimpleName() + " - " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Met à jour les labels d'information
     */
    private void updateInfoLabels() {
        if (plateauActuel == null) return;
        
        labelPhaseActuelle.setText("Phase: " + plateauActuel.getPhaseActuelle());
        
        if (monJoueur != null) {
            labelJoueurActuel.setText("Joueur: " + monJoueur.getNom());
            int trios = monJoueur.getTrios().size();
            afficherMessage("📊 UPDATE LABELS: Joueur " + monJoueur.getId() + " a " + trios + " trio(s)");
            labelScore.setText("Trios: " + trios);
        }
    }
    
    /**
     * Classe interne pour afficher une carte du milieu
     */
    private class CartePanel extends JPanel {
        private Carte carte;
        private boolean selected;
        private int index;
        
        public CartePanel(int index) {
            this.index = index;
            this.carte = null;
            this.selected = false;
            setPreferredSize(new java.awt.Dimension(100, 100));
            setBorder(BorderFactory.createLineBorder(java.awt.Color.BLACK, 2));
            setBackground(java.awt.Color.WHITE);
        }
        
        public void setCarte(Carte carte, boolean selected) {
            this.carte = carte;
            this.selected = selected;
            repaint();
        }
        
        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            
            if (carte == null) {
                return;
            }
            
            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
            
            // Couleur de fond selon sélection
            if (selected) {
                setBackground(new java.awt.Color(200, 255, 200));  // Vert clair
            } else {
                setBackground(java.awt.Color.WHITE);
            }
            super.paintComponent(g2d);
            
            // Afficher la valeur de la carte
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 28));
            g2d.setColor(java.awt.Color.BLACK);
            String texte = String.valueOf(carte.getValeur());
            java.awt.FontMetrics fm = g2d.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(texte)) / 2;
            int y = ((getHeight() - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(texte, x, y);
            
            // Afficher la couleur
            String couleurTexte = carte.getCouleur().toString().substring(0, 1);
            java.awt.Color couleur = switch(carte.getCouleur()) {
                case ROUGE -> java.awt.Color.RED;
                case VERT -> new java.awt.Color(0, 128, 0);
                case VIOLET -> new java.awt.Color(128, 0, 128);
            };
            g2d.setColor(couleur);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 12));
            g2d.drawString(couleurTexte, 10, 20);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientTrioGUI());
    }
}
