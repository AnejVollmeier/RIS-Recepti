package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import si.um.feri.ris.projekt.Recepti.service.KolicinaService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
public class Recepti {

    public Recepti(int id, String ime, String opis) {
        this.id = id;
        this.ime = ime;
        this.opis = opis;
        this.steviloPorcij = 1;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String ime;
    private String opis;

    private int steviloPorcij = 1; // število porcij za recept

    private String navodila; // koraki priprave
    private String slikaUrl; // URL slike recepta

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Avtor recepta
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "avtor_id")
    @JsonIgnoreProperties({ "recepti", "komentarji", "ocene", "vsecki", "geslo" })
    private Uporabnik avtor;

    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "zaporedje")
    @JsonManagedReference
    private List<Sestavine> sestavine = new ArrayList<>();

    // Komentarji na receptu
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Komentar> komentarji = new ArrayList<>();

    // Ocene recepta
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Ocena> ocene = new ArrayList<>();

    // Všečki recepta
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Vsecek> vsecki = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addSestavina(Sestavine s) {
        s.setRecept(this);
        this.sestavine.add(s);
    }

    public void setSestavine(List<Sestavine> nove) {
        this.sestavine.clear();
        if (nove != null) {
            for (Sestavine s : nove)
                addSestavina(s);
        }
    }

    public void addKomentar(Komentar k) {
        k.setRecept(this);
        this.komentarji.add(k);
    }

    public void addOcena(Ocena o) {
        o.setRecept(this);
        this.ocene.add(o);
    }

    public void addVsecek(Vsecek v) {
        v.setRecept(this);
        this.vsecki.add(v);
    }

    /**
     * Izračuna povprečno oceno recepta.
     */
    @Transient
    public Double getPovprecnaOcena() {
        if (ocene == null || ocene.isEmpty()) {
            return null;
        }
        return ocene.stream()
                .mapToInt(Ocena::getVrednost)
                .average()
                .orElse(0.0);
    }

    /**
     * Vrne število všečkov.
     */
    @Transient
    public int getSteviloVseckov() {
        return vsecki != null ? vsecki.size() : 0;
    }

    /**
     * Izračuna sestavine za določeno število porcij.
     * 
     * @param zelenoPorcije   Število porcij, za katere želimo izračunati sestavine
     * @param kolicinaService Service za pretvorbo količin
     * @return Seznam sestavin s preračunanimi količinami
     */
    @Transient
    public List<SestavinaDto> getSestavineZaPorcije(int zelenoPorcije, KolicinaService kolicinaService) {
        if (sestavine == null || sestavine.isEmpty()) {
            return new ArrayList<>();
        }

        List<SestavinaDto> rezultat = new ArrayList<>();

        for (Sestavine sestavina : sestavine) {
            String novaKolicina = kolicinaService.izracunajNovoKolicino(
                    sestavina.getKolicina(),
                    this.steviloPorcij,
                    zelenoPorcije);

            rezultat.add(new SestavinaDto(
                    sestavina.getNaziv(),
                    novaKolicina,
                    sestavina.getKolicina() // originalna količina
            ));
        }

        return rezultat;
    }

    /**
     * DTO za sestavino s preračunano količino.
     */
    public static class SestavinaDto {
        public String naziv;
        public String kolicina;
        public String originalaKolicina;

        public SestavinaDto(String naziv, String kolicina, String originalaKolicina) {
            this.naziv = naziv;
            this.kolicina = kolicina;
            this.originalaKolicina = originalaKolicina;
        }

        // Getters in Setters za JSON serializacijo
        public String getNaziv() {
            return naziv;
        }

        public void setNaziv(String naziv) {
            this.naziv = naziv;
        }

        public String getKolicina() {
            return kolicina;
        }

        public void setKolicina(String kolicina) {
            this.kolicina = kolicina;
        }

        public String getOriginalaKolicina() {
            return originalaKolicina;
        }

        public void setOriginalaKolicina(String originalaKolicina) {
            this.originalaKolicina = originalaKolicina;
        }
    }
}