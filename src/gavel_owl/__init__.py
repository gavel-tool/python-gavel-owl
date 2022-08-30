__version__ = '0.0.0'

import os

from gavel_owl.dialects.owl.dialect import OWLDialect
from gavel_owl.dialects.fowl.dialect import AnnotatedOWLDialect

package_directory = os.path.dirname(os.path.abspath(__file__))
