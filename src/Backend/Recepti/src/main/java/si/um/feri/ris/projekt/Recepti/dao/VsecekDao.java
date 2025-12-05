package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vsecek;

import java.util.List;
import java.util.Optional;

@Repository
public interface VsecekDao extends JpaRepository<Vsecek, Long> {

    List<Vsecek> findByRecept(Recepti recept);

    List<Vsecek> findByReceptId(int receptId);

    List<Vsecek> findByUporabnik(Uporabnik uporabnik);

    List<Vsecek> findByUporabnikId(Long uporabnikId);

    Optional<Vsecek> findByUporabnikAndRecept(Uporabnik uporabnik, Recepti recept);

    Optional<Vsecek> findByUporabnikIdAndReceptId(Long uporabnikId, int receptId);

    boolean existsByUporabnikAndRecept(Uporabnik uporabnik, Recepti recept);

    @Modifying
    @Transactional
    void deleteByUporabnikAndRecept(Uporabnik uporabnik, Recepti recept);

    int countByRecept(Recepti recept);

    int countByReceptId(int receptId);
}

