package driver;

import driver.protocol.ProtocolException;
import static driver.Main.protocol;
import driver.protocol.Command;
import mlogger.Log;

import static driver.protocol.Command.setPwm;

public class Test {

    public static void test() throws ProtocolException {

        protocol.sendHello();
        
        
      //  Index.test.updateGroups();
              

        if (true)
            return;

        new Thread(new Runnable() {

            @Override
            public void run() {

                int id = 1;

                try {
                    ;

                    if (true)
                        return;;

                    send(setPwm, 0, 9, 63, 0);
                    state(sypialnia, 0, 2);

                    //    state(korytarz, 4095, 100);
                    //  state(korytarz, 0, 200);
                    state(sypialnia, 0, 200);

                    if (true)
                        return;;
                    state(korytarz, 4095, 100);

                    state(korytarz, 0, 200);
                    //     animacja(korytarz);

                    if (true)
                        return;

                    //    send(setPwm, false, id, 63, 0, 4095);
                    send(setPwm, 0, id, 63, 0);

                    for (int i = 0; i < 6; i++) {
                        send(setPwm, 3, id, 1 << i, 30);
                        Thread.sleep(600);
                    }
                    Thread.sleep(1000);

                    for (int i = 0; i < 6; i++) {
                        send(setPwm, 2, id, 1 << i, 4096);
                        Thread.sleep(100);
                    }
                    //      if (true) return;

                    Thread.sleep(200);
                    for (int i = 0; i < 6; i++) {
                        send(setPwm, 2, id, 1 << i, 0);
                        Thread.sleep(100);
                    }

                } catch (Exception e) {
                    Log.error(e);
                }
            }
        }).start();

    }

    private final static int[][] sypialnia = new int[][]{
        {9, 4},
        {9, 5},
        {9, 1},
        {9, 6},
        {5, 5},
        {5, 6},
        {5, 3},
        {5, 4},
        {5, 1},
        {5, 2},
        {1, 3}

    };

    private final static int[][] korytarz = new int[][]{
        {1, 5},
        {1, 4},
        {1, 6},
        {8, 6},
        {8, 4},
        {8, 2},
        {8, 1},
        {3, 4},
        {3, 1},
        {3, 2},
        {3, 3}

    };

    public static void send(Command command, int params, int terminalId, int data, int value)
            throws ProtocolException {
        Main.protocol.send(command, params, terminalId, data, value);
    }

    private static void animacja(int[][] arr) throws ProtocolException, InterruptedException {
        for (int i = 0; i < arr.length; i++) {
            send(setPwm, 3, arr[i][0], 1 << arr[i][1] - 1, 30);
            Thread.sleep(500);
        }

        Thread.sleep(1000);

        //        send(setPwm, false, id, 63, 0, 0);
        for (int i = 0; i < arr.length; i++) {
            send(setPwm, 2, arr[i][0], 1 << arr[i][1] - 1, 4096);
            Thread.sleep(200);
        }
        //  Thread.sleep(200);

        for (int i = 0; i < arr.length; i++) {
            send(setPwm, 2, arr[i][0], 1 << arr[i][1] - 1, 0);
            Thread.sleep(200);
        }

    }

    private static void state(int[][] arr, int state, int delay) throws ProtocolException, InterruptedException {
        for (int i = 0; i < arr.length; i++) {
            send(setPwm, 2, arr[i][0], 1 << arr[i][1] - 1, state);
            Thread.sleep(delay);
        }
    }

}
