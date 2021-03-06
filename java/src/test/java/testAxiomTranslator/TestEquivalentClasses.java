package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestEquivalentClasses extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLClass testClass1 = df.getOWLClass("Test_1");
        OWLClass testClass2 = df.getOWLClass("Test_2");
        OWLClass testClass3 = df.getOWLClass("Test_3");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");
        LogicElement[] variables = {var0};
        OWLTranslator.variableCounter = 1;

        return Stream.of(
            //test Equivalent Classes
            Arguments.of(
                df.getOWLEquivalentClassesAxiom(testClass0, testClass1),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0},
                        new BinaryFormula(
                            testClass0.accept(new OWLClassExpressionTranslator(var0)),
                            new BinaryConnective(2),
                            testClass1.accept(new OWLClassExpressionTranslator(var0))
                        )
                    )))
            ),
            //test Equivalent Classes nested
            Arguments.of(
                df.getOWLEquivalentClassesAxiom(testClass0, (df.getOWLObjectSomeValuesFrom(testProperty0, testClass1))),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0},
                        new BinaryFormula(
                            testClass0.accept(new OWLClassExpressionTranslator(var0)),
                            new BinaryConnective(2),
                            df.getOWLObjectSomeValuesFrom(testProperty0, testClass1).accept(new OWLClassExpressionTranslator(var0))
                        )
                    )))
            ) //,
            //  Arguments.of(
            //           df.getOWLEquivalentClassesAxiom(testClass0, testClass1, testClass2, testClass3),
            //           "[∀[x]: (Test_0(x)) <=> (Test_1(x)), ∀[x]: (Test_0(x)) <=> (Test_2(x)), ∀[x]: (Test_0(x)) <=> (Test_3(x)), ∀[x]: (Test_1(x)) <=> (Test_2(x)), ∀[x]: (Test_1(x)) <=> (Test_3(x)), ∀[x]: (Test_2(x)) <=> (Test_3(x))]"
            //   )
            //TODO: get more than one class tested

        );
    }


}