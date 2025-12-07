package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import si.um.feri.ris.projekt.Recepti.vao.Jedilnik;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JedilnikDao extends JpaRepository<Jedilnik, Long> {

    List<Jedilnik> findByUporabnik(Uporabnik uporabnik);

    List<Jedilnik> findByUporabnikId(Long uporabnikId);

    @Query("select distinct j from Jedilnik j left join fetch j.recepti where j.uporabnik.id = :uporabnikId")
    List<Jedilnik> findByUporabnikIdWithRecepti(@Param("uporabnikId") Long uporabnikId);

    @Query("select j from Jedilnik j left join fetch j.recepti where j.id = :id")
    java.util.Optional<Jedilnik> findByIdWithRecepti(@Param("id") Long id);

    List<Jedilnik> findByDatum(LocalDate datum);

    List<Jedilnik> findByDatumBetween(LocalDate start, LocalDate end);

    List<Jedilnik> findByUporabnikIdAndDatumBetween(Long uporabnikId, LocalDate start, LocalDate end);
}
