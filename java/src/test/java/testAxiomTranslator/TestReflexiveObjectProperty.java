package testAxiomTranslator;

import fol.LogicElement;
import fol.QuantifiedFormula;
import fol.Quantifier;
import fol.Variable;
import fol.BinaryFormula;
import fol.BinaryConnective;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import translation.OWLPropertyExpressionTranslator;
import translation.OWLClassExpressionTranslator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestReflexiveObjectProperty extends StandardAxiomTest {

    private static Stream<Arguments> provideTestCases() {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLObjectPropertyExpression testProperty0 = df.getOWLObjectProperty("Prop0");
        LogicElement[] variables = {var0};

        return Stream.of(
            //test Disjoint Properties
            Arguments.of(
                df.getOWLReflexiveObjectPropertyAxiom(testProperty0),
                new ArrayList<LogicElement>(Arrays.asList(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{var0},
                        new BinaryFormula(
                            df.getOWLThing().accept(new OWLClassExpressionTranslator(var0)),
                            new BinaryConnective(3),
                            testProperty0.accept(new OWLPropertyExpressionTranslator(var0, var0))
                        )
                    )
                ))
            )
        );
    }
}