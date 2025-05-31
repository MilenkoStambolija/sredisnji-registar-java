package hr.srsveze.sredisnji_registar.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OsobaTest {

    @Test
    void constructor_shouldInitializeFieldsCorrectly() {
        Osoba osoba = new Osoba("12345678901", "  Pero  ", "  Perić  ", "M", "01.01.1990", null);
        assertEquals("12345678901", osoba.getOib(), "OIB should be trimmed and set.");
        assertEquals("Pero", osoba.getIme(), "Ime should be trimmed.");
        assertEquals("Perić", osoba.getPrezime(), "Prezime should be trimmed.");
        assertEquals("M", osoba.getSpol().toUpperCase(), "Spol should be set and toUpperCase.");
        assertEquals("01.01.1990", osoba.getDatumRodjenja(), "Datum rođenja should be set.");
        assertEquals("", osoba.getDatumSmrti(), "Datum smrti should be empty string if null passed.");
    }

    @Test
    void constructor_shouldHandleNullValuesGracefully() {
        Osoba osoba = new Osoba(null, null, null, null, null, null);
        assertEquals("", osoba.getOib(), "OIB should be empty string if null.");
        assertEquals("", osoba.getIme(), "Ime should be empty string if null.");
        assertEquals("", osoba.getPrezime(), "Prezime should be empty string if null.");
        assertEquals("", osoba.getSpol(), "Spol should be empty string if null.");
        assertEquals("", osoba.getDatumRodjenja(), "Datum rođenja should be empty string if null.");
        assertEquals("", osoba.getDatumSmrti(), "Datum smrti should be empty string if null.");
    }

    @Test
    void setters_shouldTrimAndSetValues() {
        Osoba osoba = new Osoba();
        osoba.setOib("  09876543210  ");
        assertEquals("09876543210", osoba.getOib());
        osoba.setIme("  Ana  ");
        assertEquals("Ana", osoba.getIme());
        osoba.setPrezime("  Anić  ");
        assertEquals("Anić", osoba.getPrezime());
        osoba.setSpol("  ž  "); // Lowercase with spaces
        assertEquals("Ž", osoba.getSpol(), "Spol should be trimmed and converted to uppercase.");
        osoba.setDatumRodjenja("  02.02.2000  ");
        assertEquals("02.02.2000", osoba.getDatumRodjenja());
        osoba.setDatumSmrti(null);
        assertEquals("", osoba.getDatumSmrti());
    }

    @Test
    void getPunoIme_shouldReturnFullNameOrNull() {
        Osoba osoba1 = new Osoba("1", "Pero", "Perić", "M", "d1", null);
        assertEquals("Pero Perić", osoba1.getPunoIme(), "Should return 'Ime Prezime'.");

        Osoba osoba2 = new Osoba("2", "Pero", null, "M", "d1", null);
        assertEquals("Pero", osoba2.getPunoIme(), "Should return 'Ime' if prezime is null.");
        
        Osoba osoba3 = new Osoba("3", null, "Perić", "M", "d1", null);
        assertEquals("Perić", osoba3.getPunoIme(), "Should return 'Prezime' if ime is null.");

        Osoba osoba4 = new Osoba("4", null, null, "M", "d1", null);
        assertNull(osoba4.getPunoIme(), "Should return null if both ime and prezime are null.");
        
        Osoba osoba5 = new Osoba("5", "  ", "  ", "M", "d1", null); // Both effectively empty after trim
        assertNull(osoba5.getPunoIme(), "Should return null if both ime and prezime are effectively empty after trim.");
    }

    @Test
    void getPunoImeOrOib_shouldReturnCorrectValue() {
        Osoba osoba1 = new Osoba("123", "Pero", "Perić", "M", "d1", null);
        assertEquals("Pero Perić", osoba1.getPunoImeOrOib(), "Should return PunoIme if available.");

        Osoba osoba2 = new Osoba("456", null, null, "M", "d1", null);
        assertEquals("456", osoba2.getPunoImeOrOib(), "Should return OIB if PunoIme is not available.");

        Osoba osoba3 = new Osoba(null, null, null, "M", "d1", null);
        assertEquals("Nepoznato", osoba3.getPunoImeOrOib(), "Should return 'Nepoznato' if neither PunoIme nor OIB is available.");
        
        Osoba osoba4 = new Osoba("", "", "", "", "", ""); // All empty strings
        assertEquals("Nepoznato", osoba4.getPunoImeOrOib(), "Should return 'Nepoznato' if OIB is empty string and PunoIme is null.");
    }

    @Test
    void equals_shouldBeTrueForSameOib() {
        Osoba osoba1 = new Osoba("12345678901", "Pero", "Perić", "M", "01.01.1990", null);
        Osoba osoba2 = new Osoba("12345678901", "Ana", "Anić", "Ž", "02.02.2000", null);
        assertTrue(osoba1.equals(osoba2), "Osobe s istim OIB-om trebaju biti jednake.");
        assertEquals(osoba1.hashCode(), osoba2.hashCode(), "Hash kodovi za osobe s istim OIB-om trebaju biti jednaki.");
    }

    @Test
    void equals_shouldBeFalseForDifferentOib() {
        Osoba osoba1 = new Osoba("12345678901", "Pero", "Perić", "M", "01.01.1990", null);
        Osoba osoba2 = new Osoba("98765432109", "Pero", "Perić", "M", "01.01.1990", null);
        assertFalse(osoba1.equals(osoba2), "Osobe s različitim OIB-om ne smiju biti jednake.");
    }

    @Test
    void equals_shouldBeFalseForNullOrEmptyOib() {
        Osoba osoba1 = new Osoba(null, "Pero", "Perić", "M", "01.01.1990", null);
        Osoba osoba2 = new Osoba(null, "Ana", "Anić", "Ž", "02.02.2000", null);
        assertFalse(osoba1.equals(osoba2), "Osobe s null OIB-om ne smiju biti jednake (osim ako su isti objekt).");

        Osoba osoba3 = new Osoba("", "Pero", "Perić", "M", "01.01.1990", null);
        Osoba osoba4 = new Osoba("", "Ana", "Anić", "Ž", "02.02.2000", null);
        assertFalse(osoba3.equals(osoba4), "Osobe s praznim OIB-om ne smiju biti jednake (osim ako su isti objekt).");
        
        Osoba osoba5 = new Osoba("123", "P", "P", "M", "d", null);
        Osoba osoba6 = new Osoba(null, "A", "A", "Z", "d", null);
        assertFalse(osoba5.equals(osoba6));
        assertFalse(osoba6.equals(osoba5));
    }
    
    @Test
    void equals_shouldHandleSymmetryAndNulls() {
        Osoba osoba1 = new Osoba("123", "P", "P", "M", "d", null);
        Osoba osobaNullOib = new Osoba(null, "N", "N", "M", "d", null);
        Osoba osobaEmptyOib = new Osoba("", "E", "E", "M", "d", null);

        assertFalse(osoba1.equals(null), "Equals should return false for null argument.");
        assertFalse(osoba1.equals(new Object()), "Equals should return false for different type.");
        
        // Test cases where OIB is null or empty
        assertFalse(osobaNullOib.equals(osoba1));
        assertFalse(osobaEmptyOib.equals(osoba1));

        Osoba osobaNullOib2 = new Osoba(null, "N2", "N2", "M", "d", null);
        assertFalse(osobaNullOib.equals(osobaNullOib2), "Two different instances with null OIBs should not be equal.");

        Osoba osobaEmptyOib2 = new Osoba("", "E2", "E2", "M", "d", null);
        assertFalse(osobaEmptyOib.equals(osobaEmptyOib2), "Two different instances with empty OIBs should not be equal.");
    }


    @Test
    void hashCode_shouldBeConsistent() {
        Osoba osoba1 = new Osoba("12345678901", "Pero", "Perić", "M", "01.01.1990", null);
        int initialHashCode = osoba1.hashCode();
        osoba1.setIme("Novo Ime"); // Modifying a field not used in hashCode
        assertEquals(initialHashCode, osoba1.hashCode(), "HashCode bi trebao ostati isti ako se OIB ne mijenja.");
    }

    @Test
    void hashCode_forNullAndEmptyOib() {
        // As per current Osoba.java, OIB being null or empty results in Objects.hash(oib)
        // Objects.hash(null) is different from Objects.hash("").
        // The proposed refactoring in step 3 was to use a constant (e.g., 31) if OIB is null/empty.
        // Since that refactoring couldn't be applied, we test the *current* behavior.
        Osoba osobaNullOib = new Osoba(null, "Pero", "Perić", "M", "d1", null);
        Osoba osobaEmptyOib = new Osoba("", "Ana", "Anić", "Ž", "d2", null);
        Osoba osobaNullOib2 = new Osoba(null, "Ivo", "Ivić", "M", "d3", null);
        Osoba osobaEmptyOib2 = new Osoba("", "Mara", "Marić", "Ž", "d4", null);

        // According to current Osoba.java:
        // public int hashCode() {
        //   return Objects.hash(oib != null && !oib.isEmpty() ? oib : System.identityHashCode(this));
        // }
        // This means two distinct objects with null/empty OIBs will likely have different hashCodes.
        // This test reflects that current (un-refactored) behavior.
        assertNotEquals(osobaNullOib.hashCode(), osobaNullOib2.hashCode(), "Hash codes for different objects with null OIB should be different due to System.identityHashCode.");
        assertNotEquals(osobaEmptyOib.hashCode(), osobaEmptyOib2.hashCode(), "Hash codes for different objects with empty OIB should be different due to System.identityHashCode.");

        // If OIB is null vs empty, they are treated differently by `oib != null && !oib.isEmpty()`
        // If oib is null, condition is false. If oib is "", condition is false.
        // So both will use System.identityHashCode(this).
        assertNotEquals(osobaNullOib.hashCode(), osobaEmptyOib.hashCode(), "Hash codes for different objects, one with null OIB, other with empty OIB, should be different.");
    }
    
    @Test
    void toString_shouldReturnNonEmptyString() {
        Osoba osoba = new Osoba("123", "Test", "Testović", "M", "t", null);
        assertNotNull(osoba.toString());
        assertFalse(osoba.toString().isEmpty());
        assertTrue(osoba.toString().startsWith("Osoba{oib='123'"));
    }

    @Test
    void constructor_spolNormalization() {
        Osoba o1 = new Osoba("1", "I", "P", "m", "dr", null); // lowercase m
        assertEquals("M", o1.getSpol(), "Spol 'm' should be normalized to 'M'");

        Osoba o2 = new Osoba("2", "I", "P", "ž", "dr", null); // lowercase ž
        assertEquals("Ž", o2.getSpol(), "Spol 'ž' should be normalized to 'Ž'");

        Osoba o3 = new Osoba("3", "I", "P", "  M  ", "dr", null); // with spaces
        assertEquals("M", o3.getSpol(), "Spol '  M  ' should be trimmed and normalized to 'M'");
        
        Osoba o4 = new Osoba("4", "I", "P", "Nepoznato", "dr", null); 
        assertEquals("NEPOZNATO", o4.getSpol(), "Spol 'Nepoznato' should be uppercased.");
    }
}
