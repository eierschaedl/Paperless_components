import os
import json
import pika
from pdf2image import convert_from_path
from PIL import Image
import pytesseract

def process_pdf(pdf_path, output_folder):
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

    # 2) Pfad /app/pdfs/DATEINAME
    pdf_path = os.path.join("/app/pdfs", file_name)
    print(f"[*] Full PDF path: {pdf_path}")

    # 3) OCR-Verarbeitung
    output_folder = "/app/output"
    os.makedirs(output_folder, exist_ok=True)
    extracted_text = process_pdf(pdf_path, output_folder)

    # 4) JSON-Objekt bauen (Dateiname + OCR-Text)
    result_payload = {
        "filepath": file_name,
        "extractedText": extracted_text
    }
    # -> z.B. {"filename":"test_pdf.pdf","extracted_text":["Seite1","Seite2"]}

    result_json = json.dumps(result_payload)

    # 5) Ergebnis an RESULT_QUEUE senden
    ch.basic_publish(
        exchange='',
        routing_key='RESULT_QUEUE',
        body=result_json.encode('utf-8')
    )
    print("[x] Sent OCR result (JSON) to RESULT_QUEUE")

    # 6) Ack, wenn fertig
    ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
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
