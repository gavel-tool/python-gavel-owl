package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestDisjointUnion extends StandardAxiomTest {

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
                //test Disjoint Classes
                Arguments.of(
                        df.getOWLDisjointUnionAxiom(testClass0, new ArrayList<OWLClassExpression>(Arrays.asList(testClass1, testClass2))),
                        new ArrayList<LogicElement>(Arrays.asList(
                                new QuantifiedFormula(
                                        new Quantifier(0), // universal quantifier
                                        new Variable[]{var0},
                                        new BinaryFormula(
                                                testClass0.accept(new OWLClassExpressionTranslator(var0)),
                                                new BinaryConnective(2), // biimplication
                                                new BinaryFormula(
                                                        testClass1.accept(new OWLClassExpressionTranslator(var0)),
                                                        new BinaryConnective(1), // disjunction
                                                        testClass2.accept(new OWLClassExpressionTranslator(var0))
                                                )
                                        )
                                ),
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var1},
                                        new UnaryFormula(
                                                new UnaryConnective(0),
                                                new BinaryFormula(
                                                        testClass1.accept(new OWLClassExpressionTranslator(var1)),
                                                        new BinaryConnective(0), // disjunction
                                                        testClass2.accept(new OWLClassExpressionTranslator(var1))
                                                )
                                        )
                                )

                        ))),
                Arguments.of(
                        df.getOWLDisjointUnionAxiom(testClass0,
                                new ArrayList<OWLClassExpression>(Arrays.asList(testClass1, testClass2, testClass3))),
                        new ArrayList<LogicElement>(Arrays.asList(
                                new QuantifiedFormula(
                                        new Quantifier(0), // universal quantifier
                                        new Variable[]{var0},
                                        new BinaryFormula(
                                                testClass0.accept(new OWLClassExpressionTranslator(var0)),
                                                new BinaryConnective(2), // biimplication
                                                new BinaryFormula(
                                                        testClass1.accept(new OWLClassExpressionTranslator(var0)),
                                                        new BinaryConnective(1), // disjunction
                                                        new BinaryFormula(
                                                                testClass2.accept(new OWLClassExpressionTranslator(var0)),
                                                                new BinaryConnective(1),
                                                                testClass3.accept(new OWLClassExpressionTranslator(var0))
                                                        )
                                                )
                                        )
                                ),
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var1},
                                        new UnaryFormula(
                                                new UnaryConnective(0),
                                                new BinaryFormula(
                                                        testClass1.accept(new OWLClassExpressionTranslator(var1)),
                                                        new BinaryConnective(0), // conjunction
                                                        testClass2.accept(new OWLClassExpressionTranslator(var1))
                                                )
                                        )),
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var2},
                                        new UnaryFormula(
                                                new UnaryConnective(0),
                                                new BinaryFormula(
                                                        testClass1.accept(new OWLClassExpressionTranslator(var2)),
                                                        new BinaryConnective(0), // conjunction
                                                        testClass3.accept(new OWLClassExpressionTranslator(var2))
                                                )
                                        )),
                                new QuantifiedFormula(
                                        new Quantifier(0),
                                        new Variable[]{var3},
                                        new UnaryFormula(
                                                new UnaryConnective(0),
                                                new BinaryFormula(
                                                        testClass2.accept(new OWLClassExpressionTranslator(var3)),
                                                        new BinaryConnective(0), // conjunction
                                                        testClass3.accept(new OWLClassExpressionTranslator(var3))
                                                )
                                        )
                                )
                        ))
                )
        );
    }
}