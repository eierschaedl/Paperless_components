import unittest
from unittest.mock import patch, MagicMock, call
import os
import json

from pdf2image import convert_from_path
from PIL import Image
import pytesseract

from src.OCR.main import (
    download_pdf_from_minio,
    process_pdf,
    callback,
)


class TestOcrWorker(unittest.TestCase):

    @patch("src.OCR.main.Minio")
    def test_download_pdf_from_minio(self, mock_minio_class):
        mock_minio_client = MagicMock()
        mock_minio_class.return_value = mock_minio_client

        bucket_name = "documents"
        object_name = "test.pdf"
        local_file = "/app/pdfs/test.pdf"

        download_pdf_from_minio(
            minio_client=mock_minio_client,
            bucket_name=bucket_name,
            object_name=object_name,
            local_file=local_file
        )

        mock_minio_client.fget_object.assert_called_once_with(
            bucket_name, object_name, local_file
        )

    @patch("src.OCR.main.convert_from_path")
    @patch("src.OCR.main.pytesseract.image_to_string")
    def test_process_pdf_success(self, mock_ocr, mock_convert_from_path):
        fake_image1 = MagicMock(spec=Image.Image)
        fake_image2 = MagicMock(spec=Image.Image)
        mock_convert_from_path.return_value = [fake_image1, fake_image2]

        mock_ocr.side_effect = ["Text1", "Text2"]

        pdf_path = "/tmp/test.pdf"
        result = process_pdf(pdf_path)

        self.assertEqual(result, ["Text1", "Text2"])
        mock_convert_from_path.assert_called_once_with(pdf_path)
        self.assertEqual(mock_ocr.call_count, 2)

    @patch("src.OCR.main.convert_from_path", side_effect=Exception("PDF error"))
    def test_process_pdf_exception(self, mock_convert_from_path):
        pdf_path = "/tmp/invalid.pdf"
        result = process_pdf(pdf_path)
        self.assertEqual(result, [])
        mock_convert_from_path.assert_called_once_with(pdf_path)

    @patch("src.OCR.main.download_pdf_from_minio")
    @patch("src.OCR.main.process_pdf", return_value=["DummyText"])
    @patch("src.OCR.main.json.dumps", wraps=json.dumps)  # Um den Aufruf von json.dumps zu beobachten
    def test_callback(
        self, mock_json_dumps, mock_process_pdf, mock_download_pdf
    ):
        mock_channel = MagicMock()
        mock_method = MagicMock(delivery_tag=123)
        mock_properties = MagicMock()

        file_name = "test_file.pdf"
        body = file_name.encode("utf-8")

        callback(
            ch=mock_channel,
            method=mock_method,
            properties=mock_properties,
            body=body
        )

        mock_download_pdf.assert_called_once_with(
            unittest.mock.ANY,
            "documents",
            file_name,
            "/app/pdfs/" + file_name
        )

        mock_process_pdf.assert_called_once_with("/app/pdfs/" + file_name)

        mock_channel.basic_publish.assert_called_once()
        args, kwargs = mock_channel.basic_publish.call_args
        self.assertEqual(kwargs["exchange"], "")
        self.assertEqual(kwargs["routing_key"], "RESULT_QUEUE")

        published_body = kwargs["body"]
        published_json = json.loads(published_body.decode("utf-8"))
        self.assertEqual(published_json["filename"], file_name)
        self.assertEqual(published_json["extracted_text"], ["DummyText"])

        mock_channel.basic_ack.assert_called_once_with(delivery_tag=123)


if __name__ == "__main__":
    unittest.main()
