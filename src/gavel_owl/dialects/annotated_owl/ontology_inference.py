
import time

from gavel.logic import problem, logic
from gavel.logic.logic import UnaryConnective
from gavel.logic.problem import AnnotatedFormula

from gavel_owl.dialects.annotated_owl.parser import OntologyHandler

# takes a premise ontology and a conjecture ontology, tries to prove satisfiability of premise ontology combined with
# negation of each conjecture axiom
# if one is satisfiable, ontology entailment failed, if none is satisfiable, premise ontology entails conjecture ontology
# return entailment without annotations (based on OWL reasoner) and entailment with annotations (based on FOL reasoner)
def prove_ontology_entailment(premise_ontology_path, conjecture_ontology_path, jp=25333, pp=25335, verbose=False, **kwargs):
    start = time.time()

    clif_properties = kwargs["clif-properties"] if "clif-properties" in kwargs else None
    tptp_properties = kwargs["tptp-properties"] if "tptp-properties" in kwargs else None
    premise_handler = OntologyHandler(premise_ontology_path, jp=jp, pp=pp, verbose=verbose,
                                      tptp_annotation_properties=tptp_properties,
                                      clif_annotation_properties=clif_properties, use_readable_names=False,
                                      save_dol=False)
    entailment_without_annot = premise_handler.check_owl_entails(conjecture_ontology_path)
    if verbose:
        print(f'Without FOL annotations: {premise_ontology_path} -> {conjecture_ontology_path}:'
              f' {entailment_without_annot}')

    if verbose:
        print('TRANSLATING PREMISE ONTOLOGY')
    premise_problem, _, _ = premise_handler.build_combined_theory()

    if verbose:
        print('TRANSLATING CONJECTURE ONTOLOGY')

    conjecture_handler = OntologyHandler(conjecture_ontology_path, jp=jp, pp=pp, verbose=verbose,
                                         tptp_annotation_properties=tptp_properties,
                                         clif_annotation_properties=clif_properties, use_readable_names=False,
                                         save_dol=False)
    conjecture_problem, _, _ = conjecture_handler.build_combined_theory()

    if verbose:
        print(f'Total time excluding reasoning: {time.time() - start}')

    import gavel.prover as prover

    vampire = prover.registry.get_prover("vampire")()
    if verbose:
        print('PROVING CONJECTURE AXIOMS')
    # use heuristic: start with last conjectures (which are more likely to be important than the background axioms at the front)
    index = len(conjecture_problem.premises)
    while index > 0:
        index -= 1
        conj = conjecture_problem.premises[index]
        conj = AnnotatedFormula(conj.logic, conj.name, problem.FormulaRole.AXIOM,
                                logic.UnaryFormula(UnaryConnective.NEGATION, conj.formula), conj.annotation)

        if verbose:
            print(f'Proving premises + {conj}')
        proof = vampire.prove(problem.Problem(premise_problem.premises + [conj], []))
        # for s in proof.steps:
        #    print("{name}: {formula}".format(name=s.name, formula=s.formula))
        if verbose:
            print(f'{proof.status._name}: {proof.status._description}')
        if proof.status._name == "Satisfiable":
            return [entailment_without_annot, False]

    if verbose:
        print(f'Total time: {time.time() - start}')

    return [entailment_without_annot, True]
