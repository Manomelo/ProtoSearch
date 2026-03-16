import { useEffect, useState } from "react"

function SearchBar({ query, setQuery, onSearch }) {
    const [suggestions, setSuggestions] = useState([])
    const [selectedIndex, setSelectedIndex] = useState(-1)

    useEffect(() => {
        if (query.length < 2) {
            setSuggestions([])
            return
        }
        fetch('/search?q=' + query)
            .then(res => res.json())
            .then(data => setSuggestions(data.results))
    }, [query])

    const handleKeyDown = (e) => {
        if (e.key === 'Enter') {
            if(selectedIndex >= 0 && suggestions[selectedIndex]){
                window.location.href = suggestions[selectedIndex].url
            }
            else {
                setSuggestions([])
                onSearch(query, 0)
            }
        } else if (e.key === "ArrowDown"){
            if(selectedIndex < suggestions.length - 1){
                setSelectedIndex(selectedIndex + 1)
            }
        } else if (e.key === "ArrowUp"){
            if (selectedIndex >= -1){
                setSelectedIndex(selectedIndex - 1)
            }
        }
    }



    return (
        <div className="relative w-full max-w-2xl">
            <input
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                onKeyDown={handleKeyDown}
                placeholder="Search..."
                className="w-full px-5 py-3 rounded-full border border-gray-300 shadow-sm focus:outline-none focus:border-blue-400 text-lg"
            />
            {suggestions.length > 0 && (
                <div className="absolute w-full bg-white border border-gray-200 rounded-2xl shadow-lg mt-1 z-10">
                    {suggestions.map((item, index) => (
                        <a
                            key={item.url}
                            href={item.url}
                            className={`block px-5 py-3 text-gray-800 first:rounded-t-2xl last:rounded-b-2xl ${
                                index === selectedIndex
                                    ? 'bg-gray-200'
                                    : 'hover:bg-gray-100'
                            }`}
                        >
                            {item.title}
                        </a>
                    ))}
                </div>
            )}
        </div>
    )
}

export default SearchBar