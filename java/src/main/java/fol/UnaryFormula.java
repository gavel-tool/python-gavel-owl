package fol;

public class UnaryFormula extends LogicElement {
    UnaryConnective con;
    LogicElement form;
    String __visit_name__ = "unary_formula";

    public UnaryFormula(UnaryConnective con, LogicElement form) {
        super();
        this.con = con;
        this.form = form;
    }

    public UnaryConnective getConnective() {
        return con;
    }

    public LogicElement getFormula() {
        return form;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        return "" + con + form;
    }
}