class Mapping:
    def __init__(self, name, inputs, outputs):
        self.name = name
        self.inputs = inputs
        self.outputs = outputs

    def check(self):
        trigger = 1
        for current in self.inputs:
            val = current.get_value()
            if val == 0:
                trigger = 0
                break

        if trigger:
            print("Found all inputs for mapping '" + self.name + "': " + str(self.inputs))
            for current in self.inputs:
                current.set_value(0)

    def control(self, msg):
        for current in self.inputs:
            for splitMsg in msg.split("|"):
                if splitMsg.strip() == current.get_name():
                    current.set_value(1)
                    print("Applying remote control for mapping '" + self.name + "' on: " + msg + ": " + str(current))
