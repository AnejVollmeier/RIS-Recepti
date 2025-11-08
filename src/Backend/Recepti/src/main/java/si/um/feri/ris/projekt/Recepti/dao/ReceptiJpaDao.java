package si.um.feri.ris.projekt.Recepti.dao;

import org.springframework.data.repository.CrudRepository;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;

import java.util.List;

public interface ReceptiJpaDao extends CrudRepository<Recepti,Integer> {

    List<Recepti> findByImeContainingIgnoreCase(String ime);
}
