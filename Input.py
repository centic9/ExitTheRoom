class Input:
    def __init__(self, name):
        self.name = name

    def get_name(self):
        return self.name

    def get_value(self):
        raise Exception("Should overwrite value() in Input")
