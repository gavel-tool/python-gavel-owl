package owlapiExamples.tutorialAnna;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.util.stream.Collectors;

public class Visitor {
    public static void main(String[] args) throws OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager mg = OWLManager.createOWLOntologyManager();
        OWLOntology o = mg.loadOntology(IRI.create("https://protege.stanford.edu/ontologies/pizza/pizza.owl"));
        //OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();

        for (OWLClass oc : o.classesInSignature().collect(Collectors.toSet())) {
            System.out.println("Class: " + oc.toString());
            for (OWLAxiom axiom : o.axioms(oc).collect(Collectors.toSet())) {
                System.out.println("\tAxiom: " + axiom.toString());

                axiom.accept(new OWLObjectVisitor() {

                    // found the subClassOf axiom
                    public void visit(OWLSubClassOfAxiom subClassAxiom) {

                        // create an object visitor to read the underlying (subClassOf) restrictions
                        subClassAxiom.getSuperClass().accept(new OWLObjectVisitor() {

                            public void visit(OWLObjectSomeValuesFrom someValuesFromAxiom) {
                                printQuantifiedRestriction(oc, someValuesFromAxiom);
                            }

                        });
                    }
                });

            }
        }
    }

    public static void printQuantifiedRestriction(OWLClass oc, OWLQuantifiedObjectRestriction restriction) {
        System.out.println("\t\tClass: " + oc.toString());
        System.out.println("\t\tClassExpressionType: " + restriction.getClassExpressionType().toString());
        System.out.println("\t\tProperty: " + restriction.getProperty().toString());
        System.out.println("\t\tObject: " + restriction.getFiller().toString());
        System.out.println();
    }

}