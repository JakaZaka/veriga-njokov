import { useNavigate, useParams } from 'react-router-dom';
import { useContext, useEffect, useState } from 'react';
import { UserContext } from '../userContext';


function ClothingItem(props){
    const userContext = useContext(UserContext); 
    const navigate = useNavigate();
    
    const [clothingItem, setClothingItem] = useState({});

    function handleClick() {
        navigate(`/clothingItem/${props.clothingItem._id}`);
    }


    

    return (
        <div className="clothingItem-card" onClick={handleClick} style={{ cursor: 'pointer' }}>
          <img
            className="clothingItem-card-img"
            src={`/api/${props.clothingItem.imageUrl}`}
            alt={props.clothingItem.name}
            style={{ width: '150px', height: '150px', objectFit: 'cover' }}
          />
          <div className="clothingItem-card-overlay">
            <h5>{props.clothingItem.name}</h5>
            <h5>{props.clothingItem.category}</h5>
            <h5>{props.clothingItem.subCategory}</h5>
            <h5>{props.clothingItem.season}</h5>
            <h5>{props.clothingItem.size}</h5>

            
           
          </div>
</div>

    );
}

export default ClothingItem;
