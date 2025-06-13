package hr.srsveze.sredisnji_registar.service;

import hr.srsveze.sredisnji_registar.model.Osoba;
import hr.srsveze.sredisnji_registar.model.VezaDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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

        // Partner relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "Ž", null), "suprug / supruga");
        specificTerms.put(Arrays.asList("Ž", "M", null), "supruga / suprug");
        terms.put("bracni_drug", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "životni partner / životni partner");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "životni partner / životni partner");
        terms.put("zivotni_partner", specificTerms);

        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "Ž", null), "izvanbračni drug / izvanbračna drugarica");
        specificTerms.put(Arrays.asList("Ž", "M", null), "izvanbračna drugarica / izvanbračni drug");
        terms.put("izvanbracni_drug", specificTerms);

        // Grandparent relationships
specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "djed / unuk");
        specificTerms.put(Arrays.asList("M", "Ž", null), "djed / unuka");
        specificTerms.put(Arrays.asList("M", null, null), "djed / unuče");
        specificTerms.put(Arrays.asList("Ž", "M", null), "baka / unuk");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "baka / unuka");
        specificTerms.put(Arrays.asList("Ž", null, null), "baka / unuče");
        specificTerms.put(Arrays.asList(null, "M", null), "djed/baka / unuk"); // Novi unos
        specificTerms.put(Arrays.asList(null, "Ž", null), "djed/baka / unuka"); // Novi unos
        specificTerms.put(Arrays.asList(null, null, null), "djed/baka / unuče");
        terms.put("djed_baka_unuk_unuka", specificTerms);

        // Grandchild relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "unuk / djed");
        specificTerms.put(Arrays.asList("M", "Ž", null), "unuk / baka");
        specificTerms.put(Arrays.asList("M", null, null), "unuk / djed/baka"); // Novi unos
        specificTerms.put(Arrays.asList("Ž", "M", null), "unuka / djed");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "unuka / baka");
        specificTerms.put(Arrays.asList("Ž", null, null), "unuka / djed/baka"); // Novi unos
        specificTerms.put(Arrays.asList(null, "M", null), "unuče / djed");
        specificTerms.put(Arrays.asList(null, "Ž", null), "unuče / baka");
        specificTerms.put(Arrays.asList(null, null, null), "unuče / djed/baka");
        terms.put("unuk_unuka_djed_baka", specificTerms);;

        // Sibling relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "brat / brat");
        specificTerms.put(Arrays.asList("M", "Ž", null), "brat / sestra");
        specificTerms.put(Arrays.asList("Ž", "M", null), "sestra / brat");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "sestra / sestra");
        specificTerms.put(Arrays.asList(null, null, null), "brat/sestra");
        terms.put("brat_sestra", specificTerms);

        // Uncle/Aunt relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", "M"), "stric / nećak");
        specificTerms.put(Arrays.asList("M", "Ž", "M"), "stric / nećakinja");
        specificTerms.put(Arrays.asList("M", "M", "Ž"), "ujak / nećak");
        specificTerms.put(Arrays.asList("M", "Ž", "Ž"), "ujak / nećakinja");
        specificTerms.put(Arrays.asList("Ž", "M", "M"), "tetka (očeva sestra) / nećak");
        specificTerms.put(Arrays.asList("Ž", "Ž", "M"), "tetka (očeva sestra) / nećakinja");
        specificTerms.put(Arrays.asList("Ž", "M", "Ž"), "tetka (majčina sestra) / nećak");
        specificTerms.put(Arrays.asList("Ž", "Ž", "Ž"), "tetka (majčina sestra) / nećakinja");
        specificTerms.put(Arrays.asList(null, null, null), "stric/tetka/ujak / nećak/inja");
        terms.put("stric_tetka_ujak_necak_necakinja", specificTerms);

        // Nephew/Niece relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", "M"), "nećak / stric");
        specificTerms.put(Arrays.asList("M", "M", "Ž"), "nećak / ujak");
        specificTerms.put(Arrays.asList("M", "Ž", "M"), "nećak / tetka (očeva sestra)");
        specificTerms.put(Arrays.asList("M", "Ž", "Ž"), "nećak / tetka (majčina sestra)");
        specificTerms.put(Arrays.asList("Ž", "M", "M"), "nećakinja / stric");
        specificTerms.put(Arrays.asList("Ž", "M", "Ž"), "nećakinja / ujak");
        specificTerms.put(Arrays.asList("Ž", "Ž", "M"), "nećakinja / tetka (očeva sestra)");
        specificTerms.put(Arrays.asList("Ž", "Ž", "Ž"), "nećakinja / tetka (majčina sestra)");
        specificTerms.put(Arrays.asList(null, null, null), "nećak/nećakinja / stric/tetka/ujak");
        terms.put("necak_necakinja_stric_tetka_ujak", specificTerms);

        // Cousin relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "bratić / bratić");
        specificTerms.put(Arrays.asList("M", "Ž", null), "bratić / sestrična");
        specificTerms.put(Arrays.asList("Ž", "M", null), "sestrična / bratić");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "sestrična / sestrična");
        specificTerms.put(Arrays.asList(null, null, null), "bratić/sestrična");
        terms.put("bratic_sestricna", specificTerms);

        // Half-sibling relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "polubrat / polubrat");
        specificTerms.put(Arrays.asList("M", "Ž", null), "polubrat / polusestra");
        specificTerms.put(Arrays.asList("Ž", "M", null), "polusestra / polubrat");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "polusestra / polusestra");
        specificTerms.put(Arrays.asList(null, null, null), "polubrat/polusestra");
        terms.put("polubrat_polusestra", specificTerms);

        // Great-grandparent relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "pradjed / praunuk");
        specificTerms.put(Arrays.asList("M", "Ž", null), "pradjed / praunuka");
        specificTerms.put(Arrays.asList("M", null, null), "pradjed / praunuče"); // A(M) pradjed, B(nepoznat spol) praunuče
        specificTerms.put(Arrays.asList("Ž", "M", null), "prabaka / praunuk");
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "prabaka / praunuka");
        specificTerms.put(Arrays.asList("Ž", null, null), "prabaka / praunuče"); // A(Ž) prabaka, B(nepoznat spol) praunuče
        specificTerms.put(Arrays.asList(null, "M", null), "pradjed/prabaka / praunuk"); // A(nepoznat spol), B(M) praunuk
        specificTerms.put(Arrays.asList(null, "Ž", null), "pradjed/prabaka / praunuka"); // A(nepoznat spol), B(Ž) praunuka
        specificTerms.put(Arrays.asList(null, null, null), "pradjed/prabaka / praunuče");
        terms.put("pradjed_prabaka_praunuk_praunuka", specificTerms);

        // Great-grandchild relationships
        specificTerms = new HashMap<>();
        specificTerms.put(Arrays.asList("M", "M", null), "praunuk / pradjed"); // A(M) praunuk, B(M) pradjed
        specificTerms.put(Arrays.asList("M", "Ž", null), "praunuk / prabaka");   // A(M) praunuk, B(Ž) prabaka <- Ovo bi trebalo riješiti problem
        specificTerms.put(Arrays.asList("M", null, null), "praunuk / pradjed/prabaka"); // A(M) praunuk, B(nepoznat spol)
        specificTerms.put(Arrays.asList("Ž", "M", null), "praunuka / pradjed"); // A(Ž) praunuka, B(M) pradjed
        specificTerms.put(Arrays.asList("Ž", "Ž", null), "praunuka / prabaka");   // A(Ž) praunuka, B(Ž) prabaka
        specificTerms.put(Arrays.asList("Ž", null, null), "praunuka / pradjed/prabaka"); // A(Ž) praunuka, B(nepoznat spol)
        specificTerms.put(Arrays.asList(null, "M", null), "praunuče / pradjed"); // A(nepoznat spol) praunuče, B(M) pradjed
        specificTerms.put(Arrays.asList(null, "Ž", null), "praunuče / prabaka"); // A(nepoznat spol) praunuče, B(Ž) prabaka
        specificTerms.put(Arrays.asList(null, null, null), "praunuče / pradjed/prabaka");
        terms.put("praunuk_praunuka_pradjed_prabaka", specificTerms);

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
            String refinedOpis = refineDescription(opisFormat, vrstaVezePrikaz, targetOsoba, povezanaOsoba);
            veze.add(new VezaDTO(
                povezanaOsoba.getOib(),
                povezanaOsoba.getPunoImeOrOib(),
                vrstaVezePrikaz,
                refinedOpis,
                tipVeze, linija, stupanj
            ));
            return true;
        }
        return false;
    }

    private String refineDescription(String opisFormat, String vrstaVezePrikaz, Osoba targetOsoba, Osoba povezanaOsoba) {
        String[] roles = vrstaVezePrikaz.split(" / ");
        String targetRole = roles[0].trim();
        String povezanaRole = roles.length > 1 ? roles[1].trim() : "";
        String refinedOpis = opisFormat;

        // Replace placeholders with names
        refinedOpis = refinedOpis.replace("{target}", targetOsoba.getPunoImeOrOib())
                                 .replace("{povezana}", povezanaOsoba.getPunoImeOrOib());

        // Enhance descriptions based on relationship type and gender
        switch (vrstaVezePrikaz.split(" / ")[0]) {
            case "otac":
            case "majka":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "sin":
            case "kći":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "djed":
            case "baka":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "unuk":
            case "unuka":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "brat":
            case "sestra":
            case "polubrat":
            case "polusestra":
            case "bratić":
            case "sestrična":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                                break;
            case "stric":
            case "ujak":
            case "tetka (očeva sestra)":
            case "tetka (majčina sestra)":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "nećak":
            case "nećakinja":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;

            case "suprug":
            case "supruga":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "životni partner":
                refinedOpis = String.format("%s je životni partner osobe %s", targetOsoba.getPunoImeOrOib(), povezanaOsoba.getPunoImeOrOib());
                break;
            case "izvanbračni drug":
            case "izvanbračna drugarica":
                refinedOpis = String.format("%s je izvanbračni partner osobe %s", targetOsoba.getPunoImeOrOib(), povezanaOsoba.getPunoImeOrOib());
                break;
            case "pradjed":
            case "prabaka":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            case "praunuk":
            case "praunuka":
                refinedOpis = String.format("%s je %s osobe %s", targetOsoba.getPunoImeOrOib(), targetRole, povezanaOsoba.getPunoImeOrOib());
                break;
            default:
                refinedOpis = refinedOpis.replace("{target}", targetOsoba.getPunoImeOrOib())
                                         .replace("{povezana}", povezanaOsoba.getPunoImeOrOib());
                break;
        }
        return refinedOpis;
    }

    private void checkGreatGrandRelationships(Osoba targetOsoba, List<VezaDTO> veze, Set<List<String>> processedRelations) {
        final String MR_FILENAME = "matica_rodjenih.csv";

        // 1. KORAK: Pronađi roditelje ciljane osobe
        List<Osoba> roditeljiTargeta = new ArrayList<>();
        csvDataLoader.maticaRodenihData.stream()
            .filter(eMap -> targetOsoba.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
            .findFirst()
            .ifPresent(rowMap -> {
                Osoba majka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB majke"));
                if (majka != null) roditeljiTargeta.add(majka);
                Osoba otac = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB oca"));
                if (otac != null) roditeljiTargeta.add(otac);
            });

        // 2. KORAK: Za svakog roditelja pronađi NJIHOVE roditelje (djedove i bake)
        List<Osoba> djedoviBake = new ArrayList<>();
        for (Osoba roditelj : roditeljiTargeta) {
            csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> roditelj.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
                .findFirst()
                .ifPresent(rowMap -> {
                    Osoba baka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB majke"));
                    if (baka != null) djedoviBake.add(baka);
                    Osoba djed = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB oca"));
                    if (djed != null) djedoviBake.add(djed);
                });
        }

        // 3. KORAK: Za svakog djeda/baku pronađi NJIHOVE roditelje (PRADJEDOVE I PRABAKE)
        for (Osoba djedBaka : djedoviBake) {
            csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> djedBaka.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
                .forEach(rowMap -> {
                    // Ovo je sada ispravno PRABAKA
                    Osoba prabaka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB majke"));
                    if (prabaka != null) {
                        addRelationshipToResults(veze, targetOsoba, prabaka, "praunuk_praunuka_pradjed_prabaka",
                                "{target} je praunuče osobe {povezana}",
                                "Izvedena", "Uspravna", "3", processedRelations, null, false);
                    }
                    // Ovo je sada ispravno PRADJED
                    Osoba pradjed = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB oca"));
                    if (pradjed != null) {
                        addRelationshipToResults(veze, targetOsoba, pradjed, "praunuk_praunuka_pradjed_prabaka",
                                "{target} je praunuče osobe {povezana}",
                                "Izvedena", "Uspravna", "3", processedRelations, null, false);
                    }
                });
        }

        // Pronađi praunučad (djeca unučadi)
        List<Osoba> djecaTargeta = csvDataLoader.maticaRodenihData.stream()
            .filter(eMap -> targetOsoba.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB majke")) ||
                             targetOsoba.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB oca")))
            .map(eMap -> csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        for (Osoba dijete : djecaTargeta) {
            csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> dijete.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB majke")) ||
                                 dijete.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB oca")))
                .map(eMap -> csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
                .filter(Objects::nonNull)
                .forEach(unukUnuka -> {
                    csvDataLoader.maticaRodenihData.stream()
                        .filter(eMap -> unukUnuka.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB majke")) ||
                                         unukUnuka.getOib().equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB oca")))
                        .forEach(rowMap -> {
                            Osoba praunukPraunuka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB osobe"));
                            if (praunukPraunuka != null) {
                                addRelationshipToResults(veze, targetOsoba, praunukPraunuka, "pradjed_prabaka_praunuk_praunuka",
                                    "{target} je %s osobe {povezana}".formatted(getRelationshipDisplayTerm("pradjed_prabaka_praunuk_praunuka", targetOsoba.getSpol(), praunukPraunuka.getSpol(), null).split(" / ")[0]),
                                    "Izvedena", "Uspravna", "3", processedRelations, null, false);
                            }
                        });
                });
        }
    }

    public Map<String, Object> pronadjiSveVeze(String oib) {
        List<VezaDTO> veze = new ArrayList<>();
        List<String> queriesLog = new ArrayList<>();
        Set<List<String>> processedRelations = new HashSet<>();

        // 1. KORAK: Dohvati glavnu osobu
        Osoba targetOsoba = csvDataLoader.centralniPopisOsoba.get(oib);

        // 2. KORAK: Provjeri postoji li osoba. Ako ne, vrati poruku o grešci.
        if (targetOsoba == null) {
            queriesLog.add("OIB '" + oib + "' nije pronađen u centralnom popisu osoba.");
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("oib", oib);
            errorResult.put("error", "OIB nije pronađen.");
            errorResult.put("relationships", Collections.emptyList());
            errorResult.put("graph_nodes", Collections.emptyList());
            errorResult.put("graph_edges", Collections.emptyList());
            errorResult.put("queries_log", queriesLog);
            errorResult.put("mainPersonInfo", new HashMap<>()); // Vraćamo prazan objekt da se frontend ne sruši
            return errorResult;
        }

        // 3. KORAK: Ako osoba postoji, kreiraj 'mainPersonInfo' objekt
        Map<String, Object> mainPersonInfo = new HashMap<>();
                 mainPersonInfo.put("punoIme", targetOsoba.getPunoImeOrOib());
                 mainPersonInfo.put("spol", targetOsoba.getSpol());
                 // Provjeravamo jesu li datumi null prije dodavanja
                 mainPersonInfo.put("datumRodjenja", targetOsoba.getDatumRodjenja() != null ? targetOsoba.getDatumRodjenja() : null);
                 mainPersonInfo.put("datumSmrti", targetOsoba.getDatumSmrti() != null ? targetOsoba.getDatumSmrti() : null);

        // Nazivi datoteka - moraju odgovarati onima u meta_podaci.csv
        final String MR_FILENAME = "matica_rodjenih.csv";
        final String MV_FILENAME = "matica_vjenčanih.csv";
        final String RZP_FILENAME_REGISTAR = "registar_životnog_partnerstva.csv";
        final String RZP_FILENAME_IZJAVA = "izjava_o_životnom partnerstvu.csv"; // Iako nije korišteno u kodu, ostavljam zbog konzistentnosti s originalom
        final String IIZ_FILENAME = "izjava_o_izvanbračnoj_zajednici.csv";

        // === PRIMARNE VEZE ===
        queriesLog.add("--- START: Traženje primarnih veza za OIB: " + oib + " ---");
        // 1. Roditelji (targetOsoba je dijete)
        queriesLog.add("Traženje roditelja u '" + MR_FILENAME + "'...");
        for (Map<String, String> rowMap : csvDataLoader.maticaRodenihData) {
            String oibDjetetaIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB osobe");
            if (oib.equals(oibDjetetaIzRetka)) {
                String oibMajkeIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB majke");
                Osoba majka = csvDataLoader.centralniPopisOsoba.get(oibMajkeIzRetka);
                if (majka != null) addRelationshipToResults(veze, targetOsoba, majka, "dijete_roditelj", "{target} je dijete osobe {povezana}", "Izravna", "Uspravna", "1", processedRelations, null, false);

                String oibOcaIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB oca");
                Osoba otac = csvDataLoader.centralniPopisOsoba.get(oibOcaIzRetka);
                if (otac != null) addRelationshipToResults(veze, targetOsoba, otac, "dijete_roditelj", "{target} je dijete osobe {povezana}", "Izravna", "Uspravna", "1", processedRelations, null, false);
            }
        }

        // 2. Djeca (targetOsoba je roditelj)
        queriesLog.add("Traženje djece u '" + MR_FILENAME + "'...");
        for (Map<String, String> rowMap : csvDataLoader.maticaRodenihData) {
            String oibMajkeIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB majke");
            String oibOcaIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB oca");
            if (oib.equals(oibMajkeIzRetka) || oib.equals(oibOcaIzRetka)) {
                String oibDjetetaIzRetka = csvDataLoader.getFromRowUsingLogicalDesc(rowMap, MR_FILENAME, "OIB osobe");
                Osoba dijete = csvDataLoader.centralniPopisOsoba.get(oibDjetetaIzRetka);
                if (dijete != null) addRelationshipToResults(veze, targetOsoba, dijete, "roditelj_dijete", "{target} je roditelj osobe {povezana}", "Izravna", "Uspravna", "1", processedRelations, null, false);
            }
        }

        // 3. Partneri - Bračni drugovi
        queriesLog.add("Traženje bračnih drugova u '" + MV_FILENAME + "'...");
        for (Map<String, String> row : csvDataLoader.maticaVjencanihData) {
            String datumPrestanka = csvDataLoader.getFromRowUsingLogicalDesc(row, MV_FILENAME, "Prestanak braka - datum događaja").trim();
            if (datumPrestanka.isEmpty()) {
                String oibOsobe1 = csvDataLoader.getFromRowUsingLogicalDesc(row, MV_FILENAME, "OIB osobe").trim();
                String oibOsobe2 = csvDataLoader.getFromRowUsingLogicalDesc(row, MV_FILENAME, "OIB bračnog druga").trim();
                String partnerOib = null;
                if (oib.equals(oibOsobe1) && !oibOsobe2.isEmpty()) partnerOib = oibOsobe2;
                else if (oib.equals(oibOsobe2) && !oibOsobe1.isEmpty()) partnerOib = oibOsobe1;
                if (partnerOib != null && !partnerOib.isEmpty()) {
                    Osoba partner = csvDataLoader.centralniPopisOsoba.get(partnerOib);
                    if (partner != null) addRelationshipToResults(veze, targetOsoba, partner, "bracni_drug", "{target} je u braku s osobom {povezana}", "Izravna", "Partnerska", "N/A", processedRelations, null, true);
                }
            }
        }

        // 4. Životni partneri
        //queriesLog.add("Traženje životnih partnera u 'Registar živ. partnerstva / Izjave'...");
        for (Map<String, String> row : csvDataLoader.registarZivPartnerstvaData) {
            String datumPrestanka = csvDataLoader.getFromRowUsingLogicalDesc(row, RZP_FILENAME_REGISTAR, "Prestanak životnog partnerstva - datum događaja").trim();
            if (datumPrestanka.isEmpty()) {
                String oibP1 = csvDataLoader.getFromRowUsingLogicalDesc(row, RZP_FILENAME_REGISTAR, "OIB osobe").trim();
                String oibP2 = csvDataLoader.getFromRowUsingLogicalDesc(row, RZP_FILENAME_REGISTAR, "OIB životnog partnera").trim();
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
        //queriesLog.add("Traženje izvanbračnih partnera u '" + IIZ_FILENAME + "'...");
        for (Map<String, String> row : csvDataLoader.izjavaIzvanbrZajData) {
            String datumPrestanka = csvDataLoader.getFromRowUsingLogicalDesc(row, IIZ_FILENAME, "Prestanak izvanbračne zajednice - datum događaja").trim();
            if (datumPrestanka.isEmpty()) {
                String oibP1 = csvDataLoader.getFromRowUsingLogicalDesc(row, IIZ_FILENAME, "OIB osobe").trim();
                String oibP2 = csvDataLoader.getFromRowUsingLogicalDesc(row, IIZ_FILENAME, "OIB izvanbračnog druga").trim();
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
        Map<String, String> targetParentsRowMap = csvDataLoader.maticaRodenihData.stream()
            .filter(eMap -> oib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
            .findFirst().orElse(null);

        if (targetParentsRowMap != null) {
            Osoba majka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(targetParentsRowMap, MR_FILENAME, "OIB majke"));
            if (majka != null) roditeljiTargeta.add(majka);
            Osoba otac = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(targetParentsRowMap, MR_FILENAME, "OIB oca"));
            if (otac != null) roditeljiTargeta.add(otac);
        }
        final List<Osoba> finalRoditeljiTargeta = Collections.unmodifiableList(new ArrayList<>(roditeljiTargeta.stream().distinct().collect(Collectors.toList())));

        List<Osoba> djecaTargeta = csvDataLoader.maticaRodenihData.stream()
            .filter(eMap -> oib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB majke")) || oib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB oca")))
            .map(eMap -> csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());
        final List<Osoba> finalDjecaTargeta = Collections.unmodifiableList(new ArrayList<>(djecaTargeta));

        // 1. Djed/Baka
        for (Osoba roditelj : finalRoditeljiTargeta) {
            final String roditeljOib = roditelj.getOib();
            queriesLog.add("Traženje roditelja od " + roditelj.getPunoImeOrOib() + " (djedova/baka od " + oib + ")");
            csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> roditeljOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
                .forEach(entryMap -> {
                    Osoba baka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB majke"));
                    if (baka != null) addRelationshipToResults(veze, targetOsoba, baka, "unuk_unuka_djed_baka", "{target} je unuče osobe {povezana}", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                    Osoba djed = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB oca"));
                    if (djed != null) addRelationshipToResults(veze, targetOsoba, djed, "unuk_unuka_djed_baka", "{target} je unuče osobe {povezana}", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                });
        }

        // 2. Unuci
        for (Osoba dijete : finalDjecaTargeta) {
            final String dijeteOib = dijete.getOib();
            queriesLog.add("Traženje djece od " + dijete.getPunoImeOrOib() + " (unuka od " + oib + ")");
            csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> dijeteOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB majke")) || dijeteOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB oca")))
                .forEach(entryMap -> {
                    Osoba unukUnuka = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe"));
                    if (unukUnuka != null) addRelationshipToResults(veze, targetOsoba, unukUnuka, "djed_baka_unuk_unuka", "{target} je djed/baka osobe {povezana}", "Izvedena", "Uspravna", "2", processedRelations, null, false);
                });
        }

        // 3. Braća/Sestre
        List<Osoba> targetSiblings = new ArrayList<>();
        if (targetParentsRowMap != null) {
            final String oibMajkeT = csvDataLoader.getFromRowUsingLogicalDesc(targetParentsRowMap, MR_FILENAME, "OIB majke");
            final String oibOcaT = csvDataLoader.getFromRowUsingLogicalDesc(targetParentsRowMap, MR_FILENAME, "OIB oca");
            queriesLog.add("Traženje braće/sestara za OIB " + oib);
            csvDataLoader.maticaRodenihData.stream()
                .filter(entryMap -> {
                    String currentChildOib = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe");
                    return !oib.equals(currentChildOib) && currentChildOib != null && !currentChildOib.isEmpty();
                })
                .forEach(entryMap -> {
                    Osoba sibling = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe"));
                    if (sibling == null) return;

                    String entryOibMajke = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB majke");
                    String entryOibOca = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB oca");

                    boolean isFull = (oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entryOibMajke)) &&
                                     (oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entryOibOca));
                    boolean isHalfMother = (oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entryOibMajke)) &&
                                           (!(oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entryOibOca)) && (entryOibOca != null && !entryOibOca.isEmpty()));
                    boolean isHalfFather = (oibOcaT != null && !oibOcaT.isEmpty() && oibOcaT.equals(entryOibOca)) &&
                                           (!(oibMajkeT != null && !oibMajkeT.isEmpty() && oibMajkeT.equals(entryOibMajke)) && (entryOibMajke != null && !entryOibMajke.isEmpty()));

                    String opis = "";
                    String tipSrodstvaPrikaz = "";
                    String vrstaVezeKljuc = "brat_sestra";
                    if (isFull) {
                        opis = "{target} i {povezana} su braća/sestre";
                        tipSrodstvaPrikaz = "";
                    } else if (isHalfMother) {
                        opis = "{target} i {povezana} su polubraća/sestre po majci";
                        tipSrodstvaPrikaz = " (polu- po majci)";
                        vrstaVezeKljuc = "polubrat_polusestra";
                    } else if (isHalfFather) {
                        opis = "{target} i {povezana} su polubraća/sestre po ocu";
                        tipSrodstvaPrikaz = " (polu- po ocu)";
                        vrstaVezeKljuc = "polubrat_polusestra";
                    }

                    // ... unutar 'forEach' petlje za braću/sestre ...

                    if (!opis.isEmpty()) { // 'opis' varijabla se sada koristi samo da znamo da smo pronašli vezu

                        // Pozivamo standardnu helper metodu umjesto ručnog stvaranja DTO objekta.
                        // Ona će interno pozvati 'refineDescription' i ispravno formatirati opis.
                        boolean added = addRelationshipToResults(
                            veze,
                            targetOsoba,
                            sibling,
                            vrstaVezeKljuc, // Ovo će biti "brat_sestra" ili "polubrat_polusestra"
                            "{target} i {povezana}", // Opis format, 'refineDescription' će ga preformatirati
                            "Izvedena",
                            "Pobočna",
                            "2",
                            processedRelations,
                            null,
                            true // Veza je simetrična
                        );

                        if (added) {
                            targetSiblings.add(sibling);
                        }
                    }
                });
        }
        final List<Osoba> finalTargetSiblings = Collections.unmodifiableList(new ArrayList<>(targetSiblings.stream().distinct().collect(Collectors.toList())));

        // 4. Stričevi/Tetke/Ujaci
        List<Osoba> unclesAuntsList = new ArrayList<>();
        for (Osoba parentOfTarget : finalRoditeljiTargeta) {
            final String parentOfTargetSpol = parentOfTarget.getSpol();
            final String parentOfTargetOib = parentOfTarget.getOib();

            Map<String, String> grandparentsRowMap = csvDataLoader.maticaRodenihData.stream()
                .filter(eMap -> parentOfTargetOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(eMap, MR_FILENAME, "OIB osobe")))
                .findFirst().orElse(null);

            if (grandparentsRowMap != null) {
                final String gpMajkaOib = csvDataLoader.getFromRowUsingLogicalDesc(grandparentsRowMap, MR_FILENAME, "OIB majke");
                final String gpOtacOib = csvDataLoader.getFromRowUsingLogicalDesc(grandparentsRowMap, MR_FILENAME, "OIB oca");
                queriesLog.add("Traženje braće/sestara roditelja " + parentOfTarget.getPunoImeOrOib() + " (stričevi/tetke/ujaci od " + oib + ")");
                csvDataLoader.maticaRodenihData.stream()
                    .filter(entryMap -> {
                        String currentChildOib = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe");
                        return !parentOfTargetOib.equals(currentChildOib) && currentChildOib != null && !currentChildOib.isEmpty();
                    })
                    .forEach(entryMap -> {
                        String entryOibMajke = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB majke");
                        String entryOibOca = csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB oca");
                        boolean isParentFullSibling = (gpMajkaOib != null && !gpMajkaOib.isEmpty() && gpMajkaOib.equals(entryOibMajke)) &&
                                                      (gpOtacOib != null && !gpOtacOib.isEmpty() && gpOtacOib.equals(entryOibOca));
                        if (isParentFullSibling) {
                            Osoba uncleAunt = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe"));
                            if (uncleAunt != null) {
                                if (addRelationshipToResults(veze, targetOsoba, uncleAunt, "necak_necakinja_stric_tetka_ujak", "{target} je nećak/inja osobi {povezana}", "Izvedena", "Pobočna", "3", processedRelations, parentOfTargetSpol, false)) {
                                    unclesAuntsList.add(uncleAunt);
                                }
                            }
                        }
                    });
            }
        }
        final List<Osoba> finalUnclesAuntsList = Collections.unmodifiableList(new ArrayList<>(unclesAuntsList.stream().distinct().collect(Collectors.toList())));

        // 5. Nećaci/Nećakinje
        for (Osoba siblingOfTarget : finalTargetSiblings) {
            final String siblingOfTargetSpol = siblingOfTarget.getSpol();
            final String siblingOfTargetOib = siblingOfTarget.getOib();
            queriesLog.add("Traženje djece od " + siblingOfTarget.getPunoImeOrOib() + " (nećaka/nećakinja od " + oib + ")");
            csvDataLoader.maticaRodenihData.stream()
                .filter(entryMap -> siblingOfTargetOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB majke")) || siblingOfTargetOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB oca")))
                .forEach(entryMap -> {
                    Osoba nephewNiece = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe"));
                    if (nephewNiece != null) {
                        addRelationshipToResults(veze, targetOsoba, nephewNiece, "stric_tetka_ujak_necak_necakinja", "{target} je stric/tetka/ujak osobi {povezana}", "Izvedena", "Pobočna", "3", processedRelations, siblingOfTargetSpol, false);
                    }
                });
        }

        // 6. Bratići/Sestrične
        for (Osoba uncleAunt : finalUnclesAuntsList) {
            final String uncleAuntOib = uncleAunt.getOib();
            queriesLog.add("Traženje djece od " + uncleAunt.getPunoImeOrOib() + " (bratića/sestrični od " + oib + ")");
            csvDataLoader.maticaRodenihData.stream()
                .filter(entryMap -> uncleAuntOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB majke")) || uncleAuntOib.equals(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB oca")))
                .forEach(entryMap -> {
                    Osoba cousin = csvDataLoader.centralniPopisOsoba.get(csvDataLoader.getFromRowUsingLogicalDesc(entryMap, MR_FILENAME, "OIB osobe"));
                    if (cousin != null) {
                        addRelationshipToResults(veze, targetOsoba, cousin, "bratic_sestricna", "{target} i {povezana} su bratići/sestrične", "Izvedena", "Pobočna", "3", processedRelations, null, true);
                    }
                });
        }

        // 7. Great-grandparent/grandchild relationships
        queriesLog.add("Traženje pradjeda/prabake i praunuka/praunuke za OIB " + oib);
        checkGreatGrandRelationships(targetOsoba, veze, processedRelations);

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
Map<String, Object> finalResult = new HashMap<>();
        finalResult.put("oib", oib);
        finalResult.put("mainPersonInfo", mainPersonInfo); // <-- Dodajemo novi objekt ovdje!
        finalResult.put("relationships", veze);
        finalResult.put("graph_nodes", graphNodes);
        finalResult.put("graph_edges", graphEdges);
        finalResult.put("queries_log", queriesLog);

        return finalResult;
    }
}