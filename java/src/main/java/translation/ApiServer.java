package translation;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import fol.*;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat;
import org.semanticweb.owlapi.metrics.DLExpressivity;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.InconsistentOntologyException;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.ShortFormFromRDFSLabelAxiomListProvider;
import py4j.GatewayServer;
import py4j.Py4JNetworkException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Integer.parseInt;

public class ApiServer {

    public HashMap<String, HashMap<String, String>> labelToIRIMapping = new HashMap<>();
    public HashMap<String, HashMap<String, String>> iriToReadableMapping = new HashMap<>();
    public HashMap<String, Map<String, String>> iriToCurieMapping = new HashMap<>();

    public static void main(String[] args) throws Exception {
        //arg0 is the java port, arg1 the python port
        try {
            //bfoFetchClifAnnotations();
            //bfoWriteClifAnnotations();
            ApiServer app = new ApiServer(parseInt(args[0]), parseInt(args[1]));
            //System.out.println(app.getIRIMatch("c/Users/simon/OneDrive/Hiwi/python-gavel-owl/ba_architecture_example.omn",
            //    "https://github.com/gavel-tool/python-gavel-owl/tptp_annotation"));
            //app.addClassInstantiationToOntology("../oeo-annot-bfo.omn", "../oeo-annot-bfo-class-instantiation.omn");
            /*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntology ontology =
                m.loadOntologyFromOntologyDocument(new File("../ba_architecture_example.omn"));
            System.out.println(app.generateIRIToCurieMapping(ontology));


            /*OWLOntologyManager m = OWLManager.createOWLOntologyManager();
            OWLOntology ontology =
                m.loadOntologyFromOntologyDocument(new File("iri-to-name-demo.ofn"));

            for (Map.Entry<String, String> entry : app.getIRItoReadableNameMapping(ontology).entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }

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

    public void addClassInstantiationToOntology(String inputPath, String outputPath)
        throws OWLOntologyCreationException, FileNotFoundException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File(inputPath));
        OWLDataFactory df = man.getOWLDataFactory();

        int index = 0;
        for (OWLEntity owlClass : ontology.signature().filter(AsOWLClass::isOWLClass).collect(Collectors.toList())) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual("http://www.example.org/",
                "ind_" + index++);
            OWLAxiom declAxiom = df.getOWLDeclarationAxiom(ind);
            OWLAxiom classAssertAxiom = df.getOWLClassAssertionAxiom((OWLClassExpression) owlClass, ind);
            ontology.add(declAxiom, classAssertAxiom);
        }
        ontology.saveOntology(new FileOutputStream(outputPath));
    }

    public static void bfoFetchClifAnnotations() throws OWLOntologyCreationException, IOException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = man.loadOntologyFromOntologyDocument(new File("bfo2-0ro.owl"));
        OWLDataFactory df = man.getOWLDataFactory();
        OWLAnnotationProperty bfoClif = df.getOWLAnnotationProperty("http://purl.obolibrary.org/obo/IAO_0000602");
        OWLAnnotationProperty bfoClifLabel =
            df.getOWLAnnotationProperty("http://purl.obolibrary.org/obo/BFO_0000180");
        OWLAnnotationProperty rdfsLabel =
            df.getOWLAnnotationProperty("http://www.w3.org/2000/01/rdf-schema#label");

        List<OWLAnnotationAssertionAxiom> bfoClifAnnots = ontology.axioms(AxiomType.ANNOTATION_ASSERTION)
            .filter(x -> x.getProperty().equals(bfoClif)).collect(Collectors.toList());
        CSVWriter writer = new CSVWriter(new FileWriter("bfo2-0clif_axioms.csv"));

        // map bfo-specific clif-labels to rdfs-labels
        List<OWLAnnotationAssertionAxiom> allBfoRdfsLabels = ontology.axioms(AxiomType.ANNOTATION_ASSERTION)
            .filter(x -> x.getProperty().equals(rdfsLabel))
            .collect(Collectors.toList());
        List<OWLAnnotationAssertionAxiom> allClifLabels = ontology.axioms(AxiomType.ANNOTATION_ASSERTION)
            .filter(x -> x.getProperty().equals(bfoClifLabel))
            .collect(Collectors.toList());
        HashMap<String, String> clifToRdfsMapping = new HashMap<>();
        for (OWLAnnotationAssertionAxiom cl: allClifLabels) {
            for (OWLAnnotationAssertionAxiom rl : allBfoRdfsLabels) {
                if (rl.getSubject().equals(cl.getSubject())) {
                    System.out.println("Match found: " + cl.getValue().toString() + ", " + rl.getValue().toString());
                    clifToRdfsMapping.put(cl.getValue().asLiteral().get().getLiteral(), rl.getValue().asLiteral().get().getLiteral());
                }
            }
        }
        clifToRdfsMapping = new HashMap<>();
        // add manual mappings for entities which are not part of BFO-OWL, put appear in clif axioms
        clifToRdfsMapping.put("processProfileOf", "process profile of");
        clifToRdfsMapping.put("bearerOfAt", "bearer of");
        clifToRdfsMapping.put("locatedInAt", "located in");
        clifToRdfsMapping.put("SpatioTemporalRegion", "spatiotemporal region"); // mistake in BFO
        clifToRdfsMapping.put("occupiesSpatioTemporalRegion", "occupies spatiotemporal region");
        clifToRdfsMapping.put("occupiesTemporalRegion", "occupies temporal region");
        clifToRdfsMapping.put("specificallyDependsOnAt", "specifically depends on");
        clifToRdfsMapping.put("specificallyDepends", "specifically depends on"); // probably mistake in BFO
        clifToRdfsMapping.put("genericallyDependsOnAt", "generically depends on");

        clifToRdfsMapping.put("continuantPartOfAt", "part of");
        clifToRdfsMapping.put("hasContinuantPartOfAt", "part of"); // probably mistake in BFO
        clifToRdfsMapping.put("occurrentPartOf", "part of");
        clifToRdfsMapping.put("memberPartOfAt", "member of");
        clifToRdfsMapping.put("temporalPartOf", "part of");
        clifToRdfsMapping.put("properContinuantPartOf", "proper part of");
        clifToRdfsMapping.put("properContinuantPartOfAt", "proper part of");
        clifToRdfsMapping.put("properOccurrentPartOf", "proper part of");
        clifToRdfsMapping.put("properTemporalPartOf", "proper part of");

        clifToRdfsMapping.put("Material Entity", "material entity"); // mistake in BFO
        clifToRdfsMapping.put("existsAt", "exists at");
        clifToRdfsMapping.put("qualityOfAt", "quality of");
        clifToRdfsMapping.put("inheresIn", "inheres in");
        clifToRdfsMapping.put("spatiallyProjectsOntoAt", "spatially projects onto");
        clifToRdfsMapping.put("temporallyProjectsOnto", "temporally projects onto");


        // sort list by length
        // avoid e.g. replacing IndependentContinuant with Independent'continuant' instead of 'independent continuant'
        List<Map.Entry<String, String>> sortedClifToRdfsEntrySet = clifToRdfsMapping.entrySet().stream()
            .sorted(new Comparator<Map.Entry<String, String>>() {
            @Override
            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return o2.getKey().length() - o1.getKey().length();
            }
        }).collect(Collectors.toList());
        System.out.println(sortedClifToRdfsEntrySet);

        for (Map.Entry<String, String> entry : clifToRdfsMapping.entrySet()) {
            System.out.println(entry.getKey() + " & " + entry.getValue() + " &  &  \\\\\n\\hline");
        }

        // generate csv lines: subject IRI, subject clif label, subject rdfs label, clif annotation value from bfo, adapted clif annotation
        for (OWLAnnotationAssertionAxiom ax: bfoClifAnnots) {
            List<OWLAnnotationAssertionAxiom> clifLabel = ontology.axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(x -> x.getProperty().equals(bfoClifLabel) && x.getSubject().equals(ax.getSubject()))
                .collect(Collectors.toList());
            List<OWLAnnotationAssertionAxiom> bfoRdfsLabel = ontology.axioms(AxiomType.ANNOTATION_ASSERTION)
                .filter(x -> x.getProperty().equals(rdfsLabel) && x.getSubject().equals(ax.getSubject()))
                .collect(Collectors.toList());
            String subjLabel = clifLabel.get(0).getValue().asLiteral().get().getLiteral();
            String subjRdfsLabel = bfoRdfsLabel.get(0).getValue().asLiteral().get().getLiteral();

            String value = ax.getValue().toString();
            String valueSanitized = value.substring(0, value.indexOf("//") - 1).replace('"', ' ');
            for (Map.Entry<String, String> set : sortedClifToRdfsEntrySet) {
                if (valueSanitized.contains(set.getKey())) {
                    System.out.println(set.getKey());
                    System.out.println(valueSanitized.indexOf(set.getKey()));
                }
                valueSanitized = valueSanitized.replace(set.getKey(), "'" + set.getValue() + "'");
            }

            //System.out.println(labelProvider.getShortForm(ax.getSubject().));
            writer.writeNext(new String[]{
               ax.getSubject().toString(),
               subjLabel,
               subjRdfsLabel,
               value,
               valueSanitized
           });
            writer.flush();
        }
        //ontology.axioms(AxiomType.ANNOTATION_ASSERTION).filter(x -> x.getProperty().equals(bfoClif)).forEach(ax -> writer.writeNext(new String[]{ax.getSubject().toString(), ax.getValue().toString(), ax.getValue().toString().substring(0, ax.getValue().toString().indexOf("//"))}));
    }

    // adds clif annotations from csv file to owl ontology
    public static void bfoWriteClifAnnotations() throws OWLOntologyCreationException, IOException,
        CsvValidationException, OWLOntologyStorageException {
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ontology =
            man.loadOntologyFromOntologyDocument(new File("bfo-2-0-clif-annotations-module.ofn"));
        OWLDataFactory df = man.getOWLDataFactory();
        OWLAnnotationProperty clifAnnot =
            df.getOWLAnnotationProperty("http://www.example.org/bfo-2-0-clif-annotations-module/clif-annotation");
        CSVReader reader = new CSVReader(new FileReader("bfo2-0clif_axioms.csv"));
        ontology.removeAxioms(ontology.axioms(AxiomType.ANNOTATION_ASSERTION).filter(x -> x.getProperty().equals(clifAnnot)));
        String[] line;
        while ((line = reader.readNext()) != null) {
            if (line.length >= 5) {
                IRI subj = IRI.create(line[0]);
                OWLLiteral value = df.getOWLLiteral(line[4]);
                OWLAnnotationAssertionAxiom ax = df.getOWLAnnotationAssertionAxiom(clifAnnot, subj, value);
                ontology.add(ax);
            }
        }
        man.saveOntology(ontology, new FunctionalSyntaxDocumentFormat(),
            new FileOutputStream("bfo-2-0-clif-annotations-module.ofn"));
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

    private HashMap<String, String> getLabelToIRIMapping(OWLOntology ontology) {
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
    // check 3 things for each entity: IRI, label, identifier (i.e., suffix of IRI)
    // if one of them matches symbol exactly, return it,
    // else look for match with Levenshtein distance < 1 (if symbol.length < 8) or < 2 (else)
    public String getIRIMatch(String ontologyPath, String symbol) throws Exception {
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

        // exact match with identifiers
        for (String iri : iris) {
            if (iri.endsWith(symbol)) {
                return iri;
            }
        }
        // find most similar match if exact match doesn't exists
        int max_dist = symbol.length() < 8 ? 1 : 2;
        ArrayList<String> bestMatches = new ArrayList<>();
        for (int i = 0; i < labels.length + iris.length; i++) {
            // iterate through labels and iris
            String match = i < labels.length ? labels[i] : iris[i - labels.length];
            // sort out very too apart labels
            if (Math.abs(match.length() - symbol.length()) > max_dist && i >= labels.length) {
                continue;
            }
            //System.out.println(labels[i]);
            // use suffixes of iris
            int lev_dist = levDist(i >= labels.length ? match.substring(match.length() - symbol.length()) : match, symbol);
            //System.out.println("Distance between " + labels[i] + " and " + symbol + ": " + lev_dist);
            if (lev_dist < max_dist) {
                max_dist = lev_dist;
                bestMatches.clear();
                bestMatches.add(i < labels.length ? ontologyLabelMapping.get(match) : match);
            } else if (lev_dist == max_dist) {
                bestMatches.add(i < labels.length ? ontologyLabelMapping.get(match) : match);
            }
        }

        if (bestMatches.isEmpty()) {
            System.out.println("No match found for " + symbol + " with a distance <= " + max_dist);
            return null;
        }

        if (bestMatches.size() > 1) {
            System.out.println("No perfect match found for " + symbol + ". The closest labels are " + bestMatches
                + "  (distance: " + max_dist + "). " + bestMatches.get(0) + " was chosen.");
        }
        else {
            System.out.println("No perfect match found for " + symbol + ". The closest label is " + bestMatches.get(0)
                + " (distance: " + max_dist + ")");
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

    /**
     * for every IRI in the ontology: map to label; if no label exists, use identifier, if multiple labels have the
     * same value, use identifier and label
     */
    private HashMap<String, String> generateIRItoReadableNameMapping(OWLOntology ontology) {
        ShortFormFromRDFSLabelAxiomListProvider labelProvider = new ShortFormFromRDFSLabelAxiomListProvider(
            new ArrayList<>(),
            ontology.axioms(Imports.INCLUDED).collect(Collectors.toList()));
        HashMap<String, String> mapping = new HashMap<>();

        for (OWLEntity entity : ontology.signature(Imports.INCLUDED).collect(Collectors.toList())) {
            String iri = entity.toStringID();
            String label = labelProvider.getShortForm(entity);
            // use identifier if no label exists
            if (label.isEmpty()) {
                label = this.shortenName(iri);
            }
            if (!mapping.containsValue(label)) {
                mapping.put(iri, label);
            } else {
                // find entry with same label, replace label with identifier + label
                for (Map.Entry<String, String> entry : mapping.entrySet()) {
                    if (entry.getValue().equals(label)) {
                        entry.setValue(entry.getValue() + " (" + this.shortenName(entry.getKey()) + ")");
                    }
                }
                mapping.put(iri, label + " (" + this.shortenName(iri) + ")");
            }
        }
        return mapping;
    }

    public HashMap<String, String> getReadableNameMapping(String ontologyPath) throws OWLOntologyCreationException {
        if (iriToReadableMapping.get(ontologyPath) == null) {
            iriToReadableMapping.put(ontologyPath, generateIRItoReadableNameMapping(OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(ontologyPath))));
        }
        return iriToReadableMapping.get(ontologyPath);
    }

    public String getReadableName(String ontologyPath, String iri) throws OWLOntologyCreationException {
        if (iriToReadableMapping.get(ontologyPath) == null) {
            iriToReadableMapping.put(ontologyPath, generateIRItoReadableNameMapping(OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(ontologyPath))));
        }
        return iriToReadableMapping.get(ontologyPath).get(iri);
    }

