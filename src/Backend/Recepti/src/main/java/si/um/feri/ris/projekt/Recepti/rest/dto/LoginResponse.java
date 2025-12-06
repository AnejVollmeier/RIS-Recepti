package si.um.feri.ris.projekt.Recepti.rest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long id;
    private String uporabniskoIme;
    private String email;
    private String vloga;
    private String token;
}
