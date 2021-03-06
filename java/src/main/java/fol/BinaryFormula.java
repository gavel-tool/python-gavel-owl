package fol;

public class BinaryFormula extends LogicElement {
    LogicElement left;
    BinaryConnective op;
    LogicElement right;
    String __visit_name__ = "binary_formula";

    public BinaryFormula(LogicElement left, BinaryConnective op, LogicElement right) {
        super();
        this.left = left;
        this.op = op;
        this.right = right;
    }

    public LogicElement getLeft() {
        return left;
    }

    public LogicElement getRight() {
        return right;
    }

    public void setRight(LogicElement right_) {
        this.right = right_;
    }

    public BinaryConnective getOp() {
        return op;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        return "(" + left + " " + op + " " + right + ")";
    }
}