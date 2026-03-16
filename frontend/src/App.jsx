import { useState } from 'react'
import SearchBar from './components/Search/SearchBar.jsx'

function App() {
    const [query, setQuery] = useState("")
    const [results, setResults] = useState([])
    const [hasSearched, setHasSearched] = useState(false)

    const handleSearch = (searchQuery) => {
        fetch('/search?q=' + searchQuery)
            .then(res => res.json())
            .then(data => {
                setResults(data.results)
                setHasSearched(true)
            })
    }

    return (
        <div className="min-h-screen bg-white">
            <div className={`flex flex-col items-center ${hasSearched ? 'pt-8' : 'justify-center min-h-screen'}`}>
                <h1 className={`font-bold text-blue-600 transition-all ${hasSearched ? 'text-3xl mb-4' : 'text-6xl mb-8'}`}>
                    ProtoSearch
                </h1>
                <SearchBar
                    query={query}
                    setQuery={setQuery}
                    onSearch={handleSearch}
                />
                {hasSearched && (
                    <div className="w-full max-w-2xl mt-6">
                        {results.map(result => (
                            <div key={result.url} className="mb-6">
                                <a
                                    href={result.url}
                                    className="text-xl text-blue-700 hover:underline font-medium"
                                >
                                    {result.title}
                                </a>
                                <p className="text-green-700 text-sm">{result.url}</p>
                                <p className="text-gray-600 text-sm mt-1">{result.snippet}</p>
                            </div>
                        ))}
                    </div>
                )}
            </div>
        </div>
    )
}

export default App