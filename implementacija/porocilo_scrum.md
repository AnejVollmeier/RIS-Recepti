# Poročilo o napredku - Scrum Sprint

**Datum:** 21.12.2025  
**Projekt:** RIS-Recepti  
**Repozitorij:** AnejVollmeier/RIS-Recepti

## Pregled sprinta

V tem sprintu smo se osredotočili na implementacijo funkcionalnosti, povezanih s **porcijami, količinami in merskimi enotami** v aplikaciji za upravljanje receptov.

## Naloge v sprintu

| Ime                                                                                                                | Opis                                                                                | Story Points |
| ------------------------------------------------------------------------------------------------------------------ | ----------------------------------------------------------------------------------- | ------------ |
| [Backend: Dodaj polje za število porcij v recept model](https://github.com/AnejVollmeier/RIS-Recepti/issues/2)     | Posodobi podatkovni model, ustvari migracijo                                        | 2            |
| [Backend: Implementiraj logiko za izračun količin sestavin](https://github.com/AnejVollmeier/RIS-Recepti/issues/3) | Ustvari metodo za pretvorbo količin, podpora za različne enote (g, kg, ml, l, itd.) | 5            |
| [Frontend: Dodaj UI element za izbiro števila porcij](https://github.com/AnejVollmeier/RIS-Recepti/issues/4)       | Dodaj slider ali input polje, implementiraj validacijo                              | 3            |
| [Frontend: Dinamično posodobi prikaz sestavin](https://github.com/AnejVollmeier/RIS-Recepti/issues/5)              | Implementiraj real-time izračun, posodobi prikaz sestavin ob spremembi              | 5            |
| [Testiranje: Napiši unit teste za izračun količin](https://github.com/AnejVollmeier/RIS-Recepti/issues/6)          | Backend testi                                                                       | 3            |
| [Dokumentacija: Posodobi dokumentacijo](https://github.com/AnejVollmeier/RIS-Recepti/issues/7)                     | Dodaj primere uporabe, posodobi README                                              | 1            |

**Skupaj Story Points:** 19

## Izvedeno delo

### 1. Frontend implementacija

- **Dodana slider komponenta** za prilagajanje števila porcij (list. jsx in form.jsx)
  - Omogoča uporabnikom dinamično spreminjanje števila porcij
  - Commit: [359d004](https://github.com/AnejVollmeier/RIS-Recepti/commit/359d004d8b51b2fa0d0627837cb20935b91380a6)

---

- **Frontend implementacija sestavin**
  - Dodana podpora za prikaz sestavin z merskimi enotami
  - Commit: [2ee7427](https://github.com/AnejVollmeier/RIS-Recepti/commit/2ee742735728d28c9a1a5add7dafb26aaec939c2)

---

### 2. Backend implementacija

- **Polje za število porcij**
  - Dodano polje za shranjevanje števila porcij na backendu
  - Commit: [49553db](https://github.com/AnejVollmeier/RIS-Recepti/commit/49553db54655ad62f8b6a8c615b068a2fc2c86ad)

---

- **Funkcionalnost pretvorbe količin**
  - Implementirana logika za avtomatično pretvorbo količin glede na število porcij
  - Commit: [ff8e224](https://github.com/AnejVollmeier/RIS-Recepti/commit/ff8e2245370fa42d7340a8bd8c82a2cd1608fa7d)

---

- **Podpora za števila brez enote**
  - Popravljen backend za pravilno obravnavo sestavin, ki ne zahtevajo merskih enot (npr. "2 jajca")
  - Commit: [2ee7427](https://github.com/AnejVollmeier/RIS-Recepti/commit/2ee742735728d28c9a1a5add7dafb26aaec939c2)

---

### 3. Testiranje

- **Testi za količine**
  - Dodani avtomatski testi za preverjanje pravilnosti izračuna količin
  - Commit: [9be6ae5](https://github.com/AnejVollmeier/RIS-Recepti/commit/9be6ae56358db72d6a12f3a77ad13e499220da9e)

---

## Prispevki članov ekipe

| Član ekipe        | Prispevki                                                                                                        |
| ----------------- | ---------------------------------------------------------------------------------------------------------------- |
| **AnejVollmeier** | Implementacija slider komponente za prilagajanje porcij, dodajanje testov za količine, dokumentacija             |
| **MatijaGusel**   | Backend implementacija (porcije, pretvorba količin, podpora za števila brez enot), frontend integracija sestavin |

## Ključne funkcionalnosti

✅ Dinamično prilagajanje števila porcij  
✅ Avtomatični preračun količin sestavin  
✅ Podpora za merske enote in števila brez enot  
✅ Testna pokritost za funkcionalnost količin

## Opombe

Vsi commiti so bili izvedeni med 20.12.2025 in 21.12.2025. Več informacij o commitih je dostopnih na: [GitHub - Commits](https://github.com/AnejVollmeier/RIS-Recepti/commits)

---

_Poročilo pripravljeno: 21.12.2025_
