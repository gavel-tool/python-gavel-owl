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

public class TestInverseObjectProperties extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");
        OWLObjectPropertyExpression testProperty2 = df.getOWLObjectProperty("Prop2");
        LogicElement[] variables = {var0, var1};

        return Stream.of(
            //test Inverse
            Arguments.of(
                df.getOWLInverseObjectPropertiesAxiom(testProperty0, testProperty1),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0, var1},
                        new BinaryFormula(
                            testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                            new BinaryConnective(2),
                            testProperty1.accept(new OWLPropertyExpressionTranslator(var1, var0))
                        )
                    )))
            )
        );
    }
}