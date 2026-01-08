# Projektno Poročilo


## 1. Osnovni podatki

### Ime skupine
Veriga Njokov

### Člani skupine
- Anđelija Lazarević
- Nela Copot
- Jaka Počkaj

### Kratek opis projekta
Projekt je digitalni dvojček, ki je namenjen spremljanju oblačil, outfitov in stanja modnih trgovin v mestu. Sistem omogoča beleženje uporabe oblačil, kombiniranje outfitov ter spremljanje razmer v trgovinah (npr. koliko ljudi je v posamezni trgovini).

### URL repozitorija
(https://github.com/JakaZaka/veriga-njokov)


## 2. Implementirane funkcionalnosti

### 2.1 Osnovni skelet android aplikacije
- **Predmet:** PORA
- **Odgovoren član:** Jaka Počkaj
- **Commit(i):** 
  - https://github.com/JakaZaka/veriga-njokov/commit/c480d877d8367c09824cb4adac96d7ecccf740a1
  - https://github.com/JakaZaka/veriga-njokov/commit/aa1bf8092d1dab393f4c73dacabda997b4ece1cd
  - https://github.com/JakaZaka/veriga-njokov/commit/88dd92450924bf31a0ae3e716677c1783ccbc23e
- **Opis:** Android aplikacijo za zbiranje senzorskih podatkov (8 senzorjev) z avtomatskim pošiljanjem na strežnik, sistem za objavljanje dogodkov digitalnega dvojčka s MQTT-style topic filteringom, ter simulator ki avtomatsko generira podatke in zaznava ekstremne dogodke (npr. več kot 50 ljudi v trgovini).
- **Screenshoti:**
  
  <img width="490" height="1096" alt="image" src="https://github.com/user-attachments/assets/e2594098-a912-4b1a-b8d1-e358849cb4d4" />
  <img width="495" height="1106" alt="image" src="https://github.com/user-attachments/assets/49ebe755-31ec-4891-8ce4-a7fb43308a23" />
  <img width="495" height="1106" alt="image" src="https://github.com/user-attachments/assets/c76df0b8-bfc5-451d-96ea-75613512db0c" />
  <img width="466" height="1109" alt="image" src="https://github.com/user-attachments/assets/5d1893b6-897b-4b5f-90d9-687d502b6453" />
  <img width="495" height="572" alt="image" src="https://github.com/user-attachments/assets/8ba9112a-8d66-4ba8-8ec4-c423f7dcedaf" />





### 2.2 Osnovni zemljevid in prikaz podatkov in podrobnih informacij na njemu
- **Predmet:** RRI
- **Odgovoren član:** Anđelija Lazarević
- **Commit(i):** 
  - https://github.com/JakaZaka/veriga-njokov/commit/2e7e5a14f2c086f0724c8a8144041e8331d683e9
  - https://github.com/JakaZaka/veriga-njokov/commit/f2126f5d0af4ed526962ce90866c173930783fe7
  - https://github.com/JakaZaka/veriga-njokov/commit/5ae171449f200ef54ae976df897df66d4e48790b
  - https://github.com/JakaZaka/veriga-njokov/commit/174d496a7a5e87008579b5e7df0f7a519a926b64
  - https://github.com/JakaZaka/veriga-njokov/commit/74f83f50d6d898b435be0da7b5a4c6377b66c537
- **Opis:** Aplikacija prikazuje 2D zemljevid, na katerem so na podlagi podatkov iz podatkovne baze označene lokacije trgovin in uporabnikov. Podatki se pridobivajo preko API klicev. Ob kliku na posamezno lokacijo se prikaže pop-up okno z dodatnimi informacijami, kot so ime, kontaktni podatki in ostali podatki, vezani na izbrano lokacijo.
- **Screenshoti:**
1. Prikaz lokacij na zemljevidu
  <img width="1919" height="1035" alt="Screenshot 2026-01-08 195459" src="https://github.com/user-attachments/assets/3aac9e4d-3cb2-4d27-b77a-24d69992cd2a" />
2. Prikaz podrobnih informacij ob kliku na določeno lokacijo
  <img width="1918" height="1032" alt="Screenshot 2026-01-08 195511" src="https://github.com/user-attachments/assets/4711a87f-2239-40ca-8066-867a539535f9" />
  <img width="1894" height="1024" alt="Screenshot 2026-01-08 195524" src="https://github.com/user-attachments/assets/e64fcdb1-1926-412d-8554-312d50271eba" />

### 2.3 [Ime funkcionalnosti 3]
- **Predmet:** [Ime predmeta]
- **Odgovoren član:** [Ime Priimek]
- **Commit(i):** 
  - [Link do commita](https://github.com/username/repo/commit/hash)
- **Opis:** [Kratek opis funkcionalnosti v 1-2 stavkih]

---

## 3. Načrtovane funkcionalnosti (še neimplementirane)

### 3.1 [Ime načrtovane funkcionalnosti 1]
- **Predmet:** PORA
- **Odgovoren član:** Jaka Počkaj
- **Okvirni rok:** 15. 1. 2026
- **Opis:** Android aplikacija bo preko strežniškega dela, implementiranega pri predmetu Spletno programiranje, pošiljala zaznane in simulirane podatke na strežnik. Podatki bodo posredovani v obliki dogodkov, ki bodo vsebovali tip podatka, čas in lokacijo ter se bodo shranjevali v podatkovno bazo za nadaljnjo obdelavo v digitalnem dvojčk

### 3.2 Prikazovanje števila ljudi v določeni trgovini na zemljevidu
- **Predmet:** RRI
- **Odgovoren član:** Jaka Počkaj
- **Okvirni rok:** 13.1.2026
- **Opis:** Na zemljevidu se bo pri posameznih trgovinah prikazovalo število ljudi v obliki grafičnih simbolov, ki bodo predstavljali trenutno število obiskovalcev v posamezni trgovini.

### 3.3 Simulacija števila ljudi v trgovinah skozi čas
- **Predmet:** RRI
- **Odgovoren član:** Nela Copot
- **Okvirni rok:** 15.1.2026
- **Opis:** Uporabniku bo na zemljevidu omogočena izbira dneva in ure (npr. sreda ob 17. uri). Na podlagi do sedaj zbranih podatkov se bo pri posameznih trgovinah prikazalo predvideno število ljudi v izbranem časovnem obdobju z uporabo grafičnih simbolov.

### 3.4 [Ime načrtovane funkcionalnosti 3]
- **Predmet:** [Ime predmeta]
- **Odgovoren član:** [Ime Priimek]
- **Okvirni rok:** [Datum]
- **Opis:** [Kratek opis kaj bo funkcionalnost počela]
