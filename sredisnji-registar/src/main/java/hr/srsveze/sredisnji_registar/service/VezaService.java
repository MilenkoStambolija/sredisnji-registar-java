package hr.srsveze.sredisnji_registar.service;

import hr.srsveze.sredisnji_registar.model.MaticaRodenihEntry;
import hr.srsveze.sredisnji_registar.model.Osoba;
import hr.srsveze.sredisnji_registar.model.VezaDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VezaServiceTest {

    @Mock
    private CsvDataLoader mockCsvDataLoader;

    private VezaService vezaService;

    // Helper method to get a specific relationship from the list
    private VezaDTO findVezaByRelatedOib(List<VezaDTO> veze, String relatedOib) {
        return veze.stream()
                .filter(v -> v.getOibPovezaneOsobe().equals(relatedOib))
                .findFirst()
                .orElse(null);
    }

    @BeforeEach
    void setUp() {
        // Initialize public collections on the mock to prevent NullPointerExceptions
        // as VezaService accesses them directly.
        mockCsvDataLoader.maticaRodenihEntries = Collections.synchronizedList(new ArrayList<>());
        mockCsvDataLoader.maticaVjencanihData = Collections.synchronizedList(new ArrayList<>());
        mockCsvDataLoader.maticaUmrlihData = Collections.synchronizedList(new ArrayList<>());
        mockCsvDataLoader.registarZivPartnerstvaData = Collections.synchronizedList(new ArrayList<>());
        mockCsvDataLoader.izjavaIzvanbrZajData = Collections.synchronizedList(new ArrayList<>());
        mockCsvDataLoader.stranciData = Collections.synchronizedList(new ArrayList<>());
        // Critical: centralniPopisOsoba must be a map that Mockito can interact with for .get()
        // and VezaService also iterates over its values.
        // For direct access from VezaService like csvDataLoader.centralniPopisOsoba.get(oib),
        // we need to use when(mockCsvDataLoader.centralniPopisOsoba.get(anyString())).thenReturn(...)
        // So, just initializing it here is fine.
        mockCsvDataLoader.centralniPopisOsoba = Collections.synchronizedMap(new HashMap<>());

        vezaService = new VezaService(mockCsvDataLoader);
    }

    @Test
    @DisplayName("pronadjiSveVeze should return 'OIB nije pronađen' if OIB does not exist")
    void testPronadjiSveVeze_OibNotFound() {
        String nonExistentOib = "99999999999";
        when(mockCsvDataLoader.centralniPopisOsoba.get(nonExistentOib)).thenReturn(null);

        Map<String, Object> result = vezaService.pronadjiSveVeze(nonExistentOib);

        assertNotNull(result);
        assertEquals(nonExistentOib, result.get("oib"));
        assertEquals("OIB nije pronađen.", result.get("error"));
        assertTrue(((List<?>) result.get("relationships")).isEmpty());
    }

    @Test
    @DisplayName("pronadjiSveVeze should find parents for a given OIB")
    void testPronadjiSveVeze_FindParents() {
        String targetOib = "001";
        String majkaOib = "101";
        String otacOib = "201";

        Osoba targetOsoba = new Osoba(targetOib, "Dijete", "Test", "M", "01.01.2000", null);
        Osoba majka = new Osoba(majkaOib, "Ana", "Anić", "Ž", "01.01.1975", null);
        Osoba otac = new Osoba(otacOib, "Ivan", "Ivić", "M", "01.01.1970", null);

        when(mockCsvDataLoader.centralniPopisOsoba.get(targetOib)).thenReturn(targetOsoba);
        when(mockCsvDataLoader.centralniPopisOsoba.get(majkaOib)).thenReturn(majka);
        when(mockCsvDataLoader.centralniPopisOsoba.get(otacOib)).thenReturn(otac);

        MaticaRodenihEntry rodenjeEntry = new MaticaRodenihEntry(
                targetOib, "Dijete", "Test", "M", "01.01.2000",
                majkaOib, "Ana", "Anić",
                otacOib, "Ivan", "Ivić"
        );
        mockCsvDataLoader.maticaRodenihEntries.add(rodenjeEntry);

        Map<String, Object> result = vezaService.pronadjiSveVeze(targetOib);
        List<VezaDTO> veze = (List<VezaDTO>) result.get("relationships");

        assertNotNull(veze);
        assertEquals(2, veze.size());
        assertTrue(veze.stream().anyMatch(v -> v.getOibPovezaneOsobe().equals(majkaOib) && v.getVrstaVezePrikaz().toLowerCase().contains("majka")));
        assertTrue(veze.stream().anyMatch(v -> v.getOibPovezaneOsobe().equals(otacOib) && v.getVrstaVezePrikaz().toLowerCase().contains("otac")));
    }

    @Test
    @DisplayName("pronadjiSveVeze should find children for a given OIB")
    void testPronadjiSveVeze_FindChildren() {
        String targetOib = "101"; // Majka
        String dijete1Oib = "001";
        String dijete2Oib = "002";

        Osoba targetOsoba = new Osoba(targetOib, "Ana", "Anić", "Ž", "01.01.1975", null);
        Osoba dijete1 = new Osoba(dijete1Oib, "Pero", "Perić", "M", "01.01.2000", null);
        Osoba dijete2 = new Osoba(dijete2Oib, "Mara", "Marić", "Ž", "02.02.2002", null);

        when(mockCsvDataLoader.centralniPopisOsoba.get(targetOib)).thenReturn(targetOsoba);
        when(mockCsvDataLoader.centralniPopisOsoba.get(dijete1Oib)).thenReturn(dijete1);
        when(mockCsvDataLoader.centralniPopisOsoba.get(dijete2Oib)).thenReturn(dijete2);

        mockCsvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(dijete1Oib, "Pero", "Perić", "M", "01.01.2000", targetOib, "Ana", "Anić", "999", "NN", "NN"));
        mockCsvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(dijete2Oib, "Mara", "Marić", "Ž", "02.02.2002", targetOib, "Ana", "Anić", "998", "NN", "NN"));

        Map<String, Object> result = vezaService.pronadjiSveVeze(targetOib);
        List<VezaDTO> veze = (List<VezaDTO>) result.get("relationships");

        assertNotNull(veze);
        assertEquals(2, veze.size());
        assertTrue(veze.stream().anyMatch(v -> v.getOibPovezaneOsobe().equals(dijete1Oib) && v.getVrstaVezePrikaz().toLowerCase().contains("sin")));
        assertTrue(veze.stream().anyMatch(v -> v.getOibPovezaneOsobe().equals(dijete2Oib) && v.getVrstaVezePrikaz().toLowerCase().contains("kći")));
    }

    @Test
    @DisplayName("pronadjiSveVeze should find spouse from MaticaVjencanih")
    void testPronadjiSveVeze_FindSpouse() {
        String targetOib = "301"; // Muz
        String zenaOib = "401";

        Osoba targetOsoba = new Osoba(targetOib, "Marko", "Markić", "M", "03.03.1980", null);
        Osoba zena = new Osoba(zenaOib, "Jana", "Janić", "Ž", "04.04.1982", null);

        when(mockCsvDataLoader.centralniPopisOsoba.get(targetOib)).thenReturn(targetOsoba);
        when(mockCsvDataLoader.centralniPopisOsoba.get(zenaOib)).thenReturn(zena);

        Map<String, String> brakRow = new HashMap<>();
        brakRow.put("OIB muža", targetOib);
        brakRow.put("OIB žene", zenaOib);
        brakRow.put("Datum prestanka braka", ""); // Active marriage
        mockCsvDataLoader.maticaVjencanihData.add(brakRow);

        Map<String, Object> result = vezaService.pronadjiSveVeze(targetOib);
        List<VezaDTO> veze = (List<VezaDTO>) result.get("relationships");

        assertNotNull(veze);
        assertEquals(1, veze.size());
        VezaDTO vezaSaZenom = findVezaByRelatedOib(veze, zenaOib);
        assertNotNull(vezaSaZenom);
        assertTrue(vezaSaZenom.getVrstaVezePrikaz().toLowerCase().contains("bračni drug"));
        assertEquals("Izravna", vezaSaZenom.getTipVeze());
        assertEquals("Partnerska", vezaSaZenom.getLinija());
    }
    
    @Test
    @DisplayName("pronadjiSveVeze should find full siblings")
    void testPronadjiSveVeze_FindFullSiblings() {
        String targetOib = "001"; // Target dijete
        String bratOib = "002";
        String sestraOib = "003";
        String majkaOib = "101";
        String otacOib = "201";

        Osoba target = new Osoba(targetOib, "Pero", "Perić", "M", "2000", null);
        Osoba brat = new Osoba(bratOib, "Ivan", "Perić", "M", "2002", null);
        Osoba sestra = new Osoba(sestraOib, "Ana", "Perić", "Ž", "2004", null);
        Osoba majka = new Osoba(majkaOib, "Mama", "Mamić", "Ž", "1970", null);
        Osoba otac = new Osoba(otacOib, "Tata", "Tatić", "M", "1970", null);

        when(mockCsvDataLoader.centralniPopisOsoba.get(targetOib)).thenReturn(target);
        when(mockCsvDataLoader.centralniPopisOsoba.get(bratOib)).thenReturn(brat);
        when(mockCsvDataLoader.centralniPopisOsoba.get(sestraOib)).thenReturn(sestra);
        when(mockCsvDataLoader.centralniPopisOsoba.get(majkaOib)).thenReturn(majka);
        when(mockCsvDataLoader.centralniPopisOsoba.get(otacOib)).thenReturn(otac);
        
        // Target's birth entry (to find parents)
        mockCsvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(targetOib, "Pero", "Perić", "M", "2000", majkaOib, "Mama", "Mamić", otacOib, "Tata", "Tatić"));
        // Brother's birth entry (same parents)
        mockCsvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(bratOib, "Ivan", "Perić", "M", "2002", majkaOib, "Mama", "Mamić", otacOib, "Tata", "Tatić"));
        // Sister's birth entry (same parents)
        mockCsvDataLoader.maticaRodenihEntries.add(new MaticaRodenihEntry(sestraOib, "Ana", "Perić", "Ž", "2004", majkaOib, "Mama", "Mamić", otacOib, "Tata", "Tatić"));

        Map<String, Object> result = vezaService.pronadjiSveVeze(targetOib);
        List<VezaDTO> veze = (List<VezaDTO>) result.get("relationships");
        
        // Expected: 2 parents + 2 siblings = 4 relationships
        assertNotNull(veze);
        // Filter out parents to check siblings
        List<VezaDTO> siblingVeza = veze.stream()
                                        .filter(v -> !v.getOibPovezaneOsobe().equals(majkaOib) && !v.getOibPovezaneOsobe().equals(otacOib))
                                        .collect(Collectors.toList());
        
        assertEquals(2, siblingVeza.size(), "Should find 2 sibling relationships.");

        VezaDTO vezaSBratom = findVezaByRelatedOib(siblingVeza, bratOib);
        assertNotNull(vezaSBratom, "Veza s bratom mora postojati.");
        assertTrue(vezaSBratom.getVrstaVezePrikaz().toLowerCase().contains("brat / brat"));
        assertTrue(vezaSBratom.getOpis().toLowerCase().contains("puna braća/sestre"));
        assertEquals("Pobočna", vezaSBratom.getLinija());
        assertEquals("2", vezaSBratom.getStupanj());

        VezaDTO vezaSaSestrom = findVezaByRelatedOib(siblingVeza, sestraOib);
        assertNotNull(vezaSaSestrom, "Veza sa sestrom mora postojati.");
        assertTrue(vezaSaSestrom.getVrstaVezePrikaz().toLowerCase().contains("brat / sestra"));
        assertTrue(vezaSaSestrom.getOpis().toLowerCase().contains("puna braća/sestre"));
    }
    
    // TODO: Add more tests:
    // - Half-siblings (po majci, po ocu)
    // - Grandparents, Grandchildren
    // - Uncles/Aunts, Nephews/Nieces (considering contextSpol for terms like stric/ujak/tetka)
    // - Cousins
    // - Life partners, Cohabiting partners
    // - Scenarios with no relationships found for a valid OIB
    // - Scenarios involving deceased individuals if it affects relationship descriptions/types
}
