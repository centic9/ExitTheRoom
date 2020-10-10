import RPi.GPIO as GPIO
import time

def button_callback(channel):
  print("Button was pushed!")

GPIO.setwarnings(True)
GPIO.setmode(GPIO.BCM)

for i in range(1, 30):
  GPIO.setup(i, GPIO.IN, pull_up_down=GPIO.PUD_DOWN) # Set pin 10 to be an input pin and set initial value to be pulled low (off)

while True:
  for i in range(1, 30):
    if GPIO.input(i) == GPIO.HIGH:
        print("Button " + str(i) + " was pushed!")
    else:
        print("Button " + str(i) + " was not pushed")
  time.sleep(0.5)

GPIO.cleanup() # Clean up

