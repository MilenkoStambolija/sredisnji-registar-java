# Središnji Registar Stanovništva - Demo Aplikacija

## O Projektu

Zakonski osnova: Zakon o Središnjem registru stanovništva
https://narodne-novine.nn.hr/clanci/sluzbeni/2025_04_67_856.html


Ova aplikacija je testni/demo projekt koji simulira funkcionalnosti Središnjeg registra stanovništva. Razvijena je kao web aplikacija s Java Spring Boot backendom i jednostavnim HTML/JavaScript frontendom. Aplikacija omogućuje učitavanje podataka o osobama i njihovim vezama iz CSV datoteka, te pretragu i prikaz tih veza za uneseni OIB.

Glavne funkcionalnosti uključuju:
* Učitavanje podataka o osobama i vezama iz više CSV datoteka.
* Traženi OIB se povlači iz OIB baze (OIB_baza.csv).
* Pronalaženje i prikaz primarnih krvnih veza (roditelji, djeca).
* Pronalaženje i prikaz partnerskih veza (bračni drugovi, životni partneri, izvanbračni partneri).
* Pronalaženje i prikaz izvedenih krvnih veza (djedovi/bake, unuci, braća/sestre - puni i polu, stričevi/tetke/ujaci, nećaci/nećakinje, bratići/sestrične).
* Tablični i grafički prikaz pronađenih veza.
* Prikaz loga "simuliranih upita" za bolje razumijevanje procesa.
* Napredni filter za tablicu veza
* Mogućnost izvoza tablice veza u Excel datoteku i csv datoteku

## Tehnologije

* **Backend:** Java, Spring Boot
    * Spring Web (za REST API)
    * Apache Commons CSV (za parsiranje CSV datoteka)
* **Frontend:** HTML, JavaScript (vanilla), Tailwind CSS (preko CDN-a), Vis.js (za grafički prikaz)
* **Build Alat:** Apache Maven
* **Izvor Podataka:** CSV datoteke

## Postavljanje i Pokretanje Projekta

### Preduvjeti
* Java Development Kit (JDK) verzija 17 ili novija.
* Apache Maven (iako projekt koristi Maven Wrapper, pa globalna instalacija nije strogo nužna).
* Git (za kloniranje repozitorija).

### Koraci za Pokretanje
1.  **Klonirajte repozitorij:**
    ```bash
    git clone [https://github.com/MilenkoStambolija/sredisnji-registar-java.git](https://github.com/MilenkoStambolija/sredisnji-registar-java.git)
    cd sredisnji-registar-java
    ```
2.  **Pripremite CSV datoteke:**
    * Unutar korijenskog direktorija projekta, kreirajte folder pod nazivom `DATA`.
    * U `DATA` folder kopirajte sve potrebne CSV datoteke:
        * `OIB_baza.csv` (primarni izvor OIB-ova, imena i prezimena)
        * `matica_rodjenih.csv`
        * `registar_stranaca.csv`
        * `matica_vjenčanih.csv`
        * `matica_umrlih.csv`
        * `registar_životnog_partnerstva.csv`
        * `izjava_o_životnom partnerstvu.csv`
        * `izjava_o_izvanbračnoj_zajednici.csv`
        * `meta_podaci.csv` (datoteka koja opisuje strukturu ostalih CSV datoteka)
    * **Važno:** Osigurajte da nazivi datoteka i nazivi stupaca unutar `meta_podaci.csv` (posebno stupci "Naziv datoteke", "Naziv stupca" i "Logički opis polja") točno odgovaraju stvarnim nazivima datoteka i njihovim headerima, te onome što se očekuje u Java kodu (`CsvDataLoader.java` i `VezaService.java`).

3.  **Pokrenite aplikaciju koristeći Maven Wrapper:**
    * Iz korijenskog direktorija projekta, u terminalu/naredbenom retku:
      * Na Windowsima:
        ```powershell
        .\mvnw.cmd spring-boot:run
        ```
      * Na Linuxu/macOS-u:
        ```bash
        ./mvnw spring-boot:run
        ```
4.  **Pristupite aplikaciji:**
    * Otvorite web preglednik i idite na adresu: `http://localhost:8080`

## Struktura Projekta

* `src/main/java/hr/srsveze/sredisnji_registar/`: Glavni Java izvorni kod.
    * `model/`: Java klase koje predstavljaju podatkovne modele (npr. `Osoba.java`, `VezaDTO.java`).
    * `service/`: Servisne klase koje sadrže poslovnu logiku (npr. `CsvDataLoader.java`, `VezaService.java`).
    * `controller/`: Spring MVC kontroleri koji obrađuju web zahtjeve (npr. `VezaController.java`).
    * `SredisnjiRegistarApplication.java`: Glavna klasa za pokretanje Spring Boot aplikacije.
* `src/main/resources/`: Resursi aplikacije.
    * `static/index.html`: Frontend HTML datoteka s JavaScriptom za korisničko sučelje.
    * `application.properties`: Konfiguracijska datoteka Spring Boota (trenutno vjerojatno prazna ili s osnovnim postavkama).
* `DATA/`: Direktorij gdje trebate smjestiti sve CSV datoteke, uključujući `meta_podaci.csv`. **Ovaj direktorij nije dio Git repozitorija i treba ga ručno kreirati i popuniti.**
* `pom.xml`: Maven konfiguracijska datoteka koja definira ovisnosti projekta i build postavke.

## Korištenje Aplikacije

1.  Nakon pokretanja, otvorite `http://localhost:8080` u pregledniku.
2.  Unesite OIB (11 znamenki) osobe za koju želite vidjeti veze.
3.  Kliknite na gumb "Pretraži".
4.  Rezultati će biti prikazani u tri taba:
    * **Tablični prikaz:** Lista svih pronađenih veza.
    * **Grafički prikaz:** Vizualizacija veza pomoću Vis.js.
    * **Opis Operacija / Upiti:** Log "simuliranih upita" koje je backend izvršio.

## Metapodaci (`meta_podaci.csv`)

Aplikacija koristi `meta_podaci.csv` datoteku za dinamičko mapiranje naziva stupaca iz izvornih CSV datoteka. Ključni stupci u `meta_podaci.csv` su:
* `Naziv datoteke`: Točan naziv CSV datoteke.
* `Naziv stupca`: Točan naziv headera (stupca) kako se pojavljuje u CSV datoteci.
* `Logički opis polja`: Jedinstveni, konzistentni string koji Java kod koristi kao interni ključ za dohvaćanje stvarnog "Naziva stupca" za određenu datoteku i podatak. **Svi "Logički opisi polja" koji se koriste u Java kodu (`CsvDataLoader.java` i `VezaService.java`) moraju točno odgovarati vrijednostima u ovom stupcu vaše `meta_podaci.csv` datoteke.**

## Buduća Poboljšanja (Prijedlozi)
* Implementacija perzistencije podataka korištenjem prave baze podataka (npr. H2, PostgreSQL).
* Optimizacija dohvaćanja veza korištenjem SQL upita.
* Dodavanje naprednijih filtera i sortiranja za prikaz veza.
* Proširenje skupa izvedenih veza.
* Korisnička autentifikacija i autorizacija.
* Poboljšanje UI/UX.

---