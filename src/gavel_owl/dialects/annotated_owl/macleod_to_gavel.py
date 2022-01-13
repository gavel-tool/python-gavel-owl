from macleod.logical.connective import (Conjunction, Disjunction, Connective, Implication, Biconditional)
from macleod.logical.logical import Logical
from macleod.logical.negation import Negation
from macleod.logical.quantifier import (Universal, Existential, Quantifier)
from macleod.logical.symbol import (Function, Predicate)
import gavel.logic.logic as gavel


def to_gavel(structure):
    if isinstance(structure, Logical):
        if isinstance(structure, Conjunction):
            return gavel.BinaryFormula(to_gavel(structure.terms[0]),
                                       gavel.BinaryConnective.CONJUNCTION,
                                       to_gavel(structure.terms[1]))
        elif isinstance(structure, Disjunction):
            return gavel.BinaryFormula(to_gavel(structure.terms[0]),
                                       gavel.BinaryConnective.DISJUNCTION,
                                       to_gavel(structure.terms[1]))
        elif isinstance(structure, Implication):
            return gavel.BinaryFormula(to_gavel(structure.terms[0]),
                                       gavel.BinaryConnective.IMPLICATION,
                                       to_gavel(structure.terms[1]))
        elif isinstance(structure, Biconditional):
            return gavel.BinaryFormula(to_gavel(structure.terms[0]),
                                       gavel.BinaryConnective.BIIMPLICATION,
                                       to_gavel(structure.terms[1]))
        elif isinstance(structure, Negation):
            return gavel.UnaryFormula(gavel.UnaryConnective.NEGATION, to_gavel(structure.terms[0]))
        elif isinstance(structure, Universal):
            vars = []
            for v in structure.variables:
                vars.append(gavel.Variable(v))
            return gavel.QuantifiedFormula(gavel.Quantifier.UNIVERSAL, vars, to_gavel(structure.terms[0]))
        elif isinstance(structure, Existential):
            vars = []
            for v in structure.variables:
                vars.append(gavel.Variable(v))
            return gavel.QuantifiedFormula(gavel.Quantifier.EXISTENTIAL, vars, to_gavel(structure.terms[0]))
        elif isinstance(structure, Predicate):
            vars = []
            for var in structure.variables:
                vars.append(gavel.Variable(var))
            return gavel.PredicateExpression(structure.name, vars)
        elif isinstance(structure, Function):
            vars = []
            for var in structure.variables:
                vars.append(gavel.Variable(var))
            return gavel.PredicateExpression(structure.name, vars)
        else:
            print("other logical: ")
            print(structure)
            print(type(structure))
            return structure
    else:
        print("not logical: ")
        print(structure)
        print(type(structure))
        return structure



