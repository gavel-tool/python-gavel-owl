package testClassExpressionTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.Symbol;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLLiteral;
import translation.OWLDataTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.stream.Stream;

public class TestDataHasValue extends StandardClassExpressionTest {

    public static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression dpe = df.getOWLDataProperty("dpe");
        OWLLiteral lit = df.getOWLLiteral(4.68);

        return Stream.of(
            Arguments.of(
                df.getOWLDataHasValue(dpe, lit),
                new BinaryFormula(
                    dpe.accept(new OWLPropertyExpressionTranslator(z, lit.accept(new OWLDataTranslator()))),
                    new BinaryConnective(0),
                    df.getTopDatatype().accept(new OWLDataTranslator((Symbol) lit.accept(new OWLDataTranslator())))
                )
            )
        );
    }
}
