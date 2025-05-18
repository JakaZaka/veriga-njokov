import { useState, useEffect } from 'react';
import ClothingItem from './ClothingIntem';
import { useLocation } from 'react-router-dom';

function Clothes(){
    const location = useLocation();
    const [clothes, setClothes] = useState([]);

    const getClothes = async function() {
       
        
        const res = await fetch('/api/clothing', {
            method: 'GET',
            credentials: 'include'
        });
        const data = await res.json();
        setClothes(data);
    };

    
    useEffect(() => {
        getClothes();

        
        const interval = setInterval(() => {
            getClothes();
        }, 1000 * 60 * 5);  

        
        return () => clearInterval(interval);
    }, [location.pathname]);

    function handleDelete(id) {
        setClothes(prev => prev.filter(clothingItem => clothingItem._id !== id));
    }

    return(
        <div>
            <h3>Clothes:</h3>
            
            <ul>
                <div className='clothes-grid'>{clothes.map(clothingItem=>(<ClothingItem clothingItem={clothingItem} key={clothingItem._id} onDelete={handleDelete}></ClothingItem>))}</div>
            </ul>
        </div>
    );
}

export default Clothes;