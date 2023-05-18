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

An extension for Gavel introducing OWL and FOWL (OWL with FOL annotations) translations as well as related functionality regarding OWL and FOWL ontologies.

Installation
============


The latest released version can be installed with::

    pip install gavel-owl


The latest development version can be installed with::

    pip install git+https://github.com/gavel-tool/python-gavel-owl.git@dev

For the development version, you need to install the jar-file manually. This is done by running ``mvn install`` in the ``java`` directory. Copy the resulting jar-file from ``java/target/java-1.0-SNAPSHOT.one-jar.jar`` to ``src/gavel_owl/jars/api.jar`` (be sure to use the one-jar-file, not the regular jar-file).

Usage
=====

This plugin extends gavel by two new dialects, `owl` and `fowl`. This enables the various
features of gavel to be used with owl / fowl ontologies. These functionalities use the
java-based OWL-API. Therefore, you have to start the java backend in order to
use most owl-based functionalities. The `gavel-owl` plugin provides a single
command to do that::

    python -m gavel start-server

After the server has been started successfully, you can translate an existing
owl ontology to first-order logic in tptp syntax using::

    python -m gavel translate owl tptp your-ontology.owl

You can also submit arguments to fist-order prover Vampire consisting of the translation of a given owl ontology as premises and conjectures formulated in tptp using::

    python -m gavel owl-prove your-premises.owl your-conjectures.tptp

In order to translate an owl ontology with first-order annotations into tptp syntax, you can use::

    python -m gavel translate fowl tptp your-ontology.owl

The running java backend can be terminated::

    python -m gavel stop-server

There are several commands available that can be accessed via::

    python -m gavel COMMAND [ARGUMENTS]

- **start-server**: starts a subprocess that connects the Python program to its Java components. Other functions such as translate will only run if this connection has been established beforehand. The optional arguments -jp and -pp can be used for custom ports. Otherwise, the default ports will be used. -jp and -pp can be used for all other commands (except prove) analogously.

- **translate**: A Gavel function that translates the contents of a given file from one language, e.g. OWL, to another language, e.g. TPTP. If the option --save is used, the translation is stored in the given file, else it is gets displayed in the command line. The following options are avaiable for translate:

    - ``--clif-properties`` (only for input dialect ``fowl``) This option accepts arbitrary many IRIs or labels of OWL annotation properties. The values of annotation axioms using these properties will be interpreted as CLIF axioms. If this option is set with no arguments, the tool will not look for any CLIF axioms. If this option is not set, it will default to ``https://github.com/gavel-tool/python-gavel-owl/clif_annotation`` as an annotation property.
    - ``--tptp-properties`` (only for input dialect ``fowl``) This is analogous to **clif-properties**. Here, the default value is ``https://github.com/gavel-tool/python-gavel-owl/tptp_annotation``.

    - ``--shorten-names -n`` If this flag is set, the short form of IRIs will be used.

    - ``--readable-names`` (only for input dialects ``fowl`` and ``owl``) This flag replaces IRIs with labels (if available) or shortened IRIs.

    - ``--no-annotations -a`` This flag can be set to avoid rendering annotations in the output dialect. For the translation from ``fowl`` to ``tptp``, these annotations contain the OWL axiom or FOL annotation the is the origin of the corresponding FOL axioms.

    - ``--jp`` (only for input dialects ``fowl`` and ``owl``) Sets the Java port for the connection to FOWL's Java server. This should be the same number used for **start-server**. The default value is ``25333``.

    - ``--pp`` (only for input dialects ``fowl`` and ``owl``) Sets the Python port for the connection to FOWL's Java server. This should be the same number used for **start-server**. The default value is ``25334``.

    - ``--verbose -v`` (only for input dialect ``fowl``) If this flag is set, additional information on the translation process will be put in the command line, such as the mapping between OWL entities and FOL names.

    - ``--save -s`` This option specifies the path for saving the translation result. If **save** is not set, the result will be put out to the command line.

    - ``--save-dol`` (only for input dialect ``fowl``) This argument can be used to set a path under which to store the DOL-file generated from the annotated ontology.

    - ``--tptp-input`` (only for input dialect ``fowl``) Use this argument in combination with one or multiple filenames to add TPTP files to the ontology. The axioms in these files are treated like TPTP annotations.
    
    - ``--clif-input`` (only for input dialect `` fowl``) Similar to ``--tptp-input``, but for CLIF files. CLIF annotations are ignored. Advanced features such as imports or modules are not supported.

- **check-consistency**: uses the OWL reasoner Hermit to determine whether a given ontology is consistent or not.

- **owl-prove**: takes two arguments, an OWL file and a TPTP file. It uses Vampire to prove the conjectures provided in the TPTP file based on the translation of the OWL file. If the --steps flag is set, it will return the proof steps, otherwise it will only return the reasoner's result.

- **stop-server**: Ends the Java connection established by start-server.

- **prove**: a function from Gavel that takes the name of a FOL prover and a TPTP file and returns the prover's result for the given problem.

- **prove-ontology-entailment**: Checks if an OWL ontology can be entailed from another. It returns the result based on OWL reasoning and based on FOL reasoning using the fowl translation.

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

