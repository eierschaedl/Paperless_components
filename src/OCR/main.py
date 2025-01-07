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
            # OCR auf dem Bild
            text = pytesseract.image_to_string(image)
            extracted_text.append(text)

            print(f"Extracted text from page {i + 1}:\n{text}\n")

        return extracted_text
    except Exception as e:
        print(f"Failed to process PDF {pdf_path}: {e}")
        return []

def callback(ch, method, properties, body):
    """
    Wird aufgerufen, sobald eine Nachricht in der Queue 'OCR_QUEUE' ankommt.
    - Erwartet: Einen Dateinamen als String (ggf. mit umschließenden " zu Beginn/Ende).
    - Verarbeitet das PDF mittels OCR.
    - Schickt das Ergebnis (extracted_text) in die RESULT_QUEUE.
    - Bestätigt anschließend die Nachricht (manual ack).
    """
    # 1) Dateinamen aus Message lesen und Anführungszeichen entfernen
    file_name = body.decode('utf-8').strip('"')
    print(f"[*] Received file name: {file_name}")

    # 2) Pfad zusammensetzen -> /app/pdfs/test_pdf.pdf
    pdf_path = os.path.join("/app/pdfs", file_name)
    print(f"[*] Full PDF path: {pdf_path}")

    # 3) OCR-Verarbeitung
    output_folder = "/app/output"
    os.makedirs(output_folder, exist_ok=True)
    extracted_text = process_pdf(pdf_path, output_folder)

    # 4) Ergebnis in RESULT_QUEUE schicken
    #    Wir wandeln die Liste der Seiten in JSON um. Du kannst natürlich auch join() benutzen.
    extracted_text_json = json.dumps(extracted_text)

    ch.basic_publish(
        exchange='',
        routing_key='RESULT_QUEUE',
        body=extracted_text_json.encode('utf-8')
    )
    print(f"[x] Sent OCR result to RESULT_QUEUE")

    # 5) Nachricht bestätigen (weil auto_ack=False in main())
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

    # 1) OCR_QUEUE deklarieren
    channel.queue_declare(queue='OCR_QUEUE', durable=True)
    # 2) RESULT_QUEUE deklarieren (falls du sie nicht an anderer Stelle deklarierst)
    channel.queue_declare(queue='RESULT_QUEUE', durable=True)

    channel.basic_qos(prefetch_count=1)

    # Consumer konfigurieren -> manuelles Ack
    channel.basic_consume(
        queue='OCR_QUEUE',
        on_message_callback=callback,
        auto_ack=False
    )

    print("[*] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()

if __name__ == "__main__":
    main()
