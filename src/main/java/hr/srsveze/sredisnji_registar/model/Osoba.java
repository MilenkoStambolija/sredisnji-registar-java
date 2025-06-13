// src/main/java/hr/srsveze/sredisnji_registar/model/Osoba.java
package hr.srsveze.sredisnji_registar.model;

import java.util.Objects;
import java.util.Arrays; // Dodajte ovaj import ako ga nema

public class Osoba {
    public String oib;
    public String ime;
    public String prezime;
    public String spol;
    public String datumRodjenja;
    public String datumSmrti;

    public Osoba() {}

    public Osoba(String oib, String ime, String prezime, String spol, String datumRodjenja, String datumSmrti) {
        this.oib = oib != null ? oib.trim() : "";
        this.ime = ime != null ? ime.trim() : "";
        this.prezime = prezime != null ? prezime.trim() : "";
        this.spol = spol != null ? spol.trim().toUpperCase() : "";
        this.datumRodjenja = datumRodjenja != null ? datumRodjenja.trim() : "";
        this.datumSmrti = datumSmrti != null ? datumSmrti.trim() : "";
    }

    public String getOib() { return oib; }
    public void setOib(String oib) { this.oib = oib != null ? oib.trim() : ""; }
    public String getIme() { return ime; }
    public void setIme(String ime) { this.ime = ime != null ? ime.trim() : ""; }
    public String getPrezime() { return prezime; }
    public void setPrezime(String prezime) { this.prezime = prezime != null ? prezime.trim() : ""; }
    public String getSpol() { return spol; }
    public void setSpol(String spol) { this.spol = spol != null ? spol.trim().toUpperCase() : ""; }
    public String getDatumRodjenja() { return datumRodjenja; }
    public void setDatumRodjenja(String datumRodjenja) { this.datumRodjenja = datumRodjenja != null ? datumRodjenja.trim() : ""; }
    public String getDatumSmrti() { return datumSmrti; }
    public void setDatumSmrti(String datumSmrti) { this.datumSmrti = datumSmrti != null ? datumSmrti.trim() : ""; }

    public String getPunoIme() {
        String punoImeStr = ((this.ime != null ? this.ime : "") + " " + (this.prezime != null ? this.prezime : "")).trim();
        return punoImeStr.isEmpty() ? null : punoImeStr;
    }

    public String getPunoImeOrOib() {
        String punoIme = getPunoIme();
        if (punoIme != null && !punoIme.isEmpty()) {
            return punoIme;
        } else if (oib != null && !oib.isEmpty()) {
            return oib;
        } else {
            return "Nepoznato";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Osoba osoba = (Osoba) o;
        return oib != null && !oib.isEmpty() && oib.equals(osoba.oib);
    }

    @Override
    public int hashCode() {
        return Objects.hash(oib != null && !oib.isEmpty() ? oib : System.identityHashCode(this));
    }

    @Override
    public String toString() {
        return "Osoba{oib='" + (oib != null ? oib : "N/A") +
               "', ime='" + (ime != null ? ime : "N/A") +
               "', prezime='" + (prezime != null ? prezime : "N/A") + "'}";
    }
}