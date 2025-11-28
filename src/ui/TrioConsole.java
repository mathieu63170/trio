package ui;

import game.Trio;
import model.*;
import java.util.*;

/**
 * Interface en mode console pour tester la logique du jeu Trio
 * Permet une interaction basique via entrées texte
 */
public class TrioConsole {
    private Trio jeu;
    private Scanner scanner;
    private List<Joueur> joueurs;

    /**
     * Constructeur de l'interface console
     */
    public TrioConsole() {
        this.scanner = new Scanner(System.in);
        this.joueurs = new ArrayList<>();
    }

    /**
     * Lance le jeu en mode console
     */
    public void lancer() {
        afficherBienvenue();
        initialiserJoueurs();
        jeu = new Trio(joueurs);
        jeu.demarrerPartie();
        jouerPartie();
        afficherResultats();
    }

    /**
     * Affiche le message de bienvenue
     */
    private void afficherBienvenue() {
        System.out.println("\n═══════════════════════════════════════════");
        System.out.println("        BIENVENUE AU JEU TRIO - UTBM");
        System.out.println("═══════════════════════════════════════════\n");
        System.out.println("Trouvez des trios de cartes valides!");
        System.out.println("Règle: Pour chaque attribut, soit identique");
        System.out.println("       soit tous différents\n");
    }

    /**
     * Initialise les joueurs
     */
    private void initialiserJoueurs() {
        System.out.print("Combien de joueurs? (1-4): ");
        int nbJoueurs = lireEntier(1, 4);

        for (int i = 1; i <= nbJoueurs; i++) {
            System.out.print("Nom du joueur " + i + ": ");
            String nom = scanner.nextLine().trim();
            if (nom.isEmpty()) {
                nom = "Joueur " + i;
            }
            joueurs.add(new Joueur("joueur_" + i, nom));
        }
    }

    /**
     * Boucle principale du jeu
     */
    private void jouerPartie() {
        while (!jeu.estTerminee()) {
            afficherEtatJeu();
            effectuerAction();
            jeu.joueurSuivant();
        }
    }

    /**
     * Affiche l'état actuel du jeu
     */
    private void afficherEtatJeu() {
        System.out.println("\n" + "─".repeat(50));
        System.out.println("Tour de: " + jeu.getJoueurActuel().getNom());
        System.out.println("─".repeat(50));

        afficherCartes();
        afficherScores();
    }

    /**
     * Affiche les cartes en jeu
     */
    private void afficherCartes() {
        System.out.println("\nCartes en jeu:");
        List<Carte> cartes = jeu.getCartesEnJeu();
        for (int i = 0; i < cartes.size(); i++) {
            String marque = jeu.getCartesSelectionnees().contains(i) ? " ✓ " : "   ";
            System.out.println(marque + "[" + i + "] " + cartes.get(i));
        }
        System.out.println("\nCartes restantes au deck: " + jeu.getNombreCartesRestantes());
    }

    /**
     * Affiche les scores
     */
    private void afficherScores() {
        System.out.println("\nScores:");
        for (Joueur j : jeu.getJoueurs()) {
            System.out.println("  " + j.getNom() + ": " + j.getScore() + " points");
        }
    }

    /**
     * Effectue une action utilisateur
     */
    private void effectuerAction() {
        System.out.println("\nActions disponibles:");
        System.out.println("  1. Sélectionner une carte (ex: 0, 5, 8)");
        System.out.println("  2. Vérifier le trio");
        System.out.println("  3. Annuler sélection");
        System.out.print("Choisissez une action: ");

        String input = scanner.nextLine().trim();

        if (input.equals("2")) {
            verifierTrio();
        } else if (input.equals("3")) {
            System.out.println("Sélection annulée!");
        } else {
            try {
                int index = Integer.parseInt(input);
                jeu.selectionnerCarte(index);
                System.out.println("Carte " + index + " sélectionnée!");
                System.out.println("Cartes sélectionnées: " + jeu.getCartesSelectionnees().size() + "/3");
            } catch (NumberFormatException e) {
                System.out.println("Entrée invalide!");
            } catch (IllegalArgumentException e) {
                System.out.println("Index de carte invalide!");
            }
        }
    }

    /**
     * Vérifie le trio sélectionné
     */
    private void verifierTrio() {
        if (jeu.getCartesSelectionnees().size() != 3) {
            System.out.println("⚠ Vous devez sélectionner 3 cartes!");
            return;
        }

        if (jeu.verifierTrio()) {
            System.out.println("✓ Bravo! Vous avez trouvé un trio!");
            System.out.println("  +1 point pour " + jeu.getJoueurActuel().getNom());
        } else {
            System.out.println("✗ Ce n'est pas un trio valide...");
        }
    }

    /**
     * Affiche les résultats finaux
     */
    private void afficherResultats() {
        System.out.println("\n" + "═".repeat(50));
        System.out.println("                 PARTIE TERMINÉE!");
        System.out.println("═".repeat(50));

        System.out.println("\nRésultats finaux:");
        List<Joueur> joueursTries = new ArrayList<>(jeu.getJoueurs());
        joueursTries.sort((j1, j2) -> Integer.compare(j2.getScore(), j1.getScore()));

        int position = 1;
        for (Joueur j : joueursTries) {
            System.out.println(position + ". " + j.getNom() + ": " + j.getScore() + " points");
            position++;
        }

        System.out.println("\n🎉 Gagnant: " + joueursTries.get(0).getNom() + "\n");
    }

    /**
     * Lit un entier avec validation
     */
    private int lireEntier(int min, int max) {
        while (true) {
            try {
                int val = Integer.parseInt(scanner.nextLine().trim());
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.print("Veuillez entrer un nombre entre " + min + " et " + max + ": ");
            } catch (NumberFormatException e) {
                System.out.print("Entrée invalide! Réessayez: ");
            }
        }
    }

    /**
     * Point d'entrée principal
     */
    public static void main(String[] args) {
        TrioConsole console = new TrioConsole();
        console.lancer();
    }
}
