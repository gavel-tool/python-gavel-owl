package testClassExpressionTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import fol.PredicateExpression;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import translation.OWLClassExpressionTranslator;

import java.util.stream.Stream;

public class TestObjectIntersectionOf extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLClass testClass1 = df.getOWLClass("Test_1");
        OWLClass testClass2 = df.getOWLClass("Test_2");
        OWLClass testClass3 = df.getOWLClass("Test_3");
        LogicElement[] variables = {z};

        return Stream.of(
                //test ObjectIntersectionOf
                Arguments.of(
                        df.getOWLObjectIntersectionOf(testClass0, testClass1),
                        new BinaryFormula(
                                new PredicateExpression(testClass0.getIRI().toString(), variables),
                                new BinaryConnective(0),
                                new PredicateExpression(testClass1.getIRI().toString(), variables))
                ),
                //Test ObjectIntersectionOf multiple arguments
                Arguments.of(
                        df.getOWLObjectIntersectionOf(testClass0, testClass1, testClass2, testClass3),
                        new BinaryFormula(
                                testClass0.accept(new OWLClassExpressionTranslator(z)),
                                new BinaryConnective(0),
                                new BinaryFormula(
                                        testClass1.accept(new OWLClassExpressionTranslator(z)),
                                        new BinaryConnective(0),
                                        new BinaryFormula(
                                                testClass2.accept(new OWLClassExpressionTranslator(z)),
                                                new BinaryConnective(0),
                                                testClass3.accept(new OWLClassExpressionTranslator(z))
                                        )
                                )
                        )
                ),
                //Test ObjectIntersectionOf nested
                Arguments.of(
                        df.getOWLObjectIntersectionOf(df.getOWLObjectIntersectionOf(testClass0, testClass1), testClass2),
                        new BinaryFormula(
                                testClass0.accept(new OWLClassExpressionTranslator(z)),
                                new BinaryConnective(0),
                                new BinaryFormula(
                                        testClass1.accept(new OWLClassExpressionTranslator(z)),
                                        new BinaryConnective(0),
                                        testClass2.accept(new OWLClassExpressionTranslator(z))
                                )
                        )
                )
        );
    }
}