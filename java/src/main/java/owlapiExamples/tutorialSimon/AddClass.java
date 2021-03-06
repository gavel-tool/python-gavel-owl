package owlapiExamples.tutorialSimon;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class AddClass {

    public static void main(String[] args) throws OWLOntologyCreationException {
        IRI IOR = IRI.create("http://www.example.com");
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology o = man.createOntology(IOR);
        OWLDataFactory df = o.getOWLOntologyManager().getOWLDataFactory();
        OWLClass person = df.getOWLClass(IOR + "#Person");
        OWLDeclarationAxiom da = df.getOWLDeclarationAxiom(person);
        o.add(da);
        // alternatives for adding axioms:
        da = df.getOWLDeclarationAxiom(df.getOWLClass(IOR + "#organization"));
        man.addAxiom(o, da);
        da = df.getOWLDeclarationAxiom(df.getOWLClass(IOR + "#material entity"));
        AddAxiom ax = new AddAxiom(o, da);
        man.applyChange(ax);
        System.out.println(o);
    }
}
