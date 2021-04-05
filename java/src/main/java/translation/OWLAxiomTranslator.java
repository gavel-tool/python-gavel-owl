package translation;

import fol.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class OWLAxiomTranslator extends OWLTranslator implements OWLAxiomVisitorEx<ArrayList<LogicElement>> {

    //SubClassOf
    public ArrayList<LogicElement> visit(@Nonnull OWLSubClassOfAxiom axiom) {
        // Translation: "A SubclassOf B" to  "\all x(A(x) -> B(x))"

        Variable var1 = getUniqueVariable();

        LogicElement child = axiom.getSubClass().accept(new OWLClassExpressionTranslator(var1));
        LogicElement parent = axiom.getSuperClass().accept(new OWLClassExpressionTranslator(var1));

        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1},
            new BinaryFormula(
                child,
                new BinaryConnective(3), // implication
                parent)));
        return res;
    }

    //Equivalent Classes
    @Override
    public ArrayList<LogicElement> visit(OWLEquivalentClassesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        List<OWLClassExpression> ops = axiom.getOperandsAsList();
        OWLClassExpression reference = ops.get(0);
        //loop over each class and connects it to the reference class
        for (int i = 1; i < ops.size(); i++) {
            Variable var1 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1},
                new BinaryFormula(
                    reference.accept(new OWLClassExpressionTranslator(var1)),
                    new BinaryConnective(2), // biimplication
                    ops.get(i).accept(new OWLClassExpressionTranslator(var1)))));
        }
        return res;
    }

    //Disjoint Classes
    @Override
    public ArrayList<LogicElement> visit(OWLDisjointClassesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();

        //loop over each pair of disjoint classes
        for (OWLDisjointClassesAxiom pairwiseAx : axiom.splitToAnnotatedPairs()) {
            OWLClassExpression[] operands = pairwiseAx.operands().toArray(OWLClassExpression[]::new);
            Variable var1 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1},
                new UnaryFormula(
                    new UnaryConnective(0), // negation
                    new BinaryFormula(
                        operands[0].accept(new OWLClassExpressionTranslator(var1)),
                        new BinaryConnective(0), // conjunction
                        operands[1].accept(new OWLClassExpressionTranslator(var1))))));
        }
        return res;
    }

    //Disjoint Union
    @Override
    public ArrayList<LogicElement> visit(OWLDisjointUnionAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        res.addAll(axiom.getOWLEquivalentClassesAxiom().accept(new OWLAxiomTranslator()));
        res.addAll(axiom.getOWLDisjointClassesAxiom().accept(new OWLAxiomTranslator()));
        return res;
    }

    //SubObject Property Of
    @Override
    public ArrayList<LogicElement> visit(OWLSubObjectPropertyOfAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getSubProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getSuperProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)))));
        return res;
    }

    //SubProperty Chain Of
    @Override
    public ArrayList<LogicElement> visit(OWLSubPropertyChainOfAxiom axiom) {
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();

        BinaryFormula chain;
        List<OWLObjectPropertyExpression> owlChain = axiom.getPropertyChain();
        ArrayList<Variable> vars = new ArrayList<>();
        Variable x1 = getUniqueVariable();
        vars.add(var1);
        vars.add(x1);
        chain = new BinaryFormula(
            owlChain.get(0).accept(new OWLPropertyExpressionTranslator(var1, x1)),
            new BinaryConnective(0), // conjunction
            null);
        BinaryFormula lastMember = chain; //saves the last translated formula so it can get connected to the next

        //loop over remaining elements of the chain
        for (int i = 1; i < owlChain.size() - 1; i++) {
            vars.add(getUniqueVariable()); //create new variable for each element
            BinaryFormula newMember = new BinaryFormula(
                owlChain.get(i).accept(new OWLPropertyExpressionTranslator(vars.get(i), vars.get(i + 1))),
                new BinaryConnective(0), // conjunction
                null);
            lastMember.setRight(newMember);
            lastMember = newMember;
        }
        lastMember.setRight(owlChain.get(owlChain.size() - 1)
            .accept(new OWLPropertyExpressionTranslator(vars.get(vars.size() - 1), var2)));

        vars.remove(var1); // we don't need var1 for the existential quantifier

        // "normal" subProperty part
        LogicElement premise = new QuantifiedFormula(new Quantifier(1), vars.toArray(new Variable[0]), chain);
        // special case: only 1 chain element -> standard subproperty
        if (owlChain.size() == 1) {
            premise = owlChain.get(0).accept(new OWLPropertyExpressionTranslator(var1, var2));
        }
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // 0 = universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                premise,
                // 1 = existential quantifier
                new BinaryConnective(3), // 3 = implication
                axiom.getSuperProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)))));
        return res;
    }

    //Equivalent Object Properties
    @Override
    public ArrayList<LogicElement> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        List<OWLObjectPropertyExpression> ops = axiom.getOperandsAsList();
        OWLObjectPropertyExpression reference = ops.get(0);
        //loops over each property expression and connects it to the reference expression
        for (int i = 1; i < ops.size(); i++) {
            Variable var1 = getUniqueVariable();
            Variable var2 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1, var2},
                new BinaryFormula(
                    reference.accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(2), // biimplication
                    ops.get(i).accept(new OWLPropertyExpressionTranslator(var1, var2)))));
        }
        return res;
    }

    //Disjoint Object Properties
    @Override
    public ArrayList<LogicElement> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();

        //loops over each pair of axioms
        for (OWLDisjointObjectPropertiesAxiom pairwiseAx : axiom.splitToAnnotatedPairs()) {
            OWLObjectPropertyExpression[] operands = pairwiseAx.operands().toArray(OWLObjectPropertyExpression[]::new);
            Variable var1 = getUniqueVariable();
            Variable var2 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1, var2},
                new UnaryFormula(
                    new UnaryConnective(0), // negation
                    new BinaryFormula(
                        operands[0].accept(new OWLPropertyExpressionTranslator(var1, var2)),
                        new BinaryConnective(0), // conjunction
                        operands[1].accept(new OWLPropertyExpressionTranslator(var1, var2))))));
        }
        return res;
    }

    //Inverse Object Properties
    @Override
    public ArrayList<LogicElement> visit(OWLInverseObjectPropertiesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getFirstProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(2), // biimplication
                axiom.getSecondProperty().accept(new OWLPropertyExpressionTranslator(var2, var1)))));
        return res;
    }

    //Object Property Domain
    @Override
    public ArrayList<LogicElement> visit(OWLObjectPropertyDomainAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getDomain().accept(new OWLClassExpressionTranslator(var1)))));
        return res;
    }

    //Object Property Range
    @Override
    public ArrayList<LogicElement> visit(OWLObjectPropertyRangeAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getRange().accept(new OWLClassExpressionTranslator(var2)))));
        return res;
    }

    //Functional Object Property
    @Override
    public ArrayList<LogicElement> visit(OWLFunctionalObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        Variable var3 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2, var3},
            new BinaryFormula(
                new BinaryFormula(
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(0), // conjunction
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var3))),
                new BinaryConnective(3), // implication
                new BinaryFormula(var2, new BinaryConnective(8), var3)))); // 8 = equality
        // TODO: find out if equality should really be a binary connective and not a predicate
        return res;
    }

    //Inverse Functional Object Property
    @Override
    public ArrayList<LogicElement> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        OWLFunctionalObjectPropertyAxiom inv = df.getOWLFunctionalObjectPropertyAxiom(
            axiom.getProperty().getInverseProperty());
        return inv.accept(new OWLAxiomTranslator());
    }

    //ReflexiveObjectProperty
    @Override
    public ArrayList<LogicElement> visit(OWLReflexiveObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1},
            axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var1))));
        return res;
    }

    //Irreflexive Object Property
    @Override
    public ArrayList<LogicElement> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1},
            new UnaryFormula(new UnaryConnective(0), // negation
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var1)))));
        return res;
    }

    //Symmetric Object Property
    @Override
    public ArrayList<LogicElement> visit(OWLSymmetricObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var2, var1)))));
        return res;
    }

    //Asymmetric Object Property
    public ArrayList<LogicElement> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new UnaryFormula(
                new UnaryConnective(0), // negation
                new BinaryFormula(
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(0), // conjunction
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var2, var1))))));
        return res;
    }

    //Transitive Object Property
    public ArrayList<LogicElement> visit(OWLTransitiveObjectPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        Variable var3 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2, var3},
            new BinaryFormula(
                new BinaryFormula(
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(0), // conjunction
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var2, var3))),
                new BinaryConnective(3), // implication
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var3)))));
        return res;
    }

    //Same Individual
    @Override
    public ArrayList<LogicElement> visit(OWLSameIndividualAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();

        List<OWLIndividual> individuals = axiom.getIndividualsAsList();
        Constant referenceIndividual = individuals.get(0).accept(new OWLIndividualTranslator());
        for (int i = 1; i < individuals.size(); i++) {
            res.add(new BinaryFormula(
                referenceIndividual,
                new BinaryConnective(8), // equality
                individuals.get(i).accept(new OWLIndividualTranslator())
            ));
        }
        for (OWLIndividual individual : axiom.getIndividualsAsList()) {
            res.add(df.getOWLThing().accept(
                new OWLClassExpressionTranslator(individual.accept(new OWLIndividualTranslator()))));
        }
        return res;
    }

    //Different Individual
    @Override
    public ArrayList<LogicElement> visit(OWLDifferentIndividualsAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        //loops over each pair of different individuals
        for (OWLDifferentIndividualsAxiom pairwiseAx : axiom.splitToAnnotatedPairs()) {
            OWLIndividual[] individuals = pairwiseAx.operands().toArray(OWLIndividual[]::new);
            res.add(new BinaryFormula(
                individuals[0].accept(new OWLIndividualTranslator()),
                new BinaryConnective(9), // inequality
                individuals[1].accept(new OWLIndividualTranslator())));
        }
        for (OWLIndividual individual : axiom.getIndividualsAsList()) {
            res.add(df.getOWLThing().accept(
                new OWLClassExpressionTranslator(individual.accept(new OWLIndividualTranslator()))));
        }
        return res;
    }

    //Class Assertion
    @Override
    public ArrayList<LogicElement> visit(OWLClassAssertionAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(axiom.getClassExpression().accept(new OWLClassExpressionTranslator(
            axiom.getIndividual().accept(new OWLIndividualTranslator()))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getIndividual().accept(new OWLIndividualTranslator()))));
        return res;
    }

    //Object Property Assertion
    @Override
    public ArrayList<LogicElement> visit(OWLObjectPropertyAssertionAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(axiom.getProperty().accept(new OWLPropertyExpressionTranslator(
            axiom.getSubject().accept(new OWLIndividualTranslator()),
            axiom.getObject().accept(new OWLIndividualTranslator()))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getSubject().accept(new OWLIndividualTranslator()))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getObject().accept(new OWLIndividualTranslator()))));
        return res;
    }

    //Negative Object Property Assertion
    @Override
    public ArrayList<LogicElement> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new UnaryFormula(
            new UnaryConnective(0), // negation
            axiom.getProperty().accept(new OWLPropertyExpressionTranslator(
                axiom.getSubject().accept(new OWLIndividualTranslator()),
                axiom.getObject().accept(new OWLIndividualTranslator())))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getSubject().accept(new OWLIndividualTranslator()))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getObject().accept(new OWLIndividualTranslator()))));
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLHasKeyAxiom axiom) {
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        int objPropertyCount = axiom.objectPropertyExpressions().mapToInt(expression -> 1).sum();
        int dataPropertyCount = axiom.dataPropertyExpressions().mapToInt(expression -> 1).sum();
        Variable[] z = new Variable[objPropertyCount];
        Variable[] w = new Variable[dataPropertyCount];
        for (int i = 0; i < z.length; i++) {
            z[i] = new Variable("z" + (i + 1));
        }
        for (int i = 0; i < w.length; i++) {
            w[i] = new Variable("w" + (i + 1));
        }
        ArrayList<OWLObjectPropertyExpression> objectPropertyExpressions =
            axiom.objectPropertyExpressions().collect(Collectors.toCollection(ArrayList::new));
        ArrayList<OWLDataPropertyExpression> dataPropertyExpressions =
            axiom.dataPropertyExpressions().collect(Collectors.toCollection(ArrayList::new));
        ArrayList<LogicElement> elements = new ArrayList<>();
        elements.add(axiom.getClassExpression().accept(new OWLClassExpressionTranslator(var1)));
        elements.add(axiom.getClassExpression().accept(new OWLClassExpressionTranslator(var2)));
        for (int i = 0; i < objPropertyCount; i++) {
            elements.add(objectPropertyExpressions.get(i).accept(new OWLPropertyExpressionTranslator(var1, z[i])));
            elements.add(objectPropertyExpressions.get(i).accept(new OWLPropertyExpressionTranslator(var2, z[i])));
        }
        for (int i = 0; i < dataPropertyCount; i++) {
            elements.add(dataPropertyExpressions.get(i).accept(new OWLPropertyExpressionTranslator(var1, w[i])));
            elements.add(dataPropertyExpressions.get(i).accept(new OWLPropertyExpressionTranslator(var2, w[i])));
        }

        Variable[] allVars = new Variable[2 + dataPropertyCount + objPropertyCount];
        allVars[0] = var1;
        allVars[1] = var2;
        if (objPropertyCount >= 0) System.arraycopy(z, 0, allVars, 2, objPropertyCount);
        for (int i = 0; i < dataPropertyCount; i++) {
            allVars[i + 2 + objPropertyCount] = w[i];
        }
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new QuantifiedFormula(
            new Quantifier(0),
            allVars,
            new BinaryFormula(
                interlinkBinaryFormulas(0, elements.stream()),
                new BinaryConnective(3), // implication
                new BinaryFormula(
                    var1,
                    new BinaryConnective(8), // EQ
                    var2
                )
            )));
        return res;
    }

    // Data Subproperties
    @Override
    public ArrayList<LogicElement> visit(OWLSubDataPropertyOfAxiom axiom) {
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();

        LogicElement child = axiom.getSubProperty().accept(new OWLPropertyExpressionTranslator(var1, var2));
        LogicElement parent = axiom.getSuperProperty().accept(new OWLPropertyExpressionTranslator(var1, var2));

        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                child,
                new BinaryConnective(3), // implication
                parent)));
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLEquivalentDataPropertiesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        List<OWLDataPropertyExpression> ops = axiom.getOperandsAsList();
        OWLDataPropertyExpression reference = ops.get(0);
        //loops over each property expression and connects it to the reference expression
        for (int i = 1; i < ops.size(); i++) {
            Variable var1 = getUniqueVariable();
            Variable var2 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1, var2},
                new BinaryFormula(
                    reference.accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(2), // biimplication
                    ops.get(i).accept(new OWLPropertyExpressionTranslator(var1, var2)))));
        }
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLDisjointDataPropertiesAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();

        //loops over each pair of axioms
        for (OWLDisjointDataPropertiesAxiom pairwiseAx : axiom.splitToAnnotatedPairs()) {
            OWLDataPropertyExpression[] operands = pairwiseAx.operands().toArray(OWLDataPropertyExpression[]::new);
            Variable var1 = getUniqueVariable();
            Variable var2 = getUniqueVariable();
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{var1, var2},
                new UnaryFormula(
                    new UnaryConnective(0), // negation
                    new BinaryFormula(
                        operands[0].accept(new OWLPropertyExpressionTranslator(var1, var2)),
                        new BinaryConnective(0), // conjunction
                        operands[1].accept(new OWLPropertyExpressionTranslator(var1, var2))))));
        }
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLDataPropertyDomainAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getDomain().accept(new OWLClassExpressionTranslator(var1)))));
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLDataPropertyRangeAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2},
            new BinaryFormula(
                axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                new BinaryConnective(3), // implication
                axiom.getRange().accept(new OWLDataTranslator(var2)))));
        return res;
    }

    @Override
    public ArrayList<LogicElement> visit(OWLFunctionalDataPropertyAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        Variable var1 = getUniqueVariable();
        Variable var2 = getUniqueVariable();
        Variable var3 = getUniqueVariable();
        res.add(new QuantifiedFormula(
            new Quantifier(0), // universal quantifier
            new Variable[]{var1, var2, var3},
            new BinaryFormula(
                new BinaryFormula(
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var2)),
                    new BinaryConnective(0), // conjunction
                    axiom.getProperty().accept(new OWLPropertyExpressionTranslator(var1, var3))),
                new BinaryConnective(3), // implication
                new BinaryFormula(var2, new BinaryConnective(8), var3)))); // 8 = equality
        return res;
    }

    //Data Property Assertion
    @Override
    public ArrayList<LogicElement> visit(OWLDataPropertyAssertionAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(axiom.getProperty().accept(new OWLPropertyExpressionTranslator(
            axiom.getSubject().accept(new OWLIndividualTranslator()),
            axiom.getObject().accept(new OWLLiteralTranslator()))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getSubject().accept(new OWLIndividualTranslator()))));
        res.add(df.getTopDatatype().accept(
            new OWLDataTranslator(axiom.getObject().accept(new OWLLiteralTranslator()))));
        return res;
    }

    //Negative Data Property Assertion
    @Override
    public ArrayList<LogicElement> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        ArrayList<LogicElement> res = new ArrayList<>();
        res.add(new UnaryFormula(
            new UnaryConnective(0), // negation
            axiom.getProperty().accept(new OWLPropertyExpressionTranslator(
                axiom.getSubject().accept(new OWLIndividualTranslator()),
                axiom.getObject().accept(new OWLLiteralTranslator())))));
        res.add(df.getOWLThing().accept(
            new OWLClassExpressionTranslator(axiom.getSubject().accept(new OWLIndividualTranslator()))));
        res.add(df.getTopDatatype().accept(
            new OWLDataTranslator(axiom.getObject().accept(new OWLLiteralTranslator()))));
        return res;
    }

    // add background axioms which are needed for all / some classes, object properties, data properties and datatypes
    @Override
    public ArrayList<LogicElement> visit(OWLDeclarationAxiom axiom) {
        ArrayList<LogicElement> res = new ArrayList<>();
        OWLDataFactory df = OWLManager.getOWLDataFactory();
        Variable x = new Variable("X");
        Variable y = new Variable("Y");

        // Classes can only be applied to things
        if (axiom.getEntity().isOWLClass()) {
            res.add(new QuantifiedFormula(
                new Quantifier(0), // universal quantifier
                new Variable[]{x},
                new BinaryFormula(
                    ((OWLClass) axiom.getEntity()).accept(new OWLClassExpressionTranslator(x)),
                    new BinaryConnective(3), // implication
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(x))
                )
            ));
        }

        // object properties can only be applied to things
        if (axiom.getEntity().isOWLObjectProperty()) {
            res.add(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    ((OWLObjectProperty) axiom.getEntity()).accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(3),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(x))
                )
            ));
            res.add(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    ((OWLObjectProperty) axiom.getEntity()).accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(3),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(y))
                )
            ));
        }

        // data properties can only be applied to one thing and one literal
        if (axiom.getEntity().isOWLDataProperty()) {
            res.add(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    ((OWLDataProperty) axiom.getEntity()).accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(3),
                    df.getOWLThing().accept(new OWLClassExpressionTranslator(x))
                )
            ));
            res.add(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x, y},
                new BinaryFormula(
                    ((OWLDataProperty) axiom.getEntity()).accept(new OWLPropertyExpressionTranslator(x, y)),
                    new BinaryConnective(3),
                    df.getTopDatatype().accept(new OWLDataTranslator(y))
                )
            ));
        }

        // datatypes can only be applied to literals
        if (axiom.getEntity().isOWLDatatype()) {
            res.add(new QuantifiedFormula(
                new Quantifier(0),
                new Variable[]{x},
                new BinaryFormula(
                    ((OWLDatatype) axiom.getEntity()).accept(new OWLDataTranslator(x)),
                    new BinaryConnective(3),
                    df.getTopDatatype().accept(new OWLDataTranslator(x))
                )
            ));
        }

        return res;
    }
}
