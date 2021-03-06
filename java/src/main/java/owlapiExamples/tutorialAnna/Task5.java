package owlapiExamples.tutorialAnna;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Task5 {
    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        //create ontology
        IRI IOR = IRI.create("http://owl.api.tutorial");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        OWLClass student = df.getOWLClass(IOR + "#Student");
        OWLClass university = df.getOWLClass(IOR + "#University");
        OWLDisjointClassesAxiom dis = df.getOWLDisjointClassesAxiom(student, university);
        o.add(dis);

        OWLClass odd = df.getOWLClass(IOR + "#odd");
        OWLSubClassOfAxiom oddIsStudent = df.getOWLSubClassOfAxiom(odd, student);
        OWLSubClassOfAxiom oddIsUni = df.getOWLSubClassOfAxiom(odd, university);
        o.add(oddIsStudent);
        o.add(oddIsUni);

        isConsistentOntology(o);

        System.out.println("Add individual of odd");
        OWLIndividual oddIndividual = df.getOWLNamedIndividual(IOR+"#oddIndividual");
        OWLClassAssertionAxiom oddIndividualIsOdd = df.getOWLClassAssertionAxiom(odd, oddIndividual);
        o.add(oddIndividualIsOdd);

        isConsistentOntology(o);

        File fileout = new File("C:\\Users\\anna\\Documents\\OwlFOlProject\\task5.omn");
        try {
            o.saveOntology(new ManchesterSyntaxDocumentFormat(), new FileOutputStream(fileout));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    static void isConsistentOntology(OWLOntology o) {
        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(o);
        if (r.isConsistent()) {
            System.out.println("ontology is consistent");
            r.getPrecomputableInferenceTypes().forEach(r::precomputeInferences);
            System.out.println("Unsatisfiable classes:");
            r.getUnsatisfiableClasses().forEach(System.out::println);
        }
        else System.out.println("ontology is inconsistent");
    }
}
