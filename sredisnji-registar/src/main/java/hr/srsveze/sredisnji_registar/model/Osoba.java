package hr.srsveze.sredisnji_registar.model;

import java.util.Objects;

public class Osoba {
    private String oib;
    private String ime;
    private String prezime;
    private String spol;
    private String datumRodjenja;
    private String datumSmrti;

    public Osoba() {} // Prazan konstruktor je važan za neke Java okvire

    public Osoba(String oib, String ime, String prezime, String spol, String datumRodjenja, String datumSmrti) {
        this.oib = oib != null ? oib.trim() : ""; // Osiguraj da nije null, koristi prazan string ako jest
        this.ime = ime != null ? ime.trim() : "";
        this.prezime = prezime != null ? prezime.trim() : "";
        this.spol = spol != null ? spol.trim().toUpperCase() : ""; // Normaliziraj spol i osiguraj da nije null
        this.datumRodjenja = datumRodjenja != null ? datumRodjenja.trim() : "";
        this.datumSmrti = datumSmrti != null ? datumSmrti.trim() : "";
    }

    // Getteri
    public String getOib() { return oib; }
    public String getIme() { return ime; }
    public String getPrezime() { return prezime; }
    public String getSpol() { return spol; }
    public String getDatumRodjenja() { return datumRodjenja; }
    public String getDatumSmrti() { return datumSmrti; }

    // Setteri
    public void setOib(String oib) { this.oib = oib != null ? oib.trim() : ""; }
    public void setIme(String ime) { this.ime = ime != null ? ime.trim() : ""; }
    public void setPrezime(String prezime) { this.prezime = prezime != null ? prezime.trim() : ""; }
    public void setSpol(String spol) { this.spol = spol != null ? spol.trim().toUpperCase() : ""; }
    public void setDatumRodjenja(String datumRodjenja) { this.datumRodjenja = datumRodjenja != null ? datumRodjenja.trim() : ""; }
    public void setDatumSmrti(String datumSmrti) { this.datumSmrti = datumSmrti != null ? datumSmrti.trim() : ""; }

    public String getPunoIme() {
        String punoImeStr = ((this.ime != null ? this.ime : "") + " " + (this.prezime != null ? this.prezime : "")).trim();
        // Vraća null ako je ime i prezime prazno, inače spojeno ime i prezime.
        // Ovo je korisno da se izbjegne vraćanje samo praznog stringa ako su oba polja prazna.
        return punoImeStr.isEmpty() ? null : punoImeStr;
    }

    public String getPunoImeOrOib() {
        String punoIme = getPunoIme();
        // Ako je punoIme null ili prazno, vrati OIB. Ako je i OIB null/prazan, vrati "Nepoznato".
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
        // Osobe su jednake ako imaju isti OIB (koji nije null ili prazan)
        return oib != null && !oib.isEmpty() && oib.equals(osoba.oib);
    }

    @Override
    public int hashCode() {
        // Hash code se temelji na OIB-u ako postoji
        return Objects.hash(oib != null && !oib.isEmpty() ? oib : System.identityHashCode(this));
    }

    @Override
    public String toString() {
        return "Osoba{oib='" + (oib != null ? oib : "N/A") +
               "', ime='" + (ime != null ? ime : "N/A") +
               "', prezime='" + (prezime != null ? prezime : "N/A") + "'}";
    }
}