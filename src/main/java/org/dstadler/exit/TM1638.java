package org.dstadler.exit;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TM1638 {
    private static final Map<Character, Integer> FONT = new HashMap<>();
    static {
        FONT.put('0', 0b00111111);
        FONT.put('1', 0b00000110);
        FONT.put('2', 0b01011011);
        FONT.put('3', 0b01001111);
        FONT.put('4', 0b01100110);
        FONT.put('5', 0b01101101);
        FONT.put('6', 0b01111101);
        FONT.put('7', 0b00000111);
        FONT.put('8', 0b01111111);
        FONT.put('9', 0b01101111);
        FONT.put('a', 0b01110111);
        FONT.put('b', 0b01111100);
        FONT.put('c', 0b01011000);
        FONT.put('d', 0b01011110);
        FONT.put('e', 0b01111001);
        FONT.put('f', 0b01110001);
        FONT.put('g', 0b01011111);
        FONT.put('h', 0b01110100);
        FONT.put('i', 0b00010000);
        FONT.put('j', 0b00001110);
        FONT.put('l', 0b00111000);
        FONT.put('n', 0b01010100);
        FONT.put('o', 0b01011100);
        FONT.put('p', 0b01110011);
        FONT.put('r', 0b01010000);
        FONT.put('s', 0b01101101);
        FONT.put('t', 0b01111000);
        FONT.put('u', 0b00111110);
        FONT.put('y', 0b01101110);

        // added from https://github.com/thilaire/rpi-TM1638/blob/master/rpi_TM1638/Font.py
        FONT.put(' ', 0b00000000);  // (32) <space>
        FONT.put('!', 0b10000110);  // (33) !
        FONT.put('"', 0b00100010);  // (34) "
        FONT.put('(', 0b00110000);  // (40) (
        FONT.put(')', 0b00000110);  // (41) )
        // FONT.put(');', 0b00000100);  // (44) );
        FONT.put('-', 0b01000000);  // (45) -
        FONT.put('.', 0b10000000);  // (46) .
        FONT.put('/', 0b01010010);  // (47) /
        FONT.put('=', 0b01001000);  // (61) =
        FONT.put('?', 0b01010011);  // (63) ?
        FONT.put('@', 0b01011111);  // (64) @
        FONT.put('A', 0b01110111);  // (65) A
        FONT.put('B', 0b01111111);  // (66) B
        FONT.put('C', 0b00111001);  // (67) C
        FONT.put('D', 0b00111111);  // (68) D
        FONT.put('E', 0b01111001);  // (69) E
        FONT.put('F', 0b01110001);  // (70) F
        FONT.put('G', 0b00111101);  // (71) G
        FONT.put('H', 0b01110110);  // (72) H
        FONT.put('I', 0b00000110);  // (73) I
        FONT.put('J', 0b00011111);  // (74) J
        FONT.put('K', 0b01101001);  // (75) K
        FONT.put('L', 0b00111000);  // (76) L
        FONT.put('M', 0b01010100);  // (77) M (equal to n!)
        //'M', 0b00010101);  // (77) M
        FONT.put('N', 0b00110111);  // (78) N
        FONT.put('O', 0b00111111);  // (79) O
        FONT.put('P', 0b01110011);  // (80) P
        FONT.put('Q', 0b01100111);  // (81) Q
        FONT.put('R', 0b00110001);  // (82) R
        FONT.put('S', 0b01101101);  // (83) S
        FONT.put('T', 0b01111000);  // (84) T
        FONT.put('U', 0b00111110);  // (85) U
        FONT.put('V', 0b00101010);  // (86) V
        FONT.put('W', 0b00011101);  // (87) W
        FONT.put('X', 0b01110110);  // (88) X
        FONT.put('Y', 0b01101110);  // (89) Y
        FONT.put('Z', 0b01011011);  // (90) Z
        FONT.put('[', 0b00111001);  // (91) [
        FONT.put(']', 0b00001111);  // (93) ]
        FONT.put('_', 0b00001000);  // (95) _
        FONT.put('`', 0b00100000);  // (96) `
        FONT.put('k', 0b01110101);  // (107) k
        FONT.put('m', 0b01010101);  // (109) m
        FONT.put('q', 0b01100111);  // (113) q
        FONT.put('v', 0b00101010);  // (118) v
        FONT.put('w', 0b00011101);  // (119) w
        FONT.put('x', 0b01110110);  // (120) x
        FONT.put('z', 0b01000111);  // (122) z
        FONT.put('{', 0b01000110);  // (123) {
        FONT.put('|', 0b00000110);  // (124) |
        FONT.put('}', 0b01110000);  // (125) }
        FONT.put('~', 0b00000001);  // (126) ~
    }

    private final GpioController gpio;
    private final Pin dio;
    //private final Pin clk;
    //private final Pin stb;

    private GpioPinDigitalOutput dioOut;
    private final GpioPinDigitalOutput clkOut;
    private final GpioPinDigitalOutput stbOut;

    public TM1638(GpioController gpio, Pin dio, Pin clk, Pin stb) {
        this.gpio = gpio;

        this.dio = dio;
        //this.clk = clk;
        //this.stb = stb;

        dioOut = gpio.provisionDigitalOutputPin(dio, "DIO");
        clkOut = gpio.provisionDigitalOutputPin(clk, "CLK");
        stbOut = gpio.provisionDigitalOutputPin(stb, "STB");
    }

    public void enable() {
        enable(7);
    }

    public void enable(int intensity) {
        stbOut.high();
        clkOut.high();

        send_command(0x40);
        send_command(0x80 | 8 | Math.min(7, intensity));

        stbOut.low();
        send_byte(0xC0);
        for(int i = 0;i < 16;i++) {
            send_byte(0x00);
        }
        stbOut.high();
    }

    private void send_command(int cmd) {
        stbOut.low();
        send_byte(cmd);
        stbOut.high();
    }

    private void send_data(int addr, int data) {
        send_command(0x44);
        stbOut.low();
        send_byte(0xC0 | addr);
        send_byte(data);
        stbOut.high();
    }

    private void send_byte(int data) {
        for(int i = 0;i < 8;i++) {
            clkOut.low();
            dioOut.setState((data & 1) == 1);
            data >>= 1;
            clkOut.high();
        }
    }

    public void set_led(int n, int color) {
        send_data((n << 1) + 1, color);
    }

    public void send_char(int pos, int data) {
        send_char(pos, data, false);
    }

    public void send_char(int pos, int data, boolean dot) {
        send_data(pos << 1, data | (dot ? 128 : 0));
    }

    public void set_digit(int pos, char digit) {
        set_digit(pos, digit, false);
    }

    public void set_digit(int pos, char digit, boolean dot) {
        for(int i = 0;i < 6;i++) {
            send_char(i, get_bit_mask(pos, digit, i), dot);
        }
    }

    private int get_bit_mask(int pos, char digit, int bit) {
        int mask = FONT.get(digit);
        return ((mask >> bit) & 1) << pos;
    }

    public void set_text(String text) {
        int dots = 0b00000000;
        int pos = text.indexOf('.');
        if (pos != -1) {
            // For my boards, the dot-order is non-linear:
            // 8 4 2 1 128 64 32 16
            int realPos = pos + (8 - text.length());
            if (realPos < 0) {
                throw new IllegalArgumentException("not possible to render: " + realPos + ": " + pos + ": " + text);
            } else if (realPos >=4) {
                dots = dots | (128 >> realPos - 4);
            } else {
                dots = dots | (8 >> realPos);
            }
            text = text.replace(".", "");
        }

        send_char(7, rotate_bits(dots));
        text = StringUtils.substring(text, 0, Math.min(8, text.length()));
        text = StringUtils.reverse(text);
        text = StringUtils.leftPad(text, 8, ' ');

        // my TM1638 board has the two 4-char displays exchanged
        text = text.substring(4, 8) + text.substring(0,4);

        for(int i = 0;i < 7;i++) {
            int b = 0b00000000;
            for(int p = 0;p < 8;p++) {
                char c = text.charAt(p);
                if (c != ' ') {
                    b = (b | get_bit_mask(p, c, i));
                }
            }
            send_char(i, rotate_bits(b));
        }
    }

    private int receive() {
        int temp = 0;

        // temporarily set DIO to input
        gpio.unprovisionPin(dioOut);
        GpioPinDigitalInput dioInTemp = gpio.provisionDigitalInputPin(dio, "DIO",
                PinPullResistance.PULL_UP);

        for(int i = 0;i < 8;i++) {
            temp >>= 1;
            clkOut.low();
            if (dioInTemp.isHigh())
                temp |= 0x80;
            clkOut.high();
        }

        // switch back DIO to output
        gpio.unprovisionPin(dioInTemp);
        dioOut = gpio.provisionDigitalOutputPin(dio, "DIO");

        return temp;
    }

    public int get_buttons() {
        int keys = 0;
        stbOut.low();
        send_byte(0x42);
        for(int i = 0;i < 4;i++) {
            keys |= receive() << i;
        }
        stbOut.high();
        return keys;
    }

    public int get_buttons64() {
        int keys = 0;
        stbOut.low();
        send_byte(0x42);
        for(int i = 0;i < 4;i++) {
            int val = receive();
            keys += val * (pow2(i * 8));
        }
        stbOut.high();
        return keys;
    }

    private int rotate_bits(int num) {
        for (int i = 0; i < 4; i++) {
            num = rotr(num, 8);
        }
        return num;
    }

    private int rotr(int num, int bits) {
        num &= (pow2(bits)-1);
        int bit = num & 1;
        num >>= 1;
        if (bit == 1) {
            num |= (1 << (bits - 1));
        }
        return num;
    }

    public static int pow2(int power) {
        // avoid integer-overflow, at most 2 to the power of 30 can be computed for integer
        if(power > 30) {
            return Integer.MAX_VALUE;
        }

        return 1 << power;
    }
}
