import os
from pdf2image import convert_from_path
from PIL import Image
import pytesseract

def process_pdf(pdf_path, output_folder):
    try:
        images = convert_from_path(pdf_path)
        extracted_text = []

        for i, image in enumerate(images):
            image_path = os.path.join(output_folder, f"page_{i + 1}.jpg")
            image.save(image_path, 'JPEG')

            text = pytesseract.image_to_string(image)
            extracted_text.append(text)

            print(f"Extracted text from page {i + 1}:\n{text}\n")

        return extracted_text

    except Exception as e:
        print(f"Failed to process PDF {pdf_path}: {e}")
        return []

if __name__ == "__main__":
    pdf_folder = "/app/pdfs"
    output_folder = "/app/output"
    os.makedirs(output_folder, exist_ok=True)

    for pdf_file in os.listdir(pdf_folder):
        if pdf_file.endswith(".pdf"):
            pdf_path = os.path.join(pdf_folder, pdf_file)
            print(f"Processing {pdf_path}...")
            process_pdf(pdf_path, output_folder)
