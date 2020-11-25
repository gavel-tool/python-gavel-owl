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

You can also install the in-development version with::

    pip install https://github.com/gavel-tool/python-gavel-owl/archive/master.zip


Documentation
=============


https://python-gavel-owl.readthedocs.io/


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
