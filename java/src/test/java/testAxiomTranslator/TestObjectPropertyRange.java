package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestObjectPropertyRange extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLClass testClass1 = df.getOWLClass("Test_1");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");
        OWLObjectPropertyExpression testProperty2 = df.getOWLObjectProperty("Prop2");
        LogicElement[] variables = {var0, var1};

        return Stream.of(
                //test Object Property Range
                Arguments.of(
                        df.getOWLObjectPropertyRangeAxiom(testProperty0, testClass0),
                        new ArrayList<LogicElement>(Arrays.asList(
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var0, var1},
                                        new BinaryFormula(
                                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                                new BinaryConnective(3),
                                                testClass0.accept(new OWLClassExpressionTranslator(var1))
                                        )
                                )))
                )

        );
    }
}