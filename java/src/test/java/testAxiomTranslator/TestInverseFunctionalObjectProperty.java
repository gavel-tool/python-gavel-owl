package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class TestInverseFunctionalObjectProperty extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {var0, var1, var2};

        return Stream.of(
            //test Inverse Functional Properties
            Arguments.of(
                df.getOWLInverseFunctionalObjectPropertyAxiom(testProperty0),
                new ArrayList<LogicElement>(Collections.singletonList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0, var1, var2},
                        new BinaryFormula(
                            new BinaryFormula(
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var1, var0)),
                                new BinaryConnective(0),
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var2, var0))
                            ),
                            new BinaryConnective(3),
                            new BinaryFormula(
                                var1,
                                new BinaryConnective(8),
                                var2
                            )
                        )
                    )))
            )
        );
    }
}