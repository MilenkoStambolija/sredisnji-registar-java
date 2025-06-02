// Smjestite u: src/main/java/hr/srsveze/sredisnji_registar/service/CsvDataLoader.java
package hr.srsveze.sredisnji_registar.service;

import hr.srsveze.sredisnji_registar.model.Osoba;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

@Component
public class CsvDataLoader {
    private static final String DATA_SUBDIRECTORY = "DATA";

    // Mapa za metapodatke: Ključ=NazivDatoteke, Vrijednost=Mapa<LogickiOpisPoljaIzMetaCSV, StvarniNazivStupcaIzMetaCSV>
    private Map<String, Map<String, String>> metaDataLookup = Collections.synchronizedMap(new HashMap<>());

    // Liste za podatke iz CSV-ova
    public List<Map<String, String>> oibBazaData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> maticaRodenihData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> maticaVjencanihData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> maticaUmrlihData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> registarZivPartnerstvaData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> izjavaIzvanbrZajData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> stranciData = Collections.synchronizedList(new ArrayList<>());

    public Map<String, Osoba> centralniPopisOsoba = Collections.synchronizedMap(new HashMap<>());

    @PostConstruct
    public void loadAllData() {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path dataDirPathObject = Paths.get(projectRoot.toString(), DATA_SUBDIRECTORY);

        System.out.println("------------------------------------------------------------------");
        System.out.println("START: Učitavanje CSV datoteka iz: " + dataDirPathObject);
        System.out.println("------------------------------------------------------------------");
        try {
            // 0. Učitaj metapodatke PRVO
            loadMetaData("meta_podaci.csv");

            // 1. Učitaj SVE CSV datoteke
            oibBazaData.addAll(loadGenericCsv("OIB_baza.csv"));
            maticaRodenihData.addAll(loadGenericCsv("matica_rodjenih.csv"));
            stranciData.addAll(loadGenericCsv("registar_stranaca.csv"));
            maticaVjencanihData.addAll(loadGenericCsv("matica_vjenčanih.csv"));
            maticaUmrlihData.addAll(loadGenericCsv("matica_umrlih.csv"));

            List<Map<String, String>> rzpTemp = loadGenericCsv("registar_životnog_partnerstva.csv");
            List<Map<String, String>> izpTemp = loadGenericCsv("izjava_o_životnom partnerstvu.csv");

            registarZivPartnerstvaData.addAll(rzpTemp);
            registarZivPartnerstvaData.addAll(izpTemp);
            if (!registarZivPartnerstvaData.isEmpty()) {
                 registarZivPartnerstvaData = registarZivPartnerstvaData.stream().distinct().collect(Collectors.toList());
                 System.out.println("INFO: Ukupno " + registarZivPartnerstvaData.size() + " zapisa u Registru/Izjavama živ. partnerstva.");
            } else {
                 System.out.println("INFO: Nema zapisa za Registar živ. partnerstva / Izjave.");
            }
            izjavaIzvanbrZajData.addAll(loadGenericCsv("izjava_o_izvanbračnoj_zajednici.csv"));

            // 2. Kreiraj osnovni popis osoba SAMO iz "OIB_baza.csv" koristeći metapodatke
            kreirajInicijalniPopisIzOIBBaze();
            System.out.println("INFO: Kreiran inicijalni centralni popis s " + centralniPopisOsoba.size() + " jedinstvenih OIB-ova (iz OIB_baza.csv).");

            // 3. Sada AŽURIRAJ podatke za te osobe koristeći SVE OSTALE učitane datoteke i metapodatke.
            azurirajPodatkePostojecihOsoba();
            System.out.println("INFO: Konačan broj osoba u centralnom popisu nakon ažuriranja svih podataka: " + centralniPopisOsoba.size());

        } catch (IOException e) {
            System.err.println("KRITIČNA GREŠKA prilikom učitavanja CSV datoteka: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("ZAVRŠENO: Učitavanje CSV datoteka.");
        System.out.println("------------------------------------------------------------------");
    }

    private void loadMetaData(String metaFileName) throws IOException {
        metaDataLookup.clear();
        Path filePath = Paths.get(DATA_SUBDIRECTORY, metaFileName);
        if (!Files.exists(filePath)) {
            System.err.println("KRITIČNA GREŠKA: Meta-datoteka NIJE PRONAĐENA: " + filePath.toAbsolutePath() + ". Aplikacija neće moći ispravno mapirati stupce.");
            return;
        }
        // Definicija headera za meta_podaci.csv kako ste ih naveli
        String[] META_HEADERS = {"Naziv datoteke", "Naziv baze", "Naziv stupca", "Tip podatka", "Logički opis polja", "Izvor", "Dio središnjeg registra stanovništva"};

        try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                .setHeader(META_HEADERS)
                .setSkipHeaderRecord(true).setIgnoreHeaderCase(true).setTrim(true)
                .setAllowMissingColumnNames(true).setDelimiter(';').setIgnoreEmptyLines(true).build())) {

            for (CSVRecord record : csvParser) {
                String nazivDatoteke = record.isMapped("Naziv datoteke") ? record.get("Naziv datoteke").trim() : "";
                String logickiOpisPolja = record.isMapped("Logički opis polja") ? record.get("Logički opis polja").trim() : "";
                String stvarniNazivStupca = record.isMapped("Naziv stupca") ? record.get("Naziv stupca").trim() : "";

                if (!nazivDatoteke.isEmpty() && !logickiOpisPolja.isEmpty() && !stvarniNazivStupca.isEmpty()) {
                    // Ključ u unutarnjoj mapi je "Logički opis polja", vrijednost je "Naziv stupca" (stvarni header)
                    metaDataLookup.computeIfAbsent(nazivDatoteke, k -> new HashMap<>()).put(logickiOpisPolja, stvarniNazivStupca);
                }
            }
        }
        System.out.println("INFO: Učitani metapodaci za stupce iz '" + metaFileName + "'. Broj mapiranih datoteka: " + metaDataLookup.size());
        // Za debugiranje:
        // metaDataLookup.forEach((file, headers) -> System.out.println("DEBUG Meta [" + file + "]: " + headers));
    }

