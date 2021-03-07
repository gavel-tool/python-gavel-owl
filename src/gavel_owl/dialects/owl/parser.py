from typing import Iterable
from gavel.dialects.base.parser import Parseable, Target
import gavel.logic.problem as problem
from py4j.java_gateway import JavaGateway
import subprocess
import gavel.logic.logic as logic
import gavel.dialects.base.parser as parser


class OWLParser(parser.StringBasedParser):

    @staticmethod
    def parseJavaToPython(node):
        if node.getVisitName() == "quantifier":
            return logic.Quantifier(node.getId())
        elif node.getVisitName() == "binary_connective":
            return logic.BinaryConnective(node.getId())
        elif node.getVisitName() == "defined_predicate":
            return logic.DefinedPredicate(node.getId())
        elif node.getVisitName() == "unary_connective":
            return logic.UnaryConnective(node.getId())
        elif node.getVisitName() == "unary_formula":
            return logic.UnaryFormula(OWLParser.parseJavaToPython(node.getConnective()),
                                      OWLParser.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "quantified_formula":
            variables = []
            for var in node.getVariables():
                variables.append(OWLParser.parseJavaToPython(var))
            return logic.QuantifiedFormula(
                OWLParser.parseJavaToPython(node.getQuantifier()), variables,
                OWLParser.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "binary_formula":
            return logic.BinaryFormula(
                OWLParser.parseJavaToPython(node.getLeft()), OWLParser.parseJavaToPython(node.getOp()),
                OWLParser.parseJavaToPython(node.getRight()))
        elif node.getVisitName() == "predicate_expression":
            # TODO: find out how to handle lists like arguments
            arguments = []
            for arg in node.getArguments():
                arguments.append(OWLParser.parseJavaToPython(arg))
            return logic.PredicateExpression(node.getPredicate(), arguments)
        elif node.getVisitName() == "typed_variable":
            return logic.TypedVariable(node.getName(), OWLParser.parseJavaToPython(node.getVType()))
        elif node.getVisitName() == "variable":
            return logic.Variable(node.getSymbol())
        elif node.getVisitName() == "constant":
            return logic.Constant(node.getSymbol())
        elif node.getVisitName() == "defined_constant":
            return logic.DefinedConstant(node.getId())
        elif node.getVisitName() == "subtype":
            return logic.Subtype(OWLParser.parseJavaToPython(node.getLeft),
                                 OWLParser.parseJavaToPython(node.getRight))
        elif node.getVisitName() == "type":
            return logic.Type(node.getName())

    def parse(self, IRI, z="", simple_mode=True, *args, **kwargs):
        gateway = JavaGateway()
        # create entry point
        app = gateway.entry_point

        sentence_enum = []
        i = 0
        for next_pair in app.translateOntology(IRI):
            next_annotation = next_pair.getSecond()
            py_root = OWLParser.parseJavaToPython(node=next_pair.getFirst())
            name = "axiom" + str(i)
            i = i + 1
            # if (z == "") or (z in str(py_root)):
            if True:
                sentence = problem.AnnotatedFormula(
                    logic="fof", name=name, role=problem.FormulaRole.AXIOM,
                    formula=py_root, annotation=next_annotation)
                sentence_enum.append(sentence)
        inferences_enum = []
        is_consistent = True
        i = 0

        if not simple_mode:
            print("Inferences:")
            for inf in app.getInferences(IRI):
                i += 1
                inferences_enum.append(problem.AnnotatedFormula(
                    logic="fof", name="inference" + str(i), role=problem.FormulaRole.CONJECTURE,
                    formula=OWLParser.parseJavaToPython(node=inf.getFirst()), annotation=inf.getSecond()
                ))

            is_consistent = app.isConsistent()

        finalProblem = problem.Problem(sentence_enum, inferences_enum)

        return finalProblem  # sentence_enum, inferences_enum, is_consistent
