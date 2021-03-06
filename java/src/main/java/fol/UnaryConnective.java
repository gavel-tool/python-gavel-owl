package fol;


public class UnaryConnective extends LogicElement {
    int id;
    String __visit_name__ = "unary_connective";

    public UnaryConnective(int type) {
        super();
        this.id = type;
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
            return "~";
        } else {
            return "UnaryPredicate(" + id + ")";
        }
    }
}
