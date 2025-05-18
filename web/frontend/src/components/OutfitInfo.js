import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';

function OutfitInfo() {
  const { id } = useParams();
  const [outfit, setOutfit] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchOutfit() {
      const res = await fetch(`/api/outfits/${id}`, { credentials: 'include' });
      const data = await res.json();
      setOutfit(data);
      setLoading(false);
    }
    fetchOutfit();
  }, [id]);

  if (loading) return <div>Loading...</div>;
  if (!outfit || outfit.message) return <div>Outfit not found.</div>;

  return (
    <div>
      <h2>{outfit.name}</h2>
      <div>
        <h4>Outfit Images:</h4>
        <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
          {outfit.images && outfit.images.length > 0 ? (
            outfit.images.map((img, idx) => (
              <img
                key={idx}
                src={img.startsWith('/images/') ? img : `/images/${img}`}
                alt={`Outfit item ${idx + 1}`}
                style={{ width: '100px', height: '100px', objectFit: 'cover' }}
              />
            ))
          ) : (
            <span>No images</span>
          )}
        </div>
      </div>
      <div>
        <h4>Clothing Items in this Outfit:</h4>
        <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
          {outfit.items && outfit.items.length > 0 ? (
            outfit.items.map((entry, idx) =>
              entry.item ? (
                <div key={entry.item._id || idx} style={{ textAlign: 'center' }}>
                  <img
                    src={
                      entry.item.imageUrl?.startsWith('/images/')
                        ? entry.item.imageUrl
                        : `/images/${entry.item.imageUrl}`
                    }
                    alt={entry.item.name}
                    style={{ width: '80px', height: '80px', objectFit: 'cover' }}
                  />
                  <div>{entry.item.name}</div>
                  <div style={{ fontSize: '0.8em', color: '#666' }}>{entry.position}</div>
                </div>
              ) : null
            )
          ) : (
            <span>No clothing items</span>
          )}
        </div>
      </div>
      <div style={{ marginTop: '20px' }}>
        <strong>Season:</strong> {outfit.season && outfit.season.join(', ')}
        <br />
        <strong>Occasion:</strong> {outfit.occasion}
      </div>
    </div>
  );
}

export default OutfitInfo;