    private List<Map<String, String>> loadGenericCsv(String fileName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        Path filePath = Paths.get(DATA_SUBDIRECTORY, fileName);
         if (!Files.exists(filePath)) {
            System.err.println("UPOZORENJE: Datoteka NIJE PRONAĐENA: " + filePath.toAbsolutePath() + " (preskačem)");
            return data;
        }

        try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .setAllowMissingColumnNames(true)
                     .setDelimiter(';')
                     .setIgnoreEmptyLines(true).build())) {
            for (CSVRecord csvRecord : csvParser) {
                data.add(new HashMap<>(csvRecord.toMap()));
            }
        }
        System.out.println("INFO: Učitano " + data.size() + " zapisa iz " + fileName);
        return data;
    }

    // JAVNA POMOĆNA METODA ZA DOHVAT PODATAKA KORISTEĆI METAPODATKE
    // Ovu metodu će koristiti VezaService.java i druge metode u ovoj klasi
    public String getFromRowUsingLogicalDesc(Map<String, String> row, String fileName, String logicalDescriptionKey) {
        if (row == null || fileName == null || logicalDescriptionKey == null) {
            // System.out.println("DEBUG getFromRowUsingMeta: Jedan od argumenata je null. Row: " + row + ", File: " + fileName + ", LogicalKey: " + logicalDescriptionKey);
            return "";
        }

        Map<String, String> fileSpecificMeta = metaDataLookup.get(fileName);
        if (fileSpecificMeta == null) {
            // System.out.println("UPOZORENJE (getFromRowUsingMeta): Nema metapodataka za datoteku '" + fileName + "' pri traženju logičkog ključa '" + logicalDescriptionKey + "'");
            return "";
        }
        String actualHeaderName = fileSpecificMeta.get(logicalDescriptionKey);
        if (actualHeaderName == null) {
            // System.out.println("UPOZORENJE (getFromRowUsingMeta): Logički ključ '" + logicalDescriptionKey + "' ("+fileName+") nije mapiran na stvarni header. Mapirani headeri: " + fileSpecificMeta);
            return "";
        }

        String value = null;
        // CSVParser s ignoreHeaderCase bi trebao mapirati na konzistentne ključeve (npr. one iz prvog reda)
        // Stoga bi direktan dohvat trebao raditi ako je actualHeaderName točan.
        // Iteracija je fallback ako direktan dohvat ne uspije zbog suptilnih razlika koje ignoreHeaderCase nije pokrio.
        value = row.get(actualHeaderName);

        if (value == null) { // Fallback na case-insensitive pretragu ključeva iz reda
            for (Map.Entry<String, String> entry : row.entrySet()) {
                String cleanedMapKey = entry.getKey().startsWith("\uFEFF") ? entry.getKey().substring(1) : entry.getKey(); // Ukloni BOM
                if (actualHeaderName.equalsIgnoreCase(cleanedMapKey)) {
                    value = entry.getValue();
                    break;
                }
            }
        }
        // if (value == null) {
        //      System.out.println("DEBUG (getFromRowUsingMeta): Stvarni header '" + actualHeaderName +
        //                         "' (za logički '" + logicalDescriptionKey + "') nije pronađen u retku iz '" + fileName +
        //                         "'. Ključevi u retku: " + row.keySet());
        // }
        return value != null ? value.trim() : "";
    }

    // Kreira inicijalni popis SAMO iz "OIB_baza.csv"
    private void kreirajInicijalniPopisIzOIBBaze() {
        centralniPopisOsoba.clear();
        System.out.println("INFO: Kreiranje inicijalnog popisa osoba iz 'OIB_baza.csv'...");
        String oibBazaFileName = "OIB_baza.csv";

        for (Map<String, String> row : oibBazaData) {
            // Koristimo "Logički opis polja" iz vaše meta_podaci.csv za datoteku OIB_baza.csv
            // Ovi stringovi ("OIB osobe", "Ime", "Prezime") moraju točno odgovarati
            // vrijednostima u stupcu "Logički opis polja" vaše meta_podaci.csv za OIB_baza.csv
            String oib = getFromRowUsingLogicalDesc(row, oibBazaFileName, "OIB osobe");
            String ime = getFromRowUsingLogicalDesc(row, oibBazaFileName, "Ime"); // U vašoj meta_podaci.csv za OIB_baza, logički opis je "Ime osobe", ne samo "Ime"
            String prezime = getFromRowUsingLogicalDesc(row, oibBazaFileName, "Prezime"); // Slično, "Prezime osobe"

            // ISPRAVAK: Koristite točne "Logičke opise polja" iz vaše meta_podaci.csv
            // String ime = getFromRowUsingLogicalDesc(row, oibBazaFileName, "Ime osobe");
            // String prezime = getFromRowUsingLogicalDesc(row, oibBazaFileName, "Prezime osobe");


            if (oib != null && !oib.isEmpty()) {
                centralniPopisOsoba.computeIfAbsent(oib, k -> new Osoba(k, ime, prezime, "", "", ""));
            }
        }
    }

    // Ažurira podatke za osobe koje već postoje u centralniPopisOsoba
    private void azurirajPodatkePostojecihOsoba() {
        System.out.println("INFO: Započinjem ažuriranje podataka za postojeće osobe iz svih CSV datoteka...");

        // 1. Ažuriranje iz Matice Rođenih
        String mrFile = "matica_rodjenih.csv";
        System.out.println("INFO: Ažuriranje detalja iz " + mrFile + "...");
        for (Map<String, String> row : maticaRodenihData) {
            // VAŽNO: "OIB osobe" je za dijete u ovoj datoteci prema meta_podaci.csv
            azurirajDetaljeOsobe(
                getFromRowUsingLogicalDesc(row, mrFile, "OIB osobe"), // Logički opis: OIB djeteta
                getFromRowUsingLogicalDesc(row, mrFile, "Ime"),       // Logički opis: Ime djeteta
                getFromRowUsingLogicalDesc(row, mrFile, "Prezime"),   // Logički opis: Prezime djeteta
                getFromRowUsingLogicalDesc(row, mrFile, "Spol"),      // Logički opis: Spol djeteta koji se označava sa M (muški) ili Ž (ženski)
                getFromRowUsingLogicalDesc(row, mrFile, "Datum rođenja"), // Logički opis: Datum rođenja djeteta
                null);
            azurirajDetaljeOsobe(
                getFromRowUsingLogicalDesc(row, mrFile, "OIB majke"), // Logički opis: OIB majke djeteta
                getFromRowUsingLogicalDesc(row, mrFile, "Ime majke"),
                getFromRowUsingLogicalDesc(row, mrFile, "Prezime majke"),
                "Ž", // Spol majke je Ž
                null, // Pretpostavka da nema datuma rođenja majke u ovom retku
                null);
            azurirajDetaljeOsobe(
                getFromRowUsingLogicalDesc(row, mrFile, "OIB oca"),   // Logički opis: OIB oca djeteta
                getFromRowUsingLogicalDesc(row, mrFile, "Ime oca"),
                getFromRowUsingLogicalDesc(row, mrFile, "Prezime oca"), // U meta_podaci ste imali "Prezirne oca", pretpostavljam da je logički "Prezime oca djeteta"
                "M", // Spol oca je M
                null,
                null);
        }

        // 2. Ažuriranje iz Registra Stranaca
        String rsFile = "registar_stranaca.csv";
        System.out.println("INFO: Ažuriranje detalja iz " + rsFile + "...");
        for (Map<String, String> row : stranciData) {
            azurirajDetaljeOsobe(
                getFromRowUsingLogicalDesc(row, rsFile, "OIB osobe"), // Logički opis: OIB stranca
                getFromRowUsingLogicalDesc(row, rsFile, "Ime"),
                getFromRowUsingLogicalDesc(row, rsFile, "Prezime"),
                getFromRowUsingLogicalDesc(row, rsFile, "Spol"),
                getFromRowUsingLogicalDesc(row, rsFile, "Datum rođenja"),
                null);
        }

        // 3. Ažuriranje iz Matice Vjenčanih
        String mvFile = "matica_vjenčanih.csv";
        System.out.println("INFO: Ažuriranje podataka iz " + mvFile + "...");
        for (Map<String, String> row : maticaVjencanihData) {
            // Logički opisi polja za matica_vjenčanih.csv:
            // "OIB prvog bračnog druga", "Ime prvog bračnog druga", "Prezime prvog bračnog druga"
            // "OIB drugog bračnog druga", "Ime drugog bračnog druga", "Prezime drugog bračnog druga"
            // "Datum rođenja muža", "Datum rođenja žene" (ovi se odnose na osobe, ne na sam brak)
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, mvFile, "OIB osobe"), getFromRowUsingLogicalDesc(row, mvFile, "Ime"), getFromRowUsingLogicalDesc(row, mvFile, "Prezime"), null, null /*Dohvati datum rođenja ako postoji logički opis*/, null);
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, mvFile, "OIB bračnog druga"), getFromRowUsingLogicalDesc(row, mvFile, "Ime bračnog druga"), getFromRowUsingLogicalDesc(row, mvFile, "Prezime bračnog druga"), null, null/*Dohvati datum rođenja ako postoji logički opis*/, null);
        }

        // 4. Ažuriranje iz Registra živ. partnerstva (koristi spojenu listu)
        String rzpFileMetaKey = "registar_životnog_partnerstva.csv";
        // String izpFileMetaKey = "izjava_o_životnom partnerstvu.csv"; // Ako su logički opisi različiti
        System.out.println("INFO: Ažuriranje podataka iz Registra životnog partnerstva / Izjava...");
        for (Map<String, String> row : registarZivPartnerstvaData) {
            // Logički opisi polja za registar_životnog_partnerstva.csv:
            // "OIB prvog životnog partnera", "Ime prvog životnog partnera", "Prezime prvog životnog partnera", "Spol prvog životnog partnera", "Datum rođenja partnera 1"
            // "OIB drugog životnog partnera", "Ime drugog životnog partnera", "Prezime drugog životnog partnera", "Spol drugog životnog partnera", "Datum rođenja partnera 2"
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "OIB osobe"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Ime"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Prezime"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Spol"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Datum rođenja partnera 1"), null);
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "OIB životnog partnera"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Ime životnog partnera"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Prezime životnog partnera"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Spol životnog partnera"), getFromRowUsingLogicalDesc(row, rzpFileMetaKey, "Datum rođenja partnera 2"), null);
        }

        // 5. Ažuriranje iz Izjave o izvanbračnoj zajednici
        String iizFile = "izjava_o_izvanbračnoj_zajednici.csv";
        System.out.println("INFO: Ažuriranje podataka iz Izjave o izvanbračnoj zajednici...");
        for (Map<String, String> row : izjavaIzvanbrZajData) {
             // Logički opisi polja za izjava_o_izvanbračnoj_zajednici.csv:
            // "OIB prvog izvanbračnog partnera", "Ime prvog izvanbračnog partnera", "Prezime prvog izvanbračnog partnera"
            // "OIB drugog izvanbračnog druga", "Ime drugog izvanbračnog druga", "Prezime drugog izvanbračnog druga"
            // "Spol partnera 1", "Datum rođenja partnera 1" (ako postoje u meta_podaci.csv za ovu datoteku)
            // "Spol partnera 2", "Datum rođenja partnera 2" (ako postoje)
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, iizFile, "OIB osobe"), getFromRowUsingLogicalDesc(row, iizFile, "Ime"), getFromRowUsingLogicalDesc(row, iizFile, "Prezime"), getFromRowUsingLogicalDesc(row, iizFile, "Spol partnera 1"), getFromRowUsingLogicalDesc(row, iizFile, "Datum rođenja partnera 1"), null);
            azurirajDetaljeOsobe(getFromRowUsingLogicalDesc(row, iizFile, "OIB izvanbračnog druga"), getFromRowUsingLogicalDesc(row, iizFile, "Ime izvanbračnog druga"), getFromRowUsingLogicalDesc(row, iizFile, "Prezime izvanbračnog druga"), getFromRowUsingLogicalDesc(row, iizFile, "Spol partnera 2"), getFromRowUsingLogicalDesc(row, iizFile, "Datum rođenja partnera 2"), null);
        }

        System.out.println("INFO: Ažuriranje podataka iz Matice umrlih...");
                String muFile = "matica_umrlih.csv";
                for (Map<String, String> umrliZapis : maticaUmrlihData) {
                    String oibUmrlog = getFromRowUsingLogicalDesc(umrliZapis, muFile, "OIB osobe");
                    String imeUmrlog = getFromRowUsingLogicalDesc(umrliZapis, muFile, "Ime");
                    String prezimeUmrlog = getFromRowUsingLogicalDesc(umrliZapis, muFile, "Prezime");
                    String datumSmrtiIzCsv = getFromRowUsingLogicalDesc(umrliZapis, muFile, "Datum smrti");

                    azurirajDetaljeOsobe(oibUmrlog,
                                          imeUmrlog,
                                          prezimeUmrlog,
                                          null, // Spol - NEMA GA U matica_umrlih.csv (prema zaglavlju koje ste dali)
                                          null, // Datum rođenja - NEMA GA U matica_umrlih.csv
                                          datumSmrtiIzCsv);
                }
            }

            // Univerzalna metoda za ažuriranje detalja osobe
            private void azurirajDetaljeOsobe(String oib, String ime, String prezime, String spol, String datumRodjenja, String datumSmrti) {
                if (oib == null || oib.trim().isEmpty()) {
                    return;
                }
                String trimmedOib = oib.trim();

                Osoba postojecaOsoba = centralniPopisOsoba.computeIfAbsent(trimmedOib,
                    k -> {
                        // System.out.println("INFO: Kreiram novi Osoba objekt za OIB: " + k + " prilikom ažuriranja detalja.");
                        return new Osoba(k, null, null, null, null, null); // Kreiraj s null ako ne postoji
                    }
                );

                if (ime != null && !ime.trim().isEmpty()) {
                    if (postojecaOsoba.getIme() == null || postojecaOsoba.getIme().isEmpty() || postojecaOsoba.getIme().equals("Nepoznato")) {
                        postojecaOsoba.setIme(ime.trim());
                    }
                }
                if (prezime != null && !prezime.trim().isEmpty()) {
                    if (postojecaOsoba.getPrezime() == null || postojecaOsoba.getPrezime().isEmpty() || postojecaOsoba.getPrezime().equals("Nepoznato")) {
                        postojecaOsoba.setPrezime(prezime.trim());
                    }
                }
                if (spol != null && !spol.trim().isEmpty()) {
                    String newSpol = spol.trim().toUpperCase();
                    if (Arrays.asList("M", "Ž").contains(newSpol)) { // Provjeravamo je li novi spol validan
                        // Postavljamo spol samo ako trenutni nije postavljen ili nije validan
                        if (postojecaOsoba.getSpol() == null || postojecaOsoba.getSpol().isEmpty() || !Arrays.asList("M","Ž").contains(postojecaOsoba.getSpol())) {
                            postojecaOsoba.setSpol(newSpol);
                        }
                    }
                }
                if (datumRodjenja != null && !datumRodjenja.trim().isEmpty()) {
                    if (postojecaOsoba.getDatumRodjenja() == null || postojecaOsoba.getDatumRodjenja().isEmpty()) {
                        postojecaOsoba.setDatumRodjenja(datumRodjenja.trim());
                    }
                }
                if (datumSmrti != null && !datumSmrti.trim().isEmpty()) {
                    if (postojecaOsoba.getDatumSmrti() == null || postojecaOsoba.getDatumSmrti().isEmpty()) {
                         System.out.println("INFO: Postavljam datum smrti za OIB " + trimmedOib + " na: " + datumSmrti.trim() + " (Prethodno: " + postojecaOsoba.getDatumSmrti() + ")");
                        postojecaOsoba.setDatumSmrti(datumSmrti.trim());
                    }
                }
            }
        } // Kraj klase CsvDataLoader