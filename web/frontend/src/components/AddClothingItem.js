import { useContext, useState } from 'react'
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import { use } from 'react';
import { useEffect } from 'react';

function AddClothingItem(props) {
    const userContext = useContext(UserContext); 
    const[name, setName] = useState('');
    const[category, setCategory] = useState('');
    const[subCategory, setSubCategory] = useState('');
    const[color, setColor] = useState('');
    const[size, setSize] = useState('');
    const[season, setSeason] = useState('');
    const[file, setFile] = useState('');
    const[uploaded, setUploaded] = useState(false);
    const[seasons, setSeasons] = useState([]);
    const[categories, setCategories] = useState([]);
    const[notes, setNotes] = useState('');

    useEffect(() => {
        async function fetchEnums() {
            const res = await fetch('/api/enums/clothing', {
                method: 'GET',
                credentials: 'include'
            });
            const data = await res.json();
            setSeasons(data.season);
            setCategories(data.category);
        }
        fetchEnums();
    }, []);

    async function onSubmit(e){
        e.preventDefault();

        if(!name){
            alert("Add the name!");
            return;
        }
        if(!category){
            alert("Choose the category!");
            return;
        }
        if(!subCategory){
            alert("Choose the sub-category!");
            return;
        }
        if(!color){
            alert("Add the color!");
            return;
        }
        if(!size){
            alert("Add the size!");
            return;
        }
        if(!season){
            alert("Choose the season!");
            return;
        }


        const formData = new FormData();
        formData.append('name', name);
        formData.append('category', category);
        formData.append('subCategory', subCategory);
        formData.append('color', color);
        formData.append('size', size);
        formData.append('season', season);
        formData.append('image', file);
        formData.append('notes', notes);

        const res = await fetch('/api/clothing', {
            method: 'POST',
            credentials: 'include',
            body: formData
        });
        const data = await res.json();

        setUploaded(true);
    }

    return (
        <form className="form-group" onSubmit={onSubmit}>
           
            {uploaded ? <Navigate replace to="/" /> : ""}
            <input type="text" className="form-control" name="name" placeholder="Clothing item name" value={name} onChange={(e)=>{setName(e.target.value)}}/>
            <label>Choose a category:</label>
             {categories.map(cat => (
            <div key={cat}>
                <input type="radio" name="category" value={cat} checked={category === cat} onChange={(e) => setCategory(e.target.value)} />
            <label>{cat}</label>
            </div>
            ))}
            <input type="text" className="form-control" name="subCategory" placeholder="Sub category" value={subCategory} onChange={(e)=>{setSubCategory(e.target.value)}}/>
            <input type="text" className="form-control" name="color" placeholder="Clothing item color" value={color} onChange={(e)=>{setColor(e.target.value)}}/>
            <input type="text" className="form-control" name="size" placeholder="Clothing item size" value={size} onChange={(e)=>{setSize(e.target.value)}}/>
             <input type="text" className="form-control" name="notes" placeholder="Notes" value={notes} onChange={(e)=>{setNotes(e.target.value)}}/>
            <label>Choose a season:</label>
             {seasons.map(seasonOption => (
                <div key={seasonOption}>
                <input type="radio" name="season" value={seasonOption} checked={season === seasonOption} onChange={(e) => setSeason(e.target.value)} />
            <label>{seasonOption}</label>
            </div>
            ))}
            <label>Choose photo</label>
            <input type="file" id="file" onChange={(e)=>{setFile(e.target.files[0])}}/>
            <input className="btn btn-primary" type="submit" name="submit" value="Naloži" />
        </form>
    )
}

export default AddClothingItem;