# import RPi.GPIO as GPIO
# import TM1638
from Output import Output


class TextOutput(Output):
    def __init__(self, name, tm1638, value, delay):
        super().__init__(name, delay)
        self.tm1638 = tm1638
        self.value = value

    def clear_internal(self):
        pass


class LEDOutput(Output):
    def __init__(self, name, pin, delay):
        super().__init__(name, delay)
        self.pin = pin
        self.value = 0

    def clear_internal(self):
        pass

    def set_value(self, value):
        self.value = value


class SoundOutput(Output):
    def __init__(self, name, file, delay):
        super().__init__(name, delay)
        self.file = file

    def clear_internal(self):
        pass

    def play(self, value):
        pass
