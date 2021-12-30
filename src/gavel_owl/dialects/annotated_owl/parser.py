import os.path

import gavel.dialects.base.parser as base_parser
import time
import tempfile

from gavel.dialects.base.parser import Parseable
from gavel.dialects.tptp.parser import *
from gavel.logic.logic import *
from gavel_owl.dialects.owl.parser import *
from lark import Token
from py4j.java_collections import ListConverter
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

from src.gavel_owl.dialects.annotated_owl.macleod_clif_parser import parse_string


# applies the name mapping from dict to each formula
def apply_mapping(formula_list, name_dict):
    res = []
    for formula in formula_list:
        res.append(apply_mapping_to_element(formula, name_dict))
    return res


# applies name mapping to a gavel-LogicElement
def apply_mapping_to_element(element, name_dict):
    if element.__visit_name__ == "quantified_formula":
        return QuantifiedFormula(
            element.quantifier,
            element.variables,
            apply_mapping_to_element(element.formula, name_dict))
    elif element.__visit_name__ == "binary_formula":
        return BinaryFormula(
            apply_mapping_to_element(element.left, name_dict), element.operator,
            apply_mapping_to_element(element.right, name_dict))
    elif element.__visit_name__ == "predicate_expression":
        return PredicateExpression(name_dict[remove_apostrophes(element.predicate)],
                                   [apply_mapping_to_element(arg, name_dict) for arg in element.arguments])
    elif element.__visit_name__ == "functor_expression":
        return FunctorExpression(name_dict[remove_apostrophes(element.functor)],
                                 [apply_mapping_to_element(arg, name_dict) for arg in element.arguments])
    elif element.__visit_name__ == "constant":
        return Constant(name_dict[remove_apostrophes(element.symbol)])
    elif element.__visit_name__ == "unary_formula":
        return UnaryFormula(element.connective, apply_mapping_to_element(element.formula, name_dict))
    else:
        return element


# 'proper part of' -> proper part of
def remove_apostrophes(string):
    if string.startswith("'") and string.endswith("'"):
        return string[1:-1]
    return string


# input: TPTP axiom fragments
# surrounds TPTP formulas with a complete axiom, then transforms it into the internal Gavel representation
def convert_tptp_fragments_to_internal_gavel(formulae):
    tptp_parser = TPTPParser()
    parser_input = ""
    for f in formulae:
        parser_input += f'fof(temp_axiom, axiom, {f}).\n'

    parser_output = tptp_parser.parse(parser_input)
    return map(lambda x: x.formula, parser_output)


# input: complete TPTP axioms
# transforms TPTP axioms into the internal Gavel representation
def convert_clif_to_internal_gavel(formulas):
    return map(lambda x: parse_string(x)[0], formulas)


# takes a list of parsed FOL formulas, returns all symbols
def get_symbols(formula_list):
    symbols = []
    for formula in formula_list:
        for s in formula.symbols():
            if type(s) == Token:
                symbol = remove_apostrophes(s.value)
            else:
                symbol = remove_apostrophes(s)  # or s.value (?, maybe for TPTP formulas)
            if not symbols.__contains__(symbol):
                symbols.append(symbol)
    return symbols


class AnnotatedOWLParser(base_parser.StringBasedParser):

    def parse(self, ontology: Parseable, *args, **kwargs) -> Iterable[Target]:
        ontology_file, name = tempfile.mkstemp()
        os.write(ontology_file, ontology)
        os.close(ontology_file)

        return self.parse_from_file(name, *args, **kwargs)

    def parse_from_file(self, ontology_path, *args, **kwargs):
        jp = int(kwargs["jp"]) if "jp" in kwargs else 25333
        pp = int(kwargs["pp"]) if "pp" in kwargs else 25334
        verbose = bool(kwargs["verbose"]) if "verbose" in kwargs else False
        shorten_iris = bool(kwargs["shorten-iris"]) if "shorten-iris" in kwargs else False
        clif_properties = list(kwargs["clif-properties"]) if "clif-properties" in kwargs else None
        tptp_properties = list(kwargs["tptp-properties"]) if "tptp-properties" in kwargs else None

        verbose = True  # TODO: remove
        shorten_iris = True
        ontology_handler = OntologyHandler(ontology_path, jp, pp, verbose, shorten_iris, tptp_properties,
                                           clif_properties)

        return ontology_handler.build_combined_theory()


def build_annotated_formulas(formulas, original_annotations):
    res = []
    i = 0
    for formula in formulas:
        res.append(problem.AnnotatedFormula(
            logic="fof",
            name="annotation_axiom" + str(i),
            role=problem.FormulaRole.AXIOM,
            formula=formula,
            annotation=f'{original_annotations[i]}'
        ))
        i += 1

    return res


