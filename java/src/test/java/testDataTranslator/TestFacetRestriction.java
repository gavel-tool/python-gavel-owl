package testDataTranslator;

import fol.LogicElement;
import fol.PredicateExpression;
import fol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.vocab.OWLFacet;
import translation.OWLDataTranslator;
import translation.OWLLiteralTranslator;
import translation.OWLTranslator;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestFacetRestriction {
    static Variable z = new Variable("Z");

    @BeforeEach
    private void resetVariableCounter() {
        OWLTranslator.variableCounter = 0;
    }

    @ParameterizedTest
    @MethodSource("provideTestCases") //pairs of OWL-expression and FOL-expression
    public void testTranslation(OWLFacetRestriction owl, Object expected) {
        LogicElement translated = owl.accept(new OWLDataTranslator(z));
        System.out.println(translated);
        assertEquals(
            expected.toString().replace("∀", "\\forall").replace("∃", "\\exists"),
            translated.toString());
    }

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLFacetRestriction facetRestriction = df.getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, 9);
        OWLFacetRestriction facetRestriction1 = df.getOWLFacetRestriction(OWLFacet.LENGTH, df.getOWLLiteral(true));

        return Stream.of(
            //test FacetRestriction
            Arguments.of(
                facetRestriction,
                new PredicateExpression(
                    OWLFacet.MAX_EXCLUSIVE.getIRI().toString(),
                    new LogicElement[]{z, facetRestriction.getFacetValue().accept(new OWLLiteralTranslator())})
            ),
            Arguments.of(
                facetRestriction1,
                new PredicateExpression(
                    OWLFacet.LENGTH.getIRI().toString(),
                    new LogicElement[]{z, facetRestriction1.getFacetValue().accept(new OWLLiteralTranslator())})
            )
        );
    }
}
