package hr.srsveze.sredisnji_registar.service;

import hr.srsveze.sredisnji_registar.model.MaticaRodenihEntry;
import hr.srsveze.sredisnji_registar.model.Osoba;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvDataLoaderTest {

    private CsvDataLoader csvDataLoader;

    @BeforeEach
    void setUp() {
        // Instantiate CsvDataLoader. This will trigger @PostConstruct and initial loadAllData().
        csvDataLoader = new CsvDataLoader();

        // Clear all lists that might have been populated by the initial loadAllData()
        // or will be used as input for our controlled tests.
        csvDataLoader.maticaRodenihEntries.clear();
        csvDataLoader.maticaVjencanihData.clear();
        csvDataLoader.maticaUmrlihData.clear();
        csvDataLoader.registarZivPartnerstvaData.clear();
        csvDataLoader.izjavaIzvanbrZajData.clear();
        csvDataLoader.stranciData.clear();
        // Crucially, clear the map that kreirajCentralniPopisOsoba populates.
        csvDataLoader.centralniPopisOsoba.clear();
    }

    @Test
    @DisplayName("Should populate centralniPopisOsoba from MaticaRodenih entries")
    void testKreirajCentralniPopis_FromMaticaRodenih() {
        // 1. Setup: Provide controlled data for maticaRodenihEntries
        csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "00000000001", "Dijete Jedan", "Dijetić", "M", "01.01.2020",
                "10000000001", "Majka Jedna", "Majić", 
                "20000000001", "Otac Jedan", "Ocić"
        ));
        csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "00000000002", "Dijete Dva", "Drugić", "Ž", "02.02.2021",
                "10000000001", "Majka Jedna", "Majić", // Same mother
                "20000000002", "Otac Dva", "Dvić"
        ));
        // Add an entry with null OIBs for parents (should still process child)
        csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "00000000003", "Dijete Tri", "Tretić", "M", "03.03.2022",
                null, null, null, 
                null, null, null 
        ));
        // Add an entry where child OIB is null (should be skipped by dodajOsobuUPopis)
         csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                null, "Dijete Četiri", "Četvrtić", "Ž", "04.04.2023",
                "10000000003", "Majka Tri", "Trstić", 
                "20000000003", "Otac Tri", "Trstić"
        ));


        // 2. Action: Call loadAllData() which will call the private kreirajCentralniPopisOsoba()
        // This method will use the lists we manually populated above.
        // We accept that loadAllData() might print warnings about missing CSV files
        // as it will try to load them first, but that part is ignored for this test's assertions.
        csvDataLoader.loadAllData(); // This re-runs the logic including kreirajCentralniPopisOsoba

        // 3. Assertions
        Map<String, Osoba> popis = csvDataLoader.centralniPopisOsoba;
        // Corrected expected count:
        // Child1 (001), Majka1 (101), Otac1 (201)
        // Child2 (002), (Majka1 already there), Otac2 (202)
        // Child3 (003), (Majka null, Otac null - not added)
        // (Child4 null - not added), Majka3 (103), Otac3 (203)
        // Total: 3 children + 2 parents from child1 + 1 parent from child2 + 2 parents from child4 = 8
        assertEquals(8, popis.size(), "Should contain 3 children and 5 parents.");


        Osoba dijete1 = popis.get("00000000001");
        assertNotNull(dijete1);
        assertEquals("Dijete Jedan", dijete1.getIme());
        assertEquals("Dijetić", dijete1.getPrezime());
        assertEquals("M", dijete1.getSpol());
        assertEquals("01.01.2020", dijete1.getDatumRodjenja());

        Osoba majka1 = popis.get("10000000001");
        assertNotNull(majka1);
        assertEquals("Majka Jedna", majka1.getIme());
        assertEquals("Majić", majka1.getPrezime());
        assertEquals("Ž", majka1.getSpol(), "Spol for mother should be Ž");

        Osoba otac1 = popis.get("20000000001");
        assertNotNull(otac1);
        assertEquals("Otac Jedan", otac1.getIme());
        assertEquals("Ocić", otac1.getPrezime());
        assertEquals("M", otac1.getSpol(), "Spol for father should be M");
        
        Osoba dijete3 = popis.get("00000000003");
        assertNotNull(dijete3, "Dijete 3 should be added even with null parents.");
        assertEquals("Dijete Tri", dijete3.getIme());

        assertNull(popis.get(null), "Should not add person with null OIB (child4).");
        assertNull(popis.get(""), "Should not add person with empty OIB.");
        
        Osoba majka3 = popis.get("10000000003");
        assertNotNull(majka3, "Majka Tri (from child 4) should be added.");
        assertEquals("Majka Tri", majka3.getIme());

        Osoba otac3 = popis.get("20000000003");
        assertNotNull(otac3, "Otac Tri (from child 4) should be added.");
        assertEquals("Otac Tri", otac3.getIme());
    }

    @Test
    @DisplayName("Should populate centralniPopisOsoba from MaticaVjencanih")
    void testKreirajCentralniPopis_FromMaticaVjencanih() {
        // 1. Setup
        Map<String, String> brak1 = new HashMap<>();
        brak1.put("OIB muža", "30000000001");
        brak1.put("Ime muža", "Muz Jedan");
        brak1.put("Prezime muža", "Muzić");
        brak1.put("Datum rođenja muža", "01.01.1980");
        brak1.put("OIB žene", "40000000001");
        brak1.put("Ime žene", "Zena Jedna");
        brak1.put("Prezime žene", "Zenić");
        brak1.put("Datum rođenja žene", "02.02.1982");
        csvDataLoader.maticaVjencanihData.add(brak1);

        // 2. Action
        csvDataLoader.loadAllData();

        // 3. Assertions
        Map<String, Osoba> popis = csvDataLoader.centralniPopisOsoba;
        assertEquals(2, popis.size(), "Should contain 2 persons from MaticaVjencanih.");

        Osoba muz1 = popis.get("30000000001");
        assertNotNull(muz1);
        assertEquals("Muz Jedan", muz1.getIme());
        assertEquals("Muzić", muz1.getPrezime());
        assertEquals("M", muz1.getSpol());
        assertEquals("01.01.1980", muz1.getDatumRodjenja());

        Osoba zena1 = popis.get("40000000001");
        assertNotNull(zena1);
        assertEquals("Zena Jedna", zena1.getIme());
        assertEquals("Zenić", zena1.getPrezime());
        assertEquals("Ž", zena1.getSpol());
        assertEquals("02.02.1982", zena1.getDatumRodjenja());
    }
    
    @Test
    @DisplayName("Should update datumSmrti from MaticaUmrlih")
    void testKreirajCentralniPopis_UpdateDatumSmrti() {
        // 1. Setup: Add an initial person via MaticaRodenih
        csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "50000000001", "Osoba Umrla", "Pokojnić", "M", "01.01.1950",
                null, null, null, null, null, null
        ));
        
        // Add death record
        Map<String, String> umrliZapis = new HashMap<>();
        umrliZapis.put("OIB umrlog", "50000000001");
        umrliZapis.put("Datum smrti", "31.12.2020");
        csvDataLoader.maticaUmrlihData.add(umrliZapis);

        // 2. Action
        csvDataLoader.loadAllData();

        // 3. Assertions
        Map<String, Osoba> popis = csvDataLoader.centralniPopisOsoba;
        assertEquals(1, popis.size());
        Osoba umrlaOsoba = popis.get("50000000001");
        assertNotNull(umrlaOsoba);
        assertEquals("Osoba Umrla", umrlaOsoba.getIme());
        assertEquals("31.12.2020", umrlaOsoba.getDatumSmrti(), "Datum smrti should be updated.");
    }

    @Test
    @DisplayName("dodajOsobuUPopis - should update existing person with new details")
    void testDodajOsobuUPopis_UpdatesExisting() {
        // 1. Setup: Add an initial minimal person
        csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "60000000001", "Prvo Ime", "", "M", "", // OIB, Ime, Prazno Prezime, Spol, Prazan DatumRodjenja
                null, null, null, null, null, null
        ));
        csvDataLoader.loadAllData(); // Initial load to populate from maticaRodenihEntries

        // Check initial state
        Osoba osoba = csvDataLoader.centralniPopisOsoba.get("60000000001");
        assertNotNull(osoba);
        assertEquals("Prvo Ime", osoba.getIme());
        assertEquals("", osoba.getPrezime());
        assertEquals("", osoba.getDatumRodjenja());

        // Clear maticaRodenihEntries and add new data for the same OIB from a different source (e.g. MaticaVjencanih)
        csvDataLoader.maticaRodenihEntries.clear(); // Important to not re-add the first entry
        csvDataLoader.centralniPopisOsoba.clear(); // Clear previous map to rebuild it

        Map<String, String> newData = new HashMap<>();
        newData.put("OIB muža", "60000000001"); // Same OIB
        newData.put("Ime muža", "Novo Ime"); 
        newData.put("Prezime muža", "Novo Prezime"); 
        newData.put("Datum rođenja muža", "01.01.1999"); 
        csvDataLoader.maticaVjencanihData.add(newData);
        
        // Add the original entry again to simulate it coming from MaticaRodenih
        // This is to ensure the "Prvo Ime" is established in the map first if it were processed first.
        // However, to test the update logic properly, we should have the initial state set,
        // then process new data. So, let's put the original data back to ensure it's "existing".
         csvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(
                "60000000001", "Prvo Ime", "", "M", "", 
                null, null, null, null, null, null
        ));


        // 2. Action
        csvDataLoader.loadAllData(); // This will process maticaRodenih (again) then maticaVjencanih

        // 3. Assertions
        Osoba updatedOsoba = csvDataLoader.centralniPopisOsoba.get("60000000001");
        assertNotNull(updatedOsoba);
        
        // Current CsvDataLoader.dodajOsobuUPopis logic:
        // Updates field if (existing is null or empty) AND (new is not null and not empty)
        // For Spol, it's more complex but generally prefers valid new "M" or "Ž" over invalid/empty existing.
        assertEquals("Prvo Ime", updatedOsoba.getIme(), 
            "Ime should be 'Prvo Ime' because MaticaRodenih is processed first, and existing non-empty fields are not overwritten by later non-empty fields from MaticaVjencanih.");
        assertEquals("Novo Prezime", updatedOsoba.getPrezime(), 
            "Prezime should be 'Novo Prezime' because it was initially empty and MaticaVjencanih provided a value.");
        assertEquals("M", updatedOsoba.getSpol());
        assertEquals("01.01.1999", updatedOsoba.getDatumRodjenja(), 
            "Datum rođenja should be '01.01.1999' as it was initially empty and MaticaVjencanih provided a value.");
    }
}
