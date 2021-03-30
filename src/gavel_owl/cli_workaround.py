# use methods from cli via a file instead of the command line
from gavel_owl.dialects.owl.parser import OWLParser
from gavel_owl.cli import start_server, stop_server
from gavel.dialects.base.dialect import get_dialect
import gavel.cli

#start_server()
gavel.cli.translate(["owl", "tptp", "Pizza.owl"])
#stop_server()

parser = OWLParser()
to = "tptp"
output_dialect = get_dialect(to)
compiler = output_dialect._compiler_cls()
print(compiler.visit(parser.parse(IRI="Pizza.owl")))
