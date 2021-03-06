package fol;

import java.util.Arrays;
import java.util.stream.Collectors;

public class PredicateExpression extends LogicElement {
    String __visit_name__ = "predicate_expression";
    String predicate;
    LogicElement[] arguments;

    public PredicateExpression(String predicate, LogicElement[] arguments) {
        super();
        this.predicate = predicate.substring(0, 1).
            replaceAll("[^A-z]", "p" + predicate.charAt(0)).toLowerCase() + predicate.substring(1);
        this.arguments = arguments;
    }

    public String getPredicate() {
        return predicate;
    }

    public LogicElement[] getArguments() {
        return arguments;
    }

    @Override
    public String getVisitName() {
        return __visit_name__;
    }

    @Override
    public String toString() {
        String argString = Arrays.stream(arguments).map(x -> x + ", ").collect(Collectors.joining());
        argString = argString.substring(0, argString.lastIndexOf(","));
        return predicate + "(" + argString + ")";
    }
}