package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import translation.OWLDataTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class TestDataPropertyRange extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression prop0 = df.getOWLDataProperty("prop0");
        OWLDataRange range1 = df.getOWLDatatype("data1");

        return Stream.of(
            Arguments.of(
                df.getOWLDataPropertyRangeAxiom(prop0, range1),
                new ArrayList<LogicElement>(Collections.singletonList(
                    new QuantifiedFormula(
                        new Quantifier(0), // universal quantifier
                        new Variable[]{var0, var1},
                        new BinaryFormula(
                            prop0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                            new BinaryConnective(3), // implication
                            range1.accept(new OWLDataTranslator(var1))
                        )
                    )
                ))
            )
        );
    }
}