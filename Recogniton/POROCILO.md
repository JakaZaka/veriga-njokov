# Detekcija in štetje oseb z YOLOv8

## Opis projekta

Ta projekt prikazuje uporabo **učenega modela YOLOv8** za **detekcijo in štetje oseb na sliki**. Program sprejme pot do slike (JPG ali PNG), izvede detekcijo objektov, prepozna osebe in vrne njihovo število. Po želji shrani tudi **annotirano sliko**, kjer so zaznani objekti označeni z okvirji (bounding boxes).

---

## Uporabljene tehnologije

* **Ultralytics YOLOv8** – predtrenirani model za detekcijo objektov
* **OpenCV (cv2)** – delo s slikami (branje, shranjevanje)
* **COCO dataset** – standardni nabor razredov za detekcijo objektov

---

## YOLOv8 – kratek opis

Model je že **treniran na COCO podatkovnem naboru**, ki vsebuje 80 razredov objektov (oseba, avto, pes, …).

---

## Razlaga implementacije

---

### 2. Razred osebe (COCO)

```python
PERSON_CLASS_ID = 0
```

V COCO podatkovnem naboru ima razred **"person"** ID vrednost `0`. To konstanto uporabimo za filtriranje zaznanih objektov.

---

### 3. Funkcija `count_people_in_img`

```python
def count_people_in_img(image_path: str, conf: float = 0.25, save_annotated: bool = True) -> int:
   ... 
```

Funkcija:

* prejme pot do slike,
* izvede detekcijo objektov,
* prešteje zaznane osebe,
* po želji shrani annotirano sliko.

#### Nalaganje modela

```python
model = YOLO('yolov8n.pt')
```

Naloži se predtrenirani YOLOv8 nano model.

#### Branje slike

```python
img = cv2.imread(image_path)
```

Če slike ni mogoče prebrati, program sproži izjemo `FileNotFoundError`, kar prepreči nadaljnje izvajanje.



#### Detekcija objektov

```python
results = model.predict(source=img, conf=conf, verbose=False)[0]
```

* `conf` predstavlja prag zaupanja (confidence threshold).
* Rezultat vsebuje zaznane objekte, njihove razrede in koordinate okvirjev.

#### Štetje oseb

```python
cls = results.boxes.cls.cpu().numpy().astype(int)
people_count = int((cls == PERSON_CLASS_ID).sum())
```

* Iz rezultatov pridobimo razrede zaznanih objektov.
* Preštejemo, kolikokrat se pojavi razred `person`.

#### Shranjevanje annotirane slike

```python
annotated = results.plot()
cv2.imwrite(out_path, annotated)
```

Če je omogočeno, se shrani slika z označenimi osebami in okvirji.

```bash
python main.py image.jpg --conf 0.35
```

Argumenti:

* `image_path` – pot do slike
* `--conf` – prag zaupanja
* `--no-save` – onemogoči shranjevanje annotirane slike
---

## Primer izpisa

```text
SAVED ANNOTATED IMAGE TO: image_annotated.jpg
NUMBER OF PEOPLE DETECTED: 5
```

# Klasifikacija oblačil z uporabo nevronskih mrež in analize slik

## 1. Uvod

Namen projekta je izdelati sistem za **avtomatsko prepoznavanje vrste oblačila** iz slik. Delo je razdeljeno na **dva glavna dela**:

1. **Učenje in evalvacija modela** za klasifikacijo oblačil na podlagi podatkovne množice **Fashion-MNIST**.
2. **Pipeline**, ki omogoča klasifikacijo **lastnih slik**, odstranjevanje ozadja z uporabo umetne inteligence ter analizo **dominantnih barv oblačila**.

---

## 2. Podatkovna množica Fashion-MNIST

Fashion-MNIST je standardna podatkovna množica za učenje algoritmov strojnega učenja, ki vsebuje:

* 60.000 učnih slik
* 10.000 testnih slik
* Slike velikosti **28 × 28** pikslov
* Sivinske slike (0–255)

Vsaka slika predstavlja kos oblačila (majica, hlače, čevlji, ipd.). V projektu smo **odstranili razred “Bag”**, ker ga ne potreujemo in nam je uničeval rezultate, zato model klasificira **9 razredov**:

