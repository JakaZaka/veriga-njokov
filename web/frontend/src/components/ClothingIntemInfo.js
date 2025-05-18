import { useContext, useEffect, useState } from 'react';
import { UserContext } from '../userContext';
import { Navigate, useParams } from 'react-router-dom';

//import Comment from './Comment';

function ClothingItemInfo(){
    const userContext = useContext(UserContext); 
    const { id } = useParams();
    const [clothingItem, setClothingItem] = useState({});
   
    useEffect(function(){
        const getClothingItemById = async function(){
            const res = await fetch(`http://localhost:8000/clothing/${id}`);
            const data = await res.json();
            setClothingItem(data);
        }
        getClothingItemById();
    }, [id]);

    


    
  
    const [clothingItemId, setClothingItemID] = useState(id);
    const [uploaded, setUploaded] = useState(false);
    const [redirectToLogin, setRedirectToLogin] = useState(false); // track if we need to redirect

   

    if (redirectToLogin) {
        return <Navigate replace to="/login" />; // redirect to login page only when trying to submit a comment
    }

   




    return (
        <>
            
            <div className="clothingItem-detail-container">
  <h1 className="Clothing-title">{clothingItem.name}</h1>

  <img
    src={`http://localhost:8000/${clothingItem.imageUrl}`}
    style={{ width: '300px', height: '300px', objectFit: 'cover' }}
    alt={clothingItem.name}
    className="photo-detail-img"
  />

  <div className="clothingItem-meta">
    <p><strong>Season:</strong> {clothingItem.season}</p>
    <p><strong>Category:</strong> {clothingItem.category}</p>
    <p><strong>Sub-category:</strong> {clothingItem.subCategory}</p>
    <p><strong>Size:</strong> {clothingItem.size}</p>
    <p><strong>Color:</strong> {clothingItem.color}</p>
    <p><strong>Notes:</strong> {clothingItem.notes}</p>
    <p><strong>Created by:</strong> {clothingItem.user ? clothingItem.user.name : 'Unknown'}</p>
  </div>

  
</div>

        </>
    );
}

export default ClothingItemInfo;