import { useState, useEffect, useContext } from 'react';
import { useLocation } from 'react-router-dom';
import { UserContext } from '../userContext';
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

function ExploreClothingItems() {
    const location = useLocation();
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
    const [sidebarItem, setSidebarItem] = useState(null);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [showWantToGetOnly, setShowWantToGetOnly] = useState(false);
    const { user } = useContext(UserContext);

    const handleWantToGet = async (itemId, alreadyWants) => {
        if (!user) {
            alert("You must be logged in to mark Want To Get.");
            return;
        }
        setClothes(prev =>
            prev.map(item => {
                if (item._id !== itemId) return item;
                let wantToGet = Array.isArray(item.wantToGet) ? [...item.wantToGet] : [];
                if (alreadyWants) {
                    wantToGet = wantToGet.filter(User => String(User) !== String(user._id));
                } else {
                    wantToGet.push(user._id);
                }
                return { ...item, wantToGet };
            })
        );

        const token = localStorage.getItem('token');
        const res = await fetch(`/api/clothing/${itemId}/wantToGet`, {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                ...(token ? { Authorization: `Bearer ${token}` } : {})
            },
            body: JSON.stringify({ want: !alreadyWants })
        });
        if (res.ok) {
            const updatedItem = await res.json();
            setClothes(prev =>
                prev.map(item => item._id === updatedItem._id ? updatedItem : item)
            );
        }
    };

    useEffect(() => {
        async function getClothes() {
            const res = await fetch('/api/clothing', {
                method: 'GET',
                credentials: 'include'
            });
            const data = await res.json();

            const filtered = data.filter(item => {
                const itemUserId = typeof item.user === 'object' && item.user !== null
                    ? item.user._id
                    : item.user;

                const isFromAnotherUser =
                    user &&
                    itemUserId &&
                    user._id &&
                    String(itemUserId) !== String(user._id) &&
                    item.wantToGive === true;

                const isFromShop = item.fromShop === true;

                // show if from another user wanting to give OR from shop
                return isFromAnotherUser || isFromShop;
            });

            setClothes(filtered);
        }
        getClothes();

        const interval = setInterval(getClothes, 1000 * 60 * 5);
        return () => clearInterval(interval);
    }, [location.pathname, user]);

    // Filtering logic
    const activeCategories = Object.entries(categoryFilters)
        .filter(([_, val]) => val)
        .map(([key]) => key);

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
        const matchesWantToGet =
            !showWantToGetOnly ||
            (user && Array.isArray(item.wantToGet) && item.wantToGet.map(String).includes(String(user._id)));
        return matchesCategory && matchesColor && matchesSize && matchesSeason && matchesWantToGet;
    });

    // Handlers for filters
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
    const toggleSeason = (season) => {
        setSeasonFilters(prev =>
            prev.includes(season)
                ? prev.filter(s => s !== season)
                : [...prev, season]
        );
    };
    const clearSeasonFilters = () => setSeasonFilters([]);

    // Sidebar handlers
    const openSidebar = (item) => {
        setSidebarItem(item);
        setSidebarOpen(true);
    };
    const closeSidebar = () => {
        setSidebarOpen(false);
        setSidebarItem(null);
    };

    return (
        <div style={{ position: 'relative' }}>
            {/* Sidebar */}
            {sidebarOpen && sidebarItem && (
                <div
                    style={{
                        position: 'fixed',
                        top: 0,
                        right: 0,
                        width: '370px',
                        height: '100vh',
                        background: '#fffaf6',
                        boxShadow: '-4px 0 24px rgba(34, 70, 34, 0.13)',
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
                            color: '#225622',
                            cursor: 'pointer'
                        }}
                        aria-label="Close"
                    >
                        ×
                    </button>
                    <img
                        src={
                            sidebarItem.imageUrl?.startsWith('http://') ||
                            sidebarItem.imageUrl?.startsWith('https://') ||
                            sidebarItem.imageUrl?.startsWith('/images/')
                                ? sidebarItem.imageUrl
                                : `/images/${sidebarItem.imageUrl}`
                        }
                        alt={sidebarItem.name}
                        style={{
                            width: '240px',
                            height: '240px',
                            objectFit: 'cover',
                            borderRadius: '14px',
                            boxShadow: '0 2px 12px rgba(34, 70, 34, 0.10)',
                            marginBottom: '18px',
                            marginTop: '18px',
                            background: '#f7f7f7'
                        }}
                    />
                    <h2 style={{
                        fontSize: '1.6em',
                        fontWeight: 700,
                        color: '#225622',
                        marginBottom: 10,
                        textAlign: 'center'
                    }}>{sidebarItem.name}</h2>
                    <div style={{ width: '100%', marginBottom: 10 }}>
                        <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Category:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.category} {sidebarItem.subCategory ? `- ${sidebarItem.subCategory}` : ''}</div>
                        <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Size:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.size}</div>
                        <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Color:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.color}</div>
                        <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Season:</div>
                        <div style={{ color: '#333', marginBottom: 8 }}>{Array.isArray(sidebarItem.season) ? sidebarItem.season.join(', ') : sidebarItem.season}</div>
                        {sidebarItem.notes && (
                            <>
                                <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Notes:</div>
                                <div style={{ color: '#333', marginBottom: 8 }}>{sidebarItem.notes}</div>
                            </>
                        )}
                        {sidebarItem.fromShop && (
                            <>
                                <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>From Shop:</div>
                                <div style={{ color: '#333', marginBottom: 8 }}>Yes</div>
                            </>
                        )}
                        {sidebarItem.clothingStoreId && (
                            <>
                                <div style={{ color: '#225622', fontWeight: 500, marginBottom: 4 }}>Shop Name:</div>
                                <div style={{ color: '#333', marginBottom: 8 }}>
                                    {sidebarItem.clothingStoreId.name || sidebarItem.clothingStoreId}
                                </div>
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
                <span style={{ fontSize: 44, color: "#225622" }}></span>
                <h2 style={{
                    margin: 0,
                    fontSize: '2.5rem',
                    fontWeight: 500,
                    color: '#225622',
                    letterSpacing: '2px',
                    fontFamily: "'Montserrat', 'Segoe UI', Arial, sans-serif",
                    textShadow: '0 2px 8px #ffe5b4'
                }}>
                    Explore
                </h2>
            </div>
            {/* Want To Get Filter */}
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: '1rem', gap: '14px' }}>
                <button
                    onClick={() => setShowWantToGetOnly(false)}
                    style={{
                        padding: '8px 18px',
                        borderRadius: '8px',
                        border: !showWantToGetOnly ? '2px solid #225622' : '1px solid #ccc',
                        background: !showWantToGetOnly ? '#ffe5b4' : '#fff',
                        color: !showWantToGetOnly ? '#225622' : '#333',
                        fontWeight: 500,
                        fontSize: '1.1em',
                        cursor: 'pointer',
                        boxShadow: !showWantToGetOnly ? '0 2px 8px #ffe5b4' : 'none',
                        transition: 'border 0.15s'
                    }}
                    aria-pressed={!showWantToGetOnly}
                >
                    All
                </button>
                <button
                    onClick={() => setShowWantToGetOnly(true)}
                    style={{
                        padding: '8px 18px',
                        borderRadius: '8px',
                        border: showWantToGetOnly ? '2px solid #225622' : '1px solid #ccc',
                        background: showWantToGetOnly ? '#ffe5b4' : '#fff',
                        color: showWantToGetOnly ? '#225622' : '#333',
                        fontWeight: 500,
                        fontSize: '1.1em',
                        cursor: 'pointer',
                        boxShadow: showWantToGetOnly ? '0 2px 8px #ffe5b4' : 'none',
                        transition: 'border 0.15s'
                    }}
                    aria-pressed={showWantToGetOnly}
                >
                    Want To Get
                </button>
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
                            border: categoryFilters[category] ? '2px solid #225622' : '1px solid #ccc',
                            backgroundColor: categoryFilters[category] ? '#ffe5b4' : '#fff',
                            color: categoryFilters[category] ? '#225622' : '#333',
                            fontWeight: 500,
                            fontSize: '18px',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            cursor: 'pointer',
                            boxShadow: categoryFilters[category] ? '0 2px 8px #ffe5b4' : 'none'
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
                            border: colorFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                            background: '#ffe5b4',
                            color: 'transparent',
                            cursor: 'pointer',
                            boxShadow: colorFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                            outline: colorFilters.length === 0 ? '2px solid #225622' : 'none',
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
                        color: '#225622',
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
                                border: colorFilters.includes(color) ? '2.5px solid #225622' : '1.5px solid #ccc',
                                background: color.toLowerCase(),
                                color: 'transparent',
                                cursor: 'pointer',
                                boxShadow: colorFilters.includes(color) ? '0 2px 8px #ffe5b4' : 'none',
                                outline: colorFilters.includes(color) ? '2px solid #225622' : 'none',
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
                            color: '#225622',
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
                            border: seasonFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                            background: '#ffe5b4',
                            color: 'transparent',
                            cursor: 'pointer',
                            boxShadow: seasonFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                            outline: seasonFilters.length === 0 ? '2px solid #225622' : 'none',
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
                        color: '#225622',
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
                                border: seasonFilters.includes(season.key) ? '2.5px solid #225622' : '1.5px solid #ccc',
                                background: '#fff',
                                color: '#222',
                                cursor: 'pointer',
                                boxShadow: seasonFilters.includes(season.key) ? '0 2px 8px #ffe5b4' : 'none',
                                outline: seasonFilters.includes(season.key) ? '2px solid #225622' : 'none',
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
                            color: '#225622',
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
                            border: sizeFilters.length === 0 ? '2.5px solid #225622' : '1.5px solid #ccc',
                            background: '#ffe5b4',
                            color: 'transparent',
                            cursor: 'pointer',
                            boxShadow: sizeFilters.length === 0 ? '0 2px 8px #ffe5b4' : 'none',
                            outline: sizeFilters.length === 0 ? '2px solid #225622' : 'none',
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
                        color: '#225622',
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
                                border: sizeFilters.includes(size) ? '2.5px solid #225622' : '1.5px solid #ccc',
                                background: sizeFilters.includes(size) ? '#ffe5b4' : '#fff',
                                color: '#333',
                                cursor: 'pointer',
                                boxShadow: sizeFilters.includes(size) ? '0 2px 8px #ffe5b4' : 'none',
                                outline: sizeFilters.includes(size) ? '2px solid #225622' : 'none',
                                marginBottom: 2,
                                fontWeight: 500,
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
            {/* Clothes grid */}
            <div className='clothes-grid'>
                {filteredClothes.length === 0 ? (
                    <div style={{ textAlign: 'center', width: '100%', marginTop: 40 }}>
                        No clothing items found.
                    </div>
                ) : (
                    filteredClothes.map(clothingItem => {
                        const wantToGet = clothingItem.wantToGet || [];
                        const alreadyWants = user && wantToGet.map(String).includes(String(user._id));
                        return (
                            <div key={clothingItem._id} className="clothingItem-card" style={{ position: 'relative' }}>
                                <div
                                    className="clothingItem-card-img-bracket"
                                    style={{ cursor: 'pointer' }}
                                    onClick={() => openSidebar(clothingItem)}
                                >
                                    <img
                                        src={
                                            clothingItem.imageUrl?.startsWith('http://') ||
                                            clothingItem.imageUrl?.startsWith('https://') ||
                                            clothingItem.imageUrl?.startsWith('/images/')
                                                ? clothingItem.imageUrl
                                                : `/images/${clothingItem.imageUrl}`
                                        }
                                        alt={clothingItem.name}
                                    />
                                </div>
                                {user && (
                                    <button
                                        style={{
                                            position: 'absolute',
                                            top: 8,
                                            right: 8,
                                            background: alreadyWants ? '#225622' : '#fff',
                                            color: alreadyWants ? '#fff' : '#225622',
                                            border: '2px solid #225622',
                                            borderRadius: 6,
                                            padding: '4px 10px',
                                            cursor: 'pointer',
                                            fontWeight: 500,
                                            transition: 'background 0.15s, color 0.15s'
                                        }}
                                        onClick={() => handleWantToGet(clothingItem._id, alreadyWants)}
                                    >
                                        {alreadyWants ? "✓ Want To Get" : "Want To Get"}
                                    </button>
                                )}
                            </div>
                        );
                    })
                )}
            </div>
        </div>
    );
}

export default ExploreClothingItems;