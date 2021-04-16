package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import translation.OWLDataTranslator;
import translation.OWLTranslator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Stream;

public class TestDatatypeDefinition extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLDatatype testDatatype = df.getOWLDatatype("Test_0");
        OWLDatatype testDatatype1 = df.getOWLDatatype("Test_1");
        OWLDatatype testDatatype2 = df.getOWLDatatype("Test_2");
        OWLDataRange testDataRange = df.getOWLDataIntersectionOf(testDatatype1, testDatatype2);
        LogicElement[] variables = {var0};

        OWLTranslator.variableCounter = 1;

        return Stream.of(
            //test Datatype Definition
            Arguments.of(
                df.getOWLDatatypeDefinitionAxiom(testDatatype, testDataRange),
                new ArrayList<LogicElement>(Collections.singletonList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0},
                        new BinaryFormula(
                            testDatatype.accept(new OWLDataTranslator(var0)),
                            new BinaryConnective(2), // 2 = Biimplication
                            testDataRange.accept(new OWLDataTranslator(var0))
                        )

                    )))
            )

        );
    }
}
