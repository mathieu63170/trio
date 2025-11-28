package model;

/**
 * Énumération des formes de cartes dans le jeu Trio
 */
public enum Forme {
    CERCLE("●"),
    CARRE("■"),
    ONDULATION("〰");

    private final String symbole;

    Forme(String symbole) {
        this.symbole = symbole;
    }

    public String getSymbole() {
        return symbole;
    }

    @Override
    public String toString() {
        return symbole;
    }
}
