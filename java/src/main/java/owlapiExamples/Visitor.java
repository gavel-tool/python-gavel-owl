package owlapiExamples;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.stream.Collectors;

public class Visitor {

    public static void main(String[] args) throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File("src\\main\\resources\\vehicle.owl"));

        System.out.println("All Subclass-relations between single classes:");
        ontology.axioms(AxiomType.SUBCLASS_OF).filter(axiom -> axiom.getSuperClass().isOWLClass()).forEach(System.out::println);

        System.out.println("\nClasses which are subclasses of a someValuesFromAxiom:");
        for (OWLClass clazz : ontology.classesInSignature().collect(Collectors.toSet())) {

            for (OWLAxiom axiom : ontology.axioms(clazz).collect(Collectors.toSet())) {

                axiom.accept(new OWLObjectVisitor() {
                    // visit subclass axioms
                    public void visit(@Nonnull OWLSubClassOfAxiom subClassAxiom) {

                        subClassAxiom.getSuperClass().accept(new OWLObjectVisitor() {
                            //visit someValuesFrom axioms
                            public void visit(@Nonnull OWLObjectSomeValuesFrom someValuesFromAxiom) {
                                System.out.println("Class: " + clazz);
                                System.out.println("Property: " + someValuesFromAxiom.getProperty());
                                System.out.println("Object: " + someValuesFromAxiom.getFiller());
                            }
                        });
                    }
                });
            }

        }

    }

}
