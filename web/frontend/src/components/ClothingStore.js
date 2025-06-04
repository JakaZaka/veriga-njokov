import { useNavigate, useParams } from 'react-router-dom';
import { useContext, useEffect, useState } from 'react';
import { UserContext } from '../userContext';
import '../FormAndStoreCard.css';



function ClothingStore(props){
    const userContext = useContext(UserContext); 
    const navigate = useNavigate();
    
    const [clothingStore, setClothingStore] = useState({});

    /*function handleClick() {
        navigate(`/clothingItem/${props.clothingItem._id}`);
    }*/


    

    return (
        <div className="clothingStore-card" /*onClick={handleClick}*/ style={{ cursor: 'pointer' }}>
            <h4>{props.clothingStore.clothingStoreId.name}</h4>
            <h5>{props.clothingStore.clothingStoreId.website}</h5>
            <h5>{props.clothingStore.address}</h5>
            <h5>{props.clothingStore.city}</h5>
            <h5>{props.clothingStore.country}</h5>
        </div>

    );
}

export default ClothingStore;
