import time


def current_milli_time():
    return int(round(time.time() * 1000))


class Output:
    def __init__(self, name, delay):
        self.name = name
        self.time = -1
        self.delay = delay

    def get_name(self):
        return self.name

    def clear_internal(self):
        raise Exception("Should overwrite clear_internal() in Output")

    def clear(self):
        if time != -1:
            if time < current_milli_time:
                self.clear_internal()

    def set_value_internal(self, value):
        raise Exception("Should overwrite set_value_internal() in Output")

    def set_value(self, value):
        self.set_value_internal(value)
        if self.delay == -1:
            self.time = -1
        else:
            self.time = current_milli_time() + self.delay
