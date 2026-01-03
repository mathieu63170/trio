package ui;

import game.Trio;
import commun.plateau.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Classe TrioConsole : interface console pour le jeu Trio
 */
public class TrioConsole {
    private Trio jeu;
    private Scanner scanner;

    public TrioConsole() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Lance une partie en mode console
     */
    public void jouer() {
        afficherBienvenue();

        // Créer les joueurs
        List<Joueur> joueurs = creerJoueurs();

        // Initialiser le jeu
        jeu = new Trio(joueurs);
        jeu.demarrerPartie();

        // Boucle de jeu
        while (jeu.isJeuEnCours()) {
            afficherPlateau();
            afficherJoueurActuel();
            afficherScore();
            
            boolean actionValid = false;
            while (!actionValid) {
                afficherMenu();
                String choix = scanner.nextLine().trim().toUpperCase();

                switch (choix) {
                    case "S":
                        selectionnerCarte();
                        break;
                    case "V":
                        if (jeu.getSelection().size() == 3) {
                            verifierTrio();
                            actionValid = true;
                        } else {
                            System.out.println("❌ Sélectionnez exactement 3 cartes!");
                        }
                        break;
                    case "A":
                        jeu.annulerSelection();
                        System.out.println("↩ Sélection annulée");
                        actionValid = true;
                        break;
                    case "Q":
                        System.out.println("🏁 Merci d'avoir joué!");
                        return;
                    default:
                        System.out.println("❌ Choix invalide!");
                }
            }

            // Vérifier fin de partie (pas assez de cartes)
            if (jeu.getPlateau().getMillieu().size() < 3) {
                afficherFinPartie();
                break;
            }
        }
    }

    private void afficherBienvenue() {
        System.out.println("===================================================");
        System.out.println("        BIENVENUE AU JEU TRIO - UTBM");
        System.out.println("===================================================\n");
        
        System.out.println("REGLES DU JEU:");
        System.out.println("  • Trouvez des TRIOS de 3 cartes valides");
        System.out.println("  • Chaque carte a 4 attributs: Valeur, Forme, Couleur, Remplissage");
        System.out.println("  • Pour chaque attribut: soit IDENTIQUE soit TOUS DIFFÉRENTS\n");
        
        System.out.println("STRUCTURE DES CARTES:");
        System.out.println("  • Valeur: 1, 2 ou 3 (nombre de symboles)");
        System.out.println("  • Forme: ● (Cercle), ■ (Carré), 〰 (Ondulation)");
        System.out.println("  • Couleur: 🔴 (Rouge), 🟢 (Vert), 🟣 (Violet)");
        System.out.println("  • Remplissage: Plein, Vide, Rayé\n");
        System.out.println("═══════════════════════════════════════════════\n");
    }

    private List<Joueur> creerJoueurs() {
        List<Joueur> joueurs = new ArrayList<>();
        
        System.out.print("Nombre de joueurs (2-6): ");
        int nbJoueurs = 0;
        try {
            nbJoueurs = Integer.parseInt(scanner.nextLine().trim());
            if (nbJoueurs < 2 || nbJoueurs > 6) {
                nbJoueurs = 3;
                System.out.println("Nombre invalide, défaut: 3 joueurs");
            }
        } catch (NumberFormatException e) {
            nbJoueurs = 3;
        }

        for (int i = 0; i < nbJoueurs; i++) {
            System.out.print("Nom du joueur " + (i + 1) + ": ");
            String nom = scanner.nextLine().trim();
            if (nom.isEmpty()) {
                nom = "Joueur " + (i + 1);
            }
            joueurs.add(new Joueur(i + 1, nom, new ArrayList<>(), new ArrayList<>()));
        }

        return joueurs;
    }

    private void afficherPlateau() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│      🎴 CARTES AU CENTRE (12)      │");
        System.out.println("└─────────────────────────────────────┘");

        List<Carte> cartes = jeu.getPlateau().getMillieu();
        for (int i = 0; i < cartes.size(); i++) {
            Carte carte = cartes.get(i);
            String marqueur = jeu.getSelection().contains(i) ? "→ " : "  ";
            String affichage = carte.isRevelee() ? carte.toString() : "?";
            System.out.printf("%s[%2d] %s\n", marqueur, i, affichage);
        }
    }

    private void afficherJoueurActuel() {
        Joueur joueur = jeu.getJoueurCourant();
        System.out.println("\n🎯 Au tour de : " + joueur.getNom());
        System.out.println("Cartes en main: " + joueur.getDeck().size());
    }

    private void afficherScore() {
        System.out.println("\n📊 SCORES:");
        for (Joueur joueur : jeu.getJoueurs()) {
            int trios = (joueur.getTrios() != null) ? joueur.getTrios().size() : 0;
            System.out.println("  " + joueur.getNom() + ": " + trios + " trios");
        }
    }

    private void afficherMenu() {
        System.out.println("\n📋 MENU:");
        System.out.println("  [S] Sélectionner une carte");
        System.out.println("  [V] Vérifier Trio (" + jeu.getSelection().size() + "/3 sélectionnées)");
        System.out.println("  [A] Annuler la sélection");
        System.out.println("  [Q] Quitter");
        System.out.print("> ");
    }

    private void selectionnerCarte() {
        System.out.print("Numéro de carte (0-11): ");
        try {
            int index = Integer.parseInt(scanner.nextLine().trim());
            if (index >= 0 && index < 12) {
                jeu.selectionnerCarte(index);
            } else {
                System.out.println("❌ Index invalide!");
            }
        } catch (NumberFormatException e) {
            System.out.println("❌ Veuillez entrer un nombre!");
        }
    }

    private void verifierTrio() {
        System.out.println("\n✓ Vérification du trio...");
        
        List<Carte> selection = new ArrayList<>();
        for (int index : jeu.getSelection()) {
            selection.add(jeu.getPlateau().getMillieu().get(index));
        }

        System.out.println("\nCartes sélectionnées:");
        for (int i = 0; i < selection.size(); i++) {
            System.out.println("  [" + i + "] " + selection.get(i).getDetails());
        }

        boolean valide = jeu.validerTrio();

        if (valide) {
            System.out.println("\n✓ ✓ ✓ TRIO VALIDE! ✓ ✓ ✓");
            System.out.println("Vous gagnez 1 point!");
        } else {
            System.out.println("\n❌ Trio invalide!");
            System.out.println("Au tour du joueur suivant...");
        }
    }

    private void afficherFinPartie() {
        System.out.println("\n╔═════════════════════════════════════╗");
        System.out.println("║         FIN DE LA PARTIE            ║");
        System.out.println("╚═════════════════════════════════════╝\n");

        afficherScore();

        Joueur gagnant = jeu.getGagnantJoueur();
        int maxTrios = (gagnant != null && gagnant.getTrios() != null) ? gagnant.getTrios().size() : 0;
        
        System.out.println("\nGAGNANT: " + gagnant.getNom() + " avec " + maxTrios + " trios!");
    }

    public static void main(String[] args) {
        TrioConsole console = new TrioConsole();
        console.jouer();
    }
}
