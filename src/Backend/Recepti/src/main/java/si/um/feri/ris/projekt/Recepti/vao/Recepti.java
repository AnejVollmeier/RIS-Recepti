package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String ime;
    private String opis;
    
    private String navodila; // koraki priprave
    private String slikaUrl; // URL slike recepta

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Avtor recepta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avtor_id")
    @JsonIgnoreProperties({"recepti", "komentarji", "ocene", "vsecki", "geslo"})
    private Uporabnik avtor;

    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "zaporedje")
    @JsonManagedReference
    private List<Sestavine> sestavine = new ArrayList<>();

    // Komentarji na receptu
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("recept-komentarji")
    private List<Komentar> komentarji = new ArrayList<>();

    // Ocene recepta
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("recept-ocene")
    private List<Ocena> ocene = new ArrayList<>();

    // Všečki recepta
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("recept-vsecki")
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
            for (Sestavine s : nove) addSestavina(s);
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
}
