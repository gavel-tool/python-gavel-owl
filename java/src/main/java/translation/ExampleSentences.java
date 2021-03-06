package translation;

import fol.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;

public class ExampleSentences {
    public static ArrayList<LogicElement> addSentences() {
        ArrayList<LogicElement> sentences = new ArrayList<>();
        sentences.add(new BinaryFormula(new Variable("a"), new BinaryConnective(1), new Variable("b")));
        UnaryFormula uf = new UnaryFormula(new UnaryConnective(0), new Constant("c"));
        sentences.add(uf);
        sentences.add(new BinaryFormula(uf, new BinaryConnective(0), uf));
        return sentences;
    }

    public static void main(String[] args) {
        testPropertyExpressionChainTranslator();
    }

    public static void testPropertyExpressionChainTranslator() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<OWLObjectPropertyExpression> chain = new ArrayList<>();
        chain.add(df.getOWLObjectProperty("pr1"));
        chain.add(df.getOWLObjectProperty("pr2"));
        chain.add(df.getOWLObjectProperty("pr3"));
        ArrayList<LogicElement> translation = df.getOWLSubPropertyChainOfAxiom(
                chain, df.getOWLObjectProperty("super")).accept(new OWLAxiomTranslator());
        System.out.println(translation);
    }

    public static void testDisjointClassesTranslator() {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();
        ArrayList<LogicElement> translation = df.getOWLDisjointClassesAxiom(
                df.getOWLClass("c1"),
                df.getOWLClass("c2"),
                df.getOWLClass("c3")).accept(new OWLAxiomTranslator());
        System.out.println(translation);
    }

    public static void testToString() {
        Quantifier q = new Quantifier(0);
        System.out.println(q);
        QuantifiedFormula quantifiedFormula = new QuantifiedFormula(
                q,
                new Variable[]{new Variable("x"), new Variable("y")},
                new BinaryFormula(
                        new PredicateExpression(
                                "Pred",
                                new LogicElement[]{new Constant("c"), new Variable("x")}),
                        new BinaryConnective(3),
                        new PredicateExpression(
                                new DefinedPredicate(2).toString(),
                                new LogicElement[]{new Constant("d"), new Variable("y")})));
        System.out.println(quantifiedFormula);
    }
}