package testDataTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import translation.OWLLiteralTranslator;

import java.util.stream.Stream;

public class TestDataOneOf extends StandardDataTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLLiteral literal0 = df.getOWLLiteral("Literal0");
        OWLLiteral literal1 = df.getOWLLiteral("Literal1");
        OWLLiteral literal2 = df.getOWLLiteral("Literal2");
        LogicElement[] variables = {z};

        return Stream.of(
            //test DataOneOf
            Arguments.of(
                df.getOWLDataOneOf(literal0, literal1),
                new BinaryFormula(
                    new BinaryFormula(variables[0], new BinaryConnective(8),
                        literal0.accept(new OWLLiteralTranslator())),

                    new BinaryConnective(1), // disjunction
                    new BinaryFormula(variables[0], new BinaryConnective(8),
                        literal1.accept(new OWLLiteralTranslator()))

                )
            ),
            //Test DataUnionOf multiple arguments
            Arguments.of(
                df.getOWLDataOneOf(literal0, literal1, literal2),
                new BinaryFormula(
                    new BinaryFormula(variables[0], new BinaryConnective(8),
                        literal0.accept(new OWLLiteralTranslator())),
                    new BinaryConnective(1),
                    new BinaryFormula(
                        new BinaryFormula(variables[0], new BinaryConnective(8),
                            literal1.accept(new OWLLiteralTranslator())),
                        new BinaryConnective(1),
                        new BinaryFormula(variables[0], new BinaryConnective(8),
                            literal2.accept(new OWLLiteralTranslator()))
                    )
                )
            )
        );
    }
}
