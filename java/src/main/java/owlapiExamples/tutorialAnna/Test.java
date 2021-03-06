package owlapiExamples.tutorialAnna;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;

//How to load, save and print the ontology
public class Test {
    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        System.out.println("Hello");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology o = manager.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        o.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);
        System.out.println(o);
        System.out.println("Axioms: "+o.getAxiomCount()+", Format: "+manager.getOntologyFormat(o));
    }
}
