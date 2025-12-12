package commun.action;

public class ActionMillieu extends Action {
    private int place;
    private static final long serialVersionUID = 1L; 
    
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
