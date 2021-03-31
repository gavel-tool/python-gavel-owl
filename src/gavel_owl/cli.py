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

import subprocess
import gavel.dialects.base.dialect as dialect
import gavel.logic.problem as problem
import gavel.prover as prover
import click
from py4j.java_gateway import JavaGateway, GatewayParameters, CallbackServerParameters

@click.group()
def owl():
    pass


@click.command()
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def start_server(jp, pp):
    """Start a server listening to ports `jp` and `pp`"""
    p = subprocess.Popen(['java', '-Xmx2048m', '-jar', 'fowl-17.jar', jp, pp], stdout=subprocess.PIPE,
                         stderr=subprocess.STDOUT, universal_newlines=True)

    for line in p.stdout:
        if "Server started" in str(line):
            print(line)
            return 0


@click.command()
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def stop_server(pp, jp):
    """Stop a running server"""
    gateway = JavaGateway(gateway_parameters= GatewayParameters(port=int(jp)),
                          callback_server_parameters=CallbackServerParameters(port=int(pp)))
    gateway.shutdown()
    print("Server stopped")


@click.command()
@click.argument("file")  # OWL ontology file
@click.argument("conjectures")  # TPTP conjecture file
@click.option("--steps", is_flag=True, default=False)
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def owl_prove(file, conjectures, steps, pp, jp):
    """prove TPTP conjectures using OWL premises"""
    #load and translate files
    owlParser = dialect.get_dialect("owl")()
    tptpParser = dialect.get_dialect("tptp")()
    with open(file, "r") as finp:
        owlProblem = owlParser.parse(finp.read(), jp=jp, pp=pp)
    with open(conjectures, "r") as finp:
        tptpProblem = tptpParser.parse(finp.read())
    sentence_enum = owlProblem.premises
    conjecture_enum = owlProblem.conjectures + tptpProblem.conjectures
    for x in sentence_enum:
        print(x)
    print("")
    #prove using the vampire prover
    print("Conjectures:")
    VampProver = prover.registry.get_prover("vampire")()
    for x in conjecture_enum:
        print(x)
        owl_problem = problem.Problem(premises=sentence_enum, conjectures=[x])
        fol_proof = VampProver.prove(problem=owl_problem)
        print(fol_proof.status._name + ": " + fol_proof.status._description)
        if steps:
            print("")
            print("Proof:")
            for step in list(fol_proof.steps):
                print(step)

@click.command()
@click.argument("ontology") # ontology
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def check_consistency(ontology, jp, pp):
    """Check if an ontology is consistent"""

    with open(ontology, "r") as finp:
        ontology = finp.read()
    gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                          callback_server_parameters=CallbackServerParameters(port=int(pp)))
    # create entry point
    app = gateway.entry_point

    if app.isConsistent(ontology):
        print("Ontology is consistent")
    else:
        print("Ontology is inconsistent")

@click.command(name='translatep', context_settings=dict(
    ignore_unknown_options=True,
    allow_extra_args=True,
))
@click.argument("frm")
@click.argument("to")
@click.argument("path")
@click.pass_context
def translateP(ctx, frm, to, path):
    data = {ctx.args[i].strip('-'): ctx.args[i+1] for i in range(0, len(ctx.args), 2)}
    input_dialect = dialect.get_dialect(frm)
    output_dialect = dialect.get_dialect(to)

    parser = input_dialect._parser_cls()
    compiler = output_dialect._compiler_cls()
    with open(path, "r") as finp:
        print(compiler.visit(parser.parse(finp.read(), **data)))



owl.add_command(start_server)
owl.add_command(stop_server)
owl.add_command(owl_prove)
owl.add_command(check_consistency)
owl.add_command(translateP)
