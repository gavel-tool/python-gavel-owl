package owlapiExamples;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

//How to load, save and print the ontology
public class Reasoner {
    public static void main(String[] args) {
        //load ontology
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLDataFactory df = man.getOWLDataFactory();

        try {
            OWLOntology o = man.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));

            //add reasoner
            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(o);

            //reason
            r.precomputeInferences(InferenceType.CLASS_HIERARCHY);

            r.unsatisfiableClasses().forEach(System.out::println);
            //get output from reasoner to specific topic, here subclasses
            //the second argument states if it should only get the direct subclasses (true) or also the rest (false)
            r.getSubClasses(df.getOWLClass("http://www.co-ode.org/ontologies/pizza/pizza.owl#RealItalianPizza"), false).forEach(System.out::println);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    }
}
