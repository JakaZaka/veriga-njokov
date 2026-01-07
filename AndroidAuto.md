# Poročilo: eCharge – Android aplikacija z Android Auto integracijo

**Avtor:** Jaka Počkaj  
**Predmet:** Platformno odvisen razvoj aplikacij  
**Letnik:** 3. letnik, RIT-UNI  
**Leto:** 2025/2026  

---

## 1. Primernost izbrane tehnologije

Glavni poudarek projekta eCharge je uporaba knjižnice **Android for Cars App Library**, ki omogoča razvoj aplikacij za okolje **Android Auto**. Gre za jasno platformno odvisno tehnologijo, saj aplikacije, razvite s to knjižnico, delujejo izključno v avtomobilskem okolju in morajo upoštevati posebna varnostna pravila.

Aplikacija eCharge je bila najprej razvita kot običajna Android aplikacija, nato pa prilagojena tako, da deluje tudi v Android Auto emulatorju. S tem je bila demonstrirana razširitev obstoječe mobilne aplikacije v platformno specifično okolje.

---

### Android for Cars App Library
- **Licenca:** Apache License 2.0  
- **Verzija:** 1.4.0  

Android for Cars App Library je uradna Google knjižnica za razvoj aplikacij, ki delujejo v okolju Android Auto in Android Automotive OS.

**Prednosti:**
- uradno podprta knjižnica s strani Googla,
- namenjena varni uporabi aplikacij med vožnjo,
- uporaba vnaprej definiranih UI predlog (templates),
- omogoča enostavno razširitev obstoječe Android aplikacije z Auto podporo.

**Slabosti:**
- zelo omejene možnosti uporabniškega vmesnika,
- aplikacija mora slediti strogim pravilom glede interakcije,
- testiranje zahteva Android Auto emulator ali združljivo napravo,
- potrebna dodatna konfiguracija (CarAppService, manifest).

**Vzdrževanje:**  
Knjižnico vzdržuje Google kot del Android ekosistema. Dokumentacija in primeri so redno posodobljeni, razvoj pa sledi novim verzijam Android platforme.

---

## 2. Lastna uporaba na GitHubu

V okviru projekta sem razvil aplikacijo **eCharge**, ki prikazuje lokacije električnih polnilnic in omogoča njihov pregled tudi v Android Auto okolju.

Aplikacija ni obsežna in je namenjena prikazu osnovnih funkcionalnosti knjižnice Android for Cars App Library.

### Osnovne funkcionalnosti

#### Mobilna aplikacija
- prikaz zemljevida z lokacijami električnih polnilnic,
- prikaz trenutne lokacije uporabnika,
- dodajanje, urejanje in brisanje polnilnic,
- izračun razdalje do izbrane polnilnice,
- pošiljanje potisnih obvestil ob dodajanju nove polnilnice.

#### Android Auto integracija
- implementiran minimalni `CarAppService`,
- prikaz seznama polnilnic v Android Auto vmesniku,
- uporaba template sistema, ki zagotavlja varno uporabo med vožnjo.

V GitHub repozitoriju je dodana dokumentacija in zaslonski posnetki delovanja aplikacije.

---

## 3. Demonstracija izjem

V aplikaciji so obravnavani osnovni primeri izjem:
- manjkajoča dovoljenja za dostop do lokacije,
- nedosegljiv GPS,
- napačni vhodni podatki pri vnosu polnilnice,
- primer, ko seznam polnilnic še ni inicializiran.

V vseh primerih je uporabnik o težavi obveščen z ustreznim opozorilnim sporočilom.

---

## 4. Uporaba v lastnem projektu

Knjižnica Android for Cars App Library je bila uporabljena neposredno v lastnem projektu eCharge. Obstoječa Android aplikacija je bila nadgrajena z Android Auto podporo, s čimer je bila demonstrirana prilagoditev aplikacije platformno odvisnemu okolju.

---

## 5. Viri

- https://developer.android.com/training/cars/apps/auto  
- https://github.com/osmdroid/osmdroid  
- https://developer.android.com/training/location  
- https://developer.android.com/develop/ui/views/notifications  
<img width="1302" height="554" alt="image" src="https://github.com/user-attachments/assets/192c5704-87bb-4935-8bef-d81cd947df15" />
<img width="973" height="544" alt="image" src="https://github.com/user-attachments/assets/064930bc-cd20-4275-8231-9202ff52e69b" />
<img width="971" height="549" alt="image" src="https://github.com/user-attachments/assets/3a88e574-e7f3-45c4-b34e-3dca2d885fe7" />
