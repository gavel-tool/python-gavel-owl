package translation;

import fol.*;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.metrics.AxiomCount;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormFromRDFSLabelAxiomListProvider;
import py4j.GatewayServer;
import py4j.Py4JNetworkException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class ApiServer {
    public static final boolean USE_FULL_IRI = false;

    public static void main(String[] args) throws Exception {
        //arg0 is the java port, arg1 the python port
        try {
            ApiServer app = new ApiServer(parseInt(args[0]), parseInt(args[1]));

            Scanner scn = new Scanner(new File("../../../../Documents/example-ontologies/fibo-merged.owl"));
            StringBuilder ontologyText = new StringBuilder();
            while (scn.hasNext()) {
                ontologyText.append(scn.nextLine()).append("\n");
            }
            System.out.println(new AxiomCount(app.loadOntology(ontologyText.toString())).getValue() + " & "
                + app.getDLComplexity(ontologyText.toString()) + " & \\\\");
            /*AnnotatedLogicElement[] translation = app.translateOntology(ontologyText.toString());
            for (AnnotatedLogicElement t : translation) {
                System.out.println(t);
            }
            System.out.println(app.getNameMapping(ontologyText.toString()));*/

        } catch (Py4JNetworkException e) {
            e.printStackTrace();
            System.out.println("Starting server failed: " + e);

        }

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

    public boolean isConsistent(String ontologyText) throws Exception {
        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(loadOntology(ontologyText));
        return r.isConsistent();
    }

    public OWLOntology loadOntology(String ontologyText) throws Exception {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = man.loadOntologyFromOntologyDocument(
                new ByteArrayInputStream(ontologyText.getBytes(StandardCharsets.UTF_8)));
        } catch (OWLOntologyCreationException owlOntologyCreationException) {
            owlOntologyCreationException.printStackTrace();
        }
        if (ontology == null) throw new Exception("Failed to read file.");
        return ontology;
    }

    public AnnotatedLogicElement[] translateOntology(String ontologyText) throws Exception {
        System.out.println("Starting Translation");
        OntologyTranslator translator = new OntologyTranslator(loadOntology(ontologyText), false);
        return translator.translate().toArray(new AnnotatedLogicElement[0]);
    }

    public AnnotatedLogicElement[] translateOntologyFromFile(String path) throws Exception {
        System.out.println("Starting Translation");
        OntologyTranslator translator = new OntologyTranslator(
            OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(path)), false);
        return translator.translate().toArray(new AnnotatedLogicElement[0]);
    }

    public HashMap<String, String> getNameMapping(String ontologyText) throws Exception {
        OWLOntology ontology = loadOntology(ontologyText);
        ontology.signature(Imports.INCLUDED).forEach(HasIRI::getIRI);
        ShortFormFromRDFSLabelAxiomListProvider labelProvider = new ShortFormFromRDFSLabelAxiomListProvider(
            new ArrayList<>(),
            ontology.axioms(Imports.INCLUDED).collect(Collectors.toList()));
        HashMap<String, String> mapping = new HashMap<>();
        ontology.signature(Imports.INCLUDED).
            forEach(x -> mapping.put(x.getIRI().toString(), labelProvider.getShortForm(x)));
        return mapping;
    }

    public AnnotatedLogicElement[] getInferences(String ontologyText) throws Exception {
        OWLOntology ontology = loadOntology(ontologyText);
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
                    "inferred: " + sub + " SubClassOf " + c)));
                return res.toArray(new AnnotatedLogicElement[0]);
            }
        } catch (InconsistentOntologyException e) {
            System.out.println("Getting inferences failed because ontology is inconsistent.");
        }
        return null;
    }

    public String getDLComplexity(String ontologyText) throws Exception {
        OWLOntology ontology = loadOntology(ontologyText);
        DLExpressivity expressivity = new DLExpressivity(ontology);
        return expressivity.getValue();
    }
}
