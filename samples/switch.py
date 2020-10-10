import RPi.GPIO as GPIO
import time

def button_callback(channel):
    print("Button was pushed!")

GPIO.setwarnings(True)
GPIO.setmode(GPIO.BCM)

GPIO.setup(19, GPIO.IN, pull_up_down=GPIO.PUD_DOWN) # Set pin 10 to be an input pin and set initial value to be pulled low (off)

while True:
    if GPIO.input(19) == GPIO.HIGH:
        print("Button was pushed!")
    else:
        print("Button was not pushed")
    time.sleep(0.5)

GPIO.cleanup() # Clean up

