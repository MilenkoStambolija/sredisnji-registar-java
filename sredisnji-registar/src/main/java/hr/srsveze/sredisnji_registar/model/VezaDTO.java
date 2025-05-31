package hr.srsveze.sredisnji_registar.model;

public class VezaDTO {
    private String oibPovezaneOsobe;
    private String imePrezimePovezaneOsobe;
    private String vrstaVezePrikaz;
    private String opis;
    private String tipVeze; // Npr. "Izravna", "Izvedena"
    private String linija;  // Npr. "Uspravna", "Pobočna", "Partnerska"
    private String stupanj; // Npr. "1", "2", "N/A"

    // Konstruktor
    public VezaDTO(String oibPovezaneOsobe, String imePrezimePovezaneOsobe, String vrstaVezePrikaz,
                   String opis, String tipVeze, String linija, String stupanj) {
        this.oibPovezaneOsobe = oibPovezaneOsobe;
        this.imePrezimePovezaneOsobe = imePrezimePovezaneOsobe;
        this.vrstaVezePrikaz = vrstaVezePrikaz;
        this.opis = opis;
        this.tipVeze = tipVeze;
        this.linija = linija;
        this.stupanj = stupanj;
    }

    // Getteri - nužni za Jackson JSON serijalizaciju (koju Spring koristi)
    public String getOibPovezaneOsobe() { return oibPovezaneOsobe; }
    public String getImePrezimePovezaneOsobe() { return imePrezimePovezaneOsobe; }
    public String getVrstaVezePrikaz() { return vrstaVezePrikaz; }
    public String getOpis() { return opis; }
    public String getTipVeze() { return tipVeze; }
    public String getLinija() { return linija; }
    public String getStupanj() { return stupanj; }
}