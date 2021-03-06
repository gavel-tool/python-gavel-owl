package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectTransformer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChangeAxiom {

    public static void main(String[] args) throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        final Map<OWLClassExpression, OWLClassExpression> REPLACEMENTS = new HashMap<>();

        IRI ex = IRI.create("example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology(ex);
        OWLDataFactory factory = man.getOWLDataFactory();

        OWLClass author = factory.getOWLClass(ex + "#author");
        OWLClass organization = factory.getOWLClass(ex + "#organization");
        OWLClass human = factory.getOWLClass(ex + "#human");
        OWLObjectProperty has_member = factory.getOWLObjectProperty(ex + "#has_member");
        OWLSubClassOfAxiom ax1 = factory.getOWLSubClassOfAxiom(author, human);
        OWLSubClassOfAxiom ax2 = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectSomeValuesFrom(has_member, author),
                organization);
        ontology.add(ax1, ax2);

        System.out.println("original axioms: ");
        ontology.logicalAxioms().forEach(System.out::println);

        // replace "has member some author" with "has member exactly 2 human"
        REPLACEMENTS.put(factory.getOWLObjectSomeValuesFrom(has_member, author), factory.getOWLObjectExactCardinality(2, has_member, human));

        OWLObjectTransformer<OWLClassExpression> replacer =
                new OWLObjectTransformer<>((x) -> true, (input) -> {
                    OWLClassExpression l = REPLACEMENTS.get(input);
                    return (l == null) ? input : l;
                }, factory, OWLClassExpression.class);

        List<OWLOntologyChange> res = replacer.change(ontology);
        ontology.applyChanges(res);

        System.out.println("replace \"has member some author\" with \"has member exactly 2 human\": ");
        ontology.logicalAxioms().forEach(System.out::println);

        // replace human with machine
        REPLACEMENTS.clear();
        REPLACEMENTS.put(factory.getOWLClass(ex + "#human"), factory.getOWLClass(ex + "#machine"));
        List<OWLOntologyChange> res2 = replacer.change(ontology);
        ontology.applyChanges(res2);

        System.out.println("replace human with machine: ");
        ontology.logicalAxioms().forEach(System.out::println);

        man.saveOntology(ontology, new RDFXMLDocumentFormat(), new FileOutputStream(new File("ChangeAxiomExample.owl")));


    }
}
