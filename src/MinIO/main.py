from minio import Minio
from minio.error import S3Error

def main():

    client = Minio(
        "minio:9000",  # Name des MinIO-Containers (aus Docker Compose)
        access_key="minioadmin",
        secret_key="minioadmin",
        secure=False  # HTTPS deaktivieren
    )

    bucket_name = "my-bucket"

    if not client.bucket_exists(bucket_name):
        client.make_bucket(bucket_name)
        print(f"Bucket '{bucket_name}' created.")
    else:
        print(f"Bucket '{bucket_name}' already exists.")

    file_path = "example.txt"
    client.fput_object(bucket_name, file_path, file_path)
    print(f"File '{file_path}' uploaded to bucket '{bucket_name}'.")

    downloaded_file = "downloaded_example.txt"
    client.fget_object(bucket_name, file_path, downloaded_file)
    print(f"File '{file_path}' downloaded as '{downloaded_file}'.")

if __name__ == "__main__":
    try:
        main()
    except S3Error as e:
        print(f"An error occurred: {e}")
