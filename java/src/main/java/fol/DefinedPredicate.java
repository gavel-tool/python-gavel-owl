package fol;

public class DefinedPredicate extends LogicElement {
    int id;
    String __visit_name__ = "defined_predicate";

    public DefinedPredicate(int id) {
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
            return "DISTINCT";
        } else if (id == 1) {
            return "LESS";
        } else if (id == 2) {
            return "LESS_EQ";
        } else if (id == 3) {
            return "GREATER";
        } else if (id == 4) {
            return "GREATER_EQ";
        } else if (id == 5) {
            return "IS_INT";
        } else if (id == 6) {
            return "IS_RAT";
        } else if (id == 7) {
            return "BOX_P";
        } else if (id == 8) {
            return "BOX_I";
        } else if (id == 9) {
            return "BOX_INT";
        } else if (id == 10) {
            return "BOX";
        } else if (id == 11) {
            return "DIA_P";
        } else if (id == 12) {
            return "DIA_I";
        } else if (id == 13) {
            return "DIA_INT";
        } else if (id == 14) {
            return "DIA";
        } else {
            return "DefinedPredicate(" + id + ")";
        }
    }
}
