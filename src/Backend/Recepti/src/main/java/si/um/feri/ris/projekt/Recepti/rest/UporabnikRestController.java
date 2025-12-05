package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;
import si.um.feri.ris.projekt.Recepti.vao.Vloga;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/uporabniki")
@CrossOrigin
public class UporabnikRestController {

    @Autowired
    private UporabnikDao uporabnikDao;

    @GetMapping
    public List<Uporabnik> vrniVse() {
        return uporabnikDao.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Uporabnik> vrniEnega(@PathVariable Long id) {
        return uporabnikDao.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/uporabniskoIme/{uporabniskoIme}")
    public ResponseEntity<Uporabnik> vrniPoUporabniskemImenu(@PathVariable String uporabniskoIme) {
        return uporabnikDao.findByUporabniskoIme(uporabniskoIme)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> ustvari(@RequestBody Uporabnik uporabnik) {
        if (uporabnikDao.existsByUporabniskoIme(uporabnik.getUporabniskoIme())) {
            return ResponseEntity.badRequest().body("Uporabniško ime že obstaja");
        }
        if (uporabnikDao.existsByEmail(uporabnik.getEmail())) {
            return ResponseEntity.badRequest().body("Email že obstaja");
        }
        if (uporabnik.getVloga() == null) {
            uporabnik.setVloga(Vloga.USER);
        }
        Uporabnik saved = uporabnikDao.save(uporabnik);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> posodobi(@PathVariable Long id, @RequestBody Uporabnik posodobljen) {
        Optional<Uporabnik> obstojecOpt = uporabnikDao.findById(id);
        if (obstojecOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Uporabnik obstojec = obstojecOpt.get();

        boolean imeSeSpreminja = !obstojec.getUporabniskoIme().equals(posodobljen.getUporabniskoIme());
        boolean emailSeSpreminja = !obstojec.getEmail().equals(posodobljen.getEmail());

        if (imeSeSpreminja && uporabnikDao.existsByUporabniskoIme(posodobljen.getUporabniskoIme())) {
            return ResponseEntity.badRequest().body("Uporabniško ime že obstaja");
        }
        if (emailSeSpreminja && uporabnikDao.existsByEmail(posodobljen.getEmail())) {
            return ResponseEntity.badRequest().body("Email že obstaja");
        }

        obstojec.setUporabniskoIme(posodobljen.getUporabniskoIme());
        obstojec.setEmail(posodobljen.getEmail());
        if (posodobljen.getGeslo() != null && !posodobljen.getGeslo().isEmpty()) {
            obstojec.setGeslo(posodobljen.getGeslo());
        }
        obstojec.setAvatar(posodobljen.getAvatar());
        obstojec.setBio(posodobljen.getBio());

        return ResponseEntity.ok(uporabnikDao.save(obstojec));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!uporabnikDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        uporabnikDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
