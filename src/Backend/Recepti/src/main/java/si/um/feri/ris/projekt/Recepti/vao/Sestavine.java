package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
@JsonPropertyOrder({"idSestavine","naziv","kolicina"})
@Data
@NoArgsConstructor
@Entity
public class Sestavine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private int id;

    private String naziv;
    private String kolicina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    @JsonBackReference
    private Recepti recept;

    public Sestavine(String naziv, String kolicina) {
        this.naziv = naziv;
        this.kolicina = kolicina;
    }

    @Transient
    @JsonProperty("idSestavine")
    public int getIdSestavine() {
        if (recept == null || recept.getSestavine() == null) return 0;
        int pos = recept.getSestavine().indexOf(this);
        return pos >= 0 ? pos + 1 : 0;
    }
}
