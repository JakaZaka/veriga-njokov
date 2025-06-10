import { useEffect, useState, useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../FormAndStoreCard.css';

const clothingIcons = {
    tops: "👕",
    bottoms: "👖",
    shoes: "👟",
    outerwear: "🧥",
    accessories: "🧢",
    dresses: "👗"
};
const categories = ["tops", "bottoms", "outerwear", "shoes", "accessories", "dresses"];
const basicColors = [
    "Black", "White", "Gray", "Red", "Blue", "Green", "Yellow", "Pink", "Purple", "Brown", "Beige", "Orange"
];
const fixedSizes = [
    "XS", "S", "M", "L", "XL", "2XL",
    "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"
];
const seasonOptions = [
    { key: "spring", icon: "🌸", label: "Spring" },
    { key: "summer", icon: "☀️", label: "Summer" },
    { key: "fall", icon: "🍂", label: "Fall" },
    { key: "winter", icon: "❄️", label: "Winter" },
    { key: "all", icon: "🌀", label: "All" }
];
const OCCASION_OPTIONS = ['casual', 'formal', 'sport', 'party', 'work', 'other'];

function AddOutfit() {
    const [name, setName] = useState('');
    const [selectedItems, setSelectedItems] = useState([]);
    const [clothes, setClothes] = useState([]);
    const [uploaded, setUploaded] = useState(false);
    const [season, setSeason] = useState([]);
    const [occasion, setOccasion] = useState('');
    const userContext = useContext(UserContext);

    // Filters for closet grid
    const [categoryFilters, setCategoryFilters] = useState({
        tops: false,
        bottoms: false,
        shoes: false,
        outerwear: false,
        accessories: false,
        dresses: false,
    });
    const [colorFilters, setColorFilters] = useState([]);
    const [sizeFilters, setSizeFilters] = useState([]);
    const [seasonFilters, setSeasonFilters] = useState([]);

    useEffect(() => {
        async function fetchClothes() {
            const token = localStorage.getItem('token');
            const res = await fetch('/api/clothing?mine=true', {
                method: 'GET',
                credentials: 'include',
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            const data = await res.json();
            setClothes(data);
        }
        fetchClothes();
    }, []);

    function toggleItem(item) {
        setSelectedItems(prev =>
            prev.includes(item._id)
                ? prev.filter(id => id !== item._id)
                : [...prev, item._id]
        );
    }

    // Season icon toggle for outfit
    function toggleSeasonIcon(option) {
        setSeason(prev =>
            prev.includes(option)
                ? prev.filter(s => s !== option)
                : [...prev, option]
        );
    }

    // Closet filter handlers
    const toggleColor = (color) => {
        setColorFilters(prev =>
            prev.includes(color)
                ? prev.filter(c => c !== color)
                : [...prev, color]
        );
    };
    const clearColorFilters = () => setColorFilters([]);
    const toggleSize = (size) => {
        setSizeFilters(prev =>
            prev.includes(size)
                ? prev.filter(s => s !== size)
                : [...prev, size]
        );
    };
    const clearSizeFilters = () => setSizeFilters([]);
    const toggleSeason = (seasonKey) => {
        setSeasonFilters(prev =>
            prev.includes(seasonKey)
                ? prev.filter(s => s !== seasonKey)
                : [...prev, seasonKey]
        );
    };
    const clearSeasonFilters = () => setSeasonFilters([]);

    // Category filter logic
    const activeCategories = Object.entries(categoryFilters)
        .filter(([_, val]) => val)
        .map(([key]) => key);

    // Filtering logic for closet grid
    const filteredClothes = clothes.filter(item => {
        const matchesCategory =
            activeCategories.length === 0 ||
            (item.category && activeCategories.includes(item.category.toLowerCase()));
        const matchesColor =
            colorFilters.length === 0 ||
            (item.color && colorFilters.includes(item.color));
        const matchesSize =
            sizeFilters.length === 0 ||
            (item.size && sizeFilters.includes(item.size));
        const matchesSeason =
            seasonFilters.length === 0 ||
            (item.season && (
                Array.isArray(item.season)
                    ? item.season.some(s => seasonFilters.includes(s))
                    : seasonFilters.includes(item.season)
            ));
        // Only show user's own items
        const itemUserId = typeof item.user === 'object' && item.user !== null
            ? item.user._id
            : item.user;
        const isMine = itemUserId && userContext.user._id && String(itemUserId) === String(userContext.user._id);
        return matchesCategory && matchesColor && matchesSize && matchesSeason && isMine;
    });

    async function handleSubmit(e) {
        e.preventDefault();
        const selectedImages = clothes
            .filter(item => selectedItems.includes(item._id))
            .map(item => item.imageUrl);

        const body = {
            name,
            items: selectedItems.map(id => ({ item: id })),
            images: selectedImages,
        };
        if (season.length > 0) body.season = season;
        if (occasion) body.occasion = occasion;

        const token = localStorage.getItem('token');
        const res = await fetch('/api/outfits', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { Authorization: `Bearer ${token}` } : {})
            },
            body: JSON.stringify(body),
        });
        if (res.ok) setUploaded(true);
    }

    if (uploaded) return <Navigate replace to="/" />;

    return (
        <div style={{
            minHeight: '100vh',
            width: '100vw',
            background: '#f5f8fa',
            display: 'flex',
            alignItems: 'flex-start',
            justifyContent: 'center',
            padding: 0,
            margin: 0
        }}>
            <div style={{
                background: '#fff',
                borderRadius: 18,
                boxShadow: '0 4px 24px rgba(25, 118, 210, 0.10), 0 1.5px 4px rgba(0,0,0,0.06)',
                width: '100%',
                maxWidth: 1200,
                margin: '32px 0',
                padding: '36px 28px 28px 28px',
                fontFamily: "'Segoe UI', 'Roboto', Arial, sans-serif",
                minHeight: 'calc(100vh - 64px)'
            }}>
                <h2 style={{ textAlign: 'center', color: '#1976d2', fontWeight: 700, marginBottom: 24 }}>Add Outfit</h2>
                <form onSubmit={handleSubmit}>
                    <div className="mb-3">
                        <label className="form-label" style={{ fontWeight: 600, color: "#1976d2", fontSize: "1.1em" }}>Outfit Name:</label>
                        <input
                            type="text"
                            className="form-control"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            required
                            style={{
                                border: "2px solid #1976d2",
                                borderRadius: "10px",
                                padding: "12px 16px",
                                fontSize: "1.15em",
                                fontWeight: 500,
                                background: "#fafdff",
                                color: "#222",
                                marginBottom: 0,
                                boxShadow: "0 2px 8px #e3eafc33"
                            }}
                            placeholder="Enter outfit name"
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label" style={{ fontWeight: 600, color: "#1976d2", fontSize: "1.1em" }}>Season:</label>
                        <div style={{ display: 'flex', gap: '18px', marginBottom: '1rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                            {seasonOptions.map(seasonOption => (
                                <div key={seasonOption.key} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <button
                                        type="button"
                                        onClick={() => toggleSeasonIcon(seasonOption.key)}
                                        style={{
                                            width: 56,
                                            height: 56,
                                            borderRadius: '12px',
                                            border: season.includes(seasonOption.key) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                            background: season.includes(seasonOption.key) ? '#e3eafc' : '#fff',
                                            color: '#222',
                                            cursor: 'pointer',
                                            boxShadow: season.includes(seasonOption.key) ? '0 2px 8px #e3eafc' : 'none',
                                            outline: season.includes(seasonOption.key) ? '2px solid #1976d2' : 'none',
                                            marginBottom: 2,
                                            fontSize: '2.6em',
                                            display: 'flex',
                                            alignItems: 'center',
                                            justifyContent: 'center',
                                            transition: 'border 0.15s',
                                            position: 'relative',
                                            overflow: 'hidden'
                                        }}
                                        aria-pressed={season.includes(seasonOption.key)}
                                        title={seasonOption.label}
                                    >
                                        <span style={{
                                            display: 'block',
                                            position: 'relative',
                                            lineHeight: 1
                                        }}>
                                            {seasonOption.icon}
                                        </span>
                                    </button>
                                    <span style={{
                                        fontSize: '0.95em',
                                        color: '#444',
                                        marginTop: 1,
                                        textAlign: 'center'
                                    }}>
                                        {seasonOption.label}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" style={{ fontWeight: 600, color: "#1976d2", fontSize: "1.1em" }}>Occasion:</label>
                        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap', marginBottom: 0 }}>
                            {OCCASION_OPTIONS.map(opt => (
                                <button
                                    key={opt}
                                    type="button"
                                    onClick={() => setOccasion(opt)}
                                    style={{
                                        padding: '8px 22px',
                                        borderRadius: '8px',
                                        border: occasion === opt ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                        background: occasion === opt ? '#e3eafc' : '#fff',
                                        color: occasion === opt ? '#1976d2' : '#333',
                                        fontWeight: 600,
                                        fontSize: '1.08em',
                                        cursor: 'pointer',
                                        boxShadow: occasion === opt ? '0 2px 8px #e3eafc' : 'none',
                                        transition: 'border 0.15s'
                                    }}
                                    aria-pressed={occasion === opt}
                                >
                                    {opt.charAt(0).toUpperCase() + opt.slice(1)}
                                </button>
                            ))}
                        </div>
                    </div>
                    <div style={{
                        borderTop: "2px solid #e3eafc",
                        margin: "32px 0 22px 0",
                        width: "100%"
                    }} />
                    <div className="mb-3">
                        <label className="form-label" style={{ fontWeight: 600, color: "#1976d2", fontSize: "1.1em" }}>Select clothing items for this outfit:</label>
                        {/* Closet-like filters */}
                        <div style={{ marginBottom: 18 }}>
                            {/* Category Filters */}
                            <div style={{ display: 'flex', gap: '10px', marginBottom: '1rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                                {categories.map(category => (
                                    <button
                                        key={category}
                                        type="button"
                                        onClick={() =>
                                            setCategoryFilters(prev => ({
                                                ...prev,
                                                [category]: !prev[category]
                                            }))
                                        }
                                        style={{
                                            padding: '6px 10px',
                                            borderRadius: '6px',
                                            border: categoryFilters[category] ? '2px solid #1976d2' : '1px solid #ccc',
                                            backgroundColor: categoryFilters[category] ? '#e3eafc' : '#fff',
                                            color: categoryFilters[category] ? '#1976d2' : '#333',
                                            fontWeight: 600,
                                            fontSize: '18px',
                                            display: 'flex',
                                            alignItems: 'center',
                                            gap: '4px',
                                            cursor: 'pointer',
                                            boxShadow: categoryFilters[category] ? '0 2px 8px #e3eafc' : 'none'
                                        }}
                                        aria-pressed={categoryFilters[category]}
                                    >
                                        <span>{clothingIcons[category]}</span>
                                        <span>{category.charAt(0).toUpperCase() + category.slice(1)}</span>
                                    </button>
                                ))}
                            </div>
                            {/* Color Filters */}
                            <div style={{ display: 'flex', gap: '18px', marginBottom: '1.5rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <button
                                        type="button"
                                        onClick={clearColorFilters}
                                        style={{
                                            width: 36,
                                            height: 36,
                                            borderRadius: '6px',
                                            border: colorFilters.length === 0 ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                            background: '#e3eafc',
                                            color: 'transparent',
                                            cursor: 'pointer',
                                            boxShadow: colorFilters.length === 0 ? '0 2px 8px #e3eafc' : 'none',
                                            outline: colorFilters.length === 0 ? '2px solid #1976d2' : 'none',
                                            marginBottom: 2,
                                            transition: 'border 0.15s'
                                        }}
                                        aria-pressed={colorFilters.length === 0}
                                        title="All Colors"
                                    >
                                        &nbsp;
                                    </button>
                                    <span style={{
                                        fontSize: '0.95em',
                                        color: '#444',
                                        marginTop: 2,
                                        textAlign: 'center'
                                    }}>
                                        All
                                    </span>
                                </div>
                                {basicColors.map(color => (
                                    <div key={color} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                        <button
                                            type="button"
                                            onClick={() => toggleColor(color)}
                                            style={{
                                                width: 36,
                                                height: 36,
                                                borderRadius: '6px',
                                                border: colorFilters.includes(color) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                                background: color.toLowerCase(),
                                                color: 'transparent',
                                                cursor: 'pointer',
                                                boxShadow: colorFilters.includes(color) ? '0 2px 8px #e3eafc' : 'none',
                                                outline: colorFilters.includes(color) ? '2px solid #1976d2' : 'none',
                                                marginBottom: 2,
                                                transition: 'border 0.15s'
                                            }}
                                            aria-pressed={colorFilters.includes(color)}
                                            title={color}
                                        >
                                            &nbsp;
                                        </button>
                                        <span style={{
                                            fontSize: '0.95em',
                                            color: '#444',
                                            marginTop: 2,
                                            textAlign: 'center'
                                        }}>
                                            {color}
                                        </span>
                                    </div>
                                ))}
                            </div>
                            {/* Season Filters */}
                            <div style={{ display: 'flex', gap: '14px', marginBottom: '2rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <button
                                        type="button"
                                        onClick={clearSeasonFilters}
                                        style={{
                                            width: 36,
                                            height: 36,
                                            borderRadius: '6px',
                                            border: seasonFilters.length === 0 ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                            background: '#e3eafc',
                                            color: 'transparent',
                                            cursor: 'pointer',
                                            boxShadow: seasonFilters.length === 0 ? '0 2px 8px #e3eafc' : 'none',
                                            outline: seasonFilters.length === 0 ? '2px solid #1976d2' : 'none',
                                            marginBottom: 2,
                                            transition: 'border 0.15s'
                                        }}
                                        aria-pressed={seasonFilters.length === 0}
                                        title="All Seasons"
                                    >
                                        &nbsp;
                                    </button>
                                    <span style={{
                                        fontSize: '0.95em',
                                        color: '#444',
                                        marginTop: 4,
                                        textAlign: 'center'
                                    }}>
                                        All
                                    </span>
                                </div>
                                {seasonOptions.map(season => (
                                    <div key={season.key} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                        <button
                                            type="button"
                                            onClick={() => toggleSeason(season.key)}
                                            style={{
                                                width: 56,
                                                height: 56,
                                                borderRadius: '12px',
                                                border: seasonFilters.includes(season.key) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                                background: '#fff',
                                                color: '#222',
                                                cursor: 'pointer',
                                                boxShadow: seasonFilters.includes(season.key) ? '0 2px 8px #e3eafc' : 'none',
                                                outline: seasonFilters.includes(season.key) ? '2px solid #1976d2' : 'none',
                                                marginBottom: 2,
                                                fontSize: '2.6em',
                                                display: 'flex',
                                                alignItems: 'center',
                                                justifyContent: 'center',
                                                transition: 'border 0.15s',
                                                position: 'relative',
                                                overflow: 'hidden'
                                            }}
                                            aria-pressed={seasonFilters.includes(season.key)}
                                            title={season.label}
                                        >
                                            <span style={{
                                                display: 'block',
                                                position: 'relative',
                                                lineHeight: 1
                                            }}>
                                                {season.icon}
                                            </span>
                                        </button>
                                        <span style={{
                                            fontSize: '0.95em',
                                            color: '#444',
                                            marginTop: 1,
                                            textAlign: 'center'
                                        }}>
                                            {season.label}
                                        </span>
                                    </div>
                                ))}
                            </div>
                            {/* Size Filters */}
                            <div style={{ display: 'flex', gap: '14px', marginBottom: '2rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    <button
                                        type="button"
                                        onClick={clearSizeFilters}
                                        style={{
                                            minWidth: 36,
                                            minHeight: 36,
                                            borderRadius: '6px',
                                            border: sizeFilters.length === 0 ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                            background: '#e3eafc',
                                            color: 'transparent',
                                            cursor: 'pointer',
                                            boxShadow: sizeFilters.length === 0 ? '0 2px 8px #e3eafc' : 'none',
                                            outline: sizeFilters.length === 0 ? '2px solid #1976d2' : 'none',
                                            marginBottom: 2,
                                            transition: 'border 0.15s'
                                        }}
                                        aria-pressed={sizeFilters.length === 0}
                                        title="All Sizes"
                                    >
                                        &nbsp;
                                    </button>
                                    <span style={{
                                        fontSize: '0.95em',
                                        color: '#444',
                                        marginTop: 2,
                                        textAlign: 'center'
                                    }}>
                                        All
                                    </span>
                                </div>
                                {fixedSizes.map(size => (
                                    <div key={size} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                        <button
                                            type="button"
                                            onClick={() => toggleSize(size)}
                                            style={{
                                                minWidth: 36,
                                                minHeight: 36,
                                                borderRadius: '6px',
                                                border: sizeFilters.includes(size) ? '2.5px solid #1976d2' : '1.5px solid #ccc',
                                                background: sizeFilters.includes(size) ? '#e3eafc' : '#fff',
                                                color: '#333',
                                                cursor: 'pointer',
                                                boxShadow: sizeFilters.includes(size) ? '0 2px 8px #e3eafc' : 'none',
                                                outline: sizeFilters.includes(size) ? '2px solid #1976d2' : 'none',
                                                marginBottom: 2,
                                                fontWeight: 600,
                                                fontSize: '1em',
                                                transition: 'border 0.15s'
                                            }}
                                            aria-pressed={sizeFilters.includes(size)}
                                            title={size}
                                        >
                                            {size}
                                        </button>
                                    </div>
                                ))}
                            </div>
                        </div>
                        {/* Closet grid */}
                        <div className='clothes-grid'>
                            {filteredClothes.length === 0 ? (
                                <div style={{ textAlign: 'center', width: '100%', marginTop: 40 }}>
                                    No clothing items found.
                                </div>
                            ) : (
                                filteredClothes.map(item => (
                                    <div
                                        key={item._id}
                                        className="clothingItem-card"
                                        style={{
                                            position: 'relative',
                                            border: selectedItems.includes(item._id) ? '2.5px solid #1976d2' : undefined,
                                            background: selectedItems.includes(item._id) ? '#e3eafc' : undefined,
                                            cursor: 'pointer'
                                        }}
                                        onClick={() => toggleItem(item)}
                                    >
                                        <div
                                            className="clothingItem-card-img-bracket"
                                            style={{ cursor: 'pointer' }}
                                        >
                                            <img
                                                src={
                                                    item.imageUrl?.startsWith('http://') ||
                                                    item.imageUrl?.startsWith('https://') ||
                                                    item.imageUrl?.startsWith('/images/')
                                                        ? item.imageUrl
                                                        : `/images/${item.imageUrl}`
                                                }
                                                alt={item.name}
                                            />
                                        </div>
                                        <div style={{
                                            textAlign: 'center',
                                            fontWeight: 600,
                                            color: '#1976d2',
                                            marginTop: 8
                                        }}>
                                            {item.name}
                                        </div>
                                        <div style={{
                                            textAlign: 'center',
                                            color: '#555',
                                            fontSize: '0.95em'
                                        }}>
                                            {item.category} {item.size && `- ${item.size}`}
                                        </div>
                                    </div>
                                ))
                            )}
                        </div>
                    </div>
                    <button
                        type="submit"
                        className="btn"
                        style={{
                            width: '100%',
                            marginTop: 24,
                            background: "#1976d2",
                            color: "#fff",
                            fontWeight: 700,
                            fontSize: "1.25em",
                            border: "none",
                            borderRadius: "12px",
                            padding: "14px 0",
                            boxShadow: "0 4px 16px #e3eafc88",
                            letterSpacing: "0.04em",
                            transition: "background 0.15s"
                        }}
                        disabled={!name || selectedItems.length === 0}
                    >
                        <span style={{ fontSize: "1.1em", verticalAlign: "middle" }}></span> Create Outfit
                    </button>
                </form>
                <div>
                    <h4>Selected items preview:</h4>
                    <div style={{ display: 'flex', gap: '10px' }}>
                        {clothes
                            .filter(item => selectedItems.includes(item._id))
                            .map(item => (
                                <img
                                    key={item._id}
                                    src={
                                        item.imageUrl?.startsWith('http://') ||
                                        item.imageUrl?.startsWith('https://') ||
                                        item.imageUrl?.startsWith('/images/')
                                            ? item.imageUrl
                                            : `/images/${item.imageUrl}`
                                    }
                                    alt={item.name}
                                    style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '6px' }}
                                />
                            ))}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default AddOutfit;