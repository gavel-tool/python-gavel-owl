package translation;

import fol.*;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import py4j.GatewayServer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class ApiServer {
    public static final boolean USE_FULL_IRI = false;

    public static void main(String[] args) throws Exception {
        //arg0 is the java port, arg1 the python port
        ApiServer app = new ApiServer(parseInt(args[0]), parseInt(args[1]));
    }

    public ApiServer(int jp, int pp) throws UnknownHostException {
        //jp is the java port, pp the python port
        // app is now the gateway.entry_point, create server
        GatewayServer server = new GatewayServer.GatewayServerBuilder(this)
            .javaPort(jp)
            .javaAddress(InetAddress.getByName("127.0.0.1"))
            .callbackClient(pp, InetAddress.getByName("127.0.0.1"))
            .build();
        server.start();
        System.out.println("Server started");
    }

    public boolean isConsistent(String origin) throws Exception {
        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(loadOntology(origin));
        return r.isConsistent();
    }

    public OWLOntology loadOntology(String origin) throws Exception {

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = man.loadOntologyFromOntologyDocument(new ByteArrayInputStream(origin.getBytes(StandardCharsets.UTF_8)));
        } catch (OWLOntologyCreationException owlOntologyCreationException) {
            owlOntologyCreationException.printStackTrace();
        }
        if (ontology == null) throw new Exception("Failed to read file.");
        return ontology;
    }


    public OWLOntology loadOntologyFromFile(String origin) throws Exception {
        // resolve shortcuts
        if (origin.toLowerCase().equals("pizza")) {
            origin = "http://protege.stanford.edu/ontologies/pizza/pizza.owl";
        } else if (origin.toLowerCase().equals("oeo")) {
            origin = "https://openenergy-platform.org/ontology/oeo/";
        }
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = man.loadOntology(IRI.create(origin));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (OWLOntologyFactoryNotFoundException e) {
            System.out.println("Failed to find this IRI, trying file instead.");
            try {
                ontology = man.loadOntologyFromOntologyDocument(new File(origin));
            } catch (OWLOntologyCreationException owlOntologyCreationException) {
                owlOntologyCreationException.printStackTrace();
            }
        }
        if (ontology == null) throw new Exception("Failed to find an IRI or a file with this name.");
        return ontology;
    }

    public AnnotatedLogicElement[] translateOntology(String origin) throws Exception {
        System.out.println("Starting Translation");
        OntologyTranslator translator = new OntologyTranslator(loadOntology(origin), false);
        return translator.translate().toArray(new AnnotatedLogicElement[0]);
    }

    public AnnotatedLogicElement[] getInferences(String origin) throws Exception {
        OWLOntology ontology = loadOntology(origin);
        try {
            OWLReasonerFactory rf = new ReasonerFactory();
            OWLReasoner r = rf.createReasoner(ontology);
            ArrayList<AnnotatedLogicElement> res = new ArrayList<>();
            for (OWLClass c : ontology.classesInSignature(Imports.INCLUDED).collect(Collectors.toSet())) {
                Variable x = new Variable("X");
                LogicElement superClass = c.accept(new OWLClassExpressionTranslator(x));
                r.subClasses(c, true).forEach(sub -> res.add(new AnnotatedLogicElement(
                    new QuantifiedFormula(
                        new Quantifier(0),
                        new Variable[]{x},
                        new BinaryFormula(
                            sub.accept(new OWLClassExpressionTranslator(x)),
                            new BinaryConnective(3), // implication
                            superClass
                        )),
                    "inferred: " + sub.toString() + " SubClassOf " + c.toString())));
                return res.toArray(new AnnotatedLogicElement[0]);
            }
        } catch (InconsistentOntologyException e) {
            System.out.println("Getting inferences failed because ontology is inconsistent.");
        }
        return null;
    }
}
