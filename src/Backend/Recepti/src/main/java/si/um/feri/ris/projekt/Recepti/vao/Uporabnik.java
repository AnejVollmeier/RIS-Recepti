package si.um.feri.ris.projekt.Recepti.vao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Entiteta, ki predstavlja uporabnika sistema.
 * Uporabnik lahko ustvarja recepte, komentarje, ocene in všečke.
 */
@Data
@NoArgsConstructor
@Entity
public class Uporabnik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String uporabniskoIme;

    @Column(unique = true, nullable = false)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String geslo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Vloga vloga = Vloga.USER;

    // Opcijski podatki profila
    private String avatar;
    private String bio;

    // Relacije
    @OneToMany(mappedBy = "avtor", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Recepti> recepti = new ArrayList<>();

    @OneToMany(mappedBy = "uporabnik", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Komentar> komentarji = new ArrayList<>();

    @OneToMany(mappedBy = "uporabnik", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Ocena> ocene = new ArrayList<>();

    @OneToMany(mappedBy = "uporabnik", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Vsecek> vsecki = new ArrayList<>();

    public Uporabnik(String uporabniskoIme, String email, String geslo) {
        this.uporabniskoIme = uporabniskoIme;
        this.email = email;
        this.geslo = geslo;
        this.vloga = Vloga.USER;
    }

    public Uporabnik(String uporabniskoIme, String email, String geslo, Vloga vloga) {
        this.uporabniskoIme = uporabniskoIme;
        this.email = email;
        this.geslo = geslo;
        this.vloga = vloga;
    }

    /**
     * Preveri, ali je uporabnik admin.
     */
    public boolean isAdmin() {
        return this.vloga == Vloga.ADMIN;
    }
}

