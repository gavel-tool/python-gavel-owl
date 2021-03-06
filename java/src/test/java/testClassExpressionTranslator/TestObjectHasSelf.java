package testClassExpressionTranslator;

import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLPropertyExpressionTranslator;

import java.util.stream.Stream;

public class TestObjectHasSelf extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLIndividual testIndividual0 = df.getOWLNamedIndividual("Individual0");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {z};

        return Stream.of(
                //test has self
                Arguments.of(
                        df.getOWLObjectHasSelf(testProperty0),
                        testProperty0.accept(new OWLPropertyExpressionTranslator(z, z))
                )
        );
    }
}