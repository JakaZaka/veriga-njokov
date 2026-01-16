"""
napovedovanje oblačil iz LASTNIH slik

Uporaba:
python predict_own_image.py path/to/your/image.jpg
python predict_own_image.py shirt.jpg --show
python predict_own_image.py my_clothes/ --batch  # za celo mapo

Kakšna mora biti slika
- eno oblačilo na čisti podlagi
- centrirano in dobro osvetljeno oblačilo
"""

import argparse
import numpy as np
import tensorflow as tf
from PIL import Image
import matplotlib.pyplot as plt
import os
import glob

LABELS = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat',
          'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']


def preprocess_image(image_path, show_steps=False):
    """
    Predobdela sliko iz poljubnega formata v 28x28 grayscale,
    podobno Fashion-MNIST formatu.

    Koraki:
    1. Naloži sliko
    2. Pretvori v grayscale
    3. Resize na 28x28
    4. Invert barve (Fashion-MNIST ima belo oblačilo na črni podlagi)
    5. Normalizira na [0, 1]
    """
    # 1. nalaganje
    img = Image.open(image_path)
    original = img.copy()

    # 2. v grayscale
    img_gray = img.convert('L')  # 'L' mode = grayscale

    # 3. Resize na 28x28 (kot Fashion-MNIST)
    img_resized = img_gray.resize((28, 28), Image.Resampling.LANCZOS)

    # 4. v numpy array
    img_array = np.array(img_resized)

    # 5. Fashion-MNIST ima belo oblačilo na črni podlagi
    # temno oblačilo na svetli podlagi -> invert
    img_inverted = 255 - img_array

    # 6. Normalizacija [0, 1]
    img_normalized = img_inverted / 255.0

    if show_steps:
        plt.figure(figsize=(15, 4))

        plt.subplot(1, 5, 1)
        plt.imshow(original)
        plt.title("1. Original")
        plt.axis('off')

        plt.subplot(1, 5, 2)
        plt.imshow(img_gray, cmap='gray')
        plt.title("2. Grayscale")
        plt.axis('off')

        plt.subplot(1, 5, 3)
        plt.imshow(img_resized, cmap='gray')
        plt.title("3. Resized 28x28")
        plt.axis('off')

        plt.subplot(1, 5, 4)
        plt.imshow(img_inverted, cmap='gray')
        plt.title("4. Inverted colors")
        plt.axis('off')

        plt.subplot(1, 5, 5)
        plt.imshow(img_normalized, cmap='gray')
        plt.title("5. Normalized")
        plt.axis('off')

        plt.tight_layout()
        plt.show()

    return img_normalized


def predict_single_image(model, image_path, show=False):
    """
    Napove razred oblačila na posamezni sliki
    """
    # Predobdela sliko
    img = preprocess_image(image_path, show_steps=show)

    # Dodaj batch dimenzijo: (28, 28) -> (1, 28, 28)
    img_batch = np.expand_dims(img, axis=0)

    # Napoved
    probs = model.predict(img_batch, verbose=0)[0]
    prediction = int(np.argmax(probs))
    confidence = probs[prediction]

    # Izpis rezultatov
    print(f"\n{'=' * 50}")
    print(f"Slika: {os.path.basename(image_path)}")
    print(f"{'=' * 50}")
    print(f"✓ NAPOVED: {LABELS[prediction]} (confidence: {confidence:.2%})")
    print(f"\nTop-5 verjetnosti:")
    for i in np.argsort(probs)[-5:][::-1]:
        bar = '█' * int(probs[i] * 50)
        print(f"  {LABELS[i]:12s}  {probs[i]:.2%}  {bar}")

    if show:
        # Prikaz originalne in predobdelane slike
        plt.figure(figsize=(12, 4))

        plt.subplot(1, 3, 1)
        original = Image.open(image_path)
        plt.imshow(original)
        plt.title(f"Original: {os.path.basename(image_path)}")
        plt.axis('off')

        plt.subplot(1, 3, 2)
        plt.imshow(img, cmap='gray')
        plt.title("Preprocessed (28x28)")
        plt.axis('off')

        plt.subplot(1, 3, 3)
        plt.barh(range(10), probs)
        plt.yticks(range(10), LABELS)
        plt.xlabel('Probability')
        plt.title(f'Prediction: {LABELS[prediction]}')
        plt.tight_layout()

        plt.show()

    return prediction, confidence, probs


