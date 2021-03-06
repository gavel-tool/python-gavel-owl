package testClassExpressionTranslator;

import fol.LogicElement;
import fol.Variable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.model.OWLClassExpression;
import translation.OWLClassExpressionTranslator;
import translation.OWLTranslator;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class StandardClassExpressionTest {
    static Variable x = new Variable("X0");
    static Variable z = new Variable("Z");

    @BeforeEach
    private void resetVariableCounter() {
        OWLTranslator.variableCounter = 0;
    }

    private static Stream<Arguments> provideTestCases() {
        return null;
    }

    @ParameterizedTest
    @MethodSource("provideTestCases") //pairs of OWL-expression and FOL-expression
    public void testTranslation(OWLClassExpression owl, Object expected) {
        LogicElement translated = owl.accept(new OWLClassExpressionTranslator(z));
        assertEquals(
            expected.toString().replace("∀", "\\forall").replace("∃", "\\exists"),
            translated.toString());
    }
}
