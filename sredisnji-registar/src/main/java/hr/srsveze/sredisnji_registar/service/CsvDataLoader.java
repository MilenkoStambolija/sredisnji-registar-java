// Smjestite u: src/main/java/hr/srsveze/sredisnji_registar/service/CsvDataLoader.java
package hr.srsveze.sredisnji_registar.service; // Provjerite odgovara li ovo vašem paketu

import hr.srsveze.sredisnji_registar.model.MaticaRodenihEntry;
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
import java.util.Arrays; // Osigurajte da je ovaj import tu

@Component
public class CsvDataLoader {
    // DATA folder je unutar korijenskog direktorija projekta
    private static final String DATA_SUBDIRECTORY = "DATA";

    public List<MaticaRodenihEntry> maticaRodenihEntries = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> maticaVjencanihData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> maticaUmrlihData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> registarZivPartnerstvaData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> izjavaIzvanbrZajData = Collections.synchronizedList(new ArrayList<>());
    public List<Map<String, String>> stranciData = Collections.synchronizedList(new ArrayList<>());

    public Map<String, Osoba> centralniPopisOsoba = Collections.synchronizedMap(new HashMap<>());

    private String getSafe(CSVRecord record, String headerName) {
        // CSVParser je konfiguriran s .withIgnoreHeaderCase(true), ali za svaki slučaj provjeravamo.
        // Ključno je da headerName ovdje točno odgovara onome što CSVParser vraća kao ključ u mapi headera.
        // Ako CSVFormat.DEFAULT.builder().setHeader() pročita headere, oni postaju ključevi.
        if (record.isMapped(headerName)) { // Bolja provjera nego getParser().getHeaderMap().containsKey()
            String value = record.get(headerName);
            return value != null ? value.trim() : "";
        }
        //System.out.println("Upozorenje: Header '" + headerName + "' nije pronađen za zapis: " + record.getRecordNumber() + " u datoteci koja se parsira.");
        return "";
    }

