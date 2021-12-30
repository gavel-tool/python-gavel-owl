package translation;

import fol.*;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class ApiServer {

    public static final boolean USE_FULL_IRI = false;
    public static final int MAX_LEV_DIST = 5;

    public HashMap<String, HashMap<String, String>> labelToIRIMapping = new HashMap<>();

    public static void main(String[] args) throws Exception {
        //arg0 is the java port, arg1 the python port
        try {
            ApiServer app = new ApiServer(parseInt(args[0]), parseInt(args[1]));

            /*Scanner scn = new Scanner(new File("../../../../Documents/example-ontologies/fibo-merged.owl"));
            StringBuilder ontologyText = new StringBuilder();
            while (scn.hasNext()) {
                ontologyText.append(scn.nextLine()).append("\n");
            }
            System.out.println(new AxiomCount(app.loadOntology(ontologyText.toString())).getValue() + " & "
                + app.getDLComplexity(ontologyText.toString()) + " & \\\\");

            //AnnotatedLogicElement[] translation = app.translateOntology(ontologyText.toString());
            AnnotatedLogicElement[] translation = app.translateOntologyFromFile("../../../../Documents/example-ontologies/oeo-merged.owl");

            for (AnnotatedLogicElement t : translation) {
                System.out.println(t);
            }
            System.out.println(app.getNameMapping(ontologyText.toString()));
*/
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
        System.out.println("Server started (jp: " + jp + ", pp: " + pp + ")");
    }

    public boolean isConsistent(String path) throws Exception {
        OWLReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(
            OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(path)));
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

    public String[] getAnnotations(String ontologyPath, ArrayList<String> annotationProperties)
        throws Exception {
        ArrayList<String> res_list = new ArrayList<>();
        OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(ontologyPath))
            .axioms(AxiomType.ANNOTATION_ASSERTION)
            // filter: axiom matches one of the annotationProperties
            .filter(ax -> {
                for (Object ap : annotationProperties) {
                    if (ax.getProperty().getIRI().toString().equals(ap)) return true;
                }
                return false;
            })
            .filter(a -> a.getValue().asLiteral().isPresent())
            .forEach(a -> res_list.add(a.getValue().asLiteral().get().getLiteral()));

        String[] q = new String[0];
        return res_list.toArray(q);
    }

    private HashMap<String, String> getLabelToIRIMapping(OWLOntology ontology) throws Exception {
        ontology.signature(Imports.INCLUDED).forEach(HasIRI::getIRI);
        ShortFormFromRDFSLabelAxiomListProvider labelProvider = new ShortFormFromRDFSLabelAxiomListProvider(
            new ArrayList<>(),
            ontology.axioms(Imports.INCLUDED).collect(Collectors.toList()));
        HashMap<String, String> mapping = new HashMap<>();
        ontology.signature(Imports.INCLUDED).
            forEach(x -> mapping.put(labelProvider.getShortForm(x), x.getIRI().toString()));
        return mapping;
    }

    private static int levDist(String a, String b) {
        int a_len = a.length();
        int b_len = b.length();
        if (a_len == 0) return b_len;
        if (b_len == 0) return a_len;
        int[] v0 = new int[b_len + 1];
        int[] v1 = new int[b_len + 1];
        for (int i = 0; i < v0.length; i++) {
            v0[i] = i;
        }
        for (int i = 0; i < a_len; i++) {
            v1[0] = i + 1;
            for (int j = 0; j < b_len; j++) {
                int cost = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
                v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
            }
            int[] tmp = v1;
            v1 = v0;
            v0 = tmp;
        }
        return v0[b_len];
    }

    // returns the (most likely) match for a given symbol
    public String getIRIMatch(String ontologyPath, String symbol) throws Exception {
        // check if symbol is a valid IRI
        // use URL pattern according to (RFC2396)
        // https://stackoverflow.com/a/13958706
        //String urlPattern = "/^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?/";
        //if (symbol.matches(urlPattern)) {
        //    return "IRI__" + symbol;
        //}
        if (labelToIRIMapping.get(ontologyPath) == null) {
            labelToIRIMapping.put(ontologyPath, getLabelToIRIMapping(OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(ontologyPath))));
        }

        HashMap<String, String> ontologyLabelMapping = labelToIRIMapping.get(ontologyPath);
        if (ontologyLabelMapping.isEmpty()) {
            System.out.println("No IRI found for " + symbol);
            return symbol;
        }
        // check for exact matches
        if (ontologyLabelMapping.containsKey(symbol)) {
            return ontologyLabelMapping.get(symbol);
        }
        if (ontologyLabelMapping.containsValue(symbol)) {
            return symbol;
        }
        // use set of all labels and IRIs
        String[] labels = ontologyLabelMapping.keySet().toArray(new String[0]);
        String[] iris = ontologyLabelMapping.values().toArray(new String[0]);
        // find most similar match if exact match doesn't exists
        int lowest_lev_dist = MAX_LEV_DIST;
        ArrayList<String> bestMatches = new ArrayList<>();
        for (int i = 0; i < labels.length + iris.length; i++) {
            // iterate through labels and iris
            String match = i < labels.length ? labels[i] : iris[i-labels.length];
            // sort out very too apart labels
            if (Math.abs(match.length() - symbol.length()) > lowest_lev_dist) {
                continue;
            }
            //System.out.println(labels[i]);
            int lev_dist = levDist(match, symbol);
            //System.out.println("Distance between " + labels[i] + " and " + symbol + ": " + lev_dist);
            if (lev_dist < lowest_lev_dist) {
                lowest_lev_dist = lev_dist;
                bestMatches.clear();
                bestMatches.add(i < labels.length ? ontologyLabelMapping.get(match) : match);
            } else if (lev_dist == lowest_lev_dist) {
                bestMatches.add(i < labels.length ? ontologyLabelMapping.get(match) : match);
            }
        }

        if (bestMatches.isEmpty()) {
            System.out.println("No match found for " + symbol + " with a distance <= " + MAX_LEV_DIST);
            return null;
        }

        if (bestMatches.size() > 1) {
            System.out.println("No perfect match found for " + symbol + ". The closest labels are " + bestMatches
                + "  (distance: " + lowest_lev_dist + "). " + bestMatches.get(0) + " was chosen.");
        }
        else {
            System.out.println("No perfect match found for " + symbol + ". The closest label is " + bestMatches.get(0)
                + " (distance: " + lowest_lev_dist + ")");
        }

        return bestMatches.get(0);
    }

    public boolean owlOntologyEntails(String premise_path, String conclusion_path) throws OWLOntologyCreationException {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology conclusion_ontology = m.loadOntologyFromOntologyDocument(new File(conclusion_path));
        ReasonerFactory rf = new ReasonerFactory();
        OWLReasoner r = rf.createReasoner(m.loadOntologyFromOntologyDocument(new File(premise_path)));
        return r.isEntailed(conclusion_ontology.axioms());
    }
}
