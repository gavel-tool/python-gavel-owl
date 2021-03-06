package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class LoadAndOutputOnto {

    public static void main(String[] args) {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        try {
            //load from web:
            //ontology = manager.loadOntology(IRI.create("http://protege.stanford.edu/ontologies/pizza/pizza.owl"));
            //load from file:
            //ontology = manager.loadOntology(IRI.create(new File("ueb10_family.owl")));
            //alternatively:
            ontology = manager.loadOntologyFromOntologyDocument(new File("oeo.omn"));
            //output:
            System.out.println(ontology);
            //other output:
            //ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
