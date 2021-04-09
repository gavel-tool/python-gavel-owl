package testClassExpressionTranslator;

import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;

import java.util.stream.Stream;

public class TestDataExactCardinality extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataRange range = df.getOWLDatatype("class0");
        OWLDataPropertyExpression testProperty0 = df.getOWLDataProperty("prop0");

        return Stream.of(
            //test Exact Cardinality
            Arguments.of(
                df.getOWLDataExactCardinality(3, testProperty0, range),
                "(\\exists[X0, X1, X2]: ((X0 != X1) & ((X0 != X2) & ((X1 != X2) & (prop0(Z, X0) & " +
                    "(class0(X0) & (prop0(Z, X1) & (class0(X1) & (prop0(Z, X2) & class0(X2))))))))) & " +
                    "\\forall[X3, X4, X5, X6]: ((prop0(Z, X3) & (class0(X3) & (prop0(Z, X4) & (class0(X4)" +
                    " & (prop0(Z, X5) & (class0(X5) & (prop0(Z, X6) & class0(X6)))))))) => ~((X3 != X4)" +
                    " & ((X3 != X5) & ((X3 != X6) & ((X4 != X5) & ((X4 != X6) & (X5 != X6))))))))"
            )

        );
    }

}
