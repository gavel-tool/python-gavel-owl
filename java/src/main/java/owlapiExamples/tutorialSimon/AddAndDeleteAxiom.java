package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class AddAndDeleteAxiom {

    public static void main(String[] args) throws OWLOntologyCreationException {
        IRI ex = IRI.create("example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.createOntology(ex);
        OWLDataFactory factory = man.getOWLDataFactory();

        // add axiom
        OWLClass demand = factory.getOWLClass(ex + "#demand");
        OWLClass quality = factory.getOWLClass(ex + "#quality");
        OWLSubClassOfAxiom dsubq = factory.getOWLSubClassOfAxiom(demand, quality);
        ontology.add(dsubq);

        // add complex axiom
        OWLClass author = factory.getOWLClass(ex + "#author");
        OWLClass organization = factory.getOWLClass(ex + "#organization");
        OWLObjectProperty has_member = factory.getOWLObjectProperty(ex + "#has_member");
        OWLSubClassOfAxiom complexAx = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectSomeValuesFrom(has_member, author),
                organization);
                //has member some author is a subclass of organization
        ontology.add(complexAx);
        ontology.logicalAxioms().forEach(System.out::println);

        // delete axiom
        ontology.remove(dsubq);
        System.out.println("after deletion: ");
        ontology.logicalAxioms().forEach(System.out::println);
        // alternatives:
        // man.removeAxiom(o, dsubq);
        // RemoveAxiom ra = new RemoveAxiom(o, dsubq);
        // man.applyChange(ra);
    }
}
