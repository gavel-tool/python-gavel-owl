from typing import Iterable
import os

import gavel.logic.problem as problem
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
import gavel.logic.logic as logic
import gavel.dialects.base.parser as parser


class OWLParser(parser.StringBasedParser):

    def __init__(self):
        self.file_path = None
        self.app = None
        self.use_readable_names = None

    def parseJavaToPython(self, node):
        if node.getVisitName() == "quantified_formula":
            return logic.QuantifiedFormula(
                logic.Quantifier(node.getQuantifier().getId()),
                [logic.Variable(var.getSymbol()) for var in node.getVariables()],
                self.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "binary_formula":
            return logic.BinaryFormula(
                self.parseJavaToPython(node.getLeft()), logic.BinaryConnective(node.getOp().getId()),
                self.parseJavaToPython(node.getRight()))
        elif node.getVisitName() == "predicate_expression":
            return logic.PredicateExpression(self.resolve_name(node.getPredicate()),
                                             [logic.Variable(arg.getSymbol()) if arg.getVisitName() == "variable"
                                              else logic.Constant(self.resolve_name(arg.getSymbol())) for arg in node.getArguments()])
        elif node.getVisitName() == "variable":
            return logic.Variable(node.getSymbol())
        elif node.getVisitName() == "constant":
            return logic.Constant(self.resolve_name(node.getSymbol()))
        elif node.getVisitName() == "unary_formula":
            return logic.UnaryFormula(logic.UnaryConnective(node.getConnective().getId()),
                                      self.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "defined_predicate":
            return logic.DefinedPredicate(node.getId())
        elif node.getVisitName() == "typed_variable":
            return logic.TypedVariable(node.getName(), self.parseJavaToPython(node.getVType()))
        elif node.getVisitName() == "defined_constant":
            return logic.DefinedConstant(node.getSymbol())
        elif node.getVisitName() == "subtype":
            return logic.Subtype(self.parseJavaToPython(node.getLeft()),
                                 self.parseJavaToPython(node.getRight()))
        elif node.getVisitName() == "type":
            return logic.Type(node.getName())

    def resolve_name(self, name):
        readable_name = name
        if self.use_readable_names:
            readable_name = self.app.getReadableName(self.file_path, name)
            if readable_name is None:
                readable_name = name

        return readable_name

    def parse_from_file(self, file_path, *args, **kwargs):
        jp = int(kwargs["jp"][0]) if "jp" in kwargs else 25333
        pp = int(kwargs["pp"][0]) if "pp" in kwargs else 25334

        self.use_readable_names = True if "readable-names" in kwargs else False

        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                              callback_server_parameters=CallbackServerParameters(port=int(pp)))
        # create entry point
        self.app = gateway.entry_point

        sentence_enum = []
        i = 0
        if not os.path.isabs(file_path):
            file_path = os.path.abspath(file_path)

        self.file_path = file_path

        for next_pair in self.app.translateOntologyFromFile(file_path):
            next_annotation = next_pair.getSecond()
            py_root = self.parseJavaToPython(node=next_pair.getFirst())
            name = "axiom" + str(i)
            i = i + 1
            sentence = problem.AnnotatedFormula(
                logic="fof", name=name, role=problem.FormulaRole.AXIOM,
                formula=py_root, annotation=next_annotation)
            sentence_enum.append(sentence)

        gateway.shutdown_callback_server()

        return problem.Problem(sentence_enum, [])

    def parse(self, ontology, z="", simple_mode=True, *args, **kwargs):
        jp = int(kwargs["jp"]) if "jp" in kwargs else 25333
        pp = int(kwargs["pp"]) if "pp" in kwargs else 25334

        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                              callback_server_parameters=CallbackServerParameters(port=int(pp)))
        # create entry point
        app = gateway.entry_point

        sentence_enum = []
        i = 0
        for next_pair in app.translateOntology(ontology):
            next_annotation = next_pair.getSecond()
            py_root = self.parseJavaToPython(node=next_pair.getFirst())
            name = "axiom" + str(i)
            i = i + 1
            sentence = problem.AnnotatedFormula(
                logic="fof", name=name, role=problem.FormulaRole.AXIOM,
                formula=py_root, annotation=next_annotation)
            sentence_enum.append(sentence)
        inferences_enum = []
        i = 0

        if not simple_mode:
            print("Inferences:")
            for inf in app.getInferences(ontology):
                i += 1
                inferences_enum.append(problem.AnnotatedFormula(
                    logic="fof", name="inference" + str(i), role=problem.FormulaRole.CONJECTURE,
                    formula=self.parseJavaToPython(node=inf.getFirst()), annotation=inf.getSecond()
                ))

        finalProblem = problem.Problem(sentence_enum, inferences_enum)

        gateway.shutdown_callback_server()
        return finalProblem  # sentence_enum, inferences_enum, is_consistent
