"""
pip install rembg[gpu]  # ce mas gpu povezavo
ali
pip install rembg[cpu]

Uporaba:
python full_classification.py slika.jpg --show-all
python full_classification.py mapa/ --batch
"""

import argparse
import os

import cv2
import matplotlib.pyplot as plt
import numpy as np
import tensorflow as tf
from PIL import Image

# trying to import rembg
try:
    from rembg import remove

    REMBG_AVAILABLE = True
except ImportError:
    REMBG_AVAILABLE = False
    print("⚠ rembg ni nameščen. Namesti z: pip install rembg")

LABELS = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat',
          'Sandal', 'Shirt', 'Sneaker', 'Ankle boot']

COLOR_NAMES = {
    'rdeča': (255, 0, 0),
    'temno rdeča': (139, 0, 0),
    'bordo': (128, 0, 32),
    'oranžno rdeča': (255, 69, 0),

    'modra': (0, 100, 255),
    'svetlo modra': (135, 206, 250),
    'temno modra': (0, 0, 139),
    'navy': (0, 0, 128),
    'turkizna': (64, 224, 208),
    'cyan': (0, 255, 255),

    'zelena': (0, 200, 0),
    'svetlo zelena': (144, 238, 144),
    'temno zelena': (0, 100, 0),
    'olivna': (128, 128, 0),
    'khaki': (189, 183, 107),

    'rumena': (255, 255, 0),
    'svetlo rumena': (255, 255, 224),
    'oranžna': (255, 165, 0),
    'koralna': (255, 127, 80),

    'vijolična': (128, 0, 128),
    'temno vijolična': (75, 0, 130),
    'rožnata': (255, 192, 203),
    'svetlo rožnata': (255, 182, 193),
    'fuksija': (255, 0, 255),

    'rjava': (139, 69, 19),
    'svetlo rjava': (181, 101, 29),
    'temno rjava': (101, 67, 33),

    'svetlo siva': (211, 211, 211),
    'siva': (128, 128, 128),
    'temno siva': (64, 64, 64),

    'črna': (30, 30, 30),  # malo dvignjeno da ujame temne barve
    'bela': (240, 240, 240),  # malo znižano da ujame bele

    'bež': (245, 245, 220),
    'krem': (255, 253, 208),
    'peščena': (194, 178, 128),
}


def remove_background_rembg(image_path):
    """
    Odstrani ozadje z rembg knjižnico
    """
    if not REMBG_AVAILABLE:
        print("⚠ rembg ni na voljo, uporabljam preprost algoritem")
        return None

    input_image = Image.open(image_path)
    output_image = remove(input_image)

    return output_image


def remove_background_fallback(image_path):
    """
    fallback če rembg ni na voljo
    """
    try:
        import cv2

        img = cv2.imread(image_path)
        if img is None:
            return None

        # GrabCut algorithm
        mask = np.zeros(img.shape[:2], np.uint8)
        height, width = img.shape[:2]
        rect = (int(width * 0.05), int(height * 0.05),
                int(width * 0.9), int(height * 0.9))

        bgd_model = np.zeros((1, 65), np.float64)
        fgd_model = np.zeros((1, 65), np.float64)

        cv2.grabCut(img, mask, rect, bgd_model, fgd_model, 5, cv2.GC_INIT_WITH_RECT)

        mask2 = np.where((mask == 2) | (mask == 0), 0, 1).astype('uint8')
        img_no_bg = img * mask2[:, :, np.newaxis]

        # make transparent background
        img_rgba = cv2.cvtColor(img_no_bg, cv2.COLOR_BGR2RGBA)
        img_rgba[:, :, 3] = mask2 * 255

        result = Image.fromarray(img_rgba)
        return result

    except ImportError:
        print("OpenCV ni nameščen, uporabljam thresholding")
        return remove_background_threshold(image_path)


def remove_background_threshold(image_path):
    """
    threshold
    """
    img = Image.open(image_path).convert('RGB')
    img_array = np.array(img)

    gray = np.mean(img_array, axis=2)
    threshold = np.percentile(gray, 70)
    mask = gray < threshold

    # RGBA z transparentnostjo
    result_array = np.zeros((img_array.shape[0], img_array.shape[1], 4), dtype=np.uint8)
    result_array[:, :, :3] = img_array
    result_array[:, :, 3] = mask * 255

    result = Image.fromarray(result_array)
    return result


