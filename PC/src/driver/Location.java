package driver;

public enum Location {

    HALL("Korytarz", 'H'),
    KITCHEN("Kuchnia", 'K'),
    BEDROM("Sypialnia", 'D'),
    ROOM("Pokój dziecięcy", 'R'),
    SALON("Salon", 'S'),
    BATHROM("Łazienka", 'T');

    public final String name;
    public final char key;

    private Location(String name, char key) {
        this.name = name;
        this.key = key;
    }

}
