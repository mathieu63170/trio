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
        if (plateau == null || action == null) {
            return plateau;
        }

        // Aiguillage selon le type d'action
        if (action instanceof ActionTrio a) {
            return appliquerActionTrio(plateau, a);
            
        } else if (action instanceof ActionMillieu a) {
            return appliquerActionMillieu(plateau, a);
            
        } else if (action instanceof ActionMax a) {
            return appliquerActionMax(plateau, a);
            
        } else if (action instanceof ActionMin a) {
            return appliquerActionMin(plateau, a);
        }

        return plateau;
    }

    // === TRIO: Vérifier 3 cartes identiques (du milieu ou de la main, y compris cartes révélées) ===
    private Plateau appliquerActionTrio(Plateau plateau, ActionTrio action) {
        List<Integer> idsCartes = action.getIdsCartes();
        List<Integer> proprietaires = action.getProprietaires();
        
        if (idsCartes == null || idsCartes.size() != 3) {
            return plateau;
        }
        
        // ✅ CHERCHER LES CARTES PAR ID PARTOUT (pas seulement chez le propriétaire)
        // Car après les changements du plateau, le propriétaire peut avoir changé
        List<Carte> cartesTrio = new ArrayList<>();
        for (int i = 0; i < idsCartes.size(); i++) {
            int idCarte = idsCartes.get(i);
            // Chercher la carte n'importe où dans le plateau
            Carte carte = trouverCarteParIDPartout(plateau, idCarte);
            if (carte == null) {
                System.out.println("   ❌ Carte ID " + idCarte + " introuvable!");
                return plateau;
            }
            cartesTrio.add(carte);
        }
        
        // Vérifier que les 3 cartes ont la même valeur
        int valeur = cartesTrio.get(0).getValeur();
        for (Carte c : cartesTrio) {
            if (c.getValeur() != valeur) {
                return plateau;
            }
        }
        
        System.out.println("   ✅ TRIO DE " + valeur + " VALIDE par Joueur " + action.getIdJoueur());
        
        Joueur joueur = trouverJoueur(plateau, action.getIdJoueur());
        if (joueur != null) {
            // Créer le trio
            Trio trio = new Trio(new ArrayList<>(cartesTrio), valeur);
            joueur.getTrios().add(trio);
            
            // Supprimer les cartes selon où elles se trouvent RÉELLEMENT
            for (int i = 0; i < cartesTrio.size(); i++) {
                Carte carte = cartesTrio.get(i);
                
                System.out.println("      → Suppression carte " + carte.getValeur() + " (ID: " + carte.getId() + ")");
                
                // ✅ Chercher où la carte se trouve RÉELLEMENT et la supprimer
                // D'abord le milieu
                if (plateau.getMillieu() != null && plateau.getMillieu().removeIf(c -> c.getId() == carte.getId())) {
                    System.out.println("        Résultat: SUPPRIMÉE du MILIEU");
                    continue;
                }
                
                // Sinon, chercher dans tous les joueurs
                boolean trouvee = false;
                if (plateau.getJoueurs() != null) {
                    for (Joueur j : plateau.getJoueurs()) {
                        if (j.getDeck() != null && j.getDeck().removeIf(c -> c.getId() == carte.getId())) {
                            System.out.println("        Résultat: SUPPRIMÉE de la main du joueur " + j.getId());
                            trouvee = true;
                            break;
                        }
                    }
                }
                
                // Supprimer des cartes révélées aussi
                if (plateau.getCartesRevelees() != null && plateau.getCartesRevelees().removeIf(cr -> cr.getCarte().getId() == carte.getId())) {
                    System.out.println("        Résultat: SUPPRIMÉE des cartes révélées");
                }
                
                if (!trouvee) {
                    System.out.println("        ⚠️  Carte ID " + carte.getId() + " non trouvée pour suppression");
                }
            }
            
            System.out.println("      → Joueur " + action.getIdJoueur() + " a " + joueur.getTrios().size() + " trio(s), Main: " + joueur.getDeck().size() + " cartes, Milieu: " + plateau.getMillieu().size() + " cartes, Révélées: " + plateau.getCartesRevelees().size());
            
            // Vérifier victoire (3 trios pour gagner)
            if (joueur.getTrios().size() >= 3) {
                plateau.setPhaseActuelle(Phase.FIN_PARTIE);
                plateau.setGagnant(action.getIdJoueur());
                System.out.println("   🎉 VICTOIRE DU JOUEUR " + action.getIdJoueur());
            }
        }
        return plateau;
    }

    // === MILIEU: Prendre une carte du milieu ===
    private Plateau appliquerActionMillieu(Plateau plateau, ActionMillieu action) {
        int place = action.getPlace();
        System.out.println("🎯 Prise au MILIEU (place " + place + ") par Joueur " + action.getIdJoueur());
        
        List<Carte> cartes = plateau.getMillieu();
        if (place < 0 || place >= cartes.size()) {
            System.out.println("   ❌ Place invalide");
            return plateau;
        }
        
        Joueur joueur = trouverJoueur(plateau, action.getIdJoueur());
        if (joueur != null) {
            Carte c = cartes.remove(place);
            joueur.getDeck().add(c);
            System.out.println("   ✓ Joueur " + action.getIdJoueur() + " a pris: " + c.getValeur());
        }
        
        return plateau;
    }

    // === MAX: Demander la plus grande carte ===
    private Plateau appliquerActionMax(Plateau plateau, ActionMax action) {
        // ATTENTION: Cette méthode ne devrait plus être appelée directement
        // Le serveur gère MAX/MIN via traiterActionMaxMin pour révélation uniquement
        System.out.println("⚠️  ActionMax reçue directement (inattendu)");
        return plateau;
    }

    // === MIN: Demander la plus petite carte ===
    private Plateau appliquerActionMin(Plateau plateau, ActionMin action) {
        // ATTENTION: Cette méthode ne devrait plus être appelée directement
        // Le serveur gère MAX/MIN via traiterActionMaxMin pour révélation uniquement
        System.out.println("⚠️  ActionMin reçue directement (inattendu)");
        return plateau;
    }

    // === UTILITAIRES ===
    private Joueur trouverJoueur(Plateau p, int id) {
        if (p == null || p.getJoueurs() == null) return null;
        return p.getJoueurs().stream()
                .filter(j -> j.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Trouve une carte par son ID et son propriétaire
     */
    private Carte trouverCarteParID(Plateau plateau, int idCarte, int proprietaire) {
        // Le milieu peut être encodé comme 0 ou -1
        if (proprietaire <= 0) {
            // Carte du milieu
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        } else {
            // Carte d'un joueur
            Joueur joueur = trouverJoueur(plateau, proprietaire);
            if (joueur != null && joueur.getDeck() != null) {
                for (Carte c : joueur.getDeck()) {
                    if (c.getId() == idCarte) {
                        return c;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Trouve une carte par son ID n'importe où dans le plateau
     * (dans le milieu OU dans n'importe quel joueur)
     * Utilisé pour appliquer les trios car le propriétaire peut avoir changé
     */
    private Carte trouverCarteParIDPartout(Plateau plateau, int idCarte) {
        // Chercher dans le milieu
        if (plateau.getMillieu() != null) {
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        }
        
        // Chercher dans tous les joueurs
        if (plateau.getJoueurs() != null) {
            for (Joueur joueur : plateau.getJoueurs()) {
                if (joueur.getDeck() != null) {
                    for (Carte c : joueur.getDeck()) {
                        if (c.getId() == idCarte) {
                            return c;
                        }
                    }
                }
            }
        }
        
        return null;
    }
}