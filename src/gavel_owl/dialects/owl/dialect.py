from gavel.dialects.base.dialect import Dialect
from gavel_owl.dialects.owl.parser import OWLParser


class OWLDialect(Dialect):
    _parser_cls = OWLParser

    @classmethod
    def _identifier(cls):
        return "owl"
