import { useState } from 'react'
import SearchBar from './components/Search/SearchBar'

function App() {
    const [query, setQuery] = useState("")
    const [results, setResults] = useState([])
    const [hasSearched, setHasSearched] = useState(false)
    const [currentPage, setCurrentPage] = useState(0)
    const [totalResults, setTotalResults] = useState(0)
    const [pageSize, setPageSize] = useState(10)

    const handleSearch = (searchQuery, page = 0) => {
        fetch(`/search?q=${searchQuery}&page=${page}`)
            .then(res => res.json())
            .then(data => {
                setResults(data.results)
                setHasSearched(true)
                setCurrentPage(data.page)
                setTotalResults(data.totalResults)
                setPageSize(data.pageSize)
            })
    }

    const totalPages = Math.ceil(totalResults / pageSize)

    const handlePageChange = (page) => {
        handleSearch(query, page)
        window.scrollTo(0, 0)
    }

    return (
        <div className="min-h-screen bg-white">
            <div className={`flex flex-col items-center ${hasSearched ? 'pt-8' : 'justify-center min-h-screen'}`}>
                <button
                    onClick={() => {
                        setHasSearched(false)
                        setResults([])
                        setQuery("")
                        setCurrentPage(0)
                        setTotalResults(0)
                    }}
                    className={`font-bold text-blue-600 cursor-pointer transition-all ${hasSearched ? 'text-3xl mb-4' : 'text-6xl mb-8'}`}>
                    Proto Search
                </button>
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

                        {totalPages > 1 && (
                            <div className="flex justify-center gap-2 mt-8 mb-8">
                                {Array.from({ length: totalPages }, (_, i) => i).map(page => (
                                    <button
                                        key={page}
                                        onClick={() => handlePageChange(page)}
                                        className={`px-4 py-2 rounded-full text-sm font-medium ${
                                            page === currentPage
                                                ? 'bg-blue-600 text-white'
                                                : 'text-blue-600 hover:bg-gray-100'
                                        }`}
                                    >
                                        {page + 1}
                                    </button>
                                ))}
                            </div>
                        )}
                    </div>
                )}
            </div>
        </div>
    )
}

export default App