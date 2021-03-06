package testClassExpressionTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.stream.Stream;

public class TestObjectSomeValuesFrom extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Class0");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");

        return Stream.of(
                //test SomeValuesFrom
                Arguments.of(
                        df.getOWLObjectSomeValuesFrom(testProperty0, testClass0),
                        new QuantifiedFormula(
                                new Quantifier(1),
                                new Variable[]{x},
                                new BinaryFormula(
                                        testProperty0.accept(new OWLPropertyExpressionTranslator(z, x)),
                                        new BinaryConnective(0),
                                        testClass0.accept(new OWLClassExpressionTranslator(x))
                                )
                        )
                )

        );
    }
}