class OntologyHandler:

    def __init__(self, ontology, jp=25333, pp=25334, verbose=True, shorten_iris=False, tptp_annotation_properties=None,
                 clif_annotation_properties=None):

        if not os.path.isabs(ontology):
            ontology = os.path.abspath(ontology)
        self.ontology_path = ontology
        self.jp = jp
        self.pp = pp
        self.verbose = verbose
        self.shorten_iris = shorten_iris
        if tptp_annotation_properties is None:
            tptp_annotation_properties = ["http://example.org/tptp_annotation",
                                          "http://openenergy-platform.org/ontology/oeo/OEO_00140158"]
        self.tptp_annotation_properties = tptp_annotation_properties
        if clif_annotation_properties is None:
            clif_annotation_properties = ["http://example.org/clif_annotation",
                                          "http://openenergy-platform.org/ontology/oeo/OEO_00140157"]
        self.clif_annotation_properties = clif_annotation_properties

        self.gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(self.jp)),
                                   callback_server_parameters=CallbackServerParameters(port=int(self.pp)))
        # create entry point
        self.app = self.gateway.entry_point

    # resolves the given annotation properties to iris in the ontology
    def get_annotation_properties(self):
        tptp_properties = []
        for name in self.tptp_annotation_properties:
            tptp_properties.append(self.app.getIRIMatch(self.ontology_path, name))
        self.tptp_annotation_properties = tptp_properties

        clif_properties = []
        for name in self.clif_annotation_properties:
            clif_properties.append(self.app.getIRIMatch(self.ontology_path, name))
        self.clif_annotation_properties = clif_properties

    # returns a list of all annotations in the ontology with the specified annotation properties
    def get_annotations(self):
        tptp_annotations = []
        java_list = ListConverter().convert(self.tptp_annotation_properties, self.gateway._gateway_client)
        for annot in self.app.getAnnotations(self.ontology_path, java_list):
            tptp_annotations.append(annot)

        clif_annotations = []
        java_list = ListConverter().convert(self.clif_annotation_properties, self.gateway._gateway_client)
        for annot in self.app.getAnnotations(self.ontology_path, java_list):
            clif_annotations.append(annot)

        self.gateway.shutdown_callback_server()

        return tptp_annotations, clif_annotations

    # returns a list of iri-matches for all used symbols
    def build_name_mapping(self, symbol_list):
        symbol_iri_dict = {}
        for s in symbol_list:
            iri = self.app.getIRIMatch(self.ontology_path, s)
            if iri is None:
                continue
            symbol_iri_dict[s] = iri
            # for line in self.java_subprocess.stdout:
            #    print(line)

        self.gateway.shutdown_callback_server()

        return symbol_iri_dict

    # returns a Gavel problem consisting of the translation of the OWL ontology and the FOL annotations
    def build_combined_theory(self):
        start = time.time()

        self.get_annotation_properties()
        if self.verbose:
            print(f'Annotation properties used:\n\t TPTP: {self.tptp_annotation_properties}\n\t '
                  f'CLIF: {self.clif_annotation_properties}')

            print(f'Annotation properties time: {time.time() - start}')
            start = time.time()

        tptp_annot, clif_annot = self.get_annotations()
        if self.verbose:
            print(f'Annotations:\n\t TPTP: {tptp_annot}\n\t CLIF: {clif_annot}')

            print(f'Annotations time: {time.time() - start}')
            start = time.time()

        parsed_formulas = list(convert_clif_to_internal_gavel(clif_annot)) + list(
            convert_tptp_fragments_to_internal_gavel(tptp_annot))

        if self.verbose:
            print(f'Parse to gavel time: {time.time() - start}')
            start = time.time()

        symbols = get_symbols(parsed_formulas)
        if self.verbose:
            print(f'Symbols: {symbols}')

            print(f'Symbols time: {time.time() - start}')
            start = time.time()

        symbol_iri_dict = self.build_name_mapping(symbols)
        if self.verbose:
            print(f'Dict: {symbol_iri_dict}')
            print(f'Dict time: {time.time() - start}')
            start = time.time()

        formulas_using_iris = apply_mapping(parsed_formulas, symbol_iri_dict)
        annot_tptp_lines = build_annotated_formulas(formulas_using_iris, clif_annot + tptp_annot)
        if self.verbose:
            print(f'TPTP Formulas with IRIs:')
            for f in annot_tptp_lines:
                print(f)

            print(f'TPTP time: {time.time() - start}')
            start = time.time()

        self.gateway.shutdown_callback_server()

        from gavel_owl.dialects.owl.parser import OWLParser
        owl_translation = OWLParser().parse_from_file(self.ontology_path)
        if self.verbose:
            counter = len(owl_translation.premises)
            if counter < 20:
                translation_cutoff = 0
            else:
                translation_cutoff = counter - 20
            print(f'Translation of OWL Ontology (last 20 lines):')
            while counter > translation_cutoff:
                print(owl_translation.premises[counter - 1])
                counter -= 1

            print(f'Translation time: {time.time() - start}')

        return problem.Problem(annot_tptp_lines + owl_translation.premises, [])
