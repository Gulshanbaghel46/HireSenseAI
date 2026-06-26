from fer import FER
import cv2

detector = FER()

camera = cv2.VideoCapture(0)

while True:

    ret, frame = camera.read()

    if not ret:
        break

    emotions = detector.detect_emotions(frame)

    for emotion in emotions:

        x, y, w, h = emotion["box"]

        dominant_emotion = max(
            emotion["emotions"],
            key=emotion["emotions"].get
        )

        cv2.rectangle(
            frame,
            (x, y),
            (x + w, y + h),
            (0,255,0),
            2
        )

        cv2.putText(
            frame,
            dominant_emotion,
            (x, y - 10),
            cv2.FONT_HERSHEY_SIMPLEX,
            1,
            (0,255,0),
            2
        )

    cv2.imshow(
        "Emotion Detection",
        frame
    )

    if cv2.waitKey(1) == ord('q'):
        break

camera.release()

cv2.destroyAllWindows()