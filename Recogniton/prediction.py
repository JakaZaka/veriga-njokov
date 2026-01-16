import numpy as np
import tensorflow as tf

LABELS = ['T-shirt/top', 'Trouser', 'Pullover', 'Dress', 'Coat', 'Sandal', 'Shirt', 'Sneaker', 'Bag', 'Ankle boot']

model = tf.keras.models.load_model("saved_model_clothes_classification.keras")

(x_train, y_train), (x_test, y_test) = tf.keras.datasets.fashion_mnist.load_data()
example = 4000
img = x_test[example] / 255.0
img = np.expand_dims(img, axis=0)

probs = model.predict(img, verbose=0)[0]
prediction = int(np.argmax(probs))

print("Predicted:", prediction, LABELS[prediction])
print("Actual:", int(y_test[example]), LABELS[int(y_test[example])])
print("\nTop-3 predictions:")
for i in np.argsort(probs)[-3:][::-1]:
    print(f"{LABELS[i]:12s}  prob={probs[i]:.4f}")
