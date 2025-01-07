import unittest
from unittest.mock import patch, MagicMock

from src.MinIO.main import main, S3Error

class TestMinioScript(unittest.TestCase):

    @patch("src.MinIO.main.Minio")
    def test_main_bucket_does_not_exist(self, mock_minio_class):
        mock_client = MagicMock()
        mock_minio_class.return_value = mock_client

        mock_client.bucket_exists.return_value = False

        main()

        mock_client.bucket_exists.assert_called_once_with("my-bucket")
        mock_client.make_bucket.assert_called_once_with("my-bucket")
        mock_client.fput_object.assert_called_once_with("my-bucket", "example.txt", "example.txt")
        mock_client.fget_object.assert_called_once_with("my-bucket", "example.txt", "downloaded_example.txt")

    @patch("src.MinIO.main.Minio")
    def test_main_bucket_already_exists(self, mock_minio_class):
        mock_client = MagicMock()
        mock_minio_class.return_value = mock_client

        mock_client.bucket_exists.return_value = True

        main()

        mock_client.bucket_exists.assert_called_once_with("my-bucket")
        mock_client.make_bucket.assert_not_called()
        mock_client.fput_object.assert_called_once_with("my-bucket", "example.txt", "example.txt")
        mock_client.fget_object.assert_called_once_with("my-bucket", "example.txt", "downloaded_example.txt")

    @patch("src.MinIO.main.Minio")
    def test_main_s3error(self, mock_minio_class):
        mock_client = MagicMock()
        mock_minio_class.return_value = mock_client

        mock_client.bucket_exists.side_effect = S3Error("BadRequest", "Something", "", "", "", None)

        with self.assertRaises(S3Error):
            main()

    def test_main_s3error_logging2(self, mock_print):
        mock_client = MagicMock()
        mock_minio_class.return_value = mock_client
        mock_client.bucket_exists.side_effect = S3Error("BadRequest", "Something", "", "", "", None)

        main()
        self.assertTrue(any("An error occurred:" in call[0][0] for call in mock_print.call_args_list))

if __name__ == "__main__":
    unittest.main()
