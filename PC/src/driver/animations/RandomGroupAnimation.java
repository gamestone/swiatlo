package driver.animations;

import driver.protocol.ProtocolException;
import driver.channels.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import mlogger.Log;

public class RandomGroupAnimation extends Thread {

    private final Group group;
    private final boolean down;
    private final int dst;
    private final Random random;

    public RandomGroupAnimation(Group group, boolean down) {

        this.down = down;

        this.group = group;
        dst = down ? 0 : group.loadValue();

        random = new Random();
    }

    @Override
    public void run() {

        int speed = down ? 2 + random.nextInt(5) : random.nextInt(5);
        int delay = random.nextInt(150) + random.nextInt((speed + 1) * 2);

        List<PwmChannel> list = Arrays.asList(group.channels);

        String order = "normal";
        switch (random.nextInt(4)) {
            case 1:
                order = "reverse";
                Collections.reverse(list);
                break;
            case 2:
                order = "random";
                Collections.shuffle(list);
                break;
            case 3:
                list.clear();
                order = "twoWay";
                for (int i = 0; i < group.channels.length; i++)
                    if (i < group.channels.length / 2)
                        list.add(group.channels[i]);
                    else
                        list.add(0, group.channels[i]);
                break;
        }

        Log.event("Animation, speed " + speed + ", delay: " + delay + ", order: " + order + ", value " + dst);

        for (Channel ch : list) {
            try {
                ch.setValue(dst, speed);
            } catch (ProtocolException ex) {
                Log.error(ex);
            }
            try {
                Thread.sleep(delay);
            } catch (InterruptedException ex) {
                return;
            }

        }
        group.currentValue = dst;
    }

}
