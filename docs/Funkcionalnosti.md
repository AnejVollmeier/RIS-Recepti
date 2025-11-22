# 1.Dodajanje svojih receptov

## Cilj

Registriran uporabnik želi v sistem dodati svoj recept, da ga lahko kasneje uporablja in deli z drugimi.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.

## Stanje sistema po PU

- Sistem shrani nov recept v bazo podatkov in ga poveže z uporabnikovim računom.
- Recept je viden drugim (če je javni) ali samo lastniku (če je zasebni – odvisno od nastavitev).

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Izbere možnost »Dodajanje svojih receptov«.
3. Vpiše naslov, opis, sestavine, korake priprave ter druge podatke.
4. Po želji doda sliko.
5. Klikne gumb »Shrani recept«.
6. Sistem potrdi shranjevanje in recept se prikaže med uporabnikovimi recepti.

## Alternativni tokovi

- Če uporabnik ne izpolni obveznih polj (npr. naslov), sistem prikaže obvestilo in zahteva dopolnitev.

## Izjeme

- Če pride do napake pri shranjevanju v bazo, sistem prikaže napako in recept se ne shrani.

---

# 2.Spreminjanje svojih receptov

## Cilj

Registriran uporabnik želi popraviti ali posodobiti že dodan recept (npr. popraviti napako ali dodati nove informacije).

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Recept mora biti že ustvarjen in pripadati temu uporabniku.

## Stanje sistema po PU

- Sistem posodobi podatke recepta v bazi.
- Vsi, ki vidijo recept, vidijo posodobljeno verzijo.

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Odpre seznam svojih receptov.
3. Izbere recept, ki ga želi spremeniti.
4. Klikne na možnost »Uredi recept«.
5. Spremeni želene podatke (npr. sestavine, korake, naslov).
6. Klikne »Shrani spremembe«.
7. Sistem posodobi recept in prikaže posodobljeno verzijo.

## Alternativni tokovi

- Če uporabnik prekliče urejanje, se spremembe ne shranijo.

## Izjeme

- Če recept med urejanjem izbriše admin, sistem pri shranjevanju javi napako.

---

# 3.Ogled svojih receptov

## Cilj

Registriran uporabnik želi pregledati vse recepte, ki jih je sam dodal.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Uporabnik mora imeti vsaj en ustvarjen recept.

## Stanje sistema po PU

- Sistem prikaže seznam uporabnikovih receptov z osnovnimi podatki.

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Izbere možnost »Ogled svojih receptov«.
3. Sistem prikaže seznam njegovih receptov.
4. Uporabnik lahko klikne posamezen recept za podrobni ogled.

## Alternativni tokovi

- Če uporabnik nima nobenega recepta, sistem prikaže prazno listo in predlaga dodajanje novega recepta.

## Izjeme

- Če pride do napake pri branju iz baze, sistem prikaže sporočilo o napaki.

---

# 4.Ogled receptov

## Cilj

Uporabnik (registriran ali neregistriran) želi pregledovati recepte, ki so javno dostopni v aplikaciji.

## Akterji

- Neregistriran uporabnik
- Registriran uporabnik

## Predpogoji

- Sistem ima v bazi vsaj en javni recept.

## Stanje sistema po PU

- Sistem prikaže seznam receptov, ki jih lahko uporabnik pregleduje.

## Scenarij

1. Uporabnik odpre aplikacijo.
2. Izbere možnost »Ogled receptov«.
3. Sistem prikaže seznam receptov (npr. z iskanjem, filtriranjem).
4. Uporabnik izbere recept in se mu prikaže podroben opis.

## Alternativni tokovi

- Če ni nobenega recepta, sistem prikaže obvestilo, da trenutno ni vsebine.

## Izjeme

- Če pride do napake pri nalaganju receptov, sistem prikaže sporočilo o napaki.

---

# 5.Všečkanje receptov

## Cilj

Registriran uporabnik želi z »všečkom« označiti recept, ki mu je všeč, in s tem izraziti podporo avtorju.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Recept mora biti prikazan (npr. preko »Ogled receptov«).

## Stanje sistema po PU

- Sistem zabeleži, da je uporabnik všečkal recept.
- Število všečkov recepta se posodobi.

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Odpre recept, ki mu je všeč.
3. Klikne gumb »Všečkaj«.
4. Sistem zabeleži všeček in posodobi prikazano število všečkov.

