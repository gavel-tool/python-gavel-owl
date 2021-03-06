package fol;

public class Type extends LogicElement {
    String __visit_name__ = "type";
    String name;

    public Type(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        return name;
    }
}
