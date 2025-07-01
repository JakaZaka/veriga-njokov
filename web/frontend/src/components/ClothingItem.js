import { useNavigate } from 'react-router-dom';
import { useContext } from 'react';
import { UserContext } from '../userContext';

function ClothingItem(props){
    const navigate = useNavigate();

    function handleClick() {
        navigate(`/clothingItem/${props.clothingItem._id}`);
    }

    return (
        <div className="clothingItem-card" onClick={handleClick} style={{ cursor: 'pointer' }}>
            <img
                className="clothingItem-card-img"
                src={`${props.clothingItem.imageUrl}`}
                alt={props.clothingItem.name}
            />
        </div>
    );
}

export default ClothingItem;