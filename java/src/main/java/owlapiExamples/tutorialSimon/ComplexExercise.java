package owlapiExamples.tutorialSimon;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;

public class ComplexExercise {
    // see http://syllabus.cs.manchester.ac.uk/pgt/2017/COMP62342/introduction-owl-api-msc.pdf, p. 26

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        IRI ex = IRI.create("example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology(ex);
        OWLDataFactory factory = man.getOWLDataFactory();

        System.out.println(ontology);

        OWLClass student = factory.getOWLClass(ex + "#student");
        OWLClass person = factory.getOWLClass(ex + "#person");
        OWLObjectProperty isEnrolledIn = factory.getOWLObjectProperty(ex + "#isEnrolledIn");
        OWLClass university = factory.getOWLClass(ex + "#university");
        OWLObjectProperty attends = factory.getOWLObjectProperty(ex + "#attends");
        OWLClass course = factory.getOWLClass(ex + "#course");
        OWLClassExpression expression = factory.getOWLObjectIntersectionOf(
                person,
                factory.getOWLObjectSomeValuesFrom(isEnrolledIn, university),
                factory.getOWLObjectSomeValuesFrom(attends, course));
        OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(student, expression);
        man.applyChange(new AddAxiom(ontology, axiom));

        System.out.println(ontology);

        OWLIndividual yourself = factory.getOWLNamedIndividual("ex" + "#yourself");
        OWLAxiom youAssertAx = factory.getOWLClassAssertionAxiom(person, yourself);
        OWLIndividual manchesterUniversity = factory.getOWLNamedIndividual("ex" + "#ManchesterUniversity");
        OWLAxiom manAssertAx = factory.getOWLClassAssertionAxiom(university, manchesterUniversity);

        OWLAxiom youEnrolledAx = factory.getOWLObjectPropertyAssertionAxiom(isEnrolledIn, yourself, manchesterUniversity);
        man.applyChanges(
                new AddAxiom(ontology, youAssertAx),
                new AddAxiom(ontology, manAssertAx),
                new AddAxiom(ontology, youEnrolledAx)
        );

        System.out.println(ontology);

        OWLAxiom youCourseAx = factory.getOWLClassAssertionAxiom(factory.getOWLObjectSomeValuesFrom(attends, course), yourself);
        man.applyChange(new AddAxiom(ontology, youCourseAx));

        System.out.println(ontology);

        ontology.axioms().forEach(System.out::println);

        ontology.saveOntology(new ManchesterSyntaxDocumentFormat(), IRI.create(new File("complexExercise.omn")));
        ontology.saveOntology(new RDFXMLDocumentFormat(), IRI.create(new File("complexExercise.owl")));

        // Task 6 (p. 32)
        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        reasoner.getInstances(student).forEach(System.out::println);
    }
}
