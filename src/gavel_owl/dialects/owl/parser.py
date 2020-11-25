from typing import Iterable

from gavel.dialects.base import parser
from gavel.dialects.base.parser import Parseable, Target


class OWLParser(parser.StringBasedParser):

    def parse(self, structure: Parseable, *args, **kwargs) -> Iterable[Target]:
        raise NotImplementedError
