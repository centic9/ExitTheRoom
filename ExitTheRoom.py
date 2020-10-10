#!/usr/bin/python3

# import RPi.GPIO as GPIO

# import TM1638
import os
from time import sleep
from GPIOInput import ButtonInput, SwitchInput, RotaryInput
from GPIOOutput import TextOutput, LEDOutput, SoundOutput
from Mapping import Mapping


CONTROL_FILE = "/tmp/control.txt"
MAPPINGS = [
    # Button 1 + Switch and Toggle -> Hello and play a sound
    Mapping("button 1",
        [ButtonInput("tm1638", "", 1), SwitchInput("press", 13), SwitchInput("toggle", 6)],
        [TextOutput("tm1638", "", "Hello", 5000), LEDOutput("", 14, 2000), SoundOutput("scream", "audio/AARGH-No2DZmefBTA.mp3", -1)]
    ),
    Mapping("rotary range",
        [RotaryInput("rotary", 12, 128, 255)],
        [TextOutput("tm1638", "", "Done", -1)]
    )
]


def read_file():
    # print("Reading file")
    try:
        file = open(CONTROL_FILE, "r")
        msg = file.read()
        file.close()

        os.remove(CONTROL_FILE)

        return msg
    except FileNotFoundError:
        pass

    return ""


print("Starting main loop")
while True:
    # print("Running main loop")

    # check control
    control = read_file()
    if control != "":
        print("Applying control-command: " + control)
        for mapping in MAPPINGS:
            mapping.control(control)

    # check inputs defined in the mappings and perform matching outputs
    for mapping in MAPPINGS:
        mapping.check()

    # remember achievements

    # delay
    sleep(0.1)
