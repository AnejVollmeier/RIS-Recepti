# Poročilo o testiranju - Unit testi

## Člani skupine in testi

| Član           | Test                          | Opis                                                                        |
| -------------- | ----------------------------- | --------------------------------------------------------------------------- |
| Anej Vollmeier | `testUstvariVeljavnoOceno()`  | Preverja pravilno ustvarjanje in shranjevanje ocene recepta v bazo podatkov |
| Anej Vollmeier | `testPovprecnaOcenaRecepta()` | Preverja pravilno računanje povprečne ocene recepta od več uporabnikov      |

## Opis testov

### Test 1: `testUstvariVeljavnoOceno()`

**Kaj preizkuša:** Ustvarjanje in shranjevanje ocene recepta v bazo.

**Zakaj je pomemben:** Preveri, da se ocene pravilno shranjujejo z vsemi podatki (uporabnik, recept, vrednost, časovni žig).

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@BeforeEach`, `@Transactional`, `@SpringBootTest`, `@Autowired`

### Test 2: `testPovprecnaOcenaRecepta()`

**Kaj preizkuša:** Računanje povprečne ocene recepta od več uporabnikov.

**Zakaj je pomemben:** Preveri, da se povprečje pravilno izračuna (3 uporabniki dajo ocene 5, 3, 4 → povprečje = 4.0).

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@BeforeEach`, `@Transactional`, `@SpringBootTest`, `@Autowired`

## ✅ Avtorji:

- **Anej Vollmeier**
- **Matija Gusel**
