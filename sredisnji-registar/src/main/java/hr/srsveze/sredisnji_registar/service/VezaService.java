// Smjestite u: src/main/java/hr/srsveze/sredisnji_registar/service/VezaService.java
package hr.srsveze.sredisnji_registar.service;

import hr.srsveze.sredisnji_registar.model.Osoba;
import hr.srsveze.sredisnji_registar.model.VezaDTO;
import hr.srsveze.sredisnji_registar.model.MaticaRodenihEntry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*; // Uključuje Arrays, List, Map, Set, Collectors, Objects
import java.util.stream.Collectors;

@Service
public class VezaService {

    private final CsvDataLoader csvDataLoader;
    private final Map<String, Map<List<String>, String>> relationshipTerms;

    @Autowired
    public VezaService(CsvDataLoader csvDataLoader) {
        this.csvDataLoader = csvDataLoader;
        this.relationshipTerms = buildRelationshipTerms();
    }

    private Map<String, Map<List<String>, String>> buildRelationshipTerms() {
        Map<String, Map<List<String>, String>> terms = new HashMap<>();
        Map<List<String>, String> specificTerms;

        // roditelj_dijete (A je roditelj, B je dijete)
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "otac / sin");
        specificTerms.put(Arrays.asList("M", "Ž", null), "otac / kći");
        specificTerms.put(Arrays.asList("M", null, null), "otac / dijete");
        specificTerms.put(Arrays.asList("Ž", "M", null), "majka / sin");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "majka / kći");
        specificTerms.put(Arrays.asList("Ž", null, null), "majka / dijete");
        specificTerms.put(Arrays.asList(null, "M", null), "roditelj / sin");
        specificTerms.put(Arrays.asList(null, "Ž", null), "roditelj / kći");
        specificTerms.put(Arrays.asList(null, null, null), "roditelj / dijete");
        terms.put("roditelj_dijete", specificTerms);

        // dijete_roditelj (A je dijete, B je roditelj)
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "sin / otac");
        specificTerms.put(Arrays.asList("M", "Ž", null), "sin / majka");
        specificTerms.put(Arrays.asList("M", null, null), "sin / roditelj");
        specificTerms.put(Arrays.asList("Ž", "M", null), "kći / otac");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "kći / majka");
        specificTerms.put(Arrays.asList("Ž", null, null), "kći / roditelj");
        specificTerms.put(Arrays.asList(null, "M", null), "dijete / otac");
        specificTerms.put(Arrays.asList(null, "Ž", null), "dijete / majka");
        specificTerms.put(Arrays.asList(null, null, null), "dijete / roditelj");
        terms.put("dijete_roditelj", specificTerms);

        specificTerms = new HashMap<>(); specificTerms.put(Arrays.asList(null,null,null), "bračni drug / bračni drug"); terms.put("bracni_drug", specificTerms);
        specificTerms = new HashMap<>(); specificTerms.put(Arrays.asList(null,null,null), "životni partner / životni partner"); terms.put("zivotni_partner", specificTerms);
        specificTerms = new HashMap<>(); specificTerms.put(Arrays.asList(null,null,null), "izvanbračni drug / izvanbračni drug"); terms.put("izvanbracni_drug", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "djed / unuk"); specificTerms.put(Arrays.asList("M", "Ž", null), "djed / unuka"); specificTerms.put(Arrays.asList("M", null, null), "djed / unuče");
        specificTerms.put(Arrays.asList("Ž", "M", null), "baka / unuk"); specificTerms.put(Arrays.asList("Ž", "Ž", null), "baka / unuka"); specificTerms.put(Arrays.asList("Ž", null, null), "baka / unuče");
        specificTerms.put(Arrays.asList(null, null, null), "djed/baka / unuče");
        terms.put("djed_baka_unuk_unuka", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "unuk / djed"); specificTerms.put(Arrays.asList("M", "Ž", null), "unuk / baka");
        specificTerms.put(Arrays.asList("Ž", "M", null), "unuka / djed"); specificTerms.put(Arrays.asList("Ž", "Ž", null), "unuka / baka");
        specificTerms.put(Arrays.asList(null, null, null), "unuče / djed/baka");
        terms.put("unuk_unuka_djed_baka", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "brat / brat"); specificTerms.put(Arrays.asList("M", "Ž", null), "brat / sestra");
        specificTerms.put(Arrays.asList("Ž", "M", null), "sestra / brat"); specificTerms.put(Arrays.asList("Ž", "Ž", null), "sestra / sestra");
        specificTerms.put(Arrays.asList(null, null, null), "brat/sestra");
        terms.put("brat_sestra", specificTerms);

        specificTerms = new HashMap<>(); // A=stric/tetka/ujak, B=nećak/inja, Context=spol roditelja od B koji je brat/sestra od A
        specificTerms.put(Arrays.asList("M", "M", "M"), "stric / nećak"); specificTerms.put(Arrays.asList("M", "Ž", "M"), "stric / nećakinja");
        specificTerms.put(Arrays.asList("M", "M", "Ž"), "ujak / nećak"); specificTerms.put(Arrays.asList("M", "Ž", "Ž"), "ujak / nećakinja");
        specificTerms.put(Arrays.asList("Ž", "M", "M"), "tetka (očeva sestra) / nećak"); specificTerms.put(Arrays.asList("Ž", "Ž", "M"), "tetka (očeva sestra) / nećakinja");
        specificTerms.put(Arrays.asList("Ž", "M", "Ž"), "tetka (majcina sestra) / nećak"); specificTerms.put(Arrays.asList("Ž", "Ž", "Ž"), "tetka (majcina sestra) / nećakinja");
        specificTerms.put(Arrays.asList(null, null, null), "stric/tetka/ujak / nećak/inja");
        terms.put("stric_tetka_ujak_necak_necakinja", specificTerms);

        specificTerms = new HashMap<>(); // A=nećak/inja, B=stric/tetka/ujak, Context=spol roditelja od A koji je brat/sestra od B
        specificTerms.put(Arrays.asList("M", "M", "M"), "nećak / stric"); specificTerms.put(Arrays.asList("M", "M", "Ž"), "nećak / ujak");
        specificTerms.put(Arrays.asList("M", "Ž", "M"), "nećak / tetka (očeva sestra)"); specificTerms.put(Arrays.asList("M", "Ž", "Ž"), "nećak / tetka (majcina sestra)");
        specificTerms.put(Arrays.asList("Ž", "M", "M"), "nećakinja / stric"); specificTerms.put(Arrays.asList("Ž", "M", "Ž"), "nećakinja / ujak");
        specificTerms.put(Arrays.asList("Ž", "Ž", "M"), "nećakinja / tetka (očeva sestra)"); specificTerms.put(Arrays.asList("Ž", "Ž", "Ž"), "nećakinja / tetka (majcina sestra)");
        specificTerms.put(Arrays.asList(null, null, null), "nećak/nećakinja / stric/tetka/ujak");
        terms.put("necak_necakinja_stric_tetka_ujak", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "bratić / bratić"); specificTerms.put(Arrays.asList("M", "Ž", null), "bratić / sestrična");
        specificTerms.put(Arrays.asList("Ž", "M", null), "sestrična / bratić"); specificTerms.put(Arrays.asList("Ž", "Ž", null), "sestrična / sestrična");
        specificTerms.put(Arrays.asList(null, null, null), "bratić/sestrična");
        terms.put("bratic_sestricna", specificTerms);

        return Collections.unmodifiableMap(terms);
    }

    private String getRelationshipDisplayTerm(String baseTermKey, String spolA, String spolB, String contextSpol) {
        Map<List<String>, String> specificTermsMap = relationshipTerms.get(baseTermKey);
        if (specificTermsMap == null) return baseTermKey.replace("_", " / ");

        spolA = (spolA == null || spolA.trim().isEmpty()) ? null : spolA.trim().toUpperCase();
        spolB = (spolB == null || spolB.trim().isEmpty()) ? null : spolB.trim().toUpperCase();
        contextSpol = (contextSpol == null || contextSpol.trim().isEmpty()) ? null : contextSpol.trim().toUpperCase();

        String term = specificTermsMap.get(Arrays.asList(spolA, spolB, contextSpol));
        if (term != null) return term;
        term = specificTermsMap.get(Arrays.asList(spolA, spolB, null));
        if (term != null) return term;
        if (spolA != null) {
            term = specificTermsMap.get(Arrays.asList(spolA, null, contextSpol)); if (term != null) return term;
            term = specificTermsMap.get(Arrays.asList(spolA, null, null)); if (term != null) return term;
        }
        if (spolB != null) {
            term = specificTermsMap.get(Arrays.asList(null, spolB, contextSpol)); if (term != null) return term;
            term = specificTermsMap.get(Arrays.asList(null, spolB, null)); if (term != null) return term;
        }
        return specificTermsMap.getOrDefault(Arrays.asList(null, null, null), baseTermKey.replace("_", " / "));
    }

    private boolean addUniqueRelationship(String oib1, String oib2, String vrstaVezePrikaz, Set<List<String>> processedRelations, boolean simetricna) {
        if (oib1 == null || oib2 == null || oib1.trim().isEmpty() || oib2.trim().isEmpty() || oib1.trim().equals(oib2.trim())) return false;
        List<String> keyParts = new ArrayList<>();
        String o1Trim = oib1.trim(); String o2Trim = oib2.trim();
        if (simetricna) {
            List<String> oibs = new ArrayList<>(Arrays.asList(o1Trim, o2Trim)); Collections.sort(oibs); keyParts.addAll(oibs);
        } else {
            keyParts.add(o1Trim); keyParts.add(o2Trim);
        }
        keyParts.add(vrstaVezePrikaz);
        return processedRelations.add(keyParts);
    }

    // ISPRAVLJENA METODA - SADA VRAĆA BOOLEAN
    private boolean addRelationshipToResults(List<VezaDTO> veze, Osoba targetOsoba, Osoba povezanaOsoba, String vrstaVezeKljuc,
                                             String opisFormat, String tipVeze, String linija, String stupanj,
                                             Set<List<String>> processedRelations, String contextSpolZaTermin, boolean simetricna) {

        if (povezanaOsoba == null || povezanaOsoba.getOib() == null || povezanaOsoba.getOib().trim().isEmpty()) return false;
        if (targetOsoba == null || targetOsoba.getOib() == null || targetOsoba.getOib().trim().isEmpty()) return false;

        String targetOib = targetOsoba.getOib();
        String targetSpol = targetOsoba.getSpol();
        String povezanaSpol = povezanaOsoba.getSpol();
        String vrstaVezePrikaz = getRelationshipDisplayTerm(vrstaVezeKljuc, targetSpol, povezanaSpol, contextSpolZaTermin);

        if (addUniqueRelationship(targetOib, povezanaOsoba.getOib(), vrstaVezePrikaz, processedRelations, simetricna)) {
            veze.add(new VezaDTO(
                povezanaOsoba.getOib(),
                povezanaOsoba.getPunoImeOrOib(),
                vrstaVezePrikaz,
                opisFormat.replace("{target}", targetOsoba.getPunoImeOrOib()).replace("{povezana}", povezanaOsoba.getPunoImeOrOib()),
                tipVeze, linija, stupanj
            ));
            return true; // Veza je uspješno dodana
        }
        return false; // Veza nije dodana (vjerojatno duplikat)
    }

    public Map<String, Object> pronadjiSveVeze(String oib) {
        List<VezaDTO> veze = new ArrayList<>();
        List<String> queriesLog = new ArrayList<>();
        Set<List<String>> processedRelations = new HashSet<>();

        Osoba targetOsoba = csvDataLoader.centralniPopisOsoba.get(oib);
        if (targetOsoba == null) {
            queriesLog.add("OIB '" + oib + "' nije pronađen u centralnom popisu osoba.");
            return Map.of("oib", oib, "error", "OIB nije pronađen.", "relationships", Collections.emptyList(),
                          "graph_nodes", Collections.emptyList(), "graph_edges", Collections.emptyList(), "queries_log", queriesLog);
        }

        // === PRIMARNE VEZE ===
        queriesLog.add("--- START: Traženje primarnih veza za OIB: " + oib + " ---");
        // 1. Roditelji (targetOsoba je dijete)
        queriesLog.add("Traženje roditelja u 'Matica rođenih'...");
        for (MaticaRodenihEntry entry : csvDataLoader.maticaRodenihEntries) {
            if (oib.equals(entry.getOibDjeteta())) {
                Osoba majka = csvDataLoader.centralniPopisOsoba.get(entry.getOibMajke());
                if (majka != null) addRelationshipToResults(veze, targetOsoba, majka, "dijete_roditelj", "{target} je dijete osobe {povezana} (majka)", "Izravna", "Uspravna", "1", processedRelations, null, false);
                Osoba otac = csvDataLoader.centralniPopisOsoba.get(entry.getOibOca());
                if (otac != null) addRelationshipToResults(veze, targetOsoba, otac, "dijete_roditelj", "{target} je dijete osobe {povezana} (otac)", "Izravna", "Uspravna", "1", processedRelations, null, false);
            }
        }

        // 2. Djeca (targetOsoba je roditelj)
        queriesLog.add("Traženje djece u 'Matica rođenih'...");
        for (MaticaRodenihEntry entry : csvDataLoader.maticaRodenihEntries) {
            if (oib.equals(entry.getOibMajke()) || oib.equals(entry.getOibOca())) {
                Osoba dijete = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                if (dijete != null) addRelationshipToResults(veze, targetOsoba, dijete, "roditelj_dijete", "{target} je roditelj osobe {povezana}", "Izravna", "Uspravna", "1", processedRelations, null, false);
            }
        }

        // 3. Partneri - Bračni drugovi
        queriesLog.add("Traženje bračnih drugova u 'Matica vjenčanih'...");
        for (Map<String, String> row : csvDataLoader.maticaVjencanihData) {
            String datumPrestanka = row.getOrDefault("Datum prestanka braka", "").trim();
            if (datumPrestanka.isEmpty()) {
                String oibMuza = row.getOrDefault("OIB muža", "").trim();
                String oibZene = row.getOrDefault("OIB žene", "").trim();
                String partnerOib = null;
                if (oib.equals(oibMuza) && !oibZene.isEmpty()) partnerOib = oibZene;
                else if (oib.equals(oibZene) && !oibMuza.isEmpty()) partnerOib = oibMuza;
                if (partnerOib != null && !partnerOib.isEmpty()) {
                    Osoba partner = csvDataLoader.centralniPopisOsoba.get(partnerOib);
                    if (partner != null) addRelationshipToResults(veze, targetOsoba, partner, "bracni_drug", "{target} je u braku s osobom {povezana}", "Izravna", "Partnerska", "N/A", processedRelations, null, true);
                }
            }
        }

        // 4. Životni partneri
        queriesLog.add("Traženje životnih partnera u 'Registar živ. partnerstva / Izjave'...");
        for (Map<String, String> row : csvDataLoader.registarZivPartnerstvaData) {
            String datumPrestanka = row.getOrDefault("Datum prestanka životnog partnerstva", "").trim();
             if (datumPrestanka.isEmpty()) {
                String oibP1 = row.getOrDefault("OIB partnera 1", "").trim();
                String oibP2 = row.getOrDefault("OIB partnera 2", "").trim();
                String partnerOib = null;
                if (oib.equals(oibP1) && !oibP2.isEmpty()) partnerOib = oibP2;
                else if (oib.equals(oibP2) && !oibP1.isEmpty()) partnerOib = oibP1;
                if (partnerOib != null && !partnerOib.isEmpty()) {
                    Osoba partner = csvDataLoader.centralniPopisOsoba.get(partnerOib);
                     if (partner != null) addRelationshipToResults(veze, targetOsoba, partner, "zivotni_partner", "{target} je u životnom partnerstvu s osobom {povezana}", "Izravna", "Partnerska", "N/A", processedRelations, null, true);
                }
            }
        }

        // 5. Izvanbračni partneri
        queriesLog.add("Traženje izvanbračnih partnera u 'Izjave o izvanbr. zajednici'...");
        for (Map<String, String> row : csvDataLoader.izjavaIzvanbrZajData) {
            String datumPrestanka = row.getOrDefault("Datum prestanka izvanbračne zajednice", "").trim();
             if (datumPrestanka.isEmpty()) {
                String oibP1 = row.getOrDefault("OIB partnera 1", "").trim();
                String oibP2 = row.getOrDefault("OIB partnera 2", "").trim();
                String partnerOib = null;
                if (oib.equals(oibP1) && !oibP2.isEmpty()) partnerOib = oibP2;
                else if (oib.equals(oibP2) && !oibP1.isEmpty()) partnerOib = oibP1;
                if (partnerOib != null && !partnerOib.isEmpty()) {
                    Osoba partner = csvDataLoader.centralniPopisOsoba.get(partnerOib);
                    if (partner != null) addRelationshipToResults(veze, targetOsoba, partner, "izvanbracni_drug", "{target} je u izvanbračnoj zajednici s osobom {povezana}", "Izravna", "Partnerska", "N/A", processedRelations, null, true);
                }
            }
        }
        queriesLog.add("--- KRAJ: Traženje primarnih veza ---");

        // === IZVEDENE VEZE ===
        queriesLog.add("--- START: Traženje izvedenih veza ---");

        List<Osoba> roditeljiTargeta = new ArrayList<>();
        MaticaRodenihEntry targetParentsEntry = csvDataLoader.maticaRodenihEntries.stream()
            .filter(e -> oib.equals(e.getOibDjeteta())).findFirst().orElse(null);
        if (targetParentsEntry != null) {
            Osoba majka = csvDataLoader.centralniPopisOsoba.get(targetParentsEntry.getOibMajke());
            if (majka != null) roditeljiTargeta.add(majka);
            Osoba otac = csvDataLoader.centralniPopisOsoba.get(targetParentsEntry.getOibOca());
            if (otac != null) roditeljiTargeta.add(otac);
        }
        final List<Osoba> finalRoditeljiTargeta = Collections.unmodifiableList(new ArrayList<>(roditeljiTargeta)); // Efektivno finalna za lambde

        List<Osoba> djecaTargeta = csvDataLoader.maticaRodenihEntries.stream()
            .filter(e -> oib.equals(e.getOibMajke()) || oib.equals(e.getOibOca()))
            .map(e -> csvDataLoader.centralniPopisOsoba.get(e.getOibDjeteta()))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        final List<Osoba> finalDjecaTargeta = Collections.unmodifiableList(new ArrayList<>(djecaTargeta)); // Efektivno finalna


        // 1. Djed/Baka (targetOsoba je unuk/unuka)
        for (Osoba roditelj : finalRoditeljiTargeta) {
            final String roditeljOib = roditelj.getOib(); // Efektivno finalna za lambdu
            queriesLog.add("Traženje roditelja od " + roditelj.getPunoImeOrOib() + " (djedova/baka od " + oib + ")");
            csvDataLoader.maticaRodenihEntries.stream()
                .filter(e -> roditeljOib.equals(e.getOibDjeteta()))
                .forEach(entry -> {
                    Osoba baka = csvDataLoader.centralniPopisOsoba.get(entry.getOibMajke());
                    if (baka != null) addRelationshipToResults(veze, targetOsoba, baka, "unuk_unuka_djed_baka", "{target} je unuče osobe {povezana} (baka)", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                    Osoba djed = csvDataLoader.centralniPopisOsoba.get(entry.getOibOca());
                    if (djed != null) addRelationshipToResults(veze, targetOsoba, djed, "unuk_unuka_djed_baka", "{target} je unuče osobe {povezana} (djed)", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                });
        }

        // 2. Unuci (targetOsoba je djed/baka)
        for(Osoba dijete : finalDjecaTargeta) {
            final String dijeteOib = dijete.getOib(); // Efektivno finalna
            queriesLog.add("Traženje djece od " + dijete.getPunoImeOrOib() + " (unuka od " + oib + ")");
            csvDataLoader.maticaRodenihEntries.stream()
                .filter(e -> dijeteOib.equals(e.getOibMajke()) || dijeteOib.equals(e.getOibOca()))
                .forEach(entry -> {
                    Osoba unukUnuka = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                    if(unukUnuka != null) addRelationshipToResults(veze, targetOsoba, unukUnuka, "djed_baka_unuk_unuka", "{target} je djed/baka osobe {povezana}", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                });
        }

        // 3. Braća/Sestre
        List<Osoba> targetSiblings = new ArrayList<>();
        if (targetParentsEntry != null) {
            final String oibMajkeT = targetParentsEntry.getOibMajke(); // Efektivno finalna
            final String oibOcaT = targetParentsEntry.getOibOca();     // Efektivno finalna
            queriesLog.add("Traženje braće/sestara za OIB " + oib);
            csvDataLoader.maticaRodenihEntries.stream()
                .filter(entry -> {
                    String currentChildOib = entry.getOibDjeteta();
                    return !oib.equals(currentChildOib) && currentChildOib != null && !currentChildOib.isEmpty();
                })
                .forEach(entry -> { // Lambda izraz
                    Osoba sibling = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                    if (sibling == null) return;

                    boolean isFull = (oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entry.getOibMajke())) &&
                                     (oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entry.getOibOca()));
                    boolean isHalfMother = (oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entry.getOibMajke())) &&
                                         (!(oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entry.getOibOca())) && (entry.getOibOca()!=null && !entry.getOibOca().isEmpty()));
                    boolean isHalfFather = (oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entry.getOibOca())) &&
                                         (!(oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entry.getOibMajke())) && (entry.getOibMajke()!=null && !entry.getOibMajke().isEmpty()));

                    String opis = ""; String tipSrodstvaPrikaz = "";
                    if (isFull) { opis = "{target} i {povezana} su puna braća/sestre"; tipSrodstvaPrikaz = " (puni)"; }
                    else if (isHalfMother) { opis = "{target} i {povezana} su polubraća/sestre po majci"; tipSrodstvaPrikaz = " (polu- po majci)"; }
                    else if (isHalfFather) { opis = "{target} i {povezana} su polubraća/sestre po ocu"; tipSrodstvaPrikaz = " (polu- po ocu)"; }

                    if (!opis.isEmpty()) {
                        String vrstaVezePrikaz = getRelationshipDisplayTerm("brat_sestra", targetOsoba.getSpol(), sibling.getSpol(), null);
                        if (addUniqueRelationship(oib, sibling.getOib(), vrstaVezePrikaz + tipSrodstvaPrikaz, processedRelations, true)) {
                             veze.add(new VezaDTO(sibling.getOib(), sibling.getPunoImeOrOib(), vrstaVezePrikaz + tipSrodstvaPrikaz,
                                opis.replace("{target}", targetOsoba.getPunoImeOrOib()).replace("{povezana}", sibling.getPunoImeOrOib()),
                                "Izvedena", "Pobočna", "2"));
                            targetSiblings.add(sibling);
                        }
                    }
                });
        }
        final List<Osoba> finalTargetSiblings = Collections.unmodifiableList(new ArrayList<>(targetSiblings.stream().distinct().collect(Collectors.toList()))); // Efektivno finalna

        // 4. Stričevi/Tetke/Ujaci (targetOsoba je nećak/inja)
        List<Osoba> unclesAuntsList = new ArrayList<>();
        for (Osoba parentOfTarget : finalRoditeljiTargeta) {
            final String parentOfTargetSpol = parentOfTarget.getSpol(); // Efektivno finalna
            final String parentOfTargetOib = parentOfTarget.getOib();   // Efektivno finalna
            MaticaRodenihEntry grandparentsEntry = csvDataLoader.maticaRodenihEntries.stream()
                .filter(e -> parentOfTargetOib.equals(e.getOibDjeteta())).findFirst().orElse(null);
            if (grandparentsEntry != null) {
                final String gpMajkaOib = grandparentsEntry.getOibMajke(); // Efektivno finalna
                final String gpOtacOib = grandparentsEntry.getOibOca();     // Efektivno finalna
                queriesLog.add("Traženje braće/sestara roditelja " + parentOfTarget.getPunoImeOrOib() + " (stričevi/tetke/ujaci od " + oib +")");
                csvDataLoader.maticaRodenihEntries.stream()
                    .filter(entry -> !parentOfTargetOib.equals(entry.getOibDjeteta()) && entry.getOibDjeteta() != null && !entry.getOibDjeteta().isEmpty())
                    .forEach(entry -> { // Lambda
                        boolean isParentFullSibling = (gpMajkaOib != null && !gpMajkaOib.isEmpty() && gpMajkaOib.equals(entry.getOibMajke())) &&
                                                      (gpOtacOib != null && !gpOtacOib.isEmpty() && gpOtacOib.equals(entry.getOibOca()));
                        if (isParentFullSibling) {
                            Osoba uncleAunt = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                            if (uncleAunt != null) {
                                // Ovdje pozivamo addRelationshipToResults, koja je sada boolean
                                if (addRelationshipToResults(veze, targetOsoba, uncleAunt, "necak_necakinja_stric_tetka_ujak", "{target} je nećak/inja osobi {povezana}", "Izvedena", "Pobočna", "3", processedRelations, parentOfTargetSpol, false)) {
                                    unclesAuntsList.add(uncleAunt);
                                }
                            }
                        }
                    });
            }
        }
        final List<Osoba> finalUnclesAuntsList = Collections.unmodifiableList(new ArrayList<>(unclesAuntsList.stream().distinct().collect(Collectors.toList()))); // Efektivno finalna

        // 5. Nećaci/Nećakinje (targetOsoba je stric/tetka/ujak)
        for (Osoba siblingOfTarget : finalTargetSiblings) {
            final String siblingOfTargetSpol = siblingOfTarget.getSpol(); // Efektivno finalna
            final String siblingOfTargetOib = siblingOfTarget.getOib();   // Efektivno finalna
            queriesLog.add("Traženje djece od " + siblingOfTarget.getPunoImeOrOib() + " (nećaka/nećakinja od " + oib + ")");
            csvDataLoader.maticaRodenihEntries.stream()
                .filter(entry -> siblingOfTargetOib.equals(entry.getOibMajke()) || siblingOfTargetOib.equals(entry.getOibOca()))
                .forEach(entry -> { // Lambda
                    Osoba nephewNiece = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                    if (nephewNiece != null) {
                        addRelationshipToResults(veze, targetOsoba, nephewNiece, "stric_tetka_ujak_necak_necakinja", "{target} je stric/tetka/ujak osobi {povezana}", "Izvedena", "Pobočna", "3", processedRelations, siblingOfTargetSpol, false);
                    }
                });
        }

        // 6. Bratići/Sestrične (prvo koljeno)
        for (Osoba uncleAunt : finalUnclesAuntsList) {
            final String uncleAuntOib = uncleAunt.getOib(); // Efektivno finalna
            queriesLog.add("Traženje djece od " + uncleAunt.getPunoImeOrOib() + " (bratića/sestrični od " + oib + ")");
            csvDataLoader.maticaRodenihEntries.stream()
                 .filter(entry -> uncleAuntOib.equals(entry.getOibMajke()) || uncleAuntOib.equals(entry.getOibOca()))
                 .forEach(entry -> { // Lambda
                    Osoba cousin = csvDataLoader.centralniPopisOsoba.get(entry.getOibDjeteta());
                    if (cousin != null) {
                        addRelationshipToResults(veze, targetOsoba, cousin, "bratic_sestricna", "{target} i {povezana} su bratići/sestrične", "Izvedena", "Pobočna", "4", processedRelations, null, true);
                    }
                });
        }
        queriesLog.add("--- KRAJ: Traženje izvedenih veza ---");

        // Priprema podataka za graf
        List<Map<String, Object>> graphNodes = new ArrayList<>();
        List<Map<String, Object>> graphEdges = new ArrayList<>();
        Set<String> addedNodesForGraph = new HashSet<>();

        if (targetOsoba != null && addedNodesForGraph.add(oib)) {
            Map<String, Object> targetNode = new HashMap<>();
            targetNode.put("id", oib); targetNode.put("label", targetOsoba.getPunoImeOrOib() + "\n(" + oib + ")");
            targetNode.put("group", targetOsoba.getSpol() != null ? targetOsoba.getSpol() : "N");
            targetNode.put("title", "OIB: " + oib + "<br>Spol: " + (targetOsoba.getSpol() != null ? targetOsoba.getSpol() : "N/A") +
                                   "<br>Rođen: " + (targetOsoba.getDatumRodjenja() != null && !targetOsoba.getDatumRodjenja().isEmpty() ? targetOsoba.getDatumRodjenja() : "N/A") +
                                   "<br>Umro: " + (targetOsoba.getDatumSmrti() != null && !targetOsoba.getDatumSmrti().isEmpty() ? targetOsoba.getDatumSmrti() : "N/A"));
            graphNodes.add(targetNode);
        }
        for (VezaDTO veza : veze) {
            Osoba povezanaOsoba = csvDataLoader.centralniPopisOsoba.get(veza.getOibPovezaneOsobe());
            if (povezanaOsoba != null && addedNodesForGraph.add(povezanaOsoba.getOib())) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", povezanaOsoba.getOib()); node.put("label", povezanaOsoba.getPunoImeOrOib() + "\n(" + povezanaOsoba.getOib() + ")");
                node.put("group", povezanaOsoba.getSpol() != null ? povezanaOsoba.getSpol() : "N");
                node.put("title", "OIB: " + povezanaOsoba.getOib() + "<br>Ime i Prezime: " + povezanaOsoba.getPunoImeOrOib() +
                                   "<br>Spol: " + (povezanaOsoba.getSpol() != null ? povezanaOsoba.getSpol() : "N/A") +
                                   "<br>Rođen: " + (povezanaOsoba.getDatumRodjenja() != null && !povezanaOsoba.getDatumRodjenja().isEmpty() ? povezanaOsoba.getDatumRodjenja() : "N/A") +
                                   "<br>Umro: " + (povezanaOsoba.getDatumSmrti() != null && !povezanaOsoba.getDatumSmrti().isEmpty() ? povezanaOsoba.getDatumSmrti() : "N/A"));
                graphNodes.add(node);
            }
            Map<String, Object> edge = new HashMap<>();
            edge.put("from", oib); edge.put("to", veza.getOibPovezaneOsobe());
            edge.put("label", veza.getVrstaVezePrikaz()); edge.put("title", veza.getOpis());
            edge.put("arrows", "to"); graphEdges.add(edge);
        }

        return Map.of("oib", oib, "relationships", veze, "graph_nodes", graphNodes, "graph_edges", graphEdges, "queries_log", queriesLog);
    }
}