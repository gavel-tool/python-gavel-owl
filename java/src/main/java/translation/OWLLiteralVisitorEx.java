package translation;

import org.semanticweb.owlapi.model.OWLAnnotationObjectVisitorEx;

// This interface is only intended to be used for literals, anonymous individuals already
// get translated by OWLIndividualTranslator

public interface OWLLiteralVisitorEx<O> extends OWLAnnotationObjectVisitorEx<O> {
}
