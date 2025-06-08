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
    const basicColors = [
    "Black", "White", "Gray", "Red", "Blue", "Green", "Yellow", "Pink", "Purple", "Brown", "Beige", "Orange"
    ];
    
    const [colorOption, setColorOption] = useState(''); // for select
    const [customColor, setCustomColor] = useState(''); // for custom input
    const clothingIcons = {
        tops: "👕",
        bottoms: "👖",
        shoes: "👟",
        outerwear: "🧥",
        accessories: "🧢",
        dresses: "👗",
    };
    const mainCategories = ["tops", "bottoms", "outerwear", "shoes", "accessories", "dresses"];

    const seasonIcons = {
        spring: "🌸",
        summer: "☀️",
        fall: "🍂",
        winter: "❄️",
        all: "🌀"
    };

    const fixedSizes = [
    "XS", "S", "M", "L", "XL", "2XL",
    "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46"
    ];
    const [sizeOption, setSizeOption] = useState('');
    const [customSize, setCustomSize] = useState('');

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

        if(!season){
            alert("Choose the season!");
            return;
        }

        let finalColor = colorOption === 'Other' ? customColor : colorOption;
        if (!finalColor) {
            alert("Choose the color!");
            return;
        }

        let finalSize = sizeOption === 'Other' ? customSize : sizeOption;
        if (!finalSize) {
            alert("Choose the size!");
            return;
        }

        const formData = new FormData();
        formData.append('name', name);
        formData.append('category', category);
        formData.append('subCategory', subCategory);
        formData.append('size', finalSize);
        formData.append('season', season);
        formData.append('image', file);
        formData.append('notes', notes);
        formData.append('color', finalColor);

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
                        <label className="form-label" htmlFor="name">Name</label>
                        <input
                            type="text"
                            className="form-control"
                            name="name"
                            id="name"
                            placeholder="Item name"
                            value={name}
                            onChange={e => setName(e.target.value)}
                            required
                        />
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Choose a category:</label>
                        <div style={{ display: 'flex', gap: '16px', marginTop: '6px', flexWrap: 'wrap' }}>
                            {mainCategories.map(cat => (
                                <label
                                    key={cat}
                                    style={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        cursor: 'pointer'
                                    }}
                                >
                                    <input
                                        type="radio"
                                        className="form-check-input"
                                        name="category"
                                        value={cat}
                                        checked={category === cat}
                                        onChange={() => setCategory(cat)}
                                        style={{ display: 'none' }}
                                    />
                                    <span
                                        style={{
                                            fontSize: '2em',
                                            borderRadius: '8px',
                                            border: category === cat ? '3px solid #1976d2' : '2px solid #ccc',
                                            background: category === cat ? '#e3f2fd' : '#fff',
                                            padding: '8px',
                                            marginBottom: '4px',
                                            transition: 'border 0.15s'
                                        }}
                                        title={cat.charAt(0).toUpperCase() + cat.slice(1)}
                                    >
                                        {clothingIcons[cat]}
                                    </span>
                                    <span style={{ fontSize: '0.95em', color: '#444' }}>
                                        {cat.charAt(0).toUpperCase() + cat.slice(1)}
                                    </span>
                                </label>
                            ))}
                        </div>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="subCategory">Sub category</label>
                        <input type="text" className="form-control" name="subCategory" id="subCategory" placeholder="Sub category" value={subCategory} onChange={(e)=>{setSubCategory(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="color">Color</label>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginTop: '6px' }}>
                            {basicColors.map(c => (
                                <label key={c} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' }}>
                                    <input
                                        type="radio"
                                        name="color"
                                        value={c}
                                        checked={colorOption === c}
                                        onChange={() => setColorOption(c)}
                                        style={{ display: 'none' }}
                                    />
                                    <span
                                        style={{
                                            width: 28,
                                            height: 28,
                                            borderRadius: '6px',
                                            border: colorOption === c ? '3px solid #1976d2' : '2px solid #ccc',
                                            background: c.toLowerCase(),
                                            display: 'inline-block',
                                            marginBottom: 2,
                                            boxShadow: colorOption === c ? '0 0 0 2px #90caf9' : undefined,
                                            transition: 'border 0.15s'
                                        }}
                                        title={c}
                                    />
                                    <span style={{ fontSize: '0.85em', color: '#444' }}>{c}</span>
                                </label>
                            ))}
                            <label style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' }}>
                                <input
                                    type="radio"
                                    name="color"
                                    value="Other"
                                    checked={colorOption === "Other"}
                                    onChange={() => setColorOption("Other")}
                                    style={{ display: 'none' }}
                                />
                                <span
                                    style={{
                                        width: 28,
                                        height: 28,
                                        borderRadius: '6px',
                                        border: colorOption === "Other" ? '3px solid #1976d2' : '2px dashed #aaa',
                                        background: '#fff',
                                        display: 'inline-block',
                                        marginBottom: 2,
                                        position: 'relative'
                                    }}
                                    title="Other"
                                >
                                    <span style={{
                                        position: 'absolute',
                                        left: '50%',
                                        top: '50%',
                                        transform: 'translate(-50%, -50%)',
                                        fontSize: '1.2em',
                                        color: '#888'
                                    }}>?</span>
                                </span>
                                <span style={{ fontSize: '0.85em', color: '#444' }}>Other</span>
                            </label>
                        </div>
                        {colorOption === "Other" && (
                            <input
                                type="text"
                                className="form-control mt-2"
                                name="customColor"
                                id="customColor"
                                placeholder="Enter custom color"
                                value={customColor}
                                onChange={e => setCustomColor(e.target.value)}
                                required
                            />
                        )}
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="size">Size</label>
                        <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginTop: '6px' }}>
                            {fixedSizes.map(s => (
                                <label key={s} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' }}>
                                    <input
                                        type="radio"
                                        name="size"
                                        value={s}
                                        checked={sizeOption === s}
                                        onChange={() => setSizeOption(s)}
                                        style={{ display: 'none' }}
                                    />
                                    <span
                                        style={{
                                            minWidth: 36,
                                            minHeight: 28,
                                            padding: '4px 10px',
                                            borderRadius: '6px',
                                            border: sizeOption === s ? '3px solid #1976d2' : '2px solid #ccc',
                                            background: sizeOption === s ? '#e3f2fd' : '#fff',
                                            display: 'inline-block',
                                            marginBottom: 2,
                                            textAlign: 'center',
                                            fontWeight: 500,
                                            fontSize: '1em',
                                            boxShadow: sizeOption === s ? '0 0 0 2px #90caf9' : undefined,
                                            transition: 'border 0.15s'
                                        }}
                                    >{s}</span>
                                </label>
                            ))}
                            <label style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', cursor: 'pointer' }}>
                                <input
                                    type="radio"
                                    name="size"
                                    value="Other"
                                    checked={sizeOption === "Other"}
                                    onChange={() => setSizeOption("Other")}
                                    style={{ display: 'none' }}
                                />
                                <span
                                    style={{
                                        minWidth: 36,
                                        minHeight: 28,
                                        padding: '4px 10px',
                                        borderRadius: '6px',
                                        border: sizeOption === "Other" ? '3px solid #1976d2' : '2px dashed #aaa',
                                        background: '#fff',
                                        display: 'inline-block',
                                        marginBottom: 2,
                                        textAlign: 'center',
                                        fontWeight: 500,
                                        fontSize: '1em',
                                        position: 'relative'
                                    }}
                                >?</span>
                                <span style={{ fontSize: '0.85em', color: '#444' }}>Other</span>
                            </label>
                        </div>
                        {sizeOption === "Other" && (
                            <input
                                type="text"
                                className="form-control mt-2"
                                name="customSize"
                                id="customSize"
                                placeholder="Enter custom size"
                                value={customSize}
                                onChange={e => setCustomSize(e.target.value)}
                                required
                            />
                        )}
                    </div>
                    <div className="mb-3">
                        <label className="form-label" htmlFor="notes">Notes</label>
                        <input type="text" className="form-control" name="notes" id="notes" placeholder="Notes" value={notes} onChange={(e)=>{setNotes(e.target.value)}}/>
                    </div>
                    <div className="mb-3">
                        <label className="form-label">Choose a season:</label>
                        <div style={{ display: 'flex', gap: '16px', marginTop: '6px', flexWrap: 'wrap' }}>
                            {seasons.map(seasonOption => (
                                <label
                                    key={seasonOption}
                                    style={{
                                        display: 'flex',
                                        flexDirection: 'column',
                                        alignItems: 'center',
                                        cursor: 'pointer'
                                    }}
                                >
                                    <input
                                        type="radio"
                                        name="season"
                                        value={seasonOption}
                                        checked={season === seasonOption}
                                        onChange={() => setSeason(seasonOption)}
                                        style={{ display: 'none' }}
                                    />
                                    <span
                                        style={{
                                            fontSize: '2em',
                                            borderRadius: '8px',
                                            border: season === seasonOption ? '3px solid #1976d2' : '2px solid #ccc',
                                            background: season === seasonOption ? '#e3f2fd' : '#fff',
                                            padding: '8px',
                                            marginBottom: '4px',
                                            transition: 'border 0.15s'
                                        }}
                                        title={seasonOption.charAt(0).toUpperCase() + seasonOption.slice(1)}
                                    >
                                        {seasonIcons[seasonOption] || "🌀"}
                                    </span>
                                    <span style={{ fontSize: '0.95em', color: '#444' }}>
                                        {seasonOption.charAt(0).toUpperCase() + seasonOption.slice(1)}
                                    </span>
                                </label>
                            ))}
                        </div>
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