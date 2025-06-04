import { useContext, useEffect, useState } from 'react';
import { UserContext } from '../userContext';
import { Navigate, useParams } from 'react-router-dom';
import '../DetailsCard.css';

function ClothingItemInfo(){
    const userContext = useContext(UserContext); 
    const { id } = useParams();
    const [clothingItem, setClothingItem] = useState({});
    const [redirectToLogin, setRedirectToLogin] = useState(false);

    useEffect(function(){
        const getClothingItemById = async function(){
            const token = localStorage.getItem('token');
            const res = await fetch(`/api/clothing/${id}`, {
                credentials: 'include',
                headers: token ? { Authorization: `Bearer ${token}` } : {},
            });
            const data = await res.json();
            setClothingItem(data);
        }
        getClothingItemById();
    }, [id]);

    if (redirectToLogin) {
        return <Navigate replace to="/login" />;
    }

    return (
        <div className="details-card">
            <div className="details-title">{clothingItem.name}</div>
            <img
                src={`${clothingItem.imageUrl}`}
                alt={clothingItem.name}
                className="details-img-main"
            />
            <div className="details-section">
                <div className="details-meta"><span className="details-label">Season:</span> <span className="details-value">{clothingItem.season?.join(', ')}</span></div>
                <div className="details-meta"><span className="details-label">Category:</span> <span className="details-value">{clothingItem.category}</span></div>
                <div className="details-meta"><span className="details-label">Sub-category:</span> <span className="details-value">{clothingItem.subCategory}</span></div>
                <div className="details-meta"><span className="details-label">Size:</span> <span className="details-value">{clothingItem.size}</span></div>
                <div className="details-meta"><span className="details-label">Color:</span> <span className="details-value">{clothingItem.color}</span></div>
                <div className="details-meta"><span className="details-label">Notes:</span> <span className="details-value">{clothingItem.notes}</span></div>
            </div>
        </div>
    );
}

export default ClothingItemInfo;