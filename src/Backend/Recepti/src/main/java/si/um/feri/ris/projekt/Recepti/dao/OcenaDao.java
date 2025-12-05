package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import si.um.feri.ris.projekt.Recepti.vao.Ocena;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.List;
import java.util.Optional;

@Repository
public interface OcenaDao extends JpaRepository<Ocena, Long> {

    List<Ocena> findByRecept(Recepti recept);

    List<Ocena> findByReceptId(int receptId);

    List<Ocena> findByUporabnik(Uporabnik uporabnik);

    Optional<Ocena> findByUporabnikAndRecept(Uporabnik uporabnik, Recepti recept);

    Optional<Ocena> findByUporabnikIdAndReceptId(Long uporabnikId, int receptId);

    @Query("SELECT AVG(o.vrednost) FROM Ocena o WHERE o.recept.id = :receptId")
    Double povprecnaOcenaZaRecept(int receptId);

    boolean existsByUporabnikAndRecept(Uporabnik uporabnik, Recepti recept);
}

