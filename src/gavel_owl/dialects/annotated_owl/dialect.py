from gavel.dialects.base.dialect import Dialect

from gavel_owl.dialects.annotated_owl.parser import AnnotatedOWLParser


class AnnotatedOWLDialect(Dialect):
    _parser_cls = AnnotatedOWLParser

    @classmethod
    def _identifier(cls):
        return "annotated-owl"
