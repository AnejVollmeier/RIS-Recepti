package si.um.feri.ris.projekt.Recepti.rest;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    @GetMapping("/info")
    public String info() {
        return "Dela.";
    }
}
