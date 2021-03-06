package testAxiomTranslator;

import fol.*;
import org.junit.jupiter.params.provider.Arguments;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLPropertyExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestAsymmetricObjectProperties extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {var0, var1};

        return Stream.of(
            //test Asymmetric
            Arguments.of(
                df.getOWLAsymmetricObjectPropertyAxiom(testProperty0),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0, var1},
                        new UnaryFormula(
                            new UnaryConnective(0),
                            new BinaryFormula(
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var1)),
                                new BinaryConnective(0),
                                testProperty0.accept(new OWLPropertyExpressionTranslator(var1, var0))
                            )
                        )
                    )))
            )
        );
    }
}