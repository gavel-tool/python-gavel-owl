package owlapiExamples.tutorialAnna;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class CHEBI {
    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        System.out.println("Hello");
        OWLOntologyManager mg = OWLManager.createOWLOntologyManager();
        //OWLOntology o = mg.loadOntologyFromOntologyDocument(new FileInputStream("C:\\Users\\anna\\Documents\\OwlFOlProject\\chebi.owl"));
        OWLOntology o = mg.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();
        //o.saveOntology(new FunctionalSyntaxDocumentFormat(), System.out);
        //System.out.println(o);
        System.out.println("Axioms: " + o.getAxiomCount() + ", Format: " + mg.getOntologyFormat(o));
        o.annotations().filter(CHEBI::classStartsWithP).forEach(System.out::println);
        System.out.println("Hello");
    }

    private static boolean classStartsWithP(OWLAnnotation e) {
        return false;
    }
}
