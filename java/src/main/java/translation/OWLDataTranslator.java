package translation;

import fol.Constant;
import fol.LogicElement;
import fol.PredicateExpression;
import fol.Symbol;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;

/*
FN:
> The semantics for data ranges is analog to the operators on classes.
> But since these features are not widely used, let's skip them until we find an ontology that uses them.

Therefore, only datatype and literal get translated
 */
public class OWLDataTranslator extends OWLTranslator implements OWLDataVisitorEx<LogicElement> {

    private final Symbol p; // first parameter to keep track of substitutions, it is a variable or constant

    public OWLDataTranslator(Symbol p) {
        this.p = p;
    }

    public OWLDataTranslator() {
        p = null;
    }

    //Datatype
    public LogicElement visit(@Nonnull OWLDatatype datatype) {
        return new PredicateExpression(getEntityName(datatype), new LogicElement[]{p});
    }

    @Override
    public LogicElement visit(OWLLiteral node) {
        return new Constant(node.getLiteral());
        // TODO: find out if constant really is the right choice
    }
}
