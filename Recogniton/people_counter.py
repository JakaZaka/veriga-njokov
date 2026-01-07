from ultralytics import YOLO
import cv2
import argparse
from pathlib import Path

PERSON_CLASS_ID = 0  # COCO dataset class ID for 'person'

def count_people_in_img(image_path: str, conf: float = 0.25, save_annotated: bool = True) -> int:
    # VRNE STEVILO LJUDI NA SLIKI

    model = YOLO('yolov8n.pt') # model za detekcijo predmetov

    img = cv2.imread(image_path)
    if img is None:
        raise FileNotFoundError(f"Image not found: {image_path}")
    
    results = model.predict(source=img, conf=conf, verbose=False)[0]

    people_count = 0
    if results.boxes is not None and len(results.boxes) > 0:
        cls = results.boxes.cls.cpu().numpy().astype(int)
        people_count = int((cls == PERSON_CLASS_ID).sum())
    
    if save_annotated:
        annotated = results.plot()
        out_path = str(Path(image_path).with_suffix("")) + "_annotated.jpg"
        cv2.imwrite(out_path, annotated)
        print(f"SAVED ANNOTATED IMAGE TO: {out_path}")

    return people_count
    

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Count the people on the image")
    parser.add_argument("image_path", help="Image path *JPG OR PNG*")
    parser.add_argument("--conf", type=float, default=0.35, help="confidence threshold")
    parser.add_argument("--no-save", action="store_true", help="if you don't want to save annotated image")
    args = parser.parse_args()



    num = count_people_in_img(args.image_path, conf=args.conf, save_annotated=not args.no_save)
    print(f"NUMBER OF PEOPLE DETECTED: {num}")