    @PostConstruct
    public void loadAllData() {
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path dataDirPathObject = Paths.get(projectRoot.toString(), DATA_SUBDIRECTORY);

        System.out.println("------------------------------------------------------------------");
        System.out.println("START: Učitavanje CSV datoteka iz: " + dataDirPathObject);
        System.out.println("------------------------------------------------------------------");
        try {
            // Koristimo nove, kraće nazive datoteka
            maticaRodenihEntries.addAll(loadMaticaRodenih("matica_rodjenih.csv"));
            maticaVjencanihData.addAll(loadGenericCsv("Matica_vjenčanih.csv")); // Pazite na veliko/malo slovo u nazivu datoteke
            maticaUmrlihData.addAll(loadGenericCsv("matica_umrlih.csv"));

            List<Map<String, String>> rzpTemp = loadGenericCsv("registar životnog partnerstva.csv");
            List<Map<String, String>> izpTemp = loadGenericCsv("izjava o životnom partnerstvu.csv");

            if (!rzpTemp.isEmpty()) registarZivPartnerstvaData.addAll(rzpTemp);
            if (!izpTemp.isEmpty()) registarZivPartnerstvaData.addAll(izpTemp);
            if (!registarZivPartnerstvaData.isEmpty()) {
                 registarZivPartnerstvaData = registarZivPartnerstvaData.stream().distinct().collect(Collectors.toList());
                 System.out.println("INFO: Ukupno " + registarZivPartnerstvaData.size() + " zapisa u Registru/Izjavama živ. partnerstva (nakon spajanja i uklanjanja duplikata).");
            } else {
                 System.out.println("INFO: Nema zapisa za Registar živ. partnerstva / Izjave.");
            }

            izjavaIzvanbrZajData.addAll(loadGenericCsv("Izjava o izvanbračnoj zajednici.csv"));
            stranciData.addAll(loadGenericCsv("Registar_stranaca.csv")); // Pazite na veliko/malo slovo

            kreirajCentralniPopisOsoba();
            System.out.println("INFO: Kreiran centralni popis s " + centralniPopisOsoba.size() + " jedinstvenih osoba.");

        } catch (IOException e) {
            System.err.println("KRITIČNA GREŠKA prilikom učitavanja CSV datoteka: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("------------------------------------------------------------------");
        System.out.println("ZAVRŠENO: Učitavanje CSV datoteka.");
        System.out.println("------------------------------------------------------------------");
    }

    private List<MaticaRodenihEntry> loadMaticaRodenih(String fileName) throws IOException {
        List<MaticaRodenihEntry> entries = new ArrayList<>();
        Path filePath = Paths.get(DATA_SUBDIRECTORY, fileName);
        if (!Files.exists(filePath)) {
            System.err.println("UPOZORENJE: Datoteka NIJE PRONAĐENA: " + filePath.toAbsolutePath());
            return entries;
        }
        try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true) // Ovo bi trebalo pomoći s razlikama u vel./malim slovima u headerima
                     .setTrim(true)
                     .setAllowMissingColumnNames(true)
                     .setDelimiter(';') // DODANO: Vaše datoteke koriste točku-zarez kao delimiter
                     .setIgnoreEmptyLines(true)
                     .build())) {
            for (CSVRecord csvRecord : csvParser) {
                entries.add(new MaticaRodenihEntry(
                    getSafe(csvRecord, "OIB djeteta"), getSafe(csvRecord, "Ime djeteta"), getSafe(csvRecord, "Prezime djeteta"),
                    getSafe(csvRecord, "Spol djeteta"), getSafe(csvRecord, "Datum rođenja djeteta"),
                    getSafe(csvRecord, "OIB majke"), getSafe(csvRecord, "Ime majke"), getSafe(csvRecord, "Prezime majke"),
                    getSafe(csvRecord, "OIB oca"), getSafe(csvRecord, "Ime oca"), getSafe(csvRecord, "Prezime oca")
                ));
            }
        }
        System.out.println("INFO: Učitano " + entries.size() + " zapisa iz " + fileName);
        return entries;
    }

    private List<Map<String, String>> loadGenericCsv(String fileName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        Path filePath = Paths.get(DATA_SUBDIRECTORY, fileName);
         if (!Files.exists(filePath)) {
            System.err.println("UPOZORENJE: Datoteka NIJE PRONAĐENA: " + filePath.toAbsolutePath());
            return data;
        }
        try (Reader reader = new InputStreamReader(Files.newInputStream(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.builder()
                     .setHeader().setSkipHeaderRecord(true).setIgnoreHeaderCase(true)
                     .setTrim(true).setAllowMissingColumnNames(true).setDelimiter(';') // DODANO
                     .setIgnoreEmptyLines(true).build())) {
            for (CSVRecord csvRecord : csvParser) {
                data.add(new HashMap<>(csvRecord.toMap()));
            }
        }
        System.out.println("INFO: Učitano " + data.size() + " zapisa iz " + fileName);
        return data;
    }

    private String getSafeMap(Map<String, String> map, String key) {
        // Prvo pokušaj s točnim ključem
        String value = map.get(key);
        // Ako nije pronađeno, pokušaj s ključem bez mogućeg BOM znaka (relevantno ako CSVFormat nije dobro očistio)
        // i ignoriraj velika/mala slova iteriranjem kroz ključeve mape.
        // Međutim, CSVParser s .setIgnoreHeaderCase(true) bi trebao ovo riješiti na razini parsiranja.
        if (value == null && key != null) {
            // Ako setIgnoreHeaderCase radi, ova dodatna provjera možda nije nužna.
            // Ostavljam je kao dodatnu sigurnost, ali idealno je da CSVParser vrati konzistentne ključeve.
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (key.equalsIgnoreCase(entry.getKey().replace("\uFEFF", ""))) {
                    value = entry.getValue();
                    break;
                }
            }
        }
        return value != null ? value.trim() : "";
    }

    private void kreirajCentralniPopisOsoba() {
        // Iz Matice Rođenih
        for (MaticaRodenihEntry entry : maticaRodenihEntries) {
            dodajOsobuUPopis(entry.getOibDjeteta(), entry.getImeDjeteta(), entry.getPrezimeDjeteta(), entry.getSpolDjeteta(), entry.getDatumRodjenjaDjeteta(), "");
            dodajOsobuUPopis(entry.getOibMajke(), entry.getImeMajke(), entry.getPrezimeMajke(), "Ž", "", "");
            dodajOsobuUPopis(entry.getOibOca(), entry.getImeOca(), entry.getPrezimeOca(), "M", "", "");
        }
        // Iz Matice Vjenčanih - KORISTITE TOČNE NAZIVE STUPACA IZ VAŠE DATOTEKE!
        for (Map<String, String> row : maticaVjencanihData) {
            dodajOsobuUPopis(getSafeMap(row,"OIB muža"), getSafeMap(row,"Ime muža"), getSafeMap(row,"Prezime muža"), "M", getSafeMap(row,"Datum rođenja muža"), "");
            dodajOsobuUPopis(getSafeMap(row,"OIB žene"), getSafeMap(row,"Ime žene"), getSafeMap(row,"Prezime žene"), "Ž", getSafeMap(row,"Datum rođenja žene"), "");
        }
        // Registar živ. partnerstva (spojeno) - KORISTITE TOČNE NAZIVE STUPACA!
        for (Map<String, String> row : registarZivPartnerstvaData) {
            dodajOsobuUPopis(getSafeMap(row,"OIB partnera 1"), getSafeMap(row,"Ime partnera 1"), getSafeMap(row,"Prezime partnera 1"), getSafeMap(row,"Spol partnera 1"), getSafeMap(row,"Datum rođenja partnera 1"), "");
            dodajOsobuUPopis(getSafeMap(row,"OIB partnera 2"), getSafeMap(row,"Ime partnera 2"), getSafeMap(row,"Prezime partnera 2"), getSafeMap(row,"Spol partnera 2"), getSafeMap(row,"Datum rođenja partnera 2"), "");
        }
        // Izjava o izvanbračnoj zajednici - KORISTITE TOČNE NAZIVE STUPACA!
        for (Map<String, String> row : izjavaIzvanbrZajData) {
            dodajOsobuUPopis(getSafeMap(row,"OIB partnera 1"), getSafeMap(row,"Ime partnera 1"), getSafeMap(row,"Prezime partnera 1"), getSafeMap(row,"Spol partnera 1"), getSafeMap(row,"Datum rođenja partnera 1"), "");
            dodajOsobuUPopis(getSafeMap(row,"OIB partnera 2"), getSafeMap(row,"Ime partnera 2"), getSafeMap(row,"Prezime partnera 2"), getSafeMap(row,"Spol partnera 2"), getSafeMap(row,"Datum rođenja partnera 2"), "");
        }
        // Stranci - KORISTITE TOČNE NAZIVE STUPACA!
        for (Map<String, String> row : stranciData) {
            dodajOsobuUPopis(getSafeMap(row,"OIB stranca"), getSafeMap(row,"Ime"), getSafeMap(row,"Prezime"), getSafeMap(row,"Spol"), getSafeMap(row,"Datum rođenja"), "");
        }

        // Dodavanje datuma smrti - KORISTITE TOČNE NAZIVE STUPACA!
        for (Map<String, String> umrliZapis : maticaUmrlihData) {
            String oibUmrlog = getSafeMap(umrliZapis, "OIB umrlog");
            if (!oibUmrlog.isEmpty() && centralniPopisOsoba.containsKey(oibUmrlog)) {
                Osoba o = centralniPopisOsoba.get(oibUmrlog);
                String datumSmrtiIzCsv = getSafeMap(umrliZapis, "Datum smrti");
                if ((o.getDatumSmrti() == null || o.getDatumSmrti().isEmpty()) && !datumSmrtiIzCsv.isEmpty()) {
                    o.setDatumSmrti(datumSmrtiIzCsv);
                }
            }
        }
    }

    private void dodajOsobuUPopis(String oib, String ime, String prezime, String spol, String datumRodjenja, String datumSmrti) {
        if (oib == null || oib.trim().isEmpty()) {
            return;
        }
        String trimmedOib = oib.trim();

        centralniPopisOsoba.compute(trimmedOib, (key, existingOsoba) -> {
            if (existingOsoba == null) {
                return new Osoba(trimmedOib, ime, prezime, spol, datumRodjenja, datumSmrti);
            } else {
                // Ažuriraj samo ako je novo polje popunjeno, a staro nije (ili je prazno)
                if ((existingOsoba.getIme() == null || existingOsoba.getIme().isEmpty()) && ime != null && !ime.isEmpty()) existingOsoba.setIme(ime);
                if ((existingOsoba.getPrezime() == null || existingOsoba.getPrezime().isEmpty()) && prezime != null && !prezime.isEmpty()) existingOsoba.setPrezime(prezime);

                String existingSpol = existingOsoba.getSpol();
                String newSpol = (spol != null && !spol.isEmpty()) ? spol.toUpperCase().trim() : null; // Osiguraj da je trimovano i veliko slovo

                // Ažuriraj spol ako je novi M ili Ž, a stari nije, ili ako je stari prazan
                if (newSpol != null && (Arrays.asList("M", "Ž").contains(newSpol)) &&
                    (existingSpol == null || existingSpol.isEmpty() || !Arrays.asList("M", "Ž").contains(existingSpol))) {
                    existingOsoba.setSpol(newSpol);
                } else if ((existingSpol == null || existingSpol.isEmpty()) && newSpol != null) { // Ako je postojeći prazan, a novi ima bilo kakvu vrijednost
                     existingOsoba.setSpol(spol.trim().toUpperCase()); // Koristi originalnu vrijednost (trim & uppercase)
                }


                if ((existingOsoba.getDatumRodjenja() == null || existingOsoba.getDatumRodjenja().isEmpty()) && datumRodjenja != null && !datumRodjenja.isEmpty()) existingOsoba.setDatumRodjenja(datumRodjenja);
                if ((existingOsoba.getDatumSmrti() == null || existingOsoba.getDatumSmrti().isEmpty()) && datumSmrti != null && !datumSmrti.isEmpty()) existingOsoba.setDatumSmrti(datumSmrti);
                return existingOsoba;
            }
        });
    }
}