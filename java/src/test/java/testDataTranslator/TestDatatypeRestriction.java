package testDataTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import fol.PredicateExpression;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLFacetRestriction;
import org.semanticweb.owlapi.vocab.OWLFacet;
import translation.OWLLiteralTranslator;

import java.util.stream.Stream;

public class TestDatatypeRestriction extends StandardDataTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDatatype testDatatype0 = df.getOWLDatatype("Test_0");
        OWLFacetRestriction facetRestriction0 = df.getOWLFacetRestriction(OWLFacet.FRACTION_DIGITS, 3.4);
        OWLFacetRestriction facetRestriction1 = df.getOWLFacetRestriction(OWLFacet.TOTAL_DIGITS, df.getOWLLiteral("text"));
        OWLFacetRestriction facetRestriction2 = df.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, 2);
        LogicElement[] variables = {z};

        return Stream.of(
            //test DatatypeRestriction
            Arguments.of(
                df.getOWLDatatypeRestriction(testDatatype0, facetRestriction0),
                new BinaryFormula(
                    new PredicateExpression(testDatatype0.getIRI().toString(), variables),
                    new BinaryConnective(0), // conjunction
                    new PredicateExpression(
                        facetRestriction0.getFacet().getIRI().toString(),
                        new LogicElement[]{z, facetRestriction0.getFacetValue().accept(new OWLLiteralTranslator())}
                    )
                )
            ),
            //Test DatatypeRestriction with multiple facets
            Arguments.of(
                df.getOWLDatatypeRestriction(testDatatype0, facetRestriction0, facetRestriction1, facetRestriction2),
                new BinaryFormula(
                    new PredicateExpression(testDatatype0.getIRI().toString(), variables),
                    new BinaryConnective(0), // conjunction
                    new BinaryFormula(
                        new PredicateExpression(
                            facetRestriction2.getFacet().getIRI().toString(),
                            new LogicElement[]{z, facetRestriction2.getFacetValue().accept(new OWLLiteralTranslator())}
                        ),
                        new BinaryConnective(0),
                        new BinaryFormula(
                            new PredicateExpression(
                                facetRestriction1.getFacet().getIRI().toString(),
                                new LogicElement[]{z, facetRestriction1.getFacetValue().accept(new OWLLiteralTranslator())}
                            ),
                            new BinaryConnective(0),
                            new PredicateExpression(
                                facetRestriction0.getFacet().getIRI().toString(),
                                new LogicElement[]{z, facetRestriction0.getFacetValue().accept(new OWLLiteralTranslator())}
                            )
                        )
                    )
                )
            )
        );
    }
}
