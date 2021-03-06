package owlapiExamples;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class HandlingIndividuals {

    public static void main(String[] args) throws OWLOntologyCreationException {
        IRI ex = IRI.create("http://example.com/");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology(ex);
        OWLDataFactory factory = man.getOWLDataFactory();

        OWLClass student = factory.getOWLClass(ex + "student");
        OWLClass person = factory.getOWLClass(ex + "person");
        OWLObjectProperty isEnrolledIn = factory.getOWLObjectProperty(ex + "isEnrolledIn");
        OWLClass university = factory.getOWLClass(ex + "university");
        OWLObjectProperty attends = factory.getOWLObjectProperty(ex + "attends");
        OWLClass course = factory.getOWLClass(ex + "course");
        OWLClassExpression studentEquivalent = factory.getOWLObjectIntersectionOf(
                person,
                factory.getOWLObjectSomeValuesFrom(isEnrolledIn, university),
                factory.getOWLObjectSomeValuesFrom(attends, course));
        OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(student, studentEquivalent);
        man.applyChange(new AddAxiom(ontology, axiom));
        // "student" is equivalent to "person and isEnrolledIn some university and attends some course"

        OWLIndividual anna = factory.getOWLNamedIndividual(ex + "anna");
        OWLAxiom annaAssertAx = factory.getOWLClassAssertionAxiom(person, anna);
        OWLIndividual ovgu = factory.getOWLNamedIndividual(ex + "OVGU");
        OWLAxiom ovguAssertAx = factory.getOWLClassAssertionAxiom(university, ovgu);

        OWLAxiom enrolledAx = factory.getOWLObjectPropertyAssertionAxiom(isEnrolledIn, anna, ovgu);
        OWLAxiom courseAx = factory.getOWLClassAssertionAxiom(factory.getOWLObjectSomeValuesFrom(attends, course), anna);

        man.applyChanges(
                new AddAxiom(ontology, annaAssertAx),
                new AddAxiom(ontology, enrolledAx),
                new AddAxiom(ontology, ovguAssertAx),
                new AddAxiom(ontology, courseAx)
        );

        System.out.println("Axioms: ");
        ontology.axioms().forEach(System.out::println);

        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        System.out.println("Students: ");
        reasoner.getInstances(student).entities().forEach(System.out::println);
    }
}
