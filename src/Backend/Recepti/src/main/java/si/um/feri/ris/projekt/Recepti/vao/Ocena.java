package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entiteta, ki predstavlja oceno recepta (1-5).
 */
@Data
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "uporabnik_id", "recept_id" })
})
public class Ocena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int vrednost; // 1-5

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "uporabnik_id", nullable = false)
    private Uporabnik uporabnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    @JsonBackReference("recept-ocene")
    private Recepti recept;

    public Ocena(int vrednost, Uporabnik uporabnik, Recepti recept) {
        this.setVrednost(vrednost);
        this.uporabnik = uporabnik;
        this.recept = recept;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Nastavi vrednost ocene z validacijo (1-5).
     */
    public void setVrednost(int vrednost) {
        if (vrednost < 1 || vrednost > 5) {
            throw new IllegalArgumentException("Ocena mora biti med 1 in 5");
        }
        this.vrednost = vrednost;
        this.updatedAt = LocalDateTime.now();
    }
}
