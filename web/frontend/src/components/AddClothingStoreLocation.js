import { useContext, useState } from 'react'
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import { use } from 'react';
import { useEffect } from 'react';
import AddClothingStore from './AddClothingStore';
import '../FormAndStoreCard.css';

function AddClothingStoreLocation(props) {
    const userContext = useContext(UserContext); 
    const[address, setAddress] = useState('');
    const[city, setCity] = useState('');
    const[country, setCountry] = useState('');
    const[store, setStore] = useState('');
    const[uploaded, setUploaded] = useState(false);
    const[stores, setStores] = useState([]);
    
    useEffect(() => {
        async function fetchStores() {
            const res = await fetch('/api/stores/existing', {
                method: 'GET',
                credentials: 'include'
            });
            const data = await res.json();
            setStores(data);
        }
        fetchStores();
    }, []);

    async function onSubmit(e){
        e.preventDefault();

        if(!address){
            alert("Add the adress!");
            return;
        }
        if(!city){
            alert("Add the city!");
            return;
        }
        if(!country){
            alert("Add the country!");
            return;
        }
        if(!store){
            alert("Choose the store!");
            return;
        }
        


        const body = {
            address: address,
            city: city,
            country: country,
            clothingStoreId: store
        };

        const res = await fetch('/api/locations', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(body)
        });

        const data = await res.json();
        setUploaded(true);
    }

    return (
    <div className="form-card-container">
        <div className="form-card">
        <h2>Add Store Location</h2>
        <form onSubmit={onSubmit}>
            <div className="mb-3">
            <label>Store:</label>
            <select name="clothingStoreId" className="form-control" value={store} onChange={(e) => setStore(e.target.value)} required>
                <option value="">Select Store</option>
                {stores.map(store => (
                <option key={store._id} value={store._id}>{store.name}</option>
                ))}
            </select>
            </div>
            <div className="mb-3">
            <input type="text" className="form-control" name="address" placeholder="Address" value={address} onChange={(e)=>{setAddress(e.target.value)}}/>
            </div>
            <div className="mb-3">
            <input type="text" className="form-control" name="city" placeholder="City" value={city} onChange={(e)=>{setCity(e.target.value)}}/>
            </div>
            <div className="mb-3">
            <input type="text" className="form-control" name="country" placeholder="Country" value={country} onChange={(e)=>{setCountry(e.target.value)}}/>
            </div>
            <button className="btn btn-primary w-100" type="submit">Naloži</button>
        </form>
        </div>
    </div>
    )
}

export default AddClothingStoreLocation;