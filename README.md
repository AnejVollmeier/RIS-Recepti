# RIS-Recepti

## -Kratek opis:

RIS-Recepti je aplikacija za upravljanje in deljenje kuharskih receptov. Projekt ima ločen frontend (JavaScript/React, Vite) in backend (Java, Spring Boot), kar omogoča neodvisen razvoj, testiranje in zagon komponent.

## Vizija projekta
RIS-Recepti je spletna aplikacija za upravljanje in deljenje kuhinjskih receptov, namenjena tako domačim kuharjem kot tudi naprednejšim uporabnikom, ki želijo organizirati, ocenjevati in izmenjevati recepte. Cilj aplikacije je poenostaviti iskanje in shranjevanje zaupanih receptov, olajšati pripravo obrokov s samodejno generiranimi seznamih sestavin in izboljšati skupnostno izmenjavo znanj z ocenjevanjem, komentarji in moderiranjem vsebin. Z RIS-Recepti želimo izboljšati uporabniško izkušnjo pri iskanju idej za kuhanje, prihraniti čas pri sestavljanju nakupovalnih list in podpreti kakovostno vsebino, ki jo ustvarjajo uporabniki.

Kljub temu da je aplikacija enostavna za uporabo, je dovolj zmogljiva, da podpira:
- hitro iskanje receptov po sestavinah, dietah in oznakah,
- enostavno ustvarjanje in urejanje receptov (vključno z nalaganjem fotografij),
- organizacijo receptov z zvezdicami/oznakami in shranjevanjem v osebne sezname,
- sodelovanje skupnosti (ocene, komentarji, poročila in moderacija).

Ciljna publika: domači kuharji, študenti, zaposleni, kuharski navdušenci in administratorji/urejevalci vsebine.

## -Dokumentacija za razvijalce:

- Struktura projekta (pomembne datoteke in mape):

  ```text
  RIS-Recepti/
  ├── .gitignore
  ├── README.md
  ├── docs/
  │   └── naloge.md            # Besednjak in ostale dokumentacije
  └── src/
      ├── Backend/
      │   └── Recepti/
      │       ├── pom.xml
      │       └── src/main/java/si/um/feri/ris/projekt/Recepti/
      │           ├── ReceptiApplication.java
      │           ├── dao/ReceptiJpaDao.java
      │           ├── rest/ReceptiRestController.java
      │           └── vao/
      │               ├── Recepti.java
      │               └── Sestavine.java
      │       └── src/main/resources/application.properties
      └── Frontend/
          └── Recepti/
              ├── package.json
              ├── .env
              └── src/
                  ├── main.jsx
                  ├── App.jsx
                  └── components/
  ```

- Standardi kodiranja in smernice:

  - Java: sledite običajnim Java konvencam (ime paketov, camelCase za metode, PascalCase za razrede). Uporabite JDK 17+.
  - Spring Boot: konfiguracije v `application.properties` (ali `application.yml`) v `src/main/resources`.
  - JavaScript/React: upoštevajte ESLint konfiguracijo v `Frontend/Recepti/` in dovoljene konvencije v `eslint.config.js`.
  - Komit sporočila: kratka predpona (`feat:`, `fix:`, `chore:`) + opis.

- Orodja in priporočene verzije:

  - Java 17 ali višje
  - Maven 3.8+
  - Node.js 18+ in npm 9+
  - MySQL 8.x
  - Vite (kot del frontend setup)

- Build in test ukazi (osnovno):

  - Backend: v mapi `Backend/Recepti/`:

    ```cmd
    mvn clean package
    mvn test
    mvn spring-boot:run
    ```

  - Frontend: v mapi `Frontend/Recepti/`:

    ```cmd
    npm install
    npm run dev
    npm run build
    ```

## -Navodila za nameščanje:

1. Namestite potrebna orodja (Java JDK, Maven, Node.js, npm, MySQL).
2. Nastavite MySQL bazo:

   - Za ta projekt naj bo MySQL dostopen na `localhost:3307`.
   - Ustvarite bazo `recepti` (če še ne obstaja):

     ```sql
     CREATE DATABASE recepti CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
     ```

   - Uporabnik: `root`, geslo: `root` (kot v primer konfiguracije spodaj).

3. Backend konfiguracija:

   - Konfiguracijske vrednosti so v datoteki `Backend/src/main/resources/application.properties`.
   - Če datoteka v repozitoriju nosi drugačen ime/put (npr. `Backend/src/main/resourses/aplication.propertis`), preverite in po potrebi popravite ime mape/ datoteke.
   - Primer vsebine (kopirajte/ustvarite v `Backend/src/main/resources/application.properties`):

     ```properties
     spring.application.name=Recepti
     spring.datasource.url=jdbc:mysql://localhost:3307/recepti
     spring.datasource.username=root
     spring.datasource.password=root
     spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
     spring.jpa.show-sql=true
     spring.jpa.generate-ddl=true
     ```

   - Zaženite backend (v `Backend/Recepti/`):

     ```cmd
     mvn spring-boot:run
     ```

