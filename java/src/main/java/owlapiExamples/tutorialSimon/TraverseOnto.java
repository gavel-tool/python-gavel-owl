package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;

public class TraverseOnto {

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File("pizza.owl.xml"));

        // all axioms:
        /* deprecated version:
        for (OWLAxiom ax: ontology.getLogicalAxioms()) {
            System.out.println(ax);
        }*/
        // new (with streams):
        //ontology.logicalAxioms().forEach(System.out::println);

        // all IRIs:
        ontology.signature().forEach(System.out::println);
        // filter: only IRIs shorter than seven characters and not built in
        ontology.signature().filter(e -> !e.isBuiltIn() && e.getIRI().getRemainder().orElse("").length() < 7)
                .forEach(System.out::println);

    }
}
