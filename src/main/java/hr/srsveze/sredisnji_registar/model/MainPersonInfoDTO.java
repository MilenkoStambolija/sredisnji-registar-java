package hr.srsveze.sredisnji_registar.model; // Prilagodite paket (package) vašoj strukturi projekta

import com.fasterxml.jackson.annotation.JsonInclude;

// Anotacija osigurava da se null polja neće prikazati u konačnom JSON-u
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MainPersonInfoDTO {

    private String punoIme;
    private String spol;
    private String datumRodjenja;
    private String datumSmrti;

    // Konstruktori
    public MainPersonInfoDTO() {
    }

    public MainPersonInfoDTO(String punoIme, String spol, String datumRodjenja, String datumSmrti) {
        this.punoIme = punoIme;
        this.spol = spol;
        this.datumRodjenja = datumRodjenja;
        this.datumSmrti = datumSmrti;
    }

    // Getteri i Setteri
    public String getPunoIme() {
        return punoIme;
    }

    public void setPunoIme(String punoIme) {
        this.punoIme = punoIme;
    }

    public String getSpol() {
        return spol;
    }

    public void setSpol(String spol) {
        this.spol = spol;
    }

    public String getDatumRodjenja() {
        return datumRodjenja;
    }

    public void setDatumRodjenja(String datumRodjenja) {
        this.datumRodjenja = datumRodjenja;
    }

    public String getDatumSmrti() {
        return datumSmrti;
    }

    public void setDatumSmrti(String datumSmrti) {
        this.datumSmrti = datumSmrti;
    }
}