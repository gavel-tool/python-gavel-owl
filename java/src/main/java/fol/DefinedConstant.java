package fol;

class DefinedConstant extends LogicElement {

    int id;
    String __visit_name__ = "defined_constant";

    public int getId() {
        return id;
    }

    public DefinedConstant(int id) {
        super();
        this.id = id;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        if (id == 0) {
            return "$true";
        } else if (id == 1) {
            return "$false";
        } else {
            return "defined constant(" + id + ")";
        }
    }
}