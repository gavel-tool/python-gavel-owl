package fol;

public class Subtype extends LogicElement {

    Type left;
    Type right;
    String __visit_name__ = "subtype";

    public Type getLeft() {
        return left;
    }

    public Type getRight() {
        return right;
    }

    public Subtype(Type left, Type right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }


    @Override
    public String toString() {
        return "subtype(" + left + ", " + right + ")";
    }
}