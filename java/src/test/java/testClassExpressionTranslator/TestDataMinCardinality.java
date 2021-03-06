package testClassExpressionTranslator;

import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;

import java.util.stream.Stream;

public class TestDataMinCardinality extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataRange r = df.getOWLDatatype("data");
        OWLDataPropertyExpression prop0 = df.getOWLDataProperty("prop0");

        return Stream.of(
            //test MinCardinality
            Arguments.of(
                df.getOWLDataMinCardinality(3, prop0, r),
                "\\exists[X0, X1, X2]: ((X0 != X1) & ((X0 != X2) & ((X1 != X2) & (prop0(Z, X0) & (data(X0)" +
                    " & (prop0(Z, X1) & (data(X1) & (prop0(Z, X2) & data(X2)))))))))"
            ),
            Arguments.of(
                df.getOWLDataMinCardinality(2, prop0, r),
                "\\exists[X0, X1]: ((X0 != X1) & (prop0(Z, X0) & (data(X0) & (prop0(Z, X1) & data(X1)))))"
            )
        );
    }
}