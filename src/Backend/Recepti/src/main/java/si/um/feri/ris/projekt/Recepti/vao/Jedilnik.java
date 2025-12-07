package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entiteta, ki predstavlja jedilnik (zbirko receptov za določen datum/obrok).
 */
@Data
@NoArgsConstructor
@Entity
public class Jedilnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;

    @Column(nullable = false)
    private LocalDate datum;

    private int steviloOseb;

    @ManyToMany
    @JoinTable(
        name = "jedilnik_recepti",
        joinColumns = @JoinColumn(name = "jedilnik_id"),
        inverseJoinColumns = @JoinColumn(name = "recept_id")
    )
    @JsonIgnoreProperties({"komentarji", "ocene", "vsecki", "sestavine"})
    private List<Recepti> recepti = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "jedilnik_alergeni",
        joinColumns = @JoinColumn(name = "jedilnik_id"),
        inverseJoinColumns = @JoinColumn(name = "alergen_id")
    )
    @JsonIgnoreProperties({"jedilniki"})
    private List<Alergen> alergeni = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uporabnik_id")
    @JsonIgnoreProperties({"recepti", "komentarji", "ocene", "vsecki", "geslo"})
    private Uporabnik uporabnik;

    public Jedilnik(String naziv, LocalDate datum, int steviloOseb) {
        this.naziv = naziv;
        this.datum = datum;
        this.steviloOseb = steviloOseb;
    }

    /**
     * Doda recept v jedilnik.
     */
    public void addRecept(Recepti recept) {
        if (recept == null) return;
        // Avoid calling contains() which uses equals/hashCode on entities (Lombok @Data can include relations)
        boolean exists = this.recepti.stream().anyMatch(r -> r.getId() == recept.getId());
        if (!exists) {
            this.recepti.add(recept);
        }
    }

    /**
     * Odstrani recept iz jedilnika.
     */
    public void removeRecept(int receptId) {
        this.recepti.removeIf(r -> r.getId() == receptId);
    }

    /**
     * Doda alergen v jedilnik.
     */
    public void addAlergen(Alergen alergen) {
        if (alergen == null) return;
        boolean exists = this.alergeni.stream().anyMatch(a -> a.getId().equals(alergen.getId()));
        if (!exists) {
            this.alergeni.add(alergen);
        }
    }
}
