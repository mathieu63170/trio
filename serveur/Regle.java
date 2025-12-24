package serveur;

import commun.action.*;
import commun.plateau.*;
import java.util.ArrayList;
import java.util.List;

public class Regle {

    /**
     * Méthode principale qui reçoit une action et modifie le plateau.
     */
    public Plateau appliquer(Plateau plateau, Action action) {
        // 1. Vérification basique
        if (!verifAction(plateau, action)) {
            System.out.println("Action invalide ou mauvais joueur !");
            return plateau;
        }

        // 2. Aiguillage vers la bonne méthode selon le type d'action
        if (action instanceof ActionMin) {
            return appliquerActionMin(plateau, (ActionMin) action);
            
        } else if (action instanceof ActionMax) {
            return appliquerActionMax(plateau, (ActionMax) action);
            
        } else if (action instanceof ActionMillieu) {
            return appliquerActionMillieu(plateau, (ActionMillieu) action);
            
        } else if (action instanceof ActionTrio) {
            return verifierResultatTrio(plateau, action.getIdJoueur());
        }

        return plateau;
    }

    // --- GESTION DES ACTIONS ---

    private Plateau appliquerActionMin(Plateau plateau, ActionMin action) {
        Joueur cible = trouverJoueur(plateau, action.getIdCible());

        if (cible != null && !cible.getDeck().isEmpty()) {
            // La main est triée : le MIN est à l'index 0
            Carte c = cible.getDeck().get(0);
            c.setRevelee(true);
            System.out.println("MIN révélé chez J" + action.getIdCible() + " : " + c.getValeur());
        }
        return plateau;
    }

    private Plateau appliquerActionMax(Plateau plateau, ActionMax action) {
        Joueur cible = trouverJoueur(plateau, action.getIdCible());

        if (cible != null && !cible.getDeck().isEmpty()) {
            // La main est triée : le MAX est à la fin de la liste
            int dernierIndex = cible.getDeck().size() - 1;
            Carte c = cible.getDeck().get(dernierIndex);
            c.setRevelee(true);
            System.out.println("MAX révélé chez J" + action.getIdCible() + " : " + c.getValeur());
        }
        return plateau;
    }

    private Plateau appliquerActionMillieu(Plateau plateau, ActionMillieu action) {
        int place = action.getPlace();
        if (place >= 0 && place < plateau.getMillieu().size()) {
            Carte c = plateau.getMillieu().get(place);
            c.setRevelee(true);
            System.out.println("Carte du milieu révélée : " + c.getValeur());
        }
        return plateau;
    }

    private Plateau verifierResultatTrio(Plateau plateau, int idJoueur) {
        List<Carte> revelees = plateau.getCarteRevelee();

        // On vérifie si les cartes révélées forment un trio (3 cartes identiques)
        if (verifierTrio(revelees)) {
            System.out.println(">>> BRAVO ! Trio de " + revelees.get(0).getValeur() + " validé !");
            
            Joueur gagnant = trouverJoueur(plateau, idJoueur);
            if (gagnant != null) {
                // 1. Ajouter le trio au joueur
                Trio trio = new Trio(new ArrayList<>(revelees), revelees.get(0).getValeur());
                gagnant.getTrios().add(trio);

                // 2. Retirer les cartes du jeu définitivement
                nettoyerPlateau(plateau, revelees);

                // 3. Vérifier les conditions de victoire
                if (gagnant.getTrios().size() >= 3 || trio.getValeur() == 7) {
                    plateau.setPhaseActuelle(Phase.FIN_PARTIE);
                    plateau.setGagnant(idJoueur);
                    System.out.println(">>> VICTOIRE DU JOUEUR " + idJoueur);
                }
            }
        } else {
            System.out.println(">>> RATÉ ! Ce n'est pas un trio.");
            // Si raté, on cache tout et on passe au joueur suivant
            cacherToutesLesCartes(plateau);
            plateau.setJoueurActuel((plateau.getJoueurActuel() + 1) % plateau.getJoueurs().size());
        }
        return plateau;
    }

    // --- UTILITAIRES ---

    private Joueur trouverJoueur(Plateau p, int id) {
        return p.getJoueurs().stream()
                .filter(j -> j.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private void nettoyerPlateau(Plateau plateau, List<Carte> cartesTrio) {
        plateau.getMillieu().removeAll(cartesTrio);
        for (Joueur j : plateau.getJoueurs()) {
            j.getDeck().removeAll(cartesTrio);
        }
    }

    private void cacherToutesLesCartes(Plateau plateau) {
        for (Joueur j : plateau.getJoueurs()) {
            for (Carte c : j.getDeck()) c.setRevelee(false);
        }
        for (Carte c : plateau.getMillieu()) c.setRevelee(false);
    }

    public boolean verifierTrio(List<Carte> cartes) {
        if (cartes == null || cartes.size() != 3) return false;
        int val = cartes.get(0).getValeur();
        for (Carte c : cartes) {
            if (c.getValeur() != val) return false;
        }
        return true;
    }

    public boolean verifAction(Plateau plateau, Action action) {
        return plateau != null && action != null && plateau.getJoueurActuel() == action.getIdJoueur();
    }
}