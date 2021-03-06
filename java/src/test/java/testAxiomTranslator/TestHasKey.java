package testAxiomTranslator;

import fol.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import translation.OWLAxiomTranslator;
import translation.OWLClassExpressionTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(Parameterized.class)
public class TestHasKey {
    private final OWLAxiom owl;
    private final ArrayList<LogicElement> expected;

    public TestHasKey(OWLAxiom owl, ArrayList<LogicElement> expected) {
        super();
        this.owl = owl;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object> provideTestCases() {
        Variable x = new Variable("X0");
        Variable y = new Variable("X1");
        Variable z1 = new Variable("Z1");
        Variable z2 = new Variable("Z2");
        Variable w1 = new Variable("W1");
        Variable w2 = new Variable("W2");

        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass a = df.getOWLClass("A");
        OWLObjectProperty b1 = df.getOWLObjectProperty("B1");
        OWLObjectProperty b2 = df.getOWLObjectProperty("B2");
        OWLDataProperty d1 = df.getOWLDataProperty("D1");
        OWLDataProperty d2 = df.getOWLDataProperty("D2");
        Variable[] variables = {x, y, z1, z2, w1, w2};

        return Arrays.asList(new Object[][]{{df.getOWLHasKeyAxiom(a, b1, b2, d1, d2),
                new ArrayList<LogicElement>(Collections.singletonList(
                        new QuantifiedFormula(
                                new Quantifier(0),
                                variables,
                                new BinaryFormula(
                                        new BinaryFormula(
                                                a.accept(new OWLClassExpressionTranslator(x)),
                                                new BinaryConnective(0), // conjunction
                                                new BinaryFormula(
                                                        a.accept(new OWLClassExpressionTranslator(y)),
                                                        new BinaryConnective(0),
                                                        new BinaryFormula(
                                                                b1.accept(new OWLPropertyExpressionTranslator(x, z1)),
                                                                new BinaryConnective(0),
                                                                new BinaryFormula(
                                                                        b1.accept(new OWLPropertyExpressionTranslator(y, z1)),
                                                                        new BinaryConnective(0),
                                                                        new BinaryFormula(
                                                                                b2.accept(new OWLPropertyExpressionTranslator(x, z2)),
                                                                                new BinaryConnective(0),
                                                                                new BinaryFormula(
                                                                                        b2.accept(new OWLPropertyExpressionTranslator(y, z2)),
                                                                                        new BinaryConnective(0),
                                                                                        new BinaryFormula(
                                                                                                d1.accept(new OWLPropertyExpressionTranslator(x, w1)),
                                                                                                new BinaryConnective(0),
                                                                                                new BinaryFormula(
                                                                                                        d1.accept(new OWLPropertyExpressionTranslator(y, w1)),
                                                                                                        new BinaryConnective(0),
                                                                                                        new BinaryFormula(
                                                                                                                d2.accept(new OWLPropertyExpressionTranslator(x, w2)),
                                                                                                                new BinaryConnective(0),
                                                                                                                d2.accept(new OWLPropertyExpressionTranslator(y, w2))
                                                                                                        )
                                                                                                )
                                                                                        )
                                                                                )
                                                                        )
                                                                )
                                                        )
                                                )
                                        ),
                                        new BinaryConnective(3), // implication
                                        new BinaryFormula(
                                                x,
                                                new BinaryConnective(8), // eq
                                                y
                                        )
                                )
                        ))
                )
        }});
    }

    @Test
    public void testHasKey() {
        ArrayList<LogicElement> translated = owl.accept(new OWLAxiomTranslator());
        assertEquals(expected.toString(), translated.toString());
    }
}
