#!/usr/bin/python3

import RPi.GPIO as GPIO
import time


pin = 21

GPIO.setwarnings(True)
GPIO.setmode(GPIO.BCM)

GPIO.setup(pin, GPIO.OUT)

for i in range(5):
    GPIO.output(pin, GPIO.HIGH)
    time.sleep(0.5)
    GPIO.output(pin, GPIO.LOW)
    time.sleep(0.5)

GPIO.cleanup() # Clean up
