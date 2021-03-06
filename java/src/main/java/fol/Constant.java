package fol;

public class Constant extends Symbol {

    String symbol;
    String __visit_name__ = "constant";

    public String getSymbol() {
        return symbol;
    }

    public Constant(String symbol) {
        super();
        this.symbol = symbol.substring(0, 1).replace("[^A-z]", "x").toLowerCase() + symbol.substring(1);
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