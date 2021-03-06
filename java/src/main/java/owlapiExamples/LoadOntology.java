package owlapiExamples;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class LoadOntology {

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {

        IRI oeo = IRI.create("https://openenergy-platform.org/ontology/oeo/");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntology(oeo);


        System.out.println("Ontology:");
        System.out.println(ontology);
        System.out.println("---");

        System.out.println("Information about the ontology, e.g. format:");
        OWLDocumentFormat format = ontology.getFormat();
        System.out.println(format);
        System.out.println("---");

        System.out.println("Full list of axioms:");
        ontology.axioms().forEach(System.out::println);
        System.out.println("----");

        System.out.println("Access to imported ontologies:");
        System.out.println("Number of axioms in imports closure: " + ontology.axioms(Imports.fromBoolean(true)).count());
        // save ontology
        ontology.saveOntology(new ManchesterSyntaxDocumentFormat(),
                new FileOutputStream(new File("src\\main\\resources\\oeoSaved.omn")));
    }
}