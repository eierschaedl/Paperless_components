import os
import json
import pika
from minio import Minio
from pdf2image import convert_from_path
from PIL import Image
import pytesseract

def download_pdf_from_minio(minio_client, bucket_name, object_name, local_file):
    """
    Lädt ein PDF aus MinIO (bucket_name/object_name) herunter
    und speichert es in local_file.
    """
    print(f"[+] Downloading from MinIO: bucket={bucket_name}, object={object_name}")
    minio_client.fget_object(bucket_name, object_name, local_file)
    print(f"[+] Download completed: {local_file}")


def process_pdf(pdf_path):
    """
    Öffnet das angegebene PDF, konvertiert jede Seite in ein Image
    und führt dann OCR durch, um den Text auszulesen.
    """
    try:
        images = convert_from_path(pdf_path)
        extracted_text = []

        for i, image in enumerate(images):
            text = pytesseract.image_to_string(image)
            extracted_text.append(text)

            print(f"Extracted text from page {i + 1}:\n{text}\n")

        return extracted_text
    except Exception as e:
        print(f"Failed to process PDF {pdf_path}: {e}")
        return []

def callback(ch, method, properties, body):
    """
    Consumer-Callback für 'OCR_QUEUE'.
    Erwartet einen Dateinamen als String (ggf. mit Anführungszeichen drumherum).
    Baut den vollständigen Pfad, führt OCR aus und schickt das Ergebnis
    als JSON zurück an 'RESULT_QUEUE'.
    """

    # 1) Dateiname auslesen und Anführungszeichen entfernen
    file_name = body.decode('utf-8').strip('"')
    print(f"[*] Received file name: {file_name}")

    # Lokaler Pfad im Container, wohin du speichern möchtest:
    local_file = "/app/pdfs/" + file_name

    # 1) PDF von MinIO herunterladen
    download_pdf_from_minio(minio_client, "documents", file_name, local_file)

    # 2) OCR durchführen
    extracted_texts = process_pdf(local_file)
    # ...

    # 3) OPTIONAL: Ergebnis an RESULT_QUEUE schicken
    result_dict = {
        "filename": file_name,
        "extracted_text": extracted_texts
    }
    result_json = json.dumps(result_dict)
    ch.basic_publish(exchange='', routing_key='RESULT_QUEUE', body=result_json.encode('utf-8'))
    print("[x] Sent OCR result (JSON) to RESULT_QUEUE")

    # 4) Nachricht bestätigen (wenn auto_ack=False)
    ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    # 1) MinIO-Verbindungsdaten aus ENV lesen
    minio_host = os.getenv("MINIO_HOST", "minio")
    minio_port = os.getenv("MINIO_PORT", "9000")
    minio_access_key = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
    minio_secret_key = os.getenv("MINIO_SECRET_KEY", "minioadmin")
    use_secure = False  # Falls du kein HTTPS in MinIO hast

    global minio_client
    minio_client = Minio(
        f"{minio_host}:{minio_port}",
        access_key=minio_access_key,
        secret_key=minio_secret_key,
        secure=use_secure
    )

    # Verbindung zu RabbitMQ
    rabbitmq_user = os.getenv("RABBITMQ_USER", "rabbitmqadmin")
    rabbitmq_pass = os.getenv("RABBITMQ_PASS", "rabbitmqadmin")

    credentials = pika.PlainCredentials(rabbitmq_user, rabbitmq_pass)
    connection = pika.BlockingConnection(
        pika.ConnectionParameters(
            host='rabbitmq',
            credentials=credentials
        )
    )
    channel = connection.channel()

    # Sicherstellen, dass die Queues existieren
    channel.queue_declare(queue='OCR_QUEUE', durable=True)
    channel.queue_declare(queue='RESULT_QUEUE', durable=True)

    channel.basic_qos(prefetch_count=1)

    # Consumer registrieren
    channel.basic_consume(
        queue='OCR_QUEUE',
        on_message_callback=callback,
        auto_ack=False
    )

    print("[*] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()

if __name__ == "__main__":
    main()
