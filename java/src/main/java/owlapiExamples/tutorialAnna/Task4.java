package owlapiExamples.tutorialAnna;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Task4 {

    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        //create ontology
        IRI IOR = IRI.create("http://owl.api.tutorial");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        OWLClass student = df.getOWLClass(IOR + "#Student");
        OWLClass university = df.getOWLClass(IOR + "#University");
        OWLClass course = df.getOWLClass(IOR + "#Course");
        OWLClass person = df.getOWLClass(IOR + "#Person");
        OWLObjectProperty attends = df.getOWLObjectProperty(IOR + "#attends");
        OWLObjectProperty isEnrolledIn = df.getOWLObjectProperty(IOR + "#isEnrolledIn");
        OWLEquivalentClassesAxiom studentEquiv = df.getOWLEquivalentClassesAxiom(
                student, df.getOWLObjectIntersectionOf(person,
                        df.getOWLObjectIntersectionOf(df.getOWLObjectSomeValuesFrom(isEnrolledIn, university),
                                df.getOWLObjectSomeValuesFrom(attends, course))));

        o.add(studentEquiv);

        OWLIndividual anna = df.getOWLNamedIndividual(IOR + "#Anna");
        OWLClassAssertionAxiom annaIsPerson = df.getOWLClassAssertionAxiom(person, anna);
        o.add(annaIsPerson);

        OWLIndividual mu = df.getOWLNamedIndividual(IOR + "#ManchesterUniversity");
        OWLClassAssertionAxiom muIsUni = df.getOWLClassAssertionAxiom(university, mu);
        o.add(muIsUni);

        OWLObjectPropertyAssertionAxiom meInMU = df.getOWLObjectPropertyAssertionAxiom(isEnrolledIn, anna, mu);
        o.add(meInMU);

        OWLClassAssertionAxiom meAttendsSomeCourse = df.getOWLClassAssertionAxiom(df.getOWLObjectSomeValuesFrom(attends, course), anna);
        o.add(meAttendsSomeCourse);

        File fileout = new File("C:\\Users\\anna\\Documents\\OwlFOlProject\\task4.omn");
        try {
            o.saveOntology(new ManchesterSyntaxDocumentFormat(), new FileOutputStream(fileout));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(o);
        r.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        //r.getSubTypes(student, false).forEach(System.out::println);
        r.getInstances(student, false).forEach(System.out::println);
    }
}