* T-shirt / top
* Trouser
* Pullover
* Dress
* Coat
* Sandal
* Shirt
* Sneaker
* Ankle boot

Odstranitev razreda je zahtevala tudi **ponovno preslikavo oznak**, da so ostale zaporedne.

---

## 3. Prvi del: Učenje klasifikacijskega modela

### 3.1 Predobdelava podatkov

Pred učenjem modela so bile slike:

* normalizirane iz območja `[0, 255]` v `[0, 1]`
* uporabljene v originalni obliki 28 × 28

Vizualizacija surovih in normaliziranih slik omogoča boljše razumevanje vhodnih podatkov.

---

### 3.2 Arhitektura modela

Uporabljen je preprost **nevronski model (MLP)**, implementiran s knjižnico TensorFlow/Keras:

* **Flatten plast** – pretvori 2D sliko v 1D vektor
* **Dense plast (128 nevronov, ReLU)** – učenje nelinearnih vzorcev
* **Dense izhodna plast (9 nevronov)** – izhodni logits za posamezne razrede

Softmax funkcija ni vključena neposredno v model, ampak se uporablja znotraj funkcije izgube.

---

### 3.3 Učenje in validacija

Model se uči z:

* optimizerjem **Adam**
* funkcijo izgube **SparseCategoricalCrossentropy (from_logits=True)**
* metriko **accuracy**

Podatki so razdeljeni na:

* 90 % učnih podatkov
* 10 % validacijskih podatkov

Med učenjem se beležijo krivulje **loss** in **accuracy**, ki omogočajo analizo konvergence modela.

---

### 3.4 Evalvacija in shranjevanje modela

Po učenju se model ovrednoti na testnem naboru podatkov. Končni model se shrani v `.keras` formatu in se uporablja v drugem delu projekta.

Model za napovedovanje vključuje tudi **Softmax plast**, saj so za prikaz rezultatov potrebne verjetnosti.

---

## 4. Drugi del: pipeline za lastne slike

Omogoča razpoznavo LASTNIH SLIK KOSOV OBLAČIL, ki jih uporabnik fotografira.

### 4.1 Deli

* uporaba **lastnih fotografij oblačil**
* **odstranjevanje ozadja** z AI
* predobdelava slik
* klasifikacija oblačila
* analiza **dominantnih barv**
* vizualizacija rezultatov

---

## 5. Odstranjevanje ozadja

### 5.1 Rembg – AI odstranjevanje ozadja

Metoda za odstranjevanje ozadja je knjižnica **rembg**, ki uporablja globoko učenje za segmentacijo ospredja.

---

### 5.2 Fallback metode

Če `rembg` ni nameščen, sistem samodejno uporabi:

1. **GrabCut (OpenCV)** – iterativna segmentacija
2. **Thresholding** – preprosta metoda na podlagi svetlosti

S tem je zagotovljeno, da pipeline deluje tudi v omejenih okoljih.

---

## 6. Predobdelava za klasifikacijo

Slike iz realnega sveta se razlikujejo od Fashion-MNIST podatkov, zato je potrebna dodatna predobdelava:

* pretvorba v sivinsko sliko
* izenačevanje razmerja stranic (padding)
* resize na 28 × 28
* inverz barv (belo oblačilo na črni podlagi)
* normalizacija

---

## 7. Analiza barv oblačila

### 7.1 Napredna barvna analiza

Barve se analizirajo samo na pikslih oblačila (alpha maska):

* odstranitev senc in bleščanja
* pretvorba v **Lab barvni prostor** 
* uporaba **KMeans clusteringa**

Rezultat so:

* ime barve
* RGB vrednost
* delež barve v odstotkih


### 7.2 Hevristike za črno, belo in sivo

Posebni pogoji zaznajo:

* zelo temna oblačila
* zelo svetla oblačila
* nizko nasičenost

To izboljša pravilnost opisa barv pri enobarvnih kosih oblačil.

---


## Batch obdelava

podpira tudi:

* obdelavo **celotnih map s slikami**
* zaporedno analizo in izpis rezultatov

To omogoča uporabo sistema v realnih scenarijih (npr. katalogi oblačil).

---



## 11. Uporabljene tehnologije

* TensorFlow / Keras
* NumPy
* Matplotlib
* OpenCV
* scikit-learn
* rembg
* Pillow



