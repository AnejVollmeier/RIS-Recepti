package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import si.um.feri.ris.projekt.Recepti.vao.Alergen;

import java.util.Optional;

@Repository
public interface AlergenDao extends JpaRepository<Alergen, Long> {

    Optional<Alergen> findByIme(String ime);

    boolean existsByIme(String ime);
}

