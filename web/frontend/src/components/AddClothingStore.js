import { useContext, useState, use, useEffect } from 'react'
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../FormAndStoreCard.css';

function AddClothingStore(props) {
    const userContext = useContext(UserContext); 
    const [name, setName] = useState('');
    const [website, setWebsite] = useState('');
    const [uploaded, setUploaded] = useState(false);

    async function onSubmit(e){
        e.preventDefault();

        if(!name){
            alert("Add the name!");
            return;
        }
        if(!website){
            alert("Add the website!");
            return;
        }


        const formData = new FormData();
        formData.append('name', name);
        formData.append('website', website);
        

        const res = await fetch('/api/stores', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name,
                website
            })
        });
        const data = await res.json();

        setUploaded(true);
    }

    return (
        <div className="form-card-container">
            <div className="form-card">
                <h2>Add Clothing Store</h2>
                {uploaded ? <Navigate replace to="/" /> : ""}
                <form onSubmit={onSubmit}>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="name">Clothing store name</label>
                        <input
                            type="text"
                            className="form-control"
                            id="name"
                            name="name"
                            placeholder="Clothing store name"
                            value={name}
                            onChange={(e)=>{setName(e.target.value)}}
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="website">Store website</label>
                        <input
                            type="text"
                            className="form-control"
                            id="website"
                            name="website"
                            placeholder="Store website"
                            value={website}
                            onChange={(e)=>{setWebsite(e.target.value)}}
                        />
                    </div>
                    <button className="btn btn-primary w-100" type="submit">Naloži</button>
                </form>
            </div>
        </div>
    )
}

export default AddClothingStore;