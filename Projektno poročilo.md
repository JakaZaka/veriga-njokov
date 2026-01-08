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



### 2.3 Blockchain (paralelizacija algoritma rudarjenje blokov z MPI) 
- **Predmet:** PIPR (paralelno in porazdeljeno računanje)
- **Odgovoren član:** Nela Copot
- **Commit(i):** 
  - [Commit 1](https://github.com/JakaZaka/veriga-njokov/commit/c917adb88ad40f0b76a4fd3b3054b7e092f16505)
  - [Commit 2](https://github.com/JakaZaka/veriga-njokov/commit/eb03074df3d15ba7f8c06ca8dbb259f08d0d5917)
  - [Commit 3](https://github.com/JakaZaka/veriga-njokov/commit/2d1efa14f6331f800265b6b8f5d4d04a805244ea)
  - [Commit 4](https://github.com/JakaZaka/veriga-njokov/commit/e6d6daaf0867ad75afd993810b4d127517cea930)
  - [Commit 5](https://github.com/JakaZaka/veriga-njokov/commit/249abc8718cd33d123034eba37a9429d2023b012)

- **Opis:** 

Naredila algoritem za rudarjenje blokov, ga paralelizirala z mpi tako da si pošiljajo bloke med sabo. Naredila tudi skripto, ki omogoča povezavo s PORA aplikacijo (vnos in sprožitev izjemnih dogodkov), torej izjemni dogodki se lahko shranjujejo v to verzijo blockchain-a.

- **Screenshoti:**

  Med rudarjenjem:
  ![Screenshot funkcionalnosti 3](images/screenshot1.png)
  Po koncu rudanjenja:


  ![Screenshot funkcionalnosti 3](images/screenshot2.png)
  
  Na podlagi tega se bodo potem delale analize, pohitritve (poročilo pohitritev ipd. pri predmetu)

  Povezava z PORA aplikacijo:

  Po zagonu:
  ![Screenshot funkcionalnosti 3](images/screenshot3.png)

  Po poslanem JSON, ki opisuje izjemen dogodek:
  ![Screenshot funkcionalnosti 3](images/screenshot4.png)



### 2.4 Nastavitve aplikacije
- **Predmet:** PORA
- **Odgovoren član:** Nela Copot
- **Commit(i):** 
  - https://github.com/JakaZaka/veriga-njokov/commit/a4f5f4f620e0f45247ad9b1a1526e43afc6a27d7
- **Opis:** Dodan settings screen, z nastavitvami za onemogočanje obvestil, izbira jezika in izbira med dnevnim in nočnim načinom
- **Screenshoti:**
  <img width="381" height="638" alt="image" src="https://github.com/user-attachments/assets/66ed357a-d5f1-4e2b-82a2-16f531d46223" />



### 2.4 Simuliranje senzorjev
- **Predmet:** PORA
- **Odgovoren član:** Anđelija Lazarević
- **Commit(i):** 
  - https://github.com/JakaZaka/veriga-njokov/commit/b674ba55cf20e22225c9a21a0b51646607138dec
- **Opis:** Simuliranje števila ljudi v trgovini, podatek se na vsak interval časa preko zalednega dela spletne aplikacije shrani v podatkovno bazo.
- **Screenshoti:**
  <img width="373" height="742" alt="image" src="https://github.com/user-attachments/assets/9e955a7b-f812-4a19-a3e4-864f94d38b00" />

### 2.4 Štetje ljudi na sliki in prva verzija modela učenja za prepoznavo tipa oblačila
- **Predmet:** URVRV (Uvod v računalniški vid in razpoznavo vzorcev)
- **Odgovoren član:** Nela Copot
- **Commit(i):** 
  - [Commit 1](https://github.com/JakaZaka/veriga-njokov/commit/ecab4e7d8d1af0745c2a609a4554d92688b7b801)
  - [Commit 2](https://github.com/JakaZaka/veriga-njokov/commit/bb4ed3219dc6054549a4521dd9def4350bea47db)

- **Opis:**

Model, ki zna prešteti število ljudi na sliki (torej v trgovini v našem primeru), narejen s pomočjo Yolov8n modela. Nato še začetek dela na modelu razpoznave kateri tip oblačila je na sliki (majica, hlače, čevlji...) s pomočjo Fashion-MNIST dataset, ki je del Pythonove tensorflow knjižnice.

- **Screenshoti:**
  Prvi model:
  ![Screenshot funkcionalnosti 4](images/primer_1_annotated.jpg)

  Osnova drugega modela:
  ![Screenshot funkcionalnosti 4](images/screenshot5.png)

---

## 3. Načrtovane funkcionalnosti (še neimplementirane)

### 3.1 PORA aplikacija
- **Predmet:** PORA
- **Odgovoren član:** Jaka Počkaj
- **Okvirni rok:** 15. 1. 2026
- **Opis:** Android aplikacija bo preko strežniškega dela, implementiranega pri predmetu Spletno programiranje, pošiljala zaznane in simulirane podatke na strežnik. Podatki bodo posredovani v obliki dogodkov, ki bodo vsebovali tip podatka, čas in lokacijo ter se bodo shranjevali v podatkovno bazo za nadaljnjo obdelavo v digitalnem dvojčku

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

### 3.4 Primerjava pohitritev in povezava s PORA - blockchain
- **Predmet:** PIPR
- **Odgovoren član:** Jaka Počkaj
- **Okvirni rok:** 15.1.2026
- **Opis:** Naredi se poročilo in podrobna analiza o faktorju pohitritve glede na število niti v blockchain-u z MPI. Poveže se s PORO. Izjemni dogodki se shranjujejo v blockchain.

### 3.5 Dokonča se model za razpoznavo oblačil tako da dela na slikah, ki jih zajame uporabnik in izdelava spletne storitve
- **Predmet:** URVRV
- **Odgovoren član:** Andjelija Lazarević
- **Okvirni rok:** 18.1.2026
- **Opis:** Zaenkrat je narejen samo osnovni model. Potrebno ga je prilagoditi, da bo deloval za katerokoli sliko oblačila. Po možnosti tudi tako, da bo prepoznal barvo. Izdela se tudi spletna storitev pri tem predmetu.

### 3.6 Dodajanje in urejanje podatkov o trgovinah
- **Predmet:** RRI
- **Odgovoren član:** Jaka Počkaj
- **Okvirni rok:** 14.1.2026
- **Opis:** Dodajanje novih trgovin ter urejanje obstoječih podatkov o trgovinah (ime, lokacija, kontaktni podatki). Podatki se bodo shranjevali v podatkovno bazo in pridobivali prek spletnih storitev (API), kar bo omogočalo sprotno posodabljanje in takojšen prikaz sprememb na zemljevidu.

### 3.7 Prikazovanje izjemnih dogodkov v trgovinah
- **Predmet:** RRI
- **Odgovoren član:** Nela Copot
- **Okvirni rok:** 16.1.2026
- **Opis:** Na zemljevidu se bo pri posameznih trgovinah prikazoval poseben vizualni status v primeru izjemnih dogodkov. Če bo število ljudi v trgovini preseglo priporočeno ali dovoljeno mejo, se bo ikona trgovine obarvala rdeče. V primeru izjemnega zaprtja trgovine (npr. zaradi prenove ali izrednih razmer) bo ikona prikazana v sivi barvi. Tako bo uporabniku omogočen hiter in pregleden vpogled v trenutno stanje trgovin.

### 3.8 Optimizacija izrisa zemljevida z lokalnim shranjevanjem ploščic
- **Predmet:** RRI
- **Odgovoren član:** Anđelija Lazarević
- **Okvirni rok:** 10.1.2026
- **Opis:** Za izboljšanje zmogljivosti bo implementirano lokalno shranjevanje pogosto uporabljenih rastrskih ploščic zemljevida. Ob izrisu zemljevida se bo preverilo, ali je posamezna ploščica že shranjena lokalno. Če ploščica ni prisotna, se bo pridobila prek API-ja in nato shranila za nadaljnjo uporabo. Ta pristop bo zmanjšal število ponovnih zahtevkov na strežnik in pospešil začetni ter ponovni izris zemljevida.
