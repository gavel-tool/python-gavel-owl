package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLAxiomTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFunctionalObjectProperty extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {var0, var1, var2};

        return Stream.of(
            //test Disjoint Properties
            Arguments.of(
                df.getOWLFunctionalObjectPropertyAxiom(testProperty0),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0, var1, var2},
                        new BinaryFormula(
                            new BinaryFormula(
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                new BinaryConnective(0),
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var2))
                            ),
                            new BinaryConnective(3),
                            new BinaryFormula(
                                var1,
                                new BinaryConnective(8),
                                var2
                            )
                        )
                    )))
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases") //pairs of OWL-expression and FOL-expression
    public void testFunctionalProperty(OWLAxiom owl, ArrayList<LogicElement> expected) {
        ArrayList<LogicElement> translated = owl.accept(new OWLAxiomTranslator());
        System.out.println(expected.toString());
        assertEquals(expected.toString(), translated.toString());
    }
}