def get_dominant_colors_advanced(image, num_colors=3):
    """
    Bolj natančna analiza barv za fotke:
    - vzame samo piksle oblačila (alpha mask)
    - odstrani sence (pretemno) in highlight (presvetlo)
    - dela v Lab prostoru
    - uporabi KMeans na "čistih" pikslih
    """
    img = image

    # v RGB + alpha mask
    if img.mode != "RGBA":
        img = img.convert("RGBA")

    arr = np.array(img)  # RGBA
    rgb = arr[:, :, :3]
    alpha = arr[:, :, 3]

    # samo piksle oblačila
    mask_fg = alpha > 40
    pixels = rgb[mask_fg]
    if len(pixels) == 0:
        return [("neznana", (128, 128, 128), 100.0)]

    # RGB -> HSV za filtriranje (shadow/highlight/saturation)
    hsv = cv2.cvtColor(pixels.reshape(-1, 1, 3).astype(np.uint8), cv2.COLOR_RGB2HSV).reshape(-1, 3)
    h = hsv[:, 0].astype(np.int32)
    s = hsv[:, 1].astype(np.int32)
    v = hsv[:, 2].astype(np.int32)

    # filtri:
    # - odstrani pretemno (sence) in presvetlo (bleščanje)
    # - odstrani "skoraj sive" (nizka saturacija), razen če je res črno/belo/sivo
    not_shadow = v > 35
    not_highlight = v < 245
    not_too_gray = s > 25

    clean_mask = not_shadow & not_highlight & not_too_gray
    clean_pixels = pixels[clean_mask]

    # če smo preveč agresivni, popusti pogoje
    if len(clean_pixels) < 300:
        clean_mask = (v > 25) & (v < 250)  # samo shadow/highlight
        clean_pixels = pixels[clean_mask]

    if len(clean_pixels) == 0:
        clean_pixels = pixels

    # downsample za hitrost
    if len(clean_pixels) > 15000:
        idx = np.random.choice(len(clean_pixels), 15000, replace=False)
        sample = clean_pixels[idx]
    else:
        sample = clean_pixels

    # KMeans v Lab prostoru (bolj stabilno)
    lab = cv2.cvtColor(sample.reshape(-1, 1, 3).astype(np.uint8), cv2.COLOR_RGB2LAB).reshape(-1, 3).astype(np.float32)

    try:
        from sklearn.cluster import KMeans
        k = min(num_colors, 5)
        km = KMeans(n_clusters=k, random_state=42, n_init=10)
        labels = km.fit_predict(lab)
        centers_lab = km.cluster_centers_

        counts = np.bincount(labels)
        order = np.argsort(counts)[::-1]

        results = []
        total = counts.sum()

        for idx in order[:num_colors]:
            # center Lab -> RGB za izpis
            center_lab = centers_lab[idx].reshape(1, 1, 3).astype(np.uint8)
            center_rgb = cv2.cvtColor(center_lab, cv2.COLOR_LAB2RGB)[0, 0]
            center_rgb = tuple(int(x) for x in center_rgb)

            name = find_closest_color_name(center_rgb)
            pct = (counts[idx] / total) * 100
            results.append((name, center_rgb, pct))

        # poseben primer: če je oblačilo res črno/belo/sivo
        # preveri delež nizke saturacije in zelo temnih/svetlih
        gray_ratio = np.mean(s < 20)
        dark_ratio = np.mean(v < 50)
        bright_ratio = np.mean(v > 220)

        if gray_ratio > 0.65:
            # hevristika za črno/belo/sivo
            mean_v = float(np.mean(v))
            if mean_v < 80:
                results.insert(0, ("črna", (30, 30, 30), min(100.0, 60.0 + dark_ratio * 40)))
            elif mean_v > 180:
                results.insert(0, ("bela", (240, 240, 240), min(100.0, 60.0 + bright_ratio * 40)))
            else:
                results.insert(0, ("siva", (128, 128, 128), 60.0))

            # obdrži samo top N
            results = results[:num_colors]

        return results

    except ImportError:
        # fallback brez sklearn: median v RGB (robustno)
        med = np.median(clean_pixels.astype(np.float32), axis=0)
        med_rgb = tuple(int(x) for x in med)
        name = find_closest_color_name(med_rgb)
        return [(name, med_rgb, 100.0)]


