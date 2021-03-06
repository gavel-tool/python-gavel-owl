package testAxiomTranslator;

import fol.Symbol;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import translation.OWLClassExpressionTranslator;
import translation.OWLDataTranslator;
import translation.OWLIndividualTranslator;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestDataPropertyAssertionAxiom extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression prop0 = df.getOWLDataProperty("prop0");
        OWLIndividual ind = df.getOWLNamedIndividual("ind");
        OWLLiteral lit = df.getOWLLiteral(24);

        return Stream.of(
            Arguments.of(
                df.getOWLDataPropertyAssertionAxiom(prop0, ind, lit),
                new ArrayList<>(Arrays.asList(
                    prop0.accept(
                        new OWLPropertyExpressionTranslator(
                            ind.accept(new OWLIndividualTranslator()),
                            lit.accept(new OWLDataTranslator()))),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(ind.accept(new OWLIndividualTranslator()))),
                    df.getTopDatatype().accept(new OWLDataTranslator((Symbol) lit.accept(new OWLDataTranslator())))
                ))
            )
        );
    }
}