package testAxiomTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import translation.OWLClassExpressionTranslator;
import translation.OWLIndividualTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestSameIndividual extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLIndividual testIndividual0 = df.getOWLNamedIndividual("testInd0");
        OWLIndividual testIndividual1 = df.getOWLNamedIndividual("testInd1");
        OWLIndividual testIndividual2 = df.getOWLNamedIndividual("testInd2");
        LogicElement[] variables = {var0};

        return Stream.of(
            //test Same Individual
            Arguments.of(
                df.getOWLSameIndividualAxiom(testIndividual0, testIndividual1),
                new ArrayList<>(Arrays.asList(
                    new BinaryFormula(
                        testIndividual0.accept(new OWLIndividualTranslator()),
                        new BinaryConnective(8),
                        testIndividual1.accept(new OWLIndividualTranslator())
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()))
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual1.accept(new OWLIndividualTranslator()))
                    )
                ))),
            Arguments.of(
                df.getOWLSameIndividualAxiom(testIndividual0, testIndividual1, testIndividual2),
                new ArrayList<>(Arrays.asList(
                    new BinaryFormula(
                        testIndividual0.accept(new OWLIndividualTranslator()),
                        new BinaryConnective(8),
                        testIndividual1.accept(new OWLIndividualTranslator())
                    ),
                    new BinaryFormula(
                        testIndividual0.accept(new OWLIndividualTranslator()),
                        new BinaryConnective(8),
                        testIndividual2.accept(new OWLIndividualTranslator())
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual0.accept(new OWLIndividualTranslator()))
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual1.accept(new OWLIndividualTranslator()))
                    ),
                    df.getOWLThing().accept(
                        new OWLClassExpressionTranslator(testIndividual2.accept(new OWLIndividualTranslator()))
                    )
                ))
            )
        );
    }
}