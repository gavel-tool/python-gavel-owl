package translation;

import fol.Constant;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLIndividualVisitorEx;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

import javax.annotation.Nonnull;

public class OWLIndividualTranslator extends OWLTranslator implements OWLIndividualVisitorEx<Constant> {

    //Anonymous Individual
    @Override
    public Constant visit(@Nonnull OWLAnonymousIndividual individual) {
        Constant res = new Constant(getEntityName(individual));
        System.out.println("An anonymous individual called '" + res + "' appeared!");
        return res;
    }

    //Named Individual
    @Override
    public Constant visit(@Nonnull OWLNamedIndividual individual) {
        return new Constant(getEntityName(individual));
    }
}
