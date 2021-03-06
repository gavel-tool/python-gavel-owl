package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class OntoInformation {

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.loadOntologyFromOntologyDocument(new File("oeo.omn"));
        System.out.println("Axioms: " + o.getAxiomCount() + ", Format: "+ o.getFormat());

    }
}
