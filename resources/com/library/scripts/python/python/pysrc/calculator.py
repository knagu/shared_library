number_types = (int, float, complex)


class calculator:

    @staticmethod
    def validate_args(x, y):
        if not isinstance(x, number_types) and not isinstance(y, number_types):
            raise ValueError

    def add(self, x, y):
        self.validate_args(x, y)
        return x + y

    def mul(self, x, y):
        self.validate_args(x, y)
        return x * y

    def sub(self, x, y):
        self.validate_args(x, y)
        return x - y

    def div(self, x, y):
        self.validate_args(x, y)
        return x / y