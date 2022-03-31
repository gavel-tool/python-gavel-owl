package translation;

import fol.Constant;
import fol.Symbol;
import fol.Variable;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.annotation.Nonnull;

public class OWLIndividualTranslator extends OWLTranslator implements OWLIndividualVisitorEx<Symbol> {

    //Anonymous Individual
    @Override
    public Variable visit(@Nonnull OWLAnonymousIndividual individual) {
        return new Variable('X' + getEntityName(individual));
    }

    //Named Individual
    @Override
    public Constant visit(@Nonnull OWLNamedIndividual individual) {
        return new Constant(getEntityName(individual));
    }
}
