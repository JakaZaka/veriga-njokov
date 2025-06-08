import { useState, useEffect, useContext } from 'react';
import ClothingItem from './ClothingItem';
import { useLocation } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../ClothingGrid.css';

function ClothingItems() {
    const location = useLocation();
    const { user } = useContext(UserContext);
    const [clothes, setClothes] = useState([]);

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
    if (!user) return null;

    return (
        <div>
            <h3>My Closet:</h3>
            <div className='clothes-grid'>
                {clothes
                    .filter(item => {
                        // item.user can be an object or an id string
                        const itemUserId = typeof item.user === 'object' && item.user !== null
                            ? item.user._id
                            : item.user;
                        return itemUserId && user._id && String(itemUserId) === String(user._id);
                    })
                    .map(clothingItem => (
                        <div key={clothingItem._id} style={{ position: 'relative' }}>
                            <ClothingItem clothingItem={clothingItem} />
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
                                {clothingItem.wantToGive ? "✓ Want To Give" : "Want To Give"}
                            </button>
                        </div>
                    ))}
            </div>
        </div>
    );
}

export default ClothingItems;