    // get part after last / or #, but at least 3 characters (e.g. http://example.org/part_of -> part_of)
    private String shortenName(String name) {
        int cut = 0;
        if (name.length() > 2) {
            String nameMinus3 = name.substring(0, name.length() - 3);
            int slash = nameMinus3.lastIndexOf('/');
            int hashtag = nameMinus3.lastIndexOf('#');
            if (slash > 0) {
                cut = slash + 1;
            }
            if (hashtag > slash) {
                cut = hashtag + 1;
            }
        }
        return name.substring(cut);
    }

    public String getOntologyIri(String ontologyPath) throws OWLOntologyCreationException {
        return OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(ontologyPath))
            .getOntologyID().getOntologyIRI().get().toString();
    }

    public String getDOLconformantOntology(String ontologyPath) throws OWLOntologyCreationException, OWLOntologyStorageException {
        OWLOntology ontology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(ontologyPath));
        OWLDocumentFormat outputSyntax = new ManchesterSyntaxDocumentFormat();

        // hets needs explicitly typed literals, which are not supported by OWL API if the data type is rdf:PlainLiteral
        // TODO: insert placeholder for each rdf:PlainLiteral literal without a language tag,
        //  after rendering replace placeholder with tag
        // TODO: find a method to filter the ontology for literals
        if (ontology.getFormat() != null && ontology.getFormat().isPrefixOWLDocumentFormat()) {
            outputSyntax.asPrefixOWLDocumentFormat().copyPrefixesFrom(ontology.getFormat().asPrefixOWLDocumentFormat());
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ontology.saveOntology(outputSyntax, outputStream);

        return outputStream.toString();
    }

    private Map<String, String> generateIRIToCurieMapping(OWLOntology ontology) throws OWLOntologyCreationException, OWLOntologyStorageException {

        /*DefaultPrefixManager pm = new DefaultPrefixManager(null, null, ontology.getOntologyID().getOntologyIRI().get().toString());
        pm.prefixNames().forEach(name -> pm.setPrefix(name, ""));

        System.out.println(pm.getDefaultPrefix());
        System.out.println(pm.getPrefixName2PrefixMap());
        ontology.signature().forEach(entity -> System.out.println(pm.getPrefixIRI(entity.getIRI())));
        OWLDocumentFormat outputSyntax = new ManchesterSyntaxDocumentFormat();
        outputSyntax.asPrefixOWLDocumentFormat().clear();
        outputSyntax.asPrefixOWLDocumentFormat().setPrefixManager(pm);
        System.out.println("--");
        outputSyntax.asPrefixOWLDocumentFormat().prefixNames().forEach(System.out::println);
        System.out.println("---");
        System.out.println(outputSyntax.isAddMissingTypes());
        outputSyntax.setAddMissingTypes(true);

        ontology.getFormat().asPrefixOWLDocumentFormat().clear();
        OWLDocumentFormat manSyn = new ManchesterSyntaxDocumentFormat();
        System.out.println(manSyn.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap());
        manSyn.asPrefixOWLDocumentFormat().copyPrefixesFrom(ontology.getFormat().asPrefixOWLDocumentFormat());
        manSyn.asPrefixOWLDocumentFormat().clear();
        System.out.println(manSyn.asPrefixOWLDocumentFormat().getPrefixName2PrefixMap());
        ontology.saveOntology(System.out);
        */
        Map<String, String> iriToCurieMap = new HashMap<>();

        Set<Map.Entry<String, String>> prefixNameToPrefix = ontology.getFormat().asPrefixOWLDocumentFormat().getPrefixName2PrefixMap().entrySet();
        List<OWLEntity> entities = ontology.signature().collect(Collectors.toList());
        for (OWLEntity entity : entities) {
            for (Map.Entry<String, String> entry : prefixNameToPrefix) {
                if (entry.getValue().equals(entity.getIRI().getNamespace())) {
                    // special case: for empty prefix, remainder without ":" is used
                    if (entry.getKey().equals(":")) {
                        iriToCurieMap.put(entity.getIRI().toString(), entity.getIRI().getRemainder().orElse(""));
                    } else {
                        iriToCurieMap.put(entity.getIRI().toString(), entry.getKey() + entity.getIRI().getRemainder().orElse(""));
                    }
                }
            }
        }
        return iriToCurieMap;
    }

    public Map<String, String> getIRIToCurieMapping(String ontologyPath) throws OWLOntologyCreationException, OWLOntologyStorageException {
        if (iriToCurieMapping.get(ontologyPath) == null) {
            iriToCurieMapping.put(ontologyPath, generateIRIToCurieMapping(OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(ontologyPath))));
        }
        return iriToCurieMapping.get(ontologyPath);
    }

    public String getCurie(String ontologyPath, String iri) throws OWLOntologyCreationException, OWLOntologyStorageException {
        if (iriToCurieMapping.get(ontologyPath) == null) {
            iriToCurieMapping.put(ontologyPath, generateIRIToCurieMapping(OWLManager.createOWLOntologyManager()
                .loadOntologyFromOntologyDocument(new File(ontologyPath))));
        }
        return iriToCurieMapping.get(ontologyPath).get(iri);
    }
}
