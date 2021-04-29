package fol;

public class DefinedConstant extends Constant {

    int id;
    String __visit_name__ = "defined_constant";

    public DefinedConstant(int id) {
        super(id == 0 ? "$true" : id == 1 ? "$false" : "defined constant(" + id + ")");
        this.id = id;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }
}
