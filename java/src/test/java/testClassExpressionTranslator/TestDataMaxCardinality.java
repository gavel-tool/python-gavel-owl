package testClassExpressionTranslator;

import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;

import java.util.stream.Stream;

public class TestDataMaxCardinality extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataRange range = df.getOWLDatatype("data");
        OWLDataPropertyExpression testProperty0 = df.getOWLDataProperty("prop0");

        return Stream.of(
            //test MaxCardinality
            Arguments.of(
                df.getOWLDataMaxCardinality(1, testProperty0, range),
                "\\forall[X0, X1]: ((prop0(Z, X0) & (data(X0) & (prop0(Z, X1) & data(X1)))) => ~(X0 != X1))"
            ),
            Arguments.of(
                df.getOWLDataMaxCardinality(2, testProperty0, range),
                "\\forall[X0, X1, X2]: ((prop0(Z, X0) & (data(X0) & (prop0(Z, X1) & (data(X1) & (prop0(Z, X2) " +
                    "& data(X2)))))) => ~((X0 != X1) & ((X0 != X2) & (X1 != X2))))"
            )
        );
    }
}
