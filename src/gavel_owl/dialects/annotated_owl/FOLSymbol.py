
# this is needed as an intermediate step between the Macleod CLIF parser (which does not distinguish between variables
# and constants) and Gavel (which does)
# a FOLSymbol can be either a constant or a variable
class FOLSymbol:

    def __init__(self, symbol):
        self.symbol = symbol
