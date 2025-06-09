import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import '../ClothingGrid.css';

function OutfitList() {
  const [outfits, setOutfits] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    async function fetchOutfits() {
      const token = localStorage.getItem('token');
      const res = await fetch('/api/outfits', {
        credentials: 'include',
        headers: token ? { Authorization: `Bearer ${token}` } : {},
      });
      const data = await res.json();
      setOutfits(data);
      setLoading(false);
    }
    fetchOutfits();
  }, []);

  return (
    <div style={{ position: 'relative' }}>
      <h2>Outfits</h2>
      <div className="outfits-grid">
        {loading ? (
          <div>Loading...</div>
        ) : outfits.length === 0 ? (
          <div style={{ textAlign: 'center', width: '100%', marginTop: 40 }}>
            No outfits found.
          </div>
        ) : (
          outfits.map(outfit => (
            <Link
              to={`/outfit/${outfit._id}`}
              key={outfit._id}
              className="outfit-card"
              style={{ textDecoration: 'none' }}
            >
              <div className="outfit-card-img-frame">
                {outfit.imageUrl ? (
                  <img
                    className="outfit-card-img-single"
                    src={
                      outfit.imageUrl.startsWith('/images/')
                        ? outfit.imageUrl
                        : `/images/${outfit.imageUrl}`
                    }
                    alt="Outfit"
                  />
                ) : (
                  <div className="outfit-card-img-grid">
                    {/* ...handle multiple images or fallback... */}
                  </div>
                )}
              </div>
              <div className="outfit-card-title">{outfit.name}</div>
            </Link>
          ))
        )}
      </div>
      {/* Floating Add Outfit Button - always visible */}
      <button
        className="add-clothing-fab"
        onClick={() => navigate('/addOutfit')}
        aria-label="Add Outfit"
      >
        +
      </button>
    </div>
  );
}

export default OutfitList;