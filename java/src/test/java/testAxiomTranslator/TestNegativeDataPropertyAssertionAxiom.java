package testAxiomTranslator;

import fol.UnaryConnective;
import fol.UnaryFormula;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import translation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestNegativeDataPropertyAssertionAxiom extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDataPropertyExpression prop0 = df.getOWLDataProperty("prop0");
        OWLIndividual ind = df.getOWLNamedIndividual("ind");
        OWLLiteral lit = df.getOWLLiteral(24);

        return Stream.of(
            Arguments.of(
                df.getOWLNegativeDataPropertyAssertionAxiom(prop0, ind, lit),
                new ArrayList<>(Arrays.asList(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        prop0.accept(
                            new OWLPropertyExpressionTranslator(
                                ind.accept(new OWLIndividualTranslator()),
                                lit.accept(new OWLLiteralTranslator())))
                    ),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(ind.accept(new OWLIndividualTranslator()))),
                    df.getTopDatatype().accept(new OWLDataTranslator(lit.accept(new OWLLiteralTranslator())))
                ))
            )
        );
    }
}