## Alternativni tokovi

- Če je uporabnik recept že všečkal, klik na gumb lahko odstrani všeček (toggle).

## Izjeme

- Če pride do napake pri shranjevanju, sistem prikaže napako in ne spremeni števila všečkov.

---

# 6.Ocenjevanje receptov

## Cilj

Registriran uporabnik želi recept oceniti (npr. z zvezdicami), da prispeva k povprečni oceni recepta.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Recept mora biti objavljen in dostopen za ogled.

## Stanje sistema po PU

- Sistem shrani oceno uporabnika.
- Posodobi povprečno oceno recepta in jo prikaže drugim uporabnikom.

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Odpre podroben pogled izbranega recepta.
3. Izbere oceno (npr. 1–5 zvezdic).
4. Potrdi oceno.
5. Sistem shrani oceno in prikaže posodobljeno povprečno oceno.

## Alternativni tokovi

- Če je uporabnik recept že ocenil, mu sistem dovoli spremembo ocene in ponovno izračuna povprečje.

## Izjeme

- Če je podana ocena izven dovoljenega razpona, sistem prikaže napako in ocene ne shrani.

---

# 7.Komentiranje receptov

## Cilj

Registriran uporabnik želi pod recept dodati komentar (npr. mnenje, vprašanje, nasvet).

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Recept mora biti objavljen in dostopen.

## Stanje sistema po PU

- Sistem shrani komentar in ga prikaže pod receptom.

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Odpre podroben pogled izbranega recepta.
3. Vpiše besedilo komentarja v vnosno polje.
4. Klikne gumb »Dodaj komentar«.
5. Sistem shrani komentar in ga prikaže v seznamu komentarjev.

## Alternativni tokovi

- Če je komentar prazen ali predolg, sistem prikaže opozorilo in komentarja ne shrani.

## Izjeme

- Če pride do napake pri shranjevanju komentarja, sistem prikaže sporočilo o napaki.

---

# 8.Dodaj v jedilnik

## Cilj

Registriran uporabnik želi izbrane recepte dodati v svoj jedilnik (npr. tedenski plan obrokov).

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti prijavljen.
- Recept mora biti objavljen in dostopen.

## Stanje sistema po PU

- Sistem doda izbrani recept v uporabnikov jedilnik skupaj z dodatnimi podatki (datum, št. oseb, alergije).

## Scenarij

1. Uporabnik se prijavi v sistem.
2. Odpre recept, ki ga želi dodati v jedilnik.
3. Izbere možnost »Dodaj v jedilnik«.
4. Sistem zahteva izbiro datuma, števila oseb in po potrebi alergij.
5. Uporabnik vpiše/izbere zahtevane podatke.
6. Potrdi dodajanje v jedilnik.
7. Sistem shrani zapis v uporabnikov jedilnik.

## Alternativni tokovi

- Če uporabnik ne določi datuma, sistem ne dovoli shranjevanja in zahteva vnos.

## Izjeme

- Če pride do napake pri shranjevanju jedilnika, sistem prikaže napako in podatkov ne shrani.

---

# 9.Izbira datuma (use case, ki je vključen pri »Dodaj v jedilnik«)

## Cilj

Pri dodajanju recepta v jedilnik želi uporabnik izbrati, na kateri dan bo recept pripravljen.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti v procesu dodajanja recepta v jedilnik.

## Stanje sistema po PU

- Sistem shrani izbrani datum kot del vnosa v jedilnik.

## Scenarij

1. Sistem prikaže koledar ali polje za vnos datuma.
2. Uporabnik izbere želeni datum.
3. Sistem sprejme datum in ga poveže z izbranim receptom v jedilniku.

## Alternativni tokovi

- Če uporabnik izbere datum v preteklosti (če to ni dovoljeno), sistem prikaže opozorilo in zahteva nov datum.

## Izjeme

- Če je format datuma neveljaven, sistem prikaže napako in datuma ne shrani.

---

# 10.Izbira alergij (extend pri dodajanju v jedilnik – dodatna funkcionalnost)

## Cilj

