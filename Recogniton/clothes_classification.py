# i'm using  Fashion-MNIST dataset for clothes classification
# The source for this: https://www.geeksforgeeks.org/machine-learning/classifying-clothing-images-in-python/

"""
Osnovni zagon:
python clothes_classification.py

Prikaz vzorcev in napovedi:
python clothes_classification.py --show-samples --show-preds

Spremenitev epoh ali batch size:
python clothes_classification.py --epochs 20 --batch-size 64 --show-preds

Shranjevanje modela:
python clothes_classification.py --save-model
"""

import argparse
import numpy as np
import tensorflow as tf
import matplotlib.pyplot as plt

LABEL_CLASS_NAMES = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag',
                     'Ankle boot']


def show_sample_grid(x, y, n=20, title="Sample images"):  # prikaz n vzorcev
    plt.figure(figsize=(15, 5))
    for i in range(n):
        plt.subplot(2, n // 2, i + 1)
        plt.imshow(x[i], cmap=plt.cm.binary)
        plt.xticks([])
        plt.yticks([])
        plt.title(LABEL_CLASS_NAMES[int(y[i])], fontsize=8)
    plt.suptitle(title)
    plt.tight_layout()
    plt.show()


def build_model():
    model = tf.keras.Sequential([
        tf.keras.layers.Flatten(input_shape=(28, 28)),
        # fashion-MNIST slike so 28x28
        # flatten-am v 1D vektor

        tf.keras.layers.Dense(128, activation="relu"),
        # polno povezana plast, 128 nevronov
        # relu... nelinearna aktivacijska funkcija, da model lahko nauci kompleksnih vzorcev

        tf.keras.layers.Dense(10)
        # izhodna plast, 10 nevronov (za 10 razredov oblačil)
        # brez softmax, izhodi so logits (nenormalizirane ocene)
    ])
    return model


def show_predictions_grid(x_test, y_test, predictions, n=30):
    """
    prikaz n testnih slik: pravilno/napacno + dejansko
    """
    plt.figure(figsize=(15, 10))
    for i in range(n):
        image = x_test[i]  # testna slika
        actual = int(y_test[i])  # dejanski razred
        predicted = int(np.argmax(predictions[i]))  # argmax... indeks max vrednosti... napovedani razred
        # predictions[i] vektor verjetnosti za vse razrede (size: 10)

        plt.subplot(5, n // 5, i + 1)
        plt.tight_layout()
        plt.xticks([])
        plt.yticks([])
        plt.imshow(image, cmap="gray")

        if predicted == actual:
            color, label = "green", "CORRECT"
        else:
            color, label = "red", "WRONG"

        plt.title(label, color=color, fontsize=10)
        plt.xlabel(f"Pred: {LABEL_CLASS_NAMES[predicted]}\nReal: {LABEL_CLASS_NAMES[actual]}", fontsize=8)
        plt.ylabel(str(i), fontsize=8)

    plt.show()


def plot_training_history(history):
    hist = history.history
    epochs = range(1, len(hist["loss"]) + 1)

    plt.figure(figsize=(12, 5))

    # LOSS
    plt.subplot(1, 2, 1)
    plt.plot(epochs, hist["loss"], label="Training loss")
    plt.plot(epochs, hist["val_loss"], label="Validation loss")
    plt.xlabel("Epochs")
    plt.ylabel("Loss")
    plt.title("Loss during training")
    plt.legend()

    # ACCURACY
    plt.subplot(1, 2, 2)
    plt.plot(epochs, hist["accuracy"], label="Training accuracy")
    plt.plot(epochs, hist["val_accuracy"], label="Validation accuracy")
    plt.xlabel("Epochs")
    plt.ylabel("Accuracy")
    plt.title("Accuracy during training")
    plt.legend()

    plt.tight_layout()
    plt.show()


def main():
    parser = argparse.ArgumentParser(description="Clothes classifyer from Fashion-MNIST dataset")
    parser.add_argument("--epochs", type=int, default=10, help="num of training epochs")
    parser.add_argument("--batch-size", type=int, default=32, help="batch size for training")
    parser.add_argument("--show-samples", action="store_true", help="show sample images from dataset")
    parser.add_argument("--show-preds", action="store_true", help="show predictions on test set")
    parser.add_argument("--save-model", action="store_true", help="save trained model")
    args = parser.parse_args()

    fashion_mnist = tf.keras.datasets.fashion_mnist
    (x_train, y_train), (x_test, y_test) = fashion_mnist.load_data()
    """
        x_train: (60000, 28, 28)

        y_train: (60000,)

        x_test: (10000, 28, 28)

        y_test: (10000,)

        --------------------------------

        x_* ... grayscale slike s pixli 0–255 (uint8).
    """

    print("Shape of training cloth images:", x_train.shape)
    print("Shape of training label:", y_train.shape)
    print("Shape of test cloth images:", x_test.shape)
    print("Shape of test labels:", y_test.shape)
    print("Pixel range (train):", x_train.min(), "to", x_train.max())

    plt.imshow(x_train[0])  # prva slika iz train seta
    plt.title("Raw image (before normalization)")
    plt.colorbar()
    plt.show()

    # predobdelava: normalizacija slik
    x_train = x_train / 255.0
    x_test = x_test / 255.0

    # vizualizacija podatkov
    if args.show_samples:
        show_sample_grid(x_train, y_train, n=30, title="Normalized sample images")

    # build modela
    model = build_model()

    # compilation modela
    model.compile(optimizer="adam", loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
                  metrics=["accuracy"])
    # loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True) ... labels so cela st.
    #  ... from_logits=True izhodne vredosti so logits
    #  ... uporabljen bo softmax znotraj loss funkcije
    # metrics=["accuracy"] ... sproti bo porocal tocnost

    # training modela
    history = model.fit(x_train, y_train, epochs=args.epochs, batch_size=args.batch_size, validation_split=0.1,
                        verbose=1)
    """
    validation_split=0.1 vzame 10% trening podatkov za validacijo:
    uči na ~54,000 slikah
    validira na ~6,000 slikah
    """
    # history ima krivulje loss/accuracy

    plot_training_history(history)

    # evalvacija modela
    test_loss, test_acc = model.evaluate(x_test, y_test, verbose=2)
    # porocilo o tocnosti na test setu... na novo racun

    # napovedi
    prediction_model = tf.keras.Sequential([model, tf.keras.layers.Softmax()])  # za prikaz verjetnosti rabim softmax
    predictions = prediction_model.predict(x_test, verbose=0)  # oblika: (10000, 10)

    # primer: prva slika
    first_pred = int(np.argmax(predictions[0]))
    print("\nFirst test image prediction:")
    print("Predicted:", first_pred, "-", LABEL_CLASS_NAMES[first_pred])
    print("Actual   :", int(y_test[0]), "-", LABEL_CLASS_NAMES[int(y_test[0])])

    # prikaz napovedi na testnih slikah
    if args.show_preds:
        show_predictions_grid(x_test, y_test, predictions, n=30)

    # shranjevanje modela
    if args.save_model:
        out_path = "saved_model_clothes_classification.keras"
        prediction_model.save(out_path)  # prediction_model, ker ima softmax
        print(f"\nSAVED MODEL TO: {out_path}")


if __name__ == "__main__":
    main()
