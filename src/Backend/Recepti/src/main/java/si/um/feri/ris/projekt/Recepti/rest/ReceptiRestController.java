package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Sestavine;

import java.net.URI;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class ReceptiRestController {

    @Autowired
    ReceptiJpaDao dao;

    @GetMapping("/recepti")
    public Iterable<Recepti> findAll(@RequestParam(value = "ime", required = false) String ime) {
        if (ime != null && !ime.isBlank()) {
            return dao.findByImeContainingIgnoreCase(ime);
        }
        return dao.findAll();
    }

    @GetMapping("/recepti/{idRecepta}")
    public ResponseEntity<Recepti> findById(@PathVariable("idRecepta") int idRecepta) {
        return dao.findById(idRecepta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/recepti")
    public ResponseEntity<Recepti> addNew(@RequestBody Recepti r) {
        if (r.getSestavine() != null) {
            for (Sestavine s : r.getSestavine()) s.setRecept(r);
        }
        Recepti saved = dao.save(r);
        return ResponseEntity.created(URI.create("/recepti/" + saved.getId())).body(saved);
    }

    @PutMapping("/recepti/{id}")
    public ResponseEntity<Recepti> updateById(@PathVariable("id") int id, @RequestBody Recepti incoming) {
        Optional<Recepti> existing = dao.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Recepti r = existing.get();
        r.setIme(incoming.getIme());
        r.setOpis(incoming.getOpis());
        r.setSestavine(incoming.getSestavine());

        Recepti saved = dao.save(r);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/recepti/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") int id) {
        if (!dao.existsById(id)) return ResponseEntity.notFound().build();
        dao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
