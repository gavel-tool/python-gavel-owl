package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class SaveOnto {

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.loadOntologyFromOntologyDocument(new File("C:\\Users\\simon\\OneDrive\\Hiwi (OEO+)\\geo.owl"));
        // parameters: ontology, format and location
        man.saveOntology(o, new ManchesterSyntaxDocumentFormat(), new FileOutputStream(new File("C:\\Users\\simon\\OneDrive\\Hiwi (OEO+)\\geo.omn")));
    }
}
