package si.um.feri.ris.projekt.Recepti.rest.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NutritionData {

    private String naziv;                    // Ime sestavine
    private Double kalorijeNa100g;          // Kalorije na 100g
    private Double mascobeNa100g;           // Maščobe na 100g (g)
    private Double proteiniNa100g;          // Proteini na 100g (g)
    private Double ogljikoviHidratiNa100g;  // Ogljikovi hidrati na 100g (g)

    // Za izračun na specifično količino
    private Double kalorije;                // Kalorije za vneseno količino
    private Double mascobe;                 // Maščobe za vneseno količino
    private Double proteini;                // Proteini za vneseno količino
    private Double ogljikoviHidrati;        // Ogljikovi hidrati za vneseno količino

    private String kolicina;                // Vnesena količina (npr. "200g", "2 kosi")
    private boolean najdeno;                // Ali smo našli podatke v API-ju
}