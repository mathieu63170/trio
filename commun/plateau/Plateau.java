package commun.plateau;

import java.util.List;

public class Plateau {
    private List<Joueur> joueurs;
    private List<Carte> millieu;
    private int joueurActuel;
    private Phase phaseActuelle;
    private int gagnant;

    public Plateau(List<Joueur> joueurs, List<Carte> millieu, int joueurActuel, 
                   Phase phaseActuelle, int gagnant) {
        this.joueurs = joueurs;
        this.millieu = millieu;
        this.joueurActuel = joueurActuel;
        this.phaseActuelle = phaseActuelle;
        this.gagnant = gagnant;
    }

    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    public void setJoueurs(List<Joueur> joueurs) {
        this.joueurs = joueurs;
    }

    public List<Carte> getMillieu() {
        return millieu;
    }

    public void setMillieu(List<Carte> millieu) {
        this.millieu = millieu;
    }

    public int getJoueurActuel() {
        return joueurActuel;
    }

    public void setJoueurActuel(int joueurActuel) {
        this.joueurActuel = joueurActuel;
    }

    public Phase getPhaseActuelle() {
        return phaseActuelle;
    }

    public void setPhaseActuelle(Phase phaseActuelle) {
        this.phaseActuelle = phaseActuelle;
    }

    public int getGagnant() {
        return gagnant;
    }

    public void setGagnant(int gagnant) {
        this.gagnant = gagnant;
    }

    public List<Carte> getCarteRevelee(){
        List<Carte> liste = new java.util.ArrayList<>();
        for(int i= 0; i<joueurs.size(); i++){
            Joueur j= joueurs.get(i);
            for(int y=0; y<j.getDeck().size(); y++){
                Carte c= j.getDeck().get(y);
                if(c.isRevelee()){
                    liste.add(c);
                }
            }
        }
        return liste;
    }
}
