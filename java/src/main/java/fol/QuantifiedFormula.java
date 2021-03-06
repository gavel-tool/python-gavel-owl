package fol;

import java.util.Arrays;

public class QuantifiedFormula extends LogicElement {
    String __visit_name__ = "quantified_formula";
    Quantifier quantifier;
    Variable[] variables;
    LogicElement formula;

    public QuantifiedFormula(Quantifier quantifier, Variable[] variables, LogicElement formula) {
        super();
        this.quantifier = quantifier;
        this.variables = variables;
        this.formula = formula;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    public Quantifier getQuantifier() {
        return quantifier;
    }

    public Variable[] getVariables() {
        return variables;
    }

    public LogicElement getFormula() {
        return formula;
    }

    @Override
    public String toString() {
        return quantifier + Arrays.toString(variables) + ": " + formula;
    }
}