# use methods from cli via a file instead of the command line
from gavel_owl.cli import start_server, stop_server
import gavel.cli

start_server()
gavel.cli.translate(["owl", "tptp", "Pizza.owl"])
stop_server()
