========
Overview
========

.. start-badges

.. list-table::
    :stub-columns: 1

    * - docs
      - |docs|
    * - tests
      - | |travis| |requires|
        |
    * - package
      - | |version| |wheel| |supported-versions| |supported-implementations|
        | |commits-since|
.. |docs| image:: https://readthedocs.org/projects/python-gavel-owl/badge/?style=flat
    :target: https://readthedocs.org/projects/python-gavel-owl
    :alt: Documentation Status

.. |travis| image:: https://api.travis-ci.org/gavel-tool/python-gavel-owl.svg?branch=master
    :alt: Travis-CI Build Status
    :target: https://travis-ci.org/gavel-tool/python-gavel-owl

.. |requires| image:: https://requires.io/github/gavel-tool/python-gavel-owl/requirements.svg?branch=master
    :alt: Requirements Status
    :target: https://requires.io/github/gavel-tool/python-gavel-owl/requirements/?branch=master

.. |version| image:: https://img.shields.io/pypi/v/gavel-owl.svg
    :alt: PyPI Package latest release
    :target: https://pypi.org/project/gavel-owl

.. |wheel| image:: https://img.shields.io/pypi/wheel/gavel-owl.svg
    :alt: PyPI Wheel
    :target: https://pypi.org/project/gavel-owl

.. |supported-versions| image:: https://img.shields.io/pypi/pyversions/gavel-owl.svg
    :alt: Supported versions
    :target: https://pypi.org/project/gavel-owl

.. |supported-implementations| image:: https://img.shields.io/pypi/implementation/gavel-owl.svg
    :alt: Supported implementations
    :target: https://pypi.org/project/gavel-owl

.. |commits-since| image:: https://img.shields.io/github/commits-since/gavel-tool/python-gavel-owl/v0.0.0.svg
    :alt: Commits since latest release
    :target: https://github.com/gavel-tool/python-gavel-owl/compare/v0.0.0...master



.. end-badges

An extension for gavel introducing OWL translations.

Installation
============

::

    pip install gavel-owl
 
 
The latest version is currently available as a pre-release and can be installed with::
 
    pip install gavel==0.1.3.dev0

    pip install gavel-owl==0.0.3.dev0


Usage
=====

This plugin extends gavel by two new dialects, `owl` and `annotated-owl`. This enables the various
features of gavel to be used with owl ontologies. These functionalities use the
java-based OWL-API. Therefore, you have to start the java backend in order to
use most owl-based functionalities. The `gavel-owl` plugin provides a single
command to do that::

    python -m gavel start-server

After the server has been started successfully, you can translate an existing
owl ontology to first-order logic in tptp syntax using::

    python -m gavel translate owl tptp your-ontology.owl

You can also submit arguments to fist-order prover Vampire consisting of the translation of a given owl ontology as premises and conjectures formulated in tptp using::

    python -m gavel owl-prove your-premises.owl your-conjectures.tptp

In order to translate an owl ontology with first-order annotation into tptp syntax, you can use

    python -m gavel translate annotated-owl tptp your-ontology.owl

The running java backend can be terminated::

    python -m gavel stop-server

There are several commands available that can be accessed via::

    python -m gavel COMMAND [ARGUMENTS]

- start-server: starts a subprocess that connects the Python program to its Java components. Other functions such as translate will only run if this connection has been established beforehand. The optional arguments -jp and -pp can be used for custom ports. Otherwise, the default ports will be used. -jp and -pp can be used for all other commands (except prove) analogously.

- translate: A Gavel function that translates the contents of a given file from one language, e.g. OWL, to another language, e.g. TPTP. If the option --save is used, the translation is stored in the given file, else it is gets displayed in the command line.

- check-consistency: uses the OWL reasoner Hermit to determine whether a given ontology is consistent or not.

- owl-prove: takes two arguments, an OWL file and a TPTP file. It uses Vampire to prove the conjectures provided in the TPTP file based on the translation of the OWL file. If the --steps flag is set, it will return the proof steps, otherwise it will only return the reasoner's result.

- stop-server: Ends the Java connection established by start-server.

- prove: a function from Gavel that takes the name of a FOL prover and a TPTP file and returns the prover's result for the given problem.

- prove-ontology-entailment: Checks if an OWL ontology can be entailed from another. It returns the result based on OWL reasoning and based on FOL reasoning using the annotated-owl translation.

For further options use::

    python -m gavel [COMMAND] --help

Development
===========

To run all the tests run::

    tox

Note, to combine the coverage data from all the tox environments run:

.. list-table::
    :widths: 10 90
    :stub-columns: 1

    - - Windows
      - ::

            set PYTEST_ADDOPTS=--cov-append
            tox

    - - Other
      - ::

            PYTEST_ADDOPTS=--cov-append tox
