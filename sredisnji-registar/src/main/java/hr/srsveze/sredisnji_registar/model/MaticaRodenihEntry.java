package hr.srsveze.sredisnji_registar.model;

public class MaticaRodenihEntry {
    private String oibDjeteta;
    private String imeDjeteta;
    private String prezimeDjeteta;
    private String spolDjeteta;
    private String datumRodjenjaDjeteta;
    private String oibMajke;
    private String imeMajke;
    private String prezimeMajke;
    private String oibOca;
    private String imeOca;
    private String prezimeOca;

    // Konstruktor
    public MaticaRodenihEntry(String oibDjeteta, String imeDjeteta, String prezimeDjeteta,
                              String spolDjeteta, String datumRodjenjaDjeteta,
                              String oibMajke, String imeMajke, String prezimeMajke,
                              String oibOca, String imeOca, String prezimeOca) {
        this.oibDjeteta = oibDjeteta != null ? oibDjeteta.trim() : "";
        this.imeDjeteta = imeDjeteta != null ? imeDjeteta.trim() : "";
        this.prezimeDjeteta = prezimeDjeteta != null ? prezimeDjeteta.trim() : "";
        this.spolDjeteta = spolDjeteta != null ? spolDjeteta.trim() : "";
        this.datumRodjenjaDjeteta = datumRodjenjaDjeteta != null ? datumRodjenjaDjeteta.trim() : "";
        this.oibMajke = oibMajke != null ? oibMajke.trim() : "";
        this.imeMajke = imeMajke != null ? imeMajke.trim() : "";
        this.prezimeMajke = prezimeMajke != null ? prezimeMajke.trim() : "";
        this.oibOca = oibOca != null ? oibOca.trim() : "";
        this.imeOca = imeOca != null ? imeOca.trim() : "";
        this.prezimeOca = prezimeOca != null ? prezimeOca.trim() : "";
    }

    // Getteri
    public String getOibDjeteta() { return oibDjeteta; }
    public String getImeDjeteta() { return imeDjeteta; }
    public String getPrezimeDjeteta() { return prezimeDjeteta; }
    public String getSpolDjeteta() { return spolDjeteta; }
    public String getDatumRodjenjaDjeteta() { return datumRodjenjaDjeteta; }
    public String getOibMajke() { return oibMajke; }
    public String getImeMajke() { return imeMajke; }
    public String getPrezimeMajke() { return prezimeMajke; }
    public String getOibOca() { return oibOca; }
    public String getImeOca() { return imeOca; }
    public String getPrezimeOca() { return prezimeOca; }
}