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
import os
from gavel_owl import package_directory

from gavel_owl.dialects.annotated_owl.ontology_inference import \
    prove_ontology_entailment as annot_owl_prove_entailment


@click.group()
def owl():
    pass


@click.command()
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def start_server(jp, pp):
    """Start a server listening to ports `jp` and `pp`"""
    p = subprocess.Popen(['java', '-Xmx2048m', '-jar', os.path.join(package_directory, 'jars', 'api.jar'), jp, pp],
                         stdout=subprocess.PIPE, stderr=subprocess.STDOUT, universal_newlines=True)

    for line in p.stdout:
        if "JarClassLoader: Warning:" not in str(line):
            print(line.replace("\n", ""))
        if "Server started" in str(line):
            return 0
        if "Starting server failed" in str(line):
            return 0


@click.command()
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def stop_server(pp, jp):
    """Stop a running server"""
    gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                          callback_server_parameters=CallbackServerParameters(port=int(pp)))
    gateway.shutdown()
    print(f'Server stopped (jp: {jp}, pp: {pp})')


@click.command()
@click.argument("file")  # OWL ontology file
@click.argument("conjectures")  # TPTP conjecture file
@click.option("--steps", is_flag=True, default=False)
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def owl_prove(file, conjectures, steps, pp, jp):
    """prove TPTP conjectures using OWL premises"""
    # load and translate files
    owl_dialect = dialect.get_dialect("owl")
    tptp_dialect = dialect.get_dialect("tptp")
    owl_parser = owl_dialect._parser_cls()
    tptp_parser = tptp_dialect._parser_cls()
    tptp_compiler = tptp_dialect._compiler_cls()

    owl_problem = owl_parser.parse_from_file(file_path=file, jp=jp, pp=pp)

    with open(conjectures, "r") as finp:
        tptp_problem = tptp_parser.parse(finp.read())

    print(tptp_compiler.visit(owl_problem))
    print("")
    # prove using the vampire prover
    print("Conjectures:")
    vampire = prover.registry.get_prover("vampire")()
    for conj in tptp_problem.conjectures:
        print(tptp_compiler.visit(conj))
        combined_problem = problem.Problem(premises=owl_problem.premises, conjectures=[conj])
        fol_proof = vampire.prove(problem=combined_problem)
        print(fol_proof.status._name + ": " + fol_proof.status._description)
        if steps:
            print("")
            print("Proof:")
            for step in list(fol_proof.steps):
                print(step)


@click.command()
@click.argument("ontology")  # ontology
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
def check_consistency(ontology, jp, pp):
    """Check if an OWL ontology is consistent"""

    gateway = JavaGateway(gateway_parameters=GatewayParameters(port=int(jp)),
                          callback_server_parameters=CallbackServerParameters(port=int(pp)))
    # create entry point
    app = gateway.entry_point

    if app.isConsistent(ontology):
        print("Ontology is consistent")
    else:
        print("Ontology is inconsistent")

    gateway.shutdown_callback_server()


@click.command(name="prove-ontology-entailment", context_settings=dict(
    ignore_unknown_options=True,
    allow_extra_args=True,
))
@click.argument("premise_ontology_path")
@click.argument("conjecture_ontology_path")
@click.option("-jp", default="25333", help="Java Port number")
@click.option("-pp", default="25334", help="Python Port number")
@click.option("--verbose", "-v", is_flag=True, default=False)
@click.pass_context
def prove_ontology_entailment(ctx, premise_ontology_path, conjecture_ontology_path, jp, pp, verbose):
    """check if an OWL ontology (annotated with FOL axioms) follows from another (annotated) OWL ontology"""

    index = 0
    kwargs = {}
    while index < len(ctx.args):
        if ctx.args[index].startswith('-'):
            command = ctx.args[index].strip('-')
            values = []
            index += 1
            while index < len(ctx.args) and not ctx.args[index].startswith('-'):
                values.append(ctx.args[index])
                index += 1

            kwargs[command] = values
        else:
            index += 1

    owl_inference, tptp_inference = annot_owl_prove_entailment(premise_ontology_path, conjecture_ontology_path, jp, pp,
                                                               verbose, **kwargs)
    entails = "entails"
    doesnotentail = "does not entail"
    print(
        f'Based on OWL, {premise_ontology_path} {entails if owl_inference else doesnotentail} {conjecture_ontology_path}')
    print(f'Based on OWL with FOL annotations, {premise_ontology_path} {entails if tptp_inference else doesnotentail} '
          f'{conjecture_ontology_path}')


owl.add_command(start_server)
owl.add_command(stop_server)
owl.add_command(owl_prove)
owl.add_command(check_consistency)
owl.add_command(prove_ontology_entailment)
