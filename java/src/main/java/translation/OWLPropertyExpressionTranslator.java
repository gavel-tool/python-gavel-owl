package translation;

import fol.PredicateExpression;
import fol.LogicElement;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectInverseOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLPropertyExpressionVisitorEx;

import javax.annotation.Nonnull;

public class OWLPropertyExpressionTranslator extends OWLTranslator implements OWLPropertyExpressionVisitorEx<PredicateExpression> {
    // p, q are parameters to keep track of substitutions, they are variables or constants
    private final LogicElement p;
    private final LogicElement q;

    public OWLPropertyExpressionTranslator(LogicElement p, LogicElement q) {
        this.p = p;
        this.q = q;
    }

    //Object property
    @Override
    public PredicateExpression visit(@Nonnull OWLObjectProperty property) {
        return new PredicateExpression(getEntityName(property), new LogicElement[]{p, q});
    }

    //InverseOf
    public PredicateExpression visit(@Nonnull OWLObjectInverseOf inverseOf) {
        return inverseOf.getInverse().accept(new OWLPropertyExpressionTranslator(q, p));
    }

    //Data property
    @Override
    public PredicateExpression visit(@Nonnull OWLDataProperty property) {
        return new PredicateExpression(getEntityName(property), new LogicElement[]{p, q});
    }
}