4. Frontend konfiguracija:

   - V mapi `Frontend/Recepti/` ustvarite datoteko `.env` (ali urejte obstoječo) z naslednjo vsebino:

     ```text
     VITE_BASE_URL=http://localhost:8080/
     ```

   - Namestite odvisnosti in zaženite razvojni strežnik:

     ```cmd
     npm install
     npm run dev
     ```

5. Dostop do aplikacije:
   - Backend bo privzeto na `http://localhost:8080/` (če je port spremenjen, preverite `application.properties`).
   - Frontend bo po privzetem na naslovu, ki ga izpiše `npm run dev` (običajno `http://localhost:5173/`), in kliče backend preko `VITE_BASE_URL`.

## - Navodila za razvijalce:

- Veje in prispevanje:

  - Uporabljajte veje po vzorcu `feature/*`, `bugfix/*`, `hotfix/*`.
  - Pred pošiljanjem Pull Requesta poskrbite za:
    - preizkus osnovne funkcionalnosti lokalno;
    - če je mogoče, enotske teste;
    - posodobitev dokumentacije, če so spremembe v API-ju ali konfiguraciji.

- Testiranje in linting:

  - Za backend zaženite `mvn test`.
  - Za frontend zaženite `npm run lint` (če je ukaz definiran) ali `npm test`.

- Lokalno debugging:

  - Backend: uporabite IDE (IntelliJ/VSCode) za zagon aplikacije v debug načinu.
  - Frontend: uporabite brskalnik in devtools.

- Kontakt in PR pravila:
  - Jasno opišite namen PR-a in navodila za testiranje.
  - Označite morebitne odvisnosti ali zahteve za bazo.

## Konfiguracija okolja (pregled)

- Frontend `.env` (v `Frontend/Recepti/.env`):

```text
VITE_BASE_URL=http://localhost:8080/
```

- Backend `application.properties` (v `Backend/src/main/resources/application.properties`):

```properties
spring.application.name=Recepti
spring.datasource.url=jdbc:mysql://localhost:3307/recepti
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.show-sql=true
spring.jpa.generate-ddl=true
```

---

Opomba: če imate v projektu datoteko `Backend/src/main/resourses/aplication.propertis` s tipkarsko napako v imenu, jo preimenujte v `resources/application.properties`, da jo Spring Boot pravilno poišče.

# Diagram Primerov Uporabe
(image.png)
# Besednjak

| Izraz | Razlaga |
|---|---|
| Recept | Entiteta, ki vsebuje naslov, opis, seznam sestavin, korake priprave in metapodatke (avtor, datum, ocena). |
| Sestavina | Element recepta z imenom in količino (npr. "200 g moke"). |
| Kategorija (Category) | Vsebinski sklop receptov (npr. Sladice, Glavne jedi), uporaben za filtriranje. |
| Oznaka / Tag | Poljubna oznaka za dodatno označevanje receptov (npr. hitro, vegansko, brezglutensko). |
| Ocena (Rating) | Numerična ocena recepta (1–5), pogosto uporabljena za izračun povprečne ocene. |
| Komentar (Comment) | Besedilno sporočilo, ki ga uporabnik doda pod recept. |
| Favoriti (Favorites) | Seznam receptov, ki jih uporabnik shranjuje kot priljubljene. |
| Uporabniški profil (User Profile) | Podatki o uporabniku (ime, email, avatar, bio). |
| Frontend | Odjemalska aplikacija napisana v React (Vite) — prikazuje uporabniški vmesnik in kliče REST API backend-a. |
| Backend | Strežniška aplikacija napisana s Spring Boot — skrbi za poslovno logiko, persistenco in REST API. |
| REST API | HTTP vmesnik za izmenjavo podatkov med frontend-om in backend-om. |
| CRUD | Operacije Create, Read, Update, Delete, osnovne operacije nad entitetami (recept, sestavina). |
| Endpoint | Končna URL točka REST API-ja (npr. GET /api/recepti). |
| Request Body / Response Body | JSON podatki, ki jih API prejme ali vrne. |
| Avtentikacija (Authentication) | Proces preverjanja identitete uporabnika (prijava). |
| Avtorizacija (Authorization) | Določanje pravic uporabnika (npr. admin lahko briše vsebino). |
| Admin | Uporabniški račun z razširjenimi pravicami (upravljanje uporabnikov, moderacija vsebin). |
| DTO (Data Transfer Object) | Oblika podatkov, ki se uporablja za prenos med plastmi aplikacije. |
| ER diagram | Diagram entitet in njihovih relacij (predlog za nadaljnjo dokumentacijo baze). |
