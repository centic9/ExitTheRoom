import time


def current_milli_time():
    return int(round(time.time() * 1000))


class Output:
    def __init__(self, name, delay):
        self.name = name
        if delay == -1:
            self.time = -1
        else:
            self.time = current_milli_time() + delay

    def get_name(self):
        return self.name

    def clear_internal(self):
        raise Exception("Should overwrite clearInternal() in Output")

    def clear(self):
        if time != -1:
            if time < current_milli_time:
                self.clear_internal()
