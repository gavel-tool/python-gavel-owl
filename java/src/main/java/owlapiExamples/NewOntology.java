package owlapiExamples;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;

//how to add classes and expressions, as well as remove them
public class NewOntology {

    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        //create ontology
        IRI IOR = IRI.create("http://owl.api.tutorial");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        //add classes
        OWLClass person = df.getOWLClass(IOR+"#Person");
        OWLClass woman = df.getOWLClass(IOR+"#Woman");
        OWLSubClassOfAxiom w_sub_p = df.getOWLSubClassOfAxiom(woman, person);
        o.add(w_sub_p);

        //add annotations
        OWLAnnotation commentAnno = df.getOWLAnnotation(df.getRDFSComment(), df.getOWLLiteral("Class representing all women", "en"));
        OWLAxiom axComment = df.getOWLAnnotationAssertionAxiom(woman.getIRI(), commentAnno);
        man.applyChange(new AddAxiom(o, axComment));

        OWLAnnotation labelAnno = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral("Woman", "en"));
        OWLAxiom axLabel = df.getOWLAnnotationAssertionAxiom(woman.getIRI(), labelAnno);
        man.applyChange(new AddAxiom(o, axLabel));

        //add complex classes
        OWLClass A = df.getOWLClass(IOR + "#A");
        OWLClass B = df.getOWLClass(IOR + "#B");
        OWLObjectProperty R = df.getOWLObjectProperty(IOR + "#R");
        OWLObjectProperty S = df.getOWLObjectProperty(IOR + "#S");
        OWLSubClassOfAxiom ax = df.getOWLSubClassOfAxiom(
                df.getOWLObjectSomeValuesFrom(R, A),
                df.getOWLObjectSomeValuesFrom(S, B));
        o.add(ax); //adds the axiom and everything necessary for it
        o.remove(ax); //removes the same axiom from the ontology

        //output
        o.saveOntology(new ManchesterSyntaxDocumentFormat(), System.out);
        o.logicalAxioms().forEach(System.out::println);
    }

}
