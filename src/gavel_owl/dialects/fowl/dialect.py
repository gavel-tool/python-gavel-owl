from gavel.dialects.base.dialect import Dialect

from gavel_owl.dialects.fowl.parser import AnnotatedOWLParser


class AnnotatedOWLDialect(Dialect):
    _parser_cls = AnnotatedOWLParser

    @classmethod
    def _identifier(cls):
        return "fowl"
