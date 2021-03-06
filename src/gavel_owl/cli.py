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

import click
from py4j.java_gateway import JavaGateway

app = None
gateway = None

@click.group()
def owl():
    pass

@click.command()
@click.option("-p", default="0815", help="Port number")
def start_server(p):
    """Start a server listening to port `p`"""
    server = subprocess.Popen(['java', '-Xmx2048m', '-jar', 'fowl-13.jar'], stdout=subprocess.PIPE,
                     stderr=subprocess.STDOUT, universal_newlines=True)

    gateway = JavaGateway()

    # create entry point
    app = gateway.entry_point

    print("start_server done")

@click.command()
def stop_server():
    """Stop a running server"""
    gateway.shutdown()
    print("stop_server done")

owl.add_command(start_server)
owl.add_command(stop_server)
