package translation;

import fol.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

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

    @Override
    public LogicElement visit(OWLDataUnionOf node) {
        Stream<OWLDatatype> disj = node.datatypesInSignature();
        Stream<LogicElement> stream = disj.map(x -> x.accept(new OWLDataTranslator(p)));
        return interlinkBinaryFormulas(1, stream); // 1 = disjunction
    }

    @Override
    public LogicElement visit(OWLDataComplementOf node) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        return new BinaryFormula(
            new UnaryFormula(new UnaryConnective(0), node.getDataRange().accept(new OWLDataTranslator(p))),
            new BinaryConnective(0), // 0 = conjunction
            df.getTopDatatype().accept(new OWLDataTranslator(p))
        );
    }

    @Override
    public LogicElement visit(OWLDataOneOf node) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Stream<LogicElement> literals = node.values().map(l ->
            new BinaryFormula(
                p,
                new BinaryConnective(8), // 8 = eq
                l.accept(new OWLLiteralTranslator())
            ));
        return interlinkBinaryFormulas(1, literals);
    }

    @Override
    public LogicElement visit(OWLFacetRestriction node) {
        return new PredicateExpression(
            node.getFacet().toString(),
            new LogicElement[]{p, node.getFacetValue().accept(new OWLLiteralTranslator())});
    }

    @Override
    public LogicElement visit(OWLDatatypeRestriction node) {
        return new BinaryFormula(
            node.getDatatype().accept(new OWLDataTranslator(p)),
            new BinaryConnective(0), // 0 = conjunction
            interlinkBinaryFormulas(0, node.facetRestrictions().map(x -> x.accept(new OWLDataTranslator(p))))
        );
    }
}
