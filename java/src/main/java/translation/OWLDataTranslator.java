package translation;

import fol.LogicElement;
import fol.PredicateExpression;
import fol.Symbol;
import org.semanticweb.owlapi.model.OWLDataIntersectionOf;
import org.semanticweb.owlapi.model.OWLDataVisitorEx;
import org.semanticweb.owlapi.model.OWLDatatype;

import javax.annotation.Nonnull;
import java.util.stream.Stream;


public class OWLDataTranslator extends OWLTranslator implements OWLDataVisitorEx<LogicElement> {

    private final Symbol p; // first parameter to keep track of substitutions, it is a variable or constant

    public OWLDataTranslator(Symbol p) {
        this.p = p;
    }

    //Datatype
    public LogicElement visit(@Nonnull OWLDatatype datatype) {
        return new PredicateExpression(getEntityName(datatype), new LogicElement[]{p});
    }

    @Override
    public LogicElement visit(OWLDataIntersectionOf node) {
        Stream<OWLDatatype> conj = node.datatypesInSignature();
        Stream<LogicElement> stream = conj.map(x -> x.accept(new OWLDataTranslator(p)));

        return interlinkBinaryFormulas(0, stream); // 0 = conjunction
    }
}
