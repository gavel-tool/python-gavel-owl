package testDataTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import fol.PredicateExpression;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDatatype;
import translation.OWLDataTranslator;

import java.util.stream.Stream;

public class TestDataIntersectionOf extends StandardDataTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDatatype testDatatype0 = df.getOWLDatatype("Test_0");
        OWLDatatype testDatatype1 = df.getOWLDatatype("Test_1");
        OWLDatatype testDatatype2 = df.getOWLDatatype("Test_2");
        OWLDatatype testDatatype3 = df.getOWLDatatype("Test_3");
        LogicElement[] variables = {z};

        return Stream.of(
            //test DataIntersectionOf
            Arguments.of(
                df.getOWLDataIntersectionOf(testDatatype0, testDatatype1),
                new BinaryFormula(
                    new PredicateExpression(testDatatype0.getIRI().toString(), variables),
                    new BinaryConnective(0),
                    new PredicateExpression(testDatatype1.getIRI().toString(), variables))
            ),
            //Test ObjectIntersectionOf multiple arguments
            Arguments.of(
                df.getOWLDataIntersectionOf(testDatatype0, testDatatype1, testDatatype2, testDatatype3),
                new BinaryFormula(
                    testDatatype0.accept(new OWLDataTranslator(z)),
                    new BinaryConnective(0),
                    new BinaryFormula(
                        testDatatype1.accept(new OWLDataTranslator(z)),
                        new BinaryConnective(0),
                        new BinaryFormula(
                            testDatatype2.accept(new OWLDataTranslator(z)),
                            new BinaryConnective(0),
                            testDatatype3.accept(new OWLDataTranslator(z))
                        )
                    )
                )
            ),
            //Test DataIntersectionOf nested
            Arguments.of(
                df.getOWLDataIntersectionOf(df.getOWLDataIntersectionOf(testDatatype0, testDatatype1), testDatatype2),
                new BinaryFormula(
                    testDatatype0.accept(new OWLDataTranslator(z)),
                    new BinaryConnective(0),
                    new BinaryFormula(
                        testDatatype1.accept(new OWLDataTranslator(z)),
                        new BinaryConnective(0),
                        testDatatype2.accept(new OWLDataTranslator(z))
                    )
                )
            )
        );
    }
}
