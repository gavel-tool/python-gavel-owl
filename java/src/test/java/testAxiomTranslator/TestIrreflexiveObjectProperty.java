package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestIrreflexiveObjectProperty extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {var0};

        return Stream.of(
            //test Irreflexive Properties
            Arguments.of(
                df.getOWLIrreflexiveObjectPropertyAxiom(testProperty0),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0},
                        new UnaryFormula(
                            new UnaryConnective(0),
                            testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var0))
                        )

                    )
                ))
            )
        );
    }
}