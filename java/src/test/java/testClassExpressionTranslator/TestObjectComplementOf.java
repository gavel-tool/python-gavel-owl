package testClassExpressionTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import translation.OWLClassExpressionTranslator;

import java.util.stream.Stream;

public class TestObjectComplementOf extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLClass testClass1 = df.getOWLClass("Test_1");
        OWLClass testClass2 = df.getOWLClass("Test_2");
        OWLClass testClass3 = df.getOWLClass("Test_3");
        LogicElement[] variables = {x};

        return Stream.of(
            //test ObjectComplementOf
            Arguments.of(
                df.getOWLObjectComplementOf(testClass0),
                new BinaryFormula(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        testClass0.accept(new OWLClassExpressionTranslator(z))),
                    new BinaryConnective(0), // conjunction
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(z))
                )
            ),
            //Test ObjectComplementOf nested
            Arguments.of(
                df.getOWLObjectComplementOf(df.getOWLObjectComplementOf(testClass0)),
                new BinaryFormula(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        new BinaryFormula(
                            new UnaryFormula(
                                new UnaryConnective(0),
                                testClass0.accept(new OWLClassExpressionTranslator(z))),
                            new BinaryConnective(0),
                            df.getOWLThing().accept(new OWLClassExpressionTranslator(z))
                        )
                    ),
                    new BinaryConnective(0),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(z))
                )
            )
        );
    }

}