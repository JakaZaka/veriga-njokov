import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import '../ClothingGrid.css';

function OutfitList() {
  const [outfits, setOutfits] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchOutfits() {
      const res = await fetch('/api/outfits', { credentials: 'include' });
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
              {outfit.images && outfit.images.length === 1 ? (
                <img
                  className="outfit-card-img-single"
                  src={outfit.images[0].startsWith('/images/') ? outfit.images[0] : `/images/${outfit.images[0]}`}
                  alt="Outfit"
                />
              ) : (
                <div className="outfit-card-img-grid">
                  {(outfit.images && outfit.images.length > 0
                    ? outfit.images.slice(0, 4)
                    : [null, null, null, null]
                  ).map((img, idx) =>
                    img ? (
                      <img
                        key={idx}
                        src={img.startsWith('/images/') ? img : `/images/${img}`}
                        alt={`Outfit piece ${idx + 1}`}
                      />
                    ) : (
                      <div
                        key={idx}
                        style={{
                          width: '100%',
                          height: '100%',
                          background: '#e0e0e0',
                          borderRadius: 8,
                        }}
                      />
                    )
                  )}
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