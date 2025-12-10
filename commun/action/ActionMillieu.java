package commun.action;

public class ActionMillieu extends Action {
    private int place;

    public ActionMillieu(int idJoueur, int place) {
        super(idJoueur);
        this.place = place;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }
}
