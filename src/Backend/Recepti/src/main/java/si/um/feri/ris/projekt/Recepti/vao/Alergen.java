package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entiteta, ki predstavlja alergen (npr. gluten, oreški).
 */
@Data
@NoArgsConstructor
@Entity
public class Alergen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String ime;

    private String opis;

    @ManyToMany(mappedBy = "alergeni")
    @JsonIgnore
    private List<Jedilnik> jedilniki = new ArrayList<>();

    public Alergen(String ime) {
        this.ime = ime;
    }

    public Alergen(String ime, String opis) {
        this.ime = ime;
        this.opis = opis;
    }
}

