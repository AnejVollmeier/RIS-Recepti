package si.um.feri. ris.projekt.Recepti. vao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"id","naziv","kolicina","kalorije","mascobe","proteini","ogljikoviHidrati","hranilneNajdene"})
@Data
@NoArgsConstructor
@Entity
public class Sestavine {

    @Id
    @GeneratedValue(strategy = GenerationType. IDENTITY)
    private int id;

    private String naziv;
    private String kolicina;

    // ========== HRANILNE VREDNOSTI ==========

    private Double kalorije;

    private Double mascobe;

    private Double proteini;

    @Column(name = "ogljikovi_hidrati")
    private Double ogljikoviHidrati;

    @Column(name = "hranilne_najdene")
    private Boolean hranilneNajdene = false;

    // =========================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    @JsonBackReference
    private Recepti recept;

    public Sestavine(String naziv, String kolicina) {
        this.naziv = naziv;
        this.kolicina = kolicina;
        this.hranilneNajdene = false;
    }
}