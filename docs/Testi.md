# Poročilo o testiranju - Unit testi

## Člani skupine in testi

| Član           | Test                          | Opis                                                                        |
| -------------- | ----------------------------- | --------------------------------------------------------------------------- |
| Anej Vollmeier | `testUstvariVeljavnoOceno()`  | Preverja pravilno ustvarjanje in shranjevanje ocene recepta v bazo podatkov |
| Anej Vollmeier | `testPovprecnaOcenaRecepta()` | Preverja pravilno računanje povprečne ocene recepta od več uporabnikov      |
| Matija Gusel   | `testUstvariNovegaUporabnika()` | Preverja ustvarjanje novega uporabnika z vsemi podatki |
| Matija Gusel   | `testIskanjePoUporabniskomImenu()` | Preverja iskanje uporabnika po uporabniškem imenu |
| Matija Gusel   | `testIsAdminPozitivenScenarij()` | Preverja, da je admin uporabnik res admin |
| Matija Gusel   | `testUstvariNovRecept()` | Preverja ustvarjanje novega recepta z vsemi podatki |
| Matija Gusel   | `testIskanjePoID()` | Preverja iskanje recepta po ID-u |
| Matija Gusel   | `testIskanjePoImenu()` | Preverja iskanje receptov po imenu (case-insensitive) |
| Matija Gusel   | `testUstvariNovJedilnik()` | Preverja ustvarjanje novega jedilnika z vsemi podatki |
| Matija Gusel   | `testIskanjePoUporabniku()` | Preverja iskanje jedilnikov po uporabniku |
| Matija Gusel   | `testDodajanjeReceptaVJedilnik()` | Preverja dodajanje recepta v jedilnik |

## Opis testov

### Test 1: `testUstvariVeljavnoOceno()`

**Kaj preizkuša:** Ustvarjanje in shranjevanje ocene recepta v bazo.

**Zakaj je pomemben:** Preveri, da se ocene pravilno shranjujejo z vsemi podatki (uporabnik, recept, vrednost, časovni žig).

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@BeforeEach`, `@Transactional`, `@SpringBootTest`, `@Autowired`

### Test 2: `testPovprecnaOcenaRecepta()`

**Kaj preizkuša:** Računanje povprečne ocene recepta od več uporabnikov.

**Zakaj je pomemben:** Preveri, da se povprečje pravilno izračuna (3 uporabniki dajo ocene 5, 3, 4 → povprečje = 4.0).

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@BeforeEach`, `@Transactional`, `@SpringBootTest`, `@Autowired`

---


### Test: `testUstvariNovegaUporabnika()`

**Kaj preizkuša:** Ustvarjanje novega uporabnika z vsemi podatki in shranjevanje v bazo.

**Zakaj je pomemben:** Zagotavlja, da je sistem sposoben pravilno shraniti uporabnika s parametri kot so uporabniško ime, email, geslo in vloga.

**Pozitivni scenarij:** Uporabnik je uspešno shranjen z ID-om in vsemi podatki.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testIskanjePoUporabniskomImenu()`

**Kaj preizkuša:** Iskanje uporabnika po uporabniškem imenu.

**Zakaj je pomemben:** Preverja, da je iskalna funkcionalnost po imenu pravilno implementirana.

**Pozitivni scenarij:** Iskanje vrne pravilnega uporabnika s točnimi podatki.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testIsAdminPozitivenScenarij()`

**Kaj preizkuša:** Preverjanje, da je ADMIN uporabnik res admin.

**Zakaj je pomemben:** Preveri pravilne pravice dostopa za administratorje.

**Pozitivni scenarij:** Metoda `isAdmin()` vrne true za ADMIN vlogo.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`

---

## Opis testov za Recepte (Matija Gusel)

### Test: `testUstvariNovRecept()`

**Kaj preizkuša:** Ustvarjanje novega recepta z vsemi podatki.

**Zakaj je pomemben:** Zagotavlja, da sistem pravilno shranjuje recepte z vsemi informacijami.

**Pozitivni scenarij:** Recept je shranjen z ID-om, imenom, opisom, navodili in avtorom.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testIskanjePoID()`

**Kaj preizkuša:** Iskanje recepta po ID-u.

**Zakaj je pomemben:** Preverja osnovno iskanje po primarnem ključu.

**Pozitivni scenarij:** Recept je pravilno najden po ID-u.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testIskanjePoImenu()`

**Kaj preizkuša:** Iskanje receptov po imenu (case-insensitive).

**Zakaj je pomemben:** Omogoča uporabnikom, da najdejo recepte po imenu.

**Pozitivni scenarij:** Iskanje najde vse recepte, ki vsebujejo iskani niz.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

---

## Opis testov za Jedilnik (Matija Gusel)

### Test: `testUstvariNovJedilnik()`

**Kaj preizkuša:** Ustvarjanje novega jedilnika z vsemi podatki.

**Zakaj je pomemben:** Zagotavlja, da sistem pravilno shranjuje jedilnike z datumom, nazivom in številom oseb.

**Pozitivni scenarij:** Jedilnik je shranjen z ID-om in vsemi podatki.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testIskanjePoUporabniku()`

**Kaj preizkuša:** Iskanje jedilnikov za specifičnega uporabnika.

**Zakaj je pomemben:** Omogoča uporabniku, da vidi svoje jedilnike.

**Pozitivni scenarij:** Metoda vrne le jedilnike tega uporabnika.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

### Test: `testDodajanjeReceptaVJedilnik()`

**Kaj preizkuša:** Dodajanje recepta v jedilnik.

**Zakaj je pomemben:** Preverka, da se recepti pravilno dodajo jedilniku.

**Pozitivni scenarij:** Recept je uspešno dodan in dostopen.

**Uporabljene anotacije:** `@Test`, `@DisplayName`, `@SpringBootTest`, `@Transactional`, `@Autowired`, `@BeforeEach`

##  Avtorji:

- **Anej Vollmeier**
- **Matija Gusel**
