package fol;


public class Quantifier extends LogicElement {
    int id; // UNIVERSAL == 0, EXISTENTIAL == 1
    String __visit_name__ = "quantifier";

    public Quantifier(int id) {
        super();
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        if (id == 0) {
            return "\\forall"; // \u2200
        } else {
            return "\\exists"; // \u2203
        }
    }
}
