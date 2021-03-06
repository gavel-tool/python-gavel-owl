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

public class TestEquivalentObjectProperties extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");

        return Stream.of(
            //test SubPropertyOf
            Arguments.of(
                df.getOWLEquivalentObjectPropertiesAxiom(testProperty0, testProperty1),
                new ArrayList<LogicElement>(Collections.singletonList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0, var1},
                        new BinaryFormula(
                            testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                            new BinaryConnective(2),
                            testProperty1.accept(new OWLPropertyExpressionTranslator(var0, var1))
                        )
                    ))
                )
            )
        );
    }
}