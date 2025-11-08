package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @OneToMany(mappedBy = "recept", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderColumn(name = "zaporedje")
    @JsonManagedReference
    private List<Sestavine> sestavine = new ArrayList<>();

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
}
