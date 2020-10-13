package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TM1638Test {
    private final GpioController gpio = mock(GpioController.class);
    private final GpioPinDigitalOutput dio = mock(GpioPinDigitalOutput.class);
    private final GpioPinDigitalOutput clk = mock(GpioPinDigitalOutput.class);
    private final GpioPinDigitalOutput stb = mock(GpioPinDigitalOutput.class);

    @BeforeEach
    public void setUp() {
        when(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "DIO")).thenReturn(dio);

        when(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_02, "CLK")).thenReturn(clk);

        when(gpio.provisionDigitalOutputPin(RaspiPin.GPIO_03, "STB")).thenReturn(stb);
    }

    @Test
    public void test() {
        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        tm1638.set_digit(1, '7');
    }

    @Test
    public void testText() {
        TM1638 tm1638 = new TM1638(gpio, RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03);
        tm1638.enable();

        tm1638.set_text("");
        tm1638.set_text("a");
        tm1638.set_text("abcdefghijklmnopqrstuvwxyz");
        tm1638.set_text("              ");
        tm1638.set_text("1.234E237");

        assertThrows(IllegalArgumentException.class, () -> tm1638.set_text("..............."));
    }

    @Test
    public void testPow2() {
        assertEquals(1, TM1638.pow2(0));
        assertEquals(2, TM1638.pow2(1));
        assertEquals(4, TM1638.pow2(2));
        assertEquals(8, TM1638.pow2(3));
        assertEquals(16, TM1638.pow2(4));
        assertEquals(32, TM1638.pow2(5));
        assertEquals(64, TM1638.pow2(6));
        assertEquals(128, TM1638.pow2(7));
        assertEquals(256, TM1638.pow2(8));
        assertEquals(512, TM1638.pow2(9));
        assertEquals(1024, TM1638.pow2(10));
        assertEquals(524288, TM1638.pow2(19));
        assertEquals(1048576, TM1638.pow2(20));
        assertEquals(536870912, TM1638.pow2(29));
        assertEquals(1073741824, TM1638.pow2(30));

        // at most 2 to the power of 30 can be computed for integer
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(31));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(32));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(33));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(40));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(41));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(24636));
        assertEquals(Integer.MAX_VALUE, TM1638.pow2(Integer.MAX_VALUE));
    }
}