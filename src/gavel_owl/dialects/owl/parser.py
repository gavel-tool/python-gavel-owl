from typing import Iterable

import gavel.logic.problem as problem
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters
import gavel.logic.logic as logic
import gavel.dialects.base.parser as parser


class OWLParser(parser.StringBasedParser):

    @staticmethod
    def parseJavaToPython(node):
        if node.getVisitName() == "quantified_formula":
            return logic.QuantifiedFormula(
                logic.Quantifier(node.getQuantifier().getId()),
                [logic.Variable(var.getSymbol()) for var in node.getVariables()],
                OWLParser.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "binary_formula":
            return logic.BinaryFormula(
                OWLParser.parseJavaToPython(node.getLeft()), logic.BinaryConnective(node.getOp().getId()),
                OWLParser.parseJavaToPython(node.getRight()))
        elif node.getVisitName() == "predicate_expression":
            return logic.PredicateExpression(node.getPredicate(),
                                             [logic.Variable(arg.getSymbol()) if arg.getVisitName() == "variable"
                                              else logic.Constant(arg.getSymbol()) for arg in node.getArguments()])
        elif node.getVisitName() == "variable":
            return logic.Variable(node.getSymbol())
        elif node.getVisitName() == "constant":
            return logic.Constant(node.getSymbol())
        elif node.getVisitName() == "unary_formula":
            return logic.UnaryFormula(logic.UnaryConnective(node.getConnective().getId()),
                                      OWLParser.parseJavaToPython(node.getFormula()))
        elif node.getVisitName() == "defined_predicate":
            return logic.DefinedPredicate(node.getId())
        elif node.getVisitName() == "typed_variable":
            return logic.TypedVariable(node.getName(), OWLParser.parseJavaToPython(node.getVType()))
        elif node.getVisitName() == "defined_constant":
            return logic.DefinedConstant(node.getSymbol())
        elif node.getVisitName() == "subtype":
            return logic.Subtype(OWLParser.parseJavaToPython(node.getLeft()),
                                 OWLParser.parseJavaToPython(node.getRight()))
        elif node.getVisitName() == "type":
            return logic.Type(node.getName())


    def parse_from_file(self, file_path, *args, **kwargs):
        jp = int(kwargs["jp"]) if "jp" in kwargs else 25333
        pp = int(kwargs["pp"]) if "pp" in kwargs else 25334

        gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                              callback_server_parameters=CallbackServerParameters(port=int(pp)))
        # create entry point
        app = gateway.entry_point

        sentence_enum = []
        i = 0
        for next_pair in app.translateOntologyFromFile(file_path):
            next_annotation = next_pair.getSecond()
            py_root = OWLParser.parseJavaToPython(node=next_pair.getFirst())
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
            py_root = OWLParser.parseJavaToPython(node=next_pair.getFirst())
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
                    formula=OWLParser.parseJavaToPython(node=inf.getFirst()), annotation=inf.getSecond()
                ))

        finalProblem = problem.Problem(sentence_enum, inferences_enum)

        gateway.shutdown_callback_server()
        return finalProblem  # sentence_enum, inferences_enum, is_consistent
