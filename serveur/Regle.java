package serveur;

import commun.action.*;
import commun.plateau.*;
import java.util.ArrayList;
import java.util.List;

// Contient la logique des règles du jeu et applique les actions sur le plateau
public class Regle {

    
    // Reçoit une action et l'applique au plateau (dispatcher de méthodes)
    public Plateau appliquer(Plateau plateau, Action action) {
        if (plateau == null || action == null) {
            return plateau;
        }

        
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

    
    // Vérifie si les 3 cartes forment un trio valide et met à jour le plateau
    // Vérifie si les 3 cartes constituent un TRIO valide et met à jour le plateau
    private Plateau appliquerActionTrio(Plateau plateau, ActionTrio action) {
        List<Integer> idsCartes = action.getIdsCartes();
        List<Integer> proprietaires = action.getProprietaires();
        
        if (idsCartes == null || idsCartes.size() != 3) {
            return plateau;
        }
        
        
        
        List<Carte> cartesTrio = new ArrayList<>();
        for (int i = 0; i < idsCartes.size(); i++) {
            int idCarte = idsCartes.get(i);
            
            Carte carte = trouverCarteParIDPartout(plateau, idCarte);
            if (carte == null) {
                System.out.println("    Carte ID " + idCarte + " introuvable!");
                return plateau;
            }
            cartesTrio.add(carte);
        }
        
        
        int valeur = cartesTrio.get(0).getValeur();
        for (Carte c : cartesTrio) {
            if (c.getValeur() != valeur) {
                return plateau;
            }
        }
        
        System.out.println("    TRIO DE " + valeur + " VALIDE par Joueur " + action.getIdJoueur());
        
        Joueur joueur = trouverJoueur(plateau, action.getIdJoueur());
        if (joueur != null) {
            
            Trio trio = new Trio(new ArrayList<>(cartesTrio), valeur);
            joueur.getTrios().add(trio);
            
            
            for (int i = 0; i < cartesTrio.size(); i++) {
                Carte carte = cartesTrio.get(i);
                
                System.out.println("      → Suppression carte " + carte.getValeur() + " (ID: " + carte.getId() + ")");
                
                
                
                if (plateau.getMillieu() != null && plateau.getMillieu().removeIf(c -> c.getId() == carte.getId())) {
                    System.out.println("        Résultat: SUPPRIMÉE du MILIEU");
                    continue;
                }
                
                
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
                
                
                if (plateau.getCartesRevelees() != null && plateau.getCartesRevelees().removeIf(cr -> cr.getCarte().getId() == carte.getId())) {
                    System.out.println("        Résultat: SUPPRIMÉE des cartes révélées");
                }
                
                if (!trouvee) {
                    System.out.println("        ️  Carte ID " + carte.getId() + " non trouvée pour suppression");
                }
            }
            
            System.out.println("      → Joueur " + action.getIdJoueur() + " a " + joueur.getTrios().size() + " trio(s), Main: " + joueur.getDeck().size() + " cartes, Milieu: " + plateau.getMillieu().size() + " cartes, Révélées: " + plateau.getCartesRevelees().size());
            
            
            if (joueur.getTrios().size() >= 3) {
                plateau.setPhaseActuelle(Phase.FIN_PARTIE);
                plateau.setGagnant(action.getIdJoueur());
                System.out.println("    VICTOIRE DU JOUEUR " + action.getIdJoueur());
            }
        }
        return plateau;
    }

    
    // Applique l'action de prise d'une carte au milieu pour le joueur
    private Plateau appliquerActionMillieu(Plateau plateau, ActionMillieu action) {
        int place = action.getPlace();
        System.out.println(" Prise au MILIEU (place " + place + ") par Joueur " + action.getIdJoueur());
        
        List<Carte> cartes = plateau.getMillieu();
        if (place < 0 || place >= cartes.size()) {
            System.out.println("    Place invalide");
            return plateau;
        }
        
        Joueur joueur = trouverJoueur(plateau, action.getIdJoueur());
        if (joueur != null) {
            Carte c = cartes.remove(place);
            joueur.getDeck().add(c);
            System.out.println("    Joueur " + action.getIdJoueur() + " a pris: " + c.getValeur());
        }
        
        return plateau;
    }

    
    private Plateau appliquerActionMax(Plateau plateau, ActionMax action) {
        
        
        System.out.println("️  ActionMax reçue directement (inattendu)");
        return plateau;
    }

    
    private Plateau appliquerActionMin(Plateau plateau, ActionMin action) {
        
        
        System.out.println("️  ActionMin reçue directement (inattendu)");
        return plateau;
    }

    
    private Joueur trouverJoueur(Plateau p, int id) {
        if (p == null || p.getJoueurs() == null) return null;
        return p.getJoueurs().stream()
                .filter(j -> j.getId() == id)
                .findFirst()
                .orElse(null);
    }
    
    
    private Carte trouverCarteParID(Plateau plateau, int idCarte, int proprietaire) {
        
        if (proprietaire <= 0) {
            
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        } else {
            
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
    
    
    private Carte trouverCarteParIDPartout(Plateau plateau, int idCarte) {
        
        if (plateau.getMillieu() != null) {
            for (Carte c : plateau.getMillieu()) {
                if (c.getId() == idCarte) {
                    return c;
                }
            }
        }
        
        
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