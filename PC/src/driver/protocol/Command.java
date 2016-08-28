package driver.protocol;

public enum Command {

    hello(0),
    setId(1),
    setPwm(2),
    getPwm(3),
    setPwmGroups(4),
    groups(5),
    switchStates(6),
    error(7),
    textMessage(8),
    resetDevice(9),
    instanceId(10),
    eepromData(11);

    public static String errorToString(int code) {

        switch (code) {
            case 1:
                return "UNKNOWN_COMMAND";
            case 2:
                return "WRONG_CRC";
            case 3:
                return "WRONG_CHANNELS";
            case 4:
                return "INCORRECT_GROUP_NUMBER";
            case 5:
                return "UART_BUFFER_CLEARED";
            case 6:
                return "ERR_SWITCH_STATE";
        }
        return null;
    }

    public final byte id;

    private Command(int id) {
        this.id = (byte) id;
    }

    public static Command get(int id) throws ProtocolException {
        for (Command c : Command.values())
            if (c.id == id)
                return c;

        throw new ProtocolException("Nieprawidłowa komenda " + id);
    }

}
