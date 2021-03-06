package owlapiExamples.tutorialSimon;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;

public class Unsatisfiablity {

    public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException {
        IRI ex = IRI.create("example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology badOntology = man.createOntology();
        OWLDataFactory dataFactory = man.getOWLDataFactory();

        OWLClass human = dataFactory.getOWLClass(ex + "#human");
        OWLClass horse = dataFactory.getOWLClass(ex + "#horse");
        OWLDisjointClassesAxiom humanHorseAx = dataFactory.getOWLDisjointClassesAxiom(human, horse);
        OWLClass zentaur = dataFactory.getOWLClass(ex + "#zentaur");
        OWLSubClassOfAxiom zentaurHumanAx = dataFactory.getOWLSubClassOfAxiom(zentaur, human);
        OWLSubClassOfAxiom zentaurHorseAx = dataFactory.getOWLSubClassOfAxiom(zentaur, horse);

        badOntology.addAxioms(humanHorseAx, zentaurHorseAx, zentaurHumanAx);

        OWLReasonerFactory reasonerFactory = new ReasonerFactory();
        OWLReasoner reasoner = reasonerFactory.createReasoner(badOntology);
        reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        System.out.println("is the ontology consistent (without individuals)? " + reasoner.isConsistent());
        System.out.println("classes equivalent to owl:Nothing: ");
        reasoner.getEquivalentClasses(dataFactory.getOWLNothing()).forEach(System.out::println);
        man.saveOntology(badOntology, new RDFXMLDocumentFormat(), IRI.create(new File("example-consistent.owl")));

        OWLIndividual bane = dataFactory.getOWLNamedIndividual(ex + "#bane");
        OWLClassAssertionAxiom baneZentaurAx = dataFactory.getOWLClassAssertionAxiom(zentaur, bane);
        badOntology.addAxiom(baneZentaurAx);
        System.out.println("ontology with individuals: " + badOntology);

        // for some reason it is necessary to instantiate a new reasoner to get the correct result for consistency
        OWLReasoner newReasoner = reasonerFactory.createReasoner(badOntology);
        // the following leads to an exception, because the ontology is inconsistent :(
        // newReasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
        System.out.println("is it consistent now? " + newReasoner.isConsistent());
        man.saveOntology(badOntology, new RDFXMLDocumentFormat(), IRI.create(new File("example-inconsistent.owl")));
    }
}
