package si.um.feri.ris.projekt.Recepti.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import si.um.feri.ris.projekt.Recepti.dao.UporabnikDao;
import si.um.feri.ris.projekt.Recepti.rest.dto.LoginRequest;
import si.um.feri.ris.projekt.Recepti.rest.dto.LoginResponse;
import si.um.feri.ris.projekt.Recepti.vao.Uporabnik;

import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthRestController {

    @Autowired
    private UporabnikDao uporabnikDao;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Uporabnik> uporabnikOpt = uporabnikDao.findByEmail(request.getEmail());
        
        if (uporabnikOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Napačen email ali geslo");
        }

        Uporabnik uporabnik = uporabnikOpt.get();

        if (!uporabnik.getGeslo().equals(request.getGeslo())) {
            return ResponseEntity.status(401).body("Napačen email ali geslo");
        }

        LoginResponse response = new LoginResponse(
            uporabnik.getId(),
            uporabnik.getUporabniskoIme(),
            uporabnik.getEmail(),
            uporabnik.getVloga().toString(),
            "dummy-token-" + uporabnik.getId() 
        );

        return ResponseEntity.ok(response);
    }
}
