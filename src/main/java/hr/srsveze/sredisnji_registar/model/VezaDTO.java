// src/main/java/hr/srsveze/sredisnji_registar/model/VezaDTO.java
package hr.srsveze.sredisnji_registar.model;

public class VezaDTO {
    public String oibPovezaneOsobe;
    public String imePrezimePovezaneOsobe;
    public String vrstaVezePrikaz;
    public String opis;
    public String tipVeze;
    public String linija;
    public String stupanj;

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

    public String getOibPovezaneOsobe() { return oibPovezaneOsobe; }
    public String getImePrezimePovezaneOsobe() { return imePrezimePovezaneOsobe; }
    public String getVrstaVezePrikaz() { return vrstaVezePrikaz; }
    public String getOpis() { return opis; }
    public String getTipVeze() { return tipVeze; }
    public String getLinija() { return linija; }
    public String getStupanj() { return stupanj; }
}