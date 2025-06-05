import { useContext, useState, useEffect } from 'react'
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../FormAndStoreCard.css';

function AddClothingItem(props) {
    const userContext = useContext(UserContext); 
    const [name, setName] = useState('');
    const [category, setCategory] = useState('');
    const [subCategory, setSubCategory] = useState('');
    const [color, setColor] = useState('');
    const [size, setSize] = useState('');
    const [season, setSeason] = useState('');
    const [file, setFile] = useState('');
    const [uploaded, setUploaded] = useState(false);
    const [seasons, setSeasons] = useState([]);
    const [categories, setCategories] = useState([]);
    const [notes, setNotes] = useState('');

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
        await res.json();

        setUploaded(true);
    }

    return (
        <div className="form-card-container">
            <div className="form-card">
                <h2>Add Clothing Item</h2>
                {uploaded ? <Navigate replace to="/" /> : ""}
                <form onSubmit={onSubmit}>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="name">Clothing item name</label>
                        <input type="text" className="form-control" name="name" id="name" placeholder="Clothing item name" value={name} onChange={(e)=>{setName(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Choose a category:</label>
                        {categories.map(cat => (
                            <div key={cat} className="form-check">
                                <input type="radio" className="form-check-input" name="category" value={cat} checked={category === cat} onChange={(e) => setCategory(e.target.value)} id={`cat-${cat}`} />
                                <label className="form-check-label" htmlFor={`cat-${cat}`}>{cat}</label>
                            </div>
                        ))}
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="subCategory">Sub category</label>
                        <input type="text" className="form-control" name="subCategory" id="subCategory" placeholder="Sub category" value={subCategory} onChange={(e)=>{setSubCategory(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="color">Color</label>
                        <input type="text" className="form-control" name="color" id="color" placeholder="Clothing item color" value={color} onChange={(e)=>{setColor(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="size">Size</label>
                        <input type="text" className="form-control" name="size" id="size" placeholder="Clothing item size" value={size} onChange={(e)=>{setSize(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="notes">Notes</label>
                        <input type="text" className="form-control" name="notes" id="notes" placeholder="Notes" value={notes} onChange={(e)=>{setNotes(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Choose a season:</label>
                        {seasons.map(seasonOption => (
                            <div key={seasonOption} className="form-check">
                                <input type="radio" className="form-check-input" name="season" value={seasonOption} checked={season === seasonOption} onChange={(e) => setSeason(e.target.value)} id={`season-${seasonOption}`} />
                                <label className="form-check-label" htmlFor={`season-${seasonOption}`}>{seasonOption}</label>
                            </div>
                        ))}
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="file">Choose photo</label>
                        <input type="file" className="form-control" id="file" onChange={(e)=>{setFile(e.target.files[0])}}/>
                    </div>
                    <button className="btn btn-primary w-100" type="submit">Naloži</button>
                </form>
            </div>
        </div>
    )
}

export default AddClothingItem;