def predict_batch(model, image_folder, show_grid=False):
    """
    Napove razrede za vse slike v mapi
    """
    # Najde vse slike
    extensions = ['*.jpg', '*.jpeg', '*.png', '*.bmp']
    image_paths = []
    for ext in extensions:
        image_paths.extend(glob.glob(os.path.join(image_folder, ext)))
        image_paths.extend(glob.glob(os.path.join(image_folder, ext.upper())))

    if not image_paths:
        print(f"Ni najdenih slik v mapi: {image_folder}")
        return

    print(f"\nNajdeno {len(image_paths)} slik. Napovedovanje...")

    results = []
    for img_path in image_paths:
        try:
            pred, conf, probs = predict_single_image(model, img_path, show=False)
            results.append({
                'path': img_path,
                'prediction': pred,
                'confidence': conf,
                'probs': probs
            })
        except Exception as e:
            print(f"Napaka pri {img_path}: {e}")

    # Povzetek
    print(f"\n{'=' * 60}")
    print(f"POVZETEK ({len(results)} slik)")
    print(f"{'=' * 60}")
    for r in results:
        print(f"{os.path.basename(r['path']):30s} -> {LABELS[r['prediction']]:12s} ({r['confidence']:.2%})")

    # Mreža slik (opcijsko)
    if show_grid and results:
        n = min(len(results), 12)
        cols = 4
        rows = (n + cols - 1) // cols

        plt.figure(figsize=(15, rows * 3))
        for i, r in enumerate(results[:n]):
            plt.subplot(rows, cols, i + 1)
            img = Image.open(r['path'])
            plt.imshow(img)
            plt.title(f"{LABELS[r['prediction']]}\n{r['confidence']:.1%}", fontsize=9)
            plt.axis('off')
        plt.tight_layout()
        plt.show()

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Napove vrsto oblačila iz tvoje lastne slike"
    )
    parser.add_argument(
        "input",
        help="Pot do slike ali mape s slikami"
    )
    parser.add_argument(
        "--model",
        default="saved_model_clothes_classification.keras",
        help="Pot do shranjenega modela (default: saved_model_clothes_classification.keras)"
    )
    parser.add_argument(
        "--show",
        action="store_true",
        help="Prikaži slike in predobdelavo"
    )
    parser.add_argument(
        "--show-steps",
        action="store_true",
        help="Prikaži korake predobdelave"
    )
    parser.add_argument(
        "--batch",
        action="store_true",
        help="Obdelaj vse slike v mapi"
    )

    args = parser.parse_args()

    # Naloži model
    print(f"Nalaganje modela: {args.model}")
    try:
        model = tf.keras.models.load_model(args.model)
        print("✓ Model naložen uspešno!")
    except Exception as e:
        print(f"✗ Napaka pri nalaganju modela: {e}")
        return

    # ali je input mapa ali slika
    if os.path.isdir(args.input):
        # mapa - batch obdelava
        predict_batch(model, args.input, show_grid=args.show)
    elif os.path.isfile(args.input):
        # posamezna slika
        if args.show_steps:
            # Prikaz korakov predobdelave
            img = preprocess_image(args.input, show_steps=True)

        predict_single_image(model, args.input, show=args.show)
    else:
        print(f"✗ Napaka: '{args.input}' ni veljavna datoteka ali mapa")


if __name__ == "__main__":
    main()