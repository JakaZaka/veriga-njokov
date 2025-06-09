import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import '../DetailsCard.css';

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
    <div className="details-card">
      <div className="details-title">{outfit.name}</div>
      <div className="details-section">
        <h4>Outfit Images:</h4>
       <div className="details-img-list">
        {outfit.imageUrl ? (
          <img
            src={
              outfit.imageUrl.startsWith('/images/')
                ? outfit.imageUrl
                : `/images/${outfit.imageUrl}`
            }
            alt="Outfit"
          />
        ) : (
          outfit.images.map((img, idx) => (
            <img
              key={idx}
              src={img.startsWith('/images/') ? img : `/images/${img}`}
              alt={`Outfit item ${idx + 1}`}
            />
          ))
        )}
    </div>
      </div>
      <div className="details-section">
        <h4>Clothing Items in this Outfit:</h4>
        <div className="details-items-list">
          {outfit.items && outfit.items.length > 0 ? (
            outfit.items.map((entry, idx) =>
              entry.item ? (
                <div key={entry.item._id || idx} className="details-item-card">
                  <img
                    src={
                      entry.item.imageUrl?.startsWith('/images/')
                        ? entry.item.imageUrl
                        : `/images/${entry.item.imageUrl}`
                    }
                    alt={entry.item.name}
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
      <div className="details-section">
        <div className="details-meta">
          <strong>Season:</strong> {outfit.season && outfit.season.join(', ')}
        </div>
        <div className="details-meta">
          <strong>Occasion:</strong> {outfit.occasion}
        </div>
      </div>
    </div>
  );
}

export default OutfitInfo;