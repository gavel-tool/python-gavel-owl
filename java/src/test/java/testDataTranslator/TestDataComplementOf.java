package testDataTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.UnaryConnective;
import fol.UnaryFormula;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import translation.OWLDataTranslator;

import java.util.stream.Stream;

public class TestDataComplementOf extends StandardDataTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDatatype testDatatype0 = df.getOWLDatatype("Test_0");

        return Stream.of(
            //test DataComplementOf
            Arguments.of(
                df.getOWLDataComplementOf(testDatatype0),
                new BinaryFormula(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        testDatatype0.accept(new OWLDataTranslator(z))),
                    new BinaryConnective(0), // conjunction
                    df.getTopDatatype().accept(new OWLDataTranslator(z))
                )
            ),
            //Test DataComplementOf nested
            Arguments.of(
                df.getOWLDataComplementOf(df.getOWLDataComplementOf(testDatatype0)),
                new BinaryFormula(
                    new UnaryFormula(
                        new UnaryConnective(0),
                        new BinaryFormula(
                            new UnaryFormula(
                                new UnaryConnective(0),
                                testDatatype0.accept(new OWLDataTranslator(z))),
                            new BinaryConnective(0),
                            df.getTopDatatype().accept(new OWLDataTranslator(z))
                        )
                    ),
                    new BinaryConnective(0),
                    df.getTopDatatype().accept(new OWLDataTranslator(z))
                )
            )
        );
    }

}
