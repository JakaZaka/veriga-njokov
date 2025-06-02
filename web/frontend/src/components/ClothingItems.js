import { useState, useEffect } from 'react';
import ClothingItem from './ClothingItem';
import { useLocation } from 'react-router-dom';

function ClothingItems() {
    const location = useLocation();
    const [clothes, setClothes] = useState([]);

    useEffect(() => {
        async function getClothes() {
            const res = await fetch('/api/clothing?mine=true', {
                method: 'GET',
                credentials: 'include'
            });
            const data = await res.json();
            setClothes(data);
        }
        getClothes();

        const interval = setInterval(getClothes, 1000 * 60 * 5);
        return () => clearInterval(interval);
    }, [location.pathname]);

    return (
        <div>
            <h3>Your Clothes:</h3>
            <div className='clothes-grid'>
                {clothes.map(clothingItem => (
                    <ClothingItem clothingItem={clothingItem} key={clothingItem._id} />
                ))}
            </div>
        </div>
    );
}

export default ClothingItems;