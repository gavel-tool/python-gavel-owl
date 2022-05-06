import os.path

import gavel.dialects.base.parser as base_parser
import time

from gavel.dialects.base.parser import Parseable
from gavel.dialects.tptp.parser import *
from gavel.logic.logic import *
from gavel_owl.dialects.owl.parser import *
from lark import Token
from py4j.java_collections import ListConverter
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

from gavel_owl.dialects.annotated_owl.FOLSymbol import FOLSymbol
from gavel_owl.dialects.annotated_owl.macleod_clif_parser import parse_string


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
    parsed_formulas = list(map(lambda x: parse_string(x)[0], formulas))
    # distinguish between variables and constants:
    return map(lambda x: find_variables(x, []), parsed_formulas)


# function that replaces FOLSymbol with Gavel-Variable (if the symbol is quantified) or -Constant (else)
def find_variables(element, variables):
    if isinstance(element, FOLSymbol):
        if element.symbol in variables:
            return Variable(element.symbol)
        else:
            return Constant(element.symbol)

    if element.__visit_name__ == "quantified_formula":
        return QuantifiedFormula(
            element.quantifier,
            element.variables,
            find_variables(element.formula, list(map(lambda var: var.symbol, element.variables)) + variables))
    elif element.__visit_name__ == "binary_formula":
        return BinaryFormula(
            find_variables(element.left, variables), element.operator,
            find_variables(element.right, variables))
    elif element.__visit_name__ == "predicate_expression":
        return PredicateExpression(element.predicate,
                                   [find_variables(arg, variables) for arg in element.arguments])
    elif element.__visit_name__ == "functor_expression":
        return FunctorExpression(element.functor,
                                 [find_variables(arg, variables) for arg in element.arguments])
    elif element.__visit_name__ == "unary_formula":
        return UnaryFormula(element.connective, find_variables(element.formula, variables))
    else:
        return element


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

    def __init__(self):
        # only needed for creating a DOL file
        self.ontology_text_dol = None
        self.name_mapping = None

    def parse(self, ontology: Parseable, *args, **kwargs) -> Iterable[Target]:
        raise NotImplementedError

    def parse_from_file(self, ontology_path, *args, **kwargs):
        jp = int(kwargs["jp"][0]) if "jp" in kwargs else 25333
        pp = int(kwargs["pp"][0]) if "pp" in kwargs else 25334
        verbose = True if "verbose" in kwargs else False
        use_readable_names = True if "readable-names" in kwargs else False
        save_dol = True if "save-dol" in kwargs else False
        clif_properties = kwargs["clif-properties"] if "clif-properties" in kwargs else None
        tptp_properties = kwargs["tptp-properties"] if "tptp-properties" in kwargs else None
        ontology_handler = OntologyHandler(ontology_path, jp, pp, verbose, tptp_properties,
                                           clif_properties, use_readable_names, save_dol)

        gavel_problem, self.ontology_text_dol, self.name_mapping = ontology_handler.build_combined_theory()

        return gavel_problem


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

    def __init__(self, ontology, jp=25333, pp=25334, verbose=True, tptp_annotation_properties=None,
                 clif_annotation_properties=None, use_readable_names=False, save_dol=False):

        self.translation_mapping = None
        self.save_dol = save_dol
        if not os.path.isabs(ontology):
            ontology = os.path.abspath(ontology)
        self.ontology_path = ontology
        self.jp = jp
        self.pp = pp
        self.verbose = verbose
        if tptp_annotation_properties is None:
            tptp_annotation_properties = ["https://github.com/gavel-tool/python-gavel-owl/tptp_annotation"]
        self.tptp_annotation_properties = tptp_annotation_properties
        if clif_annotation_properties is None:
            clif_annotation_properties = ["https://github.com/gavel-tool/python-gavel-owl/clif_annotation"]
        self.clif_annotation_properties = clif_annotation_properties
        self.use_readable_names = use_readable_names

        self.gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(self.jp)),
                                   callback_server_parameters=CallbackServerParameters(port=int(self.pp)))
        # create entry point
        self.app = self.gateway.entry_point

    # resolves the given annotation properties to iris in the ontology
    def get_annotation_properties(self):
        tptp_properties = []
        for name in self.tptp_annotation_properties:
            match = self.app.getIRIMatch(self.ontology_path, name)
            tptp_properties.append(match)
        self.tptp_annotation_properties = tptp_properties

        clif_properties = []
        for name in self.clif_annotation_properties:
            match = self.app.getIRIMatch(self.ontology_path, name)
            clif_properties.append(match)
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

    # returns a list of name-matches (iris or readable names) for all used symbols
    def build_name_mapping(self, symbol_list):
        symbol_name_dict = {}
        for s in symbol_list:
            iri = self.app.getIRIMatch(self.ontology_path, s)
            if iri is None:
                continue
            if self.use_readable_names:
                readable_name = self.app.getReadableName(self.ontology_path, iri)
                if readable_name != "":
                    iri = readable_name

            symbol_name_dict[s] = iri
            # for line in self.java_subprocess.stdout:
            #    print(line)

        self.gateway.shutdown_callback_server()

        return symbol_name_dict

    def check_owl_entails(self, conjecture_path):
        if not os.path.isabs(conjecture_path):
            conjecture_path = os.path.abspath(conjecture_path)
        return self.app.owlOntologyEntails(self.ontology_path, conjecture_path)

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

        symbol_name_dict = self.build_name_mapping(symbols)
        if self.verbose:
            print(f'Dict: {symbol_name_dict}')
            print(f'Dict time: {time.time() - start}')
            start = time.time()

        formulas_using_iris = apply_mapping(parsed_formulas, symbol_name_dict)
        annot_tptp_lines = build_annotated_formulas(formulas_using_iris, clif_annot + tptp_annot)
        if self.verbose:
            print(f'Formulas with resolved names:')
            for f in annot_tptp_lines:
                print(f)

            print(f'TPTP time: {time.time() - start}')
            start = time.time()

        self.gateway.shutdown_callback_server()

        from gavel_owl.dialects.owl.parser import OWLParser

        kwargs = {}
        if self.use_readable_names:
            kwargs['readable-names'] = []

        owl_translation_parser = OWLParser()
        owl_translation = owl_translation_parser.parse_from_file(self.ontology_path, **kwargs)

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

        if self.save_dol:
            ontology_text_dol = self.app.getDOLconformantOntology(self.ontology_path)
            iriToReadableMapping = self.app.getReadableNameMapping(self.ontology_path)
            iriToCurieMapping = self.app.getIRIToCurieMapping(self.ontology_path)

            gavel_name_to_owl_name_mapping = {}
            if self.use_readable_names:
                for key in iriToReadableMapping:
                    if key in iriToCurieMapping:
                        gavel_name_to_owl_name_mapping[iriToReadableMapping[key]] = iriToCurieMapping[key]
                    else:
                        # IRIs need to be put in < >
                        gavel_name_to_owl_name_mapping[iriToReadableMapping[key]] = '<' + key + '>'
            else:
                for key in iriToCurieMapping:
                    gavel_name_to_owl_name_mapping[key] = iriToCurieMapping[key]

            return problem.Problem(annot_tptp_lines + owl_translation.premises, []), \
                   ontology_text_dol, gavel_name_to_owl_name_mapping

        return problem.Problem(annot_tptp_lines + owl_translation.premises, []), None, None
