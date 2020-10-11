#!/usr/bin/python3

import RPi.GPIO as GPIO
import time


GPIO.setwarnings(True)
GPIO.setmode(GPIO.BCM)

GPIO.setup(4, GPIO.OUT)

for i in range(5):
    GPIO.output(4, GPIO.HIGH)
    time.sleep(0.5)
    GPIO.output(4, GPIO.LOW)
    time.sleep(0.5)

GPIO.cleanup() # Clean up
