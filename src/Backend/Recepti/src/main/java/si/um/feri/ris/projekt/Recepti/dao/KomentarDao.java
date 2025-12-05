package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.um.feri.ris.projekt.Recepti.vao.Komentar;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.List;

@Repository
public interface KomentarDao extends JpaRepository<Komentar, Long> {

    List<Komentar> findByRecept(Recepti recept);

    List<Komentar> findByReceptId(int receptId);

    List<Komentar> findByUporabnik(Uporabnik uporabnik);

    List<Komentar> findByUporabnikId(Long uporabnikId);

    void deleteAllByRecept(Recepti recept);
}

