package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class CreateOnto {
    public static void main(String[] args) {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o;

        try {
            o = man.createOntology();
            System.out.println(o);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}
