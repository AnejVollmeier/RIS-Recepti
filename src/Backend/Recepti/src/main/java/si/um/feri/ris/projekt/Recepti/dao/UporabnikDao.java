package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.Optional;

@Repository
public interface UporabnikDao extends JpaRepository<Uporabnik, Long> {

    Optional<Uporabnik> findByUporabniskoIme(String uporabniskoIme);

    Optional<Uporabnik> findByEmail(String email);

    boolean existsByUporabniskoIme(String uporabniskoIme);

    boolean existsByEmail(String email);
}


