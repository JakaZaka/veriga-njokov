import { useState, useEffect, useContext } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import { MdOutlineCheckroom, MdDelete } from "react-icons/md";
import '../ClothingGrid.css';

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

function ClothingItems() {
    const location = useLocation();
    const { user } = useContext(UserContext);
    const [clothes, setClothes] = useState([]);
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
    const [showWantToGiveOnly, setShowWantToGiveOnly] = useState(false);
    const [sidebarItem, setSidebarItem] = useState(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);

    // For delete confirmation dialog
    const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
    const [deleteItemId, setDeleteItemId] = useState(null);

    const navigate = useNavigate();

    useEffect(() => {
        if (!user) {
            setClothes([]);
            return;
        }
        async function getClothes() {
            const token = localStorage.getItem('token');
            const res = await fetch('/api/clothing?mine=true', {
                method: 'GET',
                credentials: 'include',
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            const data = await res.json();
            setClothes(data);
        }
        getClothes();

        const interval = setInterval(getClothes, 1000 * 60 * 5);
        return () => clearInterval(interval);
    }, [location.pathname, user]);

    const handleWantToGive = async (itemId, currentValue) => {
        setClothes(prev =>
            prev.map(item =>
                item._id === itemId ? { ...item, wantToGive: !currentValue } : item
            )
        );
        const token = localStorage.getItem('token');
        const res = await fetch(`/api/clothing/${itemId}`, {
            method: 'PUT',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { Authorization: `Bearer ${token}` } : {})
            },
            body: JSON.stringify({ wantToGive: !currentValue })
        });
        if (res.ok) {
            const updatedItem = await res.json();
            setClothes(prev =>
                prev.map(item =>
                    item._id === updatedItem._id ? updatedItem : item
                )
            );
        }
    };

    // Show custom dialog instead of window.confirm
    const handleDeleteItem = (itemId) => {
        setDeleteDialogOpen(true);
        setDeleteItemId(itemId);
    };

    const confirmDelete = async () => {
        const itemId = deleteItemId;
        setDeleteDialogOpen(false);
        setDeleteItemId(null);
        const token = localStorage.getItem('token');
        const res = await fetch(`/api/clothing/${itemId}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: token ? { Authorization: `Bearer ${token}` } : {},
        });
        if (res.ok) {
            setClothes(prev => prev.filter(item => item._id !== itemId));
        } else {
            const error = await res.json();
            alert(error.message || 'Failed to delete item');
            console.error('Delete error:', error);
        }
    };

    const cancelDelete = () => {
        setDeleteDialogOpen(false);
        setDeleteItemId(null);
    };

    if (!user) return null;

    // Filter items for the current user
    const userClothes = clothes.filter(item => {
        const itemUserId = typeof item.user === 'object' && item.user !== null
            ? item.user._id
            : item.user;
        return itemUserId && user._id && String(itemUserId) === String(user._id);
    });

    // Category filter logic
    const activeCategories = Object.entries(categoryFilters)
        .filter(([_, val]) => val)
        .map(([key]) => key);

    // Filtering logic
    const filteredClothes = userClothes.filter(item => {
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
        const matchesWantToGive =
            !showWantToGiveOnly || item.wantToGive === true;
        return matchesCategory && matchesColor && matchesSize && matchesSeason && matchesWantToGive;
    });

    // Handler for color filter toggle
    const toggleColor = (color) => {
        setColorFilters(prev =>
            prev.includes(color)
                ? prev.filter(c => c !== color)
                : [...prev, color]
        );
    };

    // Handler for "All" colors (reset)
    const clearColorFilters = () => setColorFilters([]);

    // Handler for size filter toggle
    const toggleSize = (size) => {
        setSizeFilters(prev =>
            prev.includes(size)
                ? prev.filter(s => s !== size)
                : [...prev, size]
        );
    };

    // Handler for "All" sizes (reset)
    const clearSizeFilters = () => setSizeFilters([]);

    // Handler for season filter toggle
    const toggleSeason = (season) => {
        setSeasonFilters(prev =>
            prev.includes(season)
                ? prev.filter(s => s !== season)
                : [...prev, season]
        );
    };

    // Handler for "All" seasons (reset)
    const clearSeasonFilters = () => setSeasonFilters([]);

    // Sidebar open handler
    const openSidebar = (item) => {
        setSidebarItem(item);
        setSidebarOpen(true);
    };

    // Sidebar close handler
    const closeSidebar = () => {
        setSidebarOpen(false);
        setSidebarItem(null);
    };

    return (
        <div style={{ position: 'relative' }}>
            {/* Delete Confirmation Dialog */}
            {deleteDialogOpen && (
                <div style={{
                    position: 'fixed',
                    top: 0,
                    left: 0,
                    width: '100vw',
                    height: '100vh',
                    background: 'rgba(0,0,0,0.25)',
                    zIndex: 3000,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    <div style={{
                        background: '#fff',
                        borderRadius: '12px',
                        boxShadow: '0 4px 24px rgba(25, 118, 210, 0.18)',
                        padding: '32px 28px 24px 28px',
                        minWidth: 320,
                        maxWidth: '90vw',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center'
                    }}>
                        <div style={{ fontSize: 32, color: '#d32f2f', marginBottom: 10 }}>
                            <MdDelete size={36} />
                        </div>
                        <div style={{ fontWeight: 700, fontSize: '1.2em', marginBottom: 10, color: '#222', textAlign: 'center' }}>
                            Delete this item?
                        </div>
                        <div style={{ color: '#555', marginBottom: 22, textAlign: 'center', fontSize: '1em' }}>
                            Are you sure you want to delete this clothing item? This action cannot be undone.
                        </div>
                        <div style={{ display: 'flex', gap: 16 }}>
                            <button
                                onClick={cancelDelete}
                                style={{
                                    padding: '8px 22px',
                                    borderRadius: 8,
                                    border: '1.5px solid #aaa',
                                    background: '#fff',
                                    color: '#333',
                                    fontWeight: 600,
                                    fontSize: '1em',
                                    cursor: 'pointer',
                                    transition: 'border 0.15s'
                                }}
                            >
                                Cancel
                            </button>
                            <button
                                onClick={confirmDelete}
                                style={{
                                    padding: '8px 22px',
                                    borderRadius: 8,
                                    border: '1.5px solid #d32f2f',
                                    background: '#d32f2f',
                                    color: '#fff',
                                    fontWeight: 600,
                                    fontSize: '1em',
                                    cursor: 'pointer',
                                    transition: 'background 0.15s'
                                }}
                            >
                                Delete
                            </button>
                        </div>
                    </div>
                </div>
            )}
            {/* Sidebar */}
            {sidebarOpen && sidebarItem && (
                <div
                    style={{
                        position: 'fixed',
                        top: 0,
                        right: 0,
                        width: '370px',
                        height: '100vh',
                        background: '#fafdff',
                        boxShadow: '-4px 0 24px rgba(25, 118, 210, 0.13)',
                        zIndex: 2000,
                        padding: '32px 28px 24px 28px',
                        overflowY: 'auto',
                        transition: 'right 0.2s',
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center'
                    }}
                >
                    <button
                        onClick={closeSidebar}
                        style={{
                            position: 'absolute',
                            top: 18,
                            right: 18,
                            background: 'none',
                            border: 'none',
                            fontSize: '2em',
                            color: '#1976d2',
                            cursor: 'pointer'
                        }}
                        aria-label="Close"
                    >
                        ×
                    </button>
                    <img
                        src={sidebarItem.imageUrl?.startsWith('/images/') ? sidebarItem.imageUrl : `/images/${sidebarItem.imageUrl}`}
                        alt={sidebarItem.name}
                        style={{
                            width: '240px',
                            height: '240px',
                            objectFit: 'cover',
                            borderRadius: '14px',
                            boxShadow: '0 2px 12px rgba(25, 118, 210, 0.10)',
                            marginBottom: '18px',
                            marginTop: '18px',
                            background: '#f7f7f7'
                        }}
                    />
                    <h2 style={{
                        fontSize: '1.6em',
                        fontWeight: 700,
                        color: '#1976d2',
                        marginBottom: 10,
                        textAlign: 'center'
                    }}>{sidebarItem.name}</h2>
                    <div style={{ width: '100%', marginBottom: 10 }}>
                        <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Category:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.category} {sidebarItem.subCategory ? `- ${sidebarItem.subCategory}` : ''}</div>
                        <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Size:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.size}</div>
                        <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Color:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.color}</div>
                        <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Season:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarItem.season) ? sidebarItem.season.join(', ') : sidebarItem.season}</div>
                        {sidebarItem.notes && (
                            <>
                                <div style={{ color: '#1976d2', fontWeight: 500, marginBottom: 4 }}>Notes:</div>
                                <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.notes}</div>
                            </>
                        )}
                    </div>
                </div>
            )}
            {/* Overlay for sidebar */}
            {sidebarOpen && (
                <div
                    onClick={closeSidebar}
                    style={{
                        position: 'fixed',
                        top: 0,
                        left: 0,
                        width: '100vw',
                        height: '100vh',
                        background: 'rgba(0,0,0,0.18)',
                        zIndex: 1500
                    }}
                />
            )}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                gap: '18px',
                margin: '32px 0 18px 0'
            }}>
                <MdOutlineCheckroom size={44} style={{ color: "#1976d2" }} />
                <h2 style={{
                    margin: 0,
                    fontSize: '2.5rem',
                    fontWeight: 800,
                    color: '#1976d2',
                    letterSpacing: '2px',
                    fontFamily: "'Montserrat', 'Segoe UI', Arial, sans-serif",
                    textShadow: '0 2px 8px #e3eafc'
                }}>
                    My Closet
                </h2>
            </div>
            {/* Category Filters */}
            <div style={{ display: 'flex', gap: '10px', marginBottom: '1rem', flexWrap: 'wrap', justifyContent: 'center' }}>
                {categories.map(category => (
                    <button
                        key={category}
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
            {/* Want To Give Filter */}
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem', gap: '14px' }}>
                <button
                    onClick={() => setShowWantToGiveOnly(false)}
                    style={{
                        padding: '8px 18px',
                        borderRadius: '8px',
                        border: !showWantToGiveOnly ? '2px solid #1976d2' : '1px solid #ccc',
                        background: !showWantToGiveOnly ? '#e3eafc' : '#fff',
                        color: !showWantToGiveOnly ? '#1976d2' : '#333',
                        fontWeight: 600,
                        fontSize: '1.1em',
                        cursor: 'pointer',
                        boxShadow: !showWantToGiveOnly ? '0 2px 8px #e3eafc' : 'none',
                        transition: 'border 0.15s'
                    }}
                    aria-pressed={!showWantToGiveOnly}
                >
                    All
                </button>
                <button
                    onClick={() => setShowWantToGiveOnly(true)}
                    style={{
                        padding: '8px 18px',
                        borderRadius: '8px',
                        border: showWantToGiveOnly ? '2px solid #1976d2' : '1px solid #ccc',
                        background: showWantToGiveOnly ? '#e3eafc' : '#fff',
                        color: showWantToGiveOnly ? '#1976d2' : '#333',
                        fontWeight: 600,
                        fontSize: '1.1em',
                        cursor: 'pointer',
                        boxShadow: showWantToGiveOnly ? '0 2px 8px #e3eafc' : 'none',
                        transition: 'border 0.15s'
                    }}
                    aria-pressed={showWantToGiveOnly}
                >
                    Want To Give
                </button>
            </div>
            <div className='clothes-grid'>
                {filteredClothes.length === 0 ? (
                    <div style={{ textAlign: 'center', width: '100%', marginTop: 40 }}>
                        No clothing items found.
                    </div>
                ) : (
                    filteredClothes.map(clothingItem => (
                        <div key={clothingItem._id} className="clothingItem-card" style={{ position: 'relative' }}>
                            <div
                                className="clothingItem-card-img-bracket"
                                style={{ cursor: 'pointer' }}
                                onClick={() => openSidebar(clothingItem)}
                            >
                                <img
                                    src={
                                        clothingItem.imageUrl?.startsWith('/images/')
                                            ? clothingItem.imageUrl
                                            : `/images/${clothingItem.imageUrl}`
                                    }
                                    alt={clothingItem.name}
                                />
                            </div>
                            <button
                                style={{
                                    position: 'absolute',
                                    top: 8,
                                    right: 8,
                                    background: clothingItem.wantToGive ? '#1976d2' : '#fff',
                                    color: clothingItem.wantToGive ? '#fff' : '#1976d2',
                                    border: '2px solid #1976d2',
                                    borderRadius: 6,
                                    padding: '4px 10px',
                                    cursor: 'pointer',
                                    fontWeight: 500,
                                    transition: 'background 0.15s, color 0.15s'
                                }}
                                onClick={() => handleWantToGive(clothingItem._id, clothingItem.wantToGive)}
                            >
                                {clothingItem.wantToGive ? "✓ Give" : "Give"}
                            </button>
                            <button
                                style={{
                                    position: 'absolute',
                                    top: 8,
                                    left: 8,
                                    background: '#fff',
                                    color: '#d32f2f',
                                    border: '2px solid #d32f2f',
                                    borderRadius: 6,
                                    padding: '4px 10px',
                                    cursor: 'pointer',
                                    fontWeight: 500,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    transition: 'background 0.15s, color 0.15s'
                                }}
                                onClick={() => handleDeleteItem(clothingItem._id)}
                                title="Delete"
                            >
                                <MdDelete size={20} />
                            </button>
                        </div>
                    ))
                )}
            </div>
            {/* Floating Add Clothing Item Button - always visible */}
            <button
                className="add-clothing-fab"
                onClick={() => navigate('/addClothingItem')}
                aria-label="Add Clothing Item"
            >
                +
            </button>
        </div>
    );
}

export default ClothingItems;