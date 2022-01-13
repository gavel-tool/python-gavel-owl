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

from src.gavel_owl.dialects.annotated_owl.macleod_clif_parser import parse_string
from src.gavel_owl.dialects.annotated_owl.parser import AnnotatedOWLParser


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


#@click.command(name='translate2', context_settings=dict(
#    ignore_unknown_options=True,
#    allow_extra_args=True,
#))
#@click.argument("path")
#@click.option("--save", metavar="SAVE_PATH", default="", help="If set, saves the translation to SAVE_PATH")
#@click.pass_context
def translate_annotated_owl_tptp(path, save):
    """
    Translates the file at PATH from the dialect specified by FRM to the dialect TO. You can get a list of all available dialects via the `dialects` command.
    Example usage:
        python -m gavel translate --save=my-output.p tptp tptp my-input-file.tptp
        This will parse a given TPTP-file into gavels internal logic and save a new equivalent TPTP theory in my-output.p.
    """
    output_dialect = dialect.get_dialect('tptp')

    parser = AnnotatedOWLParser()
    compiler = output_dialect._compiler_cls(shorten_names=True)
    # if the parameter save is specified, the translation gets saved as a file with that name
    if save != "":
        with open(str(save), 'w') as file:
            file.write(compiler.visit(parser.parse_from_file(path)))
    else:
        print(compiler.visit(parser.parse_from_file(path)))


if __name__ == '__main__':
    print(parse_string('(forall (a b) (if (precedes a b) (not (precedes b a))))')[0])
    translate_annotated_owl_tptp('../../ba_architecture_example.omn', 'ba_architecture_example_tptp.p')

owl.add_command(start_server)
owl.add_command(stop_server)
owl.add_command(owl_prove)
owl.add_command(check_consistency)
#owl.add_command(translate_annotated_owl_tptp)
