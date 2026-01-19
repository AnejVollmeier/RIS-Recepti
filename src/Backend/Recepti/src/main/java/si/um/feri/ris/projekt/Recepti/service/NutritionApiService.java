package si.um.feri.ris.projekt.Recepti.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import si.um.feri.ris.projekt.Recepti.rest.dto.NutritionData;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class NutritionApiService {

    private static final Logger log = Logger.getLogger(NutritionApiService.class.getName());

    @Value("${nutrition.api.base-url:https://world.openfoodfacts.org}")
    private String apiBaseUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Translation cache (populated dynamically via API)
    private static final Map<String, String> TRANSLATION_DB = new HashMap<>();

    // Local hardcoded database for problematic ingredients
    private static final Map<String, double[]> LOCAL_NUTRITION_DB = new HashMap<>();

    static {
        // Format: { calories, fat, protein, carbs }
        LOCAL_NUTRITION_DB.put("mleko", new double[] { 64, 3.5, 3.3, 4.8 });
        LOCAL_NUTRITION_DB.put("čebula", new double[] { 40, 0.1, 1.1, 9.3 });
        LOCAL_NUTRITION_DB.put("guanciale", new double[] { 655, 69.0, 9.0, 0 });
    }

    public NutritionApiService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(15000);
        this.restTemplate = new RestTemplate(factory);
        this.objectMapper = new ObjectMapper();
    }

    public NutritionData searchByName(String ime) {
        if (ime == null || ime.trim().isEmpty()) {
            log.warning("Ime sestavine je prazno");
            return createEmptyNutritionData(ime);
        }

        // First check local hardcoded database
        NutritionData localData = searchLocalDatabase(ime);
        if (localData != null) {
            log.info("Najdeno v lokalni hardkodirani bazi: " + ime);
            return localData;
        }

        // Translate to English and call external API
        try {
            String searchTerm = translateToEnglish(ime);
            String encoded = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
            String url = apiBaseUrl + "/cgi/search.pl?search_terms=" + encoded
                    + "&search_simple=1&json=1&page_size=1&fields=product_name,nutriments";
            log.info("Kličem API: " + url + " (original: " + ime + ", translated: " + searchTerm + ")");

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
            data.setNaziv(firstProduct.has("product_name") ? firstProduct.get("product_name").asText() : ime);

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

    private String translateToEnglish(String ime) {
        String normalized = ime.toLowerCase().trim();

        // Check cache first
        if (TRANSLATION_DB.containsKey(normalized)) {
            return TRANSLATION_DB.get(normalized);
        }

        // Use MyMemory translation API (free, no key needed)
        try {
            String encoded = URLEncoder.encode(ime, StandardCharsets.UTF_8);
            String url = "https://api.mymemory.translated.net/get?q=" + encoded + "&langpair=sl|en";
            log.info("Prevajam: " + ime);

            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                JsonNode responseData = root.get("responseData");
                if (responseData != null && responseData.has("translatedText")) {
                    String translated = responseData.get("translatedText").asText();
                    // Cache the translation for future use
                    TRANSLATION_DB.put(normalized, translated.toLowerCase());
                    log.info("Prevedeno: " + ime + " -> " + translated);
                    return translated;
                }
            }
        } catch (Exception e) {
            log.warning("Napaka pri prevajanju: " + e.getMessage());
        }

        // Fallback: return original
        return ime;
    }

    private NutritionData searchLocalDatabase(String ime) {
        String cleaned = cleanString(ime);

        // Check for direct match after cleaning
        if (LOCAL_NUTRITION_DB.containsKey(cleaned)) {
            return createFromLocal(ime, LOCAL_NUTRITION_DB.get(cleaned));
        }

        // Check if any key is contained in the cleaned string
        for (Map.Entry<String, double[]> entry : LOCAL_NUTRITION_DB.entrySet()) {
            if (cleaned.contains(entry.getKey())) {
                return createFromLocal(ime, entry.getValue());
            }
        }

        return null;
    }

    private String cleanString(String text) {
        if (text == null)
            return "";
        // Remove everything except letters (converts "mleko 3.5%" -> "mleko")
        // and normalize Slovenian characters for easier matching
        String cleaned = text.toLowerCase()
                .replace("č", "c")
                .replace("š", "s")
                .replace("ž", "z")
                .replaceAll("[^a-z]", "");
        return cleaned;
    }

    private NutritionData createFromLocal(String ime, double[] values) {
        NutritionData data = new NutritionData();
        data.setNaziv(ime);
        data.setKalorijeNa100g(values[0]);
        data.setMascobeNa100g(values[1]);
        data.setProteiniNa100g(values[2]);
        data.setOgljikoviHidratiNa100g(values[3]);
        data.setNajdeno(true);
        return data;
    }

    public NutritionData calculateForQuantity(NutritionData data, String kolicina) {
        if (data == null || !data.isNajdeno()) {
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
            String q = kolicina.toLowerCase().trim();

            // Extract number part
            String numberPart = q.replaceAll("[^0-9.,]", "").replace(",", ".");
            if (numberPart.isEmpty())
                return 0;

            double value = Double.parseDouble(numberPart);

            // Check units
            if (q.contains("kg")) {
                return value * 1000;
            } else if (q.contains("dag")) {
                return value * 10;
            } else if (q.contains("mg")) {
                return value / 1000;
            } else if (q.contains("dcl") || q.contains("dl")) {
                return value * 100;
            } else if (q.contains("cl")) {
                return value * 10;
            } else if (q.contains("ml")) { // Order matters: check ml after others to avoid partial matches if possible,
                                           // though 'ml' is distinct enough usually
                return value;
            } else if (q.contains("l ") || q.endsWith("l")) { // Check 'l' carefully to avoid matching 'ml', 'dcl', etc.
                                                              // if logic was simpler
                // Simple contains("l") matches "ml". Better logic needed or specific order.
                // Actually, let's look for specific unit endings or words.
                if (q.matches(".*\\b(l|litra|liter)\\b.*") || q.endsWith("l")) {
                    if (!q.contains("ml") && !q.contains("dcl") && !q.contains("dl")) {
                        return value * 1000;
                    }
                }
            } else if (q.contains("žlica") || q.contains("žlici") || q.contains("žlice")) {
                return value * 15; // Approx 15g per tablespoon
            } else if (q.contains("žlička") || q.contains("žlički") || q.contains("žličke")) {
                return value * 5; // Approx 5g per teaspoon
            } else if (q.contains("kos")) {
                return value * 100; // Approx 100g per piece
            } else if (q.contains("ščepec")) {
                return 1; // 1g
            }

            // If explicit "g" unit
            if (q.contains("g") && !q.contains("kg") && !q.contains("dag") && !q.contains("mg")) {
                return value;
            }

            // No units found (e.g. "3", "2.5") -> Assumption: It is pieces
            // Average weight of "1 piece" of generic vegetable/fruit is often ~100g-150g.
            // Let's default to 100g per unit if no unit specified.
            // This fixes "3 korenje" being treated as "3g" (now "300g").
            return value * 100;

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