import { useContext, useState } from 'react'
import { Navigate } from 'react-router';
import { UserContext } from '../userContext';
import { use } from 'react';
import { useEffect } from 'react';

function AddClothingStore(props) {
    const userContext = useContext(UserContext); 
    const[name, setName] = useState('');
    const[website, setWebsite] = useState('');
    const[uploaded, setUploaded] = useState(false);

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
        

        const res = await fetch('http://localhost:8000/stores', {
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
        <form className="form-group" onSubmit={onSubmit}>
           
            {uploaded ? <Navigate replace to="/" /> : ""}
            <input type="text" className="form-control" name="name" placeholder="Clothing item name" value={name} onChange={(e)=>{setName(e.target.value)}}/>
            <input type="text" className="form-control" name="website" placeholder="Store website" value={website} onChange={(e)=>{setWebsite(e.target.value)}}/>
            <input className="btn btn-primary" type="submit" name="submit" value="Naloži" />
        </form>
    )
}

export default AddClothingStore;