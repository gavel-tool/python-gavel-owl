package testClassExpressionTranslator;

import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import java.util.stream.Stream;

public class TestObjectMinCardinality extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("class0");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("prop0");
        LogicElement[] variables = {z};

        return Stream.of(
            //test MinCardinality
            Arguments.of(
                df.getOWLObjectMinCardinality(3, testProperty0, testClass0),
                "\\exists[X0, X1, X2]: ((X0 != X1) & ((X0 != X2) & ((X1 != X2) & (prop0(Z, X0) & (class0(X0)" +
                    " & (prop0(Z, X1) & (class0(X1) & (prop0(Z, X2) & class0(X2)))))))))"
            ),
            Arguments.of(
                        df.getOWLObjectMinCardinality(2, testProperty0, testClass0),
                        "\\exists[X0, X1]: ((X0 != X1) & (prop0(Z, X0) & (class0(X0) & (prop0(Z, X1) & class0(X1)))))"
                )
        );
    }
}
