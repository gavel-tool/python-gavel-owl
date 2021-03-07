"""
Module that contains the command line app.

Why does this file exist, and why not put this in __main__?

  You might be tempted to import things from __main__ later, but that will cause
  problems: the code will get executed twice:

  - When you run `python -m gavel` python will execute
    ``__main__.py`` as a script. That means there won't be any
    ``gavel.__main__`` in ``sys.modules``.
  - When you import __main__ it will get executed again (as a module) because
    there's no ``gavel.__main__`` in ``sys.modules``.

  Also see (1) from http://click.pocoo.org/5/setuptools/#setuptools-integration
"""
__all__ = ["owl"]

import os
import subprocess
import gavel.dialects.base.dialect as dialect
import gavel.logic.problem as problem
from gavel.dialects.tptp.parser import TPTPParser
import click
from py4j.java_gateway import JavaGateway
from gavel.prover.vampire.interface import VampireInterface


@click.group()
def owl():
    pass


@click.command()
@click.option("-p", default="0815", help="Port number")
def start_server(p):
    """Start a server listening to port `p`"""
    p = subprocess.Popen(['java', '-Xmx2048m', '-jar', 'fowl-15.jar'], stdout=subprocess.PIPE,
                          stderr=subprocess.STDOUT, universal_newlines=True)

    for line in p.stdout:
        print(line)
        if "Server started" in str(line):
            return 0


@click.command()
def stop_server():
    """Stop a running server"""
    gateway = JavaGateway()
    gateway.shutdown()
    print("stop_server done")

@click.command()
@click.argument("f") #file
@click.argument("c") #conjectures
@click.option("--steps", is_flag=True, default=False)
def owl_prove(f, c, steps):
    axiomParser = dialect.get_dialect("owl")._parser_cls()
    conjParser = dialect.get_dialect("tptp")._parser_cls()
    compiler = dialect.get_dialect("tptp")._compiler_cls()
    sentence_enum = []
    conjecture_enum = []
    with open(f, "r") as finp:
        owlProblem = axiomParser.parse(finp.read())
    with open(c, "r") as finp:
        tptpProblem = conjParser.parse(finp.read())
    sentence_enum = owlProblem.premises
    conjecture_enum = owlProblem.conjectures + tptpProblem.conjectures
    for x in sentence_enum:
        print(x)
    print("")
    print("Conjecture:")
    prover = VampireInterface()
    for x in conjecture_enum:
        print(x)
        owl_problem = problem.Problem(premises=sentence_enum, conjectures=[x])
        fol_proof = prover.prove(owl_problem)

        if steps:
            print("")
            print("Proof:")
            for steps in fol_proof.steps:
                print(steps)
        else:
            print(fol_proof.status._name + ": " + fol_proof.status._description)

owl.add_command(start_server)
owl.add_command(stop_server)
owl.add_command(owl_prove)
