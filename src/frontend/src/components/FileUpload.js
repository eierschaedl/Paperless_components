import React, { useState } from 'react';
import axios from 'axios';

const FileUpload = () => {
    const [fileData, setFileData] = useState({
        file: null,
        fileName: '',
        filePath: '',
        timestamp: '',
    });

    const handleFileChange = (event) => {
        const file = event.target.files[0];
        if (file && file.type === 'application/pdf') {
            const timestamp = new Date().toISOString();
            setFileData({
                file,
                fileName: file.name,
                filePath: `/dummy/path/to/${file.name}`,  // Dummy file path
                timestamp,
            });
        } else {
            alert("Please select a PDF file.");
        }
    };

    const handleUpload = async () => {
        if (!fileData.file) {
            alert("Please select a file first!");
            return;
        }

        const formData = new FormData();
        formData.append('file', fileData.file);

        try {
            const response = await axios.post('http://localhost:8081/document/upload', formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                },
            });

            if (response.status === 200) {
                alert("File uploaded successfully!");
            } else {
                alert("File upload failed.");
            }
        } catch (error) {
            console.error("Error uploading file:", error);
            alert("An error occurred while uploading the file.");
        }
    };

    return (
        <div>
            <h2>Upload PDF File</h2>
            <input type="file" accept="application/pdf" onChange={handleFileChange} />
            {fileData.fileName && (
                <div>
                    <p>File name: {fileData.fileName}</p>
                </div>
            )}
            <button onClick={handleUpload}>Upload</button>
        </div>
    );
};

export default FileUpload;
