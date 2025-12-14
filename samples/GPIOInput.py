#import RPi.GPIO as GPIO
# import TM1638
from Input import Input


class ButtonInput(Input):
    def __init__(self, name, tm1638, buttons):
        super().__init__(name)
        self.tm1638 = tm1638
        self.value = 0
        self.buttons = buttons

    def get_value(self):
        return self.value

    def set_value(self, value):
        self.value = value


class SwitchInput(Input):
    def __init__(self, name, pin):
        super().__init__(name)

        # Set pin 10 to be an input pin and set initial value to be pulled low (off)
        #GPIO.setup(pin, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

        self.pin = pin
        self.value = 0

    def get_value(self):
        #if GPIO.input(19) == GPIO.HIGH:
        #return 1
        #else:
        #return 0

        return self.value

    def set_value(self, value):
        self.value = value


class RotaryInput(Input):
    def __init__(self, name, pin, valueFrom, valueTo):
        super().__init__(name)
        self.pin = pin
        self.value = 0
        self.valueFrom = valueFrom
        self.valueTo = valueTo

    def get_value(self):
        return self.value

    def set_value(self, value):
        self.value = value
