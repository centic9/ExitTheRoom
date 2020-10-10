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
        print("Had control-command: " + msg)
        file.close()

        os.remove(CONTROL_FILE)
    except FileNotFoundError:
        pass


print("Starting main loop")
while True:
    # print("Running main loop")

    # check control
    read_file()

    # read inputs

    # check mappings

    # perform outputs

    # remember achievements

    # delay
    sleep(0.1)
