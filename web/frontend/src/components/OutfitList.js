import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

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
      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '20px' }}>
        {outfits.map(outfit => (
          <div key={outfit._id} style={{ border: '1px solid #ccc', padding: 10, borderRadius: 8, width: 200 }}>
            <Link to={`/outfit/${outfit._id}`}>
              <h4>{outfit.name}</h4>
              {outfit.images && outfit.images.length > 0 && (
                <div
                  style={{
                    display: 'grid',
                    gridTemplateColumns: 'repeat(2, 1fr)',
                    gridTemplateRows: 'repeat(2, 1fr)',
                    gap: '2px',
                    width: '100%',
                    height: 120,
                    overflow: 'hidden',
                  }}
                >
                  {outfit.images.slice(0, 4).map((img, idx) => (
                    <img
                      key={idx}
                      src={img.startsWith('/images/') ? img : `/images/${img}`}
                      alt={`Outfit piece ${idx + 1}`}
                      style={{
                        width: '100%',
                        height: '60px',
                        objectFit: 'cover',
                        borderRadius: 2,
                        background: '#eee'
                      }}
                    />
                  ))}
                </div>
              )}
            </Link>
          </div>
        ))}
      </div>
    </div>
  );
}

export default OutfitList;