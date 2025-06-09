import { useEffect, useState, useContext } from 'react';
import { Link } from 'react-router-dom';
import '../ClothingGrid.css';
import { UserContext } from '../userContext';

function OutfitList() {
  const [outfits, setOutfits] = useState([]);
  const [loading, setLoading] = useState(true);
  const userContext = useContext(UserContext);

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

  if (loading) return <div>Loading...</div>;
  if (!outfits.length) return <div>No outfits found.</div>;

  return (
    <div>
      <h2>Outfits</h2>
      <div className="outfits-grid">
        {outfits.map(outfit => (
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
        ))}
      </div>
    </div>
  );
}

export default OutfitList;