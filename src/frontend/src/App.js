import './App.css';
import React, {useState, useEffect} from 'react';
import axios from 'axios';
import SearchBar from './components/SearchBar';

function App() {
  /*ToDo
  simple Dashboard (search, upload)
  Hello World-request to web server
   */

  const header = <h1>Paperless Application</h1>;

  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);

  const handleSearch = (searchQuery) => {
    setQuery(searchQuery);
  };

  useEffect(() => {
    if (query) {
      const fetchData = async () => {
        try {
          const response = await axios.get(`http://localhost:8081/document/search?text=${query}`);
          setResults(response.data.results);
        } catch (error) {
          console.error("Error fetching data:", error);
        }
      };
      fetchData();
    }
  }, [query]);

  return (
    <div className="App">
      {header}
      <SearchBar onSearch={handleSearch} />
    </div>
  );
}
export default App;