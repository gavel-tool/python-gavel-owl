package owlapiExamples;

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

        //create ontology
        IRI IOR = IRI.create("http://owl.api.tutorial");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        OWLClass A = df.getOWLClass(IOR + "#A");
        OWLClass B = df.getOWLClass(IOR + "#B");
        OWLClass X = df.getOWLClass(IOR + "#X"); //new class!
        OWLObjectProperty R = df.getOWLObjectProperty(IOR + "#R");
        OWLObjectProperty S = df.getOWLObjectProperty(IOR + "#S");
        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(
                df.getOWLObjectSomeValuesFrom(R, A),
                df.getOWLObjectSomeValuesFrom(S, B));
        o.add(ax);
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
        o.logicalAxioms().forEach(System.out::println);

        //replace (R,A) with X
        //first create map with your replacements
        final Map<OWLClassExpression, OWLClassExpression> replacements = new HashMap<>();
        replacements.put(df.getOWLObjectSomeValuesFrom(R, A), X);

        //then create a transformer based on that
        OWLObjectTransformer<OWLClassExpression> replacer =
                new OWLObjectTransformer<>((x) -> true, input -> {
                    OWLClassExpression l = replacements.get(input);
                    if (l == null) return input;
                    return l;
                }, df, OWLClassExpression.class);

        //then apply the transformer
        List<OWLOntologyChange> results = replacer.change(o);
        o.applyChanges(results);

        //print ontology
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
        o.logicalAxioms().forEach(System.out::println);
    }

}
