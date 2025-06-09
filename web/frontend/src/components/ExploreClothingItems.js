import { useState, useEffect, useContext } from 'react';
import ClothingItem from './ClothingItem';
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

    // Filtering by selected categories
    const activeCategories = Object.entries(categoryFilters)
        .filter(([_, val]) => val)
        .map(([key]) => key);

    const filteredClothes = activeCategories.length === 0
        ? clothes
        : clothes.filter(item =>
            item.category && activeCategories.includes(item.category.toLowerCase())
        );

    return (
        <div>
            <div style={{ display: 'flex', gap: '10px', marginBottom: '1rem', flexWrap: 'wrap' }}>
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
                        border: categoryFilters[category] ? '2px solid #333' : '1px solid #ccc',
                        backgroundColor: categoryFilters[category] ? '#eee' : '#fff',
                        cursor: 'pointer',
                        fontSize: '18px',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '4px'
                    }}
                    aria-pressed={categoryFilters[category]}
                    >
                    <span>{clothingIcons[category]}</span>
                    <span>{category.charAt(0).toUpperCase() + category.slice(1)}</span>
                    </button>
            ))}
            </div>
            <h3>Explore Clothes:</h3>
            <div className='clothes-grid'>
                {filteredClothes.map(clothingItem => {
                    const wantToGet = clothingItem.wantToGet || [];
                    const alreadyWants = user && wantToGet.map(String).includes(String(user._id));
                    return (
                        <div key={clothingItem._id} style={{ position: 'relative' }}>
                            <ClothingItem clothingItem={clothingItem} />
                            {user && (
                                <button
                                    style={{
                                        background: alreadyWants ? '#1976d2' : '#fff',
                                        color: alreadyWants ? '#fff' : '#1976d2',
                                        border: '2px solid #1976d2',
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
                })}
            </div>
        </div>
    );
}

export default ExploreClothingItems;