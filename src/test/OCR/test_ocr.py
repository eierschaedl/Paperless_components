import unittest
from unittest.mock import patch, MagicMock, call
import os
import json

# Diese Imports sind nur dann nötig, wenn du sie im Test wirklich verwendest.
# Für Mocks ist es oft ausreichend, nur den Ort zu patchen (z.B. "src.OCR.main.convert_from_path").
from pdf2image import convert_from_path
from PIL import Image
import pytesseract

# Hier importierst du nun deine Funktionen aus main.py
# (bzw. aus dem Package src.OCR.main)
from src.OCR.main import (
    download_pdf_from_minio,
    process_pdf,
    callback,
    # Falls du die globale minio_client brauchst, z. B.:
    # minio_client
)


class TestOcrWorker(unittest.TestCase):
    """
    Test-Klasse für die Funktionen im OCR Worker.
    """

    @patch("src.OCR.main.Minio")
    def test_download_pdf_from_minio(self, mock_minio_class):
        """
        Testet die download_pdf_from_minio-Funktion.
        Wir mocken den Minio-Client und prüfen, ob fget_object mit korrekten Parametern aufgerufen wird.
        """
        mock_minio_client = MagicMock()
        mock_minio_class.return_value = mock_minio_client

        bucket_name = "documents"
        object_name = "test.pdf"
        local_file = "/app/pdfs/test.pdf"

        # Aufruf der zu testenden Funktion
        download_pdf_from_minio(
            minio_client=mock_minio_client,
            bucket_name=bucket_name,
            object_name=object_name,
            local_file=local_file
        )

        # Assertions: Wurde fget_object korrekt aufgerufen?
        mock_minio_client.fget_object.assert_called_once_with(
            bucket_name, object_name, local_file
        )

    @patch("src.OCR.main.convert_from_path")
    @patch("src.OCR.main.pytesseract.image_to_string")
    def test_process_pdf_success(self, mock_ocr, mock_convert_from_path):
        """
        Testet process_pdf, wenn das PDF problemlos gelesen werden kann.
        """
        # Wir mocken convert_from_path so, dass es 2 "Images" zurückliefert
        fake_image1 = MagicMock(spec=Image.Image)
        fake_image2 = MagicMock(spec=Image.Image)
        mock_convert_from_path.return_value = [fake_image1, fake_image2]

        # Wir mocken pytesseract.image_to_string,
        # so dass es z. B. "Text1" und "Text2" zurückgibt
        mock_ocr.side_effect = ["Text1", "Text2"]

        pdf_path = "/tmp/test.pdf"
        result = process_pdf(pdf_path)

        # Erwartungen prüfen
        self.assertEqual(result, ["Text1", "Text2"])
        mock_convert_from_path.assert_called_once_with(pdf_path)
        self.assertEqual(mock_ocr.call_count, 2)

    @patch("src.OCR.main.convert_from_path", side_effect=Exception("PDF error"))
    def test_process_pdf_exception(self, mock_convert_from_path):
        """
        Testet process_pdf bei einer Exception in convert_from_path.
        Dann sollte ein leeres Array zurückkommen.
        """
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
        """
        Testet die callback-Funktion in Isolation:
        - Wir mocken den Minio-Download
        - Wir mocken process_pdf
        - Wir checken, ob wir korrekt basic_publish auf RESULT_QUEUE machen
        - Wir checken, ob basic_ack am Ende aufgerufen wird
        """

        # 1) Mock des 'channel'
        mock_channel = MagicMock()
        mock_method = MagicMock(delivery_tag=123)
        mock_properties = MagicMock()

        # Der Body enthält den Dateinamen
        file_name = "test_file.pdf"
        body = file_name.encode("utf-8")

        # 2) Aufruf der callback-Funktion
        callback(
            ch=mock_channel,
            method=mock_method,
            properties=mock_properties,
            body=body
        )

        # 3) Assertions:
        #    download_pdf_from_minio aufgerufen?
        #    Falls du in main.py einen globalen minio_client hast,
        #    kannst du ihn als src.OCR.main.minio_client ansprechen:
        mock_download_pdf.assert_called_once_with(
            # ACHTUNG: Wenn du in callback(...)
            #          download_pdf_from_minio(minio_client, ...)
            #          aufrufst und "minio_client" ist
            #          src.OCR.main.minio_client, dann muss
            #          hier dein Patch den EXACTEN Pfad matchen.
            #          Also so:
            # from src.OCR.main import minio_client
            # ...
            # oder
            # @patch("src.OCR.main.download_pdf_from_minio")
            # (wie wir's machen)
            #
            # Wenn du "minio_client" selbst in callback
            # via global abholst, dann brauchst du
            # mock_download_pdf.assert_called_once_with(
            #     main.minio_client,
            #     "documents",
            #     file_name,
            #     "/app/pdfs/" + file_name
            # )
            #
            # Hier, wir belassen es so, wie dein Callback es aufruft:
            # download_pdf_from_minio(minio_client, "documents", file_name, "/app/pdfs/" + file_name)
            #
            # => Also 4 Parameter:
            unittest.mock.ANY,  # anstelle von main.minio_client,
                                # wir akzeptieren ANY, um den Test stabil zu halten
            "documents",
            file_name,
            "/app/pdfs/" + file_name
        )

        # process_pdf aufgerufen?
        mock_process_pdf.assert_called_once_with("/app/pdfs/" + file_name)

        # basic_publish -> RESULT_QUEUE?
        mock_channel.basic_publish.assert_called_once()
        args, kwargs = mock_channel.basic_publish.call_args
        self.assertEqual(kwargs["exchange"], "")
        self.assertEqual(kwargs["routing_key"], "RESULT_QUEUE")

        # Prüfen, ob das JSON correct ausschaut
        published_body = kwargs["body"]
        published_json = json.loads(published_body.decode("utf-8"))
        self.assertEqual(published_json["filename"], file_name)
        self.assertEqual(published_json["extracted_text"], ["DummyText"])

        # basic_ack -> Liefern wir an RabbitMQ
        mock_channel.basic_ack.assert_called_once_with(delivery_tag=123)


if __name__ == "__main__":
    unittest.main()
