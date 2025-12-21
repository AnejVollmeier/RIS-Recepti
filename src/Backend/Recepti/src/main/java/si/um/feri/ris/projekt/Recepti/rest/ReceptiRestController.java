package si.um.feri. ris.projekt.Recepti. rest;

import org.springframework. beans.factory.annotation.Autowired;
import org.springframework. http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt. Recepti.dao.ReceptiJpaDao;
import si. um.feri.ris. projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Sestavine;

import java.net. URI;
import java.util. Optional;

@RestController
@RequestMapping("/api/recepti")
@CrossOrigin
public class ReceptiRestController {

    @Autowired
    ReceptiJpaDao dao;

    @GetMapping
    public Iterable<Recepti> findAll(@RequestParam(value = "ime", required = false) String ime) {
        if (ime != null && !ime.isBlank()) {
            return dao.findByImeContainingIgnoreCase(ime);
        }
        return dao.findAll();
    }

    @GetMapping("/{idRecepta}")
    public ResponseEntity<Recepti> findById(@PathVariable("idRecepta") int idRecepta) {
        return dao.findById(idRecepta)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Recepti> addNew(@RequestBody Recepti r) {
        if (r.getSestavine() != null) {
            for (Sestavine s : r.getSestavine()) s.setRecept(r);
        }
        Recepti saved = dao.save(r);
        return ResponseEntity. created(URI.create("/api/recepti/" + saved.getId())).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Recepti> updateById(@PathVariable("id") int id, @RequestBody Recepti incoming) {
        Optional<Recepti> existing = dao.findById(id);
        if (existing.isEmpty()) return ResponseEntity.notFound().build();

        Recepti r = existing.get();
        r.setIme(incoming.getIme());
        r.setOpis(incoming.getOpis());
        r.setNavodila(incoming.getNavodila());
        r.setSlikaUrl(incoming.getSlikaUrl());
        r.setSteviloPorcij(incoming. getSteviloPorcij()); // DODANO: posodobi število porcij
        r.setSestavine(incoming.getSestavine());
        // Avtor recepta se ne more spreminjat, zato ga ne posodabljamo

        Recepti saved = dao.save(r);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") int id) {
        if (!dao.existsById(id)) return ResponseEntity.notFound().build();
        dao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}