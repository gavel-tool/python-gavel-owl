package fol;

public class BinaryConnective extends LogicElement {
    /*
    CONJUNCTION = 0
    DISJUNCTION = 1
    BIIMPLICATION = 2
    IMPLICATION = 3
    REVERSE_IMPLICATION = 4
    SIMILARITY = 5
    NEGATED_CONJUNCTION = 6
    NEGATED_DISJUNCTION = 7
    EQ = 8
    NEQ = 9
    APPLY = 10
    PRODUCT = 12
    UNION = 13
    GENTZEN_ARROW = 14
    ASSIGN = 15
    ARROW = 16
     */
    int id;
    String __visit_name__ = "binary_connective";

    public BinaryConnective(int id) {
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
            return "&";
        } else if (id == 1) {
            return "|";
        } else if (id == 2) {
            return "<=>";
        } else if (id == 3) {
            return "=>";
        } else if (id == 4) {
            return "<=";
        } else if (id == 5) {
            return "<~>";
        } else if (id == 6) {
            return "~&";
        } else if (id == 7) {
            return "~|";
        } else if (id == 8) {
            return "=";
        } else if (id == 9) {
            return "!=";
        } else if (id == 10) {
            return "@";
        } else if (id == 12) {
            return "*";
        } else if (id == 13) {
            return "+";
        } else if (id == 14) {
            return "-->";
        } else if (id == 15) {
            return ":=";
        } else if (id == 16) {
            return ">";
        } else {
            return "BinaryConnective(" + id + ")";
        }
    }
}