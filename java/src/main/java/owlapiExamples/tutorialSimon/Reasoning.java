package owlapiExamples.tutorialSimon;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class Reasoning {

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        OWLDataFactory dataFactory = man.getOWLDataFactory();
        System.out.println(ontology);

        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);

        reasoner.getSubClasses(
                dataFactory.getOWLClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#Pizza"),
                true).forEach(System.out::println);


    }
}
