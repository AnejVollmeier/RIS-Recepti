package si.um.feri.ris.projekt.Recepti.rest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
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

    @PersistenceContext
    private EntityManager em;

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
    public ResponseEntity<?> vrniZaUporabnika(@PathVariable Long uporabnikId) {
        try {
            List<Jedilnik> result = jedilnikDao.findByUporabnikIdWithRecepti(uporabnikId);
            // Map to DTOs to avoid lazy-loading / serialization problems
            List<JedilnikDto> dtos = result.stream().map(j -> {
                JedilnikDto d = new JedilnikDto();
                d.id = j.getId();
                d.naziv = j.getNaziv();
                d.datum = j.getDatum();
                d.steviloOseb = j.getSteviloOseb();
                // map minimal recipe info
                if (j.getRecepti() != null) {
                    d.recepti = j.getRecepti().stream().map(r -> {
                        ReceptDto rd = new ReceptDto();
                        rd.id = r.getId();
                        rd.ime = r.getIme();
                        return rd;
                    }).toList();
                }
                return d;
            }).toList();
            return ResponseEntity.ok(dtos);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri pridobivanju jedilnikov za uporabnika: " + ex.getMessage());
        }
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

    // helper to map Jedilnik to JedilnikDto
    private JedilnikDto toDto(Jedilnik j) {
        JedilnikDto d = new JedilnikDto();
        d.id = j.getId();
        d.naziv = j.getNaziv();
        d.datum = j.getDatum();
        d.steviloOseb = j.getSteviloOseb();
        if (j.getRecepti() != null) {
            d.recepti = j.getRecepti().stream().map(r -> {
                ReceptDto rd = new ReceptDto();
                rd.id = r.getId();
                rd.ime = r.getIme();
                return rd;
            }).toList();
        }
        return d;
    }

    @Transactional
    @PostMapping("/{jedilnikId}/recepti/{receptId}")
    public ResponseEntity<?> dodajRecept(@PathVariable Long jedilnikId, @PathVariable int receptId) {
        try {
            Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(jedilnikId);
            if (jedilnikOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Recepti recept = receptiDao.findById(receptId).orElse(null);
            if (recept == null) {
                return ResponseEntity.badRequest().body("Recept ne obstaja");
            }

            // Insert into join table using native query to avoid JPA merge problems
            try {
                em.createNativeQuery("INSERT INTO jedilnik_recepti (jedilnik_id, recept_id) VALUES (?, ?)")
                        .setParameter(1, jedilnikId)
                        .setParameter(2, receptId)
                        .executeUpdate();
                em.flush();
                em.clear(); // clear cache so findByIdWithRecepti sees new data
            } catch (Exception e) {
                e.printStackTrace();
            }

            Jedilnik updated = jedilnikDao.findByIdWithRecepti(jedilnikId).orElse(jedilnikOpt.get());
            return ResponseEntity.ok(toDto(updated));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri dodajanju recepta: " + ex.getMessage());
        }
    }

    @Transactional
    @DeleteMapping("/{jedilnikId}/recepti/{receptId}")
    public ResponseEntity<?> odstraniRecept(@PathVariable Long jedilnikId, @PathVariable int receptId) {
        try {
            // Use native delete to remove from join table
            em.createNativeQuery("DELETE FROM jedilnik_recepti WHERE jedilnik_id = ? AND recept_id = ?")
                    .setParameter(1, jedilnikId)
                    .setParameter(2, receptId)
                    .executeUpdate();
            em.flush();
            em.clear();

            Jedilnik updated = jedilnikDao.findByIdWithRecepti(jedilnikId).orElse(null);
            if (updated == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(toDto(updated));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri odstranjevanju recepta: " + ex.getMessage());
        }
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

    @Transactional
    @PostMapping("/{jedilnikId}/recepti")
    public ResponseEntity<?> dodajReceptBody(@PathVariable Long jedilnikId, @RequestBody AddReceptRequest request) {
        try {
            Optional<Jedilnik> jedilnikOpt = jedilnikDao.findById(jedilnikId);
            if (jedilnikOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Recepti recept = receptiDao.findById(request.getReceptId()).orElse(null);
            if (recept == null) {
                return ResponseEntity.badRequest().body("Recept ne obstaja");
            }

            // Insert into join table using native query
            try {
                em.createNativeQuery("INSERT INTO jedilnik_recepti (jedilnik_id, recept_id) VALUES (?, ?)")
                        .setParameter(1, jedilnikId)
                        .setParameter(2, request.getReceptId())
                        .executeUpdate();
                em.flush();
                em.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // optionally update metadata on jedilnik
            Jedilnik jedilnik = jedilnikDao.findById(jedilnikId).orElse(jedilnikOpt.get());
            if (request.getDatum() != null) {
                jedilnik.setDatum(request.getDatum());
            }
            if (request.getSteviloOseb() != null) {
                jedilnik.setSteviloOseb(request.getSteviloOseb());
            }
            if (request.getAlergenIds() != null) {
                for (Long aid : request.getAlergenIds()) {
                    alergenDao.findById(aid).ifPresent(jedilnik::addAlergen);
                }
            }
            jedilnikDao.save(jedilnik);
            em.flush();
            em.clear();

            Jedilnik updated = jedilnikDao.findByIdWithRecepti(jedilnikId).orElse(jedilnik);
            return ResponseEntity.ok(toDto(updated));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Napaka pri dodajanju recepta (body): " + ex.getMessage());
        }
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

    // DTO for adding a recipe with optional metadata
    public static class AddReceptRequest {
        private int receptId;
        private LocalDate datum;
        private Integer steviloOseb;
        private java.util.List<Long> alergenIds;

        public int getReceptId() { return receptId; }
        public void setReceptId(int receptId) { this.receptId = receptId; }
        public LocalDate getDatum() { return datum; }
        public void setDatum(LocalDate datum) { this.datum = datum; }
        public Integer getSteviloOseb() { return steviloOseb; }
        public void setSteviloOseb(Integer steviloOseb) { this.steviloOseb = steviloOseb; }
        public java.util.List<Long> getAlergenIds() { return alergenIds; }
        public void setAlergenIds(java.util.List<Long> alergenIds) { this.alergenIds = alergenIds; }
    }

    // lightweight DTOs to avoid returning full JPA entities
    public static class JedilnikDto {
        public Long id;
        public String naziv;
        public java.time.LocalDate datum;
        public int steviloOseb;
        public java.util.List<ReceptDto> recepti;
    }

    public static class ReceptDto {
        public Integer id;
        public String ime;
    }
}
