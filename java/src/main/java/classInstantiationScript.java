import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.stream.Collectors;

public class classInstantiationScript {

    public static void addClassInstantiationToOntology(String inputPath, String outputPath)
        throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(inputPath));
        OWLDataFactory df = man.getOWLDataFactory();

        int index = 0;
        for (OWLEntity owlClass : ontology.signature().filter(AsOWLClass::isOWLClass).collect(Collectors.toList())) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual(
                "https://github.com/gavel-tool/python-gavel-owl/", "ind_" + index++);
            OWLAxiom declAxiom = df.getOWLDeclarationAxiom(ind);
            OWLAxiom classAssertAxiom = df.getOWLClassAssertionAxiom((OWLClassExpression) owlClass, ind);
            ontology.add(declAxiom, classAssertAxiom);
        }
        ontology.saveOntology(new FileOutputStream(outputPath));
    }

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        addClassInstantiationToOntology(args[0], args[1]);
    }
}
