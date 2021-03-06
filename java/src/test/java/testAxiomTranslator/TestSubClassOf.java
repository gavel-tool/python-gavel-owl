package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLPropertyExpressionTranslator;
import translation.OWLTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;


public class TestSubClassOf extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLClass testClass1 = df.getOWLClass("Test_1");
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");
        OWLClass cajun = df.getOWLClass("Pizza_Cajun");
        OWLObjectPropertyExpression hasTopping = df.getOWLObjectProperty("hasTopping");
        OWLClass mozzarella = df.getOWLClass("MozzarellaTopping");

        OWLTranslator.variableCounter = 1;

        return Stream.of(
                //test SubClassOf
                Arguments.of(
                        df.getOWLSubClassOfAxiom(testClass0, testClass1),
                        new ArrayList<LogicElement>(Collections.singletonList(
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var0},
                                        new BinaryFormula(
                                                testClass0.accept(new OWLClassExpressionTranslator(var0)),
                                                new BinaryConnective(3),
                                                testClass1.accept(new OWLClassExpressionTranslator(var0))
                                        )
                                )
                        ))
                ),
                //test SubClassOf nested
                Arguments.of(
                        df.getOWLSubClassOfAxiom(testClass0, (df.getOWLObjectSomeValuesFrom(testProperty0, df.getOWLObjectSomeValuesFrom(testProperty1, testClass1)))),
                        new ArrayList<LogicElement>(Collections.singletonList(
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var0},
                                        new BinaryFormula(
                                                testClass0.accept(new OWLClassExpressionTranslator(var0)),
                                                new BinaryConnective(3),
                                                df.getOWLObjectSomeValuesFrom(testProperty0, df.getOWLObjectSomeValuesFrom(testProperty1, testClass1)).accept(new OWLClassExpressionTranslator(var0))
                                        )
                                )
                        ))
                ),
                // test combination with SomeValuesFrom
                Arguments.of(
                        df.getOWLSubClassOfAxiom(cajun, df.getOWLObjectSomeValuesFrom(hasTopping, mozzarella)),
                        new ArrayList<LogicElement>(Collections.singletonList(new QuantifiedFormula(
                                new Quantifier(0), // universal quantifier
                                new Variable[]{var0},
                                new BinaryFormula(
                                        cajun.accept(new OWLClassExpressionTranslator(var0)),
                                        new BinaryConnective(3), // implication
                                        new QuantifiedFormula(
                                                new Quantifier(1), // existential quantifier
                                                new Variable[]{var1},
                                                new BinaryFormula(
                                                        hasTopping.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                                        new BinaryConnective(0), // conjunction
                                                        mozzarella.accept(new OWLClassExpressionTranslator(var1))
                                                )
                                        )
                                )
                        )))
                )
        );
    }
}