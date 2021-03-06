package owlapiExamples.tutorialAnna;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectTransformer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//how to replace classes
public class Replacing {

    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        IRI IOR = IRI.create("http://owl.api.tutorial");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();
        OWLClass person = df.getOWLClass(IOR + "#Person");
        OWLClass woman = df.getOWLClass(IOR + "#Woman");
        OWLSubClassOfAxiom w_sub_p = df.getOWLSubClassOfAxiom(woman, person);

        o.add(w_sub_p);

        OWLClass A = df.getOWLClass(IOR + "#A");
        OWLClass B = df.getOWLClass(IOR + "#B");
        OWLClass X = df.getOWLClass(IOR + "#X");
        OWLObjectProperty R = df.getOWLObjectProperty(IOR + "#R");
        OWLObjectProperty S = df.getOWLObjectProperty(IOR + "#S");
        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(
                df.getOWLObjectSomeValuesFrom(R, A),
                df.getOWLObjectSomeValuesFrom(S, B));
        o.add(ax);
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
        o.logicalAxioms().forEach(System.out::println);

        //replace (R,A) with X
        final Map<OWLClassExpression, OWLClassExpression> replacements = new HashMap<>();
        replacements.put(df.getOWLObjectSomeValuesFrom(R, A), X);

        OWLObjectTransformer<OWLClassExpression> replacer =
                new OWLObjectTransformer<>((x) -> true, input -> {
                    OWLClassExpression l = replacements.get(input);
                    if (l == null) return input;
                    return l;
                }, df, OWLClassExpression.class);

        List<OWLOntologyChange> results = replacer.change(o);
        o.applyChanges(results);

        //print ontology
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
        o.logicalAxioms().forEach(System.out::println);
    }

}
