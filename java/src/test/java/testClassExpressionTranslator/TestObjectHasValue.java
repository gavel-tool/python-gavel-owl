package testClassExpressionTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLIndividualTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.stream.Stream;

public class TestObjectHasValue extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLIndividual testIndividual0 = df.getOWLNamedIndividual("Individual0");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {z};

        return Stream.of(
            //test has value
            Arguments.of(
                df.getOWLObjectHasValue(testProperty0, testIndividual0),
                new BinaryFormula(
                    testProperty0.accept(
                        new OWLPropertyExpressionTranslator(z, testIndividual0.accept(new OWLIndividualTranslator()))),
                    new BinaryConnective(0),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator())))
                )
            )
        );
    }
}