package translation;

import fol.Constant;
import org.semanticweb.owlapi.model.OWLLiteral;

import javax.annotation.Nonnull;

public class OWLLiteralTranslator extends OWLTranslator implements OWLLiteralVisitorEx<Constant> {


    @Override
    public Constant visit(@Nonnull OWLLiteral node) {
        return new Constant(node.getLiteral());
    }
}
