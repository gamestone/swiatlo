package driver;

import com.hashes.Hex;
import driver.protocol.ProtocolException;

import static driver.Index.T13;
import driver.channels.Channel;
import driver.channels.PwmChannel;
import java.util.LinkedHashMap;
import java.util.Map;

public class RGB {

    //  http://www.w3schools.com/colors/colors_hsl.asp
    public final static Map<String, RGB> all = new LinkedHashMap<>();

    public final Location location;

    public final String key;
    public final String name;

    public final RGBChannel chaH;
    public final RGBChannel chaS;
    public final RGBChannel chaL;

    public final PwmChannel chaR;
    public final PwmChannel chaG;
    public final PwmChannel chaB;

    public int hue;
    public int saturation;
    public int lightness;

    public RGB(Location location, String key, String name, PwmChannel chaR, PwmChannel chaG, PwmChannel chaB) {
        this.location = location;

        this.chaR = chaR;
        this.chaG = chaG;
        this.chaB = chaB;

        this.chaH = new RGBChannel(HSL.H, key);
        this.chaS = new RGBChannel(HSL.S, key);
        this.chaL = new RGBChannel(HSL.L, key);
        this.key = key;
        this.name = name;
        all.put(key, this);
    }

    protected void doSetValue() throws ProtocolException {
        float r, g, b;

        float h = hue / 4095f;
        float s = saturation / 4095f;
        float l = lightness / 4095f;

        if (s == 0f) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5f ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        System.out.println(Hex.toString((byte) (r * 255)) + " "
                + Hex.toString((byte) (g * 255)) + " "
                + Hex.toString((byte) (b * 255)));

        chaR.setValue((int) (r * 4095), 0);
        chaG.setValue((int) (g * 4095), 0);
        chaB.setValue((int) (b * 4095), 0);
    }

    public static float hueToRgb(float p, float q, float t) {
        if (t < 0f)
            t += 1f;
        if (t > 1f)
            t -= 1f;
        if (t < 1f / 6f)
            return p + (q - p) * 6f * t;
        if (t < 1f / 2f)
            return q;
        if (t < 2f / 3f)
            return p + (q - p) * (2f / 3f - t) * 6f;
        return p;
    }

    public static void setRGB(int value, boolean left, boolean right) throws ProtocolException {

        int r = (value) & 0xFF;
        int g = (value >> 8) & 0xFF;
        int b = (value >> 16) & 0xFF;
        int a = (value >> 24) & 0xFF;

        int color = 0;

        if (left) {
            T13.C1.setValue(4095 * b / 255, 0);
            T13.C2.setValue(4095 * g / 255, 0);
            T13.C3.setValue(4095 * r / 255, 0);
        }

        if (right) {
            T13.C4.setValue(4095 * r / 255, 0);
            T13.C5.setValue(4095 * g / 255, 0);
            T13.C6.setValue(4095 * b / 255, 0);
        }

        // 1 - L B
        // 2 - L G
        // 3 - L R
        //4 - R R
        // 5 - R G
        // 6 - L B
    }

    public static enum HSL {
        H, S, L
    }

    public class RGBChannel extends Channel {

        public final HSL hsl;

        public RGBChannel(HSL hsl, String key) {
            super(-1, key + ":" + hsl.name());
            this.hsl = hsl;
        }

        @Override
        protected void doSetValue(int value, int speed) throws ProtocolException {
            switch (hsl) {
                case H:
                    hue = value;
                    break;
                case S:
                    saturation = value;
                    break;
                case L:
                    lightness = value;
                    break;
            }

            RGB.this.doSetValue();
        }

    }

}
