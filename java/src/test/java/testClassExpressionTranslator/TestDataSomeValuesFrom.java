package testClassExpressionTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import translation.OWLDataTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.stream.Stream;

public class TestDataSomeValuesFrom extends StandardClassExpressionTest {

    public static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression dpe = df.getOWLDataProperty("dpe");
        OWLDataRange dr = df.getOWLDatatype("dr");

        return Stream.of(
            Arguments.of(
                df.getOWLDataSomeValuesFrom(dpe, dr),
                new QuantifiedFormula(
                    new Quantifier(1), // existential quantifier
                    new Variable[]{x},
                    new BinaryFormula(
                        dpe.accept(new OWLPropertyExpressionTranslator(z, x)),
                        new BinaryConnective(0), // conjunction
                        dr.accept(new OWLDataTranslator(x))
                    )
                )
            )
        );
    }
}
