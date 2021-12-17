package translation;

import fol.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.vocab.OWLFacet;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OntologyTranslator {
    OWLOntology ontology;
    boolean verbose = false;

    OntologyTranslator(OWLOntology ontology) {
        this.ontology = ontology;
    }

    OntologyTranslator(OWLOntology ontology, boolean verbose) {
        this(ontology);
        this.verbose = verbose;
    }


    public ArrayList<AnnotatedLogicElement> translate() {
        // add mandatory background axioms
        ArrayList<AnnotatedLogicElement> result = new ArrayList<>(getMandatoryBackgroundAxioms());
        ArrayList<AnnotatedLogicElement> axiomsContainingAnonymousIndividuals = new ArrayList<>();
        Stream<OWLAnonymousIndividual> anonymousIndividuals = ontology.anonymousIndividuals();

        //check if certain keywords get used
        boolean nothing = false,
            topObjectProperty = false,
            bottomObjectProperty = false,
            topDataProperty = false,
            bottomDataProperty = false;
        OWLDataFactory df = OWLManager.getOWLDataFactory();

        // ontology translation
        for (OWLAxiom axiom : ontology.axioms(Imports.INCLUDED).collect(Collectors.toSet())) {
            Set<OWLEntity> signature = axiom.signature().collect(Collectors.toSet());
            if (signature.stream().anyMatch(e -> e.equals(df.getOWLNothing()))) nothing = true;
            if (signature.stream().anyMatch(e -> e.equals(df.getOWLTopObjectProperty()))) topObjectProperty = true;
            if (signature.stream().anyMatch(e -> e.equals(df.getOWLBottomObjectProperty())))
                bottomObjectProperty = true;
            if (signature.stream().anyMatch(e -> e.equals(df.getOWLTopDataProperty()))) topDataProperty = true;
            if (signature.stream().anyMatch(e -> e.equals(df.getOWLBottomDataProperty()))) bottomDataProperty = true;

            try {
                OWLTranslator.variableCounter = 0;
                ArrayList<LogicElement> translated = axiom.accept(new OWLAxiomTranslator());
                for (LogicElement t : translated) {
                    if (axiom.anonymousIndividuals().count() > 0) {
                        axiomsContainingAnonymousIndividuals.add(new AnnotatedLogicElement(t, axiom.toString()));
                    }
                    result.add(new AnnotatedLogicElement(t, axiom.toString()));
                }

            } catch (NullPointerException e) {
                if (verbose) System.out.println("Implementation in OWLAxiomTranslator is missing");
                if (verbose) System.out.println("\tAxiom >> " + axiom + " << was not translated");
            }
        }

        // if anonymous individuals exist: merge all axioms containing them into a single axiom, then existentially quantify
        // over the anonymous individuals
        if (anonymousIndividuals.count() > 0 && axiomsContainingAnonymousIndividuals.size() > 0) {
            LogicElement conjunction = axiomsContainingAnonymousIndividuals.get(0).getFirst();
            StringBuilder annotation = new StringBuilder("\\exists " + anonymousIndividuals.collect(Collectors.toList())
                + ": " + axiomsContainingAnonymousIndividuals.get(0).getSecond());
            for (int i = 1; i < axiomsContainingAnonymousIndividuals.size(); i++) {
                conjunction = new BinaryFormula(conjunction, new BinaryConnective(0),
                    axiomsContainingAnonymousIndividuals.get(i).getFirst());
                annotation.append(" & ").append(axiomsContainingAnonymousIndividuals.get(i).getSecond());
            }
            result.add(new AnnotatedLogicElement(
                new QuantifiedFormula(
                    new Quantifier(1),
                    anonymousIndividuals.map(x -> new Variable(x.toStringID())).toArray(Variable[]::new),
                    conjunction),
                annotation.toString()));
        }

        // add background axioms needed only if the keyword is used
        result.addAll(getOptionalBackgroundAxioms(nothing, topObjectProperty, bottomObjectProperty,
            topDataProperty, bottomDataProperty));

        return result;
    }

    private ArrayList<AnnotatedLogicElement> getMandatoryBackgroundAxioms() {
        ArrayList<AnnotatedLogicElement> result = new ArrayList<>();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Variable x = new Variable("X");
        Variable y = new Variable("Y");
        // domain consists of objects and data
        result.add(new AnnotatedLogicElement(new QuantifiedFormula(
            new Quantifier(0), // universal Quantifier
            new Variable[]{x},
            new BinaryFormula(
                df.getOWLThing().accept(new OWLClassExpressionTranslator(x)),
                new BinaryConnective(1), // disjunction
                df.getTopDatatype().accept(new OWLDataTranslator(x))
            )
        ), "background axiom: domain consists of objects and data"));
        // object domain and data domain are disjoint
        result.add(new AnnotatedLogicElement(new QuantifiedFormula(
            new Quantifier(0),
            new Variable[]{x},
            new BinaryFormula(
                df.getOWLThing().accept(new OWLClassExpressionTranslator(x)),
                new BinaryConnective(3), // implication
                new UnaryFormula(
                    new UnaryConnective(0), // negation
                    df.getTopDatatype().accept(new OWLDataTranslator(x))
                )
            )
        ), "background axiom: object domain and data domain are disjoint"));
        // there are things
        result.add(new AnnotatedLogicElement(new QuantifiedFormula(
            new Quantifier(1), // existential quantifier
            new Variable[]{x},
            df.getOWLThing().accept(new OWLClassExpressionTranslator(x))
        ), "background axiom: there are things"));
        // there are literals
        result.add(new AnnotatedLogicElement(new QuantifiedFormula(
            new Quantifier(1), // existential quantifier
            new Variable[]{x},
            df.getTopDatatype().accept(new OWLDataTranslator(x))
        ), "background axiom: there are literals"));

        // facets
        for (IRI facet : OWLFacet.getFacetIRIs())
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{x, y},
                new BinaryFormula(
                    new PredicateExpression(facet.toString(), new Variable[]{x, y}),
                    new BinaryConnective(3), // implication
                    new BinaryFormula(
                        df.getTopDatatype().accept(new OWLDataTranslator(x)),
                        new BinaryConnective(0), // conjunction
                        df.getTopDatatype().accept(new OWLDataTranslator(y))
                    )
                )
            ), "facets can only be applied to literals"));

        return result;
    }

    private ArrayList<AnnotatedLogicElement> getOptionalBackgroundAxioms(boolean nothing, boolean topObject,
                                                                         boolean bottomObject, boolean topData,
                                                                         boolean bottomData) {
        ArrayList<AnnotatedLogicElement> result = new ArrayList<>();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Variable x = new Variable("X");
        Variable y = new Variable("Y");

        // there are no instances of owl:Nothing
        if (nothing)
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{x},
                new UnaryFormula(
                    new UnaryConnective(0),
                    df.getOWLNothing().accept(new OWLClassExpressionTranslator(x))
                )
            ), "background axiom: owl:Thing has no instances"));

        // all pairs of things are connected via owl:topObjectProperty
        if (topObject)
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    df.getOWLTopObjectProperty().accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(2), // biimplication
                    new BinaryFormula(
                        df.getOWLThing().accept(new OWLClassExpressionTranslator(x)),
                        new BinaryConnective(0), // conjunction
                        df.getOWLThing().accept(new OWLClassExpressionTranslator(y))
                    )
                )
            ), "background axiom: all pairs of things are connected via owl:topObjectProperty"));
        // there are no instances of owl:bottomObjectProperty
        if (bottomObject)
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new UnaryFormula(
                    new UnaryConnective(0),
                    df.getOWLBottomObjectProperty().accept(new OWLPropertyExpressionTranslator(x, y))
                )
            ), "background axiom: there are no instances of owl:bottomObjectProperty"));

        // all things and literals are connected via owl:topDataProperty
        if (topData)
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    df.getOWLTopDataProperty().accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(2), // biimplication
                    new BinaryFormula(
                        df.getOWLThing().accept(new OWLClassExpressionTranslator(x)),
                        new BinaryConnective(0), // conjunction
                        df.getTopDatatype().accept(new OWLDataTranslator(y))
                    )
                )
            ), "background axiom: all things and literals are connected via owl:topDataProperty"));
        // there are no instances of owl:bottomDataProperty
        if (bottomData)
            result.add(new AnnotatedLogicElement(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new UnaryFormula(
                    new UnaryConnective(0),
                    df.getOWLBottomDataProperty().accept(new OWLPropertyExpressionTranslator(x, y))
                )
            ), "background axiom: there are no instances of owl:bottomDataProperty"));

        return result;
    }
}
