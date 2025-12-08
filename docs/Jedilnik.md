# Dokumentacija dodatne funkcionalnosti: Jedilnik

## Opis funkcionalnosti

**Jedilnik** je funkcionalnost, ki uporabnikom omogoča ustvarjanje in upravljanje lastnih jedilnikov (meal plans). Uporabnik lahko:

- Ustvari nov jedilnik z nazivom, datumom in številom oseb
- Dodaja recepte v jedilnik s strani Recepti
- Pregleduje recepte, ki so dodani v posamezen jedilnik
- Odstranjuje recepte iz jedilnika
- Briše celotne jedilnike

Jedilniki so vezani na prijavljenega uporabnika, tako da vsak uporabnik vidi le svoje jedilnike.

---

## Kako funkcionalnost deluje

### Backend (Spring Boot)

Jedilnik je implementiran kot entiteta `Jedilnik` z naslednjimi polji:
- `id` - unikatni identifikator
- `naziv` - ime jedilnika
- `datum` - datum jedilnika (LocalDate)
- `steviloOseb` - število oseb, za katere je jedilnik namenjen
- `recepti` - seznam receptov (ManyToMany relacija z entiteto `Recepti`)
- `uporabnik` - lastnik jedilnika (ManyToOne relacija z entiteto `Uporabnik`)


### Frontend (React)

Frontend je sestavljen iz dveh glavnih komponent:

1. **Stran Jedilnik (`/jedilnik`)** - prikazuje seznam uporabnikovih jedilnikov z možnostjo:
   - Ustvarjanja novega jedilnika (obrazec z nazivom, datumom, številom oseb)
   - Pregleda receptov v vsakem jedilniku
   - Odstranjevanja receptov iz jedilnika
   - Brisanja jedilnikov

2. **Stran Recepti (`/recepti`)** - poleg obstoječih funkcionalnosti ima gumb "Dodaj v jedilnik", ki:
   - Odpre dropdown z uporabnikovimi jedilniki
   - Omogoči izbiro jedilnika
   - Doda recept v izbrani jedilnik

---

## Kako preizkusiti funkcionalnost

### Predpogoji
1. Zagnana MySQL baza podatkov na portu 3307 z bazo `recepti`
2. Zagnan backend (Spring Boot) na portu 8080
3. Zagnan frontend (Vite/React) na portu 5173

### Koraki za testiranje

#### 1. Registracija in prijava
- Pojdi na stran **Registracija** (`/registracija`)
- Ustvari nov uporabniški račun
- Prijavi se na strani **Prijava** (`/prijava`)

#### 2. Ustvarjanje jedilnika
- V navigaciji klikni na **Jedilnik** (ta povezava je vidna samo prijavljenim uporabnikom)
- V obrazcu "Ustvari nov jedilnik" vnesi:
  - **Naziv**: npr. "Ponedeljkov jedilnik"
  - **Datum**: izberi datum
  - **Število oseb**: npr. 4
- Klikni **Ustvari**
- Jedilnik se pojavi v seznamu spodaj

#### 3. Dodajanje receptov v jedilnik
- Pojdi na stran **Recepti** (`/recepti`)
- Če ni receptov, najprej ustvari recept v obrazcu "Dodaj recept"
- Pri receptu klikni gumb **Dodaj v jedilnik**
- V dropdown meniju izberi želeni jedilnik
- Klikni **Potrdi**
- Prikaže se sporočilo "Recept uspešno dodan v jedilnik"

#### 4. Pregled jedilnika z recepti
- Pojdi nazaj na stran **Jedilnik**
- Klikni **Osveži seznam** ali počakaj na avtomatsko osvežitev
- Pod izbranim jedilnikom so zdaj prikazani dodani recepti

#### 5. Odstranjevanje recepta iz jedilnika
- Na strani Jedilnik poišči jedilnik z receptom
- Pri receptu klikni gumb **Odstrani**
- Recept je odstranjen iz jedilnika

#### 6. Brisanje jedilnika
- Na strani Jedilnik klikni **Izbriši jedilnik**
- Potrdi brisanje
- Jedilnik je izbrisan skupaj z vsemi povezavami na recepte

---

## Tehnične podrobnosti

### Uporabljene tehnologije
- **Backend**: Java 25, Spring Boot 3.5.6, Spring Data JPA, MySQL
- **Frontend**: React 18, Vite, Axios

### Varnost
- Jedilniki so vezani na uporabnika preko `uporabnikId`
- Uporabnik lahko vidi in ureja le svoje jedilnike
- Za dostop do funkcionalnosti Jedilnik mora biti uporabnik prijavljen

