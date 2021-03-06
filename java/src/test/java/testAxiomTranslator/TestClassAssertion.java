package testAxiomTranslator;

import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import translation.OWLClassExpressionTranslator;
import translation.OWLIndividualTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestClassAssertion extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLClass testClass0 = df.getOWLClass("Test_0");
        OWLIndividual testIndividual0 = df.getOWLNamedIndividual("testInd0");
        LogicElement[] variables = {var0};

        return Stream.of(
            //test Class Assertion
            Arguments.of(
                df.getOWLClassAssertionAxiom(testClass0, testIndividual0),
                new ArrayList<>(Arrays.asList(
                    testClass0.accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()))),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()))
                    )
                ))
            )

        );
    }
}