package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class AddAnotations {

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        IRI ex = IRI.create("example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology(ex);
        OWLDataFactory factory = man.getOWLDataFactory();
        OWLClass ghost = factory.getOWLClass(ex + "#H_000001");
        OWLAnnotation label = factory.getOWLAnnotation(factory.getRDFSLabel(), factory.getOWLLiteral("ghost", "en"));

        OWLAxiom annotationAx = factory.getOWLAnnotationAssertionAxiom(ghost.getIRI(), label);
        man.applyChange(new AddAxiom(ontology, annotationAx));

        ontology.axioms().forEach(System.out::println);

        man.saveOntology(ontology, new TurtleDocumentFormat(), new FileOutputStream(new File("annotationTest.omn")));
    }
}
