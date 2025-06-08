import { useEffect, useState, useContext } from 'react';
import { Navigate } from 'react-router-dom';
import { UserContext } from '../userContext';
import '../FormAndStoreCard.css';

const SEASON_OPTIONS = ['spring', 'summer', 'fall', 'winter', 'all'];
const OCCASION_OPTIONS = ['casual', 'formal', 'sport', 'party', 'work', 'other'];

function AddOutfit() {
  const [name, setName] = useState('');
  const [selectedItems, setSelectedItems] = useState([]);
  const [clothes, setClothes] = useState([]);
  const [uploaded, setUploaded] = useState(false);
  const [season, setSeason] = useState([]);
  const [occasion, setOccasion] = useState('');
  const userContext = useContext(UserContext);
  //console.log(userContext);
  //console.log("b");

  useEffect(() => {
    async function fetchClothes() {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/clothing?mine=true', { 
          method: 'GET',
          credentials: 'include',
          headers: token ? { Authorization: `Bearer ${token}` } : {},
       });
      const data = await res.json();
      setClothes(data);
    }
    fetchClothes();
  }, []);

  function toggleItem(item) {
    setSelectedItems(prev =>
      prev.includes(item._id)
        ? prev.filter(id => id !== item._id)
        : [...prev, item._id]
    );
  }

  function toggleSeason(option) {
    setSeason(prev =>
      prev.includes(option)
        ? prev.filter(s => s !== option)
        : [...prev, option]
    );
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const selectedImages = clothes
      .filter(item => selectedItems.includes(item._id))
      .map(item => item.imageUrl);

    const body = {
      name,
      items: selectedItems.map(id => ({ item: id })),
      images: selectedImages,
    };
    if (season.length > 0) body.season = season;
    if (occasion) body.occasion = occasion;

    const res = await fetch('/api/outfits', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    if (res.ok) setUploaded(true);
  }

  if (uploaded) return <Navigate replace to="/" />;

  return (
    <div className="form-card-container">
      <div className="form-card">
        <h2>Add Outfit</h2>
        <form onSubmit={handleSubmit}>
          <div className="mb-3">
            <label className="form-label">Outfit Name:</label>
            <input
              type="text"
              className="form-control"
              value={name}
              onChange={e => setName(e.target.value)}
              required
            />
          </div>
          <div className="mb-3">
            <label className="form-label">Season:</label>
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
              {SEASON_OPTIONS.map(option => (
                <label key={option} style={{ cursor: 'pointer', marginRight: 10 }}>
                  <input
                    type="checkbox"
                    checked={season.includes(option)}
                    onChange={() => toggleSeason(option)}
                  />{' '}
                  {option}
                </label>
              ))}
            </div>
          </div>
          <div className="mb-3">
            <label className="form-label">Occasion:</label>
            <select
              className="form-control"
              value={occasion}
              onChange={e => setOccasion(e.target.value)}
            >
              <option value="">Select occasion</option>
              {OCCASION_OPTIONS.map(opt => (
                <option key={opt} value={opt}>{opt}</option>
              ))}
            </select>
          </div>
          <div className="mb-3">
            <label className="form-label">Select clothing items for this outfit:</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
              {clothes
              .filter(item => {
                const itemUserId = typeof item.user === 'object' && item.user !== null
                  ? item.user._id
                  : item.user;
                return itemUserId && userContext.user._id && String(itemUserId) === String(userContext.user._id);
              })
              .map(item => (
                <div
                  key={item._id}
                  style={{
                    border: selectedItems.includes(item._id) ? '2px solid #1976d2' : '1px solid #ccc',
                    padding: '5px',
                    cursor: 'pointer',
                    textAlign: 'center',
                    borderRadius: '8px',
                    background: selectedItems.includes(item._id) ? '#e3eafc' : '#fff'
                  }}
                  onClick={() => toggleItem(item)}
                >
                  <img
                    src={item.imageUrl?.startsWith('/images/')
                      ? item.imageUrl
                      : `/images/${item.imageUrl}`}
                    alt={item.name}
                    style={{ width: '80px', height: '80px', objectFit: 'cover', borderRadius: '6px' }}
                  />
                  <div>{item.name}</div>
                </div>
              ))}
            </div>
          </div>
          <button
            type="submit"
            className="btn btn-primary w-100 mt-3"
            disabled={!name || selectedItems.length === 0}
          >
            Create Outfit
          </button>
        </form>
        <div>
          <h4>Selected items preview:</h4>
          <div style={{ display: 'flex', gap: '10px' }}>
            {clothes
              .filter(item => selectedItems.includes(item._id))
              .map(item => (
                <img
                  key={item._id}
                  src={item.imageUrl?.startsWith('/images/')
                    ? item.imageUrl
                    : `/images/${item.imageUrl}`}
                  alt={item.name}
                  style={{ width: '60px', height: '60px', objectFit: 'cover', borderRadius: '6px' }}
                />
              ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default AddOutfit;