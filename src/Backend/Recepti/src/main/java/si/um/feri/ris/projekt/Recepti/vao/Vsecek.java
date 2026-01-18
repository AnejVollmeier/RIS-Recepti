package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entiteta, ki predstavlja všeček (favorit) recepta s strani uporabnika.
 */
@Data
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uporabnik_id", "recept_id" })
})
public class Vsecek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "uporabnik_id", nullable = false)
    private Uporabnik uporabnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    @JsonBackReference("recept-vsecki")
    private Recepti recept;

    public Vsecek(Uporabnik uporabnik, Recepti recept) {
        this.uporabnik = uporabnik;
        this.recept = recept;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