COLOR_NAMES_LAB = {}
for name, rgb in COLOR_NAMES.items():
    rgb_arr = np.uint8([[list(rgb)]])  # shape (1,1,3) RGB
    lab = cv2.cvtColor(rgb_arr, cv2.COLOR_RGB2LAB)[0, 0]
    COLOR_NAMES_LAB[name] = lab


def find_closest_color_name(rgb):
    """
    Najde najbližje ime barve z Lab razdaljo (bolj pravilno kot RGB).
    """
    rgb_arr = np.uint8([[list(rgb)]])
    lab = cv2.cvtColor(rgb_arr, cv2.COLOR_RGB2LAB)[0, 0].astype(np.int32)

    min_dist = 10 ** 9
    closest = "neznana"
    for name, lab_ref in COLOR_NAMES_LAB.items():
        d = np.linalg.norm(lab - lab_ref.astype(np.int32))
        if d < min_dist:
            min_dist = d
            closest = name
    return closest


def preprocess_for_classification(image, strategy='pad'):
    """
    Predobdelava za klasifikacijski model
    """
    from PIL import ImageOps

    # Če je RGBA
    if image.mode == 'RGBA':
        bg = Image.new('RGB', image.size, (255, 255, 255))
        bg.paste(image, mask=image.split()[3])
        image = bg

    img_gray = image.convert('L')

    # PAD
    width, height = img_gray.size
    if width > height:
        padding = ((width - height) // 2, 0)
        img_padded = ImageOps.expand(img_gray, border=padding, fill=255)
    else:
        padding = (0, (height - width) // 2)
        img_padded = ImageOps.expand(img_gray, border=padding, fill=255)

    img_resized = img_padded.resize((28, 28), Image.Resampling.LANCZOS)
    img_array = np.array(img_resized)
    img_inverted = 255 - img_array
    img_normalized = img_inverted / 255.0

    return img_normalized


def analyze_clothing_advanced(image_path, model, show_steps=False):
    """
    Celotna analiza z rembg
    """
    print(f"Nalagam sliko...")
    original = Image.open(image_path)

    print(f"Odstranjujem ozadje (AI model)...")
    if REMBG_AVAILABLE:
        img_no_bg = remove_background_rembg(image_path)
    else:
        img_no_bg = remove_background_fallback(image_path)

    if img_no_bg is None:
        print(f"⚠ Napaka pri odstranjevanju, uporabljam originalno")
        img_no_bg = original

    if show_steps:
        plt.figure(figsize=(6, 6))
        plt.imshow(img_no_bg)
        plt.title("Brez ozadja (rembg)")
        plt.axis("off")
        plt.show()

    print(f"Analiziram barve ...")
    colors = get_dominant_colors_advanced(img_no_bg, num_colors=3)

    print(f"Klasificiram oblačilo...")
    img_processed = preprocess_for_classification(img_no_bg)
    img_batch = np.expand_dims(img_processed, axis=0)
    probs = model.predict(img_batch, verbose=0)[0]
    prediction = int(np.argmax(probs))
    confidence = probs[prediction]

    results = {
        'original': original,
        'no_background': img_no_bg,
        'processed': img_processed,
        'classification': LABELS[prediction],
        'confidence': confidence,
        'probabilities': probs,
        'colors': colors  # List of (name, rgb, percentage)
    }

    if show_steps:
        visualize_results_advanced(results, image_path)

    return results


def visualize_results_advanced(results, image_path):
    """
    Napredna vizualizacija
    """
    fig = plt.figure(figsize=(18, 10))

    # 1. Original
    plt.subplot(2, 5, 1)
    plt.imshow(results['original'])
    plt.title("1. ORIGINAL", fontsize=11, fontweight='bold')
    plt.axis('off')

    # 2. Brez ozadja
    plt.subplot(2, 5, 2)
    plt.imshow(results['no_background'])
    plt.title("2. NO BACKGROUND\n(AI removal)", fontsize=11, fontweight='bold')
    plt.axis('off')

    # 3. Top 3 barve
    plt.subplot(2, 5, 3)
    color_bars = np.zeros((100, 300, 3), dtype=np.uint8)
    x_start = 0
    for name, rgb, pct in results['colors']:
        width = int(300 * pct / 100)
        color_bars[:, x_start:x_start + width] = rgb
        x_start += width
    plt.imshow(color_bars)
    plt.title("3. BARVE\n(dominantne)", fontsize=11, fontweight='bold')
    plt.axis('off')

    # 4. Processed
    plt.subplot(2, 5, 4)
    plt.imshow(results['processed'], cmap='gray')
    plt.title("4. PREPROCESSED\n(28x28)", fontsize=11, fontweight='bold')
    plt.axis('off')

    # 5. Klasifikacija
    plt.subplot(2, 5, 5)
    plt.text(0.5, 0.6, results['classification'],
             ha='center', va='center', fontsize=28, fontweight='bold',
             color='green' if results['confidence'] > 0.6 else 'orange')
    plt.text(0.5, 0.3, f"{results['confidence']:.1%}",
             ha='center', va='center', fontsize=20)
    plt.xlim(0, 1)
    plt.ylim(0, 1)
    plt.axis('off')
    plt.title("5. REZULTAT", fontsize=11, fontweight='bold')

    # 6. Verjetnosti
    plt.subplot(2, 5, 6)
    colors_bars = ['green' if i == np.argmax(results['probabilities']) else 'lightblue'
                   for i in range(9)]
    plt.barh(range(9), results['probabilities'], color=colors_bars)
    plt.yticks(range(9), LABELS, fontsize=8)
    plt.xlabel('Probability', fontsize=9)
    plt.title("6. PROBABILITIES", fontsize=11, fontweight='bold')

    # 7. Barvna analiza
    plt.subplot(2, 5, 7)
    color_names = [c[0] for c in results['colors']]
    color_pcts = [c[2] for c in results['colors']]
    bar_colors = [tuple(c / 255 for c in results['colors'][i][1]) for i in range(len(results['colors']))]

    plt.barh(range(len(color_names)), color_pcts, color=bar_colors)
    plt.yticks(range(len(color_names)), color_names, fontsize=9)
    plt.xlabel('Delež (%)', fontsize=9)
    plt.title("7. COLOR ANALYSIS", fontsize=11, fontweight='bold')

    # 8. Barvni vzorci
    plt.subplot(2, 5, 8)
    for i, (name, rgb, pct) in enumerate(results['colors']):
        y = 1 - (i * 0.3)
        color_square = np.ones((30, 80, 3), dtype=np.uint8) * np.array(rgb, dtype=np.uint8)
        plt.imshow(color_square, extent=[0, 0.5, y - 0.2, y + 0.1])
        plt.text(0.55, y - 0.05, f"{name}\n{pct:.1f}%",
                 fontsize=9, va='center')
    plt.xlim(0, 1)
    plt.ylim(0, 1)
    plt.axis('off')
    plt.title("8. COLOR SWATCHES", fontsize=11, fontweight='bold')

    # 9-10. Povzetek
    plt.subplot(2, 5, 9)
    summary = f"""
╔═══════════════════════════╗
║     ANALIZA OBLAČILA      ║
╚═══════════════════════════╝

Slika:
   {os.path.basename(image_path)[:20]}

Tip oblačila:
   {results['classification']}
   
Zaupanje:
   {results['confidence']:.1%}

Glavna barva:
   {results['colors'][0][0]}
"""

    plt.text(0.05, 0.95, summary,
             ha='left', va='top', fontsize=9, family='monospace',
             bbox=dict(boxstyle='round', facecolor='lightblue', alpha=0.3))
    plt.xlim(0, 1)
    plt.ylim(0, 1)
    plt.axis('off')
    plt.title("9. SUMMARY", fontsize=11, fontweight='bold')

    # top razredi
    plt.subplot(2, 5, 10)
    top5_idx = np.argsort(results['probabilities'])[-5:][::-1]
    top5_labels = [LABELS[i] for i in top5_idx]
    top5_probs = [results['probabilities'][i] for i in top5_idx]

    colors_top = ['green' if i == 0 else 'lightcoral' for i in range(5)]
    plt.bar(range(5), top5_probs, color=colors_top)
    plt.xticks(range(5), top5_labels, rotation=45, ha='right', fontsize=7)
    plt.ylabel('Probability', fontsize=9)
    plt.title("10. TOP 5 CLASSES", fontsize=11, fontweight='bold')
    plt.ylim(0, 1)

    plt.tight_layout()
    plt.show()


def print_results_advanced(results, image_path):
    """
    Lepši izpis v konzolo
    """
    print(f"\n{'=' * 70}")
    print(f"║  ANALIZA OBLAČILA: {os.path.basename(image_path):<45} ║")
    print(f"{'=' * 70}")

    print(f"\nTIP OBLAČILA:")
    print(f"   ╠══ {results['classification']}")
    print(f"   ╠══ Confidence: {results['confidence']:.1%}")

    confidence_bar = '█' * int(results['confidence'] * 50)
    confidence_empty = '░' * (50 - int(results['confidence'] * 50))
    print(f"   ╚══ [{confidence_bar}{confidence_empty}]")

    print(f"\nANALIZA BARV:")
    for i, (name, rgb, pct) in enumerate(results['colors'], 1):
        symbol = '╠' if i < len(results['colors']) else '╚'
        bar = '█' * int(pct / 2)
        print(f"   {symbol}══ #{i}: {name:15s} {pct:5.1f}%  {bar}")
        print(f"   {'║' if i < len(results['colors']) else ' '}    RGB: {rgb}")

    print(f"\nTOP-5 KLASIFIKACIJE:")
    for i, idx in enumerate(np.argsort(results['probabilities'])[-5:][::-1], 1):
        symbol = '╠' if i < 5 else '╚'
        bar = '█' * int(results['probabilities'][idx] * 50)
        marker = '→' if i == 1 else ' '
        print(f"   {symbol}══ {marker} {LABELS[idx]:12s} {results['probabilities'][idx]:5.1%}  {bar}")

    print(f"\n{'=' * 70}\n")


def main():
    parser = argparse.ArgumentParser(
        description="Napreden pipeline z AI odstranjevanjem ozadja (rembg)"
    )
    parser.add_argument("input", help="Pot do slike ali mape")
    parser.add_argument(
        "--model",
        default="saved_model_clothes_classification.keras",
        help="Pot do modela"
    )
    parser.add_argument(
        "--show-all",
        action="store_true",
        help="Prikaži celotno analizo"
    )
    parser.add_argument(
        "--batch",
        action="store_true",
        help="Batch obdelava"
    )

    args = parser.parse_args()

    # preveri če je rembg nameščen
    if not REMBG_AVAILABLE:
        print("\n" + "=" * 70)
        print("⚠ OPOZORILO: rembg knjižnica ni nameščena")
        print("=" * 70)
        print("Za najboljše odstranjevanje ozadja namesti:")
        print("  pip install rembg")
        print("\nZa GPU podporo:")
        print("  pip install rembg[gpu]")
        print("\nNadaljujem z enostavnejšimi metodami...\n")

    # Nalaganje modela
    print(f"Nalaganje modela: {args.model}")
    try:
        model = tf.keras.models.load_model(args.model)
        print("Model naložen!\n")
    except Exception as e:
        print(f"Napaka: {e}")
        return

    # Single ali batch
    if args.batch or os.path.isdir(args.input):
        # Batch processing
        import glob
        extensions = ['*.jpg', '*.jpeg', '*.png']
        image_paths = []
        for ext in extensions:
            image_paths.extend(glob.glob(os.path.join(args.input, ext)))

        print(f"Najdeno {len(image_paths)} slik\n")

        for i, img_path in enumerate(image_paths, 1):
            print(f"[{i}/{len(image_paths)}] {os.path.basename(img_path)}")
            try:
                results = analyze_clothing_advanced(img_path, model, show_steps=False)
                print_results_advanced(results, img_path)
            except Exception as e:
                print(f"Napaka: {e}\n")
    else:
        # Single image
        results = analyze_clothing_advanced(args.input, model, show_steps=args.show_all)
        print_results_advanced(results, args.input)


if __name__ == "__main__":
    main()
