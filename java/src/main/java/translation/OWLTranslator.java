package translation;

import fol.BinaryConnective;
import fol.BinaryFormula;
import fol.LogicElement;
import fol.Variable;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class OWLTranslator {
    public static int variableCounter = 0;

    //helper function: combines the elements of a stream with a binary connective
    public LogicElement interlinkBinaryFormulas(int binaryConnectiveId, Stream<LogicElement> elemStream) {
        LogicElement[] elementsNull = elemStream.toArray(LogicElement[]::new);
        LogicElement[] elements = Arrays.stream(elementsNull).filter(Objects::nonNull).toArray(LogicElement[]::new);
        if (elements.length == 0) return null;
        if (elements.length == 1) return elements[0];

        BinaryFormula completeFormula = new BinaryFormula(
            elements[0],
            new BinaryConnective(binaryConnectiveId),
            null);

        BinaryFormula lastMember = completeFormula;
        for (int i = 1; i < elements.length - 1; i++) {
            BinaryFormula newMember = new BinaryFormula(
                    elements[i],
                    new BinaryConnective(binaryConnectiveId),
                    null);
            lastMember.setRight(newMember);
            lastMember = newMember;
        }

        lastMember.setRight(elements[elements.length - 1]);
        return completeFormula;
    }

    public String getEntityName(OWLEntity entity) {
        String s = entity.toStringID();
        /*
        //shorten names
        if (!ApiServer.USE_FULL_IRI) {
            s = s.substring(s.lastIndexOf("/") + 1);
        }
        s = s.startsWith("http://") ? s.substring(7) : s;
        s = s.startsWith("www.") ? s.substring(4) : s;
        // make names TPTP-conformal
        s = s.replaceAll("[^a-zA-Z0-9]", "_");
        */
        return s;
    }

    public String getEntityName(OWLAnonymousIndividual individual) {
        String s = individual.toStringID();
        /*
        if (!ApiServer.USE_FULL_IRI) {
            s = s.substring(s.lastIndexOf("/") + 1);
        }
        s = s.startsWith("http://") ? s.substring(7) : s;
        s = s.startsWith("www.") ? s.substring(4) : s;
        s = s.replaceAll("[^a-zA-Z0-9]", "_");
         */
        return s;
    }

    // use a counter to make sure every variable only appears once in an axiom
    public Variable getUniqueVariable() {
        return new Variable("X" + variableCounter++);
    }
}
