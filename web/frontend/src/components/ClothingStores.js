import { useState, useEffect } from 'react';
import ClothingStore from './ClothingStore';
import { useLocation } from 'react-router-dom';

function Stores(){
    const location = useLocation();
    const [stores, setStores] = useState([]);

    const getStores = async function() {
       
        
        const res = await fetch('http://localhost:8000/locations', {
            method: 'GET',
            credentials: 'include'
        });
       const data = await res.json();
if (Array.isArray(data)) {
  setStores(data);
} else {
  console.error("Expected an array but got:", data);
  setStores([]);
}
    };

    
    useEffect(() => {
        getStores();

        
        const interval = setInterval(() => {
            getStores();
        }, 1000 * 60 * 5);  

        
        return () => clearInterval(interval);
    }, []);

    function handleDelete(id) {
        setStores(prev => prev.filter(storeLocation => storeLocation._id !== id));
    }

    return(
        <div>
            <h3>Stores:</h3>
            
            <ul>
                <div className='stores-grid'>{stores.map(clothingStore=>(<ClothingStore clothingStore={clothingStore} key={clothingStore._id} onDelete={handleDelete}></ClothingStore>))}</div>
            </ul>
        </div>
    );
}

export default Stores;