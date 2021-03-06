package fol;

public class Variable extends Symbol {

    String symbol;
    String __visit_name__ = "variable";

    public String getSymbol() {
        return symbol;
    }

    public Variable(String symbol) {
        super();
        this.symbol = symbol.substring(0, 1).replace("[^A-z]", "X").toUpperCase() + symbol.substring(1);
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        return symbol;
    }
}