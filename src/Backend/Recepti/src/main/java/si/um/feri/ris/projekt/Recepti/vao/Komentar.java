package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entiteta, ki predstavlja komentar uporabnika o receptu.
 */
@Data
@NoArgsConstructor
@Entity
public class Komentar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String besedilo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "uporabnik_id", nullable = false)
    private Uporabnik uporabnik;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recept_id", nullable = false)
    @JsonBackReference("recept-komentarji")
    private Recepti recept;

    public Komentar(String besedilo, Uporabnik uporabnik, Recepti recept) {
        this.besedilo = besedilo;
        this.uporabnik = uporabnik;
        this.recept = recept;
        this.createdAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Uredi besedilo komentarja.
     */
    public void uredi(String novoBesedilo) {
        this.besedilo = novoBesedilo;
        this.updatedAt = LocalDateTime.now();
    }
}
