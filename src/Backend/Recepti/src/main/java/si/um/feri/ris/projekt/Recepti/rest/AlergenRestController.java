package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.AlergenDao;
import si.um.feri.ris.projekt.Recepti.vao.Alergen;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/alergeni")
@CrossOrigin
public class AlergenRestController {

    @Autowired
    private AlergenDao alergenDao;

    @GetMapping
    public List<Alergen> vrniVse() {
        return alergenDao.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Alergen> vrniEnega(@PathVariable Long id) {
        return alergenDao.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ime/{ime}")
    public ResponseEntity<Alergen> vrniPoImenu(@PathVariable String ime) {
        return alergenDao.findByIme(ime)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> ustvari(@RequestBody Alergen alergen) {
        if (alergenDao.existsByIme(alergen.getIme())) {
            return ResponseEntity.badRequest().body("Alergen s tem imenom že obstaja");
        }
        Alergen saved = alergenDao.save(alergen);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> posodobi(@PathVariable Long id, @RequestBody Alergen posodobljen) {
        Optional<Alergen> obstojecOpt = alergenDao.findById(id);
        if (obstojecOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Alergen obstojec = obstojecOpt.get();
        if (!obstojec.getIme().equals(posodobljen.getIme()) 
                && alergenDao.existsByIme(posodobljen.getIme())) {
            return ResponseEntity.badRequest().body("Alergen s tem imenom že obstaja");
        }
        
        obstojec.setIme(posodobljen.getIme());
        obstojec.setOpis(posodobljen.getOpis());
        return ResponseEntity.ok(alergenDao.save(obstojec));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!alergenDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        alergenDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

