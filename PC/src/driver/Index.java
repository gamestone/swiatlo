package driver;

import driver.channels.Group;
import driver.channels.PwmChannel;
import driver.switches.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class Index {

    public final static Map<Location, PwmChannel[]> locationChannels = new LinkedHashMap<>();

    // ---------------------- terminale ----------------------------------
    public final static Terminal T1 = new Terminal(Location.KITCHEN, 1);
    public final static Terminal T2 = new Terminal(Location.KITCHEN, 2);
    public final static Terminal T3 = new Terminal(Location.KITCHEN, 3);
    public final static Terminal T8 = new Terminal(Location.KITCHEN, 8);

    public final static Terminal T5 = new Terminal(Location.BEDROM, 5);
    public final static Terminal T7 = new Terminal(Location.BEDROM, 7);
    public final static Terminal T9 = new Terminal(Location.BEDROM, 9);
    public final static Terminal T13 = new Terminal(Location.BEDROM, 13);

    public final static Terminal T10 = new Terminal(Location.BATHROM, 10);

    public final static Terminal T11 = new Terminal(Location.SALON, 11);
    public final static Terminal T12 = new Terminal(Location.SALON, 12);
    public final static Terminal T15 = new Terminal(Location.SALON, 15);
    public final static Terminal T16 = new Terminal(Location.SALON, 16);
    public final static Terminal T19 = new Terminal(Location.SALON, 19);

    public final static Terminal T17 = new Terminal(Location.ROOM, 17);
    public final static Terminal T4 = new Terminal(Location.ROOM, 4);
    public final static Terminal T14 = new Terminal(Location.ROOM, 14);
    //----------------------- grupy ----------------------------
    public final static Group gKuchnia = new Group(1, Location.KITCHEN, "Sufit",
            T1.C4, T1.C6, T8.C6, T8.C4, T8.C2, T8.C1, T3.C4,
            T3.C1, T3.C2, T3.C3, T2.C5, T2.C3, T2.C4, T2.C2);

    public final static Group gKuchniaSzafki = new Group(101, Location.KITCHEN, "Szafki", T8.C5);

    public final static Group gKuchniaStol = new Group(102, Location.KITCHEN, "Stół",
            T2.C1, T2.C6);

    public final static Group gKorytarz = new Group(2, Location.HALL, "Sufit",
            T1.C4, T1.C5, T7.C3, T7.C1, T7.C2, T7.C5, T7.C6,
            T1.C1, T1.C2, T2.C2);

    public final static Group gKorytarzZarowka = new Group(103, Location.HALL, "Żarówka", T3.C6);

    public final static Group gSypialnia = new Group(3, Location.BEDROM, "Sufit",
            T9.C4, T9.C5, T9.C1, T9.C6, T5.C5, T5.C6,
            T5.C3, T5.C4, T5.C1, T5.C2, T1.C3, T7.C4);

    public final static Group gSypialniaLustro = new Group(104, Location.BEDROM, "Lustro",
            T9.C2, T9.C3);

    public final static Group gLazienka = new Group(105, Location.BATHROM, "Sufit",
            T10.C1, T10.C2, T10.C3, T10.C5);

    public final static Group gLazienkaLustro = new Group(106, Location.BATHROM, "Lustro",
            T10.C4);

    public final static Group gSalonSufit = new Group(4, Location.SALON, "Sufit",
            T11.C3, T11.C1, T12.C6, T12.C4, T12.C1, T12.C2, T16.C1,
            T16.C4, T16.C3, T16.C5, T16.C2, T16.C6, T11.C5, T11.C2);

    public final static Group gSalonZabudowa = new Group(5, Location.SALON, "Zabudowa",
            T12.C3, T12.C5, T11.C4, T11.C6);

    public final static Group gSalonZyrandolWew = new Group(107, Location.SALON, "Żyrandol (wewnętrzny)",
            T15.C4, T15.C5, T15.C6);

    public final static Group gSalonZyrandolZew = new Group(108, Location.SALON, "Żyrandol (zewnętrzny)",
            T15.C1, T15.C2, T15.C3);

    public final static Group gSalonPrzod = new Group(6, Location.SALON, "Przód",
            T12.C4, T12.C6, T11.C1, T11.C3);

    public final static Group gPokojSufit = new Group(7, Location.ROOM, "Sufit",
            T14.C3, T14.C1, T4.C3, T4.C2, T4.C1, T4.C6, T4.C4, T4.C5,
            T17.C5, T17.C4, T17.C3, T17.C1);

    public final static Group gPokojZyrandol = new Group(109, Location.ROOM, "Żyrandol",
            T17.C2, T17.C6);

    public final static Group gPokojKinkiet = new Group(110, Location.ROOM, "Kinkiet", T14.C2);

    public final static Group gSalonTV = new Group(111, Location.SALON, "TV", T19.channels);

    //===========================================================================
    public final static RGB rgbSalon1 = new RGB(Location.SALON, "rgbS1", "RGB 1",
            T13.C3, T13.C2, T13.C1);

    public final static RGB rgbSalon2 = new RGB(Location.SALON, "rgbS2", "RGB 2",
            T13.C4, T13.C5, T13.C6);

    public final static RGB rgbRoom = new RGB(Location.ROOM, "rgbR", "RGB",
            T14.C5, T14.C6, T14.C4);
    //===========================================================================
    public final static Switch swKuchnia1 = new Switch(
            Location.KITCHEN, "1A", Index.T1, 0x20, 0x10, gKuchnia);

    public final static Switch swKuchnia2 = new Switch(
            Location.KITCHEN, "1B", Index.T1, 0x02, 0x01, gKuchniaStol);

    public final static Switch swKuchniaSzafki = new Switch(
            Location.KITCHEN, "2", Index.T8, 0x40, 0x04, gKuchniaSzafki);

    public final static Switch swSypialnia1 = new Switch(
            Location.BEDROM, "1A", Index.T7, 0x02, 0x01, gSypialnia);

    public final static Switch swSypialnia2 = new Switch(
            Location.BEDROM, "1B", Index.T7, 0x20, 0x10, gKorytarz);

    public final static Switch swSypialniaLustro = new Switch(
            Location.BEDROM, "3", Index.T9, 0x04, 0x40, gSypialniaLustro);

    public final static Switch swSypialniaLozko1 = new Switch(
            Location.BEDROM, "2A", Index.T9, 0x20, 0x10, gSypialnia);

    public final static Switch swSypialniaLozko2 = new Switch(
            Location.BEDROM, "2B", Index.T9, 0x02, 0x01, gSypialniaLustro);

    public final static Switch swKorytarz1 = new Switch(
            Location.HALL, "1A", Index.T1, 0x40, 0x80, gKorytarz);

    public final static Switch swKorytarz2 = new Switch(
            Location.HALL, "1B", Index.T1, 0x04, 0x08, gKorytarzZarowka);

    public final static Switch swLazienka1 = new Switch(
            Location.BATHROM, "1A", Index.T10, 0x02, 0x01, gLazienka);

    public final static Switch swLazienka2 = new Switch(
            Location.BATHROM, "1B", Index.T10, 0x20, 0x10, gLazienkaLustro);

    public final static Switch swSalon1 = new Switch(
            Location.SALON, "3A", Index.T7, 0x80, 0x40, gSalonSufit);

    public final static Switch swSalon2 = new Switch(
            Location.SALON, "3B", Index.T7, 0x08, 0x04, gSalonZabudowa);

    public final static Switch swSalonA1 = new Switch(
            Location.SALON, "1A", Index.T13, 0x80, 0x40, gSalonZyrandolWew);

    public final static Switch swSalonA2 = new Switch(
            Location.SALON, "1B", Index.T13, 0x08, 0x04, gSalonZyrandolZew);

    public final static Switch swSalonB1 = new Switch(
            Location.SALON, "2A", Index.T13, 0x20, 0x10, gSalonTV);

    public final static Switch swSalonB2 = new Switch(
            Location.SALON, "2B", Index.T13, 0x02, 0x01, gSalonTV);

    public final static Switch swPokojA1 = new Switch(
            Location.ROOM, "1A", Index.T17, 0x04, 0x08, Index.gPokojSufit);

    public final static Switch swPokojA2 = new Switch(
            Location.ROOM, "1B", Index.T17, 0x40, 0x80, gPokojZyrandol);

    public final static Switch swPokojB1 = new Switch(
            Location.ROOM, "2A", Index.T17, 0x02, 0x01, gPokojKinkiet);

    public final static Switch swPokojB2 = new Switch(
            Location.ROOM, "2B", Index.T17, 0x20, 0x10, gPokojKinkiet);

    static {
        locationChannels.put(Location.HALL, new PwmChannel[]{
            T1.C4, T1.C5, T7.C3, T7.C1, T7.C2, T7.C5, T7.C6,
            T1.C1, T1.C2, T2.C2, T3.C6
        });

        locationChannels.put(Location.KITCHEN, new PwmChannel[]{
            T1.C4, T1.C6, T8.C6, T8.C4, T8.C2, T8.C1, T3.C4,
            T3.C1, T3.C2, T3.C3, T2.C5, T2.C3, T2.C4, T2.C2,
            T8.C5, T2.C1, T2.C6
        });

        locationChannels.put(Location.BEDROM, new PwmChannel[]{
            T9.C4, T9.C5, T9.C1, T9.C6, T5.C5, T5.C6,
            T5.C3, T5.C4, T5.C1, T5.C2, T1.C3, T7.C4,
            T9.C2, T9.C3
        });

        locationChannels.put(Location.BATHROM, new PwmChannel[]{
            T10.C1, T10.C2, T10.C3, T10.C5, T10.C4
        });

        locationChannels.put(Location.SALON, new PwmChannel[]{
            T11.C3, T11.C1, T12.C6, T12.C4, T12.C1, T12.C2, T16.C1,
            T16.C4, T16.C3, T16.C5, T16.C2, T16.C6, T11.C5, T11.C2,
            T12.C3, T12.C5, T11.C4, T11.C6, T15.C4, T15.C5, T15.C6,
            T15.C1, T15.C2, T15.C3, T12.C4, T12.C6, T11.C1, T11.C3,
            T19.C1, T19.C2, T19.C3, T19.C4, T19.C5, T19.C6
        });

        locationChannels.put(Location.ROOM, new PwmChannel[]{
            T14.C3, T14.C1, T4.C3, T4.C2, T4.C1, T4.C6, T4.C4, T4.C5,
            T17.C5, T17.C4, T17.C3, T17.C1, T17.C2, T17.C6, T14.C2
        });

        for (Map.Entry<Location, PwmChannel[]> en : locationChannels.entrySet()) {
            for (PwmChannel c : en.getValue())
                c.location = en.getKey();
        }
    }
}
