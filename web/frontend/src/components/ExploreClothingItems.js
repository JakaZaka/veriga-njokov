import { useState, useEffect, useContext } from 'react';
import ClothingItem from './ClothingItem';
import { useLocation } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../ClothingGrid.css';

function ExploreClothingItems() {
    const location = useLocation();
    const [clothes, setClothes] = useState([]);
    const { user } = useContext(UserContext);

    useEffect(() => {
        async function getClothes() {
            const res = await fetch('/api/clothing', {
                method: 'GET',
                credentials: 'include'
            });
            const data = await res.json();
            // Filter out current user's clothes robustly
            const filtered = user
                ? data.filter(item => {
                    const itemUserId = typeof item.user === 'object' && item.user !== null
                        ? item.user._id
                        : item.user;
                    return itemUserId && user._id && String(itemUserId) !== String(user._id);
                })
                : data;
            setClothes(filtered);
        }
        getClothes();

        const interval = setInterval(getClothes, 1000 * 60 * 5);
        return () => clearInterval(interval);
    }, [location.pathname, user]);

    return (
        <div>
            <h3>Explore Clothes:</h3>
            <div className='clothes-grid'>
                {clothes.map(clothingItem => (
                    <ClothingItem clothingItem={clothingItem} key={clothingItem._id} />
                ))}
            </div>
        </div>
    );
}

export default ExploreClothingItems;