Uporabnik želi označiti alergije ali sestavine, ki jih želi izključiti pri načrtovanju jedilnika (za konkreten recept ali za predlog zamenjav).

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti v procesu dodajanja recepta v jedilnik.

## Stanje sistema po PU

- Sistem shrani označene alergije in jih upošteva pri prikazu sestavin ali zamenjav.

## Scenarij

1. Sistem prikaže seznam možnih alergenov (npr. gluten, laktoza, oreščki).
2. Uporabnik izbere alergije, ki ga zadevajo.
3. Potrdi izbor.
4. Sistem shrani podatke in po potrebi označi problematične sestavine v receptu ali predlaga alternative.

## Alternativni tokovi

- Uporabnik ne izbere nobene alergije in nadaljuje brez sprememb.

## Izjeme

- Če pride do napake pri shranjevanju alergij, sistem prikaže napako, a dodajanje recepta v jedilnik vseeno lahko nadaljuje brez alergij.

---

# 11.Izberi število oseb (extend pri dodajanju v jedilnik – dodatna funkcionalnost)

## Cilj

Uporabnik želi določiti, za koliko oseb bo recept pripravljen, da se ustrezno preračunajo količine sestavin.

## Akterji

- Registriran uporabnik

## Predpogoji

- Uporabnik mora biti v procesu dodajanja recepta v jedilnik.

## Stanje sistema po PU

- Sistem shrani število oseb in na tej osnovi preračuna količine sestavin.

## Scenarij

1. Sistem prikaže polje za vnos števila oseb.
2. Uporabnik vnese željeno število (npr. 4).
3. Sistem preračuna količine sestavin in jih prikaže.
4. Podatek se shrani skupaj z vnosom v jedilnik.

## Alternativni tokovi

- Če uporabnik ne spremeni števila oseb, sistem privzeto uporabi osnovno količino (npr. za 2 osebi).

## Izjeme

- Če uporabnik vnese neveljavno vrednost (npr. 0 ali negativno število), sistem prikaže napako in zahteva ponoven vnos.

---

# 12.Brisanje vseh komentarjev

## Cilj

Admin želi odstraniti vse komentarje (npr. zaradi neprimerne vsebine ali reseta sistema).

## Akterji

- Admin

## Predpogoji

- Uporabnik mora biti prijavljen kot admin.
- V sistemu morajo obstajati komentarji.

## Stanje sistema po PU

- Vsi komentarji se izbrišejo iz baze.
- Pri receptih se prikaže, da ni komentarjev.

## Scenarij

1. Admin se prijavi v sistem.
2. Odpre administracijski vmesnik.
3. Izbere možnost »Brisanje vseh komentarjev«.
4. Sistem prikaže opozorilo in zahteva potrditev.
5. Admin potrdi brisanje.
6. Sistem izbriše vse komentarje in prikaže potrditev uspeha.

## Alternativni tokovi

- Admin prekliče brisanje, komentarji ostanejo nespremenjeni.

## Izjeme

- Če pride do napake pri brisanju, sistem prikaže napako in komentarji ostanejo.

---

# 13.Brisanje vseh receptov

## Cilj

Admin želi iz sistema odstraniti vse recepte (npr. ob ponovnem zagonu sistema ali večji kršitvi pogojev uporabe).

## Akterji

- Admin

## Predpogoji

- Uporabnik mora biti prijavljen kot admin.
- V sistemu obstajajo recepti.

## Stanje sistema po PU

- Vsi recepti se izbrišejo iz baze podatkov.
- Povezani podatki (ocenitve, komentarji, všečki, vnosi v jedilnik) se po pravilih sistema prav tako izbrišejo ali označijo kot neveljavni.

## Scenarij

1. Admin se prijavi v sistem.
2. Odpre administracijski vmesnik.
3. Izbere možnost »Brisanje vseh receptov«.
4. Sistem prikaže opozorilo o posledicah brisanja.
5. Admin ponovno potrdi, da želi izbrisati vse recepte.
6. Sistem izbriše recepte in prikaže potrditev.

## Alternativni tokovi

- Admin prekliče brisanje, recepti ostanejo v sistemu.

## Izjeme

- Če pride do napake pri brisanju, sistem prikaže napako in recepti delno ali v celoti ostanejo; sistem mora adminu jasno povedati, kaj se je zgodilo.
