#!/usr/bin/python3

import RPi.GPIO as GPIO
import TM1638
import time


led = 21
buzzer = 12

GPIO.setwarnings(True)
GPIO.setmode(GPIO.BCM)

GPIO.setup(led, GPIO.OUT)
GPIO.setup(buzzer, GPIO.OUT)


# These are the pins the display is connected to. Adjust accordingly.
# In addition to these you need to connect to 5V and ground.

DIO = 17
CLK = 27
STB = 22

display = TM1638.TM1638(DIO, CLK, STB)

display.enable(1)

for i in range(1, 30):
    if i == led or i == buzzer or i == DIO or i == CLK or i == STB:
        continue

    # Set pin to be an input pin and set initial value to be pulled low (off)
    GPIO.setup(i, GPIO.IN, pull_up_down=GPIO.PUD_DOWN)

count = 0
keysPrev = -1
try:
    while True:
        GPIO.output(led, GPIO.HIGH)
        GPIO.output(buzzer, GPIO.HIGH)
        time.sleep(0.5)
        GPIO.output(led, GPIO.LOW)
        GPIO.output(buzzer, GPIO.LOW)

        for i in range(1, 30):
            if i == led or i == buzzer or i == DIO or i == CLK or i == STB:
                continue

            if GPIO.input(i) == GPIO.HIGH:
                print("Button " + str(i) + " was pushed!")
            else:
                print("Button " + str(i) + " was not pushed")
        
        keys = display.get_buttons64()
        if keys != 0 and keys != keysPrev:
            print(str(keys))
            keysPrev = keys
            display.set_text(str(keys))

        time.sleep(0.5)
except KeyboardInterrupt:
    display.set_text('')
    GPIO.cleanup()
    pass

