package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.AlergenDao;
import si.um.feri.ris.projekt.Recepti.dao.JedilnikDao;
import si.um.feri.ris.projekt.Recepti.dao.ReceptiJpaDao;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.vao.Alergen;
import si.um.feri.ris.projekt.Recepti.vao.Jedilnik;
import si.um.feri.ris.projekt.Recepti.vao.Recepti;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/jedilniki")
@CrossOrigin
public class JedilnikRestController {

    @Autowired
    private JedilnikDao jedilnikDao;

    @Autowired
    private ReceptiJpaDao receptiDao;

    @Autowired
    private UporabnikDao uporabnikDao;

    @Autowired
    private AlergenDao alergenDao;

    @GetMapping
    public List<Jedilnik> vrniVse() {
        return jedilnikDao.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Jedilnik> vrniEnega(@PathVariable Long id) {
        return jedilnikDao.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/uporabnik/{uporabnikId}")
    public List<Jedilnik> vrniZaUporabnika(@PathVariable Long uporabnikId) {
        return jedilnikDao.findByUporabnikId(uporabnikId);
    }

    @GetMapping("/datum/{datum}")
    public List<Jedilnik> vrniZaDatum(@PathVariable LocalDate datum) {
        return jedilnikDao.findByDatum(datum);
    }

    @PostMapping
    public ResponseEntity<?> ustvari(@RequestBody JedilnikRequest request) {
        Jedilnik jedilnik = new Jedilnik(request.getNaziv(), request.getDatum(), request.getSteviloOseb());
        
        if (request.getUporabnikId() != null) {
            Optional<Uporabnik> uporabnikOpt = uporabnikDao.findById(request.getUporabnikId());
            uporabnikOpt.ifPresent(jedilnik::setUporabnik);
        }
        
        Jedilnik saved = jedilnikDao.save(jedilnik);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> posodobi(@PathVariable Long id, @RequestBody JedilnikRequest request) {
        Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(id);
        if (jedilnikOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Jedilnik jedilnik = jedilnikOpt.get();
        jedilnik.setNaziv(request.getNaziv());
        jedilnik.setDatum(request.getDatum());
        jedilnik.setSteviloOseb(request.getSteviloOseb());
        
        return ResponseEntity.ok(jedilnikDao.save(jedilnik));
    }

    @PostMapping("/{jedilnikId}/recepti/{receptId}")
    public ResponseEntity<?> dodajRecept(@PathVariable Long jedilnikId, @PathVariable int receptId) {
        Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(jedilnikId);
        if (jedilnikOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<Recepti> receptOpt = receptiDao.findById(receptId);
        if (receptOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Recept ne obstaja");
        }
        
        Jedilnik jedilnik = jedilnikOpt.get();
        jedilnik.addRecept(receptOpt.get());
        return ResponseEntity.ok(jedilnikDao.save(jedilnik));
    }

    @DeleteMapping("/{jedilnikId}/recepti/{receptId}")
    public ResponseEntity<?> odstraniRecept(@PathVariable Long jedilnikId, @PathVariable int receptId) {
        Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(jedilnikId);
        if (jedilnikOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Jedilnik jedilnik = jedilnikOpt.get();
        jedilnik.removeRecept(receptId);
        return ResponseEntity.ok(jedilnikDao.save(jedilnik));
    }

    @PostMapping("/{jedilnikId}/alergeni/{alergenId}")
    public ResponseEntity<?> dodajAlergen(@PathVariable Long jedilnikId, @PathVariable Long alergenId) {
        Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(jedilnikId);
        if (jedilnikOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Optional<Alergen> alergenOpt = alergenDao.findById(alergenId);
        if (alergenOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Alergen ne obstaja");
        }
        
        Jedilnik jedilnik = jedilnikOpt.get();
        jedilnik.addAlergen(alergenOpt.get());
        return ResponseEntity.ok(jedilnikDao.save(jedilnik));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> izbrisi(@PathVariable Long id) {
        if (!jedilnikDao.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        jedilnikDao.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DTO za ustvarjanje/urejanje jedilnika
    public static class JedilnikRequest {
        private String naziv;
        private LocalDate datum;
        private int steviloOseb;
        private Long uporabnikId;

        public String getNaziv() { return naziv; }
        public void setNaziv(String naziv) { this.naziv = naziv; }
        public LocalDate getDatum() { return datum; }
        public void setDatum(LocalDate datum) { this.datum = datum; }
        public int getSteviloOseb() { return steviloOseb; }
        public void setSteviloOseb(int steviloOseb) { this.steviloOseb = steviloOseb; }
        public Long getUporabnikId() { return uporabnikId; }
        public void setUporabnikId(Long uporabnikId) { this.uporabnikId = uporabnikId; }
    }
}

