package testAxiomTranslator;

import fol.LogicElement;
import fol.UnaryConnective;
import fol.UnaryFormula;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLIndividualTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestNegativeObjectPropertyAssertion extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLIndividual testIndividual0 = df.getOWLNamedIndividual("testInd0");
        OWLIndividual testIndividual1 = df.getOWLNamedIndividual("testInd1");
        LogicElement[] variables = {var0};

        return Stream.of(
            //test Negative Property Assertion
            Arguments.of(
                df.getOWLNegativeObjectPropertyAssertionAxiom(testProperty0, testIndividual0, testIndividual1),
                new ArrayList<LogicElement>(Arrays.asList(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        testProperty0.accept(new OWLPropertyExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()),
                            testIndividual1.accept(new OWLIndividualTranslator()))
                        )
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()))
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual1.accept(new OWLIndividualTranslator()))
                    )
                ))
            )

        );
    }

}