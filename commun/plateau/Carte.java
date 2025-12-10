package commun.plateau;

public class Carte {
    private int valeur;
    private boolean revelee;

    public Carte(int valeur, boolean revelee) {
        this.valeur = valeur;
        this.revelee = revelee;
    }

    public int getValeur() {
        return valeur;
    }

    public void setValeur(int valeur) {
        this.valeur = valeur;
    }

    public boolean isRevelee() {
        return revelee;
    }

    public void setRevelee(boolean revelee) {
        this.revelee = revelee;
    }
}
