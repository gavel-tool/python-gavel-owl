package fol;

public class TypedVariable extends LogicElement {

    String name;
    Type vtype;
    String __visit_name__ = "typed_variable";

    public String getName() {
        return name;
    }

    public Type getVType() {
        return vtype;
    }

    public TypedVariable(String name, Type vtype) {
        super();
        this.name = name;
        this.vtype = vtype;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        return name + "(type: " + vtype + ")";
    }
}