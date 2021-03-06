package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLAxiomTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestObjectSubPropertyOf extends StandardAxiomTest {


    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        OWLObjectPropertyExpression testProperty1 = df.getOWLObjectProperty("Prop1");
        OWLObjectPropertyExpression testProperty2 = df.getOWLObjectProperty("Prop2");
        LogicElement[] variables = {var0, var1};

        return Stream.of(
                //test SubPropertyOf
                Arguments.of(
                        df.getOWLSubObjectPropertyOfAxiom(testProperty0, testProperty1),
                        new ArrayList<LogicElement>(Arrays.asList(
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var0, var1},
                                        new BinaryFormula(
                                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                                new BinaryConnective(3),
                                                testProperty1.accept(new OWLPropertyExpressionTranslator(var0, var1))
                                        )
                                )))
                ),
                //test PropertyExpressionChains
                Arguments.of(
                    df.getOWLSubPropertyChainOfAxiom(new ArrayList<>(Arrays.asList(testProperty0)), testProperty2),
                    new ArrayList<LogicElement>(Arrays.asList(
                        new QuantifiedFormula(
                            new Quantifier(0),
                            new Variable[]{var0, var1},
                            new BinaryFormula(
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                new BinaryConnective(3),
                                testProperty2.accept(new OWLPropertyExpressionTranslator(var0, var1))
                            )
                        ))
                        )

                ),
                Arguments.of(
                        df.getOWLSubPropertyChainOfAxiom(new ArrayList<>(Arrays.asList(testProperty0, testProperty1)), testProperty2),
                        new ArrayList<LogicElement>(Collections.singletonList(
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var0, var1},
                                        new BinaryFormula(
                                                new QuantifiedFormula(
                                                        new Quantifier(1),
                                                        new Variable[]{var2},
                                                        new BinaryFormula(
                                                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var2)),
                                                                new BinaryConnective(0),
                                                                testProperty1.accept(new OWLPropertyExpressionTranslator(var2, var1)))),
                                                new BinaryConnective(3),
                                                testProperty2.accept(new OWLPropertyExpressionTranslator(var0, var1))
                                        )
                                ))
                        )
                )
                //TODO: Fix PropertyExpressionChains
        );
    }

    @ParameterizedTest
    @MethodSource("provideTestCases") //pairs of OWL-expression and FOL-expression
    public void testSubPropertyOf(OWLAxiom owl, ArrayList<LogicElement> expected) {
        ArrayList<LogicElement> translated = owl.accept(new OWLAxiomTranslator());
        System.out.println(expected.toString());
        assertEquals(expected.toString(), translated.toString());
    }
}