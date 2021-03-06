package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class TestFunctionalDataProperty extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression prop0 = df.getOWLDataProperty("prop0");

        return Stream.of(
            Arguments.of(
                df.getOWLFunctionalDataPropertyAxiom(prop0),
                new ArrayList<LogicElement>(Collections.singletonList(
                    new QuantifiedFormula(
                        new Quantifier(0), // universal quantifier
                        new Variable[]{var0, var1, var2},
                        new BinaryFormula(
                            new BinaryFormula(
                                prop0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                new BinaryConnective(0), // conjunction
                                prop0.accept(new OWLPropertyExpressionTranslator(var0, var2))
                            ),
                            new BinaryConnective(3), // implication
                            new BinaryFormula(var1, new BinaryConnective(8), var2) // 8 = EQ
                        )
                    )
                ))
            )
        );
    }
}