#!/usr/bin/python3

# import RPi.GPIO as GPIO
# import TM1638
import os
from time import sleep


CONTROL_FILE = "/tmp/control.txt"


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
        print("Had control-command: " + control)

    # read inputs

    # check mappings

    # perform outputs

    # remember achievements

    # delay
    sleep(0.1)
