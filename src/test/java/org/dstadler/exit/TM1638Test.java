package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.RaspiPin;
import net.bytebuddy.pool.TypePool;
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

        assertEquals(Integer.MAX_VALUE, TM1638.pow2(Integer.MAX_VALUE));
    }
}