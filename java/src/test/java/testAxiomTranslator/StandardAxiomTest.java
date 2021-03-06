package testAxiomTranslator;

import fol.LogicElement;
import fol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import translation.OWLAxiomTranslator;
import translation.OWLTranslator;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class StandardAxiomTest {
    static Variable var0 = new Variable("X0");
    static Variable var1 = new Variable("X1");
    static Variable var2 = new Variable("X2");
    static Variable var3 = new Variable("X3");

    @BeforeEach
    private void resetVariableCounter() {
        OWLTranslator.variableCounter = 0;
    }

    private static Stream<Arguments> provideTestCases() {
        return null;
    }

    @ParameterizedTest
    @MethodSource("provideTestCases") //pairs of OWL-expression and FOL-expression
    public void testTranslation(OWLAxiom owl, ArrayList<LogicElement> expected) {
        ArrayList<LogicElement> translated = owl.accept(new OWLAxiomTranslator());
        assertEquals(expected.toString(), translated.toString());
    }
}
