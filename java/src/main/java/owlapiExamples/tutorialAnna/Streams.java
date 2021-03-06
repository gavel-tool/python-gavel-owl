package owlapiExamples.tutorialAnna;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

//how to iterate and filter over the ontology
public class Streams {
    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        System.out.println("Hello");
        OWLOntologyManager mg = OWLManager.createOWLOntologyManager();
        OWLOntology o = mg.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));

        //filter the ontology using Streams
        o.signature().filter(Streams::classStartsWithP).forEach(System.out::println);

    }
    // getRemainder returns an Optional, orElse defines what gets returned when the Optional is null
    private static boolean classStartsWithP(OWLEntity e) {
        return !e.isBuiltIn() && e.getIRI().getRemainder().orElse("").startsWith("P");
    }
}
