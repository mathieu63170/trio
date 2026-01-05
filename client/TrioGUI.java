package client;

import commun.action.*;
import commun.plateau.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

/**
 * TrioGUI - Interface client modernisée pour le jeu Trio
 * Affiche le plateau de jeu avec le milieu, la main du joueur, et les autres joueurs
 */
public class TrioGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Connexion
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int monID = -1;
    
    // Données du jeu
    private Plateau plateauActuel;
    private Joueur monJoueur;
    private List<Joueur> autresJoueurs = new ArrayList<>();
    
    // Sélection
    private List<Carte> cartesSel = new ArrayList<>();  // Cartes sélectionnées pour le trio
    private List<Integer> proprietairesSel = new ArrayList<>();  // Propriétaires des cartes sélectionnées
    private List<Integer> indicesMilieuSel = new ArrayList<>();  // Indices du milieu pour les cartes sélectionnées (ou -1 si pas du milieu)
    private int etapeActuelle = 0;  // 0-3 pour les 4 étapes du tour
    private Set<Integer> cartesReveleesDuMilieu = new HashSet<>();  // Indices des cartes du milieu révélées pendant ce tour
    private List<Integer> cartesReveleesIDs = new ArrayList<>();  // IDs des cartes révélées du tour précédent
    private int joueurActuelPrecedent = -1;  // Pour détecter les changements de tour
    
    // Composants UI
    private JPanel panelMilieu;
    private JPanel panelCartesRevelees;  // Nouveau: panel séparé pour les cartes révélées
    private JPanel panelMainJoueur;
    private JPanel panelAutresJoueurs;
    private JLabel labelInfo;
    private JButton btnVerifierTrio;
    private JTextArea textLog;

    public TrioGUI() {
        super("Trio - Jeu de Cartes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setResizable(true);
        
        initConnectionUI();
        setVisible(true);
    }

    /**
     * Interface initiale de connexion
     */
    private void initConnectionUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Titre
        JLabel titre = new JLabel("Connexion au Serveur Trio");
        titre.setFont(new Font("Arial", Font.BOLD, 28));
        titre.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titre, gbc);
        
        // Host
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lHost = new JLabel("Serveur:");
        lHost.setForeground(Color.WHITE);
        panel.add(lHost, gbc);
        
        JTextField tfHost = new JTextField("localhost", 20);
        gbc.gridx = 1;
        panel.add(tfHost, gbc);
        
        // Port
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lPort = new JLabel("Port:");
        lPort.setForeground(Color.WHITE);
        panel.add(lPort, gbc);
        
        JTextField tfPort = new JTextField("5000", 20);
        gbc.gridx = 1;
        panel.add(tfPort, gbc);
        
        // Bouton connexion
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnConnect = new JButton("Se Connecter");
        btnConnect.setFont(new Font("Arial", Font.BOLD, 16));
        btnConnect.setBackground(new Color(100, 200, 100));
        btnConnect.setForeground(Color.WHITE);
        btnConnect.addActionListener(e -> {
            String host = tfHost.getText();
            int port = Integer.parseInt(tfPort.getText());
            connecterAuServeur(host, port);
        });
        panel.add(btnConnect, gbc);
        
        setContentPane(panel);
    }

    /**
     * Se connecte au serveur
     */
    private void connecterAuServeur(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            // Recevoir l'ID
            Object obj = in.readObject();
            if (obj instanceof String) {
                String msg = (String) obj;
                if (msg.startsWith("ID:")) {
                    monID = Integer.parseInt(msg.substring(3));
                    System.out.println("✓ Connecté avec l'ID: " + monID);
                    initGameUI();
                    startReceiver();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion: " + e.getMessage());
        }
    }

    /**
     * Initialise l'interface de jeu
     */
    private void initGameUI() {
        setContentPane(createGamePanel());
        revalidate();
        repaint();
    }

    /**
     * Crée le panel principal du jeu
     */
    private JPanel createGamePanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(60, 60, 70));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // --- HAUT: Info du jeu ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topPanel.setBackground(new Color(80, 80, 90));
        
        labelInfo = new JLabel("Joueur " + monID + " | En attente...");
        labelInfo.setForeground(Color.WHITE);
        labelInfo.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(labelInfo);
        
        main.add(topPanel, BorderLayout.NORTH);
        
        // --- CENTRE: Milieu et Main du joueur ---
        JPanel centrePanel = new JPanel(new BorderLayout(10, 10));
        centrePanel.setBackground(new Color(60, 60, 70));
        
        // Milieu (haut)
        JPanel panelMilieuContainer = new JPanel(new BorderLayout());
        panelMilieuContainer.setBackground(new Color(40, 100, 40));
        panelMilieuContainer.setBorder(new TitledBorder("MILIEU"));
        panelMilieu = new JPanel(new GridLayout(3, 3, 8, 8));
        panelMilieu.setBackground(new Color(40, 100, 40));
        panelMilieu.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelMilieuContainer.add(panelMilieu, BorderLayout.CENTER);
        centrePanel.add(panelMilieuContainer, BorderLayout.NORTH);
        
        // Cartes révélées (milieu-haut)
        JPanel panelCartesReveleesContainer = new JPanel(new BorderLayout());
        panelCartesReveleesContainer.setBackground(new Color(100, 140, 50));
        panelCartesReveleesContainer.setBorder(new TitledBorder("CARTES RÉVÉLÉES (MAX/MIN)"));
        panelCartesRevelees = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelCartesRevelees.setBackground(new Color(100, 140, 50));
        panelCartesRevelees.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelCartesReveleesContainer.add(panelCartesRevelees, BorderLayout.CENTER);
        
        // Ajouter les deux panels au centre
        JPanel topCentrePanel = new JPanel(new BorderLayout(10, 10));
        topCentrePanel.setBackground(new Color(60, 60, 70));
        topCentrePanel.add(panelMilieuContainer, BorderLayout.NORTH);
        topCentrePanel.add(panelCartesReveleesContainer, BorderLayout.CENTER);
        centrePanel.add(topCentrePanel, BorderLayout.NORTH);
        
        // Main du joueur (bas)
        JPanel panelMainContainer = new JPanel(new BorderLayout());
        panelMainContainer.setBackground(new Color(50, 50, 100));
        panelMainContainer.setBorder(new TitledBorder("Ma Main"));
        panelMainJoueur = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelMainJoueur.setBackground(new Color(50, 50, 100));
        panelMainJoueur.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelMainContainer.add(new JScrollPane(panelMainJoueur), BorderLayout.CENTER);
        centrePanel.add(panelMainContainer, BorderLayout.CENTER);
        
        main.add(centrePanel, BorderLayout.CENTER);
        
        // --- BAS: Boutons et logs ---
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(60, 60, 70));
        
        // Boutons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(new Color(60, 60, 70));
        
        btnVerifierTrio = new JButton("✓ Vérifier Trio (0/3)");
        btnVerifierTrio.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerifierTrio.setEnabled(false);
        btnVerifierTrio.addActionListener(e -> verifierTrio());
        btnPanel.add(btnVerifierTrio);
        
        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        
        // Logs
        textLog = new JTextArea(4, 50);
        textLog.setEditable(false);
        textLog.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textLog.setBackground(new Color(30, 30, 40));
        textLog.setForeground(new Color(150, 255, 150));
        JScrollPane scrollLog = new JScrollPane(textLog);
        bottomPanel.add(scrollLog, BorderLayout.CENTER);
        
        main.add(bottomPanel, BorderLayout.SOUTH);
        
        // --- DROITE: Autres joueurs ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(60, 60, 70));
        rightPanel.setBorder(new TitledBorder("Autres Joueurs"));
        panelAutresJoueurs = new JPanel();
        panelAutresJoueurs.setLayout(new BoxLayout(panelAutresJoueurs, BoxLayout.Y_AXIS));
        panelAutresJoueurs.setBackground(new Color(60, 60, 70));
        rightPanel.add(new JScrollPane(panelAutresJoueurs), BorderLayout.CENTER);
        main.add(rightPanel, BorderLayout.EAST);
        
        return main;
    }

    /**
     * Lance le thread de réception des messages du serveur
     */
    private void startReceiver() {
        new Thread(() -> {
            try {
                while (true) {
                    Object obj = in.readObject();
                    if (obj instanceof Plateau) {
                        plateauActuel = (Plateau) obj;
                        SwingUtilities.invokeLater(this::afficherPlateau);
                    }
                }
            } catch (EOFException e) {
                afficherLog("Connexion fermée");
            } catch (Exception e) {
                afficherLog("Erreur réception: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Affiche le plateau à jour
     */
    private void afficherPlateau() {
        if (plateauActuel == null) return;
        
        // ✅ VÉRIFIER LA VICTOIRE D'ABORD!
        if (plateauActuel.getPhaseActuelle() == Phase.FIN_PARTIE) {
            afficherEcranVictoire();
            return;
        }
        
        // Actualiser monJoueur IMMÉDIATEMENT avec les données du plateau reçu
        monJoueur = plateauActuel.getJoueurs().stream()
            .filter(j -> j.getId() == monID)
            .findFirst()
            .orElse(null);
        
        // VÉRIFIER que les cartes sélectionnées existent toujours AVANT tout autre traitement
        // (important après un TRIO valide où les cartes sont supprimées)
        verifierCartesSelectionneesValides();
        
        // Détecter changement de joueur actuel (fin de tour) et réinitialiser la sélection locale
        if (joueurActuelPrecedent != -1 && joueurActuelPrecedent != plateauActuel.getJoueurActuel()) {
            cartesReveleesDuMilieu.clear();  // Réinitialiser les cartes du milieu révélées localement
            cartesReveleesIDs.clear();  // Réinitialiser les IDs des cartes révélées
            cartesSel.clear();  // Réinitialiser les cartes sélectionnées
            proprietairesSel.clear();  // Réinitialiser les propriétaires
            indicesMilieuSel.clear();  // Réinitialiser les indices du milieu
            // NOTE: On ne réinitialise PAS plateauActuel.getCartesRevelees() - elles restent révélées!
            afficherLog("🔄 Changement de tour");
        }
        joueurActuelPrecedent = plateauActuel.getJoueurActuel();
        
        // Si les cartes révélées sont vides (après un TRIO valide ou au changement de tour)
        // réinitialiser aussi cartesReveleesIDs pour tracker les NOUVELLES cartes révélées
        if (plateauActuel.getCartesRevelees().isEmpty()) {
            cartesReveleesIDs.clear();
            
            // Aussi réinitialiser la sélection si elle contenait des cartes révélées
            if (!cartesSel.isEmpty()) {
                // Chercher si des cartes sélectionnées sont des cartes révélées d'autres joueurs
                boolean hasRevealedCardsSelected = false;
                for (int i = 0; i < cartesSel.size(); i++) {
                    int proprietaire = proprietairesSel.get(i);
                    if (proprietaire != monID && proprietaire > 0) {
                        hasRevealedCardsSelected = true;
                        break;
                    }
                }
                if (hasRevealedCardsSelected) {
                    afficherLog("🔄 Cartes révélées supprimées - Sélection réinitialisée!");
                    cartesSel.clear();
                    proprietairesSel.clear();
                    indicesMilieuSel.clear();
                    mettreAJourBoutons();
                }
            }
        }
        
        // ===== SÉLECTION AUTOMATIQUE DES NOUVELLES CARTES RÉVÉLÉES =====
        // Détecter les NOUVELLES cartes révélées (MAX/MIN des autres joueurs)
        if (plateauActuel.getCartesRevelees() != null) {
            for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
                Carte carte = cr.getCarte();
                // Si cette carte révélée n'était pas déjà sélectionnée et n'est pas du milieu
                // (car les cartes du milieu sont gérées manuellement par le joueur)
                if (cr.getIdProprietaire() != monID && cr.getIdProprietaire() != -1) {  // Cartes d'autres joueurs (MAX/MIN)
                    // Vérifier si ce carte n'est pas déjà dans la sélection
                    boolean dejaSelectionnee = false;
                    for (Integer id : cartesReveleesIDs) {
                        if (id == carte.getId()) {
                            dejaSelectionnee = true;
                            break;
                        }
                    }
                    
                    // Si c'est une NOUVELLE carte révélée, l'ajouter à la sélection automatiquement
                    if (!dejaSelectionnee && cartesSel.size() < 3) {
                        cartesSel.add(carte);
                        proprietairesSel.add(cr.getIdProprietaire());
                        indicesMilieuSel.add(-1);  // Pas une carte du milieu
                        cartesReveleesIDs.add(carte.getId());  // Tracker cet ID pour plus tard
                        afficherLog("✨ Carte automatiquement sélectionnée: " + carte.getValeur() + " (Joueur " + cr.getIdProprietaire() + ")");
                        mettreAJourBoutons();
                    }
                }
            }
        }
        
        // monJoueur est déjà actualisé au début de afficherPlateau()
        
        // Autres joueurs
        autresJoueurs = new ArrayList<>(plateauActuel.getJoueurs());
        autresJoueurs.removeIf(j -> j.getId() == monID);
        
        // Mettre à jour les panneaux
        afficherMilieu();
        afficherMainJoueur();
        afficherAutresJoueurs();
        mettreAJourLabels();
    }

    /**
     * Affiche les cartes du milieu
     */
    private void afficherMilieu() {
        panelMilieu.removeAll();
        panelCartesRevelees.removeAll();  // Aussi nettoyer les cartes révélées
        
        if (plateauActuel.getMillieu() == null) {
            panelMilieu.revalidate();
            panelCartesRevelees.revalidate();
            return;
        }
        
        // Afficher les cartes du milieu FACE CACHÉE (en gris)
        for (int i = 0; i < plateauActuel.getMillieu().size(); i++) {
            Carte c = plateauActuel.getMillieu().get(i);
            final int index = i;
            
            // Créer un bouton face cachée
            JButton btn = new JButton("🂠");  // Dos de carte
            btn.setPreferredSize(new Dimension(60, 90));
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.setBackground(new Color(100, 100, 120));
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorder(new LineBorder(Color.BLACK, 2));
            
            // Vérifier si CETTE CARTE (par index) a déjà été révélée pendant ce tour
            if (cartesReveleesDuMilieu.contains(index)) {
                // Afficher la carte révélée
                btn.setText(c.getValeur() + "");
                btn.setBackground(getCouleurCarte(c));
            }
            
            boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
            btn.setEnabled(cEstMonTour && cartesSel.size() < 3);  // Déverrouiller pendant le tour
            
            btn.addActionListener(e -> selectionnerCarteMilieu(c, index, -1));
            panelMilieu.add(btn);
        }
        
        panelMilieu.revalidate();
        
        // === SECTION SÉPARÉE : TOUTES les cartes révélées (milieu + joueurs) ===
        afficherToutesCartesRevelees();
    }

    /**
     * Affiche TOUTES les cartes révélées (milieu + joueurs) dans la section révélée
     */
    private void afficherToutesCartesRevelees() {
        panelCartesRevelees.removeAll();
        
        // Récupérer toutes les cartes révélées
        if (plateauActuel == null || plateauActuel.getCartesRevelees() == null) {
            panelCartesRevelees.revalidate();
            return;
        }
        
        // Afficher les cartes révélées du milieu et des joueurs
        for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
            Carte c = cr.getCarte();
            int idProprietaire = cr.getIdProprietaire();
            String typeRev = cr.getTypeRevealation();
            
            JButton btn = creerBoutonCarte(c);
            // Couleur spéciale pour les cartes révélées
            btn.setBackground(new Color(255, 215, 0));  // Or/jaune
            
            String tooltipText;
            if (idProprietaire == -1) {
                tooltipText = "Milieu révélé";
            } else if (idProprietaire == monID) {
                tooltipText = "Ta carte révélée (" + typeRev + ")";
            } else {
                tooltipText = typeRev + " - Joueur " + idProprietaire;
            }
            btn.setToolTipText(tooltipText);
            
            boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
            
            // Vérifier si cette CARTE SPÉCIFIQUE (par ID) est déjà sélectionnée
            boolean dejaSelectionnee = false;
            for (Carte carteSel : cartesSel) {
                if (carteSel.getId() == c.getId()) {  // ✅ Utiliser l'ID au lieu de equals()
                    dejaSelectionnee = true;
                    break;
                }
            }
            
            btn.setEnabled(cEstMonTour && cartesSel.size() < 3 && !dejaSelectionnee);  // Désactiver si déjà sélectionnée
            
            if (dejaSelectionnee) {
                btn.setBackground(new Color(200, 150, 0));  // Couleur plus sombre pour indiquer la sélection
                btn.setBorder(new LineBorder(Color.GREEN, 3));  // Bordure verte pour montrer la sélection
            }
            
            btn.addActionListener(e -> selectionnerCarteRevealee(c, idProprietaire));
            panelCartesRevelees.add(btn);
        }
        
        panelCartesRevelees.revalidate();
    }

    /**
     * Affiche la main du joueur
     */
    private void afficherMainJoueur() {
        panelMainJoueur.removeAll();
        
        if (monJoueur == null || monJoueur.getDeck() == null) {
            panelMainJoueur.revalidate();
            return;
        }
        
        boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
        
        // Créer une liste triée des cartes par valeur croissante
        List<Carte> carteTriees = new ArrayList<>(monJoueur.getDeck());
        carteTriees.sort((c1, c2) -> Integer.compare(c1.getValeur(), c2.getValeur()));
        
        for (int i = 0; i < carteTriees.size(); i++) {
            Carte c = carteTriees.get(i);
            final int index = i;
            JButton btn = creerBoutonCarte(c);
            
            // Vérifier si cette carte spécifique (par ID) est déjà sélectionnée
            boolean dejaSelectionnee = false;
            for (Carte carteSel : cartesSel) {
                if (carteSel.getId() == c.getId()) {
                    dejaSelectionnee = true;
                    break;
                }
            }
            
            btn.setEnabled(cEstMonTour && cartesSel.size() < 3 && !dejaSelectionnee);  // Vérifier l'ID unique
            
            if (dejaSelectionnee) {
                btn.setBackground(new Color(200, 150, 0));  // Couleur plus sombre
                btn.setBorder(new LineBorder(Color.GREEN, 3));  // Bordure verte
            }
            
            btn.addActionListener(e -> selectionnerCarteMain(c, index));
            panelMainJoueur.add(btn);
        }
        
        panelMainJoueur.revalidate();
    }

    /**
     * Affiche les autres joueurs et leurs trios
     */
    private void afficherAutresJoueurs() {
        panelAutresJoueurs.removeAll();
        
        boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
        
        for (Joueur j : autresJoueurs) {
            JPanel pJoueur = new JPanel();
            pJoueur.setLayout(new BoxLayout(pJoueur, BoxLayout.Y_AXIS));
            pJoueur.setBackground(new Color(70, 70, 80));
            pJoueur.setBorder(new TitledBorder("Joueur " + j.getId()));
            
            // Info joueur
            JPanel pInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
            pInfo.setBackground(new Color(70, 70, 80));
            
            JLabel lTrios = new JLabel("Trios: " + j.getTrios().size() + "/3");
            lTrios.setForeground(Color.WHITE);
            lTrios.setFont(new Font("Arial", Font.BOLD, 11));
            pInfo.add(lTrios);
            
            JLabel lCartes = new JLabel(" | Cartes: " + (j.getDeck() != null ? j.getDeck().size() : 0));
            lCartes.setForeground(Color.WHITE);
            lCartes.setFont(new Font("Arial", Font.PLAIN, 11));
            pInfo.add(lCartes);
            
            pJoueur.add(pInfo);
            
            // Boutons MAX et MIN
            JPanel pBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pBoutons.setBackground(new Color(70, 70, 80));
            
            JButton btnMax = new JButton("↑ Plus Grande");
            btnMax.setFont(new Font("Arial", Font.BOLD, 11));
            btnMax.setBackground(new Color(100, 150, 100));
            btnMax.setForeground(Color.WHITE);
            btnMax.setEnabled(cEstMonTour);
            btnMax.addActionListener(e -> demanderCarte(j.getId(), "MAX"));
            pBoutons.add(btnMax);
            
            JButton btnMin = new JButton("↓ Plus Petite");
            btnMin.setFont(new Font("Arial", Font.BOLD, 11));
            btnMin.setBackground(new Color(150, 100, 100));
            btnMin.setForeground(Color.WHITE);
            btnMin.setEnabled(cEstMonTour);
            btnMin.addActionListener(e -> demanderCarte(j.getId(), "MIN"));
            pBoutons.add(btnMin);
            
            pJoueur.add(pBoutons);
            
            panelAutresJoueurs.add(pJoueur);
            panelAutresJoueurs.add(Box.createVerticalStrut(10));
        }
        
        panelAutresJoueurs.revalidate();
    }

    /**
     * Demande une carte à un joueur (MAX ou MIN)
     */
    private void demanderCarte(int idJoueur, String type) {
        try {
            commun.action.Action action;
            if (type.equals("MAX")) {
                action = new ActionMax(monID, idJoueur);
            } else {
                action = new ActionMin(monID, idJoueur);
            }
            out.writeObject(action);
            out.flush();
            afficherLog("📤 Demande de carte " + type + " au joueur " + idJoueur);
        } catch (IOException e) {
            afficherLog("❌ Erreur: " + e.getMessage());
        }
    }

    /**
     * Sélectionne une carte du milieu
     */
    private void selectionnerCarteMilieu(Carte carte, int index, int proprietaire) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog("❌ Ce n'est pas ton tour!");
            return;
        }
        
        // Vérifier si cette MÊME CARTE (du milieu au même index) est déjà sélectionnée
        for (int i = 0; i < cartesSel.size(); i++) {
            if (i < indicesMilieuSel.size()) {
                int indMilieu = indicesMilieuSel.get(i);
                // Si on essaie de sélectionner la même position du milieu, refuser
                if (indMilieu == index) {
                    afficherLog("❌ Cette carte est déjà sélectionnée!");
                    return;
                }
            }
        }
        
        // Limiter à 3 cartes au total (milieu + main)
        if (cartesSel.size() >= 3) {
            afficherLog("❌ Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        // Ajouter la carte à la sélection
        cartesSel.add(carte);
        proprietairesSel.add(-1);  // -1 = milieu
        indicesMilieuSel.add(index);  // Tracker l'index du milieu
        cartesReveleesDuMilieu.add(index);  // Ajouter l'INDEX, pas la carte
        etapeActuelle++;
        afficherLog("📍 Carte milieu révélée: " + carte.getValeur() + " (" + carte.getCouleur() + ")");
        mettreAJourBoutons();
        afficherMilieu();  // Rafraîchir l'affichage du milieu
    }

    /**
     * Sélectionne une carte de la main
     */
    private void selectionnerCarteMain(Carte carte, int index) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog("❌ Ce n'est pas ton tour!");
            return;
        }
        
        // Vérifier si une carte IDENTIQUE est déjà sélectionnée (par ID unique)
        for (Carte c : cartesSel) {
            if (c.getId() == carte.getId()) {
                afficherLog("❌ Cette carte est déjà sélectionnée!");
                return;
            }
        }
        
        // Limiter à 3 cartes au total
        if (cartesSel.size() >= 3) {
            afficherLog("❌ Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        cartesSel.add(carte);
        proprietairesSel.add(monID);
        indicesMilieuSel.add(-1);  // -1 car ce n'est pas une carte du milieu
        etapeActuelle++;
        afficherLog("🎴 Carte main sélectionnée: " + carte.getValeur() + " (" + carte.getCouleur() + ")");
        mettreAJourBoutons();
        afficherMainJoueur();  // Rafraîchir pour montrer les cartes désactivées
    }

    /**
     * Sélectionne une carte révélée (MAX/MIN d'un autre joueur)
     */
    private void selectionnerCarteRevealee(Carte carte, int idProprietaire) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog("❌ Ce n'est pas ton tour!");
            return;
        }
        
        // Vérifier si une carte IDENTIQUE est déjà sélectionnée (par ID unique)
        for (Carte c : cartesSel) {
            if (c.getId() == carte.getId()) {
                afficherLog("❌ Cette carte est déjà sélectionnée!");
                return;
            }
        }
        
        // Limiter à 3 cartes au total
        if (cartesSel.size() >= 3) {
            afficherLog("❌ Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        cartesSel.add(carte);
        proprietairesSel.add(idProprietaire);  // Le propriétaire original, pas monID
        indicesMilieuSel.add(-1);  // -1 car ce n'est pas une carte du milieu
        etapeActuelle++;
        afficherLog("⭐ Carte révélée sélectionnée: " + carte.getValeur() + " (du Joueur " + idProprietaire + ")");
        mettreAJourBoutons();
        afficherMilieu();  // Rafraîchir pour mettre à jour l'affichage
    }

    /**
     * Vérifie le trio
     */
    private void verifierTrio() {
        if (cartesSel.size() != 3) {
            afficherLog("❌ Sélectionnez 3 cartes! (" + cartesSel.size() + "/3)");
            return;
        }
        
        try {
            // Extraire les IDs des cartes au lieu de passer les cartes elles-mêmes
            List<Integer> idsCartes = new ArrayList<>();
            for (Carte carte : cartesSel) {
                idsCartes.add(carte.getId());
            }
            
            ActionTrio action = new ActionTrio(monID, idsCartes, proprietairesSel);
            out.writeObject(action);
            out.flush();
            afficherLog("✅ Trio envoyé au serveur avec " + cartesSel.size() + " cartes");
        } catch (IOException e) {
            afficherLog("❌ Erreur: " + e.getMessage());
        } finally {
            // TOUJOURS annuler la sélection après l'envoi (même si erreur)
            annulerSelection();
        }
    }

    /**
     * Annule la sélection actuelle
     */
    private void annulerSelection() {
        cartesSel.clear();
        proprietairesSel.clear();
        indicesMilieuSel.clear();  // Réinitialiser aussi les indices
        cartesReveleesDuMilieu.clear();  // Réinitialiser les cartes révélées du milieu sélectionnées
        // NOTE: On ne touche PAS à cartesReveleesIDs ici! 
        // On l'aura vidé après avoir REÇU le nouveau plateau du serveur dans afficherPlateau()
        mettreAJourBoutons();
        afficherLog("↩️ Sélection annulée");
    }

    /**
     * Crée un bouton pour une carte
     */
    private JButton creerBoutonCarte(Carte c) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(60, 90));
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setText(c.getValeur() + "");
        btn.setBackground(getCouleurCarte(c));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(new LineBorder(Color.BLACK, 2));
        return btn;
    }

    /**
     * Retourne la couleur pour afficher une carte
     */
    private Color getCouleurCarte(Carte c) {
        return switch(c.getCouleur()) {
            case ROUGE -> new Color(200, 50, 50);
            case VERT -> new Color(50, 200, 50);
            case VIOLET -> new Color(150, 50, 200);
            default -> Color.GRAY;
        };
    }

    /**
     * Met à jour les boutons
     */
    private void mettreAJourBoutons() {
        btnVerifierTrio.setText("✓ Vérifier Trio (" + cartesSel.size() + "/3)");
        btnVerifierTrio.setEnabled(cartesSel.size() == 3);
    }

    /**
     * Met à jour les labels d'info
     */
    private void mettreAJourLabels() {
        if (monJoueur != null && plateauActuel != null) {
            int trios = monJoueur.getTrios().size();
            int joueurActuel = plateauActuel.getJoueurActuel();
            String tourInfo = (joueurActuel == monID) ? "🎯 TON TOUR" : "Joueur " + joueurActuel + " joue";
            labelInfo.setText(tourInfo + " | Trios: " + trios + "/3 | Étape: " + (etapeActuelle + 1) + "/4");
        }
    }

    /**
     * Vérifie que les cartes sélectionnées existent toujours dans le plateau
     * Si une carte n'existe plus, on vide toute la sélection
     */
    private void verifierCartesSelectionneesValides() {
        if (cartesSel.isEmpty()) return;
        
        // Actualiser monJoueur avec les données du plateau ACTUEL
        monJoueur = plateauActuel.getJoueurs().stream()
            .filter(j -> j.getId() == monID)
            .findFirst()
            .orElse(null);
        
        // Vérifier chaque carte sélectionnée
        for (int i = 0; i < cartesSel.size(); i++) {
            Carte carte = cartesSel.get(i);
            int proprietaire = proprietairesSel.get(i);
            boolean carteTrouvee = false;
            
            if (proprietaire <= 0) {
                // Carte du milieu
                for (Carte c : plateauActuel.getMillieu()) {
                    if (c.getId() == carte.getId()) {
                        carteTrouvee = true;
                        break;
                    }
                }
            } else if (proprietaire == monID) {
                // Carte de ma main
                if (monJoueur != null) {
                    for (Carte c : monJoueur.getDeck()) {
                        if (c.getId() == carte.getId()) {
                            carteTrouvee = true;
                            break;
                        }
                    }
                }
            } else {
                // Carte d'un autre joueur (révélée)
                for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
                    if (cr.getIdProprietaire() == proprietaire && cr.getCarte().getId() == carte.getId()) {
                        carteTrouvee = true;
                        break;
                    }
                }
            }
            
            // Si une carte n'existe plus, vider toute la sélection
            if (!carteTrouvee) {
                afficherLog("❌ Carte ID " + carte.getId() + " (prop: " + proprietaire + ") n'existe plus - Sélection réinitialisée!");
                cartesSel.clear();
                proprietairesSel.clear();
                indicesMilieuSel.clear();
                cartesReveleesIDs.clear();
                mettreAJourBoutons();
                return;  // Sortir après nettoyage
            }
        }
    }

    /**
     * Affiche l'écran de fin de partie avec le gagnant
     */
    private void afficherEcranVictoire() {
        String nomGagnant = "Joueur " + plateauActuel.getGagnant();
        
        // Chercher le nom réel du gagnant
        for (Joueur j : plateauActuel.getJoueurs()) {
            if (j.getId() == plateauActuel.getGagnant()) {
                nomGagnant = j.getNom();
                break;
            }
        }
        
        final String gagnantNom = nomGagnant;
        final int gagnantID = plateauActuel.getGagnant();
        
        // Créer l'écran de victoire
        JPanel victoryPanel = new JPanel();
        victoryPanel.setLayout(new BoxLayout(victoryPanel, BoxLayout.Y_AXIS));
        victoryPanel.setBackground(new Color(30, 30, 40));
        
        if (gagnantID == monID) {
            JLabel victoryLabel = new JLabel("🎉 VICTOIRE! 🎉");
            victoryLabel.setFont(new Font("Arial", Font.BOLD, 60));
            victoryLabel.setForeground(new Color(0, 255, 0));
            victoryLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            victoryPanel.add(Box.createVerticalStrut(100));
            victoryPanel.add(victoryLabel);
            
            JLabel messageLabel = new JLabel("Vous avez gagné avec 3 trios!");
            messageLabel.setFont(new Font("Arial", Font.PLAIN, 40));
            messageLabel.setForeground(Color.WHITE);
            messageLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            victoryPanel.add(Box.createVerticalStrut(30));
            victoryPanel.add(messageLabel);
        } else {
            JLabel defeatLabel = new JLabel("DÉFAITE");
            defeatLabel.setFont(new Font("Arial", Font.BOLD, 60));
            defeatLabel.setForeground(new Color(255, 100, 100));
            defeatLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            victoryPanel.add(Box.createVerticalStrut(80));
            victoryPanel.add(defeatLabel);
            
            JLabel winnerLabel = new JLabel(gagnantNom);
            winnerLabel.setFont(new Font("Arial", Font.BOLD, 50));
            winnerLabel.setForeground(new Color(255, 215, 0));
            winnerLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            victoryPanel.add(Box.createVerticalStrut(20));
            victoryPanel.add(winnerLabel);
            
            JLabel hasWonLabel = new JLabel("a gagné avec 3 trios!");
            hasWonLabel.setFont(new Font("Arial", Font.PLAIN, 40));
            hasWonLabel.setForeground(Color.WHITE);
            hasWonLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
            victoryPanel.add(Box.createVerticalStrut(20));
            victoryPanel.add(hasWonLabel);
        }
        
        victoryPanel.add(Box.createVerticalGlue());
        
        // Remplacer le contenu
        getContentPane().removeAll();
        getContentPane().add(victoryPanel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
        
        afficherLog("🏆 FIN DE LA PARTIE - " + gagnantNom + " a gagné!");
    }
    
    /**
     * Affiche un message dans le log
     */
    private void afficherLog(String message) {
        SwingUtilities.invokeLater(() -> {
            textLog.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] " + message + "\n");
            textLog.setCaretPosition(textLog.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TrioGUI());
    }
}
