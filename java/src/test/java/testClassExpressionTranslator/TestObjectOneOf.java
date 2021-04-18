package testClassExpressionTranslator;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.Constant;
import fol.LogicElement;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.stream.Stream;

public class TestObjectOneOf extends StandardClassExpressionTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLIndividual ind0 = df.getOWLNamedIndividual("Individual0");
        OWLIndividual ind1 = df.getOWLNamedIndividual("Individual1");
        OWLIndividual ind2 = df.getOWLNamedIndividual("Individual2");
        LogicElement[] variables = {z};

        return Stream.of(
            //test ObjectOneOf
            Arguments.of(
                df.getOWLObjectOneOf(ind0, ind1),
                new BinaryFormula(
                    new BinaryFormula(variables[0], new BinaryConnective(8), new Constant(ind0.toStringID())),
                    new BinaryConnective(1), // disjunction
                    new BinaryFormula(variables[0], new BinaryConnective(8), new Constant(ind1.toStringID()))
                )
            ),
            //Test ObjectUnionOf multiple arguments
            Arguments.of(
                df.getOWLObjectOneOf(ind0, ind1, ind2),
                new BinaryFormula(
                    new BinaryFormula(variables[0], new BinaryConnective(8), new Constant(ind0.toStringID())),
                    new BinaryConnective(1),
                    new BinaryFormula(
                        new BinaryFormula(variables[0], new BinaryConnective(8), new Constant(ind1.toStringID())),
                        new BinaryConnective(1),
                        new BinaryFormula(variables[0], new BinaryConnective(8), new Constant(ind2.toStringID()))
                    )
                )
            )
        );
    }
}
