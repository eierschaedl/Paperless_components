import os
import json
import pika
from minio import Minio
from pdf2image import convert_from_path
from PIL import Image
import pytesseract

minio_client = None

def download_pdf_from_minio(minio_client, bucket_name, object_name, local_file):
    # Lädt ein PDF aus MinIO herunter und speichert es im OCR Container
    print(f"[+] Downloading from MinIO: bucket={bucket_name}, object={object_name}")
    minio_client.fget_object(bucket_name, object_name, local_file)
    print(f"[+] Download completed: {local_file}")

def process_pdf(pdf_path):
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
    # Queue Management
    file_name = body.decode('utf-8').strip('"')
    print(f"[*] Received file name: {file_name}")
    local_file = "/app/pdfs/" + file_name

    download_pdf_from_minio(minio_client, "documents", file_name, local_file)
    extracted_texts = process_pdf(local_file)
    result_dict = {
        "filename": file_name,
        "extracted_text": extracted_texts
    }
    result_json = json.dumps(result_dict)
    ch.basic_publish(exchange='', routing_key='RESULT_QUEUE', body=result_json.encode('utf-8'))
    print("[x] Sent OCR result (JSON) to RESULT_QUEUE")
    ch.basic_ack(delivery_tag=method.delivery_tag)

def main():
    # Verbindungen zu MinIO und RabbitMQ
    minio_host = os.getenv("MINIO_HOST", "minio")
    minio_port = os.getenv("MINIO_PORT", "9000")
    minio_access_key = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
    minio_secret_key = os.getenv("MINIO_SECRET_KEY", "minioadmin")
    use_secure = False

    global minio_client
    minio_client = Minio(
        f"{minio_host}:{minio_port}",
        access_key=minio_access_key,
        secret_key=minio_secret_key,
        secure=use_secure
    )

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

    channel.queue_declare(queue='OCR_QUEUE', durable=True)
    channel.queue_declare(queue='RESULT_QUEUE', durable=True)

    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(
        queue='OCR_QUEUE',
        on_message_callback=callback,
        auto_ack=False
    )

    print("[*] Waiting for messages. To exit press CTRL+C")
    channel.start_consuming()

if __name__ == "__main__":
    main()
