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

import click


@click.group()
def owl():
    pass

@click.command()
@click.option("-p", default="0815", help="Port number")
def start_server(p):
    """Start a server listening to port `p`"""
    raise NotImplementedError


@click.command()
def stop_server():
    """Stop a running server"""
    raise NotImplementedError
