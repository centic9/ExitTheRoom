import RPi.GPIO as GPIO
import time
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BCM)
buzzer=26
GPIO.setup(buzzer,GPIO.OUT)
try:
        while True:
                GPIO.output(buzzer,GPIO.HIGH)
                time.sleep(0.5)
                GPIO.output(buzzer,GPIO.LOW)
                time.sleep(0.5)
except KeyboardInterrupt:
        GPIO.cleanup()
        pass
