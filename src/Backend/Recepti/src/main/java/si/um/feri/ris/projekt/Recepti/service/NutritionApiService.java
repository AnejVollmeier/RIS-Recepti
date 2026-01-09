package si.um.feri.ris.projekt.Recepti.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import si.um.feri.ris.projekt.Recepti.rest.dto.NutritionData;

import java.util.logging.Logger;

@Service
public class NutritionApiService {

    private static final Logger log = Logger.getLogger(NutritionApiService.class.getName());

    @Value("${nutrition.api.base-url:https://world.openfoodfacts.org}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public NutritionApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public NutritionData searchByName(String ime) {
        if (ime == null || ime.trim().isEmpty()) {
            log.warning("Ime sestavine je prazno");
            return createEmptyNutritionData(ime);
        }

        try {
            String url = apiBaseUrl + "/cgi/search.pl?search_terms=" + ime + "&search_simple=1&json=1";
            log.info("Kličem API: " + url);

            String response = restTemplate.getForObject(url, String.class);

            if (response == null) {
                log.warning("API ni vrnil odgovora za:  " + ime);
                return createEmptyNutritionData(ime);
            }

            // Parsiraj JSON odgovor
            JsonNode root = objectMapper.readTree(response);
            JsonNode products = root.get("products");

            if (products == null || products.size() == 0) {
                log.warning("Ni najdenih izdelkov za: " + ime);
                return createEmptyNutritionData(ime);
            }

            // Vzemi prvi rezultat
            JsonNode firstProduct = products.get(0);
            JsonNode nutriments = firstProduct.get("nutriments");

            if (nutriments == null) {
                log.warning("Izdelek nima hranilnih podatkov:  " + ime);
                return createEmptyNutritionData(ime);
            }

            // Pridobi hranilne podatke
            NutritionData data = new NutritionData();
            data.setNaziv(firstProduct.has("product_name") ?
                    firstProduct.get("product_name").asText() : ime);

            data.setKalorijeNa100g(getDoubleValue(nutriments, "energy-kcal_100g"));
            data.setMascobeNa100g(getDoubleValue(nutriments, "fat_100g"));
            data.setProteiniNa100g(getDoubleValue(nutriments, "proteins_100g"));
            data.setOgljikoviHidratiNa100g(getDoubleValue(nutriments, "carbohydrates_100g"));
            data.setNajdeno(true);

            log.info("Uspešno najdeni podatki za: " + ime);
            return data;

        } catch (Exception e) {
            log.severe("Napaka pri iskanju hranilnih podatkov: " + e.getMessage());
            return createEmptyNutritionData(ime);
        }
    }

    public NutritionData calculateForQuantity(NutritionData data, String kolicina) {
        if (data == null || ! data.isNajdeno()) {
            return data;
        }

        try {
            // Ekstrakcija grammov iz količine
            double grami = extractGrams(kolicina);

            if (grami <= 0) {
                log.warning("Ne morem ekstraktirati gramov iz:  " + kolicina);
                return data;
            }

            // Izračunaj faktor (npr. za 200g je faktor 2.0)
            double faktor = grami / 100.0;

            // Izračunaj vrednosti za količino
            data.setKalorije(data.getKalorijeNa100g() * faktor);
            data.setMascobe(data.getMascobeNa100g() * faktor);
            data.setProteini(data.getProteiniNa100g() * faktor);
            data.setOgljikoviHidrati(data.getOgljikoviHidratiNa100g() * faktor);
            data.setKolicina(kolicina);

            log.info("Izračunan za količino " + kolicina + ": " + data.getKalorije() + " kcal");
            return data;

        } catch (Exception e) {
            log.severe("Napaka pri izračunu za količino: " + e.getMessage());
            return data;
        }
    }

    private double extractGrams(String kolicina) {
        if (kolicina == null || kolicina.trim().isEmpty()) {
            return 0;
        }

        try {
            // Odstrani vse razen številk in decimalne pike
            String cleaned = kolicina.toLowerCase()
                    .replaceAll("[^0-9.]", "");

            if (cleaned.isEmpty()) {
                return 0;
            }

            double value = Double.parseDouble(cleaned);

            // Če je v kilogramih (npr. "1kg"), pretvori v grame
            if (kolicina.toLowerCase().contains("kg")) {
                value *= 1000;
            }
            // Če so kosi, predpostavi ~50g na kos (prilagodljivo)
            else if (kolicina.toLowerCase().contains("kos")) {
                value *= 50;
            }

            return value;

        } catch (NumberFormatException e) {
            log.warning("Ne morem pretvoriti v grame: " + kolicina);
            return 0;
        }
    }

    /**
     * Pomožna metoda za pridobitev double vrednosti iz JSON
     */
    private Double getDoubleValue(JsonNode node, String fieldName) {
        if (node.has(fieldName)) {
            return node.get(fieldName).asDouble();
        }
        return 0.0;
    }

    /**
     * Ustvari prazen NutritionData objekt
     */
    private NutritionData createEmptyNutritionData(String ime) {
        NutritionData data = new NutritionData();
        data.setNaziv(ime);
        data.setNajdeno(false);
        data.setKalorijeNa100g(0.0);
        data.setMascobeNa100g(0.0);
        data.setProteiniNa100g(0.0);
        data.setOgljikoviHidratiNa100g(0.0);
        return data;
    }
}