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


// Fenêtre principale côté client : connexion et interface de jeu
public class TrioGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Map<Integer, String> MATIERE_PAR_VALEUR = Map.ofEntries(
        Map.entry(1, "AP4A"),
        Map.entry(2, "IA41"),
        Map.entry(3, "IT44"),
        Map.entry(4, "RE4E"),
        Map.entry(5, "RS40"),
        Map.entry(6, "SI40"),
        Map.entry(7, "PC40"),
        Map.entry(8, "SY41"),
        Map.entry(9, "WE4A"),
        Map.entry(10, "ST40"),
        Map.entry(11, "HN01"),
        Map.entry(12, "AP4B")
    );
    
    
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int monID = -1;
    
    
    private Plateau plateauActuel;
    private Joueur monJoueur;
    private List<Joueur> autresJoueurs = new ArrayList<>();
    
    
    private List<Carte> cartesSel = new ArrayList<>();  
    private List<Integer> proprietairesSel = new ArrayList<>();  
    private List<Integer> indicesMilieuSel = new ArrayList<>();  
    private int etapeActuelle = 0;  
    private Set<Integer> cartesReveleesDuMilieu = new HashSet<>();  
    private List<Integer> cartesReveleesIDs = new ArrayList<>();  
    private int joueurActuelPrecedent = -1;  
    
    
    private JPanel panelMilieu;
    private JPanel panelCartesRevelees;  
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

    
    // Crée l'interface de connexion (hôte, port, bouton)
    private void initConnectionUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(40, 40, 50));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(20, 20, 20, 20);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        
        JLabel titre = new JLabel("Connexion au Serveur Trio");
        titre.setFont(new Font("Arial", Font.BOLD, 28));
        titre.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titre, gbc);
        
        
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        JLabel lHost = new JLabel("Serveur:");
        lHost.setForeground(Color.WHITE);
        panel.add(lHost, gbc);
        
        JTextField tfHost = new JTextField("localhost", 20);
        gbc.gridx = 1;
        panel.add(tfHost, gbc);
        
        
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lPort = new JLabel("Port:");
        lPort.setForeground(Color.WHITE);
        panel.add(lPort, gbc);
        
        JTextField tfPort = new JTextField("5000", 20);
        gbc.gridx = 1;
        panel.add(tfPort, gbc);
        
        
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

    
    // Ouvre une connexion au serveur et initialise les flux (utilisé par l'UI)
    private void connecterAuServeur(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            
            
            Object obj = in.readObject();
            if (obj instanceof String) {
                String msg = (String) obj;
                if (msg.startsWith("ID:")) {
                    monID = Integer.parseInt(msg.substring(3));
                    System.out.println(" Connecté avec l'ID: " + monID);
                    initGameUI();
                    startReceiver();
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur de connexion: " + e.getMessage());
        }
    }

    
    private void initGameUI() {
        setContentPane(createGamePanel());
        revalidate();
        repaint();
    }

    
    // Construit le panneau principal du jeu (milieu, main, infos)
    private JPanel createGamePanel() {
        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(new Color(60, 60, 70));
        main.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        topPanel.setBackground(new Color(80, 80, 90));
        
        labelInfo = new JLabel("Joueur " + monID + " | En attente...");
        labelInfo.setForeground(Color.WHITE);
        labelInfo.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(labelInfo);
        
        main.add(topPanel, BorderLayout.NORTH);
        
        
        JPanel centrePanel = new JPanel(new BorderLayout(10, 10));
        centrePanel.setBackground(new Color(60, 60, 70));
        
        
        JPanel panelMilieuContainer = new JPanel(new BorderLayout());
        panelMilieuContainer.setBackground(new Color(40, 100, 40));
        panelMilieuContainer.setBorder(new TitledBorder("MILIEU"));
        panelMilieu = new JPanel(new GridLayout(3, 3, 8, 8));
        panelMilieu.setBackground(new Color(40, 100, 40));
        panelMilieu.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelMilieuContainer.add(panelMilieu, BorderLayout.CENTER);
        centrePanel.add(panelMilieuContainer, BorderLayout.NORTH);
        
        
        JPanel panelCartesReveleesContainer = new JPanel(new BorderLayout());
        panelCartesReveleesContainer.setBackground(new Color(100, 140, 50));
        panelCartesReveleesContainer.setBorder(new TitledBorder("CARTES RÉVÉLÉES (MAX/MIN)"));
        panelCartesRevelees = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelCartesRevelees.setBackground(new Color(100, 140, 50));
        panelCartesRevelees.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelCartesReveleesContainer.add(panelCartesRevelees, BorderLayout.CENTER);
        
        
        JPanel topCentrePanel = new JPanel(new BorderLayout(10, 10));
        topCentrePanel.setBackground(new Color(60, 60, 70));
        topCentrePanel.add(panelMilieuContainer, BorderLayout.NORTH);
        topCentrePanel.add(panelCartesReveleesContainer, BorderLayout.CENTER);
        centrePanel.add(topCentrePanel, BorderLayout.NORTH);
        
        
        JPanel panelMainContainer = new JPanel(new BorderLayout());
        panelMainContainer.setBackground(new Color(50, 50, 100));
        panelMainContainer.setBorder(new TitledBorder("Ma Main"));
        panelMainJoueur = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panelMainJoueur.setBackground(new Color(50, 50, 100));
        panelMainJoueur.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelMainContainer.add(new JScrollPane(panelMainJoueur), BorderLayout.CENTER);
        centrePanel.add(panelMainContainer, BorderLayout.CENTER);
        
        main.add(centrePanel, BorderLayout.CENTER);
        
        
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBackground(new Color(60, 60, 70));
        
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        btnPanel.setBackground(new Color(60, 60, 70));
        
        btnVerifierTrio = new JButton(" Vérifier Trio (0/3)");
        btnVerifierTrio.setFont(new Font("Arial", Font.BOLD, 12));
        btnVerifierTrio.setEnabled(false);
        btnVerifierTrio.addActionListener(e -> verifierTrio());
        btnPanel.add(btnVerifierTrio);
        
        bottomPanel.add(btnPanel, BorderLayout.NORTH);
        
        
        textLog = new JTextArea(4, 50);
        textLog.setEditable(false);
        textLog.setFont(new Font("Monospaced", Font.PLAIN, 10));
        textLog.setBackground(new Color(30, 30, 40));
        textLog.setForeground(new Color(150, 255, 150));
        JScrollPane scrollLog = new JScrollPane(textLog);
        bottomPanel.add(scrollLog, BorderLayout.CENTER);
        
        main.add(bottomPanel, BorderLayout.SOUTH);
        
        
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

    
    // Démarre le thread qui écoute les messages envoyés par le serveur
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

    
    // Met à jour l'affichage complet du plateau reçu (milieu, mains, révélées)
    private void afficherPlateau() {
        if (plateauActuel == null) return;
        
        
        if (plateauActuel.getPhaseActuelle() == Phase.FIN_PARTIE) {
            afficherEcranVictoire();
            return;
        }
        
        
        monJoueur = plateauActuel.getJoueurs().stream()
            .filter(j -> j.getId() == monID)
            .findFirst()
            .orElse(null);
        
        
        
        verifierCartesSelectionneesValides();
        
        
        if (joueurActuelPrecedent != -1 && joueurActuelPrecedent != plateauActuel.getJoueurActuel()) {
            cartesReveleesDuMilieu.clear();  
            cartesReveleesIDs.clear();  
            cartesSel.clear();  
            proprietairesSel.clear();  
            indicesMilieuSel.clear();  
            
            afficherLog(" Changement de tour");
        }
        joueurActuelPrecedent = plateauActuel.getJoueurActuel();
        
        
        
        if (plateauActuel.getCartesRevelees().isEmpty()) {
            cartesReveleesIDs.clear();
            
            
            if (!cartesSel.isEmpty()) {
                
                boolean hasRevealedCardsSelected = false;
                for (int i = 0; i < cartesSel.size(); i++) {
                    int proprietaire = proprietairesSel.get(i);
                    if (proprietaire != monID && proprietaire > 0) {
                        hasRevealedCardsSelected = true;
                        break;
                    }
                }
                if (hasRevealedCardsSelected) {
                    afficherLog(" Cartes révélées supprimées - Sélection réinitialisée!");
                    cartesSel.clear();
                    proprietairesSel.clear();
                    indicesMilieuSel.clear();
                    mettreAJourBoutons();
                }
            }
        }
        
        
        
        if (plateauActuel.getCartesRevelees() != null) {
            for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
                Carte carte = cr.getCarte();
                
                
                if (cr.getIdProprietaire() != monID && cr.getIdProprietaire() != -1) {  
                    
                    boolean dejaSelectionnee = false;
                    for (Integer id : cartesReveleesIDs) {
                        if (id == carte.getId()) {
                            dejaSelectionnee = true;
                            break;
                        }
                    }
                    
                    
                    if (!dejaSelectionnee && cartesSel.size() < 3) {
                        cartesSel.add(carte);
                        proprietairesSel.add(cr.getIdProprietaire());
                        indicesMilieuSel.add(-1);  
                        cartesReveleesIDs.add(carte.getId());  
                        afficherLog(" Carte automatiquement sélectionnée: " + carte.getValeur() + " (Joueur " + cr.getIdProprietaire() + ")");
                        mettreAJourBoutons();
                    }
                }
            }
        }
        
        
        
        
        autresJoueurs = new ArrayList<>(plateauActuel.getJoueurs());
        autresJoueurs.removeIf(j -> j.getId() == monID);
        
        
        afficherMilieu();
        afficherMainJoueur();
        afficherAutresJoueurs();
        mettreAJourLabels();
    }

    
    // Affiche les cartes présentes au centre du plateau
    private void afficherMilieu() {
        panelMilieu.removeAll();
        panelCartesRevelees.removeAll();  
        
        if (plateauActuel.getMillieu() == null) {
            panelMilieu.revalidate();
            panelCartesRevelees.revalidate();
            return;
        }
        
        
        for (int i = 0; i < plateauActuel.getMillieu().size(); i++) {
            Carte c = plateauActuel.getMillieu().get(i);
            final int index = i;
            
            
            JButton btn = new JButton("🂠");  
            btn.setPreferredSize(new Dimension(60, 90));
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            btn.setBackground(new Color(100, 100, 120));
            btn.setForeground(Color.WHITE);
            btn.setOpaque(true);
            btn.setBorder(new LineBorder(Color.BLACK, 2));
            
            
            if (cartesReveleesDuMilieu.contains(index)) {
                
                btn.setText(c.getValeur() + "");
                btn.setBackground(getCouleurCarte(c));
            }
            
            boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
            // Ne pas désactiver visuellement : vérifie les conditions dans l'écouteur
            btn.addActionListener(e -> {
                if (!cEstMonTour || cartesSel.size() >= 3) return;
                selectionnerCarteMilieu(c, index, -1);
            });
            panelMilieu.add(btn);
        }
        
        panelMilieu.revalidate();
        
        
        afficherToutesCartesRevelees();
    }

    
    // Affiche la liste des cartes révélées publiquement
    private void afficherToutesCartesRevelees() {
        panelCartesRevelees.removeAll();
        
        
        if (plateauActuel == null || plateauActuel.getCartesRevelees() == null) {
            panelCartesRevelees.revalidate();
            return;
        }
        
        
        for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
            Carte c = cr.getCarte();
            int idProprietaire = cr.getIdProprietaire();
            String typeRev = cr.getTypeRevealation();
            
            JButton btn = creerBoutonCarte(c);
            
            btn.setBackground(new Color(255, 215, 0));  
            
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
            
            
            boolean dejaSelectionnee = false;
            for (Carte carteSel : cartesSel) {
                if (carteSel.getId() == c.getId()) {  
                    dejaSelectionnee = true;
                    break;
                }
            }
            
            final boolean dejaSelectionneeFinal = dejaSelectionnee;
            btn.addActionListener(e -> {
                if (!cEstMonTour || cartesSel.size() >= 3 || dejaSelectionneeFinal) return;
                selectionnerCarteRevealee(c, idProprietaire);
            });
            
            if (dejaSelectionnee) {
                btn.setBackground(new Color(200, 150, 0));  
                btn.setBorder(new LineBorder(Color.GREEN, 3));  
            }
            panelCartesRevelees.add(btn);
        }
        
        panelCartesRevelees.revalidate();
    }

    
    // Affiche la main (les cartes) du joueur local
    private void afficherMainJoueur() {
        panelMainJoueur.removeAll();
        
        if (monJoueur == null || monJoueur.getDeck() == null) {
            panelMainJoueur.revalidate();
            return;
        }
        
        boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
        
        
        List<Carte> carteTriees = new ArrayList<>(monJoueur.getDeck());
        carteTriees.sort((c1, c2) -> Integer.compare(c1.getValeur(), c2.getValeur()));
        
        for (int i = 0; i < carteTriees.size(); i++) {
            Carte c = carteTriees.get(i);
            final int index = i;
            JButton btn = creerBoutonCarte(c);
            
            
            boolean dejaSelectionnee = false;
            for (Carte carteSel : cartesSel) {
                if (carteSel.getId() == c.getId()) {
                    dejaSelectionnee = true;
                    break;
                }
            }
            
            final boolean dejaSelectionneeFinal = dejaSelectionnee;
            btn.addActionListener(e -> {
                if (!cEstMonTour || cartesSel.size() >= 3 || dejaSelectionneeFinal) return;
                selectionnerCarteMain(c, index);
            });
            
            if (dejaSelectionnee) {
                btn.setBackground(new Color(200, 150, 0));  
                btn.setBorder(new LineBorder(Color.GREEN, 3));  
            }
            panelMainJoueur.add(btn);
        }
        
        panelMainJoueur.revalidate();
    }

    
    // Affiche des résumés pour les autres joueurs (triors, cartes cachées)
    private void afficherAutresJoueurs() {
        panelAutresJoueurs.removeAll();
        
        boolean cEstMonTour = (plateauActuel != null && plateauActuel.getJoueurActuel() == monID);
        
        for (Joueur j : autresJoueurs) {
            JPanel pJoueur = new JPanel();
            pJoueur.setLayout(new BoxLayout(pJoueur, BoxLayout.Y_AXIS));
            pJoueur.setBackground(new Color(70, 70, 80));
            TitledBorder tb = new TitledBorder("Joueur " + j.getId());
            tb.setTitleColor(Color.WHITE);
            pJoueur.setBorder(tb);
            
            
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
            
            
            JPanel pBoutons = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            pBoutons.setBackground(new Color(70, 70, 80));
            
            JButton btnMax = new JButton("↑ Plus Grande");
            btnMax.setFont(new Font("Arial", Font.BOLD, 11));
            btnMax.setBackground(new Color(100, 150, 100));
            btnMax.setForeground(Color.WHITE);

            btnMax.addActionListener(e -> { if (!cEstMonTour) return; demanderCarte(j.getId(), "MAX"); });
            pBoutons.add(btnMax);
            
            JButton btnMin = new JButton("↓ Plus Petite");
            btnMin.setFont(new Font("Arial", Font.BOLD, 11));
            btnMin.setBackground(new Color(150, 100, 100));
            btnMin.setForeground(Color.WHITE);
            // Ne pas désactiver visuellement : contrôle via le listener
            btnMin.addActionListener(e -> { if (!cEstMonTour) return; demanderCarte(j.getId(), "MIN"); });
            pBoutons.add(btnMin);
            
            pJoueur.add(pBoutons);
            
            panelAutresJoueurs.add(pJoueur);
            panelAutresJoueurs.add(Box.createVerticalStrut(10));
        }
        
        panelAutresJoueurs.revalidate();
    }

    
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
            afficherLog(" Demande de carte " + type + " au joueur " + idJoueur);
        } catch (IOException e) {
            afficherLog(" Erreur: " + e.getMessage());
        }
    }

    
    // Gère la sélection d'une carte au milieu (clic utilisateur)
    private void selectionnerCarteMilieu(Carte carte, int index, int proprietaire) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog(" Ce n'est pas ton tour!");
            return;
        }
        
        
        for (int i = 0; i < cartesSel.size(); i++) {
            if (i < indicesMilieuSel.size()) {
                int indMilieu = indicesMilieuSel.get(i);
                
                if (indMilieu == index) {
                    afficherLog(" Cette carte est déjà sélectionnée!");
                    return;
                }
            }
        }
        
        
        if (cartesSel.size() >= 3) {
            afficherLog(" Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        
        cartesSel.add(carte);
        proprietairesSel.add(-1);  
        indicesMilieuSel.add(index);  
        cartesReveleesDuMilieu.add(index);  
        etapeActuelle++;
        afficherLog(" Carte milieu révélée: " + carte.getValeur() + " (" + carte.getCouleur() + ")");
        mettreAJourBoutons();
        afficherMilieu();  
    }

    
    // Sélectionne une carte dans la main du joueur (pour construire un TRIO)
    private void selectionnerCarteMain(Carte carte, int index) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog(" Ce n'est pas ton tour!");
            return;
        }
        
        
        for (Carte c : cartesSel) {
            if (c.getId() == carte.getId()) {
                afficherLog(" Cette carte est déjà sélectionnée!");
                return;
            }
        }
        
        
        if (cartesSel.size() >= 3) {
            afficherLog(" Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        cartesSel.add(carte);
        proprietairesSel.add(monID);
        indicesMilieuSel.add(-1);  
        etapeActuelle++;
        afficherLog(" Carte main sélectionnée: " + carte.getValeur() + " (" + carte.getCouleur() + ")");
        mettreAJourBoutons();
        afficherMainJoueur();  
    }

    
    // Sélectionne une carte qui a déjà été révélée (peut faire partie d'un TRIO)
    private void selectionnerCarteRevealee(Carte carte, int idProprietaire) {
        if (plateauActuel == null || plateauActuel.getJoueurActuel() != monID) {
            afficherLog(" Ce n'est pas ton tour!");
            return;
        }
        
        
        for (Carte c : cartesSel) {
            if (c.getId() == carte.getId()) {
                afficherLog(" Cette carte est déjà sélectionnée!");
                return;
            }
        }
        
        
        if (cartesSel.size() >= 3) {
            afficherLog(" Tu as déjà sélectionné 3 cartes!");
            return;
        }
        
        cartesSel.add(carte);
        proprietairesSel.add(idProprietaire);  
        indicesMilieuSel.add(-1);  
        etapeActuelle++;
        afficherLog("Carte révélée sélectionnée: " + carte.getValeur() + " (du Joueur " + idProprietaire + ")");
        mettreAJourBoutons();
        afficherMilieu();  
    }

    
    // Vérifie localement si les 3 cartes sélectionnées forment un trio
    private void verifierTrio() {
        if (cartesSel.size() != 3) {
            afficherLog(" Sélectionnez 3 cartes! (" + cartesSel.size() + "/3)");
            return;
        }
        
        try {
            
            List<Integer> idsCartes = new ArrayList<>();
            for (Carte carte : cartesSel) {
                idsCartes.add(carte.getId());
            }
            
            ActionTrio action = new ActionTrio(monID, idsCartes, proprietairesSel);
            out.writeObject(action);
            out.flush();
            afficherLog(" Trio envoyé au serveur avec " + cartesSel.size() + " cartes");
        } catch (IOException e) {
            afficherLog(" Erreur: " + e.getMessage());
        } finally {
            
            annulerSelection();
        }
    }

    
    // Annule la sélection courante de cartes et remet l'UI à jour
    private void annulerSelection() {
        cartesSel.clear();
        proprietairesSel.clear();
        indicesMilieuSel.clear();  
        cartesReveleesDuMilieu.clear();  
        
        
        mettreAJourBoutons();
        afficherLog("Sélection annulée");
    }

    
    // Crée et configure un JButton pour représenter une carte à l'écran
    private JButton creerBoutonCarte(Carte c) {
        JButton btn = new JButton();
        btn.setPreferredSize(new Dimension(60, 90));
        btn.setFont(new Font("Arial", Font.BOLD, 10));
        btn.setText(c.getValeur() + " | " + MATIERE_PAR_VALEUR.getOrDefault(c.getValeur(), "AP4A"));
        btn.setBackground(getCouleurCarte(c));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(true);
        btn.setBorder(new LineBorder(Color.BLACK, 2));
        return btn;
    }

    
    // Retourne la couleur Swing correspondant à la couleur de la carte
    private Color getCouleurCarte(Carte c) {
        return switch(c.getCouleur()) {
            case ROUGE -> new Color(200, 50, 50);
            case VERT -> new Color(50, 200, 50);
            case VIOLET -> new Color(150, 50, 200);
            default -> Color.GRAY;
        };
    }

    
    // Met à jour le texte et l'état des boutons liés aux actions
    private void mettreAJourBoutons() {
        btnVerifierTrio.setText(" Vérifier Trio (" + cartesSel.size() + "/3)");
        btnVerifierTrio.setEnabled(cartesSel.size() == 3);
    }

    
    // Met à jour les labels d'information (qui joue, triors, étape)
    private void mettreAJourLabels() {
        if (monJoueur != null && plateauActuel != null) {
            int trios = monJoueur.getTrios().size();
            int joueurActuel = plateauActuel.getJoueurActuel();
            String tourInfo = (joueurActuel == monID) ? " TON TOUR" : "Joueur " + joueurActuel + " joue";
            labelInfo.setText(tourInfo + " | Trios: " + trios + "/3 | Étape: " + (etapeActuelle + 1) + "/4");
        }
    }

    
    // Vérifie que les cartes sélectionnées appartiennent bien aux emplacements attendus
    private void verifierCartesSelectionneesValides() {
        if (cartesSel.isEmpty()) return;
        
        
        monJoueur = plateauActuel.getJoueurs().stream()
            .filter(j -> j.getId() == monID)
            .findFirst()
            .orElse(null);
        
        
        for (int i = 0; i < cartesSel.size(); i++) {
            Carte carte = cartesSel.get(i);
            int proprietaire = proprietairesSel.get(i);
            boolean carteTrouvee = false;
            
            if (proprietaire <= 0) {
                
                for (Carte c : plateauActuel.getMillieu()) {
                    if (c.getId() == carte.getId()) {
                        carteTrouvee = true;
                        break;
                    }
                }
            } else if (proprietaire == monID) {
                
                if (monJoueur != null) {
                    for (Carte c : monJoueur.getDeck()) {
                        if (c.getId() == carte.getId()) {
                            carteTrouvee = true;
                            break;
                        }
                    }
                }
            } else {
                
                for (CarteRevealee cr : plateauActuel.getCartesRevelees()) {
                    if (cr.getIdProprietaire() == proprietaire && cr.getCarte().getId() == carte.getId()) {
                        carteTrouvee = true;
                        break;
                    }
                }
            }
            
            
            if (!carteTrouvee) {
                afficherLog(" Carte ID " + carte.getId() + " (prop: " + proprietaire + ") n'existe plus - Sélection réinitialisée!");
                cartesSel.clear();
                proprietairesSel.clear();
                indicesMilieuSel.clear();
                cartesReveleesIDs.clear();
                mettreAJourBoutons();
                return;  
            }
        }
    }

    
    private void afficherEcranVictoire() {
        String nomGagnant = "Joueur " + plateauActuel.getGagnant();
        
        
        for (Joueur j : plateauActuel.getJoueurs()) {
            if (j.getId() == plateauActuel.getGagnant()) {
                nomGagnant = j.getNom();
                break;
            }
        }
        
        final String gagnantNom = nomGagnant;
        final int gagnantID = plateauActuel.getGagnant();
        
        
        JPanel victoryPanel = new JPanel();
        victoryPanel.setLayout(new BoxLayout(victoryPanel, BoxLayout.Y_AXIS));
        victoryPanel.setBackground(new Color(30, 30, 40));
        
        if (gagnantID == monID) {
            JLabel victoryLabel = new JLabel(" VICTOIRE! ");
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
        
        
        getContentPane().removeAll();
        getContentPane().add(victoryPanel, BorderLayout.CENTER);
        getContentPane().revalidate();
        getContentPane().repaint();
        
        afficherLog(" FIN DE LA PARTIE - " + gagnantNom + " a gagné!");
    }
    
    
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
